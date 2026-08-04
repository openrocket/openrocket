package info.openrocket.swing.gui.figure3d.rendering;

import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_DOUBLEBUFFER;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glColorMask;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glDrawBuffer;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL11.glPolygonMode;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_SRGB;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Shared full-screen geometry and texture shader used by post-processing and presentation.
 */
public final class FullscreenQuad implements GpuResource {

	private static final String VERTEX_SHADER_PATH = "/shaders/post/screen_quad_vertex.glsl";
	private static final String FRAGMENT_SHADER_PATH = "/shaders/post/screen_quad_fragment.glsl";
	private static final int POSITION_COMPONENTS = 2;
	private static final int TEXTURE_COORDINATE_COMPONENTS = 2;
	private static final int FLOATS_PER_VERTEX = POSITION_COMPONENTS + TEXTURE_COORDINATE_COMPONENTS;
	private static final int VERTEX_COUNT = 6;
	private static final int TEXTURE_UNIT = 0;
	private static final int VERTEX_STRIDE_BYTES = FLOATS_PER_VERTEX * Float.BYTES;
	private static final long TEXTURE_COORDINATE_OFFSET_BYTES = POSITION_COMPONENTS * Float.BYTES;

	private static final float[] VERTICES = {
			-1.0f,  1.0f,  0.0f, 1.0f,
			-1.0f, -1.0f,  0.0f, 0.0f,
			1.0f, -1.0f,  1.0f, 0.0f,

			-1.0f,  1.0f,  0.0f, 1.0f,
			1.0f, -1.0f,  1.0f, 0.0f,
			1.0f,  1.0f,  1.0f, 1.0f
	};

	private final GLShader textureShader;
	private final int textureUniform;
	private final int gammaCorrectionUniform;
	private final int vertexArrayId;
	private final int vertexBufferId;

	FullscreenQuad() {
		textureShader = new GLShader(VERTEX_SHADER_PATH, FRAGMENT_SHADER_PATH);
		textureUniform = textureShader.requireUniformLocation("screenTexture");
		gammaCorrectionUniform = textureShader.requireUniformLocation("applyGammaCorrection");

		vertexArrayId = glGenVertexArrays();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.VERTEX_ARRAY, vertexArrayId,
				"FullscreenQuad vertex array");
		vertexBufferId = glGenBuffers();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.BUFFER, vertexBufferId,
				"FullscreenQuad vertex buffer");

		glBindVertexArray(vertexArrayId);
		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
		glBufferData(GL_ARRAY_BUFFER, VERTICES, GL_STATIC_DRAW);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, POSITION_COMPONENTS, GL_FLOAT, false, VERTEX_STRIDE_BYTES, 0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(1, TEXTURE_COORDINATE_COMPONENTS, GL_FLOAT, false,
				VERTEX_STRIDE_BYTES, TEXTURE_COORDINATE_OFFSET_BYTES);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}

	/**
	 * Draws the quad with the shader and textures already prepared by the caller.
	 */
	public void draw() {
		glBindVertexArray(vertexArrayId);
		glDrawArrays(GL_TRIANGLES, 0, VERTEX_COUNT);
		glBindVertexArray(0);
	}

	/**
	 * Draws a texture to the currently bound framebuffer without gamma conversion.
	 */
	public void drawTexture(int textureId, TextureBinder textureBinder) {
		textureShader.use();
		textureBinder.bindTexture(TEXTURE_UNIT, GL_TEXTURE_2D, textureId);
		glUniform1i(textureUniform, TEXTURE_UNIT);
		glUniform1i(gammaCorrectionUniform, 0);
		draw();
	}

	void copyTextureTo(OffscreenRenderTarget target, int textureId, int width, int height) {
		target.bind();
		glViewport(0, 0, width, height);
		glDisable(GL_DEPTH_TEST);
		glClear(GL_COLOR_BUFFER_BIT);
		drawTextureDirect(textureId, false);
		glEnable(GL_DEPTH_TEST);
		target.unbind();
	}

	void present(int textureId, int width, int height) {
		if (textureId == 0) {
			return;
		}

		int presentBuffer = glGetInteger(GL_DOUBLEBUFFER) != 0 ? GL_BACK : GL_FRONT;
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glDrawBuffer(presentBuffer);
		glDepthMask(true);
		glColorMask(true, true, true, true);
		glViewport(0, 0, width, height);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_SCISSOR_TEST);
		glDisable(GL_STENCIL_TEST);
		glDisable(GL_BLEND);
		glDisable(GL_CULL_FACE);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		// Apply gamma in the shader so presentation does not depend on whether the
		// platform's default framebuffer actually exposes sRGB conversion.
		glDisable(GL_FRAMEBUFFER_SRGB);

		drawTextureDirect(textureId, true);

		glEnable(GL_FRAMEBUFFER_SRGB);
		glEnable(GL_DEPTH_TEST);
	}

	private void drawTextureDirect(int textureId, boolean applyGammaCorrection) {
		textureShader.use();
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, textureId);
		glUniform1i(textureUniform, TEXTURE_UNIT);
		glUniform1i(gammaCorrectionUniform, applyGammaCorrection ? 1 : 0);
		draw();
		if (applyGammaCorrection) {
			glUniform1i(gammaCorrectionUniform, 0);
		}
	}

	@Override
	public void cleanup() {
		textureShader.cleanup();
		GpuResourceTracker.release(GpuResourceTracker.ResourceType.VERTEX_ARRAY, vertexArrayId);
		glDeleteVertexArrays(vertexArrayId);
		GpuResourceTracker.release(GpuResourceTracker.ResourceType.BUFFER, vertexBufferId);
		glDeleteBuffers(vertexBufferId);
	}
}
