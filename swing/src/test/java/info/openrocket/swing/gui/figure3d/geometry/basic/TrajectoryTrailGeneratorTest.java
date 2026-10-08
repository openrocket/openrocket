package info.openrocket.swing.gui.figure3d.geometry.basic;

import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrajectoryTrailGeneratorTest {
	private static final int SEGMENTS = 8;

	@Test
	void seededPiecesLineUpWithTheWholePathTube() {
		List<Vector3f> path = new ArrayList<>();
		for (int i = 0; i <= 60; i++) {
			// A twisting climb, so the ring frame rotates noticeably along the path.
			double t = i / 10.0;
			path.add(new Vector3f((float) (20 * Math.cos(t)), (float) (15 * t), (float) (20 * Math.sin(t))));
		}
		List<Vector3f> frames = TrajectoryTrailGenerator.ringFrames(path);
		Mesh whole = TrajectoryTrailGenerator.create(path, 1.0f, SEGMENTS);
		int join = 30;
		Mesh second = TrajectoryTrailGenerator.create(path.subList(join, path.size()), 1.0f, SEGMENTS,
				frames.get(join));

		// The seeded piece's first ring matches the whole tube's ring at that sample, up to the
		// one-sided tangent a piece has at its end.
		float maxDistance = 0.0f;
		for (int s = 0; s < SEGMENTS; s++) {
			Vector3f expected = whole.getVertices().get(join * SEGMENTS + s).position;
			Vector3f actual = second.getVertices().get(s).position;
			maxDistance = Math.max(maxDistance, expected.distance(actual));
		}
		assertTrue(maxDistance < 0.1f, "Seeded ring must line up with the whole-path ring: " + maxDistance);
	}

	@Test
	void unseededTubeIsUnchangedByTheSeedOverload() {
		List<Vector3f> path = List.of(new Vector3f(), new Vector3f(1, 2, 0), new Vector3f(3, 3, 1));
		Mesh plain = TrajectoryTrailGenerator.create(path, 0.5f, SEGMENTS);
		Mesh seededWithNull = TrajectoryTrailGenerator.create(path, 0.5f, SEGMENTS, null);
		assertEquals(plain.getVertices().size(), seededWithNull.getVertices().size());
		for (int i = 0; i < plain.getVertices().size(); i++) {
			assertEquals(plain.getVertices().get(i).position, seededWithNull.getVertices().get(i).position);
		}
	}

	@Test
	void ringFramesRepeatAcrossDuplicatePoints() {
		List<Vector3f> path = List.of(new Vector3f(), new Vector3f(), new Vector3f(0, 1, 0), new Vector3f(0, 2, 0));
		List<Vector3f> frames = TrajectoryTrailGenerator.ringFrames(path);
		assertEquals(path.size(), frames.size());
		for (Vector3f frame : frames) {
			assertEquals(1.0f, frame.length(), 1e-5f);
			assertEquals(0.0f, frame.y, 1e-5f, "Ring axis must stay perpendicular to a vertical path");
		}
	}
}
