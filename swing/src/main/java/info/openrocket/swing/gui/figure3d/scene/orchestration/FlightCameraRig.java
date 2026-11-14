package info.openrocket.swing.gui.figure3d.scene.orchestration;

import info.openrocket.core.util.MathUtil;
import info.openrocket.swing.gui.figure3d.animation.PoseProvider;
import info.openrocket.swing.gui.figure3d.constants.CameraConstants;
import info.openrocket.swing.gui.figure3d.scene.controllers.CameraControls;
import info.openrocket.swing.gui.figure3d.scene.graph.Camera;
import org.joml.Vector3f;

/**
 * The flight replay's camera behaviors, applied once per frame while a replay plays:
 * <ul>
 *   <li><b>Free</b>: the user orbits freely, after an optional one-shot fit of the whole flight.</li>
 *   <li><b>Follow</b>: the orbit pivot tracks the chosen body, keeping the user's pan and zoom.</li>
 *   <li><b>Pad</b>: a fixed eye near the pad turns to watch the body through a telephoto lens
 *   that narrows as it climbs, keeping its launch size on screen.</li>
 * </ul>
 * Switching behavior (or tracked body) animates the camera from the pose last shown to the new
 * one. Setters may be called from the Swing thread and take effect on the next frame; the frame
 * methods run on the render thread, in the order {@link #beginFrame()}, {@link #beforeInput()},
 * {@link #update(double, double, float)}.
 */
public final class FlightCameraRig {
	private static final float FOLLOW_FRAME_MARGIN = 1.8f;
	private static final float OVERVIEW_CLOSEST_DISTANCE_FACTOR = 0.001f;
	private static final float OVERVIEW_FARTHEST_DISTANCE_FACTOR = 20.0f;
	private static final float PAD_MIN_FIELD_OF_VIEW = (float) Math.toRadians(0.5);
	private static final float PAD_MAX_FIELD_OF_VIEW = (float) Math.toRadians(100.0);
	private static final float TRANSITION_SECONDS = 0.6f;
	// A paused replay renders on demand, so the first frame's delta can span the whole pause.
	private static final float MAX_TRANSITION_STEP_SECONDS = 1.0f / 30.0f;

	private enum Behavior { FREE, FOLLOW, PAD }

	private final CameraControls cameraController;

	// The tracked body and the offset from its pose origin to its geometric center, published
	// together so a switch never pairs one body's trajectory with another's center.
	private volatile TrackTarget trackTarget = null;
	private volatile Behavior behavior = Behavior.FREE;

	// Follow: the user's pan relative to the tracked body, and the pivot before this frame's input.
	private volatile boolean pendingFollowFit = false;
	private final Vector3f followPanOffset = new Vector3f();
	private Vector3f centerBeforeInput = null;

	// Pad: the fixed eye; the wheel scales the telephoto lens instead of moving the eye.
	private volatile Vector3f padEye = null;
	private volatile boolean pendingPadReset = false;
	private float padZoomScale = 1.0f;
	private float lastAppliedPadDistance = Float.NaN;
	private float padReferenceDistance = Float.NaN;
	private float baseFieldOfView = Float.NaN;

	// Free: the whole-flight framing applied once when entering the behavior.
	private volatile Vector3f trajectoryCenter = null;
	private volatile Vector3f trajectoryDimensions = null;
	private volatile boolean pendingTrajectoryFit = false;

	// Transitions: each blended frame shows an interpolated pose; the next frame first restores
	// the behavior's own pose, so the behavior (and input) never sees the interpolation.
	private volatile boolean pendingTransition = false;
	private Camera.Pose transitionFrom = null;
	private Camera.Pose transitionTarget = null;
	private float transitionElapsed = 0.0f;

	FlightCameraRig(CameraControls cameraController) {
		this.cameraController = cameraController;
	}

	private record TrackTarget(PoseProvider provider, Vector3f centerOffset) {
		private TrackTarget {
			centerOffset = centerOffset != null ? new Vector3f(centerOffset) : null;
		}

		/** The tracked body's geometric center at the given time. */
		private Vector3f pivotAt(double time) {
			Vector3f pivot = provider.getPosition(time);
			if (centerOffset != null) {
				pivot.add(provider.getOrientation(time).transform(new Vector3f(centerOffset)));
			}
			return pivot;
		}
	}

	// ---------- Behaviors (callable from the Swing thread) ----------

	/** Lets the user orbit freely around wherever the camera is. */
	public void free() {
		lastAppliedPadDistance = Float.NaN;
		pendingTransition = true;
		behavior = Behavior.FREE;
	}

	/** Frames the tracked body and keeps the orbit pivot on it. */
	public void follow() {
		lastAppliedPadDistance = Float.NaN;
		pendingTransition = true;
		pendingFollowFit = true;
		behavior = Behavior.FOLLOW;
	}

	/** Watches the tracked body from a fixed eye position near the pad, like launch footage. */
	public void watchFromPad(Vector3f eyePosition) {
		padEye = eyePosition != null ? new Vector3f(eyePosition) : null;
		pendingPadReset = true;
		pendingTransition = true;
		behavior = Behavior.PAD;
	}

