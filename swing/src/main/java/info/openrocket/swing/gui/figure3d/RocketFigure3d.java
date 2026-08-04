package info.openrocket.swing.gui.figure3d;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.StateChangeListener;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.SolidColorBackground;
import info.openrocket.swing.gui.figure3d.scene.graph.Camera;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;
import info.openrocket.swing.gui.figure3d.scene.orchestration.Scene3DOrchestrator;
import info.openrocket.swing.gui.figure3d.scene.properties.DisplaySettings;
import info.openrocket.swing.gui.figure3d.ui.GLScenePanel;
import info.openrocket.swing.gui.figure3d.ui.HUDPanel;
import info.openrocket.swing.gui.figure3d.utils.ColorUtils;
import info.openrocket.swing.gui.figureelements.RocketInfo;
import info.openrocket.swing.gui.figureelements.RocketInfoContextHelper;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.util.GUIUtil;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.SecondaryLoop;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.EventObject;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Swing adapter for the new GLScenePanel renderer.
 *
 * <p>The previous adapter introduced extra render threads and schedulers on top of
 * AWTGLCanvas. The embedded Swing path uses a shared scheduler so all canvases
 * serialize native drawing-surface access.</p>
 */
public class RocketFigure3d extends JPanel implements SharedCanvasRenderScheduler.Client {

	private static final Logger log = LoggerFactory.getLogger(RocketFigure3d.class);
	// Single shared render thread — AWTGLCanvas.render() acquires a JAWT drawing-surface
	// lock that must be serialized across canvases on most platforms. Per-window threads
	// cause silent failures or deadlocks on the second canvas. We compensate for the old
	// round-robin frame-rate degradation by rendering *all* dirty windows per tick instead
	// of one window per tick, so N idle windows cost nothing and N active windows all
	// receive their full 16 ms cadence.
	private static final SharedCanvasRenderScheduler RENDER_SCHEDULER = SharedCanvasRenderScheduler.getInstance();

	public static final int TYPE_FIGURE = 2;
	public static final int TYPE_UNFINISHED = 3;
	public static final int TYPE_FINISHED = 4;

	private static Color backgroundColor;

	static {
		initColors();
	}

	public interface ComponentSelectionListener {
		void componentClicked(RocketComponent[] clicked, MouseEvent event);
	}

	private final OpenRocketDocument document;
	private final HUDPanel hudPanel;
	private final RocketInfo rocketInfo;
	private final boolean enable3d;
	private final List<ComponentSelectionListener> selectionListeners = new CopyOnWriteArrayList<>();
	private final List<StateChangeListener> changeListeners = new CopyOnWriteArrayList<>();

	private volatile GLScenePanel glScenePanel;
	private volatile boolean renderingEnabled = false;
	private volatile boolean disposed = false;
	private volatile boolean selectionBridgeInstalled = false;
	private RocketComponent[] pendingSelection;
	private boolean glFailureLogged = false;
	private boolean glNativesUnavailable = false;
	private Color customBackgroundColor = null;
	private volatile int currentType = TYPE_FINISHED;
	private volatile boolean drawCarets = true;
	private volatile double zoomScale = 1.0;
	private volatile boolean zoomFitting = true;
	private volatile Double pendingZoomScale = null;
	private volatile boolean panModeEnabled = false;
	// Latest CG/CP handed down by the owning panel, kept so they can be re-applied to a
	// scene that did not exist yet when they arrived, or that has since been rebuilt.
	private volatile CoordinateIF latestCG = null;
	private volatile CoordinateIF latestCP = null;
	// Demand-driven rendering: the background thread only calls renderFrame() when this is true.
	// Starts true so the first frame renders immediately without an explicit trigger.
	private volatile boolean dirty = true;
	private final AtomicReference<GLScenePanel> pendingContextResetRebuild = new AtomicReference<>();
	// Camera pose captured from a canvas that is about to be rebuilt, so the new
	// canvas resumes at the same view instead of resetting to the default pose.
	private final AtomicReference<CameraRestoreState> pendingCameraRestore = new AtomicReference<>();

