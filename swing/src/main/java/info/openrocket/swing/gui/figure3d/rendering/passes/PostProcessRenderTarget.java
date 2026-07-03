package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.swing.gui.figure3d.rendering.GpuResourceTracker;
import org.lwjgl.opengl.GL33;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL21.GL_SRGB8_ALPHA8;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;

/**
 * Color-only framebuffer used by screen-space post-processing passes.
 */
final class PostProcessRenderTarget {

	private final String label;
	private int framebufferId;
	private int colorTextureId;
	private int width;
	private int height;

	PostProcessRenderTarget(String label, int width, int height) {
		this.label = label;
		resize(width, height);
	}

	void bind() {
		GL33.glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);
	}

	void unbind() {
		GL33.glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	int getColorTextureId() {
		return colorTextureId;
	}

	int getWidth() {
		return width;
	}

	int getHeight() {
		return height;
	}

	void resize(int newWidth, int newHeight) {
		if (newWidth == width && newHeight == height && framebufferId != 0) {
			return;
		}

		width = newWidth;
		height = newHeight;
		cleanup();
		initialize();
	}

	private void initialize() {
		framebufferId = GL33.glGenFramebuffers();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.FRAMEBUFFER, framebufferId,
				label + " framebuffer " + width + "x" + height);
		GL33.glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);

		colorTextureId = GL33.glGenTextures();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.TEXTURE, colorTextureId,
				label + " color texture");
		glBindTexture(GL_TEXTURE_2D, colorTextureId);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB8_ALPHA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE,
				(java.nio.ByteBuffer) null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		GL33.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorTextureId, 0);

		if (GL33.glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
			GL33.glBindFramebuffer(GL_FRAMEBUFFER, 0);
			cleanup();
			throw new IllegalStateException(label + " framebuffer is not complete");
		}

		GL33.glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	void cleanup() {
		if (framebufferId != 0) {
			GpuResourceTracker.release(GpuResourceTracker.ResourceType.FRAMEBUFFER, framebufferId);
			GL33.glDeleteFramebuffers(framebufferId);
			framebufferId = 0;
		}
		if (colorTextureId != 0) {
			GpuResourceTracker.release(GpuResourceTracker.ResourceType.TEXTURE, colorTextureId);
			GL33.glDeleteTextures(colorTextureId);
			colorTextureId = 0;
		}
	}

	static void drawFullscreenQuad(int screenQuadVAO) {
		GL33.glBindVertexArray(screenQuadVAO);
		glDrawArrays(GL_TRIANGLES, 0, 6);
		GL33.glBindVertexArray(0);
	}
}
