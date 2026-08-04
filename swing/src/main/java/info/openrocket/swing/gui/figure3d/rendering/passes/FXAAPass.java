package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.swing.gui.figure3d.rendering.GLShader;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

/**
 * Fast approximate anti-aliasing, applied as a single full-screen pass over the
 * rendered scene texture in linear colour space.
 */
public class FXAAPass implements ScreenTexturePass {

	private final GLShader shader;
	private final int screenQuadVAO;
	private final PostProcessRenderTarget target;
	private int inputTexture;

	/**
	 * Creates a new FXAA post-processing pass.
	 * 
	 * Initializes the FXAA shaders and creates framebuffer resources for the
	 * specified resolution. The pass is ready to process input textures immediately.
	 * 
	 * @param screenQuadVAO Vertex array object for full-screen quad rendering
	 * @param initialWidth Initial framebuffer width in pixels
	 * @param initialHeight Initial framebuffer height in pixels
	 * @throws ShaderException If shader compilation fails
	 */
	public FXAAPass(int screenQuadVAO, int initialWidth, int initialHeight) {
		this.shader = new GLShader("/shaders/post/fxaa_vertex.glsl", "/shaders/post/fxaa_fragment.glsl");
		this.shader.requireUniformLocations("screenTexture", "rt_w", "rt_h");
		this.screenQuadVAO = screenQuadVAO;
		this.target = new PostProcessRenderTarget("FXAA", initialWidth, initialHeight);
		this.shader.use();
		this.shader.setUniformInt("screenTexture", 0); // Set texture unit once
		this.shader.unbind();
	}

	@Override
	public void setInputTexture(int textureId) {
		this.inputTexture = textureId;
	}

	@Override
	public int getOutputTexture() {
		return target.getColorTextureId();
	}

	@Override
	public void render(SceneView scene, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
		target.bind();
		glClear(GL_COLOR_BUFFER_BIT);
		glDisable(GL_DEPTH_TEST);

		shader.use();
		shader.setUniformFloat("rt_w", (float) target.getWidth());
		shader.setUniformFloat("rt_h", (float) target.getHeight());

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, inputTexture);

		PostProcessRenderTarget.drawFullscreenQuad(screenQuadVAO);

		shader.unbind();
		target.unbind();
		glEnable(GL_DEPTH_TEST);
	}

	@Override
	public void resize(int width, int height) {
		target.resize(width, height);
	}

	@Override
	public void cleanup() {
		shader.cleanup();
		target.cleanup();
	}
}
