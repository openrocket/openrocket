package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.swing.gui.figure3d.core.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.core.geometry.Vertex;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * OpenGL-optimized mesh representation for hardware-accelerated rendering.
 * 
 * Converts application mesh data into GPU-resident vertex buffer objects (VBOs)
 * and vertex array objects (VAOs) for efficient rendering. Each vertex contains:
 * - 3D position (x, y, z)
 * - Surface normal (nx, ny, nz) for lighting calculations
 * - Texture coordinates (u, v) for material mapping
 * - Surface ID (packed as float) for material identification
 * 
 * The mesh data is uploaded to GPU memory once during construction and can be
 * rendered multiple times with minimal CPU overhead using indexed drawing.
 */
public class RenderableMesh implements Renderable {
	private final int vaoId;
	private final int vboId;
	private final int eboId;
	private final int vertexCount;

	/**
	 * Creates a new renderable mesh from application mesh data.
	 * 
	 * Converts the mesh vertices and indices into OpenGL buffer objects
	 * and sets up vertex attribute pointers for efficient rendering.
	 * The vertex data is uploaded to GPU memory and the input mesh
	 * can be safely discarded after construction.
	 * 
	 * @param mesh The source mesh containing vertices and indices
	 */
	public RenderableMesh(Mesh mesh) {
		// Vertex data has 9 floats: 3 pos, 3 norm, 2 uv, 1 surfaceID (packed as float)
		FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(mesh.getVertices().size() * 9);
		for (Vertex vertex : mesh.getVertices()) {
			vertexBuffer.put(vertex.position.x).put(vertex.position.y).put(vertex.position.z);
			vertexBuffer.put(vertex.normal.x).put(vertex.normal.y).put(vertex.normal.z);
			vertexBuffer.put(vertex.texCoords.x).put(vertex.texCoords.y);
			vertexBuffer.put(Float.intBitsToFloat(vertex.surfaceID)); // Pack int as float
		}
		vertexBuffer.flip();

		IntBuffer indexBuffer = MemoryUtil.memAllocInt(mesh.getIndices().size());
		indexBuffer.put(mesh.getIndices().stream().mapToInt(i -> i).toArray());
		indexBuffer.flip();

		this.vertexCount = mesh.getIndices().size();
		int stride = 9 * Float.BYTES;

		vaoId = GL33.glGenVertexArrays();
		GL33.glBindVertexArray(vaoId);

		vboId = GL33.glGenBuffers();
		GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, vboId);
		GL33.glBufferData(GL33.GL_ARRAY_BUFFER, vertexBuffer, GL33.GL_STATIC_DRAW);

		eboId = GL33.glGenBuffers();
		GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, eboId);
		GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL33.GL_STATIC_DRAW);

		// Position attribute (location = 0)
		GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, stride, 0);
		GL33.glEnableVertexAttribArray(0);

		// Normal attribute (location = 1)
		GL33.glVertexAttribPointer(1, 3, GL33.GL_FLOAT, false, stride, 3 * Float.BYTES);
		GL33.glEnableVertexAttribArray(1);

		// Texture Coordinate attribute (location = 2)
		GL33.glVertexAttribPointer(2, 2, GL33.GL_FLOAT, false, stride, 6 * Float.BYTES);
		GL33.glEnableVertexAttribArray(2);

		// Surface ID attribute (location = 3)
		GL33.glVertexAttribPointer(3, 1, GL33.GL_FLOAT, false, stride, 8 * Float.BYTES);
		GL33.glEnableVertexAttribArray(3);

		GL33.glBindVertexArray(0);

		MemoryUtil.memFree(vertexBuffer);
		MemoryUtil.memFree(indexBuffer);
	}

	/**
	 * Renders this mesh using indexed triangle drawing.
	 * 
	 * Binds the vertex array object and issues a draw call to render
	 * all triangles in this mesh. The appropriate shader program should
	 * be active before calling this method.
	 */
    @Override
    public void render() {
		GL33.glBindVertexArray(vaoId);
		GL33.glDrawElements(GL33.GL_TRIANGLES, vertexCount, GL33.GL_UNSIGNED_INT, 0);
		GL33.glBindVertexArray(0);
	}

	/**
	 * Releases all OpenGL resources associated with this mesh.
	 * 
	 * Deletes the vertex buffer object, element buffer object, and
	 * vertex array object. This method should be called when the mesh
	 * is no longer needed to prevent GPU memory leaks.
	 */
    @Override
    public void cleanup() {
		GL33.glDeleteBuffers(vboId);
		GL33.glDeleteBuffers(eboId);
		GL33.glDeleteVertexArrays(vaoId);
	}
}
