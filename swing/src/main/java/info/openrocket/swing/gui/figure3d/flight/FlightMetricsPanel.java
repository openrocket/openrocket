package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.MathUtil;
import info.openrocket.swing.gui.figure3d.animation.PlaybackClock;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A compact telemetry strip for the flight replay: shows the simulation, its flight configuration,
 * the tracked body's altitude, velocity, acceleration and position interpolated at the current
 * playback time, and each group of stages' flight phase. Lives beside the 3D view (a Swing
 * component cannot overlay the heavyweight GL canvas).
 */
@SuppressWarnings("serial")
class FlightMetricsPanel extends JPanel {
	private static final Translator trans = Application.getTranslator();
	private static final int POLL_INTERVAL_MS = 100;
	private static final String EMPTY = "—";

	private final JLabel simulationLabel = valueLabel();
	private final JLabel configLabel = valueLabel();
	private final JLabel timeLabel = valueLabel();
	private final JLabel altitudeLabel = valueLabel();
	private final JLabel velocityLabel = valueLabel();
	private final JLabel accelerationLabel = valueLabel();
	private final JLabel positionLabel = valueLabel();
	private final JPanel metricsGrid = new JPanel(new GridLayout(2, 4, 10, 2));
	private final JPanel stageStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 2));
	private final Timer pollTimer = new Timer(POLL_INTERVAL_MS, e -> refresh());

	private PlaybackClock clock;
	private FlightReplayData replayData;
	private FlightData flightData;
	private List<FlightReplayData.StageStatus> displayedStageStatuses;
	private List<Double> times;
	private List<Double> altitude;
	private List<Double> velocity;
	private List<Double> acceleration;
	private List<Double> east;
	private List<Double> north;

	FlightMetricsPanel() {
		super(new BorderLayout());
		setBackground(new Color(0x1E1E1E));
		setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
		metricsGrid.setOpaque(false);
		addField(trans.get("Flight3DFrame.metrics.simulation"), simulationLabel);
		addField(trans.get("Flight3DFrame.metrics.configuration"), configLabel);
		addField(trans.get("Flight3DFrame.metrics.time"), timeLabel);
		addField(trans.get("Flight3DFrame.metrics.altitude"), altitudeLabel);
		addField(trans.get("Flight3DFrame.metrics.velocity"), velocityLabel);
		addField(trans.get("Flight3DFrame.metrics.acceleration"), accelerationLabel);
		addField(trans.get("Flight3DFrame.metrics.position"), positionLabel);
		add(metricsGrid, BorderLayout.CENTER);
		stageStatusPanel.setOpaque(false);
		add(stageStatusPanel, BorderLayout.SOUTH);
		updateStageStatuses(List.of());
	}

	void setReplay(Simulation simulation, PlaybackClock clock, FlightReplayData replayData) {
		this.clock = clock;
		this.replayData = replayData;
		if (simulation == null || clock == null || !simulation.hasSimulationData()) {
			this.times = null;
			pollTimer.stop();
			clearValues();
			return;
		}

		simulationLabel.setText(orEmpty(simulation.getName()));
		configLabel.setText(orEmpty(configurationName(simulation)));

		this.flightData = simulation.getSimulatedData();
		loadBranch(0);
		pollTimer.start();
	}

	/** Shows the telemetry of another flying body (its simulation branch). */
	void setTrackedBody(int bodyIndex) {
		if (flightData == null || replayData == null
				|| bodyIndex < 0 || bodyIndex >= replayData.getFlightBodies().size()) {
			return;
		}
		loadBranch(replayData.getFlightBodies().get(bodyIndex).branchIndex());
	}

	private void loadBranch(int branchIndex) {
		FlightDataBranch branch = flightData.getBranch(branchIndex);
		this.times = branch.get(FlightDataType.TYPE_TIME);
		this.altitude = branch.get(FlightDataType.TYPE_ALTITUDE);
		this.velocity = branch.get(FlightDataType.TYPE_VELOCITY_TOTAL);
		this.acceleration = branch.get(FlightDataType.TYPE_ACCELERATION_TOTAL);
		this.east = branch.get(FlightDataType.TYPE_POSITION_X);
		this.north = branch.get(FlightDataType.TYPE_POSITION_Y);
		refresh();
	}

	void dispose() {
		pollTimer.stop();
		clock = null;
		replayData = null;
		flightData = null;
		times = null;
		altitude = null;
		velocity = null;
		acceleration = null;
		east = null;
		north = null;
		clearValues();
	}

	private void refresh() {
		if (clock == null || times == null || times.isEmpty()) {
			return;
		}
		double t = clock.getTime();
		timeLabel.setText(String.format(trans.get("Flight3DFrame.metrics.timeFormat"), t));
		altitudeLabel.setText(formatMetric(altitude, t, FlightDataType.TYPE_ALTITUDE));
		velocityLabel.setText(formatMetric(velocity, t, FlightDataType.TYPE_VELOCITY_TOTAL));
		accelerationLabel.setText(formatMetric(acceleration, t, FlightDataType.TYPE_ACCELERATION_TOTAL));
		positionLabel.setText(formatPosition(t));
		updateStageStatuses(replayData != null ? replayData.getStageStatuses(t) : List.of());
	}

	private String formatMetric(List<Double> values, double t, FlightDataType type) {
		if (values == null) {
			return EMPTY;
		}
		double value = MathUtil.interpolate(times, values, t);
		if (Double.isNaN(value)) {
			return EMPTY;
		}
		return type.getUnitGroup().toStringUnit(value);
	}

	private String formatPosition(double t) {
		if (east == null || north == null) {
			return EMPTY;
		}
		double x = MathUtil.interpolate(times, east, t);
		double y = MathUtil.interpolate(times, north, t);
		if (Double.isNaN(x) || Double.isNaN(y)) {
			return EMPTY;
		}
		UnitGroup distance = FlightDataType.TYPE_POSITION_X.getUnitGroup();
		return String.format(trans.get("Flight3DFrame.metrics.positionFormat"),
				distance.toStringUnit(x), distance.toStringUnit(y));
	}

	private void clearValues() {
		for (JLabel label : new JLabel[] { simulationLabel, configLabel, timeLabel, altitudeLabel,
				velocityLabel, accelerationLabel, positionLabel }) {
			label.setText(EMPTY);
		}
		displayedStageStatuses = null;
		updateStageStatuses(List.of());
	}

	private void updateStageStatuses(List<FlightReplayData.StageStatus> statuses) {
		if (statuses.equals(displayedStageStatuses)) {
			return;
		}
		displayedStageStatuses = List.copyOf(statuses);
		stageStatusPanel.removeAll();
		JLabel title = new JLabel(trans.get("Flight3DFrame.metrics.stageStates") + ":");
		title.setForeground(new Color(0x9AA0A6));
		title.setFont(title.getFont().deriveFont(Font.PLAIN, 11f));
		stageStatusPanel.add(title);
		if (statuses.isEmpty()) {
			stageStatusPanel.add(valueLabel());
		} else {
			for (FlightReplayData.StageStatus status : statuses) {
				JLabel chip = new JLabel(String.format(trans.get("Flight3DFrame.metrics.stageStatusFormat"),
						stageGroupName(status.stages()), phaseName(status.phase())));
				chip.setOpaque(true);
				chip.setBackground(phaseColor(status.phase()));
				chip.setForeground(new Color(0xFFFFFF));
				chip.setFont(chip.getFont().deriveFont(Font.BOLD, 11f));
				chip.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
				stageStatusPanel.add(chip);
			}
		}
		stageStatusPanel.revalidate();
		stageStatusPanel.repaint();
	}

	private void addField(String name, JLabel valueLabel) {
		JPanel field = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		field.setOpaque(false);
		JLabel nameLabel = new JLabel(name + ":");
		nameLabel.setForeground(new Color(0x9AA0A6));
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.PLAIN, 11f));
		field.add(nameLabel);
		field.add(valueLabel);
		metricsGrid.add(field);
	}

	/** Names a group of stages, e.g. "Sustainer + Payload", numbering unnamed stages. */
	static String stageGroupName(List<AxialStage> stages) {
		return stages.stream()
				.map(stage -> {
					String name = stage.getName();
					return name == null || name.isBlank()
							? String.format(trans.get("Flight3DFrame.metrics.stageFallback"),
									stage.getStageNumber() + 1)
							: name;
				})
				.collect(Collectors.joining(" + "));
	}

	private static String phaseName(FlightReplayData.FlightPhase phase) {
		return switch (phase) {
			case ON_PAD -> trans.get("Flight3DFrame.metrics.state.onPad");
			case UNDER_THRUST -> trans.get("Flight3DFrame.metrics.state.underThrust");
			case COASTING -> trans.get("Flight3DFrame.metrics.state.coasting");
			case RECOVERY -> trans.get("Flight3DFrame.metrics.state.recovery");
			case TUMBLING -> trans.get("Flight3DFrame.metrics.state.tumbling");
			case LANDED -> trans.get("Flight3DFrame.metrics.state.landed");
		};
	}

	private static Color phaseColor(FlightReplayData.FlightPhase phase) {
		return switch (phase) {
			case ON_PAD -> new Color(0x4B5563);
			case UNDER_THRUST -> new Color(0xA64B17);
			case COASTING -> new Color(0x385A7C);
			case RECOVERY -> new Color(0x327052);
			case TUMBLING -> new Color(0x8A3D46);
			case LANDED -> new Color(0x555555);
		};
	}

	private static JLabel valueLabel() {
		JLabel label = new JLabel(EMPTY);
		label.setForeground(new Color(0xE8EAED));
		label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
		return label;
	}

	private static String configurationName(Simulation simulation) {
		try {
			return simulation.getActiveConfiguration().getName();
		} catch (RuntimeException e) {
			return null;
		}
	}

	private static String orEmpty(String value) {
		return value == null || value.isBlank() ? EMPTY : value;
	}
}
