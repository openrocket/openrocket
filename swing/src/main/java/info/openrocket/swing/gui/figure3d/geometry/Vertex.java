package info.openrocket.swing.gui.figure3d.geometry;

import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * Represents a single vertex in a 3D mesh.
 * It holds positional data as well as other attributes like normals and texture coordinates.
 */
public class Vertex {
	/** Number of floats used for the position attribute when packed for the GPU. */
	public static final int POSITION_FLOATS = 3;
	/** Number of floats used for the normal attribute when packed for the GPU. */
	public static final int NORMAL_FLOATS = 3;
	/** Number of floats used for the texture coordinate attribute when packed for the GPU. */
	public static final int TEX_COORD_FLOATS = 2;
	/** Number of floats used for the surface ID attribute (an int packed as a float). */
	public static final int SURFACE_ID_FLOATS = 1;
	/**
	 * Total number of floats per vertex in the packed GPU layout:
	 * position, normal, texture coordinates, surface ID.
	 *
	 * This is the single source of truth for the vertex stride. Any code that
	 * packs vertex data or sets up vertex attribute pointers must derive its
	 * sizes and offsets from these constants; a mismatch is not reported by
	 * OpenGL and silently corrupts the rendered geometry.
	 */
	public static final int FLOATS_PER_VERTEX =
			POSITION_FLOATS + NORMAL_FLOATS + TEX_COORD_FLOATS + SURFACE_ID_FLOATS;

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
