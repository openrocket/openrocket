package info.openrocket.swing.gui.figure3d;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.SolidColorBackground;
import info.openrocket.swing.gui.figure3d.scene.core.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.scene.orchestration.Scene3DOrchestrator;
import info.openrocket.swing.gui.figure3d.ui.GLScenePanel;
import info.openrocket.swing.gui.figure3d.ui.HUDPanel;
import info.openrocket.swing.gui.figure3d.utils.ColorUtils;
import info.openrocket.swing.gui.figureelements.RocketInfo;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.util.GUIUtil;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.SecondaryLoop;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Swing adapter for the new GLScenePanel renderer.
 *
 * <p>The previous adapter introduced extra render threads and schedulers on top of
 * AWTGLCanvas. This version keeps rendering on the EDT with a single Swing timer,
 * which is a better fit for the surrounding Swing lifecycle.</p>
 */
public class RocketFigure3d extends JPanel {

	private static final Logger log = LoggerFactory.getLogger(RocketFigure3d.class);
	private static final int FRAME_INTERVAL_MS = 16;
	private static final EdtRenderScheduler EDT_RENDER_SCHEDULER = new EdtRenderScheduler();

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
	private final List<ComponentSelectionListener> selectionListeners = new CopyOnWriteArrayList<>();

	private GLScenePanel glScenePanel;
	private boolean renderingEnabled = false;
	private boolean disposed = false;
	private boolean selectionBridgeInstalled = false;
	private RocketComponent[] pendingSelection;
	private boolean glFailureLogged = false;
	private Color customBackgroundColor = null;

	public RocketFigure3d(OpenRocketDocument document) {
		this.document = document;
		this.rocketInfo = new RocketInfo(document.getRocket().getSelectedConfiguration());
		this.hudPanel = new HUDPanel(document.getRocket(), rocketInfo);
		setLayout(new BorderLayout());
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(RocketFigure3d::updateColors);
	}

	public static void updateColors() {
		backgroundColor = GUIUtil.getUITheme().getBackgroundColor();
	}

