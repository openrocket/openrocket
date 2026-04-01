package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.swing.gui.figure3d.rendering.Shader;
import info.openrocket.swing.gui.figure3d.rendering.ShaderProgram;
import info.openrocket.swing.gui.figure3d.rendering.TextureBinder;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.scene.properties.GraphicsQualitySettings;
import info.openrocket.swing.gui.figure3d.window.WindowManager;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL33;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL21.GL_SRGB8_ALPHA8;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

/**
 * Screen-space ambient occlusion post-process using the main scene depth texture.
 */
public class AmbientOcclusionPass implements RenderPass, ScreenTexturePass {

    private final ShaderProgram shader;
    private final TextureBinder textureStateManager;
    private final GraphicsQualitySettings qualitySettings;
    private final int screenQuadVAO;
    private final Matrix4f inverseProjection = new Matrix4f();

    private final int projectionUniform;
    private final int inverseProjectionUniform;
    private final int screenSizeUniform;
    private final int radiusUniform;
    private final int strengthUniform;
    private final int biasUniform;
    private final int sampleCountUniform;
    private final int screenTextureUniform;
    private final int depthTextureUniform;

    private int inputTexture;
    private int depthTexture;
    private int outputFBO;
    private int outputTexture;
    private int screenWidth;
    private int screenHeight;

    public AmbientOcclusionPass(int screenQuadVAO, TextureBinder textureStateManager,
                                GraphicsQualitySettings qualitySettings, int initialWidth, int initialHeight) throws Exception {
        this.shader = new Shader("/shaders/post/screen_quad_vertex.glsl", "/shaders/post/ambient_occlusion_fragment.glsl");
        this.screenQuadVAO = screenQuadVAO;
        this.textureStateManager = textureStateManager;
        this.qualitySettings = qualitySettings;

        this.projectionUniform = shader.getUniformLocation("projection");
        this.inverseProjectionUniform = shader.getUniformLocation("inverseProjection");
        this.screenSizeUniform = shader.getUniformLocation("screenSize");
        this.radiusUniform = shader.getUniformLocation("radius");
        this.strengthUniform = shader.getUniformLocation("strength");
        this.biasUniform = shader.getUniformLocation("bias");
        this.sampleCountUniform = shader.getUniformLocation("sampleCount");
        this.screenTextureUniform = shader.getUniformLocation("screenTexture");
        this.depthTextureUniform = shader.getUniformLocation("depthTexture");

        shader.use();
        GL33.glUniform1i(screenTextureUniform, 0);
        GL33.glUniform1i(depthTextureUniform, 1);
        shader.unbind();

        resize(initialWidth, initialHeight);
    }

    @Override
    public void setInputTexture(int textureId) {
        this.inputTexture = textureId;
    }

    public void setDepthTexture(int textureId) {
        this.depthTexture = textureId;
    }

    @Override
    public int getOutputTexture() {
        return outputTexture;
    }

    @Override
    public void render(SceneView scene, WindowManager windowManager, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        if (inputTexture == 0 || depthTexture == 0) {
            return;
        }

        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, outputFBO);
        GL33.glClear(GL_COLOR_BUFFER_BIT);
        glDisable(GL_DEPTH_TEST);

        shader.use();
        shader.setUniform(projectionUniform, projectionMatrix);
        inverseProjection.set(projectionMatrix).invert();
        shader.setUniform(inverseProjectionUniform, inverseProjection);
        GL33.glUniform2f(screenSizeUniform, (float) screenWidth, (float) screenHeight);
        GL33.glUniform1f(radiusUniform, qualitySettings.getAmbientOcclusionRadius());
        GL33.glUniform1f(strengthUniform, qualitySettings.getAmbientOcclusionStrength());
        GL33.glUniform1f(biasUniform, qualitySettings.getAmbientOcclusionBias());
        GL33.glUniform1i(sampleCountUniform, qualitySettings.getAmbientOcclusionSampleCount());

        textureStateManager.bindTexture(0, GL_TEXTURE_2D, inputTexture);
        textureStateManager.setTextureParams(inputTexture, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, GL_LINEAR, GL_LINEAR);
        textureStateManager.bindTexture(1, GL_TEXTURE_2D, depthTexture);
        textureStateManager.setTextureParams(depthTexture, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, GL_NEAREST, GL_NEAREST);

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

    private void createFramebuffer() {
        outputFBO = GL33.glGenFramebuffers();
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, outputFBO);

        outputTexture = GL33.glGenTextures();
        GL33.glBindTexture(GL_TEXTURE_2D, outputTexture);
        GL33.glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB8_ALPHA8, screenWidth, screenHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
        GL33.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        GL33.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, outputTexture, 0);

        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Ambient occlusion framebuffer is not complete");
        }

        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
    }

    private void cleanupFramebuffer() {
        if (outputFBO != 0) {
            GL33.glDeleteFramebuffers(outputFBO);
            outputFBO = 0;
        }
        if (outputTexture != 0) {
            GL33.glDeleteTextures(outputTexture);
            outputTexture = 0;
        }
    }

    @Override
    public void cleanup() {
        shader.cleanup();
        cleanupFramebuffer();
    }
}
