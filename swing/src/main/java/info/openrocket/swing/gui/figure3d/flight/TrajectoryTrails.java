package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.swing.gui.figure3d.animation.PoseProvider;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.basic.SphereGenerator;
import info.openrocket.swing.gui.figure3d.geometry.basic.TrajectoryTrailGenerator;
import info.openrocket.swing.gui.figure3d.materials.Appearance3D;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The flight path drawn as tubes through each body's center, bright where it has flown and faded
 * where it is still to come, plus a marker at the rocket and one per notable flight event. At
 * the whole-flight zoom the rocket is only a few pixels, so these make the trajectory legible.
 *
 * <p>Each path is split into chunks built once per decoration scale; playback only toggles
 * chunk visibility and rebuilds the two short pieces either side of the playback position.
 * Elapsed and upcoming parts are separate, non-overlapping tubes (not a bright tube over a faded
 * full-length one), which avoids coaxial z-fighting. Decorations shrink as the camera closes in,
 * down to a thin guide line in the close-up views. All methods run on the render thread.
 */
final class TrajectoryTrails {
	static final int SAMPLES = 240;
	static final int CHUNK_SAMPLES = 12;
	private static final float SEPARATED_TRAIL_RADIUS_SCALE = 0.7f;
	private static final float MIN_DECORATION_SCALE = 0.04f;
	private static final float DECORATION_SCALE_REBUILD_THRESHOLD = 0.06f;
	private static final Vector3f ACTIVE_UPCOMING_COLOR = new Vector3f(0.16f, 0.42f, 0.28f);
	private static final Vector3f ACTIVE_ELAPSED_COLOR = new Vector3f(0.35f, 1.0f, 0.55f);
	private static final Vector3f SEPARATED_UPCOMING_COLOR = new Vector3f(0.40f, 0.24f, 0.12f);
	private static final Vector3f SEPARATED_ELAPSED_COLOR = new Vector3f(1.0f, 0.55f, 0.18f);
	private static final Vector3f POSITION_MARKER_COLOR = new Vector3f(1.0f, 0.95f, 0.35f);

	/**
	 * One body's sampled center path. The provider and center offset let the split be placed at
	 * the exact current position: the samples are too sparse for linear interpolation to keep up
	 * with a rocket accelerating at tens of g.
	 */
	private record TrailPath(List<Vector3f> points, List<Vector3f> ringFrames, boolean primary,
			double startFraction, PoseProvider provider, Vector3f centerOffset) {
		private TrailPath(List<Vector3f> points, boolean primary, double startFraction, PoseProvider provider,
				Vector3f centerOffset) {
			this(points, TrajectoryTrailGenerator.ringFrames(points), primary, startFraction, provider, centerOffset);
		}
	}

	/** One path's scene objects: per-chunk elapsed and upcoming tubes plus the split chunk's pieces. */
	private static final class TrailGeometry {
		private final TrailPath path;
		// Indexed by chunk; null where a chunk produced no geometry.
		private final List<SceneObject> elapsedChunks = new ArrayList<>();
		private final List<SceneObject> upcomingChunks = new ArrayList<>();
		private final List<SceneObject> splitPieces = new ArrayList<>();
		private int splitChunk = -1;

		private TrailGeometry(TrailPath path) {
			this.path = path;
		}
	}

	private final SceneView scene;
	private final List<TrailPath> paths = new ArrayList<>();
	private final List<TrailGeometry> geometries = new ArrayList<>();
	private final List<SceneObject> eventMarkers = new ArrayList<>();
	private SceneObject positionMarker;
	private boolean trailsShown = true;
	private float radius = 1.0f;
	private float decorationScale = 1.0f;
	private float closeUpScale = MIN_DECORATION_SCALE;
	private float overviewFitDistance = Float.NaN;
	private double lastFraction = -1.0;

	TrajectoryTrails(SceneView scene) {
		this.scene = scene;
	}

