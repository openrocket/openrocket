package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.swing.gui.figure3d.rendering.GLShader;
import info.openrocket.swing.gui.figure3d.rendering.FullscreenQuad;
import info.openrocket.swing.gui.figure3d.rendering.GpuResourceTracker;
import info.openrocket.swing.gui.figure3d.rendering.MainShaderUniforms;
import info.openrocket.swing.gui.figure3d.rendering.TextureBinder;
import info.openrocket.swing.gui.figure3d.rendering.TextureStateManager;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.List;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
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
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL14.glBlendFuncSeparate;
import static org.lwjgl.opengl.GL21.GL_SRGB8_ALPHA8;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL30.GL_RENDERBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glBindRenderbuffer;
import static org.lwjgl.opengl.GL30.glCheckFramebufferStatus;
import static org.lwjgl.opengl.GL30.glDeleteFramebuffers;
import static org.lwjgl.opengl.GL30.glDeleteRenderbuffers;
import static org.lwjgl.opengl.GL30.glFramebufferRenderbuffer;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;
import static org.lwjgl.opengl.GL30.glGenRenderbuffers;
import static org.lwjgl.opengl.GL30.glRenderbufferStorage;
import static org.lwjgl.opengl.GL30.glUniform1f;
import static org.lwjgl.opengl.GL30.glUniform1i;
import static org.lwjgl.opengl.GL30.glUniform2f;
import static org.lwjgl.opengl.GL30.glUniform4f;

/**
 * Draws coloured outlines around the selected objects.
 *
 * <p>Runs in five steps, over a scene framebuffer and a separate mask framebuffer:
 * copy the scene texture to the output, render the selected objects as white
 * silhouettes into the mask, detect edges in the mask, colour those edges, and
 * blend the result over the scene. The whole pass is skipped when nothing is
 * selected.</p>
 */
public class OutlinePass implements ScreenTexturePass {

	private final GLShader mainShader;
	private final MainShaderUniforms mainShaderUniforms;
	private final GLShader outlinePostProcessShader;
	private final TextureBinder textureStateManager;
	private final FullscreenQuad fullscreenQuad;
	private final PostProcessRenderTarget outlineTarget;
	private int maskFBO;
	private int maskTexture;
	private int maskDepthRBO;
	private final Vector4f selectionColor;
	private static final float SELECTION_OUTLINE_WIDTH = 5.0f;
	private int screenWidth;
	private int screenHeight;
	private int inputTexture;
	private boolean hasSelection;
	private final int selectionTextureUniform;
	private final int outlineColorUniform;
	private final int outlineWidthUniform;
	private final int screenSizeUniform;

	/**
	 * Creates a new outline rendering pass with dual framebuffer system.
	 * 
	 * Initializes the complete outline rendering pipeline including scene composition,
	 * mask generation, and edge detection shaders. Sets up framebuffer resources for
	 * the specified resolution.
	 * 
	 * @param mainShader The main geometry shader for mask rendering
	 * @param mainShaderUniforms Cached uniform locations for performance
	 * @param textureStateManager Manager for optimized texture state changes
	 * @param fullscreenQuad shared full-screen geometry and texture renderer
	 * @param selectionColor RGBA color for outline rendering
	 * @param initialWidth Initial framebuffer width in pixels
	 * @param initialHeight Initial framebuffer height in pixels
	 * @throws ShaderException If shader compilation fails
	 */
	public OutlinePass(GLShader mainShader, MainShaderUniforms mainShaderUniforms,
					   TextureBinder textureStateManager, FullscreenQuad fullscreenQuad,
					   Vector4f selectionColor, int initialWidth, int initialHeight) {
		this.mainShader = mainShader;
		this.mainShaderUniforms = mainShaderUniforms;
		this.outlinePostProcessShader = new GLShader("/shaders/post/outline_vertex.glsl", "/shaders/post/outline_fragment.glsl");
		this.textureStateManager = textureStateManager;
		this.fullscreenQuad = fullscreenQuad;
		this.selectionColor = selectionColor;
		this.screenWidth = initialWidth;
		this.screenHeight = initialHeight;
		this.outlineTarget = new PostProcessRenderTarget("Outline output", initialWidth, initialHeight);
		createMaskFramebuffer(initialWidth, initialHeight);

		this.selectionTextureUniform = outlinePostProcessShader.requireUniformLocation("selectionTexture");
		this.outlineColorUniform = outlinePostProcessShader.requireUniformLocation("outlineColor");
		this.outlineWidthUniform = outlinePostProcessShader.requireUniformLocation("outlineWidth");
		this.screenSizeUniform = outlinePostProcessShader.requireUniformLocation("screenSize");
	}

