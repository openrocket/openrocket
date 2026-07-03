package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.swing.gui.figure3d.rendering.GLShader;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.window.WindowManager;
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
    public void render(SceneView scene, WindowManager windowManager, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
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
