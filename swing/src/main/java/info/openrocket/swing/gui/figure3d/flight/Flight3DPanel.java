package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.gui.figure3d.SharedCanvasRenderScheduler;
import info.openrocket.swing.gui.figure3d.animation.PlaybackClock;
import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.rendering.FrameOverlay;
import info.openrocket.swing.gui.figure3d.scene.controllers.CameraControls;
import info.openrocket.swing.gui.figure3d.scene.graph.Camera;
import info.openrocket.swing.gui.figure3d.scene.graph.Scene;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.orchestration.FlightCameraRig;
import info.openrocket.swing.gui.figure3d.scene.orchestration.Scene3DOrchestrator;
import info.openrocket.swing.gui.figure3d.scene.properties.DisplaySettings;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import info.openrocket.swing.gui.figure3d.ui.GLScenePanel;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 * The 3D view of a flight replay: animates a simulation's flight in the LWJGL engine and applies
 * the replay window's camera mode, visibility toggles and tracked body to it.
 *
 * <p>Each replay's scene is assembled once on the render thread ({@link #initializeReplay}) from
 * {@link ReplayPoses} (the trajectories), {@link TrajectoryTrails}, {@link ReplayExhaust},
 * {@link RecoveryDevices} and {@link LaunchSiteScenery}; every frame {@link #onFlightFrame}
 * brings them to the playback time. Everything shown is a function of that time, so seeking
 * shows exactly what continuous playback would.
 *
 * <p>Threading: the package-private setters run on the Swing thread. They store their state in
 * volatile fields and hand scene changes to the render thread through
 * {@link Scene3DOrchestrator#enqueueGlTask}; the scene parts are only touched on the render
 * thread. The panel hosts one canvas at a time and rebuilds it if the graphics context is lost.
 */
@SuppressWarnings("serial")
class Flight3DPanel extends JPanel implements SharedCanvasRenderScheduler.Client {
	private static final Logger log = LoggerFactory.getLogger(Flight3DPanel.class);
	private static final Translator trans = Application.getTranslator();
	private static final boolean DEBUG = Boolean.getBoolean("openrocket.figure3d.debug");
	private static final boolean IS_MACOS = SystemInfo.getPlatform() == SystemInfo.Platform.MAC_OS;
	private static final SharedCanvasRenderScheduler RENDER_SCHEDULER = SharedCanvasRenderScheduler.getInstance();
	private static final long RENDER_SHUTDOWN_TIMEOUT_MS = 2_000;
	private static final int STARTUP_RENDER_DELAY_MS = 120;
	// The default flame exposure is tuned for the pad view's tightly packed plume; the replay
	// plume spreads its particles wider, so it needs more exposure to read as fire.
	private static final float FLAME_EXPOSURE = 0.2f;

	/**
	 * Everything built for one replay, published once its scene is ready.
	 *
	 * @param bounds        the trajectory's bounds, or null without a finite trajectory
	 * @param initialAngleX the orbit yaw the replay opened with, restored by resetting the view
	 * @param initialAngleY the orbit pitch the replay opened with
	 * @param baseFieldOfView the replay camera's normal lens
	 */
	private record Replay(Scene3DOrchestrator orchestrator, PlaybackClock clock, ReplayPoses poses,
			TrajectoryTrails trails, ReplayExhaust exhaust, RecoveryDevices recovery, FlightTargetMarker targetMarker,
			WindIndicator windIndicator, ReplayPoses.Bounds bounds, float rocketLength, float initialAngleX,
			float initialAngleY, float baseFieldOfView) {
		private FlightCameraRig camera() {
			return orchestrator.getFlightCamera();
		}
	}

	private OpenRocketDocument document;
	private Simulation simulation;
	private FlightData flightData;
	private FlightConfigurationId replayConfigurationId;
	private GLScenePanel glPanel;
	private final AtomicReference<GLScenePanel> pendingCanvasRebuild = new AtomicReference<>();
	private final AtomicLong replayGeneration = new AtomicLong();
	private final AtomicBoolean dirty = new AtomicBoolean(true);
	private volatile BiConsumer<PlaybackClock, FlightReplayData> replayReadyCallback;
	private volatile long earliestRenderAtMs;
	private volatile boolean renderLoopRunning = false;
	private volatile Replay replay;

	// View state chosen in the replay window; kept across canvas rebuilds.
	private volatile FlightCameraMode cameraMode = FlightCameraMode.OVERVIEW;
	private volatile boolean panModeEnabled = false;
	private volatile boolean trailVisible = true;
	private volatile boolean exhaustVisible = true;
	private volatile int trackedBodyIndex = 0;

	Flight3DPanel() {
		setLayout(new BorderLayout());
	}

	void setReplayReadyCallback(BiConsumer<PlaybackClock, FlightReplayData> replayReadyCallback) {
		this.replayReadyCallback = replayReadyCallback;
	}

	void setSimulation(OpenRocketDocument doc, Simulation sim) {
		debug("setSimulation start");
		if (doc != null && sim != null && doc == document && sim == simulation && glPanel != null) {
			debug("setSimulation: already set");
			return;
		}
		clearDoc();
		if (doc == null || sim == null) {
			debug("setSimulation: doc/sim=null");
			return;
		}

		document = doc;
		simulation = sim;
		flightData = sim.getSimulatedData();
		replayConfigurationId = sim.getFlightConfigurationId();

		GLScenePanel panel = createCanvas("3D flight replay view unavailable");
		if (panel == null) {
			document = null;
			simulation = null;
			flightData = null;
			replayConfigurationId = null;
			return;
		}
		installCanvas(panel);
		startRenderLoop();
		debug("setSimulation done");
	}

	void clearDoc() {
		debug("clearDoc");
		replayGeneration.incrementAndGet();
		Replay current = replay;
		if (current != null) {
			current.orchestrator().setFlightFrameListener(null);
		}
		replay = null;
		stopRenderLoop();
		if (glPanel != null) {
			RENDER_SCHEDULER.awaitQuiescence(RENDER_SHUTDOWN_TIMEOUT_MS);
			disposeCurrentCanvas(glPanel);
		}
		pendingCanvasRebuild.set(null);
		document = null;
		simulation = null;
		flightData = null;
		replayConfigurationId = null;
	}

	private GLScenePanel createCanvas(String unavailableMessage) {
		try {
			return new GLScenePanel(document.getRocket(), null, replayConfigurationId);
		} catch (UnsatisfiedLinkError | ExceptionInInitializerError e) {
			log.warn("{}: LWJGL native libraries not found for {}/{}.",
					unavailableMessage, System.getProperty("os.name"), System.getProperty("os.arch"), e);
			return null;
		}
	}

	private void installCanvas(GLScenePanel panel) {
		glPanel = panel;
		panel.setFlightReplayInteraction(true);
		panel.setPanModeEnabled(panModeEnabled && cameraMode != FlightCameraMode.PAD);
		panel.setRenderActivityCallback(this::markDirty);
		panel.setRenderRequestCallback(this::requestRenderNow);
		long generation = replayGeneration.get();
		panel.setInitializationHook(orchestrator ->
				initializeReplay(orchestrator, panel, generation));
		panel.setGraphicsResetCallback(() -> requestCanvasRebuild(panel));
		panel.setGlInitFailureCallback(() -> SwingUtilities.invokeLater(() -> showGLInitFailureUI(panel)));
		earliestRenderAtMs = System.currentTimeMillis() + STARTUP_RENDER_DELAY_MS;
		add(panel, BorderLayout.CENTER);
		revalidate();
		repaint();
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
		dirty.set(true);
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
		if (panel.hasGlInitFailed()) {
			stopRenderLoop();
			return;
		}
		if (System.currentTimeMillis() < earliestRenderAtMs) {
			dirty.set(true);
			return;
		}
		if (!panel.isDisplayable() || !panel.isShowing()) {
			return;
		}
		if (panel.getWidth() <= 0 || panel.getHeight() <= 0) {
			return;
		}
		panel.render();
		processPendingCanvasRebuild(panel);
	}

	@Override
	public boolean isRenderActive() {
		return renderLoopRunning && glPanel != null && document != null && flightData != null;
	}

	@Override
	public boolean shouldRenderOnTick() {
		Replay current = replay;
		if (current != null && (current.clock().getRate() != 0.0
				|| current.orchestrator().getFlightCamera().isTransitioning())) {
			dirty.set(false);
			return true;
		}
		return dirty.getAndSet(false);
	}

	private void markDirty() {
		dirty.set(true);
	}

	void requestRenderNow() {
		markDirty();
		if (renderLoopRunning) {
			RENDER_SCHEDULER.requestImmediate(this);
		}
	}

	@Override
	public void renderScheduledFrame() {
		renderFrame();
	}

	@Override
	public String getRenderDebugName() {
		return "Flight3DPanel";
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

	private void processPendingCanvasRebuild(GLScenePanel panel) {
		if (!pendingCanvasRebuild.compareAndSet(panel, null)) {
			return;
		}
		rebuildCanvasAfterBlankDefaultFramebuffer(panel);
	}

	private void rebuildCanvasAfterBlankDefaultFramebuffer(GLScenePanel failedPanel) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> rebuildCanvasAfterBlankDefaultFramebuffer(failedPanel));
			return;
		}
		if (glPanel != failedPanel || document == null || flightData == null) {
			return;
		}

		boolean resumeRenderLoop = renderLoopRunning;
		stopRenderLoop();
		RENDER_SCHEDULER.awaitQuiescence(RENDER_SHUTDOWN_TIMEOUT_MS);
		if (glPanel != failedPanel || document == null || flightData == null) {
			if (resumeRenderLoop && glPanel != null) {
				startRenderLoop();
			}
			return;
		}

		disposeCurrentCanvas(failedPanel);
		GLScenePanel panel = createCanvas("3D flight replay view unavailable during recovery");
		if (panel == null) {
			return;
		}
		installCanvas(panel);
		if (resumeRenderLoop) {
			startRenderLoop();
		}
	}

	private void disposeCurrentCanvas(GLScenePanel panel) {
		panel.setInitializationHook(null);
		panel.setGraphicsResetCallback(null);
		panel.setRenderActivityCallback(null);
		panel.setRenderRequestCallback(null);
		if (IS_MACOS) {
			// Keep the same teardown order as PhotoPanel: detach first so cleanup avoids
			// re-entering the macOS JAWT surface path after peer teardown has begun.
			remove(panel);
			glPanel = null;
			panel.cleanup();
		} else {
			panel.cleanup();
			remove(panel);
			glPanel = null;
		}
		revalidate();
		repaint();
	}

	/** Builds the replay scene once the canvas's orchestrator exists. Runs on the render thread. */
	private void initializeReplay(Scene3DOrchestrator orchestrator, GLScenePanel initializedPanel, long generation) {
		FlightData data = flightData;
		OpenRocketDocument doc = document;
		if (data == null || doc == null) {
			return;
		}

		RenderingConfiguration config = orchestrator.getRenderingConfiguration();
		config.getDisplay().setMode(DisplaySettings.RenderMode.FINISHED);
		config.getVisualEffects().setCaretsVisible(false);
		config.getVisualEffects().setRotateRocketOnDrag(false);
		// The live particle simulation is tuned for the close-up pad views and does not read at
		// flight scale; the replay drives the smoke and flame renderers itself (ReplayExhaust).
		config.getVisualEffects().setParticleEffectsEnabled(false);
		config.getVisualEffects().setFlameExposureScale(FLAME_EXPOSURE);
		orchestrator.rebuildRocketScene(false);
		Scene scene = orchestrator.getScene();
		prepareRocketObjects(scene);
		LaunchSiteScenery.addSky(scene);

		FlightReplayData replayData = new FlightReplayData(data, doc.getRocket());
		CameraControls cameraControls = orchestrator.getCameraController();
		// The design view's rocket center stands in for bodies without rendered geometry.
		Vector3f rocketCenter = cameraControls.computeRocketCenter();
		ReplayPoses poses = ReplayPoses.create(scene, replayData, data, rocketCenter);
		orchestrator.bindFlightPosesToRocket(poses.providersByStage(), poses.primaryProvider(),
				replayData.getStartTime(), replayData.getEndTime());
		// The cameras orbit the tracked body's middle, not the rocket's nose.
		orchestrator.getFlightCamera().setTrackCenterOffset(poses.bodies().get(0).centerOffset());

		Vector3f rocketSize = cameraControls.computeRocketSize();
		// The rocket's long axis runs along X in the unposed scene.
		float rocketLength = rocketSize != null ? Math.max(rocketSize.x, 1.0f) : RenderingConstants.WORLD_SCALE;
		// Pad by the rocket's largest dimension so its body is not clipped at the trajectory's ends.
		float padding = rocketSize != null ? maxComponent(rocketSize) * 0.5f : 0.0f;
		ReplayPoses.Bounds bounds = poses.trajectoryBounds(replayData.getStartTime(), replayData.getEndTime(),
				TrajectoryTrails.SAMPLES, padding);
		float flightSize = bounds != null ? maxComponent(bounds.dimensions()) : 0.0f;
		Camera camera = cameraControls.getCamera();
		LaunchSiteScenery.addGroundAndHaze(scene, flightSize, camera.getFieldOfView());
		LaunchSiteScenery.addLaunchPad(scene, rocketLength);

		TrajectoryTrails trails = new TrajectoryTrails(scene);
		if (bounds != null) {
			trails.build(poses, replayData, rocketCenter, flightSize, rocketLength, trailVisible,
					cameraMode == FlightCameraMode.OVERVIEW);
		}
		ReplayExhaust exhaust = new ReplayExhaust();
		exhaust.build(scene, config, orchestrator.getMotorExhaustMounts(), poses, replayData, trails.radius(),
				rocketLength);
		RecoveryDevices recovery = new RecoveryDevices();
		recovery.build(scene, replayData, poses, rocketLength);
		log.info("Flight replay: {} flying bod(ies), {} smoke puff(s), {} flame(s), {} recovery device(s)",
				poses.bodies().size(), exhaust.puffCount(), exhaust.flameCount(), recovery.count());

		// Replay subjects can be thousands of units from the eye; keep depth precision there.
		camera.setNearPlaneScalesWithDistance(true);
		FlightTargetMarker targetMarker = new FlightTargetMarker(camera);
		FlightOrientationGizmo gizmo = new FlightOrientationGizmo();
		WindIndicator windIndicator = new WindIndicator(initializedPanel::getHeight);
		orchestrator.getRenderer().setFrameOverlay(new FrameOverlay() {
			@Override
			public void render(Matrix4f cameraViewMatrix, int width, int height) {
				targetMarker.render(cameraViewMatrix, width, height);
				gizmo.render(cameraViewMatrix, width, height);
				windIndicator.render(cameraViewMatrix, width, height);
			}

			@Override
			public void cleanup() {
				targetMarker.cleanup();
				gizmo.cleanup();
				windIndicator.cleanup();
			}
		});

		PlaybackClock clock = orchestrator.getPlaybackClock();
		clock.setRate(0.0);
		Replay ready = new Replay(orchestrator, clock, poses, trails, exhaust, recovery, targetMarker, windIndicator,
				bounds, rocketLength, camera.getAngleX(), camera.getAngleY(), camera.getFieldOfView());
		trackedBodyIndex = 0;
		replay = ready;
		applyCameraMode(ready, cameraMode);
		ready.camera().skipTransition();
		orchestrator.setFlightFrameListener(time -> onFlightFrame(ready, time));

		BiConsumer<PlaybackClock, FlightReplayData> callback = replayReadyCallback;
		if (callback != null) {
			SwingUtilities.invokeLater(() -> {
				if (generation == replayGeneration.get() && glPanel == initializedPanel && document == doc) {
					callback.accept(clock, replayData);
				}
			});
		}
	}

	/** Keeps the rocket drawn over the trail running through it, and out of selection. */
	private static void prepareRocketObjects(Scene scene) {
		scene.setSelection(List.of());
		for (SceneObject object : scene.getObjects()) {
			object.setSelected(false);
			object.setSelectable(false);
			if (object.getRocketComponent() != null) {
				object.setRenderInForeground(true);
			}
		}
	}

	/**
	 * Brings the replay to the playback time. Runs on the render thread every frame, after the
	 * camera behavior and before rendering.
	 */
	private void onFlightFrame(Replay current, double time) {
		current.exhaust().update(time, exhaustVisible);
		current.recovery().update(time);
		int bodyIndex = trackedBodyIndex;
		if (bodyIndex >= 0 && bodyIndex < current.poses().bodies().size()) {
			ReplayPoses.TrackedBody body = current.poses().bodies().get(bodyIndex);
			current.targetMarker().setTarget(body.centerAt(time), current.rocketLength());
			current.windIndicator().setWind(body.wind(), time);
		}

		PlaybackClock clock = current.clock();
		double span = Math.max(1.0e-9, clock.getEnd() - clock.getStart());
		double fraction = Math.max(0.0, Math.min(1.0, (time - clock.getStart()) / span));
		CameraControls cameraControls = current.orchestrator().getCameraController();
		Camera camera = cameraControls.getCamera();
		// A narrowed (telephoto) lens magnifies like moving closer; size decorations by that.
		float effectiveDistance = camera.getDistance() * (float) (Math.tan(camera.getFieldOfView() / 2.0)
				/ Math.tan(current.baseFieldOfView() / 2.0));
		boolean overview = cameraMode == FlightCameraMode.OVERVIEW;
		// A transition shows interpolated distances, not the overview's fit.
		boolean overviewFitted = overview && cameraControls.isZoomFitting() && !current.camera().isTransitioning();
		current.trails().update(time, fraction, effectiveDistance, overviewFitted, !overview);
	}

	/** Switches the camera mode; the change is applied on the render thread. */
	void setCameraMode(FlightCameraMode mode) {
		FlightCameraMode previous = this.cameraMode;
		this.cameraMode = mode;
		if (mode == FlightCameraMode.PAD) {
			setPanModeEnabled(false);
		}
		Replay current = replay;
		if (current == null) {
			// Applied when the replay is ready.
			return;
		}
		if (previous == FlightCameraMode.PAD && mode != FlightCameraMode.PAD) {
			// The pad view's angles look steeply up from the ground; kept, they would put the
			// orbiting views' eye far below it. Start them from the replay's opening angles.
			restoreInitialCameraAngles(current);
		}
		applyCameraMode(current, mode);
	}

	FlightCameraMode getCameraMode() {
		return cameraMode;
	}

	/** Resets the current camera mode's view, including any orbit the user applied. */
	void fitView() {
		Replay current = replay;
		if (current != null) {
			restoreInitialCameraAngles(current);
			applyCameraMode(current, cameraMode);
			requestRenderNow();
		}
	}

	void zoomIn() {
		zoomBy(1.0f);
	}

	void zoomOut() {
		zoomBy(-1.0f);
	}

	private void zoomBy(float scrollAmount) {
		Replay current = replay;
		if (current != null) {
			current.orchestrator().zoomFlightCamera(scrollAmount);
			requestRenderNow();
		}
	}

	void setPanModeEnabled(boolean enabled) {
		boolean accepted = enabled && cameraMode != FlightCameraMode.PAD;
		panModeEnabled = accepted;
		GLScenePanel panel = glPanel;
		if (panel != null) {
			panel.setPanModeEnabled(accepted);
		}
		requestRenderNow();
	}

	void setTrailVisible(boolean visible) {
		trailVisible = visible;
		Replay current = replay;
		if (current != null) {
			current.orchestrator().enqueueGlTask(() ->
					current.trails().setShown(visible, cameraMode == FlightCameraMode.OVERVIEW));
		}
		requestRenderNow();
	}

	void setExhaustVisible(boolean visible) {
		exhaustVisible = visible;
		requestRenderNow();
	}

	/** Tracks another flying body with the follow and pad cameras, markers and telemetry. */
	void setTrackedBody(int index) {
		trackedBodyIndex = index;
		Replay current = replay;
		if (current != null && index >= 0 && index < current.poses().bodies().size()) {
			ReplayPoses.TrackedBody body = current.poses().bodies().get(index);
			current.orchestrator().enqueueGlTask(() -> {
				current.camera().setTrackTarget(body.provider(), body.centerOffset());
				current.trails().trackBody(body);
			});
		}
		requestRenderNow();
	}

	/** All trail tube objects, for tests; call on the render thread. */
	List<SceneObject> trailObjects() {
		Replay current = replay;
		return current != null ? current.trails().trailObjects() : List.of();
	}

	private void applyCameraMode(Replay current, FlightCameraMode mode) {
		// The position marker is sized for the trajectory and would dwarf the rocket up close,
		// so it shows only in the overview. The trails are resized for the new camera next frame.
		boolean markerShown = mode == FlightCameraMode.OVERVIEW;
		current.orchestrator().enqueueGlTask(() -> {
			current.trails().setShown(trailVisible, markerShown);
			current.trails().invalidate();
		});
		current.orchestrator().setFlightPanEnabled(mode != FlightCameraMode.PAD);

		FlightCameraRig camera = current.camera();
		switch (mode) {
			case FOLLOW -> camera.follow();
			case PAD -> {
				// A launch-footage viewpoint: a few meters out from the pad at head height.
				float away = Math.max(7.0f * RenderingConstants.WORLD_SCALE, current.rocketLength() * 4.0f);
				camera.watchFromPad(new Vector3f(away, 1.7f * RenderingConstants.WORLD_SCALE, away));
			}
			default -> {
				if (current.bounds() != null) {
					camera.frameTrajectory(current.bounds().center(), current.bounds().dimensions());
				} else {
					camera.free();
					current.orchestrator().focusOnRocket();
				}
			}
		}
	}

	/** Queued before a mode's refit, which then frames its view from the original angles. */
	private static void restoreInitialCameraAngles(Replay current) {
		current.orchestrator().enqueueGlTask(() -> {
			Camera camera = current.orchestrator().getCameraController().getCamera();
			camera.setAngleX(current.initialAngleX());
			camera.setAngleY(current.initialAngleY());
			camera.resetViewOffset();
		});
	}

	private static float maxComponent(Vector3f vector) {
		return Math.max(vector.x, Math.max(vector.y, vector.z));
	}

	private static void debug(String message) {
		if (!DEBUG) {
			return;
		}
		System.out.println("[Flight3DPanel][" + Thread.currentThread().getName() + "] " + message);
	}
}
