package info.openrocket.swing.gui.figure3d.geometry.basic;

import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.Vertex;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TubeGeneratorTest {

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
}
