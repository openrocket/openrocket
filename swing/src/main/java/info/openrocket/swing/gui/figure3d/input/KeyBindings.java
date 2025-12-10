package info.openrocket.swing.gui.figure3d.input;

/**
 * Abstraction for registering and executing keyboard-driven actions.
 */
public interface KeyBindings {
    void addSinglePressAction(int key, Runnable action);
    void addPressAndHoldAction(int key, Runnable action);
    void handleQueuedEvents();
    boolean isKeyPressed(int key);
}

