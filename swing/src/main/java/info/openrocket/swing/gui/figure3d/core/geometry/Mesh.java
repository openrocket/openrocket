package info.openrocket.swing.gui.figure3d.core.geometry;

import org.joml.Vector3f;

import java.util.List;

/**
 * Represents a 3D mesh, defined by a list of vertices and indices.
 * This class is a pure data container and is independent of any rendering API.
 * This makes it suitable for geometry generation and for exporting to file formats.
 */
public class Mesh {
	private final List<Vertex> vertices;
	private final IntList indices;
	private final Vector3f boundsMin;
	private final Vector3f boundsMax;

	/**
	 * Constructs a new Mesh.
	 * @param vertices The list of vertices that make up the mesh.
	 * @param indices The list of indices that define the faces of the mesh.
	 */
	public Mesh(List<Vertex> vertices, IntList indices) {
		this.vertices = vertices;
		this.indices = indices;
		this.boundsMin = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
		this.boundsMax = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
		for (Vertex vertex : vertices) {
			boundsMin.min(vertex.position);
			boundsMax.max(vertex.position);
		}
		if (vertices.isEmpty()) {
			boundsMin.set(0.0f, 0.0f, 0.0f);
			boundsMax.set(0.0f, 0.0f, 0.0f);
		}
	}

	/**
	 * @return The list of vertices in the mesh.
	 */
	public List<Vertex> getVertices() {
		return vertices;
	}

	/**
	 * @return The list of indices for the mesh.
	 */
	public IntList getIndices() {
		return indices;
	}

	public Vector3f getBoundsMin(Vector3f destination) {
		if (destination == null) {
			destination = new Vector3f();
		}
		return destination.set(boundsMin);
	}

	public Vector3f getBoundsMax(Vector3f destination) {
		if (destination == null) {
			destination = new Vector3f();
		}
		return destination.set(boundsMax);
	}
}
