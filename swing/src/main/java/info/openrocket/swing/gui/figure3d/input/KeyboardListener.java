package info.openrocket.swing.gui.figure3d.input;

/**
 * Minimal keyboard event listener abstraction for window backends.
 */
public interface KeyboardListener {
    /**
     * Process a raw key event.
     * @param key AWT {@code KeyEvent.VK_*}-style key code
     * @param action GLFW_PRESS or GLFW_RELEASE
     */
    void handleKeyEvent(int key, int action);

    /**
     * Clear all key state, e.g., when window focus is lost.
     */
    void clearAllKeyStates();
}
