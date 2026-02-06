package info.openrocket.swing.gui.figure3d_old.photo;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.rocketcomponent.BodyComponent;
import info.openrocket.core.util.BoundingBox;
import info.openrocket.core.util.ORColor;
import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.core.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.SolidColorBackground;
import info.openrocket.swing.gui.figure3d.scene.core.Camera;
import info.openrocket.swing.gui.figure3d.scene.core.Light;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.scene.core.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.orchestration.Scene3DOrchestrator;
import info.openrocket.swing.gui.figure3d.scene.properties.DisplaySettings;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import info.openrocket.swing.gui.figure3d.scene.properties.VisualEffectsSettings;
import info.openrocket.swing.gui.figure3d.ui.GLScenePanel;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class PhotoPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(PhotoPanel.class);
	private static final boolean DEBUG = Boolean.getBoolean("openrocket.figure3d.debug");

	private final PhotoSettings settings;
	private OpenRocketDocument document;
	private GLScenePanel glPanel;
	private final List<ImageCallback> imageCallbacks = new ArrayList<>();
	private final AtomicBoolean captureQueued = new AtomicBoolean(false);
	private final AtomicBoolean settingsApplyQueued = new AtomicBoolean(false);
	private final AtomicBoolean pendingApply = new AtomicBoolean(true);
	private final Map<SceneObject, Matrix4f> baseTransforms = new IdentityHashMap<>();
	private final Map<ParticleEmitter, EmitterBase> baseEmitters = new IdentityHashMap<>();
	private boolean lastFlame;
	private boolean lastSmoke;
	private boolean lastSparks;
	private boolean lastParticlesEnabled;
	private volatile long earliestRenderAtMs;
	private Timer renderTimer;
	private static final int FRAME_INTERVAL_MS = 16;
	private static final int STARTUP_RENDER_DELAY_MS = 120;

	private static final class EmitterBase {
		private final Vector3f position;
		private final Vector3f direction;

		private EmitterBase(Vector3f position, Vector3f direction) {
			this.position = new Vector3f(position);
			this.direction = new Vector3f(direction);
		}
	}

	interface ImageCallback {
		void performAction(BufferedImage i);
	}

	PhotoPanel(OpenRocketDocument document, PhotoSettings settings) {
		this.settings = settings;
		setLayout(new BorderLayout());
		debug("PhotoPanel ctor");
		settings.addChangeListener(e -> applySettings());
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
		glPanel = new GLScenePanel(doc.getRocket(), null, false);
		earliestRenderAtMs = System.currentTimeMillis() + STARTUP_RENDER_DELAY_MS;
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
			remove(glPanel);
			glPanel.cleanup();
			glPanel = null;
		}
		imageCallbacks.clear();
		captureQueued.set(false);
		settingsApplyQueued.set(false);
		pendingApply.set(false);
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
		boolean transparent = settings.getSkyColorOpacity() < 1.0;
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
		if (renderTimer != null) {
			return;
		}
		renderTimer = new Timer(FRAME_INTERVAL_MS, e -> renderFrame());
		renderTimer.setRepeats(true);
		renderTimer.setCoalesce(true);
		renderTimer.start();
	}

	private void stopRenderLoop() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(this::stopRenderLoop);
			return;
		}
		if (renderTimer != null) {
			renderTimer.stop();
			renderTimer = null;
		}
	}

	private void renderFrame() {
		GLScenePanel panel = glPanel;
		if (panel == null) {
			return;
		}
		if (panel.glInitFailed) {
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
		if (pendingApply.get() && panel.getScene3DOrchestrator() != null && !settingsApplyQueued.get()) {
			applySettings();
		}
	}

	private void applySettingsOnGlThread(Scene3DOrchestrator orchestrator) {
		debug("applySettingsOnGlThread");
		SceneView scene = orchestrator.getScene();
		if (scene == null) {
			debug("applySettingsOnGlThread: no scene");
			return;
		}
		RenderingConfiguration config = orchestrator.getRenderingConfiguration();
		// PhotoStudio should render as a solid, production-style preview.
		config.getDisplay().setMode(DisplaySettings.RenderMode.FINISHED);
		config.getDisplay().setRenderInternalSurfaces(true);
		config.getQuality().setBackfaceCullingEnabled(true);

		boolean rebuild = applyEffects(config);
		if (rebuild) {
			orchestrator.rebuildRocketScene();
			baseTransforms.clear();
			baseEmitters.clear();
		}

		enforceOpaqueBodyComponents(scene);
		applyBackground(scene);
		applyLighting(scene);
		applyCamera(scene.getCamera());
		applyRocketTransform(scene, config);
	}

	private void enforceOpaqueBodyComponents(SceneView scene) {
		for (SceneObject obj : scene.getObjects()) {
			if (obj.getRocketComponent() instanceof BodyComponent) {
				obj.getAppearance().setOpacity(1.0f);
			}
		}
	}

	private void applyCamera(Camera camera) {
		// PhotoStudio model transforms already recenter the rocket around world origin.
		// Keep camera orbit pivot locked to origin to match legacy JOGL behavior.
		camera.setCenterOfInterest(new Vector3f(0.0f, 0.0f, 0.0f));
		camera.setAngleX((float) Math.toDegrees(settings.getViewAz()));
		camera.setAngleY((float) Math.toDegrees(settings.getViewAlt()));
		camera.setFieldOfView(settings.getFov());
		camera.setDistance((float) (settings.getViewDistance() * RenderingConstants.WORLD_SCALE));
		camera.update();
	}

	private void applyBackground(SceneView scene) {
		ORColor sky = settings.getSkyColor();
		if (sky == null) {
			sky = new ORColor(0, 0, 0);
		}
		float alpha = (float) settings.getSkyColorOpacity();
		scene.setBackground(new SolidColorBackground(
				sky.getRed() / 255.0f,
				sky.getGreen() / 255.0f,
				sky.getBlue() / 255.0f,
				alpha));
	}

	private void applyLighting(SceneView scene) {
		if (scene.getLightController().getLights().isEmpty()) {
			return;
		}
		Light light = scene.getLightController().getLight(0);
		ORColor sun = settings.getSunlight();
		if (sun != null) {
			light.setColor(sun.getRed() / 255.0f, sun.getGreen() / 255.0f, sun.getBlue() / 255.0f);
		}
		float alt = (float) settings.getLightAlt();
		float az = (float) settings.getLightAz();
		float x = (float) (Math.cos(alt) * Math.sin(az));
		float y = (float) Math.sin(alt);
		float z = (float) (Math.cos(alt) * Math.cos(az));
		light.setDirection(x, y, -z);
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

		Matrix4f globalTransform = new Matrix4f()
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
			Matrix4f base = baseTransforms.computeIfAbsent(obj, key -> new Matrix4f(obj.getModelMatrix()));
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
		System.out.println("[PhotoPanel][" + Thread.currentThread().getName() + "] " + message);
	}

	private boolean applyEffects(RenderingConfiguration config) {
		VisualEffectsSettings effects = config.getVisualEffects();
		effects.setMotionBlurEnabled(settings.isMotionBlurred());
		boolean particlesEnabled = settings.isFlame() || settings.isSmoke() || settings.isSparks();
		effects.setParticleEffectsEnabled(particlesEnabled);
		effects.setFlameParticlesEnabled(settings.isFlame());
		effects.setSmokeParticlesEnabled(settings.isSmoke());
		effects.setSparkParticlesEnabled(settings.isSparks());

		boolean rebuild = particlesEnabled != lastParticlesEnabled
				|| settings.isFlame() != lastFlame
				|| settings.isSmoke() != lastSmoke
				|| settings.isSparks() != lastSparks;

		lastParticlesEnabled = particlesEnabled;
		lastFlame = settings.isFlame();
		lastSmoke = settings.isSmoke();
		lastSparks = settings.isSparks();

		return rebuild;
	}
}
