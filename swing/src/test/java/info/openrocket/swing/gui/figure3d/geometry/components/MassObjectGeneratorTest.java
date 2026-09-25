package info.openrocket.swing.gui.figure3d.geometry.components;

import info.openrocket.core.rocketcomponent.MassObject;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.Vertex;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MassObjectGeneratorTest {

	@Test
	void collapsedTipsDoNotEmitDegenerateTriangles() {
		MassObject massObject = mock(MassObject.class);
		when(massObject.getLength()).thenReturn(0.20);
		when(massObject.getRadius()).thenReturn(0.03);
		Mesh mesh = MassObjectGenerator.create(massObject, new RenderingConfiguration());

		for (int i = 0; i < mesh.getIndices().size(); i += 3) {
			Vertex v0 = mesh.getVertices().get(mesh.getIndices().get(i));
			Vertex v1 = mesh.getVertices().get(mesh.getIndices().get(i + 1));
			Vertex v2 = mesh.getVertices().get(mesh.getIndices().get(i + 2));
			float doubleAreaSquared = new Vector3f(v1.position).sub(v0.position)
					.cross(new Vector3f(v2.position).sub(v0.position)).lengthSquared();
			assertTrue(doubleAreaSquared > 1.0e-12f, "Triangle " + (i / 3) + " must have non-zero area");
		}
	}
}
