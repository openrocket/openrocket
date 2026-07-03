package info.openrocket.swing.gui.figure3d.core.geometry.basic;

import info.openrocket.swing.gui.figure3d.core.geometry.GeometryGenerator;
import info.openrocket.swing.gui.figure3d.core.geometry.IntList;
import info.openrocket.swing.gui.figure3d.core.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.core.geometry.Vertex;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a sphere or spherical section mesh.
 */
public class SphereGenerator implements GeometryGenerator {

	/**
	 * Creates a sphere or a section of a sphere mesh.
	 *
	 * @param radius The radius of the sphere.
	 * @param sectors The number of segments around the circumference (longitude).
	 * @param stacks The number of segments along the height (latitude).
	 * @param startStackAngleRad The starting angle for stacks in radians (0 for top pole, PI for bottom pole).
	 * @param endStackAngleRad The ending angle for stacks in radians.
	 * @param startSectorAngleRad The starting angle for sectors in radians (0 to 2*PI).
	 * @param endSectorAngleRad The ending angle for sectors in radians.
	 * @return A combined Mesh object for the sphere section.
	 */
	public static Mesh create(float radius, int sectors, int stacks,
							  float startStackAngleRad, float endStackAngleRad,
							  float startSectorAngleRad, float endSectorAngleRad) {
		List<Vertex> vertices = new ArrayList<>();
		IntList indices = new IntList();

		// Ensure valid angles
		startStackAngleRad = Math.max(0, startStackAngleRad);
		endStackAngleRad = Math.min((float)Math.PI, endStackAngleRad);
		if (startStackAngleRad >= endStackAngleRad) { // Ensure a valid range
			endStackAngleRad = (float)Math.PI;
		}

		float stackAngleRange = endStackAngleRad - startStackAngleRad;
		float sectorAngleRange = endSectorAngleRad - startSectorAngleRad;

		// Generate vertices
		for (int i = 0; i <= stacks; ++i) {
			float stackAngle = startStackAngleRad + (stackAngleRange / stacks * i); // Angle from top pole to bottom pole
			float z = radius * (float) Math.cos(stackAngle); // Z-coordinate (height)
			float xy = radius * (float) Math.sin(stackAngle); // Radius in XY plane for this stack

			for (int j = 0; j <= sectors; ++j) {
				float sectorAngle = startSectorAngleRad + (sectorAngleRange / sectors * j); // Angle around the Z-axis
				float x = xy * (float) Math.cos(sectorAngle);
				float y = xy * (float) Math.sin(sectorAngle);

				Vector3f position = new Vector3f(x, y, z);
				Vector3f normal = new Vector3f(x, y, z).normalize();

				// Calculate UVs: U maps to sector angle, V maps to stack angle
				Vector2f texCoords = new Vector2f(
						(sectorAngle - startSectorAngleRad) / sectorAngleRange,
						(stackAngle - startStackAngleRad) / stackAngleRange
				);
				// Handle edge cases for UVs if range is zero, prevent NaN
				if (Float.isNaN(texCoords.x)) texCoords.x = 0;
				if (Float.isNaN(texCoords.y)) texCoords.y = 0;

				vertices.add(new Vertex(position, normal, texCoords, 0));
			}
		}

		// Generate indices
		for (int i = 0; i < stacks; ++i) {
			int k1 = i * (sectors + 1);     	 // Current stack's first vertex
			int k2 = k1 + sectors + 1; 			// Next stack's first vertex

			for (int j = 0; j < sectors; ++j, ++k1, ++k2) {
				// Avoid creating degenerate triangles at the poles if they are part of the range
				if (i != 0 || startStackAngleRad > 0.0001f) { // Not the very first stack (top pole) OR top pole is clipped
					indices.add(k1);
					indices.add(k2);
					indices.add(k1 + 1);
				}
				if (i != (stacks - 1) || endStackAngleRad < Math.PI - 0.0001f) { // Not the very last stack (bottom pole) OR bottom pole is clipped
					indices.add(k1 + 1);
					indices.add(k2);
					indices.add(k2 + 1);
				}
			}
		}

		return new Mesh(vertices, indices);
	}

	/**
	 * Creates a full sphere mesh (convenience method).
	 */
	public static Mesh create(float radius, int sectors, int stacks) {
		return create(radius, sectors, stacks, 0, (float)Math.PI, 0, (float)Math.PI * 2.0f);
	}
}
