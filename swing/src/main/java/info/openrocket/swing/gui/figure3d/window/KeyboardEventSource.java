package info.openrocket.swing.gui.figure3d.window;

import info.openrocket.swing.gui.figure3d.input.KeyboardListener;

/**
 * Allows a window backend to source keyboard events to a listener.
 */
public interface KeyboardEventSource {
    void setupKeyboardCallbacks(KeyboardListener listener);
}

