package info.openrocket.swing.gui.figure3d.geometry.components;

import info.openrocket.core.rocketcomponent.MassObject;
import info.openrocket.core.util.RocketComponentUtils;
import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.geometry.IntList;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.Vertex;
import info.openrocket.swing.gui.figure3d.scene.properties.GraphicsQualitySettings;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a 3D mesh for a MassObject, which can represent various shapes
 * like parachutes, streamers, or custom mass components.
 */
public class MassObjectGenerator {

	/**
	 * Creates a mesh for a MassObject.
	 * The shape is a body of revolution defined by the radius at different points along its length.
	 * The mesh is generated along the +X axis.
	 *
	 * @param massObject The MassObject to create a mesh for.
	 * @param config The render configuration.
	 * @return A Mesh object representing the mass object.
	 */
	public static Mesh create(MassObject massObject, RenderingConfiguration config) {
		List<Vertex> vertices = new ArrayList<>();
		IntList indices = new IntList();

		GraphicsQualitySettings.RenderQuality renderQuality = config.getQuality().getQuality();
		int slices = switch (renderQuality) {
			case LOW -> RenderingConstants.LOW_COMPLEX_SEGMENT_COUNT;
			case MEDIUM -> RenderingConstants.MEDIUM_COMPLEX_SEGMENT_COUNT;
			case HIGH -> RenderingConstants.HIGH_COMPLEX_SEGMENT_COUNT;
		};

		int stacks = switch (renderQuality) {
			case LOW -> RenderingConstants.LOW_SEGMENT_COUNT;
			case MEDIUM -> RenderingConstants.MEDIUM_SEGMENT_COUNT;
			case HIGH -> RenderingConstants.HIGH_SEGMENT_COUNT;
		};

		float length = (float) massObject.getLength();
		float dx = length / stacks;
		float halfLength = length / 2.0f;

		// Pre-calculate the radial offset due to the component's position
		// In OpenRocket, radial direction is an angle from the +Z axis.
		// In our engine's coordinate system (Y-up, X-longitudinal), this corresponds to a rotation around the X-axis.
		float yOffset = (float) (massObject.getRadialPosition() * Math.cos(massObject.getRadialDirection()));
		float zOffset = (float) (massObject.getRadialPosition() * Math.sin(massObject.getRadialDirection()));

		// Generate vertices
		for (int j = 0; j <= stacks; j++) {
			float currentX = j * dx;
			// Force the radius to zero for the final stack of vertices to close the shape.
			float r = (j == stacks) ? 0.0f : (float) RocketComponentUtils.getMassObjectRadius(massObject, currentX);
			float v = (float) j / stacks; // Texture V coordinate

			for (int i = 0; i <= slices; i++) {
				float u = (float) i / slices; // Texture U coordinate
				float angle = u * 2.0f * (float) Math.PI;

				float y = (float) Math.cos(angle);
				float z = (float) Math.sin(angle);

				Vector3f position = new Vector3f(currentX - halfLength, y * r + yOffset, z * r + zOffset);
				Vector3f normal = new Vector3f(0, y, z).normalize(); // Simple radial normal, pointing outwards from the X-axis
				Vector2f texCoords = new Vector2f(u, v);

				vertices.add(new Vertex(position, normal, texCoords, 0));
			}
		}

		// Generate indices for the quad strips
		for (int j = 0; j < stacks; j++) {
			for (int i = 0; i < slices; i++) {
				int row1 = j * (slices + 1);
				int row2 = (j + 1) * (slices + 1);

				int p1 = row1 + i;
				int p2 = row2 + i;
				int p3 = row2 + i + 1;
				int p4 = row1 + i + 1;

				// Reversed winding order to be counter-clockwise (CCW)
				indices.add(p1);
				indices.add(p4);
				indices.add(p2);

				indices.add(p4);
				indices.add(p3);
				indices.add(p2);
			}
		}

		return new Mesh(vertices, indices);
	}
}
