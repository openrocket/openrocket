package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.startup.Application;

import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * The replay's timeline: a scrub track with a tick per flight event in the colors of the 3D
 * event markers. Clicking a tick jumps to that event; pressing elsewhere scrubs, snapping the
 * thumb to the pointer. Event tooltips show as soon as the pointer is over the track. The slider
 * reports gestures to a {@link Listener} and leaves playback to it.
 */
@SuppressWarnings("serial")
final class TimelineSlider extends JSlider {
	private static final Translator trans = Application.getTranslator();
	/** Slider positions across the replay; fine enough for sub-frame seeking on long flights. */
	static final int STEPS = 10_000;
	// Horizontal margin between the component edge and the track ends.
	private static final int TRACK_INSET = 12;
	private static final int MARKER_PICK_RADIUS = 8;

	/** A flight event on the timeline. */
	record Marker(double time, String label, FlightEvent.Type type) {
		@Override
		public String toString() {
			return String.format(trans.get("Flight3DFrame.eventTimeFormat"), label, time);
		}
	}

	/** Receives the user's gestures on the timeline. */
	interface Listener {
		void markerClicked(Marker marker);

		/** The user pressed on the track; {@link #scrubbedTo} follows. */
		void scrubStarted();

		/** The thumb moved under the user's drag to the given slider value. */
		void scrubbedTo(int value);

		void scrubEnded();
	}

	private Listener listener;
	private List<Marker> markers = List.of();
	private double rangeStart;
	private double rangeEnd;
	private Marker hoveredMarker;
	private boolean scrubbing;
	private boolean markerGesture;
	// The tooltip manager is shared application-wide, so its delay is only shortened while the
	// pointer is over the timeline and restored when it leaves.
	private int savedInitialDelay = -1;

	TimelineSlider() {
		super(0, STEPS, 0);
		setPaintTicks(false);
		setToolTipText("");
		setPreferredSize(new Dimension(300, 44));
		getAccessibleContext().setAccessibleName(trans.get("Flight3DFrame.timeline"));
		MouseAdapter hover = new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				showTooltipsInstantly(true);
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				updateMarkerHover(e.getX(), e.getY());
			}

