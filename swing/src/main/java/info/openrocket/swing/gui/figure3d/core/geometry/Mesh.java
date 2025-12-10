package info.openrocket.swing.gui.figure3d.core.geometry;

import java.util.List;

/**
 * Represents a 3D mesh, defined by a list of vertices and indices.
 * This class is a pure data container and is independent of any rendering API.
 * This makes it suitable for geometry generation and for exporting to file formats.
 */
public class Mesh {
	private final List<Vertex> vertices;
	private final List<Integer> indices;

	/**
	 * Constructs a new Mesh.
	 * @param vertices The list of vertices that make up the mesh.
	 * @param indices The list of indices that define the faces of the mesh.
	 */
	public Mesh(List<Vertex> vertices, List<Integer> indices) {
		this.vertices = vertices;
		this.indices = indices;
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
	public List<Integer> getIndices() {
		return indices;
	}
}
