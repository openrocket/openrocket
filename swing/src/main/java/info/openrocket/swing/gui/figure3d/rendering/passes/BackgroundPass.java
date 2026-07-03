package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.swing.gui.figure3d.rendering.GLShader;
import info.openrocket.swing.gui.figure3d.rendering.GLShader;
import info.openrocket.swing.gui.figure3d.rendering.TextureBinder;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.Background;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.GradientBackground;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.HDRIBackground;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.ImageBackground;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.SkyboxBackground;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.SolidColorBackground;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.window.WindowManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL33;
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

/**
 * Multi-technique background rendering pass for diverse environmental effects.
 * 
 * This render pass provides comprehensive background rendering capabilities,
 * supporting multiple background types with specialized rendering techniques
 * for each. The pass automatically detects the background type and applies
 * the appropriate rendering method to create the desired environmental effect.
 * 
 * Supported background techniques:
 * 
 * 1. **Solid Color Backgrounds**:
 *    - Simple color clearing with optional transparency
 *    - Checkerboard pattern for transparent backgrounds
 *    - Optimal performance for simple use cases
 * 
 * 2. **Gradient Backgrounds**:
 *    - Smooth vertical color transitions using interpolated quads
 *    - Perfect for atmospheric and horizon effects
 *    - Linear color space rendering for accurate blending
 * 
 * 3. **Skybox Backgrounds**:
 *    - Cubemap-based 360-degree environments
 *    - Rendered at infinite distance with depth buffer optimization
 *    - Seamless cube face transitions
 * 
 * 4. **HDRI Backgrounds**:
 *    - High Dynamic Range equirectangular environment maps
 *    - Realistic lighting and environmental reflections
 *    - Automatic exposure control and tone mapping
 * 
 * Technical implementation:
 * - Specialized shaders for each background type
 * - Depth buffer management for proper geometry integration
 * - Texture state management for optimal performance
 * - Automatic viewport-sized geometry generation
 * 
 * The pass ensures backgrounds are rendered behind all geometry while
 * maintaining proper depth testing for scene objects.
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

        // Gradient background VAO
        float[] gradientQuadVertices = { -1.0f,  1.0f, -1.0f, -1.0f, 1.0f,  1.0f, 1.0f, -1.0f };
        FloatBuffer quadVboBuffer = MemoryUtil.memAllocFloat(gradientQuadVertices.length).put(gradientQuadVertices).flip();
        gradientVao = GL33.glGenVertexArrays();
        gradientVbo = GL33.glGenBuffers();
        GL33.glBindVertexArray(gradientVao);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, gradientVbo);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, quadVboBuffer, GL33.GL_STATIC_DRAW);
        GL33.glEnableVertexAttribArray(0);
        GL33.glVertexAttribPointer(0, 2, GL33.GL_FLOAT, false, 0, 0);
        GL33.glBindVertexArray(0);
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
        skyboxVao = GL33.glGenVertexArrays();
        skyboxVbo = GL33.glGenBuffers();
        GL33.glBindVertexArray(skyboxVao);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, skyboxVbo);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, skyboxVertices, GL33.GL_STATIC_DRAW);
        GL33.glEnableVertexAttribArray(0);
        GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 3 * 4, 0);
        GL33.glBindVertexArray(0);
    }

    @Override
    public void render(SceneView scene, WindowManager windowManager, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
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
                GL33.glUniform1f(checkerboardShader.getUniformLocation("scale"), 20.0f);
                GL33.glBindVertexArray(gradientVao);
                GL33.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
                GL33.glBindVertexArray(0);
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
            GL33.glUniform3f(gradientShader.getUniformLocation("topColor"), topColor.x, topColor.y, topColor.z);
            GL33.glUniform3f(gradientShader.getUniformLocation("bottomColor"), bottomColor.x, bottomColor.y, bottomColor.z);
            GL33.glBindVertexArray(gradientVao);
            GL33.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
            GL33.glBindVertexArray(0);
            glEnable(GL_DEPTH_TEST);
            glDepthMask(true); // Ensure depth writes are enabled for geometry
            // Reset texture unit to 0 for geometry pass
            GL33.glActiveTexture(GL33.GL_TEXTURE0);
        } else if (background instanceof ImageBackground imageBackground) {
            glClear(GL_DEPTH_BUFFER_BIT);
            glDisable(GL_DEPTH_TEST);
            imageShader.use();
            textureStateManager.bindTexture(0, GL_TEXTURE_2D, imageBackground.getTexture().getId());
            imageShader.setUniformInt("backgroundImage", 0);
            GL33.glBindVertexArray(gradientVao);
            GL33.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
            GL33.glBindVertexArray(0);
            glEnable(GL_DEPTH_TEST);
            glDepthMask(true);
            GL33.glActiveTexture(GL33.GL_TEXTURE0);
        } else if (background instanceof SkyboxBackground || background instanceof HDRIBackground) {
            // Clear depth first so geometry can render in front
            glClear(GL_DEPTH_BUFFER_BIT);
            glDepthFunc(GL_LEQUAL);
            if (background instanceof SkyboxBackground skybox) {
                skyboxShader.use();
                skyboxShader.setUniformMatrix4f("view", new Matrix4f(new org.joml.Matrix3f(scene.getCamera().getViewMatrix())));
                skyboxShader.setUniformMatrix4f("projection", scene.getCamera().getProjectionMatrix());
                textureStateManager.bindTexture(0, GL33.GL_TEXTURE_CUBE_MAP, skybox.getCubemapTexture().getId());
                skyboxShader.setUniformInt("skybox", 0);
                GL33.glBindVertexArray(skyboxVao);
                GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, 36);
            } else {
                HDRIBackground hdri = (HDRIBackground) background;
                hdriShader.use();
                hdriShader.setUniformMatrix4f("view", new Matrix4f(new org.joml.Matrix3f(scene.getCamera().getViewMatrix())));
                hdriShader.setUniformMatrix4f("projection", scene.getCamera().getProjectionMatrix());
                hdriShader.setUniformFloat("exposure", 1.0f);
                textureStateManager.bindTexture(0, GL_TEXTURE_2D, hdri.getHdriTexture().getId());
                hdriShader.setUniformInt("equirectangularMap", 0);
                GL33.glBindVertexArray(skyboxVao);
                GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, 36);
            }
            GL33.glBindVertexArray(0);
            glDepthFunc(GL_LESS);
            glDepthMask(true); // Ensure depth writes are enabled for geometry
            // Reset texture unit to 0 for geometry pass
            GL33.glActiveTexture(GL33.GL_TEXTURE0);
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
        GL33.glDeleteVertexArrays(gradientVao);
        GL33.glDeleteBuffers(gradientVbo);
        GL33.glDeleteVertexArrays(skyboxVao);
        GL33.glDeleteBuffers(skyboxVbo);
    }
}