	/**
	 * Samples each body's path and adds the trails and markers.
	 *
	 * @param sharedCenter   a rocket-local point compared on every body to find where a
	 *                       separated body's path leaves the primary one
	 * @param flightSize     the largest dimension of the trajectory's bounds
	 * @param rocketLength   the rocket's length, which sizes the close-up guide line
	 * @param trailsShown    whether the trails and event markers start visible
	 * @param markerShown    whether the position marker starts visible
	 */
	void build(ReplayPoses poses, FlightReplayData replayData, Vector3f sharedCenter, float flightSize,
			float rocketLength, boolean trailsShown, boolean markerShown) {
		double startTime = replayData.getStartTime();
		double endTime = replayData.getEndTime();
		radius = Math.max(flightSize * 0.003f, 1.0f);
		decorationScale = 1.0f;
		closeUpScale = closeUpTrailScale(rocketLength, radius);
		this.trailsShown = trailsShown;

		PoseProvider primary = poses.primaryProvider();
		List<Vector3f> primaryPath = samplePath(primary, sharedCenter, startTime, endTime);
		ReplayPoses.TrackedBody primaryBody = poses.bodyFor(primary);
		paths.add(new TrailPath(samplePath(primary, primaryBody.centerOffset(), startTime, endTime), true, 0.0,
				primary, primaryBody.centerOffset()));
		for (PoseProvider separated : poses.distinctProviders()) {
			if (separated == primary) {
				continue;
			}
			// Before separation a body rides the primary path; plot it only from where it leaves
			// that path, comparing through the same point on both so only separation registers.
			int from = firstDivergenceIndex(samplePath(separated, sharedCenter, startTime, endTime),
					primaryPath, radius * 3.0f);
			// Draw it through the body's own center, where the body flies.
			Vector3f center = poses.bodyFor(separated).centerOffset();
			List<Vector3f> full = samplePath(separated, center, startTime, endTime);
			if (full.size() - from >= 2) {
				// The path covers playback fractions [from / SAMPLES, 1], so its elapsed coloring
				// only starts once the flight passes the separation point.
				paths.add(new TrailPath(new ArrayList<>(full.subList(from, full.size())), false,
						(double) from / SAMPLES, separated, center));
			}
		}

		positionMarker = decoration(SphereGenerator.create(radius * 2.5f, 16, 12), POSITION_MARKER_COLOR);
		positionMarker.setBasePosition(primaryBody.centerOffset() != null ? primaryBody.centerOffset() : new Vector3f());
		positionMarker.setPoseProvider(primary);
		positionMarker.setVisible(markerShown);
		addEventMarkers(replayData, primaryBody);

		rebuild(0.0, startTime);
	}

	/** One colored sphere on the primary path per notable event; colors match the slider ticks. */
	private void addEventMarkers(FlightReplayData replayData, ReplayPoses.TrackedBody primaryBody) {
		for (var event : FlightEventMarkers.selectDisplayEvents(replayData.getAllEvents())) {
			double time = event.getTime();
			if (time < replayData.getStartTime() || time > replayData.getEndTime()) {
				continue;
			}
			SceneObject marker = decoration(SphereGenerator.create(radius * 1.2f, 12, 8),
					FlightEventMarkers.colorOf(event.getType()));
			marker.setVisible(trailsShown);
			marker.getModelMatrix().translation(primaryBody.centerAt(time));
			eventMarkers.add(marker);
		}
	}

	/** Shows or hides the trails with their event markers, and the position marker. */
	void setShown(boolean trailsShown, boolean markerShown) {
		this.trailsShown = trailsShown;
		applyVisibility();
		for (SceneObject marker : eventMarkers) {
			marker.setVisible(trailsShown);
		}
		if (positionMarker != null) {
			positionMarker.setVisible(markerShown);
		}
	}

	/** Moves the position marker onto another body. */
	void trackBody(ReplayPoses.TrackedBody body) {
		if (positionMarker != null) {
			positionMarker.setBasePosition(body.centerOffset() != null ? body.centerOffset() : new Vector3f());
			positionMarker.setPoseProvider(body.provider());
		}
	}

	/** Forces the next {@link #update} to place the split, e.g. after a view change. */
	void invalidate() {
		lastFraction = -1.0;
	}

	/** The trail tube radius at full (whole-flight) decoration scale. */
	float radius() {
		return radius;
	}

	/**
	 * Moves the split to the playback time and resizes the decorations for the camera.
	 *
	 * @param fraction             playback progress in [0, 1]
	 * @param cameraDistance       the camera distance, adjusted for any narrowed lens
	 * @param overviewFitted       whether the camera currently shows the fitted whole flight,
	 *                             whose distance is the reference for full-size decorations
	 * @param closeUp              whether the view is a close-up of the rocket
	 */
	void update(double time, double fraction, float cameraDistance, boolean overviewFitted, boolean closeUp) {
		if (paths.isEmpty()) {
			return;
		}
		if (overviewFitted) {
			overviewFitDistance = cameraDistance;
		}
		float scale = decorationScale(cameraDistance, overviewFitDistance);
		if (closeUp) {
			// The trail runs through the rocket's center; up close keep it a thin guide line.
			scale = Math.min(scale, closeUpScale);
		}
		boolean scaleChanged = Math.abs(scale - decorationScale) / Math.max(decorationScale, 1.0e-6f)
				>= DECORATION_SCALE_REBUILD_THRESHOLD;
		if (!trailRebuildRequired(fraction, lastFraction, scaleChanged)) {
			return;
		}
		lastFraction = fraction;
		if (scaleChanged) {
			decorationScale = scale;
			for (SceneObject marker : eventMarkers) {
				marker.setUniformScale(scale);
			}
			if (positionMarker != null) {
				positionMarker.setUniformScale(scale);
			}
		}
		if (scaleChanged || geometries.isEmpty()) {
			rebuild(fraction, time);
		} else {
			updateSplit(fraction, time);
		}
	}

