package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.swing.gui.figure3d.rendering.GpuResource;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;
import org.joml.Matrix4f;

/** One stage of the rendering pipeline, with context-owned resources. */
public interface RenderPass extends GpuResource {
	/**
	 * Renders this stage to its configured target.
	 *
	 * @param scene scene data to render
	 * @param viewMatrix camera view transform
	 * @param projectionMatrix camera projection transform
	 */
	void render(SceneView scene, Matrix4f viewMatrix, Matrix4f projectionMatrix);

	/**
	 * Resizes any size-dependent resources owned by this stage.
	 *
	 * @param width framebuffer width in pixels
	 * @param height framebuffer height in pixels
	 */
	void resize(int width, int height);
}