	private static final class CameraRestoreState {
		private final float angleX;
		private final float angleY;
		private final float distance;
		private final float fieldOfView;
		private final Vector3f centerOfInterest;

		private CameraRestoreState(Camera camera) {
			this.angleX = camera.getAngleX();
			this.angleY = camera.getAngleY();
			this.distance = camera.getDistance();
			this.fieldOfView = camera.getFieldOfView();
			this.centerOfInterest = new Vector3f(camera.getCenterOfInterest());
		}
	}

	public RocketFigure3d(OpenRocketDocument document) {
		this.document = document;
		this.rocketInfo = new RocketInfo(document.getRocket().getSelectedConfiguration());
		this.rocketInfo.set3DView(true);
		this.hudPanel = new HUDPanel(document, rocketInfo);
		this.enable3d = is3dEnabled();
		setLayout(new BorderLayout());
		setBackground(getBackgroundColor());
	}

	public static boolean is3dEnabled() {
		if (System.getProperty("openrocket.3d.disable") != null) {
			return false;
		}
		return Application.getPreferences().getBoolean(ApplicationPreferences.OPENGL_ENABLED, true);
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(RocketFigure3d::updateColors);
	}

	public static void updateColors() {
		backgroundColor = GUIUtil.getUITheme().getBackgroundColor();
	}