	/** All trail tube objects, for tests. */
	List<SceneObject> trailObjects() {
		List<SceneObject> objects = new ArrayList<>();
		for (TrailGeometry geometry : geometries) {
			geometry.elapsedChunks.stream().filter(Objects::nonNull).forEach(objects::add);
			geometry.upcomingChunks.stream().filter(Objects::nonNull).forEach(objects::add);
			objects.addAll(geometry.splitPieces);
		}
		return objects;
	}

	/** Rebuilds every path's chunk tubes at the current decoration scale, then places the split. */
	private void rebuild(double fraction, double time) {
		for (TrailGeometry geometry : geometries) {
			removeAndCleanupObjects(scene, geometry.splitPieces);
			removeAndCleanupObjects(scene, geometry.elapsedChunks);
			removeAndCleanupObjects(scene, geometry.upcomingChunks);
		}
		geometries.clear();
		for (TrailPath path : paths) {
			TrailGeometry geometry = new TrailGeometry(path);
			for (int chunk = 0; chunk < trailChunkCount(path.points().size()); chunk++) {
				int start = chunk * CHUNK_SAMPLES;
				List<Vector3f> chunkPoints = path.points().subList(start, trailChunkEnd(chunk, path.points().size()) + 1);
				Vector3f seed = path.ringFrames().get(start);
				geometry.elapsedChunks.add(addTube(chunkPoints, seed, path, true));
				geometry.upcomingChunks.add(addTube(chunkPoints, seed, path, false));
			}
			geometries.add(geometry);
		}
		updateSplit(fraction, time);
	}

	/**
	 * Moves each path's split to the playback fraction: whole chunks only change visibility, and
	 * the chunk containing the split is replaced by two short pieces meeting at the body's exact
	 * position at the playback time, so the boundary sits precisely on the rocket.
	 */
	private void updateSplit(double fraction, double time) {
		for (TrailGeometry geometry : geometries) {
			removeAndCleanupObjects(scene, geometry.splitPieces);
			TrailPath path = geometry.path;
			List<Vector3f> points = path.points();
			int pointCount = points.size();
			int chunkCount = trailChunkCount(pointCount);
			double localFraction = Math.max(0.0, Math.min(1.0,
					(fraction - path.startFraction()) / Math.max(1.0e-9, 1.0 - path.startFraction())));
			int index = Math.min((int) Math.floor(localFraction * (pointCount - 1)), pointCount - 2);
			geometry.splitChunk = splitChunk(localFraction, index, chunkCount);
			if (geometry.splitChunk < 0 || geometry.splitChunk >= chunkCount) {
				continue;
			}

			Vector3f boundary = ReplayPoses.pointOnBody(path.provider(), path.centerOffset(), time);
			int chunkStart = geometry.splitChunk * CHUNK_SAMPLES;
			List<Vector3f> elapsed = new ArrayList<>(points.subList(chunkStart, index + 1));
			elapsed.add(boundary);
			List<Vector3f> upcoming = new ArrayList<>();
			upcoming.add(new Vector3f(boundary));
			upcoming.addAll(points.subList(index + 1, trailChunkEnd(geometry.splitChunk, pointCount) + 1));
			addSplitPiece(geometry, elapsed, path.ringFrames().get(chunkStart), true);
			addSplitPiece(geometry, upcoming, path.ringFrames().get(index), false);
		}
		applyVisibility();
	}

	private void applyVisibility() {
		for (TrailGeometry geometry : geometries) {
			for (int chunk = 0; chunk < geometry.elapsedChunks.size(); chunk++) {
				setVisibleIfPresent(geometry.elapsedChunks.get(chunk), trailsShown && chunk < geometry.splitChunk);
				setVisibleIfPresent(geometry.upcomingChunks.get(chunk), trailsShown && chunk > geometry.splitChunk);
			}
			for (SceneObject piece : geometry.splitPieces) {
				piece.setVisible(trailsShown);
			}
		}
	}

	private void addSplitPiece(TrailGeometry geometry, List<Vector3f> points, Vector3f seed, boolean elapsed) {
		SceneObject piece = addTube(points, seed, geometry.path, elapsed);
		if (piece != null) {
			geometry.splitPieces.add(piece);
		}
	}

