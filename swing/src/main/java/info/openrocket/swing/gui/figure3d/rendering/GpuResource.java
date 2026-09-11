package info.openrocket.swing.gui.figure3d.rendering;

/**
 * A resource that owns GPU-side objects (buffers, textures, shader programs, ...)
 * and must be explicitly released.
 *
 * OpenGL resources are not garbage collected; forgetting to call {@link #cleanup()}
 * leaks GPU memory for the lifetime of the context.
 */
public interface GpuResource {
	/**
	 * Releases all GPU resources owned by this object. The object must not be
	 * used afterwards. Must be called on a thread with a current OpenGL context.
	 */
	void cleanup();
}
