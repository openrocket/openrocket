package info.openrocket.swing.gui.figure3d.core.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Guards the packed GPU vertex layout constants. If the layout ever changes,
 * this test forces the change to be deliberate — a stride mismatch is not
 * reported by OpenGL and silently corrupts the rendered geometry.
 */
public class VertexTest {

	@Test
	public void testPackedVertexLayout() {
		assertEquals(3, Vertex.POSITION_FLOATS);
		assertEquals(3, Vertex.NORMAL_FLOATS);
		assertEquals(2, Vertex.TEX_COORD_FLOATS);
		assertEquals(1, Vertex.SURFACE_ID_FLOATS);
		assertEquals(Vertex.POSITION_FLOATS + Vertex.NORMAL_FLOATS + Vertex.TEX_COORD_FLOATS + Vertex.SURFACE_ID_FLOATS,
				Vertex.FLOATS_PER_VERTEX);
		// The shaders and RenderableMesh attribute pointers assume 9 floats per vertex.
		assertEquals(9, Vertex.FLOATS_PER_VERTEX);
	}
}