	/** Adds a hidden tube along the points, or returns null when they span no length. */
	private SceneObject addTube(List<Vector3f> points, Vector3f seed, TrailPath path, boolean elapsed) {
		if (points.size() < 2) {
			return null;
		}
		// A separated body climbs along the path the primary just flew, so their tubes coincide
		// there; drawing it thinner lets the main trail cover it instead of the two flickering.
		float tubeRadius = radius * decorationScale * (path.primary() ? 1.0f : SEPARATED_TRAIL_RADIUS_SCALE);
		Mesh mesh = TrajectoryTrailGenerator.create(points, tubeRadius, 8, seed);
		if (mesh.getVertices().isEmpty()) {
			return null;
		}
		Vector3f color = path.primary()
				? (elapsed ? ACTIVE_ELAPSED_COLOR : ACTIVE_UPCOMING_COLOR)
				: (elapsed ? SEPARATED_ELAPSED_COLOR : SEPARATED_UPCOMING_COLOR);
		SceneObject tube = decoration(mesh, color);
		tube.setVisible(false);
		return tube;
	}

	/** An unlit, unselectable object drawn behind the rocket where they overlap. */
	private SceneObject decoration(Mesh mesh, Vector3f color) {
		Appearance3D appearance = new Appearance3D(new Vector3f(color));
		appearance.setUnlit(true);
		SceneObject object = new SceneObject(mesh, new Vector3f(), appearance);
		object.setSelectable(false);
		object.setForegroundDecoration(true);
		scene.addObject(object);
		return object;
	}

	private static List<Vector3f> samplePath(PoseProvider provider, Vector3f localPoint, double startTime,
			double endTime) {
		List<Vector3f> points = new ArrayList<>(SAMPLES + 1);
		for (int i = 0; i <= SAMPLES; i++) {
			points.add(ReplayPoses.pointOnBody(provider, localPoint, startTime + (endTime - startTime) * i / SAMPLES));
		}
		return points;
	}

	/** The first sample where the paths are farther apart than the threshold, or the path length if never. */
	static int firstDivergenceIndex(List<Vector3f> path, List<Vector3f> reference, float threshold) {
		int count = Math.min(path.size(), reference.size());
		float thresholdSquared = threshold * threshold;
		for (int i = 0; i < count; i++) {
			if (path.get(i).distanceSquared(reference.get(i)) > thresholdSquared) {
				return i;
			}
		}
		return path.size();
	}

	private static void setVisibleIfPresent(SceneObject object, boolean visible) {
		if (object != null) {
			object.setVisible(visible);
		}
	}

	static void removeAndCleanupObjects(SceneView scene, List<SceneObject> objects) {
		for (SceneObject object : objects) {
			if (object != null) {
				scene.removeObject(object);
				object.cleanup();
			}
		}
		objects.clear();
	}

	/** Number of chunks covering a path of the given sample count; chunks share their end samples. */
	static int trailChunkCount(int pointCount) {
		return pointCount < 2 ? 0 : (pointCount - 2) / CHUNK_SAMPLES + 1;
	}

	/** Index of the last sample in the given chunk. */
	static int trailChunkEnd(int chunk, int pointCount) {
		return Math.min((chunk + 1) * CHUNK_SAMPLES, pointCount - 1);
	}

	/**
	 * Returns the chunk the split falls in for a path segment index; -1 when nothing has elapsed
	 * and the chunk count when everything has, so all chunks then show whole in one color.
	 */
	static int splitChunk(double localFraction, int segmentIndex, int chunkCount) {
		if (localFraction <= 0.0) {
			return -1;
		}
		if (localFraction >= 1.0) {
			return chunkCount;
		}
		return Math.min(segmentIndex / CHUNK_SAMPLES, chunkCount - 1);
	}

	/** Decoration scale relative to the fitted whole-flight view: smaller as the camera closes in. */
	static float decorationScale(float cameraDistance, float overviewDistance) {
		if (!Float.isFinite(cameraDistance) || !Float.isFinite(overviewDistance)
				|| cameraDistance <= 0.0f || overviewDistance <= 0.0f) {
			return 1.0f;
		}
		return Math.max(MIN_DECORATION_SCALE, Math.min(1.0f, cameraDistance / overviewDistance));
	}

	/** Decoration scale that makes the trail about 1% of the rocket's length thick in the close-up views. */
	static float closeUpTrailScale(float rocketLength, float trailRadius) {
		if (!Float.isFinite(rocketLength) || rocketLength <= 0.0f || !Float.isFinite(trailRadius) || trailRadius <= 0.0f) {
			return MIN_DECORATION_SCALE;
		}
		return Math.min(1.0f, rocketLength * 0.01f / trailRadius);
	}

	static boolean trailRebuildRequired(double fraction, double previousFraction, boolean scaleChanged) {
		return scaleChanged || previousFraction < 0.0 || Double.compare(fraction, previousFraction) != 0;
	}
}
