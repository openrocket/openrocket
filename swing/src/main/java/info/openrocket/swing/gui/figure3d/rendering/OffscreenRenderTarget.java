package info.openrocket.swing.gui.figure3d.rendering;


import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_DEPTH_COMPONENT24;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL21.GL_SRGB8_ALPHA8;
import static org.lwjgl.opengl.GL33.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL33.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL33.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL33.GL_DRAW_FRAMEBUFFER;
import static org.lwjgl.opengl.GL33.GL_MAX_SAMPLES;
import static org.lwjgl.opengl.GL33.GL_READ_FRAMEBUFFER;
import static org.lwjgl.opengl.GL33.GL_RENDERBUFFER;
import static org.lwjgl.opengl.GL33.glBindFramebuffer;
import static org.lwjgl.opengl.GL33.glBindRenderbuffer;
import static org.lwjgl.opengl.GL33.glBlitFramebuffer;
import static org.lwjgl.opengl.GL33.glCheckFramebufferStatus;
import static org.lwjgl.opengl.GL33.glDeleteFramebuffers;
import static org.lwjgl.opengl.GL33.glDeleteRenderbuffers;
import static org.lwjgl.opengl.GL33.glDeleteTextures;
import static org.lwjgl.opengl.GL33.glFramebufferRenderbuffer;
import static org.lwjgl.opengl.GL33.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL33.glGenFramebuffers;
import static org.lwjgl.opengl.GL33.glGenRenderbuffers;
import static org.lwjgl.opengl.GL33.glGenTextures;
import static org.lwjgl.opengl.GL33.glGetInteger;
import static org.lwjgl.opengl.GL33.glRenderbufferStorageMultisample;

/**
 * Utility wrapper around an OpenGL framebuffer/texture pair that is used as
 * the render target for the multi-pass pipeline.
 *
 * <p>This class keeps the bookkeeping for creating, resizing and destroying the
 * framebuffer attachments in a single place so the renderer can focus on draw
 * logic.</p>
 */
public class OffscreenRenderTarget implements GpuResource {

	private int framebufferId;
	private int colorTextureId;
	private int depthTextureId;
	private int multisampleFramebufferId;
	private int multisampleColorRenderbufferId;
	private int multisampleDepthRenderbufferId;
	private int width;
	private int height;
	private int requestedSamples;
	private int activeSamples;

	public OffscreenRenderTarget(int width, int height) {
		this(width, height, 0);
	}

	public OffscreenRenderTarget(int width, int height, int samples) {
		requestedSamples = Math.max(0, samples);
		resize(width, height);
	}

	public void bind() {
		glBindFramebuffer(GL_FRAMEBUFFER, activeSamples > 1 ? multisampleFramebufferId : framebufferId);
	}

	public void unbind() {
		resolveMultisampleAttachments();
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	public int getColorTextureId() {
		return colorTextureId;
	}

	public int getFramebufferId() {
		return framebufferId;
	}

	public int getDepthTextureId() {
		return depthTextureId;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getSamples() {
		return activeSamples;
	}

	public void setSamples(int samples) {
		int sanitizedSamples = Math.max(0, samples);
		if (requestedSamples == sanitizedSamples && framebufferId != 0) {
			return;
		}
		requestedSamples = sanitizedSamples;
		cleanup();
		initializeAttachments();
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
		activeSamples = determineActiveSampleCount(requestedSamples);

		framebufferId = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);

		colorTextureId = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, colorTextureId);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB8_ALPHA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorTextureId, 0);