	/**
	 * Frames the whole flight once, then lets the user orbit and zoom freely around it.
	 *
	 * @param center     center of the trajectory's bounding box
	 * @param dimensions size of the trajectory's bounding box
	 */
	public void frameTrajectory(Vector3f center, Vector3f dimensions) {
		trajectoryCenter = center != null ? new Vector3f(center) : null;
		trajectoryDimensions = dimensions != null ? new Vector3f(dimensions) : null;
		behavior = Behavior.FREE;
		lastAppliedPadDistance = Float.NaN;
		pendingTrajectoryFit = true;
		pendingTransition = true;
	}

	/**
	 * Tracks another flying body: its trajectory and the offset from its pose origin to its
	 * geometric center. The follow view keeps its pan and zoom; the switch is animated.
	 */
	public void setTrackTarget(PoseProvider provider, Vector3f centerOffset) {
		if (provider == null) {
			throw new IllegalArgumentException("provider is null");
		}
		trackTarget = new TrackTarget(provider, centerOffset);
		pendingTransition = true;
	}

	/** Changes the tracked trajectory without animating, keeping the center offset. */
	void setTrackProvider(PoseProvider provider) {
		TrackTarget current = trackTarget;
		trackTarget = new TrackTarget(provider, current != null ? current.centerOffset() : null);
	}

	/** Changes the offset from the tracked trajectory's pose origin to the body's center. */
	public void setTrackCenterOffset(Vector3f offset) {
		TrackTarget current = trackTarget;
		if (current != null) {
			trackTarget = new TrackTarget(current.provider(), offset);
		}
	}

	/** Whether a camera transition between behaviors is still animating. */
	public boolean isTransitioning() {
		return pendingTransition || transitionFrom != null;
	}

	/** Applies the requested behavior immediately, e.g. when a replay first opens. */
	public void skipTransition() {
		pendingTransition = false;
		transitionFrom = null;
	}

	// ---------- Frame (render thread) ----------

	/** Starts a frame: captures a requested transition's start, and undoes last frame's blend. */
	void beginFrame() {
		Camera camera = cameraController.getCamera();
		if (pendingTransition) {
			pendingTransition = false;
			// Start from what is on screen, which may itself be mid-transition.
			transitionFrom = camera.capturePose();
			transitionElapsed = 0.0f;
		}
		if (transitionTarget != null) {
			camera.restorePose(transitionTarget);
			transitionTarget = null;
		}
	}

	/** Records the pivot before input, so follow keeps only the user's input delta. */
	void beforeInput() {
		centerBeforeInput = new Vector3f(cameraController.getCamera().getCenterOfInterest());
	}

	/**
	 * Applies the current behavior for the playback time, then blends any transition.
	 *
	 * @param time      playback time
	 * @param startTime playback start, where the pad lens is referenced
	 * @param deltaTime wall-clock seconds since the previous frame
	 */
	void update(double time, double startTime, float deltaTime) {
		Camera camera = cameraController.getCamera();
		Behavior current = behavior;
		TrackTarget target = trackTarget;
		if (current != Behavior.PAD && Float.isFinite(baseFieldOfView) && camera.getFieldOfView() != baseFieldOfView) {
			// Leaving the telephoto view: restore the lens before any fit uses it.
			camera.setFieldOfView(baseFieldOfView);
		}
		if (current == Behavior.PAD && target != null) {
			updatePad(camera, target, time, startTime);
		} else if (current == Behavior.FOLLOW && target != null) {
			updateFollow(camera, target.pivotAt(time));
		} else if (pendingTrajectoryFit) {
			pendingTrajectoryFit = false;
			Vector3f center = trajectoryCenter;
			Vector3f dimensions = trajectoryDimensions;
			if (center != null && dimensions != null) {
				cameraController.focusOnBounds(center, dimensions,
						OVERVIEW_CLOSEST_DISTANCE_FACTOR, OVERVIEW_FARTHEST_DISTANCE_FACTOR);
			}
		}
		blendTransition(camera, deltaTime);
	}

	private void updatePad(Camera camera, TrackTarget target, double time, double startTime) {
		Vector3f eye = padEye;
		if (eye == null) {
			return;
		}
		if (pendingPadReset) {
			pendingPadReset = false;
			padZoomScale = 1.0f;
			lastAppliedPadDistance = Float.NaN;
			padReferenceDistance = eye.distance(target.pivotAt(startTime));
			if (!Float.isFinite(baseFieldOfView)) {
				baseFieldOfView = camera.getFieldOfView();
			}
		}
		// Wheel input changed the orbit distance since the last frame; read that ratio as a
		// lens zoom, then put the eye back at the pad.
		padZoomScale = updatedPadZoomScale(padZoomScale, camera.getDistance(), lastAppliedPadDistance);
		float distance = lookFrom(camera, eye, target.pivotAt(time));
		float autoTan = telephotoTanHalfFieldOfView(baseFieldOfView, padReferenceDistance, distance);
		padZoomScale = MathUtil.clamp(padZoomScale,
				(float) Math.tan(PAD_MIN_FIELD_OF_VIEW / 2.0) / autoTan,
				(float) Math.tan(PAD_MAX_FIELD_OF_VIEW / 2.0) / autoTan);
		camera.setFieldOfView(2.0 * Math.atan(autoTan * padZoomScale));
		lastAppliedPadDistance = distance;
		// This behavior owns the distance; a resize refit to the last fitted bounds would
		// otherwise be read as a huge wheel zoom on the next frame.
		cameraController.setZoomFitting(false);
	}