			@Override
			public void mouseExited(MouseEvent e) {
				updateMarkerHover(-1, -1);
				showTooltipsInstantly(false);
			}
		};
		addMouseListener(hover);
		addMouseMotionListener(hover);
	}

	void setListener(Listener listener) {
		this.listener = listener;
	}

	/** Shows the given events on a timeline spanning the given playback times. */
	void setEvents(List<Marker> markers, double start, double end) {
		this.markers = markers != null ? List.copyOf(markers) : List.of();
		this.rangeStart = start;
		this.rangeEnd = end;
		hoveredMarker = null;
		repaint();
	}

	List<Marker> getMarkers() {
		return markers;
	}

	/** Whether the user is dragging the thumb, so playback must not move it. */
	boolean isScrubbing() {
		return scrubbing;
	}

	/** Forgets any gesture in progress, e.g. when the replay is closed mid-drag. */
	void resetGesture() {
		scrubbing = false;
		markerGesture = false;
	}

	/** Restores the application's tooltip delay if the pointer is still over the timeline. */
	void dispose() {
		showTooltipsInstantly(false);
	}

	@Override
	protected void processMouseEvent(MouseEvent event) {
		if (isEnabled() && listener != null && SwingUtilities.isLeftMouseButton(event)) {
			if (event.getID() == MouseEvent.MOUSE_PRESSED) {
				requestFocusInWindow();
				Marker marker = findMarkerNear(event.getX(), event.getY());
				markerGesture = marker != null;
				if (markerGesture) {
					listener.markerClicked(marker);
				} else {
					scrubbing = true;
					listener.scrubStarted();
					scrubAt(event.getX());
				}
				return;
			}
			if (event.getID() == MouseEvent.MOUSE_RELEASED) {
				if (scrubbing) {
					scrubAt(event.getX());
					scrubbing = false;
					listener.scrubEnded();
				}
				markerGesture = false;
				return;
			}
		}
		super.processMouseEvent(event);
	}

	@Override
	protected void processMouseMotionEvent(MouseEvent event) {
		if (event.getID() == MouseEvent.MOUSE_DRAGGED && (scrubbing || markerGesture)) {
			if (scrubbing) {
				scrubAt(event.getX());
			}
			return;
		}
		super.processMouseMotionEvent(event);
	}

	private void scrubAt(int x) {
		int left = getInsets().left + TRACK_INSET;
		int width = Math.max(1, getWidth() - getInsets().right - TRACK_INSET - left);
		setValue((int) Math.round((x - left) * (double) STEPS / width));
		listener.scrubbedTo(getValue());
	}

	private void showTooltipsInstantly(boolean instant) {
		ToolTipManager manager = ToolTipManager.sharedInstance();
		if (instant && savedInitialDelay < 0) {
			savedInitialDelay = manager.getInitialDelay();
			manager.setInitialDelay(0);
		} else if (!instant && savedInitialDelay >= 0) {
			manager.setInitialDelay(savedInitialDelay);
			savedInitialDelay = -1;
		}
	}

	@Override
	public String getToolTipText(MouseEvent event) {
		Marker marker = findMarkerNear(event.getX(), event.getY());
		return marker != null ? marker.toString() : null;
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		Graphics2D g2 = (Graphics2D) graphics.create();
		try {
			g2.setColor(getBackground());
			g2.fillRect(0, 0, getWidth(), getHeight());
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			int left = getInsets().left + TRACK_INSET;
			int right = Math.max(left, getWidth() - getInsets().right - TRACK_INSET);
			int trackY = getHeight() / 2 - 4;
			int thumbX = left + (int) Math.round((right - left) * getValue() / (double) STEPS);
			Color trackColor = UIManager.getColor("Slider.trackColor");
			g2.setColor(trackColor != null ? trackColor : Color.GRAY);
			g2.fillRoundRect(left, trackY - 2, right - left, 4, 4, 4);
			if (!isEnabled()) {
				return;
			}
			g2.setColor(accentColor());
			g2.fillRoundRect(left, trackY - 2, thumbX - left, 4, 4, 4);
			g2.fillOval(thumbX - 6, trackY - 6, 12, 12);
			if (hasFocus()) {
				g2.drawOval(thumbX - 9, trackY - 9, 18, 18);
			}
			if (rangeEnd <= rangeStart) {
				return;
			}
			int y = markerY();
			for (Marker marker : markers) {
				// The trajectory's event marker colors, so the ticks and the 3D markers read as
				// the same events.
				g2.setColor(FlightEventMarkers.hasColor(marker.type())
						? FlightEventMarkers.awtColorOf(marker.type()) : accentColor());
				int x = xForTime(marker.time());
				g2.drawLine(x, y - 6, x, y + 5);
				int diameter = marker == hoveredMarker ? 8 : 6;
				g2.fillOval(x - diameter / 2, y - diameter / 2, diameter, diameter);
			}
		} finally {
			g2.dispose();
		}
	}

	private Marker findMarkerNear(int x, int y) {
		if (markers.isEmpty() || rangeEnd <= rangeStart || Math.abs(markerY() - y) > MARKER_PICK_RADIUS + 1) {
			return null;
		}
		Marker nearest = null;
		int nearestDistance = Integer.MAX_VALUE;
		for (Marker marker : markers) {
			int distance = Math.abs(xForTime(marker.time()) - x);
			if (distance < nearestDistance) {
				nearest = marker;
				nearestDistance = distance;
			}
		}
		return nearestDistance <= MARKER_PICK_RADIUS ? nearest : null;
	}

	private void updateMarkerHover(int x, int y) {
		Marker marker = findMarkerNear(x, y);
		if (marker == hoveredMarker) {
			return;
		}
		hoveredMarker = marker;
		setCursor(marker != null ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
		repaint();
	}

	private int markerY() {
		return getHeight() / 2 + 8;
	}

	private int xForTime(double time) {
		Insets insets = getInsets();
		int left = insets.left + TRACK_INSET;
		int right = getWidth() - insets.right - TRACK_INSET;
		if (right <= left || rangeEnd <= rangeStart) {
			return left;
		}
		double fraction = Math.max(0.0, Math.min(1.0, (time - rangeStart) / (rangeEnd - rangeStart)));
		return left + (int) Math.round(fraction * (right - left));
	}

	private static Color accentColor() {
		Color color = UIManager.getColor("Component.accentColor");
		return color != null ? color : new Color(0xC35A00);
	}
}