	private void ensureCanvasCreatedOnEdt() {
		if (!enable3d || glNativesUnavailable) {
			return;
		}
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(this::ensureCanvasCreatedOnEdt);
			return;
		}
		if (glScenePanel != null || disposed) {
			return;
		}
		GLScenePanel panel;
		try {
			panel = new GLScenePanel(document.getRocket(), hudPanel);
		} catch (UnsatisfiedLinkError | ExceptionInInitializerError e) {
			glNativesUnavailable = true;
			log.warn("3D view disabled: LWJGL native libraries not available for {}/{}. " +
					"Use a platform-specific build or install the appropriate natives.",
					System.getProperty("os.name"), System.getProperty("os.arch"), e);
			return;
		}
		panel.setPanModeEnabled(panModeEnabled);
		// Mark dirty whenever the user interacts (drag, scroll, resize, key) so the
		// background thread renders on demand rather than unconditionally.
		panel.setRenderActivityCallback(this::markDirty);
		panel.setRenderRequestCallback(this::requestRenderNow);
		panel.setGraphicsResetCallback(() -> requestContextResetRebuild(panel));
		panel.setInitializationHook(orchestrator -> {
			applyViewType(orchestrator, currentType);
			applyCaretVisibility(orchestrator, drawCarets);
			orchestrator.setCaretPositions(latestCG, latestCP);
			Double requestedZoomScale = pendingZoomScale;
			if (requestedZoomScale != null) {
				orchestrator.getCameraController().setZoomScale(requestedZoomScale);
			}
			CameraRestoreState cameraRestore = pendingCameraRestore.getAndSet(null);
			if (cameraRestore != null) {
				restoreCamera(orchestrator, cameraRestore);
			}
			updateZoomState(orchestrator);
		});
		glScenePanel = panel;
		add(panel, BorderLayout.CENTER);
		applyBackgroundColor(panel);
		revalidate();
		repaint();
	}

	private void rebuildCanvasAfterGraphicsReset(GLScenePanel failedPanel) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> rebuildCanvasAfterGraphicsReset(failedPanel));
			return;
		}
		if (disposed || !renderingEnabled) {
			return;
		}
		if (glScenePanel != failedPanel) {
			return;
		}
		log.info("Rebuilding 3D canvas after a graphics context reset");
		captureCameraForRestore(failedPanel);
		failedPanel.setRenderActivityCallback(null);
		failedPanel.setRenderRequestCallback(null);
		failedPanel.setGraphicsResetCallback(null);
		remove(failedPanel);
		glScenePanel = null;
		selectionBridgeInstalled = false;
		revalidate();
		repaint();
		failedPanel.cleanup();

		ensureCanvasCreatedOnEdt();
		GLScenePanel panel = glScenePanel;
		if (panel != null) {
			applyBackgroundColor(panel);
		}
		requestRenderNow();
		scheduleStartupWatchdog();
	}

	private void maybeInstallSelectionBridge(GLScenePanel panel) {
		if (selectionBridgeInstalled || panel.glInitFailed || !panel.awaitInitialized(0)) {
			return;
		}
		applyBackgroundColor(panel);
		panel.addSceneSelectionListener(selection -> {
			MouseEvent event = panel.consumePendingSelectionClickEvent();
			if (event == null) {
				return;
			}
			RocketComponent[] components = selection.stream()
					.map(SceneObject::getRocketComponent)
					.filter(rc -> rc != null)
					.toArray(RocketComponent[]::new);
			Runnable notifyListeners = () -> {
				for (ComponentSelectionListener listener : selectionListeners) {
					listener.componentClicked(components, event);
				}
			};
			if (SwingUtilities.isEventDispatchThread()) {
				notifyListeners.run();
			} else {
				SwingUtilities.invokeLater(notifyListeners);
			}
		});
		selectionBridgeInstalled = true;
		if (pendingSelection != null) {
			RocketComponent[] selection = pendingSelection;
			pendingSelection = null;
			setSelection(selection);
		}
	}

	private void renderFrame() {
		if (!enable3d || !renderingEnabled || disposed) {
			return;
		}
		ensureCanvasCreatedOnEdt();
		GLScenePanel panel = glScenePanel;
		if (panel == null) {
			// Canvas not created yet — keep dirty so the scheduler retries each tick
			// until the EDT creates it.
			markDirty();
			return;
		}
		if (!panel.isDisplayable() || !panel.isShowing() || panel.getWidth() <= 0 || panel.getHeight() <= 0) {
			// Panel not yet laid out or visible. Keep dirty only for the startup period
			// (before the first frame), or while a resize is still outstanding: dropping the
			// dirty flag there would leave the canvas showing a frame drawn for the previous
			// size, with the newly exposed area never painted. Once a frame has completed and
			// no resize is pending the panel is genuinely off-screen (minimized etc.) and we
			// should not spin.
			if (!panel.hasCompletedFrame() || panel.hasPendingResize()) {
				markDirty();
			}
			return;
		}

		panel.render();
		if (processPendingContextResetRebuild(panel)) {
			return;
		}
		if (panel.glInitFailed) {
			if (!glFailureLogged) {
				log.error("GL initialization/rendering failed in RocketFigure3d");
				glFailureLogged = true;
			}
			stopRendering();
			return;
		}
		maybeInstallSelectionBridge(panel);
		updateZoomState(panel.getScene3DOrchestrator());
	}

	private void captureCameraForRestore(GLScenePanel failedPanel) {
		Scene3DOrchestrator orchestrator = failedPanel.getScene3DOrchestrator();
		if (orchestrator == null || orchestrator.getCameraController() == null) {
			return;
		}
		Camera camera = orchestrator.getCameraController().getCamera();
		if (camera != null) {
			pendingCameraRestore.set(new CameraRestoreState(camera));
		}
	}

	private void restoreCamera(Scene3DOrchestrator orchestrator, CameraRestoreState state) {
		Camera camera = orchestrator.getCameraController().getCamera();
		if (camera == null) {
			return;
		}
		camera.setCenterOfInterest(state.centerOfInterest);
		camera.setAngleX(state.angleX);
		camera.setAngleY(state.angleY);
		camera.setFieldOfView(state.fieldOfView);
		camera.setDistance(state.distance);
		camera.update();
	}

	private void requestContextResetRebuild(GLScenePanel failedPanel) {
		pendingContextResetRebuild.compareAndSet(null, failedPanel);
	}

	private boolean processPendingContextResetRebuild(GLScenePanel panel) {
		if (!pendingContextResetRebuild.compareAndSet(panel, null)) {
			return false;
		}
		if (SwingUtilities.isEventDispatchThread()) {
			rebuildCanvasAfterGraphicsReset(panel);
		} else {
			try {
				SwingUtilities.invokeAndWait(() -> rebuildCanvasAfterGraphicsReset(panel));
			} catch (Exception e) {
				log.warn("Failed to rebuild the 3D canvas after a graphics reset", e);
			}
		}
		return true;
	}

	/** Marks this view as needing a render on the next scheduler tick. */
	void markDirty() {
		dirty = true;
	}

	@Override
	public boolean isRenderActive() {
		return enable3d && renderingEnabled && !disposed;
	}

	@Override
	public boolean shouldRenderOnTick() {
		if (!dirty) {
			return false;
		}
		// Clear before rendering so marks set during renderFrame() survive.
		dirty = false;
		return true;
	}

	@Override
	public void renderScheduledFrame() {
		renderFrame();
	}

	@Override
	public String getRenderDebugName() {
		return "RocketFigure3d";
	}

	private void requestRenderNow() {
		if (!enable3d) {
			return;
		}
		markDirty();
		RENDER_SCHEDULER.requestImmediate(this);
	}

	/**
	 * Called by RocketPanel when switching to 3D mode.
	 */
	public void startRendering() {
		if (!enable3d || disposed) {
			return;
		}
		renderingEnabled = true;
		glFailureLogged = false;
		SwingUtilities.invokeLater(() -> {
			if (!renderingEnabled || disposed) {
				return;
			}
			ensureCanvasCreatedOnEdt();
			GLScenePanel panel = glScenePanel;
			if (panel != null) {
				applyBackgroundColor(panel);
			}
			RENDER_SCHEDULER.register(this);
			requestRenderNow();
			scheduleStartupWatchdog();
		});
	}

	/**
	 * Called by RocketPanel when switching back to 2D mode.
	 */
	public void stopRendering() {
		renderingEnabled = false;
		RENDER_SCHEDULER.unregister(this);
	}

	public void addComponentSelectionListener(ComponentSelectionListener listener) {
		if (listener != null) {
			selectionListeners.add(listener);
		}
	}

	public void addChangeListener(StateChangeListener listener) {
		if (listener != null) {
			changeListeners.add(listener);
		}
	}

	public void removeChangeListener(StateChangeListener listener) {
		changeListeners.remove(listener);
	}

	private Color getBackgroundColor() {
		return customBackgroundColor != null ? customBackgroundColor : backgroundColor;
	}

	public void setCustomBackgroundColor(Color color) {
		this.customBackgroundColor = color;
		setBackground(getBackgroundColor());
		GLScenePanel panel = glScenePanel;
		if (panel != null) {
			applyBackgroundColor(panel);
			panel.repaint();
		}
	}

	private void applyBackgroundColor(GLScenePanel panel) {
		markDirty();
		if (panel == null) {
			return;
		}
		Color color = getBackgroundColor();
		// A heavyweight Canvas may expose its AWT background while its native
		// drawable is being resized. Match it to the scene so this fallback can
		// never appear as a white flash.
		panel.setBackground(color);
		if (panel.glInitFailed || !panel.awaitInitialized(0)) {
			return;
		}
		Scene3DOrchestrator orchestrator = panel.getScene3DOrchestrator();
		if (orchestrator == null) {
			return;
		}
		float srgbR = color.getRed() / 255.0f;
		float srgbG = color.getGreen() / 255.0f;
		float srgbB = color.getBlue() / 255.0f;
		float alpha = color.getAlpha() / 255.0f;
		Vector4f linear = ColorUtils.srgbToLinear(new Vector4f(srgbR, srgbG, srgbB, alpha));
		orchestrator.enqueueGlTask(() -> {
			SceneView scene = orchestrator.getScene();
			if (scene != null) {
				scene.setBackground(new SolidColorBackground(linear.x, linear.y, linear.z, linear.w));
			}
		});
	}

	public void flushTextureCaches() {
		// Managed by Texture and renderer internals.
	}

	public void updateFigure() {
		markDirty();
		GLScenePanel panel = glScenePanel;
		if (panel != null) {
			panel.markHudForUpdate();
			panel.repaint();
		}
	}

	public void setCustomTextColor(Color color) {
		rocketInfo.setCustomTextColors(null, color);
		rocketInfo.set3DView(true);
		updateFigure();
	}

	// Compatibility methods expected by RocketPanel.

	public void setType(int type) {
		markDirty();
		currentType = type;
		GLScenePanel panel = glScenePanel;
		if (panel == null || panel.glInitFailed || !panel.awaitInitialized(0)) {
			return;
		}
		Scene3DOrchestrator orchestrator = panel.getScene3DOrchestrator();
		if (orchestrator == null) {
			return;
		}
		applyViewType(orchestrator, type);
		panel.markHudForUpdate();
		panel.repaint();
	}

	private void applyViewType(Scene3DOrchestrator orchestrator, int type) {
		DisplaySettings.RenderMode mode = switch (type) {
			case TYPE_FIGURE -> DisplaySettings.RenderMode.XRAY;
			case TYPE_UNFINISHED -> DisplaySettings.RenderMode.UNFINISHED;
			case TYPE_FINISHED -> DisplaySettings.RenderMode.FINISHED;
			default -> DisplaySettings.RenderMode.FINISHED;
		};
		orchestrator.enqueueGlTask(() -> {
			orchestrator.getRenderingConfiguration().getDisplay().setMode(mode);
		});
	}

	private void applyCaretVisibility(Scene3DOrchestrator orchestrator, boolean visible) {
		orchestrator.enqueueGlTask(() -> {
			orchestrator.getRenderingConfiguration().getVisualEffects().setCaretsVisible(visible);
			orchestrator.getRenderingConfiguration().notifyListeners();
		});
	}

	private void updateZoomState(Scene3DOrchestrator orchestrator) {
		if (orchestrator == null) {
			return;
		}
		double currentZoomScale = orchestrator.getCameraController().getZoomScale();
		boolean currentlyFitting = orchestrator.getCameraController().isZoomFitting();
		if (Math.abs(currentZoomScale - zoomScale) <= 0.001 && currentlyFitting == zoomFitting) {
			return;
		}
		zoomScale = currentZoomScale;
		zoomFitting = currentlyFitting;
		fireChangeEvent();
	}

	private void fireChangeEvent() {
		EventObject event = new EventObject(this);
		for (StateChangeListener listener : changeListeners) {
			listener.stateChanged(event);
		}
	}

	public void setDrawCarets(boolean draw) {
		markDirty();
		drawCarets = draw;
		hudPanel.setVisible(draw);
		GLScenePanel panel = glScenePanel;
		if (panel != null) {
			Scene3DOrchestrator orchestrator = panel.getScene3DOrchestrator();
			if (orchestrator != null) {
				applyCaretVisibility(orchestrator, draw);
			}
			panel.markHudForUpdate();
			panel.repaint();
		}
	}

	public void setDragRotationSensitivity(float sensitivity) {
		markDirty();
		GLScenePanel panel = glScenePanel;
		if (panel == null) {
			return;
		}
		Scene3DOrchestrator orchestrator = panel.getScene3DOrchestrator();
		if (orchestrator == null) {
			return;
		}
		orchestrator.enqueueGlTask(() -> {
			orchestrator.getRenderingConfiguration().getVisualEffects().setDragRotationSensitivity(sensitivity);
			orchestrator.getRenderingConfiguration().notifyListeners();
		});
		panel.repaint();
	}

	public void setPanModeEnabled(boolean enabled) {
		markDirty();
		panModeEnabled = enabled;
		hudPanel.setPanModeEnabled(enabled);
		GLScenePanel panel = glScenePanel;
		if (panel != null) {
			panel.setPanModeEnabled(enabled);
			panel.markHudForUpdate();
			panel.repaint();
		}
		requestRenderNow();
	}

	public double getZoomScale() {
		return zoomScale;
	}

	public boolean isZoomFitting() {
		return zoomFitting;
	}

	public void setZoomScale(double scale) {
		if (Double.isNaN(scale) || Double.isInfinite(scale) || scale <= 0.0) {
			return;
		}
		pendingZoomScale = scale;
		GLScenePanel panel = glScenePanel;
		if (panel == null || panel.glInitFailed || !panel.awaitInitialized(0)) {
			return;
		}
		Scene3DOrchestrator orchestrator = panel.getScene3DOrchestrator();
		if (orchestrator == null) {
			return;
		}
		orchestrator.enqueueGlTask(() -> orchestrator.getCameraController().setZoomScale(scale));
		requestRenderNow();
	}

	public void zoomToFit() {
		pendingZoomScale = 1.0;
		GLScenePanel panel = glScenePanel;
		if (panel == null || panel.glInitFailed || !panel.awaitInitialized(0)) {
			return;
		}
		Scene3DOrchestrator orchestrator = panel.getScene3DOrchestrator();
		if (orchestrator == null) {
			return;
		}
		orchestrator.enqueueGlTask(orchestrator::resetViewAndFocusOnRocket);
		requestRenderNow();
	}

	public void setCaretPositions(CoordinateIF cg, CoordinateIF cp) {
		markDirty();
		RocketInfoContextHelper.applyCgAndCp(rocketInfo, cg, cp);
		latestCG = cg;
		latestCP = cp;
		pushCaretPositions();
		GLScenePanel panel = glScenePanel;
		if (panel != null) {
			panel.markHudForUpdate();
		}
	}

	/**
	 * Hands the latest centre of gravity and centre of pressure to the scene, so the markers
	 * agree with the figures and the overlay beside them.
	 *
	 * <p>These arrive from the owning panel, which computes them under whatever flight
	 * conditions are in force — the Component Analysis window can override Mach, angle of
	 * attack and roll rate. The renderer has no way to know about those, so it must be told
	 * rather than left to work the positions out for itself.</p>
	 */
	private void pushCaretPositions() {
		GLScenePanel panel = glScenePanel;
		Scene3DOrchestrator orchestrator = panel == null ? null : panel.getScene3DOrchestrator();
		if (orchestrator == null) {
			// Applied by the initialization hook once the scene exists.
			return;
		}
		CoordinateIF cg = latestCG;
		CoordinateIF cp = latestCP;
		orchestrator.enqueueGlTask(() -> orchestrator.setCaretPositions(cg, cp));
	}

	public void clearRelativeExtra() {
		// Not implemented in the HUD overlay yet.
	}

	public void clearAbsoluteExtra() {
		// HUD already renders the shared RocketInfo instance.
	}

	public void addAbsoluteExtra(RocketInfo info) {
		// HUD already renders the shared RocketInfo instance.
	}

	public void setSelection(RocketComponent[] components) {
		markDirty();
		RocketComponent[] copy = components != null ? components.clone() : null;
		GLScenePanel panel = glScenePanel;
		if (panel == null || panel.glInitFailed || !panel.awaitInitialized(0)) {
			pendingSelection = copy;
			return;
		}
		Scene3DOrchestrator orchestrator = panel.getScene3DOrchestrator();
		if (orchestrator == null) {
			pendingSelection = copy;
			return;
		}
		orchestrator.enqueueGlTask(() -> {
			SceneView scene = orchestrator.getScene();
			if (scene == null) {
				return;
			}
			List<SceneObject> selectedObjects = scene.getObjects().stream()
					.filter(obj -> {
						RocketComponent rc = obj.getRocketComponent();
						if (rc == null || copy == null) {
							return false;
						}
						for (RocketComponent component : copy) {
							if (component == rc) {
								return true;
							}
						}
						return false;
					})
					.collect(Collectors.toList());
			scene.setSelection(selectedObjects);
		});
	}

	public Scene3DOrchestrator getSceneController() {
		if (!enable3d) {
			return null;
		}
		GLScenePanel panel = glScenePanel;
		return panel != null ? panel.getScene3DOrchestrator() : null;
	}

	public int getCanvasRenderCallCount() {
		GLScenePanel panel = glScenePanel;
		return panel != null ? panel.getRenderCallCount() : 0;
	}

	public int getCanvasPaintCallCount() {
		GLScenePanel panel = glScenePanel;
		return panel != null ? panel.getPaintCallCount() : 0;
	}

	public int getCanvasSwapCallCount() {
		GLScenePanel panel = glScenePanel;
		return panel != null ? panel.getSwapCallCount() : 0;
	}

	public boolean hasCompletedCanvasFrame() {
		GLScenePanel panel = glScenePanel;
		return panel != null && panel.hasCompletedFrame();
	}

	public String getCanvasDebugState() {
		if (!enable3d) {
			return "3d-disabled";
		}
		GLScenePanel panel = glScenePanel;
		return panel != null ? panel.getDebugStateSummary() : "panel=null";
	}

	public BufferedImage captureImage() {
		if (!enable3d) {
			return null;
		}
		GLScenePanel panel = glScenePanel;
		if (panel == null || panel.glInitFailed || !panel.awaitInitialized(0)) {
			return null;
		}

		AtomicReference<BufferedImage> result = new AtomicReference<>();
		if (SwingUtilities.isEventDispatchThread()) {
			SecondaryLoop loop = Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();
			panel.requestImageCapture(false, image -> {
				result.set(image);
				loop.exit();
			});
			requestRenderNow();
			loop.enter();
			return compositeHudOverlay(result.get(), panel);
		}

		CountDownLatch latch = new CountDownLatch(1);
		panel.requestImageCapture(false, image -> {
			result.set(image);
			latch.countDown();
		});
		requestRenderNow();
		try {
			if (!latch.await(2, TimeUnit.SECONDS)) {
				log.warn("Timed out capturing 3D image");
				return null;
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		}
		return compositeHudOverlay(result.get(), panel);
	}

	private BufferedImage compositeHudOverlay(BufferedImage image, GLScenePanel panel) {
		if (image == null || panel == null || hudPanel == null || !hudPanel.isVisible()) {
			return image;
		}

		if (SwingUtilities.isEventDispatchThread()) {
			paintHudIntoImage(image, panel);
			return image;
		}

		try {
			SwingUtilities.invokeAndWait(() -> paintHudIntoImage(image, panel));
		} catch (Exception e) {
			log.warn("Failed to paint HUD into captured 3D image", e);
		}
		return image;
	}

	private void paintHudIntoImage(BufferedImage image, GLScenePanel panel) {
		if (image == null || panel == null || hudPanel == null || !hudPanel.isVisible()) {
			return;
		}

		int logicalWidth = Math.max(1, panel.getWidth());
		int logicalHeight = Math.max(1, panel.getHeight());
		double scaleX = image.getWidth() / (double) logicalWidth;
		double scaleY = image.getHeight() / (double) logicalHeight;

		Graphics2D g2 = image.createGraphics();
		try {
			g2.scale(scaleX, scaleY);
			hudPanel.setBounds(0, 0, logicalWidth, logicalHeight);
			hudPanel.paint(g2);
		} finally {
			g2.dispose();
		}
	}

	public void cleanup() {
		stopRendering();
		disposed = true;
		GLScenePanel panel = glScenePanel;
		glScenePanel = null;
		selectionBridgeInstalled = false;
		pendingSelection = null;
		pendingContextResetRebuild.set(null);
		pendingCameraRestore.set(null);
		if (panel != null) {
			panel.setGraphicsResetCallback(null);
			panel.cleanup();
		}
	}

	@Override
	public void removeNotify() {
		// Release GL resources while the child canvas is still displayable. The
		// subsequent AWT removeNotify() owns native context and peer disposal.
		cleanup();
		super.removeNotify();
	}

	private void scheduleStartupWatchdog() {
		int[] delaysMs = {750, 1500, 3000};
		for (int delayMs : delaysMs) {
			Timer timer = new Timer(delayMs, e -> runStartupWatchdog(delayMs));
			timer.setRepeats(false);
			timer.start();
		}
	}

	private void runStartupWatchdog(int delayMs) {
		if (!enable3d || !renderingEnabled || disposed) {
			return;
		}
		ensureCanvasCreatedOnEdt();
		GLScenePanel panel = glScenePanel;
		if (panel == null) {
			return;
		}
		if (panel.hasCompletedFrame()) {
			return;
		}
		revalidate();
		repaint();
		panel.revalidate();
		panel.repaint();
		// Re-assert dirty on all platforms: with demand-driven rendering the background
		// thread only fires when dirty=true, so the watchdog must re-mark it if the
		// initial render was dropped (e.g. panel not yet showing when the first tick ran).
		requestRenderNow();
	}

}
