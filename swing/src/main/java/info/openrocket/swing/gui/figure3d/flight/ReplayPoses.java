package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.swing.gui.figure3d.animation.PoseProvider;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The replay's trajectories as rendered: each flying body rotates about its own center (see
 * {@link BodyCenteredPoseProvider}) and the whole rocket is lifted so it starts resting on the
 * ground. Also resolves which trajectory, center, wind and simulation branch belong to a body,
 * stage or component. Built once per replay on the render thread, before the first posed frame
 * while object transforms are still rocket-local; immutable afterwards.
 */
final class ReplayPoses {
	private static final Logger log = LoggerFactory.getLogger(ReplayPoses.class);

	/** A separately flying body: its trajectory, geometric center and simulation branch. */
	record TrackedBody(PoseProvider provider, Vector3f centerOffset, WindField wind, FlightDataBranch branch) {
		/** The body's geometric center at the given time. */
		Vector3f centerAt(double time) {
			return pointOnBody(provider, centerOffset, time);
		}
	}

	/** An axis-aligned box around the flight. */
	record Bounds(Vector3f center, Vector3f dimensions) {
	}

	private final Map<AxialStage, PoseProvider> providersByStage;
	private final PoseProvider primaryProvider;
	private final List<TrackedBody> bodies;

	private ReplayPoses(Map<AxialStage, PoseProvider> providersByStage, PoseProvider primaryProvider,
			List<TrackedBody> bodies) {
		this.providersByStage = providersByStage;
		this.primaryProvider = primaryProvider;
		this.bodies = List.copyOf(bodies);
	}

	/**
	 * @param fallbackCenter the rocket-local center used for a body without rendered geometry
	 */
	static ReplayPoses create(SceneView scene, FlightReplayData replayData, FlightData data, Vector3f fallbackCenter) {
		// 1. Rotate each body about its own center, tracking which stages it is attached to.
		Map<PoseProvider, BodyCenteredPoseProvider> centeredByOriginal = new IdentityHashMap<>();
		for (FlightReplayData.FlightBody body : replayData.getFlightBodies()) {
			centeredByOriginal.put(body.provider(), centerOnItself(scene, replayData, body, fallbackCenter));
		}
		Map<AxialStage, PoseProvider> centeredByStage = new LinkedHashMap<>();
		replayData.getProvidersByStage().forEach((stage, provider) ->
				centeredByStage.put(stage, centeredOrSelf(centeredByOriginal, provider)));
		PoseProvider centeredPrimary = centeredOrSelf(centeredByOriginal, replayData.getPrimaryProvider());

		// 2. Lift everything so the rocket starts on the ground. Wrap each trajectory once:
		// stages flying together must keep sharing one provider, which is how the trails and
		// tracked bodies tell the bodies apart.
		float lift = startGroundLift(scene, centeredByStage, centeredPrimary, replayData.getStartTime());
		Map<PoseProvider, PoseProvider> lifted = new IdentityHashMap<>();
		Map<AxialStage, PoseProvider> byStage = new LinkedHashMap<>();
		centeredByStage.forEach((stage, provider) -> byStage.put(stage, lifted(lifted, provider, lift)));
		PoseProvider primary = lifted(lifted, centeredPrimary, lift);

		// 3. Resolve each body to its final trajectory, center, wind and branch.
		List<TrackedBody> bodies = new ArrayList<>();
		for (FlightReplayData.FlightBody body : replayData.getFlightBodies()) {
			PoseProvider provider = providerForStage(body.stages().get(0), byStage, primary);
			FlightDataBranch branch = data.getBranch(body.branchIndex());
			bodies.add(new TrackedBody(provider, centeredByOriginal.get(body.provider()).getBodyCenter(),
					WindField.fromBranch(branch), branch));
		}
		return new ReplayPoses(byStage, primary, bodies);
	}

	Map<AxialStage, PoseProvider> providersByStage() {
		return providersByStage;
	}

	PoseProvider primaryProvider() {
		return primaryProvider;
	}

	/** The flying bodies, primary first. */
	List<TrackedBody> bodies() {
		return bodies;
	}

	/** Each distinct trajectory once; stages share one until they separate. */
	Collection<PoseProvider> distinctProviders() {
		Set<PoseProvider> providers = Collections.newSetFromMap(new IdentityHashMap<>());
		providers.add(primaryProvider);
		providers.addAll(providersByStage.values());
		return providers;
	}

