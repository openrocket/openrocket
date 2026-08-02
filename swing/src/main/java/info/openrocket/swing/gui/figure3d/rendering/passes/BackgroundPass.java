package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.swing.gui.figure3d.rendering.GLShader;
import info.openrocket.swing.gui.figure3d.rendering.TextureBinder;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.Background;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.GradientBackground;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.HDRIBackground;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.ImageBackground;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.SkyboxBackground;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.SolidColorBackground;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_LESS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL33.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL33.GL_FLOAT;
import static org.lwjgl.opengl.GL33.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL33.GL_TEXTURE0;
import static org.lwjgl.opengl.GL33.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL33.GL_TRIANGLES;
import static org.lwjgl.opengl.GL33.glActiveTexture;
import static org.lwjgl.opengl.GL33.glBindBuffer;
import static org.lwjgl.opengl.GL33.glBindVertexArray;
import static org.lwjgl.opengl.GL33.glBufferData;
import static org.lwjgl.opengl.GL33.glDeleteBuffers;
import static org.lwjgl.opengl.GL33.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL33.glDrawArrays;
import static org.lwjgl.opengl.GL33.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL33.glGenBuffers;
import static org.lwjgl.opengl.GL33.glGenVertexArrays;
import static org.lwjgl.opengl.GL33.glUniform1f;
import static org.lwjgl.opengl.GL33.glUniform1i;
import static org.lwjgl.opengl.GL33.glUniform3f;
import static org.lwjgl.opengl.GL33.glVertexAttribPointer;

/**
 * Draws the scene background, behind all geometry.
 *
 * <p>Dispatches on the background type, each with its own shader: a solid colour
 * (drawn as a checkerboard when transparent), a vertical gradient, a cubemap
 * skybox, or an equirectangular HDRI. Skybox and HDRI are drawn at infinite
 * distance so they stay put as the camera moves.</p>
 */
public class BackgroundPass implements RenderPass {

	private final GLShader gradientShader;
	private final int gradientVao;
	private final int gradientVbo;
	private final GLShader imageShader;
	private final GLShader skyboxShader;
	private final int skyboxVao;
	private final int skyboxVbo;
	private final GLShader hdriShader;
	private final GLShader checkerboardShader;
	private final TextureBinder textureStateManager;
	private final Matrix3f skyboxViewRotation = new Matrix3f();
	private final Matrix4f skyboxViewMatrix = new Matrix4f();
	private final int gradientTopColorLocation;
	private final int gradientBottomColorLocation;
	private final int imageSamplerLocation;
	private final int skyboxViewLocation;
	private final int skyboxProjectionLocation;
	private final int skyboxSamplerLocation;
	private final int hdriViewLocation;
	private final int hdriProjectionLocation;
	private final int hdriExposureLocation;
	private final int hdriSamplerLocation;
	private final int checkerboardScaleLocation;

