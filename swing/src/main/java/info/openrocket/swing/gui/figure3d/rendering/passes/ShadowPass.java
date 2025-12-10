package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.swing.gui.figure3d.rendering.Shader;
import info.openrocket.swing.gui.figure3d.rendering.ShaderProgram;
import info.openrocket.swing.gui.figure3d.scene.core.Light;
import info.openrocket.swing.gui.figure3d.scene.core.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.scene.properties.GraphicsQualitySettings;
import info.openrocket.swing.gui.figure3d.window.WindowManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL33.GL_CLAMP_TO_BORDER;
import static org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL30.GL_NONE;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glCheckFramebufferStatus;
import static org.lwjgl.opengl.GL30.glDeleteFramebuffers;
import static org.lwjgl.opengl.GL30.glDeleteTextures;
import static org.lwjgl.opengl.GL30.glDrawBuffer;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL30.glReadBuffer;

/**
 * Depth-only shadow map generation pass.
 *
 * Renders the scene from the perspective of the primary directional light to
 * a depth texture. The resulting shadow map and light-space matrix are consumed
 * by the main geometry shader to apply shadowing during the lighting pass.
 */
public class ShadowPass implements RenderPass {

    private static final int MIN_SHADOW_MAP_SIZE = 1024;
    private static final int MAX_SHADOW_MAP_SIZE = 4096;
    private static final float ORTHO_EXTENTS = 50.0f;
    private static final float LIGHT_DISTANCE = 60.0f;
    private static final float NEAR_PLANE = 1.0f;
    private static final float FAR_PLANE = 200.0f;
    private static final float SHADOW_STRENGTH = 0.7f;

    private final ShaderProgram depthShader;
    private final Matrix4f lightSpaceMatrix = new Matrix4f();
    private final Vector3f activeLightDirection = new Vector3f();
    private int depthMapFbo;
    private int depthMapTexture;
    private int shadowMapSize;
    private int lastViewportWidth;
    private int lastViewportHeight;
    private GraphicsQualitySettings.RenderQuality currentQuality = null;
    private float resolutionScale = 1.0f;
    private boolean shadowsEnabled = true;
    private int shadowCastingLightIndex = -1;
    private boolean hasValidShadow = false;

    public ShadowPass(int initialWidth, int initialHeight) throws Exception {
        this.depthShader = new Shader("/shaders/shadow_vertex.glsl", "/shaders/shadow_fragment.glsl");
        this.lastViewportWidth = initialWidth;
        this.lastViewportHeight = initialHeight;
        updateShadowMapSize(initialWidth, initialHeight);
        initializeFramebuffer();
    }

