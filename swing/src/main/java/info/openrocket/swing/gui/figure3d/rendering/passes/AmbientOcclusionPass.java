package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.swing.gui.figure3d.rendering.GLShader;
import info.openrocket.swing.gui.figure3d.rendering.TextureBinder;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.scene.properties.GraphicsQualitySettings;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL33;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

/**
 * Screen-space ambient occlusion post-process using the main scene depth texture.
 */
public class AmbientOcclusionPass implements RenderPass, ScreenTexturePass {

    private final GLShader shader;
    private final TextureBinder textureStateManager;
    private final GraphicsQualitySettings qualitySettings;
    private final int screenQuadVAO;
    private final PostProcessRenderTarget target;
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

    public AmbientOcclusionPass(int screenQuadVAO, TextureBinder textureStateManager,
                                GraphicsQualitySettings qualitySettings, int initialWidth, int initialHeight) {
        this.shader = new GLShader("/shaders/post/screen_quad_vertex.glsl", "/shaders/post/ambient_occlusion_fragment.glsl");
        this.screenQuadVAO = screenQuadVAO;
        this.textureStateManager = textureStateManager;
        this.qualitySettings = qualitySettings;
        this.target = new PostProcessRenderTarget("Ambient occlusion", initialWidth, initialHeight);

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
        return target.getColorTextureId();
    }

    @Override
    public void render(SceneView scene, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        if (inputTexture == 0 || depthTexture == 0) {
            return;
        }

        target.bind();
        glClear(GL_COLOR_BUFFER_BIT);
        glDisable(GL_DEPTH_TEST);

        shader.use();
        shader.setUniformMatrix4f(projectionUniform, projectionMatrix);
        inverseProjection.set(projectionMatrix).invert();
        shader.setUniformMatrix4f(inverseProjectionUniform, inverseProjection);
        GL33.glUniform2f(screenSizeUniform, (float) target.getWidth(), (float) target.getHeight());
        GL33.glUniform1f(radiusUniform, qualitySettings.getAmbientOcclusionRadius());
        GL33.glUniform1f(strengthUniform, qualitySettings.getAmbientOcclusionStrength());
        GL33.glUniform1f(biasUniform, qualitySettings.getAmbientOcclusionBias());
        GL33.glUniform1i(sampleCountUniform, qualitySettings.getAmbientOcclusionSampleCount());

        textureStateManager.bindTexture(0, GL_TEXTURE_2D, inputTexture);
        textureStateManager.setTextureParams(GL_TEXTURE_2D, inputTexture, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE,
                GL_LINEAR, GL_LINEAR);
        textureStateManager.bindTexture(1, GL_TEXTURE_2D, depthTexture);
        textureStateManager.setTextureParams(GL_TEXTURE_2D, depthTexture, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE,
                GL_NEAREST, GL_NEAREST);

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
