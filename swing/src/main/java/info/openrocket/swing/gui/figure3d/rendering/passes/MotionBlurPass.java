package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.swing.gui.figure3d.rendering.GLShader;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL33;

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
    private final PostProcessRenderTarget target;
    private int inputTexture;
    private int depthTexture;
    private final int blurFactorLocation;
    private final int blurDirectionLocation;
    private final int screenTextureLocation;
    private final int depthTextureLocation;

    /**
     * Creates a new motion blur post-processing pass.
     * 
     * Initializes motion blur shaders and creates framebuffer resources for the
     * specified resolution. The blur factor defaults to 1.0 for moderate effect.
     * 
     * @param screenQuadVAO Vertex array object for full-screen quad rendering
     * @param initialWidth Initial framebuffer width in pixels
     * @param initialHeight Initial framebuffer height in pixels
     * @throws ShaderException If shader compilation fails
     */
    public MotionBlurPass(int screenQuadVAO, int initialWidth, int initialHeight) {
        this.shader = new GLShader("/shaders/post/motion_blur_vertex.glsl", "/shaders/post/motion_blur_fragment.glsl");
        this.screenQuadVAO = screenQuadVAO;
        this.target = new PostProcessRenderTarget("Motion blur", initialWidth, initialHeight);
        this.blurFactorLocation = shader.getUniformLocation("blurFactor");
        this.blurDirectionLocation = shader.getUniformLocation("blurDirection");
        this.screenTextureLocation = shader.getUniformLocation("screenTexture");
        this.depthTextureLocation = shader.getUniformLocation("depthTexture");
        this.shader.use();
        GL33.glUniform1i(screenTextureLocation, 0);
        GL33.glUniform1i(depthTextureLocation, 1);
        this.shader.unbind();
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
        return target.getColorTextureId();
    }

    @Override
    public void render(SceneView scene, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        target.bind();
        glClear(GL_COLOR_BUFFER_BIT);
        glDisable(GL_DEPTH_TEST);

        shader.use();
        GL33.glUniform1f(blurFactorLocation, blurFactor);
        GL33.glUniform2f(blurDirectionLocation, blurDirection.x, blurDirection.y);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, inputTexture);

        GL33.glActiveTexture(GL33.GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, depthTexture);

        PostProcessRenderTarget.drawFullscreenQuad(screenQuadVAO);

        shader.unbind();
        glActiveTexture(GL_TEXTURE0);
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
