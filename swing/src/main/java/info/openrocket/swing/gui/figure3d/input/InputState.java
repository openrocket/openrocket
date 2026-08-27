package info.openrocket.swing.gui.figure3d.input;

import java.awt.Point;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe handoff from AWT input listeners to the render thread.
 */
public class InputState {
	/**
	 * Thread-safe container for drag deltas to avoid allocations.
	 */
	public static final class DragDelta {
		public float dx;
		public float dy;
	}

	// Deltas are accumulated over a frame and reset after consumption.
	private float dx;
	private float dy;
	private float scrollDelta;

	// Mouse position at the time of the most recent scroll event (window coordinates).
	public volatile int scrollMouseX = -1;
	public volatile int scrollMouseY = -1;

	// State flags
	public volatile boolean isPanning;
	public volatile boolean isShiftPressed;
	public volatile boolean isLightDragging;
	/** True for exactly one frame when a new mouse drag gesture begins. */
	public volatile boolean dragJustStarted;

	// For thread-safe click detection between UI thread (Swing) and render thread.
	public final AtomicReference<Point> clickPoint = new AtomicReference<>();

	// Click locations cross from the EDT to the render thread.
	public final AtomicReference<Point> doubleClickPoint = new AtomicReference<>();

	/**
	 * Accumulates drag deltas in a thread-safe manner.
	 */
	public synchronized void addDrag(float deltaX, float deltaY) {
		dx += deltaX;
		dy += deltaY;
	}

	/**
	 * Consumes and resets the accumulated drag deltas.
	 */
	public synchronized DragDelta consumeDragDelta(DragDelta out) {
		out.dx = dx;
		out.dy = dy;
		dx = 0f;
		dy = 0f;
		return out;
	}

	/**
	 * Accumulates scroll deltas in a thread-safe manner.
	 */
	public synchronized void addScroll(float delta) {
		scrollDelta += delta;
	}

	/**
	 * Accumulates scroll deltas and records the cursor position at the time of the event.
	 */
	public synchronized void addScroll(float delta, int mouseX, int mouseY) {
		scrollDelta += delta;
		scrollMouseX = mouseX;
		scrollMouseY = mouseY;
	}

	/**
	 * Consumes and resets the accumulated scroll delta.
	 */
	public synchronized float consumeScrollDelta() {
		float delta = scrollDelta;
		scrollDelta = 0f;
		return delta;
	}
}
