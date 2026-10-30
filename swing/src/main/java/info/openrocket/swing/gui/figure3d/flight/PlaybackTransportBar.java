package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.gui.figure3d.animation.PlaybackClock;
import info.openrocket.swing.gui.util.Icons;
import info.openrocket.swing.gui.widgets.IconButton;
import info.openrocket.swing.gui.widgets.SingleRowScrollPane;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * The replay's controls, in two rows below the view plus the event list: camera mode, tracked
 * stage, zoom, pan and visibility toggles (scrolling when the window is too narrow), then the
 * transport buttons, speed and loop beside the timeline. It drives the {@link PlaybackClock}
 * directly and polls it to keep the timeline and time label current during playback; view
 * changes go to listeners. All methods run on the Swing thread.
 */
@SuppressWarnings("serial")
class PlaybackTransportBar extends JPanel {
	private static final Translator trans = Application.getTranslator();
	private static final int POLL_INTERVAL_MS = 100;
	private static final double FRAME_STEP_SECONDS = 1.0 / 60.0;
	// Separates an option checkbox from the controls before it in its row.
	private static final int OPTION_GAP = 12;
	private static final EnumSet<FlightEvent.Type> MARKER_TYPES = EnumSet.of(
			FlightEvent.Type.IGNITION,
			FlightEvent.Type.LAUNCHROD,
			FlightEvent.Type.LIFTOFF,
			FlightEvent.Type.BURNOUT,
			FlightEvent.Type.STAGE_SEPARATION,
			FlightEvent.Type.APOGEE,
			FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT,
			FlightEvent.Type.GROUND_HIT);

	private final JButton restartButton = new IconButton(Icons.PLAYBACK_RESTART);
	private final JButton previousFrameButton = new IconButton(Icons.PLAYBACK_STEP_BACK);
	private final JButton playPauseButton = new IconButton(Icons.PLAYBACK_PLAY);
	private final JButton nextFrameButton = new IconButton(Icons.PLAYBACK_STEP_FORWARD);
	private final JCheckBox loopButton = new JCheckBox(trans.get("Flight3DFrame.loop"));
	private final JComboBox<TimelineSlider.Marker> eventCombo = new JComboBox<>();
	private final JCheckBox trailButton = new JCheckBox(trans.get("Flight3DFrame.showTrail"), true);
	private final JCheckBox exhaustButton = new JCheckBox(trans.get("Flight3DFrame.showExhaust"), true);
	private final TimelineSlider scrubSlider = new TimelineSlider();
	// Slow motion down to 0.1x makes a sub-second motor burn watchable; up to 16x shortens
	// long recovery descents.
	private static final double[] SPEEDS = { 0.1, 0.25, 0.5, 1.0, 2.0, 4.0, 8.0, 16.0 };
	private final JComboBox<SpeedOption> speedCombo = new JComboBox<>(
			Arrays.stream(SPEEDS).mapToObj(SpeedOption::new).toArray(SpeedOption[]::new));
	private final JComboBox<FlightCameraMode> cameraModeCombo = new JComboBox<>(FlightCameraMode.values());
	private final JComboBox<TrackedBodyOption> trackedBodyCombo = new JComboBox<>();
	private final JButton zoomOutButton = new IconButton(Icons.ZOOM_OUT);
	private final JButton zoomInButton = new IconButton(Icons.ZOOM_IN);
	private final JButton zoomFitButton = new IconButton(Icons.ZOOM_RESET);
	private final JToggleButton panButton = new JToggleButton(Icons.PAN_VIEW);
	private final JLabel timeLabel = new JLabel(formatTime(0.0, 0.0), SwingConstants.RIGHT);
	private final Timer pollTimer = new Timer(POLL_INTERVAL_MS, e -> pollClock());
	private final JScrollPane viewControlsScroll;

	private PlaybackClock clock;
	private Consumer<FlightCameraMode> cameraModeListener;
	private IntConsumer trackedBodyListener;
	private Runnable zoomOutListener;
	private Runnable zoomInListener;
	private Runnable zoomFitListener;
	private Consumer<Boolean> panModeListener;
	private Consumer<Boolean> trailVisibilityListener;
	private Consumer<Boolean> exhaustVisibilityListener;
	private Runnable replayChangeListener;
	private boolean viewControlsEnabled;
	private boolean programmaticUpdate;
	private double rateBeforeScrub;
	private boolean updatingEvents;
	private boolean updatingTrackedBodies;