	/** The body flying on the given trajectory, or the primary body. */
	TrackedBody bodyFor(PoseProvider provider) {
		for (TrackedBody body : bodies) {
			if (body.provider() == provider) {
				return body;
			}
		}
		return bodies.get(0);
	}

	/** The trajectory a component flies on; components outside any stage follow the primary one. */
	PoseProvider providerFor(RocketComponent component) {
		return providerForStage(stageFor(component), providersByStage, primaryProvider);
	}

	PoseProvider providerForStage(AxialStage stage) {
		return providerForStage(stage, providersByStage, primaryProvider);
	}

	/**
	 * The box the trajectory origins sweep over the playback, padded so a rocket of the given
	 * size is not clipped at its ends, or null without a finite trajectory.
	 */
	Bounds trajectoryBounds(double startTime, double endTime, int samples, float padding) {
		Vector3f min = new Vector3f(Float.POSITIVE_INFINITY);
		Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY);
		for (PoseProvider provider : distinctProviders()) {
			for (int i = 0; i <= samples; i++) {
				Vector3f position = provider.getPosition(startTime + (endTime - startTime) * i / samples);
				min.min(position);
				max.max(position);
			}
		}
		if (!Float.isFinite(min.x) || !Float.isFinite(max.x)) {
			return null;
		}
		min.sub(padding, padding, padding);
		max.add(padding, padding, padding);
		return new Bounds(new Vector3f(min).add(max).mul(0.5f), new Vector3f(max).sub(min));
	}

	/** Where a rocket-local point on a body is at the given time; a null point is the pose origin. */
	static Vector3f pointOnBody(PoseProvider provider, Vector3f localPoint, double time) {
		Vector3f position = provider.getPosition(time);
		if (localPoint != null) {
			position.add(provider.getOrientation(time).transform(new Vector3f(localPoint)));
		}
		return position;
	}

	/** The stage a component belongs to, or null for scenery and detached components. */
	static AxialStage stageFor(RocketComponent component) {
		if (component == null) {
			return null;
		}
		try {
			return component instanceof AxialStage stage ? stage : component.getStage();
		} catch (IllegalStateException e) {
			return null;
		}
	}

	private static PoseProvider providerForStage(AxialStage stage, Map<AxialStage, PoseProvider> providersByStage,
			PoseProvider primaryProvider) {
		// The replay data's map is immutable and rejects null lookups.
		PoseProvider provider = stage != null ? providersByStage.get(stage) : null;
		return provider != null ? provider : primaryProvider;
	}

	private static PoseProvider centeredOrSelf(Map<PoseProvider, BodyCenteredPoseProvider> centered,
			PoseProvider provider) {
		PoseProvider result = centered.get(provider);
		return result != null ? result : provider;
	}

	private static PoseProvider lifted(Map<PoseProvider, PoseProvider> lifted, PoseProvider provider, float lift) {
		if (lift <= 1.0e-4f) {
			return provider;
		}
		return lifted.computeIfAbsent(provider, original -> new OffsetPoseProvider(original, new Vector3f(0.0f, lift, 0.0f)));
	}

	/** Rotates a body about the center of whichever stages it is attached to at each moment. */
	private static BodyCenteredPoseProvider centerOnItself(SceneView scene, FlightReplayData replayData,
			FlightReplayData.FlightBody body, Vector3f fallbackCenter) {
		AxialStage stage = body.stages().get(0);
		List<AxialStage> group = replayData.getAttachedStages(stage, replayData.getStartTime());
		List<Double> switchTimes = new ArrayList<>();
		List<Vector3f> centers = new ArrayList<>();
		centers.add(stageGroupCenter(scene, Set.copyOf(group), fallbackCenter));
		for (double time : replayData.getSeparationTimes()) {
			List<AxialStage> attached = replayData.getAttachedStages(stage, time);
			if (!attached.equals(group)) {
				switchTimes.add(time);
				centers.add(stageGroupCenter(scene, Set.copyOf(attached), fallbackCenter));
				group = attached;
			}
		}
		return new BodyCenteredPoseProvider(body.provider(), switchTimes, centers);
	}

	/** Center of the rocket-local bounds of every object in the given stages, or the fallback. */
	private static Vector3f stageGroupCenter(SceneView scene, Set<AxialStage> stages, Vector3f fallback) {
		Vector3f min = new Vector3f(Float.POSITIVE_INFINITY);
		Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY);
		Vector3f boundsMin = new Vector3f();
		Vector3f boundsMax = new Vector3f();
		Vector3f corner = new Vector3f();
		for (SceneObject object : scene.getObjects()) {
			Mesh mesh = object.getMesh();
			// Scenery has no stage, and the immutable set rejects null lookups.
			AxialStage stage = stageFor(object.getRocketComponent());
			if (mesh == null || stage == null || !stages.contains(stage)) {
				continue;
			}
			mesh.getBoundsMin(boundsMin);
			mesh.getBoundsMax(boundsMax);
			for (int i = 0; i < 8; i++) {
				corner.set((i & 1) == 0 ? boundsMin.x : boundsMax.x,
						(i & 2) == 0 ? boundsMin.y : boundsMax.y,
						(i & 4) == 0 ? boundsMin.z : boundsMax.z);
				object.getModelMatrix().transformPosition(corner);
				min.min(corner);
				max.max(corner);
			}
		}
		if (!Float.isFinite(min.x) || !Float.isFinite(max.x)) {
			return fallback;
		}
		return min.add(max).mul(0.5f);
	}

	/** How far the rocket must be lifted so its lowest point starts on the ground. */
	private static float startGroundLift(SceneView scene, Map<AxialStage, PoseProvider> providersByStage,
			PoseProvider primaryProvider, double startTime) {
		float minY = Float.POSITIVE_INFINITY;
		int contributingObjects = 0;
		Matrix4f modelTransform = new Matrix4f();
		Vector3f boundsMin = new Vector3f();
		Vector3f boundsMax = new Vector3f();
		for (SceneObject object : scene.getObjects()) {
			Mesh mesh = object.getMesh();
			if (object.getRocketComponent() == null || mesh == null) {
				continue;
			}
			// Components without their own trajectory measure against the primary one, so the
			// lift never silently collapses to zero and leaves the rocket sunk into the ground.
			PoseProvider provider = providerForStage(stageFor(object.getRocketComponent()), providersByStage,
					primaryProvider);
			modelTransform.identity()
					.translate(provider.getPosition(startTime))
					.rotate(provider.getOrientation(startTime))
					.mul(object.getModelMatrix());
			mesh.getBoundsMin(boundsMin);
			mesh.getBoundsMax(boundsMax);
			minY = Math.min(minY, lowestTransformedCornerY(boundsMin, boundsMax, modelTransform));
			contributingObjects++;
		}
		float lift = (!Float.isFinite(minY) || minY >= 0.0f) ? 0.0f : -minY;
		log.info("Flight replay ground seating: {} rocket object(s), lowest Y {}, applying lift {}",
				contributingObjects, Float.isFinite(minY) ? minY : Float.NaN, lift);
		return lift;
	}

	/** The minimum world-space Y of a mesh's axis-aligned bounds after the given transform. */
	static float lowestTransformedCornerY(Vector3f boundsMin, Vector3f boundsMax, Matrix4f transform) {
		float minY = Float.POSITIVE_INFINITY;
		Vector3f corner = new Vector3f();
		for (int i = 0; i < 8; i++) {
			corner.set((i & 1) == 0 ? boundsMin.x : boundsMax.x,
					(i & 2) == 0 ? boundsMin.y : boundsMax.y,
					(i & 4) == 0 ? boundsMin.z : boundsMax.z);
			transform.transformPosition(corner);
			minY = Math.min(minY, corner.y);
		}
		return minY;
	}

	/** A trajectory shifted by a constant world offset. */
	private static final class OffsetPoseProvider implements PoseProvider {
		private final PoseProvider delegate;
		private final Vector3f offset;

		private OffsetPoseProvider(PoseProvider delegate, Vector3f offset) {
			this.delegate = delegate;
			this.offset = new Vector3f(offset);
		}

		@Override
		public Vector3f getPosition(double t) {
			return new Vector3f(delegate.getPosition(t)).add(offset);
		}

		@Override
		public Quaternionf getOrientation(double t) {
			return delegate.getOrientation(t);
		}

		@Override
		public Vector3f getLinearVelocity(double t) {
			return delegate.getLinearVelocity(t);
		}

		@Override
		public Vector3f getAngularVelocity(double t) {
			return delegate.getAngularVelocity(t);
		}

		@Override
		public double getStartTime() {
			return delegate.getStartTime();
		}

		@Override
		public double getEndTime() {
			return delegate.getEndTime();
		}
	}
}
