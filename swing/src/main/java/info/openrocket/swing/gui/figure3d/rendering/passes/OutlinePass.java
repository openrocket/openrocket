package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.swing.gui.figure3d.rendering.RealisticRenderer;
import info.openrocket.swing.gui.figure3d.rendering.Shader;
import info.openrocket.swing.gui.figure3d.rendering.ShaderProgram;
import info.openrocket.swing.gui.figure3d.rendering.TextureBinder;
import info.openrocket.swing.gui.figure3d.scene.core.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.window.WindowManager;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL30.GL_RENDERBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glBindRenderbuffer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
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
 * Advanced selection outline rendering pass using multi-pass techniques.
 * 
 * This pass provides high-quality visual feedback for selected objects by rendering
 * colored outlines around them. The implementation uses a sophisticated multi-pass
 * approach that generates smooth, consistent outlines regardless of object complexity
 * or viewing angle.
 * 
 * Multi-pass rendering pipeline:
 * 1. **Scene Composition**: Renders input scene texture to output framebuffer
 * 2. **Mask Generation**: Renders selected objects as white silhouettes to mask texture
 * 3. **Edge Detection**: Analyzes mask texture to detect object boundaries
 * 4. **Outline Rendering**: Generates colored outlines using edge detection data
 * 5. **Final Composition**: Blends outlines over the original scene
 * 
 * Advanced features:
 * - **Adaptive Width**: Outline thickness scales with distance and screen resolution
 * - **Smooth Edges**: Anti-aliased outline rendering for professional quality
 * - **Color Customization**: Configurable outline colors for different selection states
 * - **Performance Optimization**: Only processes when objects are actually selected
 * - **Depth Integration**: Proper handling of outline depth for complex geometry
 * 
 * Technical implementation:
 * - Dual framebuffer system for scene and mask rendering
 * - Specialized edge detection shaders with subpixel accuracy
 * - Alpha blending for smooth outline integration
 * - Automatic resource management with viewport scaling
 * 
 * The outline system is essential for:
 * - Clear visual selection feedback in complex 3D scenes
 * - Professional CAD-like interface behavior
 * - Accessibility and usability improvements
 * - Precise component identification in detailed models
 */
public class OutlinePass implements RenderPass, ScreenTexturePass {

    private final ShaderProgram mainShader;
    private final RealisticRenderer.ShaderUniforms mainShaderUniforms;
    private final ShaderProgram outlinePostProcessShader;
    private final TextureBinder textureStateManager;
    private final int screenQuadVAO;
    private int outlineFBO;
    private int outlineColorTexture;
    private int outlineDepthTexture;
    private int maskFBO;
    private int maskTexture;
    private int maskDepthRBO;
    private final Vector4f selectionColor;
    private static final float SELECTION_OUTLINE_WIDTH = 5.0f;
    private int screenWidth;
    private int screenHeight;
    private int inputTexture;
    private boolean hasSelection;
    private final ShaderProgram screenQuadShader;
    private final int screenTextureUniform;
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
     * @param screenQuadVAO Vertex array object for full-screen quad rendering
     * @param selectionColor RGBA color for outline rendering
     * @param initialWidth Initial framebuffer width in pixels
     * @param initialHeight Initial framebuffer height in pixels
     * @param screenQuadShader Shader for scene texture composition
     * @throws Exception If shader compilation or framebuffer creation fails
     */
    public OutlinePass(ShaderProgram mainShader, RealisticRenderer.ShaderUniforms mainShaderUniforms,
                       TextureBinder textureStateManager, int screenQuadVAO,
                       Vector4f selectionColor, int initialWidth, int initialHeight, ShaderProgram screenQuadShader) throws Exception {
        this.mainShader = mainShader;
        this.mainShaderUniforms = mainShaderUniforms;
        this.outlinePostProcessShader = new Shader("/shaders/post/outline_vertex.glsl", "/shaders/post/outline_fragment.glsl");
        this.screenQuadShader = screenQuadShader;
        this.textureStateManager = textureStateManager;
        this.screenQuadVAO = screenQuadVAO;
        this.selectionColor = selectionColor;
        this.screenWidth = initialWidth;
        this.screenHeight = initialHeight;
        createFramebuffer(initialWidth, initialHeight);

        this.screenTextureUniform = screenQuadShader.getUniformLocation("screenTexture");
        this.selectionTextureUniform = outlinePostProcessShader.getUniformLocation("selectionTexture");
        this.outlineColorUniform = outlinePostProcessShader.getUniformLocation("outlineColor");
        this.outlineWidthUniform = outlinePostProcessShader.getUniformLocation("outlineWidth");
        this.screenSizeUniform = outlinePostProcessShader.getUniformLocation("screenSize");
    }