	private void createMaskFramebuffer(int width, int height) {
		maskFBO = glGenFramebuffers();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.FRAMEBUFFER, maskFBO,
				"Outline mask framebuffer " + width + "x" + height);
		glBindFramebuffer(GL_FRAMEBUFFER, maskFBO);
		maskTexture = createTexture(width, height, GL_SRGB8_ALPHA8, GL_RGBA, GL_UNSIGNED_BYTE, "Outline mask texture");
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, maskTexture, 0);
		maskDepthRBO = glGenRenderbuffers();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.RENDERBUFFER, maskDepthRBO,
				"Outline mask depth renderbuffer");
		glBindRenderbuffer(GL_RENDERBUFFER, maskDepthRBO);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, maskDepthRBO);
		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
			cleanupMaskFramebuffer();
			throw new IllegalStateException("Mask framebuffer is not complete");
		}

		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	private int createTexture(int width, int height, int internalFormat, int format, int type, String label) {
		int texture = glGenTextures();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.TEXTURE, texture, label);
		glBindTexture(GL_TEXTURE_2D, texture);
		glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, (java.nio.ByteBuffer) null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		return texture;
	}

	@Override
	public void render(SceneView scene, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
		List<SceneObject> selectedObjects = scene.getSelectedObjects();
		hasSelection = !selectedObjects.isEmpty();

		// Always composite through the outline FBO so selected and non-selected frames
		// follow the same presentation path.
		// 1. Bind the outline FBO to draw the final result of this pass
		outlineTarget.bind();
		glViewport(0, 0, screenWidth, screenHeight);
		glClear(GL_COLOR_BUFFER_BIT);

		// 2. Draw the input texture (the scene from the previous pass) into our FBO
		drawScreenTexture(inputTexture);

		if (hasSelection) {
			// 3. Render the outlines on top
			// Bind the mask FBO
			glBindFramebuffer(GL_FRAMEBUFFER, maskFBO);
			glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			// Render selected objects in white into the mask texture
			mainShader.use();
			mainShader.setUniformMatrix4f(mainShaderUniforms.projection, projectionMatrix);
			mainShader.setUniformMatrix4f(mainShaderUniforms.view, viewMatrix);
			glUniform1i(mainShaderUniforms.forceWhite, 1);
			glEnable(GL_DEPTH_TEST);

			for (SceneObject obj : selectedObjects) {
				mainShader.setUniformMatrix4f(mainShaderUniforms.model, obj.getModelMatrix());
				obj.getRenderableMesh().render();
			}

			glUniform1i(mainShaderUniforms.forceWhite, 0);

			// Now, blend the outlines onto our main FBO
			outlineTarget.bind();
			renderOutlinePostProcess(screenWidth, screenHeight, maskTexture);
		}

		// Unbind FBO, ready for the next pass
		outlineTarget.unbind();
		glEnable(GL_DEPTH_TEST);
	}

	@Override
	public void resize(int width, int height) {
		this.screenWidth = width;
		this.screenHeight = height;
		outlineTarget.resize(width, height);
		cleanupMaskFramebuffer();
		createMaskFramebuffer(width, height);
	}

	@Override
	public void setInputTexture(int textureId) {
		this.inputTexture = textureId;
	}

	@Override
	public int getOutputTexture() {
		return outlineTarget.getColorTextureId();
	}

	/**
	 * Renders a texture to the current framebuffer using full-screen quad.
	 * 
	 * @param textureId The OpenGL texture ID to render
	 */
	private void drawScreenTexture(int textureId) {
		glDisable(GL_DEPTH_TEST);
		fullscreenQuad.drawTexture(textureId, textureStateManager);
	}

	/**
	 * Applies edge detection and outline rendering using the selection mask.
	 * 
	 * Processes the selection mask texture to detect edges and renders colored
	 * outlines with proper alpha blending over the scene.
	 * 
	 * @param width Screen width for edge detection calculations
	 * @param height Screen height for edge detection calculations
	 * @param maskTexture The selection mask texture containing object silhouettes
	 */
	private void renderOutlinePostProcess(int width, int height, int maskTexture) {
		glDisable(GL_DEPTH_TEST);
		glEnable(GL_BLEND);
		glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

		outlinePostProcessShader.use();

		textureStateManager.bindTexture(0, GL_TEXTURE_2D, maskTexture);
		glUniform1i(selectionTextureUniform, 0);

		glUniform4f(outlineColorUniform,
				selectionColor.x, selectionColor.y, selectionColor.z, selectionColor.w);
		glUniform1f(outlineWidthUniform, SELECTION_OUTLINE_WIDTH);
		glUniform2f(screenSizeUniform, (float)width, (float)height);

		fullscreenQuad.draw();

		glDisable(GL_BLEND);
		glEnable(GL_DEPTH_TEST);
	}

	@Override
	public void cleanup() {
		outlinePostProcessShader.cleanup();
		outlineTarget.cleanup();
		cleanupMaskFramebuffer();
	}

	private void cleanupMaskFramebuffer() {
		if (maskFBO != 0) {
			GpuResourceTracker.release(GpuResourceTracker.ResourceType.FRAMEBUFFER, maskFBO);
			glDeleteFramebuffers(maskFBO);
			maskFBO = 0;
		}
		if (maskTexture != 0) {
			GpuResourceTracker.release(GpuResourceTracker.ResourceType.TEXTURE, maskTexture);
			TextureStateManager.evictDeletedTexture(maskTexture);
			glDeleteTextures(maskTexture);
			maskTexture = 0;
		}
		if (maskDepthRBO != 0) {
			GpuResourceTracker.release(GpuResourceTracker.ResourceType.RENDERBUFFER, maskDepthRBO);
			glDeleteRenderbuffers(maskDepthRBO);
			maskDepthRBO = 0;
		}
	}
}
