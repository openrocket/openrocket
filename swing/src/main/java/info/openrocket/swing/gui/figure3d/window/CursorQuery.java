package info.openrocket.swing.gui.figure3d.window;

/**
 * Provides cursor position querying and mouse button state.
 */
public interface CursorQuery {
    /**
     * @return array [x, y] cursor position in window coordinates
     */
    double[] getCursorPosition();

    /**
     * Checks if a mouse button is currently pressed.
     */
    boolean isMouseButtonPressed(int button);
}