	/**
	 * Creates a new background pass with all supported rendering techniques.
	 * 
	 * Initializes shaders for gradient, skybox, HDRI, and checkerboard backgrounds,
	 * and creates the necessary vertex array objects for rendering full-screen quads
	 * and skybox cubes.
	 * 
	 * @param textureStateManager Texture state manager for optimized texture binding
	 * @throws ShaderException If shader compilation fails
	 */
	public BackgroundPass(TextureBinder textureStateManager) {
		gradientShader = new GLShader("/shaders/background/gradient_vertex.glsl", "/shaders/background/gradient_fragment.glsl");
		imageShader = new GLShader("/shaders/background/image_vertex.glsl", "/shaders/background/image_fragment.glsl");
		skyboxShader = new GLShader("/shaders/background/skybox_vertex.glsl", "/shaders/background/skybox_fragment.glsl");
		hdriShader = new GLShader("/shaders/background/hdri_vertex.glsl", "/shaders/background/hdri_fragment.glsl");
		checkerboardShader = new GLShader("/shaders/background/checkerboard_vertex.glsl", "/shaders/background/checkerboard_fragment.glsl");
		this.textureStateManager = textureStateManager;
		gradientTopColorLocation = gradientShader.getUniformLocation("topColor");
		gradientBottomColorLocation = gradientShader.getUniformLocation("bottomColor");
		imageSamplerLocation = imageShader.getUniformLocation("backgroundImage");
		skyboxViewLocation = skyboxShader.getUniformLocation("view");
		skyboxProjectionLocation = skyboxShader.getUniformLocation("projection");
		skyboxSamplerLocation = skyboxShader.getUniformLocation("skybox");
		hdriViewLocation = hdriShader.getUniformLocation("view");
		hdriProjectionLocation = hdriShader.getUniformLocation("projection");
		hdriExposureLocation = hdriShader.getUniformLocation("exposure");
		hdriSamplerLocation = hdriShader.getUniformLocation("equirectangularMap");
		checkerboardScaleLocation = checkerboardShader.getUniformLocation("scale");

		// Gradient background VAO
		float[] gradientQuadVertices = { -1.0f,  1.0f, -1.0f, -1.0f, 1.0f,  1.0f, 1.0f, -1.0f };
		FloatBuffer quadVboBuffer = MemoryUtil.memAllocFloat(gradientQuadVertices.length).put(gradientQuadVertices).flip();
		gradientVao = glGenVertexArrays();
		gradientVbo = glGenBuffers();
		glBindVertexArray(gradientVao);
		glBindBuffer(GL_ARRAY_BUFFER, gradientVbo);
		glBufferData(GL_ARRAY_BUFFER, quadVboBuffer, GL_STATIC_DRAW);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
		glBindVertexArray(0);
		MemoryUtil.memFree(quadVboBuffer);

		// Skybox cube VAO
		float[] skyboxVertices = {
				// positions
				-1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
				1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f,

				-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f,
				-1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f,

				1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
				1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f,

				-1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
				1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f,

				-1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f,
				1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f,

				-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f,
				1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f
		};
		skyboxVao = glGenVertexArrays();
		skyboxVbo = glGenBuffers();
		glBindVertexArray(skyboxVao);
		glBindBuffer(GL_ARRAY_BUFFER, skyboxVbo);
		glBufferData(GL_ARRAY_BUFFER, skyboxVertices, GL_STATIC_DRAW);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * 4, 0);
		glBindVertexArray(0);
	}

	@Override
	public void render(SceneView scene, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
		renderBackground(scene, scene.getBackground());
	}

	/**
	 * Renders the appropriate background based on its type.
	 * 
	 * Automatically detects the background type and dispatches to the appropriate
	 * rendering method. Handles depth buffer state and texture binding for each
	 * background technique.
	 * 
	 * @param scene The scene containing camera and other rendering context
	 * @param background The background object to render
	 */
	private void renderBackground(SceneView scene, Background background) {
		if (background instanceof SolidColorBackground solidBackground) {
			Vector4f color = solidBackground.getColor();
			// If background has transparency, blend it over a checkerboard
			if (color.w < 1.0f) {
				// Clear depth first so geometry can render in front
				glClear(GL_DEPTH_BUFFER_BIT);
				glDisable(GL_DEPTH_TEST);
				checkerboardShader.use();
				checkerboardShader.setUniformVector4f("bgColor", color);
				glUniform1f(checkerboardScaleLocation, 20.0f);
				glBindVertexArray(gradientVao);
				glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
				glBindVertexArray(0);
				glEnable(GL_DEPTH_TEST);
			} else {
				glClearColor(color.x, color.y, color.z, 1.0f);
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			}
		} else if (background instanceof GradientBackground gradientBackground) {
			// Clear depth first so geometry can render in front
			glClear(GL_DEPTH_BUFFER_BIT);
			glDisable(GL_DEPTH_TEST);
			gradientShader.use();
			Vector3f topColor = gradientBackground.getTopColor();
			Vector3f bottomColor = gradientBackground.getBottomColor();
			glUniform3f(gradientTopColorLocation, topColor.x, topColor.y, topColor.z);
			glUniform3f(gradientBottomColorLocation, bottomColor.x, bottomColor.y, bottomColor.z);
			glBindVertexArray(gradientVao);
			glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
			glBindVertexArray(0);
			glEnable(GL_DEPTH_TEST);
			glDepthMask(true); // Ensure depth writes are enabled for geometry
			// Reset texture unit to 0 for geometry pass
			glActiveTexture(GL_TEXTURE0);
		} else if (background instanceof ImageBackground imageBackground) {
			glClear(GL_DEPTH_BUFFER_BIT);
			glDisable(GL_DEPTH_TEST);
			imageShader.use();
			textureStateManager.bindTexture(0, GL_TEXTURE_2D, imageBackground.getTexture().getId());
			glUniform1i(imageSamplerLocation, 0);
			glBindVertexArray(gradientVao);
			glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
			glBindVertexArray(0);
			glEnable(GL_DEPTH_TEST);
			glDepthMask(true);
			glActiveTexture(GL_TEXTURE0);
		} else if (background instanceof SkyboxBackground || background instanceof HDRIBackground) {
			// Clear depth first so geometry can render in front
			glClear(GL_DEPTH_BUFFER_BIT);
			glDepthFunc(GL_LEQUAL);
			skyboxViewRotation.set(scene.getCamera().getViewMatrix());
			skyboxViewMatrix.set(skyboxViewRotation);
			if (background instanceof SkyboxBackground skybox) {
				skyboxShader.use();
				skyboxShader.setUniformMatrix4f(skyboxViewLocation, skyboxViewMatrix);
				skyboxShader.setUniformMatrix4f(skyboxProjectionLocation, scene.getCamera().getProjectionMatrix());
				textureStateManager.bindTexture(0, GL_TEXTURE_CUBE_MAP, skybox.getCubemapTexture().getId());
				glUniform1i(skyboxSamplerLocation, 0);
				glBindVertexArray(skyboxVao);
				glDrawArrays(GL_TRIANGLES, 0, 36);
			} else {
				HDRIBackground hdri = (HDRIBackground) background;
				hdriShader.use();
				hdriShader.setUniformMatrix4f(hdriViewLocation, skyboxViewMatrix);
				hdriShader.setUniformMatrix4f(hdriProjectionLocation, scene.getCamera().getProjectionMatrix());
				glUniform1f(hdriExposureLocation, 1.0f);
				textureStateManager.bindTexture(0, GL_TEXTURE_2D, hdri.getHdriTexture().getId());
				glUniform1i(hdriSamplerLocation, 0);
				glBindVertexArray(skyboxVao);
				glDrawArrays(GL_TRIANGLES, 0, 36);
			}
			glBindVertexArray(0);
			glDepthFunc(GL_LESS);
			glDepthMask(true); // Ensure depth writes are enabled for geometry
			// Reset texture unit to 0 for geometry pass
			glActiveTexture(GL_TEXTURE0);
		}
	}

	@Override
	public void resize(int width, int height) {
		// Nothing to do
	}

	@Override
	public void cleanup() {
		gradientShader.cleanup();
		imageShader.cleanup();
		skyboxShader.cleanup();
		hdriShader.cleanup();
		checkerboardShader.cleanup();
		glDeleteVertexArrays(gradientVao);
		glDeleteBuffers(gradientVbo);
		glDeleteVertexArrays(skyboxVao);
		glDeleteBuffers(skyboxVbo);
	}
}