	private void updateFollow(Camera camera, Vector3f pivot) {
		Vector3f previousPivot = centerBeforeInput != null ? centerBeforeInput : camera.getCenterOfInterest();
		if (pendingFollowFit) {
			pendingFollowFit = false;
			// Fit a rotation-independent envelope, including room for the exhaust. Fitting the
			// unposed horizontal design crops a vertical rocket on wide windows.
			float diameter = cameraController.computeRocketSize().length() * FOLLOW_FRAME_MARGIN;
			cameraController.focusOnBounds(pivot, new Vector3f(diameter));
			// The fit clamps the zoom range to the rocket; open it back up so the user can zoom
			// well out while still tracking the flight.
			camera.setZoomLimits(Math.max(0.01f, camera.getDistance() * 0.05f), camera.getDistance() * 100.0f);
			followPanOffset.zero();
			previousPivot = new Vector3f(pivot);
			camera.setCenterOfInterest(pivot);
		}
		// Retain only the user's input delta, so a resize refit cannot move the tracking target
		// back to the location where follow mode was entered.
		trackFlightPivot(camera, previousPivot, new Vector3f(pivot).add(followPanOffset));
		followPanOffset.set(camera.getCenterOfInterest()).sub(pivot);
	}

	private void blendTransition(Camera camera, float deltaTime) {
		if (transitionFrom == null) {
			return;
		}
		transitionElapsed += Math.min(Math.max(deltaTime, 0.0f), MAX_TRANSITION_STEP_SECONDS);
		float progress = transitionElapsed / TRANSITION_SECONDS;
		if (progress >= 1.0f) {
			transitionFrom = null;
			return;
		}
		transitionTarget = camera.capturePose();
		float eased = progress * progress * (3.0f - 2.0f * progress);
		camera.restorePose(Camera.Pose.blend(transitionFrom, transitionTarget, eased));
	}

	// ---------- Camera math ----------

	/** Moves with the rocket while preserving the user's pan relative to it, including on seeks. */
	static void trackFlightPivot(Camera camera, Vector3f previousPivot, Vector3f pivot) {
		camera.setCenterOfInterest(new Vector3f(camera.getCenterOfInterest()).sub(previousPivot).add(pivot));
	}

	/**
	 * Expresses a look-from-eye-to-target view through the orbit camera's center of interest,
	 * distance and angles, widening the zoom clamp so an earlier fit cannot clip the computed
	 * distance. Returns the eye-to-target distance.
	 */
	static float lookFrom(Camera camera, Vector3f eye, Vector3f target) {
		Vector3f toEye = new Vector3f(eye).sub(target);
		float eyeDistance = Math.max(CameraConstants.MIN_DISTANCE, Math.max(0.5f, toEye.length()));
		toEye.normalize();
		camera.setZoomLimits(Math.max(CameraConstants.MIN_DISTANCE, eyeDistance * 0.1f),
				Math.max(eyeDistance * 10.0f, 10.0f));
		camera.setDistance(eyeDistance);
		camera.setAngleX((float) Math.atan2(toEye.x, toEye.z));
		camera.setAngleY((float) Math.asin(Math.max(-1.0f, Math.min(1.0f, toEye.y))));
		camera.setCenterOfInterest(target);
		// A pan offset left over from another view would shift the fixed sightline.
		camera.resetViewOffset();
		return camera.getDistance();
	}

	/** Applies the wheel's change of the orbit distance since the last frame as a lens zoom factor. */
	static float updatedPadZoomScale(float currentScale, float cameraDistance, float lastAppliedDistance) {
		if (!Float.isFinite(lastAppliedDistance) || lastAppliedDistance <= 0.0f
				|| !Float.isFinite(cameraDistance) || cameraDistance <= 0.0f) {
			return currentScale;
		}
		return currentScale * cameraDistance / lastAppliedDistance;
	}

	/**
	 * Tangent of the half field of view that keeps the rocket at its launch size on screen: the
	 * base lens until the rocket is farther than at launch, then narrowing in proportion.
	 */
	static float telephotoTanHalfFieldOfView(float baseFieldOfView, float referenceDistance, float distance) {
		float baseTan = (float) Math.tan(baseFieldOfView / 2.0);
		if (!Float.isFinite(referenceDistance) || referenceDistance <= 0.0f || distance <= referenceDistance) {
			return baseTan;
		}
		return baseTan * referenceDistance / distance;
	}
}