	private void ensureCanvasCreatedOnEdt() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(this::ensureCanvasCreatedOnEdt);
			return;
		}
		if (glScenePanel != null || disposed) {
			return;
		}
		GLScenePanel panel = new GLScenePanel(document.getRocket(), hudPanel);
		glScenePanel = panel;
		add(panel, BorderLayout.CENTER);
		applyBackgroundColor(panel);
		revalidate();
		repaint();
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
			for (ComponentSelectionListener listener : selectionListeners) {
				listener.componentClicked(components, event);
			}
		});
		selectionBridgeInstalled = true;
		if (pendingSelection != null) {
			RocketComponent[] selection = pendingSelection;
			pendingSelection = null;
			setSelection(selection);
		}
	}

	private void renderFrameOnEdt() {
		if (!renderingEnabled || disposed) {
			return;
		}
		ensureCanvasCreatedOnEdt();
		GLScenePanel panel = glScenePanel;
		if (panel == null) {
			return;
		}
		if (!panel.isDisplayable() || !panel.isShowing() || panel.getWidth() <= 0 || panel.getHeight() <= 0) {
			return;
		}

		panel.render();
		if (panel.glInitFailed) {
			if (!glFailureLogged) {
				log.error("GL initialization/rendering failed in RocketFigure3d");
				glFailureLogged = true;
			}
			stopRendering();
			return;
		}
		maybeInstallSelectionBridge(panel);
	}

	/**
	 * Called by RocketPanel when switching to 3D mode.
	 */
	public void startRendering() {
		if (disposed) {
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
				panel.requestPeerBoundsSyncNow();
				applyBackgroundColor(panel);
			}
			EDT_RENDER_SCHEDULER.register(this);
		});
	}

	/**
	 * Called by RocketPanel when switching back to 2D mode.
	 */
	public void stopRendering() {
		renderingEnabled = false;
		EDT_RENDER_SCHEDULER.unregister(this);
	}

	public void addComponentSelectionListener(ComponentSelectionListener listener) {
		if (listener != null) {
			selectionListeners.add(listener);
		}
	}

	private Color getBackgroundColor() {
		return customBackgroundColor != null ? customBackgroundColor : backgroundColor;
	}

	public void setCustomBackgroundColor(Color color) {
		this.customBackgroundColor = color;
		GLScenePanel panel = glScenePanel;
		if (panel != null) {
			applyBackgroundColor(panel);
			panel.repaint();
		}
	}

	private void applyBackgroundColor(GLScenePanel panel) {
		if (panel == null || panel.glInitFailed || !panel.awaitInitialized(0)) {
			return;
		}
		Scene3DOrchestrator orchestrator = panel.getScene3DOrchestrator();
		if (orchestrator == null) {
			return;
		}
		Color color = getBackgroundColor();
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
		GLScenePanel panel = glScenePanel;
		if (panel != null) {
			panel.markHudForUpdate();
			panel.repaint();
		}
	}

	// Compatibility methods expected by RocketPanel.

	public void setType(int type) {
		// The new pipeline currently uses one renderer path for these view modes.
	}

	public void setDrawCarets(boolean draw) {
		hudPanel.setVisible(draw);
		GLScenePanel panel = glScenePanel;
		if (panel != null) {
			panel.markHudForUpdate();
		}
	}

	public void setCG(CoordinateIF cg) {
		if (cg != null) {
			rocketInfo.setCG(cg.getX());
		}
		GLScenePanel panel = glScenePanel;
		if (panel != null) {
			panel.markHudForUpdate();
		}
	}

	public void setCP(CoordinateIF cp) {
		if (cp != null) {
			rocketInfo.setCP(cp.getX());
		}
		GLScenePanel panel = glScenePanel;
		if (panel != null) {
			panel.markHudForUpdate();
		}
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
		GLScenePanel panel = glScenePanel;
		return panel != null ? panel.getScene3DOrchestrator() : null;
	}

	public BufferedImage captureImage() {
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
			panel.render();
			loop.enter();
			return result.get();
		}

		CountDownLatch latch = new CountDownLatch(1);
		panel.requestImageCapture(false, image -> {
			result.set(image);
			latch.countDown();
		});
		SwingUtilities.invokeLater(panel::render);
		try {
			if (!latch.await(2, TimeUnit.SECONDS)) {
				log.warn("Timed out capturing 3D image");
				return null;
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		}
		return result.get();
	}

	public void cleanup() {
		stopRendering();
		disposed = true;
		GLScenePanel panel = glScenePanel;
		glScenePanel = null;
		selectionBridgeInstalled = false;
		pendingSelection = null;
		if (panel != null) {
			panel.cleanup();
		}
	}

	@Override
	public void removeNotify() {
		stopRendering();
		disposed = true;
		selectionBridgeInstalled = false;
		pendingSelection = null;
		// During removeNotify the native drawing surface can already be in teardown.
		// Avoid forcing GLScenePanel.cleanup() here to prevent JAWT crashes on macOS.
		glScenePanel = null;
		super.removeNotify();
	}

	private static final class EdtRenderScheduler implements ActionListener {
		private final ArrayList<RocketFigure3d> active = new ArrayList<>();
		private final Timer timer;
		private int index = 0;

		private EdtRenderScheduler() {
			timer = new Timer(FRAME_INTERVAL_MS, this);
			timer.setCoalesce(true);
		}

		private void register(RocketFigure3d figure) {
			if (!SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeLater(() -> register(figure));
				return;
			}
			if (active.contains(figure)) {
				return;
			}
			active.add(figure);
			if (!timer.isRunning()) {
				timer.start();
			}
		}

		private void unregister(RocketFigure3d figure) {
			if (!SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeLater(() -> unregister(figure));
				return;
			}
			int removedIndex = active.indexOf(figure);
			if (removedIndex < 0) {
				return;
			}
			active.remove(removedIndex);
			if (index > removedIndex) {
				index--;
			}
			if (index >= active.size()) {
				index = 0;
			}
			if (active.isEmpty()) {
				timer.stop();
				index = 0;
			}
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			for (int i = active.size() - 1; i >= 0; i--) {
				RocketFigure3d figure = active.get(i);
				if (!figure.renderingEnabled || figure.disposed) {
					active.remove(i);
					if (index > i) {
						index--;
					}
				}
			}
			if (active.isEmpty()) {
				timer.stop();
				index = 0;
				return;
			}
			if (index >= active.size()) {
				index = 0;
			}
			RocketFigure3d figure = active.get(index);
			index = (index + 1) % active.size();
			try {
				figure.renderFrameOnEdt();
			} catch (Throwable t) {
				log.error("Render scheduler failed for one 3D panel", t);
			}
		}
	}
}
