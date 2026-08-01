package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.swing.gui.figure3d.materials.Appearance3D;
import info.openrocket.swing.gui.figure3d.rendering.DefaultMaterialBinder;
import info.openrocket.swing.gui.figure3d.rendering.MaterialBinder;
import info.openrocket.swing.gui.figure3d.rendering.RealisticRenderer;
import info.openrocket.swing.gui.figure3d.rendering.GLRenderableMesh;
import info.openrocket.swing.gui.figure3d.rendering.GLShader;
import info.openrocket.swing.gui.figure3d.rendering.TextureBinder;
import info.openrocket.swing.gui.figure3d.scene.core.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.scene.properties.DisplaySettings;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE_MODE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL11.glIsEnabled;
import static org.lwjgl.opengl.GL11.glPolygonMode;
import static org.lwjgl.opengl.GL14.glBlendFuncSeparate;

/**
 * Renders the scene geometry with its materials.
 *
 * <p>Objects are split into opaque and transparent, the transparent ones sorted
 * back to front, and then drawn in three passes: opaque with depth writes on,
 * transparent with depth writes off, and finally the "render on top" objects with
 * depth testing off. Wireframe, X-ray and unfinished modes are handled here too.</p>
 */
public class GeometryPass implements RenderPass {

    private final GLShader mainShader;
    private final RenderingConfiguration config;
    private final TextureBinder textureStateManager;
    private final RealisticRenderer.ShaderUniforms mainShaderUniforms;
    private final RealisticRenderer.RenderStats renderStats;
    private final MaterialBinder materialBinder = new DefaultMaterialBinder();

    /**
     * Creates a new geometry pass with the specified rendering components.
     * 
     * @param mainShader The main shader program for geometry rendering
     * @param config Rendering configuration for quality and display settings
     * @param textureStateManager Manager for optimized texture state changes
     * @param mainShaderUniforms Cached uniform locations for performance
     * @param renderStats Statistics collector for performance monitoring
     */
    public GeometryPass(GLShader mainShader, RenderingConfiguration config,
						TextureBinder textureStateManager, RealisticRenderer.ShaderUniforms mainShaderUniforms,
						RealisticRenderer.RenderStats renderStats) {
        this.mainShader = mainShader;
        this.config = config;
        this.textureStateManager = textureStateManager;
        this.mainShaderUniforms = mainShaderUniforms;
        this.renderStats = renderStats;
    }

    @Override
    public void render(SceneView scene, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        mainShader.use();
        
        List<SceneObject> opaqueObjects = new ArrayList<>();
        List<SceneObject> transparentObjects = new ArrayList<>();
        for (SceneObject obj : scene.getObjects()) {
            boolean isXrayTransparent = config.getDisplay().getMode() == DisplaySettings.RenderMode.XRAY &&
                    isFigureTransparentComponent(obj.getRocketComponent());
            boolean isUnfinishedTransparent = config.getDisplay().getMode() == DisplaySettings.RenderMode.UNFINISHED &&
                    obj.getRocketComponent() instanceof BodyTube;
            if (obj.getAppearance().getOpacity() < 1.0f || isXrayTransparent || isUnfinishedTransparent) {
                transparentObjects.add(obj);
            } else {
                opaqueObjects.add(obj);
            }
        }

        // Sort transparent objects from back to front
        transparentObjects.sort((o1, o2) -> {
            Vector3f pos1 = new Vector3f();
            o1.getModelMatrix().getTranslation(pos1);
            Vector3f pos2 = new Vector3f();
            o2.getModelMatrix().getTranslation(pos2);
            float dist1 = scene.getCamera().getPosition().distanceSquared(pos1);
            float dist2 = scene.getCamera().getPosition().distanceSquared(pos2);
            return Float.compare(dist2, dist1);
        });

        renderObjects(opaqueObjects, transparentObjects, viewMatrix);
    }

    /**
     * Renders objects using a multi-pass technique for proper transparency.
     * 
     * Implements a three-pass rendering approach:
     * 1. Opaque objects with full depth testing
     * 2. Transparent objects with disabled depth writes
     * 3. "Render on top" objects with disabled depth testing
     * 
     * @param opaqueObjects List of fully opaque objects to render first
     * @param transparentObjects List of transparent objects requiring special handling
     */
    private void renderObjects(List<SceneObject> opaqueObjects, List<SceneObject> transparentObjects, Matrix4f viewMatrix) {
        glEnable(GL_BLEND);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

        // --- Opaque Pass ---
        glDepthMask(true);
        for (SceneObject obj : opaqueObjects) {
            boolean isXray = config.getDisplay().getMode() == DisplaySettings.RenderMode.XRAY &&
					isFigureTransparentComponent(obj.getRocketComponent());

            if (!obj.isRenderOnTop() && !isXray) {
                renderSingleObject(obj);
            }
        }

        // --- Transparent & Special Pass ---
        glDepthMask(false);
        for (SceneObject obj : transparentObjects) {
            boolean isXray = config.getDisplay().getMode() == DisplaySettings.RenderMode.XRAY &&
					isFigureTransparentComponent(obj.getRocketComponent());
            if (!obj.isRenderOnTop() || isXray) { // Render on top objects are also rendered here if they are transparent
                renderTransparentObject(obj, viewMatrix);
            }
        }

        // --- "On Top" Pass (for opaque objects only) ---
        glDepthMask(true);
        glDisable(GL_DEPTH_TEST);
        for (SceneObject obj : opaqueObjects) {
            if (obj.isRenderOnTop()) {
                renderSingleObject(obj);
            }
        }
        glEnable(GL_DEPTH_TEST);

        glDisable(GL_BLEND);
    }

