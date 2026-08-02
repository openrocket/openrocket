package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.swing.gui.figure3d.core.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.core.geometry.IntList;
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
 * Converts application mesh data into GPU-resident buffer objects using indexed
 * (element-based) drawing:
 * - A vertex buffer object (VBO) holds the packed vertex data
 * - An element buffer object (EBO) holds the triangle indices
 * - A vertex array object (VAO) captures the buffer bindings and attribute layout
 *
 * Each vertex is packed as {@link Vertex#FLOATS_PER_VERTEX} floats:
 * - 3D position (x, y, z)
 * - Surface normal (nx, ny, nz) for lighting calculations
 * - Texture coordinates (u, v) for material mapping
 * - Surface ID (an int packed as a float) for material identification
 *
 * The mesh data is uploaded to GPU memory once during construction and can be
 * rendered multiple times with minimal CPU overhead. Note that rendering is
 * done exclusively through the EBO ({@code glDrawElements}); switching away
 * from indexed drawing would require changing both the buffer setup and the
 * draw calls, and OpenGL would give no error if they disagree.
 */
public class GLRenderableMesh implements Renderable {
	// Byte offsets of each attribute within a packed vertex
	private static final int POSITION_OFFSET_BYTES = 0;
	private static final int NORMAL_OFFSET_BYTES = Vertex.POSITION_FLOATS * Float.BYTES;
	private static final int TEX_COORD_OFFSET_BYTES = (Vertex.POSITION_FLOATS + Vertex.NORMAL_FLOATS) * Float.BYTES;
	private static final int SURFACE_ID_OFFSET_BYTES =
			(Vertex.POSITION_FLOATS + Vertex.NORMAL_FLOATS + Vertex.TEX_COORD_FLOATS) * Float.BYTES;
	private static final int STRIDE_BYTES = Vertex.FLOATS_PER_VERTEX * Float.BYTES;

	private final int vertexArrayObjectId;
	private final int vertexBufferObjectId;
	private final int elementBufferObjectId;
	private final int indexCount;
	private final Matrix4f scratchModelViewMatrix = new Matrix4f();
	private final Matrix4f lastSortedModelViewMatrix = new Matrix4f();
	private final Vector3f scratchCentroid = new Vector3f();
	private int sortedElementBufferObjectId;
	private List<Vertex> sortedVertices;
	private IntList sortedIndices;
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
	public GLRenderableMesh(Mesh mesh) {
		FloatBuffer vertexBuffer = packVertexData(mesh.getVertices());
		IntBuffer indexBuffer = packIndexData(mesh.getIndices());
		this.indexCount = mesh.getIndices().size();

		vertexArrayObjectId = GL33.glGenVertexArrays();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.VERTEX_ARRAY, vertexArrayObjectId, "mesh vao");
		GL33.glBindVertexArray(vertexArrayObjectId);

		vertexBufferObjectId = uploadVertexBuffer(vertexBuffer);
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.BUFFER, vertexBufferObjectId, "mesh vbo");
		elementBufferObjectId = uploadElementBuffer(indexBuffer);
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.BUFFER, elementBufferObjectId, "mesh ebo");
		configureVertexAttributes();

		GL33.glBindVertexArray(0);

		MemoryUtil.memFree(vertexBuffer);
		MemoryUtil.memFree(indexBuffer);

		GLErrors.check("mesh buffer upload");
	}

	/**
	 * Packs the vertices into a tightly interleaved float buffer matching the
	 * attribute layout set up in {@link #configureVertexAttributes()}.
	 */
	private static FloatBuffer packVertexData(List<Vertex> vertices) {
		FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(vertices.size() * Vertex.FLOATS_PER_VERTEX);
		for (Vertex vertex : vertices) {
			vertexBuffer.put(vertex.position.x).put(vertex.position.y).put(vertex.position.z);
			vertexBuffer.put(vertex.normal.x).put(vertex.normal.y).put(vertex.normal.z);
			vertexBuffer.put(vertex.texCoords.x).put(vertex.texCoords.y);
			vertexBuffer.put(Float.intBitsToFloat(vertex.surfaceID)); // Pack int as float
		}
		vertexBuffer.flip();
		return vertexBuffer;
	}

	private static IntBuffer packIndexData(IntList indices) {
		IntBuffer indexBuffer = MemoryUtil.memAllocInt(indices.size());
		for (int i = 0; i < indices.size(); i++) {
			indexBuffer.put(indices.get(i));
		}
		indexBuffer.flip();
		return indexBuffer;
	}

	private static int uploadVertexBuffer(FloatBuffer vertexBuffer) {
		int bufferId = GL33.glGenBuffers();
		GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, bufferId);
		GL33.glBufferData(GL33.GL_ARRAY_BUFFER, vertexBuffer, GL33.GL_STATIC_DRAW);
		return bufferId;
	}

	private static int uploadElementBuffer(IntBuffer indexBuffer) {
		int bufferId = GL33.glGenBuffers();
		GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, bufferId);
		GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL33.GL_STATIC_DRAW);
		return bufferId;
	}

	/**
	 * Sets up the vertex attribute pointers for the currently bound VAO/VBO.
	 * The attribute locations, sizes, and offsets must stay in sync with the
	 * packing in {@link #packVertexData(List)} and the vertex shader inputs.
	 */
	private static void configureVertexAttributes() {
		// Position attribute (location = 0)
		GL33.glVertexAttribPointer(0, Vertex.POSITION_FLOATS, GL33.GL_FLOAT, false, STRIDE_BYTES, POSITION_OFFSET_BYTES);
		GL33.glEnableVertexAttribArray(0);

		// Normal attribute (location = 1)
		GL33.glVertexAttribPointer(1, Vertex.NORMAL_FLOATS, GL33.GL_FLOAT, false, STRIDE_BYTES, NORMAL_OFFSET_BYTES);
		GL33.glEnableVertexAttribArray(1);

		// Texture coordinate attribute (location = 2)
		GL33.glVertexAttribPointer(2, Vertex.TEX_COORD_FLOATS, GL33.GL_FLOAT, false, STRIDE_BYTES, TEX_COORD_OFFSET_BYTES);
		GL33.glEnableVertexAttribArray(2);

		// Surface ID attribute (location = 3)
		GL33.glVertexAttribPointer(3, Vertex.SURFACE_ID_FLOATS, GL33.GL_FLOAT, false, STRIDE_BYTES, SURFACE_ID_OFFSET_BYTES);
		GL33.glEnableVertexAttribArray(3);
	}

	/**
	 * Renders this mesh using indexed (element-based) triangle drawing.
	 *
	 * Binds the vertex array object and element buffer object, then issues a
	 * {@code glDrawElements} call to render all triangles in this mesh. This
	 * draw call only works with the EBO setup done in the constructor; it
	 * would silently render nothing if the element buffer were removed.
	 * The appropriate shader program should be active before calling this method.
	 */
	@Override
	public void render() {
		GL33.glBindVertexArray(vertexArrayObjectId);
		GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, elementBufferObjectId);
		GL33.glDrawElements(GL33.GL_TRIANGLES, indexCount, GL33.GL_UNSIGNED_INT, 0);
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

		GL33.glBindVertexArray(vertexArrayObjectId);
		GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, sortedElementBufferObjectId);
		GL33.glDrawElements(GL33.GL_TRIANGLES, indexCount, GL33.GL_UNSIGNED_INT, 0);
		GL33.glBindVertexArray(0);
	}

	private void ensureSortedResources(Mesh mesh) {
		if (sortedElementBufferObjectId != 0) {
			return;
		}

		sortedVertices = mesh.getVertices();
		sortedIndices = mesh.getIndices();
		triangleSortKeys = new long[sortedIndices.size() / 3];
		sortedIndexBuffer = MemoryUtil.memAllocInt(sortedIndices.size());

		sortedElementBufferObjectId = GL33.glGenBuffers();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.BUFFER, sortedElementBufferObjectId, "mesh sorted ebo");
		GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, sortedElementBufferObjectId);
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

		GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, sortedElementBufferObjectId);
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
		GpuResourceTracker.release(GpuResourceTracker.ResourceType.BUFFER, vertexBufferObjectId);
		GL33.glDeleteBuffers(vertexBufferObjectId);
		GpuResourceTracker.release(GpuResourceTracker.ResourceType.BUFFER, elementBufferObjectId);
		GL33.glDeleteBuffers(elementBufferObjectId);
		if (sortedElementBufferObjectId != 0) {
			GpuResourceTracker.release(GpuResourceTracker.ResourceType.BUFFER, sortedElementBufferObjectId);
			GL33.glDeleteBuffers(sortedElementBufferObjectId);
		}
		GpuResourceTracker.release(GpuResourceTracker.ResourceType.VERTEX_ARRAY, vertexArrayObjectId);
		GL33.glDeleteVertexArrays(vertexArrayObjectId);
		if (sortedIndexBuffer != null) {
			MemoryUtil.memFree(sortedIndexBuffer);
		}
	}
}
