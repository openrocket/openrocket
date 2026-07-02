package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.swing.gui.figure3d.rendering.Shader;
import info.openrocket.swing.gui.figure3d.rendering.Shader;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.window.WindowManager;
import org.joml.Matrix4f;
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
 * Fast Approximate Anti-Aliasing (FXAA) post-processing pass.
 * 
 * This pass implements NVIDIA's FXAA algorithm to provide efficient anti-aliasing
 * without the performance cost of traditional multisampling techniques. FXAA
 * operates entirely in screen space by analyzing pixel luminance patterns to
 * detect and smooth aliased edges.
 * 
 * Algorithm overview:
 * 1. **Edge Detection**: Analyzes luminance differences between neighboring pixels
 * 2. **Direction Analysis**: Determines the orientation of detected edges
 * 3. **Subpixel Sampling**: Applies weighted sampling to smooth jagged edges
 * 4. **Adaptive Quality**: Adjusts smoothing intensity based on edge characteristics
 * 
 * Benefits:
 * - **Performance**: Minimal GPU overhead compared to MSAA
 * - **Compatibility**: Works with any rendering pipeline
 * - **Quality**: Effective at reducing most aliasing artifacts
 * - **Memory**: No additional memory overhead for sample storage
 * 
 * Technical implementation:
 * - Single full-screen pass using specialized FXAA shaders
 * - Operates on final rendered image in linear color space
 * - Uses dedicated framebuffer for output texture generation
 * - Automatically handles viewport resizing for resolution independence
 * 
 * FXAA is particularly effective for:
 * - Geometric edge smoothing
 * - Texture aliasing reduction
 * - Specular highlight smoothing
 * - Sub-pixel detail preservation
 */
public class FXAAPass implements RenderPass, ScreenTexturePass {

    private final Shader shader;
    private final int screenQuadVAO;
    private int inputTexture;
    private int fxaaFBO;
    private int fxaaTexture;
    private int screenWidth;
    private int screenHeight;

    /**
     * Creates a new FXAA post-processing pass.
     * 
     * Initializes the FXAA shaders and creates framebuffer resources for the
     * specified resolution. The pass is ready to process input textures immediately.
     * 
     * @param screenQuadVAO Vertex array object for full-screen quad rendering
     * @param initialWidth Initial framebuffer width in pixels
     * @param initialHeight Initial framebuffer height in pixels
     * @throws Exception If shader compilation or framebuffer creation fails
     */
    public FXAAPass(int screenQuadVAO, int initialWidth, int initialHeight) throws Exception {
        this.shader = new Shader("/shaders/post/fxaa_vertex.glsl", "/shaders/post/fxaa_fragment.glsl");
        this.screenQuadVAO = screenQuadVAO;
        this.shader.use();
        this.shader.setUniformInt("screenTexture", 0); // Set texture unit once
        this.shader.unbind();
        resize(initialWidth, initialHeight);
    }

    @Override
    public void setInputTexture(int textureId) {
        this.inputTexture = textureId;
    }

    @Override
    public int getOutputTexture() {
        return fxaaTexture;
    }

    @Override
    public void render(SceneView scene, WindowManager windowManager, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, fxaaFBO);
        GL33.glClear(GL_COLOR_BUFFER_BIT);
        glDisable(GL_DEPTH_TEST);

        shader.use();
        shader.setUniformFloat("rt_w", (float)screenWidth);
        shader.setUniformFloat("rt_h", (float)screenHeight);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, inputTexture);

        GL33.glBindVertexArray(screenQuadVAO);
        GL33.glDrawArrays(GL_TRIANGLES, 0, 6);
        GL33.glBindVertexArray(0);

        shader.unbind();
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
    }

    @Override
    public void resize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;

        cleanupFramebuffer();
        createFramebuffer();
    }

    /**
     * Creates the FXAA output framebuffer and associated textures.
     * 
     * Sets up a complete framebuffer with RGB color attachment for storing
     * the anti-aliased output. Uses linear filtering for smooth results.
     */
    private void createFramebuffer() {
        fxaaFBO = GL33.glGenFramebuffers();
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, fxaaFBO);

        fxaaTexture = GL33.glGenTextures();
        GL33.glBindTexture(GL_TEXTURE_2D, fxaaTexture);
        GL33.glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB8_ALPHA8, screenWidth, screenHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
        GL33.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        GL33.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, fxaaTexture, 0);

        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("FXAA Framebuffer is not complete!");
        }

        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
    }

    /**
     * Releases FXAA framebuffer resources.
     * 
     * Deletes the framebuffer object and associated color texture to prevent
     * GPU memory leaks during resize operations or cleanup.
     */
    private void cleanupFramebuffer() {
        if (fxaaFBO != 0) {
            GL33.glDeleteFramebuffers(fxaaFBO);
        }
        if (fxaaTexture != 0) {
            GL33.glDeleteTextures(fxaaTexture);
        }
    }

    @Override
    public void cleanup() {
        shader.cleanup();
        cleanupFramebuffer();
    }
}
