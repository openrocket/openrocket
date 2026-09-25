package info.openrocket.swing.gui.figure3d.rendering.backgrounds;


/** A renderable scene background that owns any GPU resources it creates. */
public interface Background {
	/**
	 * Releases all GPU resources associated with this background.
	 */
	void cleanup();
}
