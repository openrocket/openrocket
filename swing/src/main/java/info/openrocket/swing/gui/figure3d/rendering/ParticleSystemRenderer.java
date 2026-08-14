package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.swing.gui.figure3d.scene.graph.Camera;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;

/**
 * Renders one category of particle effects as part of a full scene frame.
 *
 * This is deliberately not a sub-interface of {@link GLRenderer}: particle
 * renderers are owned and invoked by a {@link GLRenderer} implementation as
 * part of its frame, rather than driving the full scene pipeline themselves.
 */
public interface ParticleSystemRenderer extends GpuResource {

	/**
	 * Renders particle systems from the scene using this renderer.
	 *
	 * @param scene The scene containing particle emitters
	 * @param camera The camera for view/projection matrices
	 */
	void render(SceneView scene, Camera camera);
}