		depthTextureId = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, depthTextureId);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24, width, height, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, (java.nio.ByteBuffer) null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthTextureId, 0);

		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
			throw new IllegalStateException("Failed to create off-screen framebuffer");
		}

		GpuResourceTracker.register(GpuResourceTracker.ResourceType.FRAMEBUFFER, framebufferId,
				"OffscreenRenderTarget resolve " + width + "x" + height);
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.TEXTURE, colorTextureId, "OffscreenRenderTarget resolve color");
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.TEXTURE, depthTextureId, "OffscreenRenderTarget resolve depth");

		if (activeSamples > 1) {
			initializeMultisampleAttachments();
		}

		glBindFramebuffer(GL_FRAMEBUFFER, 0);

		GLErrors.check("off-screen render target creation");
	}

	private void initializeMultisampleAttachments() {
		multisampleFramebufferId = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, multisampleFramebufferId);

		multisampleColorRenderbufferId = glGenRenderbuffers();
		glBindRenderbuffer(GL_RENDERBUFFER, multisampleColorRenderbufferId);
		glRenderbufferStorageMultisample(GL_RENDERBUFFER, activeSamples, GL_SRGB8_ALPHA8, width, height);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER,
				multisampleColorRenderbufferId);

		multisampleDepthRenderbufferId = glGenRenderbuffers();
		glBindRenderbuffer(GL_RENDERBUFFER, multisampleDepthRenderbufferId);
		glRenderbufferStorageMultisample(GL_RENDERBUFFER, activeSamples, GL_DEPTH_COMPONENT24, width, height);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER,
				multisampleDepthRenderbufferId);

		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
			throw new IllegalStateException("Failed to create multisampled off-screen framebuffer");
		}

		GpuResourceTracker.register(GpuResourceTracker.ResourceType.FRAMEBUFFER, multisampleFramebufferId,
				"OffscreenRenderTarget msaa " + width + "x" + height + " (" + activeSamples + "x)");
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.RENDERBUFFER, multisampleColorRenderbufferId,
				"OffscreenRenderTarget msaa color");
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.RENDERBUFFER, multisampleDepthRenderbufferId,
				"OffscreenRenderTarget msaa depth");
	}

	private void resolveMultisampleAttachments() {
		if (activeSamples <= 1 || multisampleFramebufferId == 0 || framebufferId == 0) {
			return;
		}

		glBindFramebuffer(GL_READ_FRAMEBUFFER, multisampleFramebufferId);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, framebufferId);
		glBlitFramebuffer(0, 0, width, height,
				0, 0, width, height,
				GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT,
				GL_NEAREST);
	}

	private int determineActiveSampleCount(int requestedSampleCount) {
		if (requestedSampleCount <= 1) {
			return 0;
		}

		int maxSamples = glGetInteger(GL_MAX_SAMPLES);
		if (maxSamples <= 1) {
			return 0;
		}

		return Math.min(requestedSampleCount, maxSamples);
	}

	@Override
	public void cleanup() {
		activeSamples = 0;
		if (multisampleFramebufferId != 0) {
			GpuResourceTracker.release(GpuResourceTracker.ResourceType.FRAMEBUFFER, multisampleFramebufferId);
			glDeleteFramebuffers(multisampleFramebufferId);
			multisampleFramebufferId = 0;
		}
		if (multisampleColorRenderbufferId != 0) {
			GpuResourceTracker.release(GpuResourceTracker.ResourceType.RENDERBUFFER, multisampleColorRenderbufferId);
			glDeleteRenderbuffers(multisampleColorRenderbufferId);
			multisampleColorRenderbufferId = 0;
		}
		if (multisampleDepthRenderbufferId != 0) {
			GpuResourceTracker.release(GpuResourceTracker.ResourceType.RENDERBUFFER, multisampleDepthRenderbufferId);
			glDeleteRenderbuffers(multisampleDepthRenderbufferId);
			multisampleDepthRenderbufferId = 0;
		}
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
		if (depthTextureId != 0) {
			GpuResourceTracker.release(GpuResourceTracker.ResourceType.TEXTURE, depthTextureId);
			TextureStateManager.evictDeletedTexture(depthTextureId);
			glDeleteTextures(depthTextureId);
			depthTextureId = 0;
		}
	}
}
