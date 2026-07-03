package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.swing.gui.figure3d.core.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.rendering.GLShader;
import info.openrocket.swing.gui.figure3d.rendering.GLShader;
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
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE_MODE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_POLYGON_OFFSET_FILL;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_VIEWPORT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGetIntegerv;
import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL11.glIsEnabled;
import static org.lwjgl.opengl.GL11.glPolygonOffset;
import static org.lwjgl.opengl.GL11.glViewport;
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
import static org.lwjgl.opengl.GL33.GL_CLAMP_TO_BORDER;

/**
 * Depth-only shadow map generation pass.
 *
 * This implementation fits the light frustum to the actual shadow casters in the
 * scene and snaps the orthographic projection to the shadow-map texel grid. The
 * result is more stable and more detailed shadows without increasing the shadow
 * sampling cost in the main lighting shader.
 */
public class ShadowPass implements RenderPass {

    private static final int MIN_SHADOW_MAP_SIZE = 1024;
    private static final int MAX_SHADOW_MAP_SIZE = 4096;
    private static final float MIN_LIGHT_DISTANCE = 28.0f;
    private static final float LIGHT_DISTANCE_MARGIN = 18.0f;
    private static final float MIN_SHADOW_WORLD_SIZE = 6.0f;
    private static final float XY_PADDING_RATIO = 0.15f;
    private static final float MIN_XY_PADDING = 1.5f;
    private static final float Z_PADDING_RATIO = 0.35f;
    private static final float MIN_Z_PADDING = 6.0f;
    private static final float MIN_NEAR_PLANE = 0.5f;
    private static final float MIN_DEPTH_RANGE = 24.0f;
    private static final float SHADOW_STRENGTH = 0.68f;
    private static final long HASH_OFFSET = 0xcbf29ce484222325L;
    private static final long HASH_PRIME = 0x100000001b3L;

    private final GLShader depthShader;
    private final Matrix4f lightSpaceMatrix = new Matrix4f();
    private final Matrix4f lightViewMatrix = new Matrix4f();
    private final Matrix4f lightProjectionMatrix = new Matrix4f();
    private final Matrix4f scratchTransformMatrix = new Matrix4f();
    private final Vector3f activeLightDirection = new Vector3f();
    private final Vector3f scratchWorldBoundsMin = new Vector3f();
    private final Vector3f scratchWorldBoundsMax = new Vector3f();
    private final Vector3f scratchLightBoundsMin = new Vector3f();
    private final Vector3f scratchLightBoundsMax = new Vector3f();
    private final Vector3f scratchBoundsMin = new Vector3f();
    private final Vector3f scratchBoundsMax = new Vector3f();
    private final Vector3f scratchCorner = new Vector3f();
    private final Vector3f scratchCenter = new Vector3f();
    private final Vector3f scratchDimensions = new Vector3f();
    private final Vector3f scratchLightPosition = new Vector3f();
    private final Vector3f scratchLightDirection = new Vector3f();
    private final Vector3f scratchUp = new Vector3f();
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
    private boolean shadowMapDirty = true;
    private long lastShadowSignature = Long.MIN_VALUE;

    public ShadowPass(int initialWidth, int initialHeight) {
        this.depthShader = new GLShader("/shaders/shadow_vertex.glsl", "/shaders/shadow_fragment.glsl");
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

        activeLightDirection.set(selection.direction());
        shadowCastingLightIndex = selection.index();
        long shadowSignature = computeShadowSignature(scene, selection);
        if (hasValidShadow && !shadowMapDirty && shadowSignature == lastShadowSignature) {
            return;
        }

        if (!buildLightSpaceMatrix(scene, selection.direction())) {
            resetShadowState();
            return;
        }

        hasValidShadow = true;

        boolean cullWasEnabled = glIsEnabled(GL_CULL_FACE);
        int previousCullFaceMode = glGetInteger(GL_CULL_FACE_MODE);
        int previousViewportX;
        int previousViewportY;
        int previousViewportWidth;
        int previousViewportHeight;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            java.nio.IntBuffer previousViewport = stack.mallocInt(4);
            glGetIntegerv(GL_VIEWPORT, previousViewport);
            previousViewportX = previousViewport.get(0);
            previousViewportY = previousViewport.get(1);
            previousViewportWidth = previousViewport.get(2);
            previousViewportHeight = previousViewport.get(3);
        }
        glViewport(0, 0, shadowMapSize, shadowMapSize);
        glBindFramebuffer(GL_FRAMEBUFFER, depthMapFbo);
        glClear(GL_DEPTH_BUFFER_BIT);
        if (!cullWasEnabled) {
            glEnable(GL_CULL_FACE);
        }
        glEnable(GL_POLYGON_OFFSET_FILL);
        glPolygonOffset(2.0f, 4.0f);
        glCullFace(GL_BACK);

