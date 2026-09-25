package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.swing.gui.figure3d.rendering.FullscreenQuad;
import info.openrocket.swing.gui.figure3d.rendering.GLErrors;
import info.openrocket.swing.gui.figure3d.rendering.GLShader;
import info.openrocket.swing.gui.figure3d.rendering.GpuResource;
import info.openrocket.swing.gui.figure3d.rendering.GpuResourceTracker;
import info.openrocket.swing.gui.figure3d.rendering.OffscreenRenderTarget;
import info.openrocket.swing.gui.figure3d.rendering.TextureBinder;
import info.openrocket.swing.gui.figure3d.rendering.TextureStateManager;

import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_ZERO;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawBuffer;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL14.glBlendFuncSeparate;
import static org.lwjgl.opengl.GL20.glDrawBuffers;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT1;
import static org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL30.GL_FUNC_ADD;
import static org.lwjgl.opengl.GL30.GL_R8;
import static org.lwjgl.opengl.GL30.GL_RED;
import static org.lwjgl.opengl.GL30.GL_RGBA16F;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glBlendEquation;
import static org.lwjgl.opengl.GL30.glCheckFramebufferStatus;
import static org.lwjgl.opengl.GL30.glClearBufferfv;
import static org.lwjgl.opengl.GL30.glDeleteFramebuffers;
import static org.lwjgl.opengl.GL30.glDeleteTextures;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;
import static org.lwjgl.opengl.GL30.glGenTextures;

/**
 * Accumulates translucent fragments without depending on object or triangle draw order.
 *
 * <p>The minimum OpenGL 3.1 feature set does not provide per-attachment blend functions, so accumulation and
 * revealage are drawn in two geometry passes. Both passes read the resolved opaque depth
 * attachment and leave depth writes disabled. The result is then composited over the opaque
 * scene in one full-screen pass.</p>
 */
final class WeightedBlendedTransparency implements GpuResource {

	private static final String VERTEX_SHADER_PATH = "/shaders/post/screen_quad_vertex.glsl";
	private static final String FRAGMENT_SHADER_PATH = "/shaders/post/transparency_composite_fragment.glsl";
	private static final int ACCUMULATION_TEXTURE_UNIT = 0;
	private static final int REVEALAGE_TEXTURE_UNIT = 1;
	private static final int[] DRAW_BUFFERS = {GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1};
	private static final float[] CLEAR_ACCUMULATION = {0.0f, 0.0f, 0.0f, 0.0f};
	private static final float[] CLEAR_REVEALAGE = {1.0f, 1.0f, 1.0f, 1.0f};

	private final FullscreenQuad fullscreenQuad;
	private final TextureBinder textureBinder;
	private final GLShader compositeShader;
	private final int accumulationTextureUniform;
	private final int revealageTextureUniform;

	private int framebufferId;
	private int accumulationTextureId;
	private int revealageTextureId;
	private int attachedDepthTextureId;
	private int width;
	private int height;

	WeightedBlendedTransparency(FullscreenQuad fullscreenQuad, TextureBinder textureBinder) {
		this.fullscreenQuad = fullscreenQuad;
		this.textureBinder = textureBinder;
		this.compositeShader = new GLShader(VERTEX_SHADER_PATH, FRAGMENT_SHADER_PATH);
		this.accumulationTextureUniform = compositeShader.requireUniformLocation("accumulationTexture");
		this.revealageTextureUniform = compositeShader.requireUniformLocation("revealageTexture");
	}

	/**
	 * Runs both order-independent geometry passes and composites them into {@code sceneTarget}.
	 * The resolved scene framebuffer remains bound when this method returns.
	 */
	void render(OffscreenRenderTarget sceneTarget, Consumer<TransparencyOutputMode> renderTransparentGeometry) {
		ensureAttachments(sceneTarget);

		glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);
		glViewport(0, 0, width, height);
		glDrawBuffers(DRAW_BUFFERS);
		glClearBufferfv(GL_COLOR, 0, CLEAR_ACCUMULATION);
		glClearBufferfv(GL_COLOR, 1, CLEAR_REVEALAGE);

		glEnable(GL_DEPTH_TEST);
		glDepthMask(false);
		glEnable(GL_BLEND);
		glBlendEquation(GL_FUNC_ADD);

