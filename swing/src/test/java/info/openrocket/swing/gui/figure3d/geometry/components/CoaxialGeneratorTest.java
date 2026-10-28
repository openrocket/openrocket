package info.openrocket.swing.gui.figure3d.geometry.components;

import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.Vertex;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoaxialGeneratorTest extends BaseTestCase {
	private static final float EPSILON = 1.0e-5f;

	/** A texture on the inside of a tube runs aft to fore, the reverse of the outer wall (#3390). */
	@Test
	void innerWallTextureRunsOppositeToOuterWall() {
		Mesh mesh = CoaxialGenerator.create(new BodyTube(0.30, 0.05, 0.002), new RenderingConfiguration());

		int checked = 0;
		for (Vertex vertex : mesh.getVertices()) {
			boolean fore = Math.abs(vertex.position.x + 0.15f) < EPSILON;
			boolean aft = Math.abs(vertex.position.x - 0.15f) < EPSILON;
			if (!fore && !aft) {
				continue;
			}
			if (vertex.surfaceID == RenderingConstants.SURFACE_ID_OUTSIDE) {
				assertEquals(fore ? 1.0f : 0.0f, vertex.texCoords.y, EPSILON, "outer wall v");
				checked++;
			} else if (vertex.surfaceID == RenderingConstants.SURFACE_ID_INSIDE) {
				assertEquals(fore ? 0.0f : 1.0f, vertex.texCoords.y, EPSILON, "inner wall v");
				checked++;
			}
		}
		assertTrue(checked > 0, "expected wall vertices at both tube ends");
	}
}
