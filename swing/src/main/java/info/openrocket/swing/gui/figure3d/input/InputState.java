package info.openrocket.swing.gui.figure3d.input;

import java.awt.Point;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A thread-safe, shared data container for mouse and keyboard state.
 * Input handlers (like MouseInputHandler or a Swing MouseAdapter) write to this state,
 * and the Scene3DOrchestrator consumes it once per frame.
 */
public class InputState {
	// Deltas are accumulated over a frame and reset after consumption.
	public volatile float dx, dy;
	public volatile float scrollDelta;

	// State flags
	public volatile boolean isPanning;
	public volatile boolean isShiftPressed;
	public volatile boolean isLightDragging;

	// For thread-safe click detection between UI thread (Swing) and render thread.
	public final AtomicReference<Point> clickPoint = new AtomicReference<>();

	// Add a new state for double-clicks
	public final AtomicReference<Point> doubleClickPoint = new AtomicReference<>();
}