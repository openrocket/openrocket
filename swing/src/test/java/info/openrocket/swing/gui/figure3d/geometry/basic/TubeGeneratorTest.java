package info.openrocket.swing.gui.figure3d.geometry.basic;

import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.Vertex;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TubeGeneratorTest {

	/**
	 * Surface IDs are bit indices tested by the fragment shader as
	 * {@code decalSurfaceMask & (1 << surfaceID)}. A hollow tube must tag its
	 * outer wall, inner wall, and both end surfaces with the corresponding
	 * SURFACE_ID_* constants — storing the DECAL_SURFACE_* mask values here
	 * instead silently breaks decal surface selection and inner-surface hiding.
	 */
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
	void surfaceMaskBitsMatchSurfaceIds() {
		assertEquals(RenderingConstants.DECAL_SURFACE_OUTSIDE, 1 << RenderingConstants.SURFACE_ID_OUTSIDE);
		assertEquals(RenderingConstants.DECAL_SURFACE_INSIDE, 1 << RenderingConstants.SURFACE_ID_INSIDE);
		assertEquals(RenderingConstants.DECAL_SURFACE_FORE, 1 << RenderingConstants.SURFACE_ID_FORE);
		assertEquals(RenderingConstants.DECAL_SURFACE_AFT, 1 << RenderingConstants.SURFACE_ID_AFT);
		// The fin edge band must stay outside every decal mask
		assertEquals(0, RenderingConstants.DECAL_SURFACE_ALL & (1 << RenderingConstants.SURFACE_ID_EDGE));
	}
}
