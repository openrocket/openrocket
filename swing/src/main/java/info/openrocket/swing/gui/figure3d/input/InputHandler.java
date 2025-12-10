package info.openrocket.swing.gui.figure3d.input;

/**
 * Interface for handling mouse and keyboard input events.
 * Implementations process raw input and update shared state for the 3D scene.
 */
public interface InputHandler {
	/**
	 * Handles mouse button events.
	 * @param button Mouse button that was pressed/released
	 * @param action Action type (press/release)
	 * @param mods Modifier keys
	 */
	void handleMouseButton(int button, int action, int mods);

	/**
	 * Handles mouse movement events.
	 * @param xpos X position of the cursor
	 * @param ypos Y position of the cursor
	 */
	void handleMouseMovement(double xpos, double ypos);

	/**
	 * Handles scroll wheel events.
	 * @param xoffset Horizontal scroll offset
	 * @param yoffset Vertical scroll offset
	 */
	void handleScroll(double xoffset, double yoffset);
}
