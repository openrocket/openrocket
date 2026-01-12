package info.openrocket.swing.gui.figure3d.input;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

/**
 * Handles keyboard input with support for single-press and press-and-hold actions.
 * Distinguishes between one-time key press events and continuous key holding behavior.
 */
public class KeyboardHandler implements KeyboardListener, KeyBindings {

	private final Set<Integer> pressedKeys = ConcurrentHashMap.newKeySet();
	private final Map<Integer, Runnable> singlePressActions = new ConcurrentHashMap<>();
	private final Map<Integer, Runnable> pressAndHoldActions = new ConcurrentHashMap<>();
	private final Set<Integer> singlePressHandled = ConcurrentHashMap.newKeySet();

	/**
	 * Processes raw GLFW key events and updates internal key state.
	 * @param key the GLFW key code
	 * @param action GLFW_PRESS or GLFW_RELEASE
	 */
    @Override
    public void handleKeyEvent(int key, int action) {
		if (action == GLFW_PRESS) {
			pressedKeys.add(key);
			// Mark that this key press hasn't been handled for single-press actions yet
			singlePressHandled.remove(key);
		} else if (action == GLFW_RELEASE) {
			pressedKeys.remove(key);
			// Reset the single-press handled state when the key is released
			singlePressHandled.remove(key);
		}
	}

	/**
	 * Registers an action to execute once per key press.
	 * @param key the GLFW key code
	 * @param action the action to execute
	 */
	public void addSinglePressAction(int key, Runnable action) {
		singlePressActions.put(key, action);
	}

	/**
	 * Registers an action to execute continuously while key is held.
	 * @param key the GLFW key code
	 * @param action the action to execute repeatedly
	 */
	public void addPressAndHoldAction(int key, Runnable action) {
		pressAndHoldActions.put(key, action);
	}

	/**
	 * This method should be called once per frame in the main loop
	 * to process all registered key actions.
	 */
	public void handleQueuedEvents() {
		// Handle press-and-hold actions
		for (Map.Entry<Integer, Runnable> entry : pressAndHoldActions.entrySet()) {
			if (isKeyPressed(entry.getKey())) {
				entry.getValue().run();
			}
		}

		// Handle single-press actions
		for (Map.Entry<Integer, Runnable> entry : singlePressActions.entrySet()) {
			if (isKeyPressed(entry.getKey()) && !singlePressHandled.contains(entry.getKey())) {
				entry.getValue().run();
				singlePressHandled.add(entry.getKey());
			}
		}
	}

	public boolean isKeyPressed(int key) {
		return pressedKeys.contains(key);
	}

	/**
	 * Clears all pressed key states. Should be called when the window loses focus
	 * to prevent stale key states from external applications.
	 */
    @Override
    public void clearAllKeyStates() {
        pressedKeys.clear();
        singlePressHandled.clear();
    }
}
