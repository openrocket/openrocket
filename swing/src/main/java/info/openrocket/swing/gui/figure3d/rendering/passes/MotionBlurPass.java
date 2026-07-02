package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.swing.gui.figure3d.rendering.GLShader;
import info.openrocket.swing.gui.figure3d.rendering.GLShader;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.window.WindowManager;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL33;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL21.GL_SRGB8_ALPHA8;

/**
 * Motion blur post-processing pass for dynamic visual effects.
 * 
 * This pass applies screen-space motion blur to create the visual impression of
 * rapid movement or camera motion. The effect enhances the perception of speed
 * and adds cinematic quality to rocket simulations, particularly during launch
 * sequences or high-velocity flight phases.
 * 
 * Motion blur techniques:
 * - **Velocity-based blurring**: Simulates motion trails based on movement direction
 * - **Configurable intensity**: Adjustable blur factor for different scenarios
 * - **Screen-space processing**: Operates on final rendered image for efficiency
 * - **Directional sampling**: Creates natural-looking blur streaks
 * 
 * Visual benefits:
 * - **Realism**: Mimics natural camera motion blur and retinal persistence
 * - **Speed perception**: Enhances sense of velocity and acceleration
 * - **Cinematic quality**: Adds professional visual polish to animations
 * - **Immersion**: Increases emotional impact during dynamic sequences
 * 
 * Technical implementation:
 * - Single full-screen pass with specialized blur shaders
 * - Uses previous frame motion vectors for accurate blur direction
 * - Dedicated framebuffer for output texture generation
 * - Configurable blur factor for artistic control
 * - Efficient sampling patterns for performance optimization
 * 
 * The motion blur intensity can be dynamically adjusted based on camera movement,
 * object velocity, or user preferences to achieve the desired visual effect.
 */
public class MotionBlurPass implements RenderPass, ScreenTexturePass {

    private final GLShader shader;
    private float blurFactor = 1f;
    private final Vector2f blurDirection = new Vector2f(1.0f, 0.0f);
    private final int screenQuadVAO;
    private int inputTexture;
    private int depthTexture;
    private int motionBlurFBO;
    private int motionBlurTexture;
    private int screenWidth;
    private int screenHeight;

    /**
     * Creates a new motion blur post-processing pass.
     * 
     * Initializes motion blur shaders and creates framebuffer resources for the
     * specified resolution. The blur factor defaults to 1.0 for moderate effect.
     * 
     * @param screenQuadVAO Vertex array object for full-screen quad rendering
     * @param initialWidth Initial framebuffer width in pixels
     * @param initialHeight Initial framebuffer height in pixels
     * @throws Exception If shader compilation or framebuffer creation fails
     */
    public MotionBlurPass(int screenQuadVAO, int initialWidth, int initialHeight) throws Exception {
        this.shader = new GLShader("/shaders/post/motion_blur_vertex.glsl", "/shaders/post/motion_blur_fragment.glsl");
        this.screenQuadVAO = screenQuadVAO;
        this.shader.use();
        this.shader.setUniformInt("screenTexture", 0);
        GL33.glUniform1i(this.shader.getUniformLocation("depthTexture"), 1);
        this.shader.unbind();
        resize(initialWidth, initialHeight);
    }

    /**
     * Sets the texture that will be blurred.
     * @param textureId The ID of the texture to process.
     */
    @Override
    public void setInputTexture(int textureId) {
        this.inputTexture = textureId;
    }

    @Override
    public int getOutputTexture() {
        return motionBlurTexture;
    }

    @Override
    public void render(SceneView scene, WindowManager windowManager, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, motionBlurFBO);
        GL33.glClear(GL_COLOR_BUFFER_BIT);
        glDisable(GL_DEPTH_TEST);

        shader.use();
        shader.setUniformFloat("blurFactor", blurFactor);
        GL33.glUniform2f(shader.getUniformLocation("blurDirection"), blurDirection.x, blurDirection.y);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, inputTexture);

        GL33.glActiveTexture(GL33.GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, depthTexture);

        GL33.glBindVertexArray(screenQuadVAO);
        GL33.glDrawArrays(GL_TRIANGLES, 0, 6);
        GL33.glBindVertexArray(0);

        shader.unbind();
        glActiveTexture(GL_TEXTURE0);
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
    }

    @Override
    public void resize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;

        cleanupFramebuffer();
        createFramebuffer();
    }

    private void createFramebuffer() {
        motionBlurFBO = GL33.glGenFramebuffers();
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, motionBlurFBO);

        motionBlurTexture = GL33.glGenTextures();
        GL33.glBindTexture(GL_TEXTURE_2D, motionBlurTexture);
        GL33.glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB8_ALPHA8, screenWidth, screenHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
        GL33.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        GL33.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, motionBlurTexture, 0);

        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Motion Blur Framebuffer is not complete!");
        }

        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
    }

    private void cleanupFramebuffer() {
        if (motionBlurFBO != 0) {
            GL33.glDeleteFramebuffers(motionBlurFBO);
        }
        if (motionBlurTexture != 0) {
            GL33.glDeleteTextures(motionBlurTexture);
        }
    }

    @Override
    public void cleanup() {
        shader.cleanup();
        cleanupFramebuffer();
    }

    /**
     * Sets the depth texture used to distinguish rocket pixels from background.
     * Background pixels (depth ~1.0) will not be blurred.
     *
     * @param textureId The OpenGL depth texture ID
     */
    public void setDepthTexture(int textureId) {
        this.depthTexture = textureId;
    }

    /**
     * Sets the blur direction in normalized screen space.
     * This should be the rocket's axis direction projected to screen coordinates.
     *
     * @param x The horizontal component of the blur direction
     * @param y The vertical component of the blur direction
     */
    public void setBlurDirection(float x, float y) {
        float len = (float) Math.sqrt(x * x + y * y);
        if (len > 0.0001f) {
            this.blurDirection.set(x / len, y / len);
        } else {
            this.blurDirection.set(1.0f, 0.0f);
        }
    }

    /**
     * Gets the current motion blur intensity factor.
     *
     * @return The blur factor (0.0 = no blur, higher values = more blur)
     */
    public float getBlurFactor() {
        return blurFactor;
    }

    /**
     * Sets the motion blur intensity factor.
     *
     * Controls the strength of the motion blur effect. Higher values produce
     * more pronounced blur streaks, while lower values create subtle effects.
     *
     * @param blurFactor The blur intensity (0.0 = no blur, typical range 0.0-2.0)
     */
    public void setBlurFactor(float blurFactor) {
        this.blurFactor = blurFactor;
    }
}
