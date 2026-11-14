package info.openrocket.swing.gui.figure3d.geometry.basic;

import info.openrocket.swing.gui.figure3d.constants.GeometryConstants;
import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.geometry.IntList;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.Vertex;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/** Generates a flat plane mesh on the XZ axis. */
public final class PlaneGenerator {
	private PlaneGenerator() {
	}

	public static Mesh create(float width, float depth, float tilingU, float tilingV,
			GeometryConstants.WindingOrder windingOrder) {
		List<Vertex> vertices = new ArrayList<>();
		IntList indices = new IntList();
		float halfWidth = width / 2.0f;
		float halfDepth = depth / 2.0f;

		vertices.add(new Vertex(new Vector3f(-halfWidth, 0, halfDepth), new Vector3f(0, 1, 0),
				new Vector2f(0, 0), RenderingConstants.SURFACE_ID_OUTSIDE));
		vertices.add(new Vertex(new Vector3f(-halfWidth, 0, -halfDepth), new Vector3f(0, 1, 0),
				new Vector2f(0, tilingV), RenderingConstants.SURFACE_ID_OUTSIDE));
		vertices.add(new Vertex(new Vector3f(halfWidth, 0, -halfDepth), new Vector3f(0, 1, 0),
				new Vector2f(tilingU, tilingV), RenderingConstants.SURFACE_ID_OUTSIDE));
		vertices.add(new Vertex(new Vector3f(halfWidth, 0, halfDepth), new Vector3f(0, 1, 0),
				new Vector2f(tilingU, 0), RenderingConstants.SURFACE_ID_OUTSIDE));

		if (windingOrder == GeometryConstants.WindingOrder.COUNTER_CLOCKWISE) {
			indices.addTriangle(0, 1, 2);
			indices.addTriangle(0, 2, 3);
		} else {
			indices.addTriangle(0, 2, 1);
			indices.addTriangle(0, 3, 2);
		}
		return new Mesh(vertices, indices);
	}

	public static Mesh create(float width, float depth, float tilingU, float tilingV) {
		return create(width, depth, tilingU, tilingV, GeometryConstants.WindingOrder.COUNTER_CLOCKWISE);
	}
}