        depthShader.use();
        depthShader.setUniformMatrix4f("lightSpaceMatrix", lightSpaceMatrix);

        for (SceneObject object : scene.getObjects()) {
            if (shouldSkipObject(object)) {
                continue;
            }
            depthShader.setUniformMatrix4f("model", object.getModelMatrix());
            object.getRenderableMesh().render();
        }

        glDisable(GL_POLYGON_OFFSET_FILL);
        if (!cullWasEnabled) {
            glDisable(GL_CULL_FACE);
        } else if (previousCullFaceMode != GL_BACK) {
            glCullFace(previousCullFaceMode);
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(previousViewportX, previousViewportY, previousViewportWidth, previousViewportHeight);
        lastShadowSignature = shadowSignature;
        shadowMapDirty = false;
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

    private boolean buildLightSpaceMatrix(SceneView scene, Vector3f lightDirection) {
        if (!computeShadowCasterBounds(scene, null, scratchWorldBoundsMin, scratchWorldBoundsMax)) {
            return false;
        }

        scratchCenter.set(scratchWorldBoundsMin).add(scratchWorldBoundsMax).mul(0.5f);
        scratchDimensions.set(scratchWorldBoundsMax).sub(scratchWorldBoundsMin);
        float worldRadius = scratchDimensions.length() * 0.5f;
        float lightDistance = Math.max(MIN_LIGHT_DISTANCE, worldRadius + LIGHT_DISTANCE_MARGIN);

        scratchLightDirection.set(lightDirection).normalize();
        scratchUp.set(0.0f, 1.0f, 0.0f);
        if (Math.abs(scratchLightDirection.dot(scratchUp)) > 0.99f) {
            scratchUp.set(0.0f, 0.0f, 1.0f);
        }

        scratchLightPosition.set(scratchLightDirection).mul(-lightDistance).add(scratchCenter);
        lightViewMatrix.identity().lookAt(scratchLightPosition, scratchCenter, scratchUp);

        if (!computeShadowCasterBounds(scene, lightViewMatrix, scratchLightBoundsMin, scratchLightBoundsMax)) {
            return false;
        }

        float spanX = Math.max(scratchLightBoundsMax.x - scratchLightBoundsMin.x, MIN_SHADOW_WORLD_SIZE);
        float spanY = Math.max(scratchLightBoundsMax.y - scratchLightBoundsMin.y, MIN_SHADOW_WORLD_SIZE);
        float spanZ = Math.max(scratchLightBoundsMax.z - scratchLightBoundsMin.z, 1.0f);

        float paddingX = Math.max(spanX * XY_PADDING_RATIO, MIN_XY_PADDING);
        float paddingY = Math.max(spanY * XY_PADDING_RATIO, MIN_XY_PADDING);
        float paddingZ = Math.max(Math.max(Math.max(spanX, spanY), spanZ) * Z_PADDING_RATIO, MIN_Z_PADDING);

        float width = spanX + paddingX * 2.0f;
        float height = spanY + paddingY * 2.0f;
        float centerX = snapProjectionCenter((scratchLightBoundsMin.x + scratchLightBoundsMax.x) * 0.5f, width);
        float centerY = snapProjectionCenter((scratchLightBoundsMin.y + scratchLightBoundsMax.y) * 0.5f, height);

        float left = centerX - width * 0.5f;
        float right = centerX + width * 0.5f;
        float bottom = centerY - height * 0.5f;
        float top = centerY + height * 0.5f;

        float nearPlane = Math.max(MIN_NEAR_PLANE, -(scratchLightBoundsMax.z + paddingZ));
        float farPlane = Math.max(nearPlane + MIN_DEPTH_RANGE, -(scratchLightBoundsMin.z - paddingZ));

        lightProjectionMatrix.identity().ortho(left, right, bottom, top, nearPlane, farPlane);
        lightProjectionMatrix.mul(lightViewMatrix, lightSpaceMatrix);
        return true;
    }

    private boolean computeShadowCasterBounds(SceneView scene, Matrix4f spaceTransform, Vector3f minBounds, Vector3f maxBounds) {
        resetBounds(minBounds, maxBounds);
        boolean foundShadowCaster = false;

        for (SceneObject object : scene.getObjects()) {
            if (shouldSkipObject(object)) {
                continue;
            }

            Mesh mesh = object.getMesh();
            if (mesh == null) {
                continue;
            }

            if (spaceTransform == null) {
                scratchTransformMatrix.set(object.getModelMatrix());
            } else {
                scratchTransformMatrix.set(spaceTransform).mul(object.getModelMatrix());
            }

            expandBoundsWithMesh(mesh, scratchTransformMatrix, minBounds, maxBounds);
            foundShadowCaster = true;
        }

        return foundShadowCaster;
    }

    private void expandBoundsWithMesh(Mesh mesh, Matrix4f transform, Vector3f minBounds, Vector3f maxBounds) {
        mesh.getBoundsMin(scratchBoundsMin);
        mesh.getBoundsMax(scratchBoundsMax);

        for (int xIndex = 0; xIndex < 2; xIndex++) {
            float x = xIndex == 0 ? scratchBoundsMin.x : scratchBoundsMax.x;
            for (int yIndex = 0; yIndex < 2; yIndex++) {
                float y = yIndex == 0 ? scratchBoundsMin.y : scratchBoundsMax.y;
                for (int zIndex = 0; zIndex < 2; zIndex++) {
                    float z = zIndex == 0 ? scratchBoundsMin.z : scratchBoundsMax.z;
                    scratchCorner.set(x, y, z);
                    transform.transformPosition(scratchCorner);
                    minBounds.min(scratchCorner);
                    maxBounds.max(scratchCorner);
                }
            }
        }
    }

    private LightSelection selectDirectionalLight(List<Light> lights) {
        for (int index = 0; index < lights.size(); index++) {
            Light light = lights.get(index);
            if (light.getType() == Light.LightType.DIRECTIONAL) {
                return new LightSelection(index, new Vector3f(light.getDirection()));
            }
        }
        return null;
    }

    @Override
    public void resize(int width, int height) {
        lastViewportWidth = width;
        lastViewportHeight = height;
        int previousSize = shadowMapSize;
        updateShadowMapSize(width, height);
        if (previousSize != shadowMapSize) {
            initializeFramebuffer();
        }
    }

    private long computeShadowSignature(SceneView scene, LightSelection selection) {
        long hash = HASH_OFFSET;
        hash = hashInt(hash, currentQuality == null ? -1 : currentQuality.ordinal());
        hash = hashInt(hash, shadowMapSize);
        hash = hashInt(hash, selection.index());
        hash = hashVector(hash, selection.direction());

        List<SceneObject> objects = scene.getObjects();
        hash = hashInt(hash, objects.size());
        for (SceneObject object : objects) {
            hash = hashInt(hash, object.getId());
            hash = hashBoolean(hash, object.isRenderOnTop());
            hash = hashBoolean(hash, object.getAppearance().isUnlit());
            hash = hashFloat(hash, object.getAppearance().getOpacity());

            if (shouldSkipObject(object) || object.getMesh() == null) {
                hash = hashBoolean(hash, false);
                continue;
            }

            hash = hashBoolean(hash, true);
            hash = hashInt(hash, System.identityHashCode(object.getMesh()));
            hash = hashMatrix(hash, object.getModelMatrix());
        }
        return hash;
    }

    private static long hashMatrix(long hash, Matrix4f matrix) {
        hash = hashFloat(hash, matrix.m00());
        hash = hashFloat(hash, matrix.m01());
        hash = hashFloat(hash, matrix.m02());
        hash = hashFloat(hash, matrix.m03());
        hash = hashFloat(hash, matrix.m10());
        hash = hashFloat(hash, matrix.m11());
        hash = hashFloat(hash, matrix.m12());
        hash = hashFloat(hash, matrix.m13());
        hash = hashFloat(hash, matrix.m20());
        hash = hashFloat(hash, matrix.m21());
        hash = hashFloat(hash, matrix.m22());
        hash = hashFloat(hash, matrix.m23());
        hash = hashFloat(hash, matrix.m30());
        hash = hashFloat(hash, matrix.m31());
        hash = hashFloat(hash, matrix.m32());
        hash = hashFloat(hash, matrix.m33());
        return hash;
    }

    private static long hashVector(long hash, Vector3f vector) {
        hash = hashFloat(hash, vector.x);
        hash = hashFloat(hash, vector.y);
        hash = hashFloat(hash, vector.z);
        return hash;
    }

    private static long hashFloat(long hash, float value) {
        return hashInt(hash, Float.floatToIntBits(value));
    }

    private static long hashBoolean(long hash, boolean value) {
        return hashInt(hash, value ? 1 : 0);
    }

    private static long hashInt(long hash, int value) {
        hash ^= Integer.toUnsignedLong(value);
        return hash * HASH_PRIME;
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
        GL33.glTexImage2D(GL_TEXTURE_2D, 0, GL33.GL_DEPTH_COMPONENT24, shadowMapSize, shadowMapSize, 0,
                GL33.GL_DEPTH_COMPONENT, GL_FLOAT, (java.nio.ByteBuffer) null);
        GL33.glTexParameteri(GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        GL33.glTexParameteri(GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        GL33.glTexParameteri(GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        GL33.glTexParameteri(GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        GL33.glTexParameteri(GL_TEXTURE_2D, GL33.GL_TEXTURE_COMPARE_MODE, GL33.GL_COMPARE_REF_TO_TEXTURE);
        GL33.glTexParameteri(GL_TEXTURE_2D, GL33.GL_TEXTURE_COMPARE_FUNC, GL33.GL_LEQUAL);
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
        markShadowMapDirty();
    }

    private void updateShadowMapSize(int width, int height) {
        int targetSize = Math.max(width, height);
        targetSize = (int) (targetSize * resolutionScale);
        targetSize = Math.max(MIN_SHADOW_MAP_SIZE, Math.min(MAX_SHADOW_MAP_SIZE, targetSize));
        shadowMapSize = targetSize;
    }

    public void setQuality(GraphicsQualitySettings.RenderQuality quality) {
        float newScale = switch (quality) {
            case LOW -> 0.6f;
            case MEDIUM -> 1.0f;
            case HIGH -> 1.4f;
        };
        if (quality == currentQuality && resolutionScale == newScale) {
            return;
        }

        currentQuality = quality;
        resolutionScale = newScale;
        int previousSize = shadowMapSize;
        updateShadowMapSize(lastViewportWidth, lastViewportHeight);
        if (previousSize != shadowMapSize) {
            initializeFramebuffer();
        }
        markShadowMapDirty();
    }

    public void setEnabled(boolean enabled) {
        if (shadowsEnabled == enabled) {
            return;
        }
        shadowsEnabled = enabled;
        if (!enabled) {
            resetShadowState();
        } else {
            markShadowMapDirty();
        }
    }

    public void resetShadowState() {
        hasValidShadow = false;
        shadowCastingLightIndex = -1;
        activeLightDirection.zero();
        lightSpaceMatrix.identity();
        markShadowMapDirty();
    }

    private void markShadowMapDirty() {
        shadowMapDirty = true;
        lastShadowSignature = Long.MIN_VALUE;
    }

    private float snapProjectionCenter(float center, float span) {
        if (shadowMapSize <= 0 || span <= 0.0f) {
            return center;
        }

        float texelSize = span / shadowMapSize;
        return Math.round(center / texelSize) * texelSize;
    }

    private static void resetBounds(Vector3f minBounds, Vector3f maxBounds) {
        minBounds.set(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        maxBounds.set(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
    }

    private record LightSelection(int index, Vector3f direction) {
    }
}