    @Override
    public void render(SceneView scene, WindowManager windowManager, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        if (!shadowsEnabled) {
            resetShadowState();
            return;
        }
        LightSelection selection = selectDirectionalLight(scene.getLightController().getLights());
        if (selection == null) {
            resetShadowState();
            return;
        }

        hasValidShadow = true;
        shadowCastingLightIndex = selection.index();
        activeLightDirection.set(selection.direction());
        buildLightSpaceMatrix(selection.direction());

        glViewport(0, 0, shadowMapSize, shadowMapSize);
        glBindFramebuffer(GL_FRAMEBUFFER, depthMapFbo);
        glClear(GL_DEPTH_BUFFER_BIT);
        glCullFace(GL33.GL_FRONT); // Reduce peter-panning

        depthShader.use();
        depthShader.setUniform("lightSpaceMatrix", lightSpaceMatrix);

        for (SceneObject object : scene.getObjects()) {
            if (shouldSkipObject(object)) {
                continue;
            }
            depthShader.setUniform("model", object.getModelMatrix());
            object.getRenderableMesh().render();
        }

        glCullFace(GL_BACK);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private boolean shouldSkipObject(SceneObject object) {
        if (object.isRenderOnTop()) {
            return true;
        }
        if (object.getAppearance().isUnlit()) {
            return true;
        }
        return object.getAppearance().getOpacity() <= 0.0f;
    }

    private void buildLightSpaceMatrix(Vector3f lightDirection) {
        Vector3f lightDirNormalized = new Vector3f(lightDirection).normalize();
        Vector3f up = new Vector3f(0, 1, 0);
        if (Math.abs(lightDirNormalized.dot(up)) > 0.99f) {
            up.set(0, 0, 1);
        }

        Vector3f lightPos = new Vector3f(lightDirNormalized).mul(-LIGHT_DISTANCE);
        Matrix4f lightView = new Matrix4f().lookAt(lightPos, new Vector3f(0, 0, 0), up);
        Matrix4f lightProjection = new Matrix4f().ortho(
                -ORTHO_EXTENTS, ORTHO_EXTENTS,
                -ORTHO_EXTENTS, ORTHO_EXTENTS,
                NEAR_PLANE, FAR_PLANE
        );
        lightProjection.mul(lightView, lightSpaceMatrix);
    }

    private LightSelection selectDirectionalLight(List<Light> lights) {
        for (int i = 0; i < lights.size(); i++) {
            Light light = lights.get(i);
            if (light.getType() == Light.LightType.DIRECTIONAL) {
                return new LightSelection(i, new Vector3f(light.getDirection()));
            }
        }
        return null;
    }

    @Override
    public void resize(int width, int height) {
        this.lastViewportWidth = width;
        this.lastViewportHeight = height;
        int previousSize = this.shadowMapSize;
        updateShadowMapSize(width, height);
        if (previousSize != this.shadowMapSize) {
            initializeFramebuffer();
        }
    }

    @Override
    public void cleanup() {
        if (depthMapFbo != 0) {
            glDeleteFramebuffers(depthMapFbo);
            depthMapFbo = 0;
        }
        if (depthMapTexture != 0) {
            glDeleteTextures(depthMapTexture);
            depthMapTexture = 0;
        }
    }

    public Matrix4f getLightSpaceMatrix() {
        return lightSpaceMatrix;
    }

    public int getDepthMapTexture() {
        return depthMapTexture;
    }

    public int getShadowCastingLightIndex() {
        return shadowCastingLightIndex;
    }

    public boolean hasShadowMap() {
        return hasValidShadow;
    }

    public float getShadowStrength() {
        return SHADOW_STRENGTH;
    }

    public Vector3f getActiveLightDirection() {
        return new Vector3f(activeLightDirection);
    }

    private void initializeFramebuffer() {
        cleanup();

        depthMapFbo = GL33.glGenFramebuffers();
        depthMapTexture = GL33.glGenTextures();
        GL33.glBindTexture(GL_TEXTURE_2D, depthMapTexture);
        GL33.glTexImage2D(GL_TEXTURE_2D, 0, GL33.GL_DEPTH_COMPONENT, shadowMapSize, shadowMapSize, 0,
                GL33.GL_DEPTH_COMPONENT, GL_FLOAT, (java.nio.ByteBuffer) null);
        GL33.glTexParameteri(GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        GL33.glTexParameteri(GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer borderColor = stack.floats(1.0f, 1.0f, 1.0f, 1.0f);
            GL33.glTexParameterfv(GL_TEXTURE_2D, GL33.GL_TEXTURE_BORDER_COLOR, borderColor);
        }

        GL33.glBindFramebuffer(GL_FRAMEBUFFER, depthMapFbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthMapTexture, 0);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Failed to create shadow map framebuffer");
        }

        GL33.glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private record LightSelection(int index, Vector3f direction) {
    }

    private void updateShadowMapSize(int width, int height) {
        int targetSize = Math.max(width, height);
        targetSize = (int) (targetSize * resolutionScale);
        targetSize = Math.max(MIN_SHADOW_MAP_SIZE, Math.min(MAX_SHADOW_MAP_SIZE, targetSize));
        this.shadowMapSize = targetSize;
    }

    public void setQuality(GraphicsQualitySettings.RenderQuality quality) {
        float newScale = switch (quality) {
            case LOW -> 0.8f;
            case MEDIUM -> 1.25f;
            case HIGH -> 2.0f;
        };
        if (quality == currentQuality && resolutionScale == newScale) {
            return;
        }
        currentQuality = quality;
        resolutionScale = newScale;
        int previousSize = this.shadowMapSize;
        updateShadowMapSize(lastViewportWidth, lastViewportHeight);
        if (previousSize != this.shadowMapSize) {
            initializeFramebuffer();
        }
    }

    public void setEnabled(boolean enabled) {
        this.shadowsEnabled = enabled;
        if (!enabled) {
            resetShadowState();
        }
    }

    public void resetShadowState() {
        hasValidShadow = false;
        shadowCastingLightIndex = -1;
        activeLightDirection.zero();
    }
}
