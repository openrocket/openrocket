package info.openrocket.swing.gui.figure3d.core.geometry;

import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * Represents a single vertex in a 3D mesh.
 * It holds positional data as well as other attributes like normals and texture coordinates.
 */
public class Vertex {
	/** The 3D position of the vertex. */
	public Vector3f position;
	/** The normal vector of the vertex, used for lighting calculations. */
	public Vector3f normal;
	/** The texture coordinates of the vertex. */
	public Vector2f texCoords;
	/** An identifier for the surface this vertex belongs to. */
	public int surfaceID;

	/**
	 * Constructs a new Vertex.
	 * @param position The 3D position of the vertex.
	 * @param normal The normal vector of the vertex.
	 * @param texCoords The texture coordinates of the vertex.
	 * @param surfaceID An identifier for the surface this vertex belongs to.
	 */
	public Vertex(Vector3f position, Vector3f normal, Vector2f texCoords, int surfaceID) {
		this.position = position;
		this.normal = normal;
		this.texCoords = texCoords;
		this.surfaceID = surfaceID;
	}
}
