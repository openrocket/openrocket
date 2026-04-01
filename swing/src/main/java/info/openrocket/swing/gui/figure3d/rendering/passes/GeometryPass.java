package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.swing.gui.figure3d.materials.Appearance3D;
import info.openrocket.swing.gui.figure3d.rendering.DefaultMaterialBinder;
import info.openrocket.swing.gui.figure3d.rendering.MaterialBinder;
import info.openrocket.swing.gui.figure3d.rendering.RealisticRenderer;
import info.openrocket.swing.gui.figure3d.rendering.RenderableMesh;
import info.openrocket.swing.gui.figure3d.rendering.ShaderProgram;
import info.openrocket.swing.gui.figure3d.rendering.TextureBinder;
import info.openrocket.swing.gui.figure3d.scene.core.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.scene.properties.DisplaySettings;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import info.openrocket.swing.gui.figure3d.window.WindowManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPolygonMode;

/**
 * Primary geometry rendering pass for 3D scene objects.
 * 
 * This pass handles the core 3D geometry rendering with sophisticated material
 * shading, transparency management, and multiple rendering modes. It processes
 * all scene objects and renders them with proper depth sorting, material
 * application, and special rendering techniques as required.
 * 
 * Rendering pipeline stages:
 * 1. **Object Classification**: Separates opaque and transparent objects
 * 2. **Depth Sorting**: Back-to-front sorting for transparent objects
 * 3. **Multi-Pass Rendering**:
 *    - Opaque pass with depth writes enabled
 *    - Transparent pass with depth writes disabled
 *    - "Render on top" pass with depth testing disabled
 * 
 * Advanced features:
 * - **Material System**: Full PBR-style material rendering with textures and decals
 * - **Transparency Handling**: Proper alpha blending with depth sorting
 * - **Rendering Modes**: Support for wireframe, X-ray, and unfinished modes
 * - **Texture Management**: Efficient texture binding and parameter optimization
 * - **Performance Tracking**: Statistics collection for optimization
 * 
 * Special rendering modes:
 * - **Wireframe**: Line-based rendering for technical visualization
 * - **X-ray**: Transparent rendering for internal component visibility
 * - **Unfinished**: Texture-free rendering for simplified visualization
 * - **Render on Top**: Overlay rendering that ignores depth
 * 
 * The pass integrates with the texture state manager for optimal performance
 * and maintains detailed statistics for debugging and optimization purposes.
 */
public class GeometryPass implements RenderPass {

    private final ShaderProgram mainShader;
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
    public GeometryPass(ShaderProgram mainShader, RenderingConfiguration config,
						TextureBinder textureStateManager, RealisticRenderer.ShaderUniforms mainShaderUniforms,
						RealisticRenderer.RenderStats renderStats) {
        this.mainShader = mainShader;
        this.config = config;
        this.textureStateManager = textureStateManager;
        this.mainShaderUniforms = mainShaderUniforms;
        this.renderStats = renderStats;
    }

    @Override
    public void render(SceneView scene, WindowManager windowManager, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
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
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

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
        renderObject(obj, false, null);
    }

    private void renderTransparentObject(SceneObject obj, Matrix4f viewMatrix) {
        renderObject(obj, true, viewMatrix);
    }

    private void renderObject(SceneObject obj, boolean sortTriangles, Matrix4f viewMatrix) {
        boolean isWireframe = isWireframe(obj);
        materialBinder.bind(obj, mainShader, mainShaderUniforms, config, textureStateManager);

        if (isWireframe) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            renderStats.stateChanges++;
        }

        if (sortTriangles && obj.getRenderableMesh() instanceof RenderableMesh renderableMesh) {
            renderableMesh.renderSorted(obj.getModelMatrix(), viewMatrix);
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

    @Override
    public void resize(int width, int height) {
        // Handled by renderer
    }

    @Override
    public void cleanup() {
        // Shaders are managed by the RealisticRenderer and cleaned up there
    }

    private static boolean isFigureTransparentComponent(RocketComponent component) {
        if (component instanceof BodyTube) {
            return true;
        }
        return component instanceof Transition;
    }
}