	PlaybackTransportBar() {
		setLayout(new BorderLayout(8, 4));
		setBorder(BorderFactory.createEmptyBorder(4, 6, 6, 6));

		// Rows: view controls and event navigation right below the 3D view, then the transport
		// buttons beside the timeline.
		JPanel playbackControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		restartButton.setToolTipText(trans.get("Flight3DFrame.restart.ttip"));
		restartButton.addActionListener(e -> restartPlayback());
		playbackControls.add(restartButton);
		previousFrameButton.setToolTipText(trans.get("Flight3DFrame.previousFrame.ttip"));
		previousFrameButton.addActionListener(e -> stepFrame(-1));
		playbackControls.add(previousFrameButton);
		playbackControls.add(playPauseButton);
		nextFrameButton.setToolTipText(trans.get("Flight3DFrame.nextFrame.ttip"));
		nextFrameButton.addActionListener(e -> stepFrame(1));
		playbackControls.add(nextFrameButton);
		playbackControls.add(speedCombo);
		speedCombo.setToolTipText(trans.get("Flight3DFrame.speed.ttip"));
		loopButton.setToolTipText(trans.get("Flight3DFrame.loop.ttip"));
		loopButton.addActionListener(e -> {
			if (clock != null) clock.setLooping(loopButton.isSelected());
		});
		loopButton.setBorder(BorderFactory.createEmptyBorder(0, OPTION_GAP, 0, 0));
		playbackControls.add(loopButton);
		eventCombo.setPrototypeDisplayValue(new TimelineSlider.Marker(999.99, trans.get("Flight3DFrame.events"), null));
		eventCombo.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index,
					boolean selected, boolean focused) {
				return super.getListCellRendererComponent(list,
						value == null ? trans.get("Flight3DFrame.events") : value, index, selected, focused);
			}
		});
		eventCombo.setToolTipText(trans.get("Flight3DFrame.events.ttip"));
		eventCombo.addActionListener(e -> {
			if (!updatingEvents && eventCombo.getSelectedItem() instanceof TimelineSlider.Marker marker) {
				pauseAndSeek(marker.time());
			}
		});

		JPanel viewControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
		cameraModeCombo.setSelectedItem(FlightCameraMode.OVERVIEW);
		cameraModeCombo.setToolTipText(trans.get("Flight3DFrame.cameraMode.ttip"));
		cameraModeCombo.addActionListener(e -> {
			updatePanControlAvailability();
			if (cameraModeListener != null) {
				cameraModeListener.accept((FlightCameraMode) cameraModeCombo.getSelectedItem());
			}
		});
		viewControls.add(cameraModeCombo);
		// Only multi-stage flights have more than one body to choose from.
		trackedBodyCombo.setVisible(false);
		trackedBodyCombo.setToolTipText(trans.get("Flight3DFrame.trackedBody.ttip"));
		trackedBodyCombo.addActionListener(e -> {
			if (!updatingTrackedBodies && trackedBodyListener != null
					&& trackedBodyCombo.getSelectedItem() instanceof TrackedBodyOption option) {
				trackedBodyListener.accept(option.index());
			}
		});
		viewControls.add(trackedBodyCombo);

		zoomOutButton.setToolTipText(trans.get("ScaleSelector.btn.ZoomOut.ttip"));
		zoomOutButton.addActionListener(e -> runViewAction(zoomOutListener));
		viewControls.add(zoomOutButton);
		zoomInButton.setToolTipText(trans.get("ScaleSelector.btn.ZoomIn.ttip"));
		zoomInButton.addActionListener(e -> runViewAction(zoomInListener));
		viewControls.add(zoomInButton);
		zoomFitButton.setToolTipText(trans.get("ScaleSelector.btn.ZoomFit.ttip"));
		zoomFitButton.addActionListener(e -> runViewAction(zoomFitListener));
		viewControls.add(zoomFitButton);
		panButton.setToolTipText(trans.get("Flight3DFrame.pan.ttip"));
		panButton.addActionListener(e -> {
			if (panModeListener != null) {
				panModeListener.accept(panButton.isSelected());
			}
		});
		viewControls.add(panButton);
		trailButton.addActionListener(e -> {
			if (trailVisibilityListener != null) trailVisibilityListener.accept(trailButton.isSelected());
		});
		exhaustButton.addActionListener(e -> {
			if (exhaustVisibilityListener != null) exhaustVisibilityListener.accept(exhaustButton.isSelected());
		});
		trailButton.setBorder(BorderFactory.createEmptyBorder(0, OPTION_GAP, 0, 0));
		viewControls.add(trailButton);
		viewControls.add(exhaustButton);
		// A strut rather than a border: an empty border would replace the combo box's own.
		viewControls.add(Box.createHorizontalStrut(OPTION_GAP));
		viewControls.add(eventCombo);
		JButton help = new JButton(trans.get("Flight3DFrame.controls"), Icons.HELP);
		help.setToolTipText(trans.get("Flight3DFrame.controls.ttip"));
		help.addActionListener(e -> JOptionPane.showMessageDialog(this, trans.get("Flight3DFrame.controls.ttip"),
				trans.get("Flight3DFrame.controls"), JOptionPane.INFORMATION_MESSAGE));
		JPanel viewRow = new JPanel(new BorderLayout());
		viewRow.add(viewControls, BorderLayout.CENTER);
		JPanel helpCell = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
		helpCell.add(help);
		viewRow.add(helpCell, BorderLayout.EAST);
		viewControlsScroll = new SingleRowScrollPane(viewRow, this::revalidate);
		add(viewControlsScroll, BorderLayout.NORTH);

		scrubSlider.setEnabled(false);
		scrubSlider.setListener(new TimelineSlider.Listener() {
			@Override
			public void markerClicked(TimelineSlider.Marker marker) {
				pauseAndSeek(marker.time());
			}

			@Override
			public void scrubStarted() {
				// Hold playback while scrubbing and resume at the same rate afterwards.
				rateBeforeScrub = clock.getRate();
				clock.setRate(0.0);
				updatePlaybackButton();
			}

			@Override
			public void scrubbedTo(int value) {
				seekToSliderValue();
			}

			@Override
			public void scrubEnded() {
				clock.setRate(rateBeforeScrub);
				updateFromClock();
				updatePlaybackButton();
				notifyReplayChanged();
			}
		});
		scrubSlider.addChangeListener(this::handleSliderChanged);
		JPanel timeline = new JPanel(new BorderLayout(8, 0));
		// Center the buttons vertically on the timeline track instead of pinning them to its top.
		JPanel playbackButtonsCell = new JPanel(new GridBagLayout());
		playbackButtonsCell.add(playbackControls);
		timeline.add(playbackButtonsCell, BorderLayout.WEST);
		timeline.add(scrubSlider, BorderLayout.CENTER);
		add(timeline, BorderLayout.CENTER);

		timeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
		timeLabel.setPreferredSize(new Dimension(160, timeLabel.getPreferredSize().height));
		timeline.add(timeLabel, BorderLayout.EAST);

		speedCombo.setSelectedItem(new SpeedOption(1.0));
		speedCombo.addActionListener(e -> {
			if (clock != null && clock.getRate() != 0.0) {
				clock.setRate(selectedSpeed());
			}
		});
		playPauseButton.addActionListener(e -> togglePlayback());
		updatePlaybackButton();
		setControlsEnabled(false);
	}

	void setCameraModeListener(Consumer<FlightCameraMode> listener) {
		this.cameraModeListener = listener;
	}

	/** Receives the index into {@link FlightReplayData#getFlightBodies()} of the body to track. */
	void setTrackedBodyListener(IntConsumer listener) {
		this.trackedBodyListener = listener;
	}

	JScrollPane getViewControlsScroll() {
		return viewControlsScroll;
	}

	JComboBox<?> getTrackedBodyCombo() {
		return trackedBodyCombo;
	}

	void setViewControlListeners(Runnable zoomOutListener, Runnable zoomInListener,
			Runnable zoomFitListener, Consumer<Boolean> panModeListener) {
		this.zoomOutListener = zoomOutListener;
		this.zoomInListener = zoomInListener;
		this.zoomFitListener = zoomFitListener;
		this.panModeListener = panModeListener;
	}

	JButton getZoomOutButton() {
		return zoomOutButton;
	}

	JButton getZoomInButton() {
		return zoomInButton;
	}

	JButton getZoomFitButton() {
		return zoomFitButton;
	}

	JToggleButton getPanButton() {
		return panButton;
	}

	JCheckBox getLoopButton() {
		return loopButton;
	}

	JCheckBox getTrailButton() {
		return trailButton;
	}

	JComboBox<FlightCameraMode> getCameraModeCombo() {
		return cameraModeCombo;
	}

	JButton getRestartButton() {
		return restartButton;
	}

	JButton getPreviousFrameButton() {
		return previousFrameButton;
	}

	JButton getPlayPauseButton() {
		return playPauseButton;
	}

	JButton getNextFrameButton() {
		return nextFrameButton;
	}

	JComboBox<?> getEventCombo() {
		return eventCombo;
	}

	JComboBox<?> getSpeedCombo() {
		return speedCombo;
	}

	JSlider getScrubSlider() {
		return scrubSlider;
	}

	void setReplayChangeListener(Runnable listener) {
		this.replayChangeListener = listener;
	}

	void setVisibilityListeners(Consumer<Boolean> trailListener, Consumer<Boolean> exhaustListener) {
		trailVisibilityListener = trailListener;
		exhaustVisibilityListener = exhaustListener;
	}

	void setReplay(PlaybackClock clock, FlightReplayData replayData) {
		clearReplay();
		this.clock = clock;
		List<TimelineSlider.Marker> markers = createMarkers(replayData);
		scrubSlider.setEvents(markers, clock != null ? clock.getStart() : 0.0, clock != null ? clock.getEnd() : 0.0);
		updatingEvents = true;
		try {
			eventCombo.removeAllItems();
			markers.forEach(eventCombo::addItem);
			eventCombo.setSelectedIndex(-1);
		} finally {
			updatingEvents = false;
		}
		setTrackedBodies(replayData != null ? replayData.getFlightBodies() : List.of());
		setControlsEnabled(clock != null);
		if (clock == null) {
			pollTimer.stop();
			updateTimeLabel(0.0, 0.0);
			return;
		}
		clock.setLooping(loopButton.isSelected());
		if (clock.getRate() != 0.0) clock.setRate(selectedSpeed());
		updatePlaybackButton();
		updateFromClock();
		pollTimer.start();
	}

	void clearReplay() {
		pollTimer.stop();
		scrubSlider.resetGesture();
		rateBeforeScrub = 0.0;
		if (clock != null) {
			clock.setRate(0.0);
		}
		clock = null;
		updatingEvents = true;
		try {
			eventCombo.removeAllItems();
		} finally {
			updatingEvents = false;
		}
		scrubSlider.setEvents(List.of(), 0.0, 0.0);
		scrubSlider.setValue(0);
		setTrackedBodies(List.of());
		setControlsEnabled(false);
		updatePlaybackButton();
		updateTimeLabel(0.0, 0.0);
	}

	void dispose() {
		pollTimer.stop();
		scrubSlider.dispose();
		clearReplay();
	}

	private void setControlsEnabled(boolean enabled) {
		restartButton.setEnabled(enabled);
		previousFrameButton.setEnabled(enabled);
		playPauseButton.setEnabled(enabled);
		nextFrameButton.setEnabled(enabled);
		speedCombo.setEnabled(enabled);
		loopButton.setEnabled(enabled);
		eventCombo.setEnabled(enabled && eventCombo.getItemCount() > 0);
		cameraModeCombo.setEnabled(enabled);
		trackedBodyCombo.setEnabled(enabled);
		trailButton.setEnabled(enabled);
		exhaustButton.setEnabled(enabled);
		scrubSlider.setEnabled(enabled);
		viewControlsEnabled = enabled;
		zoomOutButton.setEnabled(enabled);
		zoomInButton.setEnabled(enabled);
		zoomFitButton.setEnabled(enabled);
		if (!enabled && panButton.isSelected()) {
			panButton.setSelected(false);
			if (panModeListener != null) {
				panModeListener.accept(false);
			}
		}
		updatePanControlAvailability();
	}

	private void setTrackedBodies(List<FlightReplayData.FlightBody> bodies) {
		updatingTrackedBodies = true;
		try {
			trackedBodyCombo.removeAllItems();
			for (int i = 0; i < bodies.size(); i++) {
				trackedBodyCombo.addItem(new TrackedBodyOption(i,
						FlightMetricsPanel.stageGroupName(bodies.get(i).stages())));
			}
		} finally {
			updatingTrackedBodies = false;
		}
		boolean visible = bodies.size() > 1;
		if (trackedBodyCombo.isVisible() != visible) {
			trackedBodyCombo.setVisible(visible);
			revalidate();
		}
	}

	private void updatePanControlAvailability() {
		boolean allowed = cameraModeCombo.getSelectedItem() != FlightCameraMode.PAD;
		if (!allowed && panButton.isSelected()) {
			panButton.setSelected(false);
			if (panModeListener != null) {
				panModeListener.accept(false);
			}
		}
		panButton.setEnabled(viewControlsEnabled && allowed);
	}

	private static void runViewAction(Runnable action) {
		if (action != null) {
			action.run();
		}
	}

	private void togglePlayback() {
		if (clock == null) {
			return;
		}
		if (clock.getRate() == 0.0) {
			if (clock.getTime() >= clock.getEnd()) {
				clock.setTime(clock.getStart());
			}
			clock.setRate(selectedSpeed());
		} else {
			clock.setRate(0.0);
		}
		updatePlaybackButton();
		updateFromClock();
		notifyReplayChanged();
	}

	private void restartPlayback() {
		if (clock == null) {
			return;
		}
		clock.setRate(0.0);
		clock.setTime(clock.getStart());
		updatePlaybackButton();
		updateFromClock();
		notifyReplayChanged();
	}

	private void stepFrame(int direction) {
		if (clock == null) {
			return;
		}
		clock.setRate(0.0);
		clock.setTime(clock.getTime() + direction * FRAME_STEP_SECONDS);
		updatePlaybackButton();
		updateFromClock();
		notifyReplayChanged();
	}

	private void pauseAndSeek(double time) {
		if (clock == null) return;
		clock.setRate(0.0);
		seekToTime(time);
		updatePlaybackButton();
	}

	private void jumpToEvent(int direction) {
		if (clock == null) return;
		double time = clock.getTime();
		double target = direction < 0 ? clock.getStart() : clock.getEnd();
		for (TimelineSlider.Marker marker : scrubSlider.getMarkers()) {
			if (direction > 0 && marker.time() > time + 1e-6) {
				target = marker.time();
				break;
			}
			if (direction < 0 && marker.time() < time - 1e-6) target = marker.time();
		}
		pauseAndSeek(target);
	}

	/** Called on the EDT by the replay window, including when its heavyweight canvas has focus. */
	boolean handleReplayKey(KeyEvent event) {
		if (clock == null || scrubSlider.isScrubbing() || event.getID() != KeyEvent.KEY_PRESSED
				|| event.isAltDown() || event.isControlDown() || event.isMetaDown()) return false;
		switch (event.getKeyCode()) {
			case KeyEvent.VK_SPACE -> togglePlayback();
			case KeyEvent.VK_LEFT -> {
				if (event.isShiftDown()) jumpToEvent(-1); else stepFrame(-1);
			}
			case KeyEvent.VK_RIGHT -> {
				if (event.isShiftDown()) jumpToEvent(1); else stepFrame(1);
			}
			case KeyEvent.VK_UP -> changeSpeed(1);
			case KeyEvent.VK_DOWN -> changeSpeed(-1);
			case KeyEvent.VK_HOME -> pauseAndSeek(clock.getStart());
			case KeyEvent.VK_END -> pauseAndSeek(clock.getEnd());
			case KeyEvent.VK_F -> runViewAction(zoomFitListener);
			case KeyEvent.VK_1 -> cameraModeCombo.setSelectedItem(FlightCameraMode.OVERVIEW);
			case KeyEvent.VK_2 -> cameraModeCombo.setSelectedItem(FlightCameraMode.FOLLOW);
			case KeyEvent.VK_3 -> cameraModeCombo.setSelectedItem(FlightCameraMode.PAD);
			default -> { return false; }
		}
		return true;
	}

	private void changeSpeed(int direction) {
		int index = Math.max(0, Math.min(speedCombo.getItemCount() - 1, speedCombo.getSelectedIndex() + direction));
		speedCombo.setSelectedIndex(index);
	}

	private void pollClock() {
		if (clock == null) {
			return;
		}
		if (clock.getRate() > 0.0 && clock.getTime() >= clock.getEnd()
				&& (!clock.isLooping() || clock.getEnd() <= clock.getStart())) {
			clock.setRate(0.0);
		}
		updatePlaybackButton();
		updateFromClock();
	}

	private void handleSliderChanged(ChangeEvent event) {
		if (programmaticUpdate || clock == null) {
			return;
		}
		seekToSliderValue();
	}

	private void seekToSliderValue() {
		if (clock == null) {
			return;
		}
		seekToTime(sliderValueToTime(scrubSlider.getValue()));
	}

	private void seekToTime(double time) {
		if (clock == null) {
			return;
		}
		clock.setTime(time);
		updateFromClock();
		notifyReplayChanged();
	}

	private void notifyReplayChanged() {
		if (replayChangeListener != null) {
			replayChangeListener.run();
		}
	}

	private void updateFromClock() {
		if (clock == null) {
			return;
		}
		double time = clock.getTime();
		if (!scrubSlider.isScrubbing()) {
			programmaticUpdate = true;
			try {
				scrubSlider.setValue(timeToSliderValue(time));
			} finally {
				programmaticUpdate = false;
			}
		}
		updateTimeLabel(time, clock.getEnd());
	}

	private void updatePlaybackButton() {
		boolean playing = clock != null && clock.getRate() != 0.0;
		playPauseButton.setIcon(playing ? Icons.PLAYBACK_PAUSE : Icons.PLAYBACK_PLAY);
		String label = trans.get(playing ? "Flight3DFrame.pause" : "Flight3DFrame.play");
		playPauseButton.setToolTipText(label + " (Space)");
		playPauseButton.getAccessibleContext().setAccessibleName(label);
	}

	private void updateTimeLabel(double time, double end) {
		timeLabel.setText(formatTime(time, end));
	}

	private static String formatTime(double time, double end) {
		return String.format(trans.get("Flight3DFrame.timeFormat"), time, end);
	}

	private int timeToSliderValue(double time) {
		if (clock == null || clock.getEnd() <= clock.getStart()) {
			return 0;
		}
		double fraction = (time - clock.getStart()) / (clock.getEnd() - clock.getStart());
		return (int) Math.round(Math.max(0.0, Math.min(1.0, fraction)) * TimelineSlider.STEPS);
	}

	private double sliderValueToTime(int value) {
		if (clock == null || clock.getEnd() <= clock.getStart()) {
			return clock != null ? clock.getStart() : 0.0;
		}
		double fraction = Math.max(0.0, Math.min(1.0, value / (double) TimelineSlider.STEPS));
		return clock.getStart() + fraction * (clock.getEnd() - clock.getStart());
	}

	private double selectedSpeed() {
		Object selected = speedCombo.getSelectedItem();
		return selected instanceof SpeedOption option ? option.rate() : 1.0;
	}

	private List<TimelineSlider.Marker> createMarkers(FlightReplayData replayData) {
		if (replayData == null) {
			return List.of();
		}
		List<TimelineSlider.Marker> markers = new ArrayList<>();
		for (FlightEvent event : replayData.getAllEvents()) {
			if (MARKER_TYPES.contains(event.getType()) && Double.isFinite(event.getTime())
					&& event.getTime() >= replayData.getStartTime() && event.getTime() <= replayData.getEndTime()) {
				TimelineSlider.Marker marker = new TimelineSlider.Marker(event.getTime(), eventLabel(event), event.getType());
				// A clustered mount reports one event per motor; list the moment only once.
				if (!markers.contains(marker)) {
					markers.add(marker);
				}
			}
		}
		markers.sort(Comparator.comparingDouble(TimelineSlider.Marker::time));
		return List.copyOf(markers);
	}

	/** Names the event's component (motor mount, stage, recovery device) so multi-stage events are distinguishable. */
	static String eventLabel(FlightEvent event) {
		RocketComponent source = event.getSource();
		String type = event.getType().toString();
		if (source == null || source == RocketComponent.REMOVED || source.getName() == null
				|| source.getName().isBlank()) {
			return type;
		}
		return String.format(trans.get("Flight3DFrame.eventSourceFormat"), type, source.getName());
	}

	private record TrackedBodyOption(int index, String label) {
		@Override
		public String toString() {
			return label;
		}
	}

	private record SpeedOption(double rate) {
		@Override
		public String toString() {
			return BigDecimal.valueOf(rate).stripTrailingZeros().toPlainString() + "x";
		}
	}
}
