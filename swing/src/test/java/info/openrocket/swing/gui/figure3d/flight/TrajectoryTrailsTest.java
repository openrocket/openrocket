package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class TrajectoryTrailsTest {
	@Test
	void trailBoundaryRebuildsForEveryDistinctPlaybackFrame() {
		assertTrue(TrajectoryTrails.trailRebuildRequired(0.100_001, 0.1, false));
		assertFalse(TrajectoryTrails.trailRebuildRequired(0.1, 0.1, false));
		assertTrue(TrajectoryTrails.trailRebuildRequired(0.1, 0.1, true));
	}

	@Test
	void closeUpTrailIsAThinLineRelativeToTheRocket() {
		// A 20-unit rocket on a flight whose trail is 18 units thick: 1% of the length is 0.2.
		assertEquals(0.2f / 18.0f, TrajectoryTrails.closeUpTrailScale(20.0f, 18.0f), 1e-6f);
		assertEquals(1.0f, TrajectoryTrails.closeUpTrailScale(20.0f, 0.05f), "Never thicken a thin trail");
		assertTrue(TrajectoryTrails.closeUpTrailScale(Float.NaN, 18.0f) > 0.0f);
	}

	@Test
	void trailChunksCoverEverySegmentAndShareEndSamples() {
		int points = 241;
		int chunks = TrajectoryTrails.trailChunkCount(points);
		assertEquals(20, chunks);
		assertEquals(0, TrajectoryTrails.trailChunkCount(1));
		assertEquals(1, TrajectoryTrails.trailChunkCount(2));
		assertEquals(TrajectoryTrails.CHUNK_SAMPLES, TrajectoryTrails.trailChunkEnd(0, points));
		assertEquals(points - 1, TrajectoryTrails.trailChunkEnd(chunks - 1, points));
		// A short last chunk still ends on the final sample.
		assertEquals(14, TrajectoryTrails.trailChunkEnd(1, 15));

		assertEquals(-1, TrajectoryTrails.splitChunk(0.0, 0, chunks), "Nothing elapsed: every chunk is upcoming");
		assertEquals(chunks, TrajectoryTrails.splitChunk(1.0, points - 2, chunks), "Everything elapsed");
		for (int segment = 0; segment < points - 1; segment++) {
			int chunk = TrajectoryTrails.splitChunk(0.5, segment, chunks);
			int start = chunk * TrajectoryTrails.CHUNK_SAMPLES;
			assertTrue(segment >= start && segment + 1 <= TrajectoryTrails.trailChunkEnd(chunk, points),
					"Segment " + segment + " must lie inside its split chunk " + chunk);
		}
	}

	@Test
	void trajectoryDecorationsShrinkAsTheCameraMovesCloser() {
		assertEquals(1.0f, TrajectoryTrails.decorationScale(1_000.0f, 1_000.0f), 1e-6f);
		assertEquals(0.5f, TrajectoryTrails.decorationScale(500.0f, 1_000.0f), 1e-6f);
		assertEquals(0.1f, TrajectoryTrails.decorationScale(100.0f, 1_000.0f), 1e-6f);
		assertEquals(0.04f, TrajectoryTrails.decorationScale(1.0f, 1_000.0f), 1e-6f);
		assertEquals(1.0f, TrajectoryTrails.decorationScale(2_000.0f, 1_000.0f), 1e-6f);
	}

	@Test
	void dynamicTrailsAreRemovedThroughTheSceneMutationApi() {
		SceneView scene = mock(SceneView.class);
		SceneObject trail = mock(SceneObject.class);
		List<SceneObject> trails = new ArrayList<>(List.of(trail));

		TrajectoryTrails.removeAndCleanupObjects(scene, trails);

		verify(scene).removeObject(trail);
		verify(scene, never()).getObjects();
		verify(trail).cleanup();
		assertTrue(trails.isEmpty());
	}

	@Test
	void aPathThatNeverLeavesTheReferenceHasNothingToDraw() {
		List<Vector3f> reference = List.of(new Vector3f(), new Vector3f(0, 10, 0), new Vector3f(0, 20, 0));
		List<Vector3f> leaving = List.of(new Vector3f(), new Vector3f(0, 10, 0), new Vector3f(50, 20, 0));

		assertEquals(2, TrajectoryTrails.firstDivergenceIndex(leaving, reference, 1.0f));
		assertEquals(reference.size(), TrajectoryTrails.firstDivergenceIndex(reference, reference, 1.0f),
				"A body that never separates must not redraw the primary path");
	}
}
