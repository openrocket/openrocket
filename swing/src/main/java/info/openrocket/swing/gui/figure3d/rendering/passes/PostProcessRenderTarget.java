package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.swing.gui.figure3d.rendering.GpuResource;
import info.openrocket.swing.gui.figure3d.rendering.GpuResourceTracker;
import info.openrocket.swing.gui.figure3d.rendering.TextureStateManager;

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
import static org.lwjgl.opengl.GL33.glBindFramebuffer;
import static org.lwjgl.opengl.GL33.glBindVertexArray;
import static org.lwjgl.opengl.GL33.glCheckFramebufferStatus;
import static org.lwjgl.opengl.GL33.glDeleteFramebuffers;
import static org.lwjgl.opengl.GL33.glDeleteTextures;
import static org.lwjgl.opengl.GL33.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL33.glGenFramebuffers;
import static org.lwjgl.opengl.GL33.glGenTextures;

/**
 * Color-only framebuffer used by screen-space post-processing passes.
 */
final class PostProcessRenderTarget implements GpuResource {

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
		glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);
	}

	void unbind() {
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
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
		framebufferId = glGenFramebuffers();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.FRAMEBUFFER, framebufferId,
				label + " framebuffer " + width + "x" + height);
		glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);

		colorTextureId = glGenTextures();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.TEXTURE, colorTextureId,
				label + " color texture");
		glBindTexture(GL_TEXTURE_2D, colorTextureId);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB8_ALPHA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE,
				(java.nio.ByteBuffer) null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorTextureId, 0);

		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
			glBindFramebuffer(GL_FRAMEBUFFER, 0);
			cleanup();
			throw new IllegalStateException(label + " framebuffer is not complete");
		}

		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	@Override
	public void cleanup() {
		if (framebufferId != 0) {
			GpuResourceTracker.release(GpuResourceTracker.ResourceType.FRAMEBUFFER, framebufferId);
			glDeleteFramebuffers(framebufferId);
			framebufferId = 0;
		}
		if (colorTextureId != 0) {
			GpuResourceTracker.release(GpuResourceTracker.ResourceType.TEXTURE, colorTextureId);
			TextureStateManager.evictDeletedTexture(colorTextureId);
			glDeleteTextures(colorTextureId);
			colorTextureId = 0;
		}
	}

	static void drawFullscreenQuad(int screenQuadVAO) {
		glBindVertexArray(screenQuadVAO);
		glDrawArrays(GL_TRIANGLES, 0, 6);
		glBindVertexArray(0);
	}
}
