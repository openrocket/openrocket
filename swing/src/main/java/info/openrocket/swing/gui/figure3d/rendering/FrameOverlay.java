package info.openrocket.swing.gui.figure3d.rendering;

import org.joml.Matrix4f;

/**
 * A small screen-space overlay drawn on top of the finished frame (e.g. an orientation gizmo).
 * Invoked with the resolved frame's framebuffer already bound; implementations set their own
 * corner viewport and restore any GL state they change.
 */
public interface FrameOverlay {
	/**
	 * @param cameraViewMatrix the scene camera's view matrix (its rotation drives the gizmo)
	 * @param width            the resolved frame width in pixels
	 * @param height           the resolved frame height in pixels
	 */
	void render(Matrix4f cameraViewMatrix, int width, int height);

	/** Releases context-owned resources while the renderer's GL context is current. */
	default void cleanup() {
	}
}
