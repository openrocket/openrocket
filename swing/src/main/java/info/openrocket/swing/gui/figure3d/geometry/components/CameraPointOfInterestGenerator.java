package info.openrocket.swing.gui.figure3d.geometry.components;

import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.IntList;
import info.openrocket.swing.gui.figure3d.geometry.Vertex;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a simple billboard cross for the camera point-of-interest marker.
 */
public final class CameraPointOfInterestGenerator {

	private static final float ARM_LENGTH = 0.11f;
	private static final float ARM_THICKNESS = 0.02f;

	private CameraPointOfInterestGenerator() {
	}

	public static Mesh create() {
		List<Vertex> vertices = new ArrayList<>();
		IntList indices = new IntList();
		Vector3f normal = new Vector3f(0, 0, 1);
		Vector2f texCoords = new Vector2f(0, 0);

		addRectangle(vertices, indices, -ARM_LENGTH, -ARM_THICKNESS * 0.5f,
				ARM_LENGTH, ARM_THICKNESS * 0.5f, normal, texCoords);
		addRectangle(vertices, indices, -ARM_THICKNESS * 0.5f, -ARM_LENGTH,
				ARM_THICKNESS * 0.5f, ARM_LENGTH, normal, texCoords);

		return new Mesh(vertices, indices);
	}

	private static void addRectangle(List<Vertex> vertices, IntList indices,
			float minX, float minY, float maxX, float maxY, Vector3f normal, Vector2f texCoords) {
		int baseIndex = vertices.size();
		vertices.add(new Vertex(new Vector3f(minX, minY, 0.0f), normal, texCoords, 0));
		vertices.add(new Vertex(new Vector3f(maxX, minY, 0.0f), normal, texCoords, 0));
		vertices.add(new Vertex(new Vector3f(maxX, maxY, 0.0f), normal, texCoords, 0));
		vertices.add(new Vertex(new Vector3f(minX, maxY, 0.0f), normal, texCoords, 0));

		indices.add(baseIndex);
		indices.add(baseIndex + 1);
		indices.add(baseIndex + 2);
		indices.add(baseIndex);
		indices.add(baseIndex + 2);
		indices.add(baseIndex + 3);
	}
}