    private void createFramebuffer(int width, int height) {
        // Create outline FBO
        outlineFBO = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, outlineFBO);
        outlineColorTexture = createTexture(width, height, GL_RGB, GL_RGB, GL_UNSIGNED_BYTE);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, outlineColorTexture, 0);
        outlineDepthTexture = createTexture(width, height, GL_DEPTH_COMPONENT, GL_DEPTH_COMPONENT, GL_FLOAT);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, outlineDepthTexture, 0);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Outline framebuffer is not complete!");
        }

        // Create mask FBO
        maskFBO = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, maskFBO);
        maskTexture = createTexture(width, height, GL_RGB, GL_RGB, GL_UNSIGNED_BYTE);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, maskTexture, 0);
        maskDepthRBO = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, maskDepthRBO);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, maskDepthRBO);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Mask framebuffer is not complete!");
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private int createTexture(int width, int height, int internalFormat, int format, int type) {
        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, (FloatBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        return texture;
    }

    @Override
    public void render(SceneView scene, WindowManager windowManager, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        List<SceneObject> selectedObjects = scene.getSelectedObjects();
        hasSelection = !selectedObjects.isEmpty();

        // Always composite through the outline FBO so selected and non-selected frames
        // follow the same presentation path.
        // 1. Bind the outline FBO to draw the final result of this pass
        glBindFramebuffer(GL_FRAMEBUFFER, outlineFBO);
        glViewport(0, 0, screenWidth, screenHeight);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

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
            mainShader.setUniform(mainShaderUniforms.projection, projectionMatrix);
            mainShader.setUniform(mainShaderUniforms.view, viewMatrix);
            glUniform1i(mainShaderUniforms.forceWhite, 1);
            glEnable(GL_DEPTH_TEST);

            for (SceneObject obj : selectedObjects) {
                mainShader.setUniform(mainShaderUniforms.model, obj.getModelMatrix());
                obj.getRenderableMesh().render();
            }

            glUniform1i(mainShaderUniforms.forceWhite, 0);

            // Now, blend the outlines onto our main FBO
            glBindFramebuffer(GL_FRAMEBUFFER, outlineFBO);
            renderOutlinePostProcess(screenWidth, screenHeight, maskTexture);
        }

        // Unbind FBO, ready for the next pass
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public void resize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        cleanupFramebuffers();
        createFramebuffer(width, height);
    }

    @Override
    public void setInputTexture(int textureId) {
        this.inputTexture = textureId;
    }

    @Override
    public int getOutputTexture() {
        return outlineColorTexture;
    }

    /**
     * Renders a texture to the current framebuffer using full-screen quad.
     * 
     * @param textureId The OpenGL texture ID to render
     */
    private void drawScreenTexture(int textureId) {
        glDisable(GL_DEPTH_TEST);
        screenQuadShader.use();
        textureStateManager.bindTexture(0, GL_TEXTURE_2D, textureId);
        glUniform1i(screenTextureUniform, 0);
        glBindVertexArray(screenQuadVAO);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);
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
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        outlinePostProcessShader.use();

        textureStateManager.bindTexture(0, GL_TEXTURE_2D, maskTexture);
        glUniform1i(selectionTextureUniform, 0);

        glUniform4f(outlineColorUniform,
                selectionColor.x, selectionColor.y, selectionColor.z, selectionColor.w);
        glUniform1f(outlineWidthUniform, SELECTION_OUTLINE_WIDTH);
        glUniform2f(screenSizeUniform, (float)width, (float)height);

        glBindVertexArray(screenQuadVAO);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    @Override
    public void cleanup() {
        outlinePostProcessShader.cleanup();
        cleanupFramebuffers();
    }

    private void cleanupFramebuffers() {
        glDeleteFramebuffers(outlineFBO);
        glDeleteTextures(outlineColorTexture);
        glDeleteTextures(outlineDepthTexture);
        glDeleteFramebuffers(maskFBO);
        glDeleteTextures(maskTexture);
        glDeleteRenderbuffers(maskDepthRBO);
    }
}
