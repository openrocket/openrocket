package info.openrocket.swing.gui.figure3d.input;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles keyboard input with support for single-press and press-and-hold actions.
 * Distinguishes between one-time key press events and continuous key holding behavior.
 */
public class KeyboardHandler {
	private static final int PRESS = 1;
	private static final int RELEASE = 0;

	private final Set<Integer> pressedKeys = ConcurrentHashMap.newKeySet();
	private final Map<Integer, Runnable> singlePressActions = new ConcurrentHashMap<>();
	private final Map<Integer, Runnable> pressAndHoldActions = new ConcurrentHashMap<>();
	private final Set<Integer> singlePressHandled = ConcurrentHashMap.newKeySet();

	/**
	 * Processes key events and updates internal key state.
	 * @param key the AWT {@code KeyEvent.VK_*}-style key code
	 * @param action {@code 1} for press or {@code 0} for release
	 */
	public void handleKeyEvent(int key, int action) {
		if (action == PRESS) {
			// AWT emits repeated keyPressed events while a key is held. Only a transition
			// from released to pressed may re-arm a single-press action.
			if (pressedKeys.add(key)) {
				singlePressHandled.remove(key);
			}
		} else if (action == RELEASE) {
			pressedKeys.remove(key);
			// Reset the single-press handled state when the key is released
			singlePressHandled.remove(key);
		}
	}

	/**
	 * Registers an action to execute once per key press.
	 * @param key the AWT {@code KeyEvent.VK_*}-style key code
	 * @param action the action to execute
	 */
	public void addSinglePressAction(int key, Runnable action) {
		singlePressActions.put(key, action);
	}

	/**
	 * Registers an action to execute continuously while key is held.
	 * @param key the AWT {@code KeyEvent.VK_*}-style key code
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
	public void clearAllKeyStates() {
		pressedKeys.clear();
		singlePressHandled.clear();
	}
}