    /**
     * Renders a single scene object with complete material and mode handling.
     * 
     * Applies all material properties, textures, and rendering mode overrides
     * for the object. Handles special cases like wireframe, X-ray, and unfinished
     * rendering modes.
     * 
     * @param obj The scene object to render with its associated mesh and materials
     */
    private void renderSingleObject(SceneObject obj) {
        renderObject(obj, false, null, false);
    }

    private void renderTransparentObject(SceneObject obj, Matrix4f viewMatrix) {
        if (shouldRenderTransparentBodyInFacePasses(obj)) {
            renderTransparentBodyObject(obj, viewMatrix);
            return;
        }
        renderObject(obj, true, viewMatrix, false);
    }

    /**
     * Transparent rocket bodies and shells look cleaner when their back-facing
     * surfaces are blended before the front-facing ones. This avoids the triangle
     * soup caused by per-triangle sorting on curved convex meshes, and hiding the
     * inner shell keeps tube wall thickness from making the whole part look too dark.
     */
    private void renderTransparentBodyObject(SceneObject obj, Matrix4f viewMatrix) {
        boolean cullWasEnabled = glIsEnabled(GL_CULL_FACE);
        int previousCullFace = glGetInteger(GL_CULL_FACE_MODE);

        if (!cullWasEnabled) {
            glEnable(GL_CULL_FACE);
            renderStats.stateChanges++;
        }

        glCullFace(GL_FRONT);
        renderStats.stateChanges++;
        renderObject(obj, false, viewMatrix, true);

        glCullFace(GL_BACK);
        renderStats.stateChanges++;
        renderObject(obj, false, viewMatrix, true);

        if (!cullWasEnabled) {
            glDisable(GL_CULL_FACE);
            renderStats.stateChanges++;
        } else if (previousCullFace != GL_BACK) {
            glCullFace(previousCullFace);
            renderStats.stateChanges++;
        }
    }

    private void renderObject(SceneObject obj, boolean sortTriangles, Matrix4f viewMatrix, boolean forceHideInnerSurfaces) {
        boolean isWireframe = isWireframe(obj);
        materialBinder.bind(obj, mainShader, mainShaderUniforms, config, textureStateManager);
        if (forceHideInnerSurfaces) {
            GL33.glUniform1i(mainShaderUniforms.hideInnerSurfaces, 1);
        }

        if (isWireframe) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            renderStats.stateChanges++;
        }

        if (sortTriangles && shouldSortTransparentTriangles(obj) &&
                obj.getRenderableMesh() instanceof GLRenderableMesh renderableMesh) {
            renderableMesh.renderSorted(obj.getMesh(), obj.getModelMatrix(), viewMatrix);
        } else {
            obj.getRenderableMesh().render();
        }
        renderStats.objectsRendered++;

        if (isWireframe) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }
    }

    private boolean isWireframe(SceneObject obj) {
        return config.getDisplay().getMode() == DisplaySettings.RenderMode.WIREFRAME ||
                config.getDisplay().getMode() == DisplaySettings.RenderMode.WIREFRAME_CULLING ||
                obj.getAppearance().getStyle() == Appearance3D.RenderStyle.WIREFRAME;
    }

    /**
     * Per-triangle sorting makes the tessellation of curved transparent rocket
     * bodies visible, especially on nose cones when they get small on screen.
     * Those components now use ordered face passes instead.
     */
    private boolean shouldSortTransparentTriangles(SceneObject obj) {
        if (isWireframe(obj)) {
            return false;
        }
        RocketComponent component = obj.getRocketComponent();
        return !isTransparentBodyComponent(component);
    }

    private boolean shouldRenderTransparentBodyInFacePasses(SceneObject obj) {
        return !isWireframe(obj) && isTransparentBodyComponent(obj.getRocketComponent());
    }

    @Override
    public void resize(int width, int height) {
        // Handled by renderer
    }

    @Override
    public void cleanup() {
        materialBinder.cleanup();
    }

    private static boolean isFigureTransparentComponent(RocketComponent component) {
        if (component instanceof BodyTube) {
            return true;
        }
        return component instanceof Transition;
    }

    private static boolean isTransparentBodyComponent(RocketComponent component) {
        if (component instanceof BodyTube) {
            return true;
        }
        return component instanceof Transition;
    }
}
