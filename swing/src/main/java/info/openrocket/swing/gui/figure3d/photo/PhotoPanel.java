package info.openrocket.swing.gui.figure3d.photo;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.BoundingBox;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.ORColor;
import info.openrocket.swing.gui.figure3d.SharedCanvasRenderScheduler;
import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.materials.Texture;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.Background;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.GradientBackground;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.HDRIBackground;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.ImageBackground;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.SkyboxBackground;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.SolidColorBackground;
import info.openrocket.swing.gui.figure3d.scene.core.Camera;
import info.openrocket.swing.gui.figure3d.scene.core.Light;
import info.openrocket.swing.gui.figure3d.scene.core.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.scene.orchestration.Scene3DOrchestrator;
import info.openrocket.swing.gui.figure3d.scene.properties.DisplaySettings;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import info.openrocket.swing.gui.figure3d.scene.properties.VisualEffectsSettings;
import info.openrocket.swing.gui.figure3d.ui.GLScenePanel;
import info.openrocket.swing.gui.figure3d.photo.sky.Sky;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Lake;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Meadow;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Miramar;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Mountains;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Orbit;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Storm;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class PhotoPanel extends JPanel implements SharedCanvasRenderScheduler.Client {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(PhotoPanel.class);
	private static final Translator trans = Application.getTranslator();
	private static final boolean DEBUG = Boolean.getBoolean("openrocket.figure3d.debug");
	private static final boolean IS_MACOS = SystemInfo.getPlatform() == SystemInfo.Platform.MAC_OS;
	private static final double CAMERA_SETTINGS_EPSILON = 1.0e-6;
	private static final float PHOTO_PARTICLE_LENGTH_SCALE = 0.82f;
	private static final float PHOTO_SMOKE_LENGTH_SCALE = 0.50f;
	private static final float PHOTO_FLAME_EXPOSURE_SCALE = 0.90f;
	private static final float PHOTO_SPARK_SPREAD_SCALE = 0.70f;
	private static final SharedCanvasRenderScheduler RENDER_SCHEDULER = SharedCanvasRenderScheduler.getInstance();
	private static final long RENDER_SHUTDOWN_TIMEOUT_MS = 2_000;
	private final PhotoSettings settings;
	private OpenRocketDocument document;
	private GLScenePanel glPanel;
	private final List<ImageCallback> imageCallbacks = new ArrayList<>();
	private final AtomicBoolean captureQueued = new AtomicBoolean(false);
	private final AtomicBoolean settingsApplyQueued = new AtomicBoolean(false);
	private final AtomicBoolean pendingApply = new AtomicBoolean(true);
	private final AtomicReference<GLScenePanel> pendingCanvasRebuild = new AtomicReference<>();
	private final AtomicBoolean syncingCameraToSettings = new AtomicBoolean(false);
	private final AtomicBoolean suppressCameraToSettingsSync = new AtomicBoolean(false);
	private final AtomicBoolean suppressLightToSettingsSync = new AtomicBoolean(false);
	private final AtomicBoolean deferringInteractiveSettingsSync = new AtomicBoolean(false);
	private final AtomicReference<CameraState> deferredCameraState = new AtomicReference<>();
	private final AtomicReference<LightDirectionState> deferredLightDirection = new AtomicReference<>();
	private final Consumer<Camera> cameraChangeListener = this::handleCameraChanged;
	private final Consumer<Light> lightChangeListener = this::handleLightChanged;
	private final MouseInputAdapter interactionSyncMouseListener = new MouseInputAdapter() {
		private boolean pressed;

		@Override
		public void mousePressed(MouseEvent e) {
			if (isTrackedDragButton(e.getButton())) {
				pressed = true;
				deferringInteractiveSettingsSync.set(true);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (pressed && isTrackedDragButton(e.getButton())) {
				pressed = false;
				finishDeferredInteractiveSettingsSync();
			}
		}
	};
	private final Map<SceneObject, Matrix4f> baseTransforms = new IdentityHashMap<>();
	private final Map<ParticleEmitter, EmitterBase> baseEmitters = new IdentityHashMap<>();
	private boolean lastFlame;
	private boolean lastSmoke;
	private boolean lastSparks;
	private boolean lastParticlesEnabled;
	private ORColor lastFlameColor;
	private ORColor lastSmokeColor;
	private float lastSmokeOpacity = Float.NaN;
	private float lastExhaustScale = Float.NaN;
	private float lastFlameAspectRatio = Float.NaN;
	private float lastSparkConcentration = Float.NaN;
	private float lastSparkWeight = Float.NaN;
	private Sky lastSky;
	private ORColor lastSkyColor;
	private float lastSkyOpacity = Float.NaN;
	private PhotoSettings.BackgroundType lastBackgroundType;
	private ORColor lastGradientTopColor;
	private ORColor lastGradientBottomColor;
	private boolean cameraSettingsTracked;
	private double lastViewAz;
	private double lastViewAlt;
	private double lastViewDistance;
	private double lastFov;
	private volatile long earliestRenderAtMs;
	private volatile boolean renderLoopRunning = false;
	private static final int FRAME_INTERVAL_MS = 16;
	private static final int STARTUP_RENDER_DELAY_MS = 120;
	private static final String[] MOUNTAINS_CUBEMAP = {
			"/datafiles/sky/box/East.jpg",
			"/datafiles/sky/box/West.jpg",
			"/datafiles/sky/box/Up.jpg",
			"/datafiles/sky/box/Down.jpg",
			"/datafiles/sky/box/North.jpg",
			"/datafiles/sky/box/South.jpg"
	};
	private static final String[] MEADOW_CUBEMAP = {
			"/datafiles/sky/Meadow/posx.jpg",
			"/datafiles/sky/Meadow/negx.jpg",
			"/datafiles/sky/Meadow/posy.jpg",
			"/datafiles/sky/Meadow/negy.jpg",
			"/datafiles/sky/Meadow/posz.jpg",
			"/datafiles/sky/Meadow/negz.jpg"
	};
	private static final String[] MIRAMAR_CUBEMAP = {
			"/datafiles/sky/miramar/miramar_ft.jpg",
			"/datafiles/sky/miramar/miramar_bk.jpg",
			"/datafiles/sky/miramar/miramar_up.jpg",
			"/datafiles/sky/miramar/miramar_dn.jpg",
			"/datafiles/sky/miramar/miramar_rt.jpg",
			"/datafiles/sky/miramar/miramar_lf.jpg"
	};

	private static final class EmitterBase {
		private final Vector3f position;
		private final Vector3f direction;

		private EmitterBase(Vector3f position, Vector3f direction) {
			this.position = new Vector3f(position);
			this.direction = new Vector3f(direction);
		}
	}

	private static final class LightDirectionState {
		private final float dx;
		private final float dy;
		private final float dz;

		private LightDirectionState(float dx, float dy, float dz) {
			this.dx = dx;
			this.dy = dy;
			this.dz = dz;
		}
	}

	private static final class CameraState {
		private final float angleX;
		private final float angleY;
		private final float distance;
		private final float fieldOfView;
		private final Vector3f centerOfInterest;

		private CameraState(Camera camera) {
			this.angleX = camera.getAngleX();
			this.angleY = camera.getAngleY();
			this.distance = camera.getDistance();
			this.fieldOfView = camera.getFieldOfView();
			this.centerOfInterest = camera.getCenterOfInterest();
		}
	}

	interface ImageCallback {
		void performAction(BufferedImage i);
	}

	PhotoPanel(OpenRocketDocument document, PhotoSettings settings) {
		this.settings = settings;
		setLayout(new BorderLayout());
		debug("PhotoPanel ctor");
		settings.addChangeListener(e -> {
			if (settings.isAdjusting()) {
				// Slider is being dragged — defer the apply until mouse release.
				pendingApply.set(true);
				return;
			}
			if (!syncingCameraToSettings.get() && !suppressLightToSettingsSync.get()) {
				applySettings();
			}
		});
	}

	void addImageCallback(ImageCallback callback) {
		if (callback == null) {
			return;
		}
		imageCallbacks.add(callback);
		requestImageCapture();
	}

	void setDoc(OpenRocketDocument doc) {
		debug("setDoc start");
		if (doc != null && doc == this.document && glPanel != null) {
			debug("setDoc: already set");
			return;
		}
		clearDoc();
		if (doc == null) {
			debug("setDoc: doc=null");
			return;
		}
		this.document = doc;
		pendingApply.set(true);
		GLScenePanel panel;
		try {
			panel = new GLScenePanel(doc.getRocket(), null, false);
		} catch (UnsatisfiedLinkError | ExceptionInInitializerError e) {
			log.warn("Photo Studio 3D view unavailable: LWJGL native libraries not found for {}/{}.",
					System.getProperty("os.name"), System.getProperty("os.arch"), e);
			return;
		}
		glPanel = panel;
		glPanel.setInitializationHook(this::initializePhotoPanelOnGlThread);
		glPanel.setBlankDefaultFramebufferCallback(() -> requestCanvasRebuild(panel));
		glPanel.setGlInitFailureCallback(() -> SwingUtilities.invokeLater(() -> showGLInitFailureUI(panel)));
		attachInteractionSyncListener(glPanel);
		earliestRenderAtMs = System.currentTimeMillis() + STARTUP_RENDER_DELAY_MS;
		invalidateCachedSceneState();
		add(glPanel, BorderLayout.CENTER);
		revalidate();
		repaint();
		startRenderLoop();
		applySettings();
		debug("setDoc done");
	}

	void clearDoc() {
		debug("clearDoc");
		stopRenderLoop();
		if (glPanel != null) {
			RENDER_SCHEDULER.awaitQuiescence(RENDER_SHUTDOWN_TIMEOUT_MS);
			disposeCurrentCanvas(glPanel);
		}
		imageCallbacks.clear();
		captureQueued.set(false);
		settingsApplyQueued.set(false);  // reset in case the GL task was cleared without running
		pendingApply.set(false);
		pendingCanvasRebuild.set(null);
		deferringInteractiveSettingsSync.set(false);
		deferredCameraState.set(null);
		deferredLightDirection.set(null);
		lastFlameColor = null;
		lastSmokeColor = null;
		lastSmokeOpacity = Float.NaN;
		lastExhaustScale = Float.NaN;
		lastFlameAspectRatio = Float.NaN;
		lastSparkConcentration = Float.NaN;
		lastSparkWeight = Float.NaN;
		lastSky = null;
		lastSkyColor = null;
		lastSkyOpacity = Float.NaN;
		cameraSettingsTracked = false;
		document = null;
	}

	PhotoSettings getSettings() {
		return settings;
	}

	private void requestImageCapture() {
		GLScenePanel panel = glPanel;
		if (panel == null) {
			debug("requestImageCapture: no panel");
			captureQueued.set(false);
			return;
		}
		if (!captureQueued.compareAndSet(false, true)) {
			return;
		}
		boolean transparent = settings.getSky() == null && settings.getSkyColorOpacity() < 1.0;
		panel.requestImageCapture(transparent, image -> {
			List<ImageCallback> callbacks = new ArrayList<>(imageCallbacks);
			imageCallbacks.clear();
			captureQueued.set(false);
			SwingUtilities.invokeLater(() -> {
				for (ImageCallback cb : callbacks) {
					try {
						cb.performAction(image);
					} catch (Throwable t) {
						log.error("Image callback failed", t);
					}
				}
			});
		});
	}

	private void applySettings() {
		GLScenePanel panel = glPanel;
		if (panel == null) {
			debug("applySettings: no panel");
			return;
		}
		Scene3DOrchestrator orchestrator = panel.getScene3DOrchestrator();
		if (orchestrator == null) {
			pendingApply.set(true);
			debug("applySettings: orchestrator not ready");
			return;
		}
		pendingApply.set(false);
		if (!settingsApplyQueued.compareAndSet(false, true)) {
			pendingApply.set(true);
			return;
		}
		debug("applySettings: enqueue");
		orchestrator.enqueueGlTask(() -> {
			try {
				applySettingsOnGlThread(orchestrator);
			} finally {
				settingsApplyQueued.set(false);
				if (pendingApply.getAndSet(false)) {
					applySettings();
				}
			}
		});
	}

	private void startRenderLoop() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(this::startRenderLoop);
			return;
		}
		if (renderLoopRunning) {
			return;
		}
		renderLoopRunning = true;
		RENDER_SCHEDULER.register(this);
		RENDER_SCHEDULER.requestImmediate(this);
	}

	private void stopRenderLoop() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(this::stopRenderLoop);
			return;
		}
		if (renderLoopRunning) {
			renderLoopRunning = false;
			RENDER_SCHEDULER.unregister(this);
		}
	}

	private void renderFrame() {
		if (!renderLoopRunning) {
			return;
		}
		GLScenePanel panel = glPanel;
		if (panel == null) {
			return;
		}
		if (panel.glInitFailed) {
			stopRenderLoop();
			return;
		}
		if (System.currentTimeMillis() < earliestRenderAtMs) {
			return;
		}
		if (!panel.isDisplayable() || !panel.isShowing()) {
			return;
		}
		if (panel.getWidth() <= 0 || panel.getHeight() <= 0) {
			return;
		}
		if (pendingApply.get() && panel.getScene3DOrchestrator() != null && !settingsApplyQueued.get()) {
			applySettings();
		}
		panel.render();
		if (processPendingCanvasRebuild(panel)) {
			return;
		}
		if (pendingApply.get() && panel.getScene3DOrchestrator() != null && !settingsApplyQueued.get()) {
			applySettings();
		}
	}

	@Override
	public boolean isRenderActive() {
		return renderLoopRunning && glPanel != null && document != null;
	}

	@Override
	public boolean shouldRenderOnTick() {
		return true;
	}

	@Override
	public void renderScheduledFrame() {
		renderFrame();
	}

	@Override
	public String getRenderDebugName() {
		return "PhotoPanel";
	}

	private void showGLInitFailureUI(GLScenePanel failedPanel) {
		if (glPanel == failedPanel) {
			disposeCurrentCanvas(failedPanel);
		}
		JLabel label = new JLabel(trans.get("PhotoPanel.glInitFailed"));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		JPanel fallback = new JPanel(new GridBagLayout());
		fallback.add(label);
		add(fallback, BorderLayout.CENTER);
		revalidate();
		repaint();
	}

	private void requestCanvasRebuild(GLScenePanel failedPanel) {
		pendingCanvasRebuild.compareAndSet(null, failedPanel);
	}

	private boolean processPendingCanvasRebuild(GLScenePanel panel) {
		if (!pendingCanvasRebuild.compareAndSet(panel, null)) {
			return false;
		}
		rebuildCanvasAfterBlankDefaultFramebuffer(panel);
		return true;
	}

	private void rebuildCanvasAfterBlankDefaultFramebuffer(GLScenePanel failedPanel) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> rebuildCanvasAfterBlankDefaultFramebuffer(failedPanel));
			return;
		}
		if (glPanel != failedPanel || document == null) {
			return;
		}

		boolean resumeRenderLoop = renderLoopRunning;
		stopRenderLoop();
		RENDER_SCHEDULER.awaitQuiescence(RENDER_SHUTDOWN_TIMEOUT_MS);
		if (glPanel != failedPanel || document == null) {
			if (resumeRenderLoop && glPanel != null) {
				startRenderLoop();
			}
			return;
		}

		disposeCurrentCanvas(failedPanel);

		GLScenePanel panel;
		try {
			panel = new GLScenePanel(document.getRocket(), null, false);
		} catch (UnsatisfiedLinkError | ExceptionInInitializerError e) {
			log.warn("Photo Studio 3D view unavailable during recovery: LWJGL native libraries not found for {}/{}.",
					System.getProperty("os.name"), System.getProperty("os.arch"), e);
			return;
		}
		glPanel = panel;
		panel.setInitializationHook(this::initializePhotoPanelOnGlThread);
		panel.setBlankDefaultFramebufferCallback(() -> requestCanvasRebuild(panel));
		panel.setGlInitFailureCallback(() -> SwingUtilities.invokeLater(() -> showGLInitFailureUI(panel)));
		attachInteractionSyncListener(panel);
		earliestRenderAtMs = System.currentTimeMillis() + STARTUP_RENDER_DELAY_MS;
		invalidateCachedSceneState();
		add(panel, BorderLayout.CENTER);
		revalidate();
		repaint();
		if (resumeRenderLoop) {
			startRenderLoop();
		}
		pendingApply.set(true);
		settingsApplyQueued.set(false);
		applySettings();
	}

	private void disposeCurrentCanvas(GLScenePanel panel) {
		detachSceneListeners(panel);
		detachInteractionSyncListener(panel);
		panel.setInitializationHook(null);
		panel.setBlankDefaultFramebufferCallback(null);
		if (IS_MACOS) {
			// On macOS, attempting runInContext cleanup during Photo Studio teardown can crash
			// inside the native JAWT surface path. Detach first so GLScenePanel.cleanup()
			// takes its non-context cleanup path instead of re-entering the native peer.
			remove(panel);
			glPanel = null;
			panel.cleanup();
		} else {
			// Other platforms still prefer explicit in-context cleanup before detach.
			panel.cleanup();
			remove(panel);
			glPanel = null;
		}
		revalidate();
		repaint();
	}

	private void initializePhotoPanelOnGlThread(Scene3DOrchestrator orchestrator) {
		orchestrator.getCameraController().addCameraChangeListener(cameraChangeListener);
		SceneView scene = orchestrator.getScene();
		if (scene != null) {
			scene.getLightController().addLightChangeListener(lightChangeListener);
		}
		applySettingsOnGlThread(orchestrator);
	}

	private void applySettingsOnGlThread(Scene3DOrchestrator orchestrator) {
		debug("applySettingsOnGlThread");
		suppressCameraToSettingsSync.set(true);
		try {
			SceneView scene = orchestrator.getScene();
			if (scene == null) {
				debug("applySettingsOnGlThread: no scene");
				return;
			}
			configurePhotoScene(scene);
			Camera camera = scene.getCamera();
			CameraState currentCameraState = camera != null ? new CameraState(camera) : null;
			boolean cameraSettingsChanged = isCameraSettingsChanged();

			RenderingConfiguration config = orchestrator.getRenderingConfiguration();
			// PhotoStudio should render as a solid, production-style preview.
			config.getDisplay().setMode(DisplaySettings.RenderMode.FINISHED);
			config.getDisplay().setRenderInternalSurfaces(true);
			config.getQuality().setBackfaceCullingEnabled(true);
			config.getQuality().setShadowsEnabled(true);
			config.getQuality().setAmbientOcclusionEnabled(true);
			config.getVisualEffects().setCaretsVisible(false);
			// Photo Studio always orbits the camera on drag; rocket drag rotation is for design views only.
			config.getVisualEffects().setRotateRocketOnDrag(false);

			boolean rebuild = applyEffects(config);
			if (rebuild) {
				orchestrator.rebuildRocketScene();
				baseTransforms.clear();
				baseEmitters.clear();
				scene = orchestrator.getScene();
				if (scene == null) {
					debug("applySettingsOnGlThread: scene lost after rebuild");
					return;
				}
				camera = scene.getCamera();
			}

			disableComponentSelection(scene);
			applyBackground(scene);
			applyLighting(scene, config);
			if (camera != null) {
				if (cameraSettingsChanged) {
					applyCamera(camera);
					rememberCameraSettings();
				} else if (currentCameraState != null) {
					restoreCamera(camera, currentCameraState);
				}
			}
			applyRocketTransform(scene, config);
		} finally {
			suppressCameraToSettingsSync.set(false);
		}
	}

	private void detachSceneListeners(GLScenePanel panel) {
		Scene3DOrchestrator orchestrator = panel.getScene3DOrchestrator();
		if (orchestrator == null) {
			return;
		}
		orchestrator.getCameraController().removeCameraChangeListener(cameraChangeListener);
		SceneView scene = orchestrator.getScene();
		if (scene != null) {
			scene.getLightController().removeLightChangeListener(lightChangeListener);
		}
	}

	private void invalidateCachedSceneState() {
		cameraSettingsTracked = false;
		lastSky = null;
		lastSkyColor = null;
		lastSkyOpacity = Float.NaN;
		lastBackgroundType = null;
		lastGradientTopColor = null;
		lastGradientBottomColor = null;
	}

	private void disableComponentSelection(SceneView scene) {
		scene.setSelection(List.of());
		for (SceneObject obj : scene.getObjects()) {
			obj.setSelected(false);
			obj.setSelectable(false);
		}
	}

	private void configurePhotoScene(SceneView scene) {
		if (scene instanceof info.openrocket.swing.gui.figure3d.scene.core.Scene actualScene) {
			// Photo Studio recenters the rocket to world origin before applying its own rotations.
			// Keep interactive drag rotation around that origin instead of the design-view centerline pivot.
			actualScene.setRocketRotationPivotOverride(0.0f, 0.0f, 0.0f);
		}
	}

	private void applyCamera(Camera camera) {
		configurePhotoCamera(camera);
		// PhotoStudio model transforms already recenter the rocket around world origin.
		// Keep camera orbit pivot locked to origin to match legacy JOGL behavior.
		camera.setCenterOfInterest(new Vector3f(0.0f, 0.0f, 0.0f));
		// Negate viewAz so camX = -d·sin(az)·cos(alt), matching the legacy JOGL camera
		// position (camera at (0,0,d), world rotated by Rx(alt)·Ry(az)).
		camera.setAngleX((float) -settings.getViewAz());
		camera.setAngleY((float) settings.getViewAlt());
		camera.setFieldOfView(settings.getFov());
		camera.setDistance((float) (settings.getViewDistance() * RenderingConstants.WORLD_SCALE));
		// Use the orbit-up vector so dragging past ±90° altitude doesn't snap the view.
		// (forceFixedUp=true would use a fixed world-up that becomes degenerate at the poles.)
		camera.setForceFixedUp(false);
		camera.update();
	}

	private void restoreCamera(Camera camera, CameraState state) {
		configurePhotoCamera(camera);
		camera.setCenterOfInterest(state.centerOfInterest);
		camera.setAngleX(state.angleX);
		camera.setAngleY(state.angleY);
		camera.setFieldOfView(state.fieldOfView);
		camera.setDistance(state.distance);
		camera.setForceFixedUp(false);
		camera.update();
	}

	private void configurePhotoCamera(Camera camera) {
		// PhotoStudio should not constrain orbit pitch or zoom as tightly as editor views.
		camera.setPitchClampingEnabled(false);
		camera.setZoomLimits(0.01f, 10000.0f);
	}

	private void applyBackground(SceneView scene) {
		PhotoSettings.BackgroundType bgType = settings.getBackgroundType();
		ORColor sky = settings.getSkyColor();
		if (sky == null) {
			sky = new ORColor(0, 0, 0);
		}
		float alpha = (float) settings.getSkyColorOpacity();
		Sky selectedSky = settings.getSky();
		ORColor gradTop = settings.getGradientTopColor();
		ORColor gradBottom = settings.getGradientBottomColor();

		if (bgType == lastBackgroundType
				&& selectedSky == lastSky
				&& Objects.equals(sky, lastSkyColor)
				&& MathUtil.equals(alpha, lastSkyOpacity, CAMERA_SETTINGS_EPSILON)
				&& Objects.equals(gradTop, lastGradientTopColor)
				&& Objects.equals(gradBottom, lastGradientBottomColor)) {
			return;
		}

		scene.setBackground(createBackground(bgType, selectedSky, sky, alpha, gradTop, gradBottom));
		lastBackgroundType = bgType;
		lastSky = selectedSky;
		lastSkyColor = copyColor(sky);
		lastSkyOpacity = alpha;
		lastGradientTopColor = gradTop != null ? copyColor(gradTop) : null;
		lastGradientBottomColor = gradBottom != null ? copyColor(gradBottom) : null;
	}

	private Background createBackground(PhotoSettings.BackgroundType bgType, Sky selectedSky,
			ORColor skyColor, float alpha, ORColor gradTop, ORColor gradBottom) {
		switch (bgType) {
			case GRADIENT -> {
				Vector3f top = gradTop != null
						? new Vector3f(gradTop.getRed() / 255.0f, gradTop.getGreen() / 255.0f, gradTop.getBlue() / 255.0f)
						: new Vector3f(0.2f, 0.4f, 0.7f);
				Vector3f bottom = gradBottom != null
						? new Vector3f(gradBottom.getRed() / 255.0f, gradBottom.getGreen() / 255.0f, gradBottom.getBlue() / 255.0f)
						: new Vector3f(0.04f, 0.08f, 0.16f);
				return new GradientBackground(top, bottom);
			}
			case TEXTURE -> {
				return createTextureBackground(selectedSky, skyColor, alpha);
			}
			default -> {
				return new SolidColorBackground(
						skyColor.getRed() / 255.0f,
						skyColor.getGreen() / 255.0f,
						skyColor.getBlue() / 255.0f,
						alpha);
			}
		}
	}

	private Background createTextureBackground(Sky selectedSky, ORColor skyColor, float alpha) {
		if (selectedSky == null) {
			return new SolidColorBackground(
					skyColor.getRed() / 255.0f,
					skyColor.getGreen() / 255.0f,
					skyColor.getBlue() / 255.0f,
					alpha);
		}

		try {
			if (selectedSky instanceof Mountains) {
				return new SkyboxBackground(new Texture(MOUNTAINS_CUBEMAP));
			}
			if (selectedSky instanceof Meadow) {
				return new SkyboxBackground(new Texture(MEADOW_CUBEMAP));
			}
			if (selectedSky instanceof Miramar) {
				return new SkyboxBackground(new Texture(MIRAMAR_CUBEMAP));
			}
			if (selectedSky instanceof Storm) {
				return new SkyboxBackground(new Texture("/datafiles/sky/cross1.jpg", Texture.AtlasLayout.HORIZONTAL_CROSS));
			}
			if (selectedSky instanceof Lake) {
				return new HDRIBackground(new Texture("/datafiles/sky/lake.jpg"));
			}
			if (selectedSky instanceof Orbit) {
				return new ImageBackground(new Texture("/datafiles/sky/space.jpg"));
			}
		} catch (RuntimeException e) {
			log.warn("Could not load Photo Studio sky '{}', falling back to solid color: {}", selectedSky, e.getMessage());
		}

		log.warn("Photo Studio sky '{}' does not have a mapped background; falling back to solid color", selectedSky);
		return new SolidColorBackground(
				skyColor.getRed() / 255.0f,
				skyColor.getGreen() / 255.0f,
				skyColor.getBlue() / 255.0f,
				alpha);
	}

	private void applyLighting(SceneView scene, RenderingConfiguration config) {
		config.getVisualEffects().setAmbientLightFactor((float) settings.getAmbiance());
		if (scene.getLightController().getLights().isEmpty()) {
			return;
		}
		Light light = scene.getLightController().getLight(0);
		ORColor sun = settings.getSunlight();
		if (sun != null) {
			float strength = (float) settings.getLightStrength();
			light.setColor(sun.getRed() / 255.0f * strength, sun.getGreen() / 255.0f * strength, sun.getBlue() / 255.0f * strength);
		}
		float alt = (float) settings.getLightAlt();
		float az = (float) settings.getLightAz();
		// Old code used glLightfv(GL_POSITION, {x, y, z, 0}) as the toward-light direction,
		// then rendered in Z-flipped (left-handed) space, making the effective to-light
		// direction in model space = (x, y, -z).
		// The new shader stores the "shining-in" direction and negates it: lightDir = -direction.
		// So direction = -(x, y, -z) = (-x, -y, z) to reproduce the old behaviour.
			// The legacy azimuth convention is offset by 90 deg: substitute (az - pi/2).
			// sin(az - pi/2) = -cos(az),  cos(az - pi/2) = sin(az)
		float x = (float) (-Math.cos(alt) * Math.cos(az));
		float y = (float) Math.sin(alt);
		float z = (float) (Math.cos(alt) * Math.sin(az));
		light.setDirection(-x, -y, z);
	}

	private void applyRocketTransform(SceneView scene, RenderingConfiguration config) {
		if (document == null) {
			debug("applyRocketTransform: no document");
			return;
		}
		BoundingBox bounds = document.getRocket().getBoundingBox();
		if (bounds == null || bounds.isEmpty()) {
			debug("applyRocketTransform: empty bounds");
			return;
		}
		double centerX = (bounds.min.getX() + bounds.max.getX()) / 2.0;
		double advance = settings.getAdvance();
		float translateX = (float) (-(centerX + advance) * RenderingConstants.WORLD_SCALE);

		Matrix4f sceneRotationTransform = new Matrix4f();
		Matrix4f sceneRotationInverse = new Matrix4f();
		if (scene instanceof info.openrocket.swing.gui.figure3d.scene.core.Scene actualScene) {
			actualScene.getRocketRotationTransform(sceneRotationTransform);
			sceneRotationInverse.set(sceneRotationTransform).invert();
		} else {
			sceneRotationTransform.identity();
			sceneRotationInverse.identity();
		}

		Matrix4f globalTransform = new Matrix4f(sceneRotationTransform)
				.rotateZ((float) -settings.getPitch())
				.rotateY((float) settings.getYaw())
				.rotateX((float) settings.getRoll())
				.translate(translateX, 0.0f, 0.0f);

		boolean resetBases = false;
		for (SceneObject obj : scene.getObjects()) {
			if (obj.getRocketComponent() != null && !baseTransforms.containsKey(obj)) {
				resetBases = true;
				break;
			}
		}
		if (resetBases) {
			baseTransforms.clear();
		}

		for (SceneObject obj : scene.getObjects()) {
			if (obj.getRocketComponent() == null) {
				continue;
			}
			Matrix4f base = baseTransforms.computeIfAbsent(obj,
					key -> sceneRotationInverse.mul(key.getModelMatrix(), new Matrix4f()));
			globalTransform.mul(base, obj.getModelMatrix());
		}

		applyParticleTransform(scene, config, globalTransform);
	}

	private void applyParticleTransform(SceneView scene, RenderingConfiguration config, Matrix4f globalTransform) {
		boolean resetBases = false;
		for (ParticleEmitter emitter : scene.getParticleEmitters()) {
			if (!baseEmitters.containsKey(emitter)) {
				resetBases = true;
				break;
			}
		}
		if (resetBases) {
			baseEmitters.clear();
		}

		VisualEffectsSettings effects = config.getVisualEffects();
		for (ParticleEmitter emitter : scene.getParticleEmitters()) {
			EmitterBase base = baseEmitters.computeIfAbsent(emitter,
					key -> new EmitterBase(emitter.getEmitterPosition(), emitter.getDirection()));

			Vector3f position = new Vector3f(base.position);
			Vector3f direction = new Vector3f(base.direction);
			globalTransform.transformPosition(position);
			globalTransform.transformDirection(direction).normalize();

			emitter.setEmitterPosition(position);
			emitter.setDirection(direction);

			if (effects.areStaticParticles()) {
				emitter.captureStaticParticles(effects.getParticleTime());
			} else {
				emitter.setStaticMode(false);
				emitter.getParticles().clear();
			}
		}
	}

	private static void debug(String message) {
		if (!DEBUG) {
			return;
		}
		log.debug("[{}] {}", Thread.currentThread().getName(), message);
	}

	private boolean applyEffects(RenderingConfiguration config) {
		VisualEffectsSettings effects = config.getVisualEffects();
		effects.setMotionBlurEnabled(settings.isMotionBlurred());
		effects.setMotionBlurFactor((float) settings.getMotionBlurAmount());
		boolean motorsAvailable = document != null
				&& document.getSelectedConfiguration() != null
				&& document.getSelectedConfiguration().hasMotors();
		boolean flameEnabled = motorsAvailable && settings.isFlame();
		boolean smokeEnabled = motorsAvailable && settings.isSmoke();
		boolean sparksEnabled = motorsAvailable && settings.isSparks();
		boolean particlesEnabled = flameEnabled || smokeEnabled || sparksEnabled;
		effects.setParticleEffectsEnabled(particlesEnabled);
		effects.setFlameParticlesEnabled(flameEnabled);
		effects.setSmokeParticlesEnabled(smokeEnabled);
		effects.setSparkParticlesEnabled(sparksEnabled);
		ORColor flameColor = colorOrDefault(settings.getFlameColor(), new ORColor(255, 100, 50));
		ORColor smokeColor = colorOrDefault(settings.getSmokeColor(), new ORColor(230, 230, 230));
		effects.setFlameColor(toColorVector(flameColor));
		effects.setSmokeColor(toColorVector(smokeColor));
		effects.setSmokeOpacity((float) settings.getSmokeOpacity());
		effects.setExhaustScale((float) settings.getExhaustScale());
		effects.setFlameAspectRatio((float) settings.getFlameAspectRatio());
		effects.setSparkConcentration((float) settings.getSparkConcentration());
		effects.setSparkWeight((float) settings.getSparkWeight());
		effects.setParticleLengthScale(PHOTO_PARTICLE_LENGTH_SCALE);
		effects.setSmokeLengthScale(PHOTO_SMOKE_LENGTH_SCALE);
		effects.setFlameExposureScale(PHOTO_FLAME_EXPOSURE_SCALE);
		effects.setSparkSpreadScale(PHOTO_SPARK_SPREAD_SCALE);

		boolean flameColorChanged = !Objects.equals(flameColor, lastFlameColor);
		boolean smokeColorChanged = !Objects.equals(smokeColor, lastSmokeColor);
		boolean smokeOpacityChanged = !MathUtil.equals(settings.getSmokeOpacity(), lastSmokeOpacity, CAMERA_SETTINGS_EPSILON);
		boolean exhaustScaleChanged = !MathUtil.equals(settings.getExhaustScale(), lastExhaustScale, CAMERA_SETTINGS_EPSILON);
		boolean flameAspectRatioChanged = !MathUtil.equals(settings.getFlameAspectRatio(), lastFlameAspectRatio, CAMERA_SETTINGS_EPSILON);
		boolean sparkConcentrationChanged = !MathUtil.equals(settings.getSparkConcentration(), lastSparkConcentration, CAMERA_SETTINGS_EPSILON);
		boolean sparkWeightChanged = !MathUtil.equals(settings.getSparkWeight(), lastSparkWeight, CAMERA_SETTINGS_EPSILON);

		boolean rebuild = particlesEnabled != lastParticlesEnabled
				|| flameEnabled != lastFlame
				|| smokeEnabled != lastSmoke
				|| sparksEnabled != lastSparks
				|| flameColorChanged
				|| smokeColorChanged
				|| smokeOpacityChanged
				|| exhaustScaleChanged
				|| flameAspectRatioChanged
				|| sparkConcentrationChanged
				|| sparkWeightChanged;

		lastParticlesEnabled = particlesEnabled;
		lastFlame = flameEnabled;
		lastSmoke = smokeEnabled;
		lastSparks = sparksEnabled;
		lastFlameColor = copyColor(flameColor);
		lastSmokeColor = copyColor(smokeColor);
		lastSmokeOpacity = (float) settings.getSmokeOpacity();
		lastExhaustScale = (float) settings.getExhaustScale();
		lastFlameAspectRatio = (float) settings.getFlameAspectRatio();
		lastSparkConcentration = (float) settings.getSparkConcentration();
		lastSparkWeight = (float) settings.getSparkWeight();

		return rebuild;
	}

	private boolean isCameraSettingsChanged() {
		if (!cameraSettingsTracked) {
			return true;
		}
		return !MathUtil.equals(settings.getViewAz(), lastViewAz, CAMERA_SETTINGS_EPSILON)
				|| !MathUtil.equals(settings.getViewAlt(), lastViewAlt, CAMERA_SETTINGS_EPSILON)
				|| !MathUtil.equals(settings.getViewDistance(), lastViewDistance, CAMERA_SETTINGS_EPSILON)
				|| !MathUtil.equals(settings.getFov(), lastFov, CAMERA_SETTINGS_EPSILON);
	}

	private void rememberCameraSettings() {
		lastViewAz = settings.getViewAz();
		lastViewAlt = settings.getViewAlt();
		lastViewDistance = settings.getViewDistance();
		lastFov = settings.getFov();
		cameraSettingsTracked = true;
	}

	private void attachInteractionSyncListener(GLScenePanel panel) {
		panel.addMouseListener(interactionSyncMouseListener);
		panel.addMouseMotionListener(interactionSyncMouseListener);
	}

	private void detachInteractionSyncListener(GLScenePanel panel) {
		panel.removeMouseListener(interactionSyncMouseListener);
		panel.removeMouseMotionListener(interactionSyncMouseListener);
	}

	private void finishDeferredInteractiveSettingsSync() {
		deferringInteractiveSettingsSync.set(false);
		CameraState cameraState = deferredCameraState.getAndSet(null);
		LightDirectionState lightDirection = deferredLightDirection.getAndSet(null);
		if (cameraState != null) {
			syncSettingsFromCameraState(cameraState);
		}
		if (lightDirection != null) {
			syncSettingsFromLightDirection(lightDirection.dx, lightDirection.dy, lightDirection.dz);
		}
	}

	/**
	 * Synchronizes camera settings from the scene back to the settings object, if they have changed.
	 * This allows the settings to reflect user interactions with the camera (e.g. orbiting with mouse drag).
	 */
	private void handleCameraChanged(Camera camera) {
		if (suppressCameraToSettingsSync.get()) {
			return;
		}
		CameraState cameraState = new CameraState(camera);
		if (deferringInteractiveSettingsSync.get()) {
			deferredCameraState.set(cameraState);
			return;
		}
		if (SwingUtilities.isEventDispatchThread()) {
			syncSettingsFromCameraState(cameraState);
		} else {
			SwingUtilities.invokeLater(() -> syncSettingsFromCameraState(cameraState));
		}
	}

	private void syncSettingsFromCameraState(CameraState cameraState) {
		// camera.angleX is stored as -viewAz (negated in applyCamera), so negate back.
		double viewAz = MathUtil.reduce2Pi(-cameraState.angleX);
		double viewAlt = cameraState.angleY;
		double viewDistance = cameraState.distance / RenderingConstants.WORLD_SCALE;
		double fov = cameraState.fieldOfView;

		if (MathUtil.equals(viewAz, settings.getViewAz(), CAMERA_SETTINGS_EPSILON)
				&& MathUtil.equals(viewAlt, settings.getViewAlt(), CAMERA_SETTINGS_EPSILON)
				&& MathUtil.equals(viewDistance, settings.getViewDistance(), CAMERA_SETTINGS_EPSILON)
				&& MathUtil.equals(fov, settings.getFov(), CAMERA_SETTINGS_EPSILON)) {
			return;
		}

		syncingCameraToSettings.set(true);
		try {
			settings.setView(viewAlt, viewAz, viewDistance, fov);
			rememberCameraSettings();
		} finally {
			syncingCameraToSettings.set(false);
		}
	}

	/**
	 * Called when the light direction is modified interactively (e.g. Alt+drag).
	 * Marshals the sync onto the EDT, matching the camera change listener pattern.
	 */
	private void handleLightChanged(Light light) {
		if (suppressLightToSettingsSync.get()) {
			return;
		}
		// Capture the direction vector from the GL thread before handing off to EDT
		org.joml.Vector3f dir = light.getDirection();
		float dx = dir.x, dy = dir.y, dz = dir.z;
		if (deferringInteractiveSettingsSync.get()) {
			deferredLightDirection.set(new LightDirectionState(dx, dy, dz));
			return;
		}
		if (SwingUtilities.isEventDispatchThread()) {
			syncSettingsFromLightDirection(dx, dy, dz);
		} else {
			SwingUtilities.invokeLater(() -> syncSettingsFromLightDirection(dx, dy, dz));
		}
	}

	/**
	 * Converts the stored light direction vector back to (lightAlt, lightAz) settings values.
	 *
	 * <p>Inverse of the applyLighting() formula:
	 * <pre>
	 *   direction = (-cos(alt)*cos(az),  -sin(alt),  cos(alt)*sin(az))
	 * </pre>
	 * Recovery:
	 * <pre>
	 *   alt = asin(-direction.y)
	 *   az  = atan2(direction.z, -direction.x)
	 * </pre>
	 */
	private void syncSettingsFromLightDirection(float dx, float dy, float dz) {
		double lightAlt = Math.asin(MathUtil.clamp(-dy, -1.0, 1.0));
		double lightAz = MathUtil.reduce2Pi(Math.atan2(dz, -dx));

		if (MathUtil.equals(lightAlt, settings.getLightAlt(), CAMERA_SETTINGS_EPSILON)
				&& MathUtil.equals(lightAz, settings.getLightAz(), CAMERA_SETTINGS_EPSILON)) {
			return;
		}

		suppressLightToSettingsSync.set(true);
		try {
			settings.setLight(lightAlt, lightAz);
		} finally {
			suppressLightToSettingsSync.set(false);
		}
	}

	private static boolean isTrackedDragButton(int button) {
		return button == MouseEvent.BUTTON1 || button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3;
	}

	private static ORColor colorOrDefault(ORColor color, ORColor fallback) {
		return color != null ? color : fallback;
	}

	private static ORColor copyColor(ORColor color) {
		if (color == null) {
			return null;
		}
		return new ORColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}

	private static Vector3f toColorVector(ORColor color) {
		return new Vector3f(
				color.getRed() / 255.0f,
				color.getGreen() / 255.0f,
				color.getBlue() / 255.0f
		);
	}
}
