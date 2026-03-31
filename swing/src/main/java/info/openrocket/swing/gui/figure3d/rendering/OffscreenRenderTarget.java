package info.openrocket.swing.gui.figure3d.rendering;

import org.lwjgl.opengl.GL33;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL30.GL_RENDERBUFFER;
import static org.lwjgl.opengl.GL21.GL_SRGB8_ALPHA8;

/**
 * Utility wrapper around an OpenGL framebuffer/texture pair that is used as
 * the render target for the multi-pass pipeline.
 *
 * <p>This class keeps the bookkeeping for creating, resizing and destroying the
 * framebuffer attachments in a single place so the renderer can focus on draw
 * logic.</p>
 */
public class OffscreenRenderTarget {

	private int framebufferId;
	private int colorTextureId;
	private int depthRenderbufferId;
	private int width;
	private int height;

	public OffscreenRenderTarget(int width, int height) {
		resize(width, height);
	}

	public void bind() {
		GL33.glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);
	}

	public void unbind() {
		GL33.glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	public int getColorTextureId() {
		return colorTextureId;
	}

	public int getFramebufferId() {
		return framebufferId;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void resize(int newWidth, int newHeight) {
		if (newWidth == width && newHeight == height && framebufferId != 0) {
			return;
		}

		width = newWidth;
		height = newHeight;

		cleanup();
		initializeAttachments();
	}

	private void initializeAttachments() {
		framebufferId = GL33.glGenFramebuffers();
		GL33.glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);

		colorTextureId = GL33.glGenTextures();
		GL33.glBindTexture(GL33.GL_TEXTURE_2D, colorTextureId);
		GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL_SRGB8_ALPHA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
		GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		GL33.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_2D, colorTextureId, 0);

		depthRenderbufferId = GL33.glGenRenderbuffers();
		GL33.glBindRenderbuffer(GL_RENDERBUFFER, depthRenderbufferId);
		GL33.glRenderbufferStorage(GL_RENDERBUFFER, GL33.GL_DEPTH_COMPONENT, width, height);
		GL33.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL33.GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthRenderbufferId);

		if (GL33.glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
			throw new IllegalStateException("Failed to create off-screen framebuffer");
		}

		GpuResourceTracker.register(GpuResourceTracker.ResourceType.FRAMEBUFFER, framebufferId, "OffscreenRenderTarget " + width + "x" + height);
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.TEXTURE, colorTextureId, "OffscreenRenderTarget color");
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.RENDERBUFFER, depthRenderbufferId, "OffscreenRenderTarget depth");

		GL33.glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	public void cleanup() {
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
		if (depthRenderbufferId != 0) {
			GpuResourceTracker.release(GpuResourceTracker.ResourceType.RENDERBUFFER, depthRenderbufferId);
			GL33.glDeleteRenderbuffers(depthRenderbufferId);
			depthRenderbufferId = 0;
		}
	}
}
