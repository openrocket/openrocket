package info.openrocket.swing.gui.figure3d.window;

import info.openrocket.swing.gui.figure3d.input.InputHandler;

/**
 * Interface for managing application windows and OpenGL contexts.
 * Provides abstraction over different windowing systems (GLFW, etc.).
 */
public interface WindowManager {
	/**
	 * Initializes the window system.
	 * @param width Window width
	 * @param height Window height
	 * @param title Window title
	 * @return Window handle
	 */
	long createWindow(int width, int height, String title);

	/**
	 * Checks if the window should close.
	 * @return true if window should close
	 */
	boolean shouldClose();

	/**
	 * Swaps the front and back buffers.
	 */
	void swapBuffers();

	/**
	 * Polls for window events.
	 */
	void pollEvents();

	/**
	 * Sets up input callbacks.
	 * @param inputHandler Handler for input events
	 */
	void setupInputCallbacks(InputHandler inputHandler);

	/**
	 * Makes the window's OpenGL context current for the calling thread.
	 */
	void makeContextCurrent();

	/**
	 * Shows the window to the user.
	 */
	void showWindow();

	/**
	 * Gets the current window size in screen coordinates.
	 *
	 * @return Array containing [width, height] in screen coordinates
	 */
	int[] getWindowSize();

	/**
	 * Gets the current window width in screen coordinates.
	 *
	 * @return The width of the window in screen coordinates.
	 */
	@Deprecated
	int getWidth();

	/**
	 * Gets the current window height in screen coordinates.
	 *
	 * @return The height of the window in screen coordinates.
	 */
	@Deprecated
	int getHeight();

	/**
	 * Sets the title of the window.
	 * @param title The new title for the window.
	 */
	void setWindowTitle(String title);

	/**
	 * Cleans up window resources.
	 */
	void cleanup();
}
