package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import org.joml.Matrix4f;

/**
 * Core interface for modular rendering pipeline components.
 * 
 * Render passes implement specific stages of the rendering pipeline, allowing for
 * flexible composition of rendering effects. Each pass can operate independently
 * or as part of a chain, enabling complex multi-pass rendering techniques such as
 * deferred shading, post-processing effects, and specialized rendering modes.
 * 
 * Common render pass types include:
 * - Geometry passes for rendering 3D objects
 * - Background passes for environment rendering
 * - Post-processing passes for visual effects
 * - Utility passes for specific rendering tasks
 * 
 * Passes are executed in sequence by the main renderer, with each pass potentially
 * rendering to different targets (screen, framebuffers, textures) depending on
 * the overall pipeline architecture.
 */
public interface RenderPass {
    /**
     * Executes this render pass with the given scene and camera matrices.
     * 
     * This method performs the specific rendering operations for this pass,
     * which may include geometry rendering, post-processing, or other GPU
     * operations. The pass may render to the current framebuffer or to its
     * own render targets.
     * 
     * @param scene The scene containing objects, lights, and other render data
     * @param viewMatrix The camera view transformation matrix
     * @param projectionMatrix The camera projection matrix
     */
    void render(SceneView scene, Matrix4f viewMatrix, Matrix4f projectionMatrix);

    /**
     * Handles viewport size changes by resizing render targets and resources.
     * 
     * Called when the rendering viewport dimensions change, allowing the pass
     * to update any size-dependent resources such as framebuffers, textures,
     * or other render targets to match the new dimensions.
     * 
     * @param width New viewport width in pixels
     * @param height New viewport height in pixels
     */
    void resize(int width, int height);

    /**
     * Releases all GPU resources allocated by this render pass.
     * 
     * Should free all OpenGL objects including shaders, buffers, textures,
     * and framebuffers created by this pass. Called when the pass is no
     * longer needed or during renderer shutdown.
     */
    void cleanup();
}
