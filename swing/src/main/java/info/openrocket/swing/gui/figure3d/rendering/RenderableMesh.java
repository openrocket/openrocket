package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.swing.gui.figure3d.core.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.core.geometry.Vertex;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Arrays;

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
	private final Matrix4f scratchModelViewMatrix = new Matrix4f();
	private final Matrix4f lastSortedModelViewMatrix = new Matrix4f();
	private final Vector3f scratchCentroid = new Vector3f();
	private int sortedEboId;
	private List<Vertex> sortedVertices;
	private List<Integer> sortedIndices;
	private long[] triangleSortKeys;
	private IntBuffer sortedIndexBuffer;
	private boolean sortedIndicesDirty = true;

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
		List<Vertex> vertices = mesh.getVertices();
		FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(vertices.size() * 9);
		for (Vertex vertex : vertices) {
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

		GLErrors.check("mesh buffer upload");
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
		GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, eboId);
		GL33.glDrawElements(GL33.GL_TRIANGLES, vertexCount, GL33.GL_UNSIGNED_INT, 0);
		GL33.glBindVertexArray(0);
	}

	public void renderSorted(Mesh mesh, Matrix4f modelMatrix, Matrix4f viewMatrix) {
		ensureSortedResources(mesh);
		scratchModelViewMatrix.set(viewMatrix).mul(modelMatrix);
		if (sortedIndicesDirty || !scratchModelViewMatrix.equals(lastSortedModelViewMatrix, 1.0e-6f)) {
			resortTriangles();
			lastSortedModelViewMatrix.set(scratchModelViewMatrix);
			sortedIndicesDirty = false;
		}

		GL33.glBindVertexArray(vaoId);
		GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, sortedEboId);
		GL33.glDrawElements(GL33.GL_TRIANGLES, vertexCount, GL33.GL_UNSIGNED_INT, 0);
		GL33.glBindVertexArray(0);
	}

	private void ensureSortedResources(Mesh mesh) {
		if (sortedEboId != 0) {
			return;
		}

		sortedVertices = mesh.getVertices();
		sortedIndices = mesh.getIndices();
		triangleSortKeys = new long[sortedIndices.size() / 3];
		sortedIndexBuffer = MemoryUtil.memAllocInt(sortedIndices.size());

		sortedEboId = GL33.glGenBuffers();
		GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, sortedEboId);
		GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, (long) sortedIndices.size() * Integer.BYTES, GL33.GL_DYNAMIC_DRAW);
		sortedIndicesDirty = true;
	}

	private void resortTriangles() {
		for (int triangle = 0; triangle < triangleSortKeys.length; triangle++) {
			int index = triangle * 3;
			int i0 = sortedIndices.get(index);
			int i1 = sortedIndices.get(index + 1);
			int i2 = sortedIndices.get(index + 2);

			Vertex v0 = sortedVertices.get(i0);
			Vertex v1 = sortedVertices.get(i1);
			Vertex v2 = sortedVertices.get(i2);

			scratchCentroid.set(v0.position)
					.add(v1.position)
					.add(v2.position)
					.mul(1.0f / 3.0f);
			scratchModelViewMatrix.transformPosition(scratchCentroid);
			triangleSortKeys[triangle] = createTriangleSortKey(scratchCentroid.z, triangle);
		}

		Arrays.sort(triangleSortKeys);

		sortedIndexBuffer.clear();
		for (long sortKey : triangleSortKeys) {
			int triangle = (int) sortKey;
			int index = triangle * 3;
			sortedIndexBuffer.put(sortedIndices.get(index))
					.put(sortedIndices.get(index + 1))
					.put(sortedIndices.get(index + 2));
		}
		sortedIndexBuffer.flip();

		GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, sortedEboId);
		GL33.glBufferSubData(GL33.GL_ELEMENT_ARRAY_BUFFER, 0, sortedIndexBuffer);
	}

	private static long createTriangleSortKey(float depth, int triangle) {
		int bits = Float.floatToRawIntBits(depth);
		int sortableDepth = bits ^ ((bits >> 31) | 0x80000000);
		return (Integer.toUnsignedLong(sortableDepth) << 32) | Integer.toUnsignedLong(triangle);
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
		if (sortedEboId != 0) {
			GL33.glDeleteBuffers(sortedEboId);
		}
		GL33.glDeleteVertexArrays(vaoId);
		if (sortedIndexBuffer != null) {
			MemoryUtil.memFree(sortedIndexBuffer);
		}
	}
}
