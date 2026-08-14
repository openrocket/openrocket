package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.IntList;
import info.openrocket.swing.gui.figure3d.geometry.Vertex;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

/**
 * GPU-resident indexed mesh using the vertex layout defined by {@link Vertex}.
 * Attribute packing, VAO setup, and shader inputs must remain in sync.
 */
public class GLRenderableMesh implements Renderable {
	// Byte offsets of each attribute within a packed vertex
	private static final int POSITION_OFFSET_BYTES = 0;
	private static final int NORMAL_OFFSET_BYTES = Vertex.POSITION_FLOATS * Float.BYTES;
	private static final int TEX_COORD_OFFSET_BYTES = (Vertex.POSITION_FLOATS + Vertex.NORMAL_FLOATS) * Float.BYTES;
	private static final int SURFACE_ID_OFFSET_BYTES =
			(Vertex.POSITION_FLOATS + Vertex.NORMAL_FLOATS + Vertex.TEX_COORD_FLOATS) * Float.BYTES;
	private static final int STRIDE_BYTES = Vertex.FLOATS_PER_VERTEX * Float.BYTES;
	private static final String UPLOAD_OPERATION = "mesh buffer upload";

	private final int vertexArrayObjectId;
	private final int vertexBufferObjectId;
	private final int elementBufferObjectId;
	private final int indexCount;
	private boolean cleaned;

	/** @param mesh source mesh whose data is uploaded to GL buffers */
	public GLRenderableMesh(Mesh mesh) {
		this.indexCount = mesh.getIndices().size();
		FloatBuffer vertexBuffer = null;
		IntBuffer indexBuffer = null;
		int vertexArrayId = 0;
		int vertexBufferId = 0;
		int elementBufferId = 0;
		boolean uploaded = false;
		try {
			vertexBuffer = packVertexData(mesh.getVertices());
			indexBuffer = packIndexData(mesh.getIndices());

			vertexArrayId = GL33.glGenVertexArrays();
			GpuResourceTracker.register(GpuResourceTracker.ResourceType.VERTEX_ARRAY, vertexArrayId, "mesh vao");
			GL33.glBindVertexArray(vertexArrayId);

			vertexBufferId = createBuffer("mesh vbo");
			uploadVertexBuffer(vertexBufferId, vertexBuffer);
			elementBufferId = createBuffer("mesh ebo");
			uploadElementBuffer(elementBufferId, indexBuffer);
			configureVertexAttributes();

			GLErrors.check(UPLOAD_OPERATION);
			uploaded = true;
		} finally {
			GL33.glBindVertexArray(0);
			if (vertexBuffer != null) {
				MemoryUtil.memFree(vertexBuffer);
			}
			if (indexBuffer != null) {
				MemoryUtil.memFree(indexBuffer);
			}
			if (!uploaded) {
				deleteBuffer(vertexBufferId);
				deleteBuffer(elementBufferId);
				deleteVertexArray(vertexArrayId);
			}
		}

		this.vertexArrayObjectId = vertexArrayId;
		this.vertexBufferObjectId = vertexBufferId;
		this.elementBufferObjectId = elementBufferId;
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

	private static int createBuffer(String label) {
		int bufferId = GL33.glGenBuffers();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.BUFFER, bufferId, label);
		return bufferId;
	}

	private static void uploadVertexBuffer(int bufferId, FloatBuffer vertexBuffer) {
		GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, bufferId);
		GL33.glBufferData(GL33.GL_ARRAY_BUFFER, vertexBuffer, GL33.GL_STATIC_DRAW);
	}

	private static void uploadElementBuffer(int bufferId, IntBuffer indexBuffer) {
		GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, bufferId);
		GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL33.GL_STATIC_DRAW);
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

		// Surface ID attribute (location = 3). The FloatBuffer carries the integer's
		// raw bits; use an integer attribute pointer so GLSL receives it directly.
		GL33.glVertexAttribIPointer(3, Vertex.SURFACE_ID_FLOATS, GL33.GL_INT, STRIDE_BYTES, SURFACE_ID_OFFSET_BYTES);
		GL33.glEnableVertexAttribArray(3);
	}

	/** Draws the mesh with indexed triangles; the caller must bind the shader. */
	@Override
	public void render() {
		GL33.glBindVertexArray(vertexArrayObjectId);
		GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, elementBufferObjectId);
		GL33.glDrawElements(GL33.GL_TRIANGLES, indexCount, GL33.GL_UNSIGNED_INT, 0);
		GL33.glBindVertexArray(0);
	}

	@Override
	public void cleanup() {
		if (cleaned) {
			return;
		}
		cleaned = true;
		deleteBuffer(vertexBufferObjectId);
		deleteBuffer(elementBufferObjectId);
		deleteVertexArray(vertexArrayObjectId);
	}

	private static void deleteBuffer(int bufferId) {
		if (bufferId == 0) {
			return;
		}
		GpuResourceTracker.release(GpuResourceTracker.ResourceType.BUFFER, bufferId);
		GL33.glDeleteBuffers(bufferId);
	}

	private static void deleteVertexArray(int vertexArrayId) {
		if (vertexArrayId == 0) {
			return;
		}
		GpuResourceTracker.release(GpuResourceTracker.ResourceType.VERTEX_ARRAY, vertexArrayId);
		GL33.glDeleteVertexArrays(vertexArrayId);
	}
}