		try {
			glDrawBuffer(GL_COLOR_ATTACHMENT0);
			glBlendFunc(GL_ONE, GL_ONE);
			renderTransparentGeometry.accept(TransparencyOutputMode.ACCUMULATION);

			glDrawBuffer(GL_COLOR_ATTACHMENT1);
			glBlendFunc(GL_ZERO, GL_ONE_MINUS_SRC_ALPHA);
			renderTransparentGeometry.accept(TransparencyOutputMode.REVEALAGE);

			composite(sceneTarget);
		} finally {
			// Establish the state expected by the particle and overlay renderers that
			// follow this pass, even if transparent geometry threw unexpectedly.
			sceneTarget.bindResolved();
			glDrawBuffer(GL_COLOR_ATTACHMENT0);
			glBlendEquation(GL_FUNC_ADD);
			glDisable(GL_BLEND);
			glDepthMask(true);
			glEnable(GL_DEPTH_TEST);
		}
	}

	void invalidateAttachments() {
		deleteAttachments();
	}

	private void composite(OffscreenRenderTarget sceneTarget) {
		sceneTarget.bindResolved();
		glViewport(0, 0, width, height);
		glDrawBuffer(GL_COLOR_ATTACHMENT0);
		glDisable(GL_DEPTH_TEST);
		glDepthMask(false);
		glEnable(GL_BLEND);
		glBlendEquation(GL_FUNC_ADD);
		glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

		compositeShader.use();
		textureBinder.bindTexture(ACCUMULATION_TEXTURE_UNIT, GL_TEXTURE_2D, accumulationTextureId);
		textureBinder.bindTexture(REVEALAGE_TEXTURE_UNIT, GL_TEXTURE_2D, revealageTextureId);
		glUniform1i(accumulationTextureUniform, ACCUMULATION_TEXTURE_UNIT);
		glUniform1i(revealageTextureUniform, REVEALAGE_TEXTURE_UNIT);
		fullscreenQuad.draw();
	}

	private void ensureAttachments(OffscreenRenderTarget sceneTarget) {
		if (framebufferId != 0 && width == sceneTarget.getWidth() && height == sceneTarget.getHeight()
				&& attachedDepthTextureId == sceneTarget.getDepthTextureId()) {
			return;
		}

		deleteAttachments();
		width = sceneTarget.getWidth();
		height = sceneTarget.getHeight();
		attachedDepthTextureId = sceneTarget.getDepthTextureId();

		framebufferId = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);
		// SceneLighting keeps a depth-comparison texture on unit 2. Attachment setup
		// must not replace that binding with a colour texture midway through the frame.
		glActiveTexture(GL_TEXTURE0);

		accumulationTextureId = createTexture(GL_RGBA16F, GL_RGBA, GL_FLOAT);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D,
				accumulationTextureId, 0);

		revealageTextureId = createTexture(GL_R8, GL_RED, GL_UNSIGNED_BYTE);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D,
				revealageTextureId, 0);

		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D,
				attachedDepthTextureId, 0);
		glDrawBuffers(DRAW_BUFFERS);

		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
			deleteAttachments();
			throw new IllegalStateException("Failed to create weighted transparency framebuffer");
		}

		GpuResourceTracker.register(GpuResourceTracker.ResourceType.FRAMEBUFFER, framebufferId,
				"WeightedBlendedTransparency " + width + "x" + height);
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.TEXTURE, accumulationTextureId,
				"WeightedBlendedTransparency accumulation");
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.TEXTURE, revealageTextureId,
				"WeightedBlendedTransparency revealage");

		// Attachment creation binds textures directly. Invalidate the shared cache so
		// later material binds cannot mistake those bindings for their cached state.
		textureBinder.reset();
		GLErrors.check("weighted transparency target creation");
	}

	private int createTexture(int internalFormat, int format, int type) {
		int textureId = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, textureId);
		glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type,
				(java.nio.ByteBuffer) null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		return textureId;
	}

	@Override
	public void cleanup() {
		deleteAttachments();
		compositeShader.cleanup();
		textureBinder.reset();
	}

	private void deleteAttachments() {
		if (framebufferId != 0) {
			GpuResourceTracker.release(GpuResourceTracker.ResourceType.FRAMEBUFFER, framebufferId);
			glDeleteFramebuffers(framebufferId);
			framebufferId = 0;
		}
		if (accumulationTextureId != 0) {
			GpuResourceTracker.release(GpuResourceTracker.ResourceType.TEXTURE, accumulationTextureId);
			TextureStateManager.evictDeletedTexture(accumulationTextureId);
			glDeleteTextures(accumulationTextureId);
			accumulationTextureId = 0;
		}
		if (revealageTextureId != 0) {
			GpuResourceTracker.release(GpuResourceTracker.ResourceType.TEXTURE, revealageTextureId);
			TextureStateManager.evictDeletedTexture(revealageTextureId);
			glDeleteTextures(revealageTextureId);
			revealageTextureId = 0;
		}
		attachedDepthTextureId = 0;
		width = 0;
		height = 0;
	}
}
