package info.openrocket.swing.gui.figure3d.core.geometry.basic;

import info.openrocket.swing.gui.figure3d.constants.GeometryConstants;
import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.core.geometry.GeometryGenerator;
import info.openrocket.swing.gui.figure3d.core.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.core.geometry.Vertex;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a simple, flat plane mesh, oriented on the XZ axis.
 */
public class PlaneGenerator implements GeometryGenerator {

	/**
	 * Creates a plane mesh with specified tiling and winding order.
	 * @param width The width of the plane on the X-axis.
	 * @param depth The depth of the plane on the Z-axis.
	 * @param tilingU The number of times the texture should tile horizontally (U-axis).
	 * @param tilingV The number of times the texture should tile vertically (V-axis).
	 * @param windingOrder The winding order of the vertices, controlling the culling direction.
	 * @return The generated Mesh object.
	 */
	public static Mesh create(float width, float depth, float tilingU, float tilingV, GeometryConstants.WindingOrder windingOrder) {
		List<Vertex> vertices = new ArrayList<>();
		List<Integer> indices = new ArrayList<>();

		float halfWidth = width / 2;
		float halfDepth = depth / 2;

		// Define the 4 vertices of the plane
		vertices.add(new Vertex(new Vector3f(-halfWidth, 0, halfDepth), new Vector3f(0, 1, 0), new Vector2f(0, 0),
				RenderingConstants.DECAL_SURFACE_OUTSIDE));           // 0: Top-left
		vertices.add(new Vertex(new Vector3f(-halfWidth, 0, -halfDepth), new Vector3f(0, 1, 0), new Vector2f(0, tilingV),
				RenderingConstants.DECAL_SURFACE_OUTSIDE));      // 1: Bottom-left
		vertices.add(new Vertex(new Vector3f(halfWidth, 0, -halfDepth), new Vector3f(0, 1, 0), new Vector2f(tilingU, tilingV),
				RenderingConstants.DECAL_SURFACE_OUTSIDE)); // 2: Bottom-right
		vertices.add(new Vertex(new Vector3f(halfWidth, 0, halfDepth), new Vector3f(0, 1, 0), new Vector2f(tilingU, 0),
				RenderingConstants.DECAL_SURFACE_OUTSIDE));      // 3: Top-right

		// Define the two triangles based on the desired winding order.
		if (windingOrder == GeometryConstants.WindingOrder.COUNTER_CLOCKWISE) {
			// CCW order makes the top face the front face.
			indices.add(0);
			indices.add(1);
			indices.add(2);

			indices.add(0);
			indices.add(2);
			indices.add(3);
		} else { // CLOCKWISE
			// CW order makes the bottom face the front face.
			indices.add(0);
			indices.add(2);
			indices.add(1);

			indices.add(0);
			indices.add(3);
			indices.add(2);
		}

		return new Mesh(vertices, indices);
	}

	public static Mesh create(float width, float depth, float tilingU, float tilingV) {
		return create(width, depth, tilingU, tilingV, GeometryConstants.WindingOrder.COUNTER_CLOCKWISE);
	}
}