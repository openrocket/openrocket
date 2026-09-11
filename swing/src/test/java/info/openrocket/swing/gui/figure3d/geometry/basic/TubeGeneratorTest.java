package info.openrocket.swing.gui.figure3d.geometry.basic;

import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.Vertex;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TubeGeneratorTest {
	private static final float EPSILON = 1.0e-5f;

	/** A hollow tube identifies its outer wall, inner wall, and both end surfaces. */
	@Test
	void hollowTubeUsesSurfaceIdIndices() {
		Mesh mesh = TubeGenerator.create(0.05f, 0.05f, 0.002f, 0.3f, 16, false);

		Set<Integer> seen = new HashSet<>();
		for (Vertex vertex : mesh.getVertices()) {
			seen.add(vertex.surfaceID);
			assertTrue(vertex.surfaceID >= 0 && vertex.surfaceID <= RenderingConstants.SURFACE_ID_EDGE,
					"Surface ID should be a small bit index, got " + vertex.surfaceID);
		}

		assertTrue(seen.contains(RenderingConstants.SURFACE_ID_OUTSIDE), "outer wall vertices expected");
		assertTrue(seen.contains(RenderingConstants.SURFACE_ID_INSIDE), "inner wall vertices expected");
		assertTrue(seen.contains(RenderingConstants.SURFACE_ID_FORE), "fore end vertices expected");
		assertTrue(seen.contains(RenderingConstants.SURFACE_ID_AFT), "aft end vertices expected");
	}

	@Test
	void uncappedShouldersKeepTheirFreeEndRingCaps() {
		Mesh mesh = createTubeWithShoulders(false);

		assertIndexedSurfaceAt(mesh, RenderingConstants.SURFACE_ID_FORE, -0.20f);
		assertIndexedSurfaceAt(mesh, RenderingConstants.SURFACE_ID_AFT, 0.20f);
		assertIndicesReferenceExistingVertices(mesh);
	}

	@Test
	void cappedShouldersGenerateFilledFreeEnds() {
		Mesh mesh = createTubeWithShoulders(true);

		assertIndexedSurfaceAt(mesh, RenderingConstants.SURFACE_ID_FORE, -0.20f);
		assertIndexedSurfaceAt(mesh, RenderingConstants.SURFACE_ID_AFT, 0.20f);
		assertIndicesReferenceExistingVertices(mesh);
	}

	private static Mesh createTubeWithShoulders(boolean capped) {
		List<TubeGenerator.RadiusPoint> profile = List.of(
				new TubeGenerator.RadiusPoint(0.0f, 0.05f),
				new TubeGenerator.RadiusPoint(1.0f, 0.05f));
		TubeGenerator.Shoulder shoulder = new TubeGenerator.Shoulder(0.05, 0.04, 0.002, capped);
		return TubeGenerator.create(profile, 0.002f, 0.30f, 16, false, shoulder, shoulder);
	}

	private static void assertIndexedSurfaceAt(Mesh mesh, int surfaceId, float expectedX) {
		for (int i = 0; i < mesh.getIndices().size(); i++) {
			Vertex vertex = mesh.getVertices().get(mesh.getIndices().get(i));
			if (vertex.surfaceID == surfaceId && Math.abs(vertex.position.x - expectedX) < EPSILON) {
				return;
			}
		}
		throw new AssertionError("No indexed surface " + surfaceId + " at x=" + expectedX);
	}

	private static void assertIndicesReferenceExistingVertices(Mesh mesh) {
		for (int i = 0; i < mesh.getIndices().size(); i++) {
			int index = mesh.getIndices().get(i);
			assertTrue(index >= 0 && index < mesh.getVertices().size(),
					() -> "Index " + index + " outside vertex range 0.." + (mesh.getVertices().size() - 1));
		}
	}
}
