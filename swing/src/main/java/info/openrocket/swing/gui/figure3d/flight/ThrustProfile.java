package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.swing.gui.figure3d.animation.TimeSeries;
import info.openrocket.swing.gui.figure3d.flight.FlightReplayData.BurnInterval;

import java.util.List;

/**
 * A branch's recorded thrust relative to each burn's average, so the replay flame grows with
 * the ignition spike and dies down through the tail-off while looking as it always did at
 * average thrust. Without thrust data every burn reads as average.
 */
final class ThrustProfile {
	private static final int AVERAGE_SAMPLES = 64;
	static final ThrustProfile STEADY = new ThrustProfile(new double[0], new double[0], List.of());

	private final double[] times;
	private final double[] thrust;
	private final List<BurnInterval> windows;
	private final double[] windowAverages;

	private ThrustProfile(double[] times, double[] thrust, List<BurnInterval> windows) {
		this.times = times;
		this.thrust = thrust;
		this.windows = List.copyOf(windows);
		this.windowAverages = new double[this.windows.size()];
		for (int i = 0; i < windowAverages.length; i++) {
			BurnInterval window = this.windows.get(i);
			double sum = 0.0;
			for (int s = 0; s < AVERAGE_SAMPLES; s++) {
				sum += thrustAt(window.start() + (window.end() - window.start()) * (s + 0.5) / AVERAGE_SAMPLES);
			}
			windowAverages[i] = sum / AVERAGE_SAMPLES;
		}
	}

	/** Uses the branch's thrust over the given burn windows. */
	static ThrustProfile fromBranch(FlightDataBranch branch, List<BurnInterval> burnWindows) {
		if (branch == null) {
			return STEADY;
		}
		List<Double> time = branch.get(FlightDataType.TYPE_TIME);
		List<Double> force = branch.get(FlightDataType.TYPE_THRUST_FORCE);
		if (time == null || force == null || time.isEmpty() || force.isEmpty()) {
			return STEADY;
		}
		int count = Math.min(time.size(), force.size());
		double[] times = new double[count];
		double[] thrust = new double[count];
		for (int i = 0; i < count; i++) {
			Double t = time.get(i);
			Double f = force.get(i);
			times[i] = t != null && Double.isFinite(t) ? t : (i > 0 ? times[i - 1] : 0.0);
			thrust[i] = f != null && Double.isFinite(f) ? Math.max(0.0, f) : 0.0;
		}
		return new ThrustProfile(times, thrust, burnWindows);
	}

	/** Thrust at the time relative to the average of the burn it falls in; 1 outside burns. */
	double relativeThrustAt(double time) {
		for (int i = 0; i < windows.size(); i++) {
			if (windows.get(i).contains(time)) {
				return windowAverages[i] > 0.0 ? thrustAt(time) / windowAverages[i] : 1.0;
			}
		}
		return 1.0;
	}

	private double thrustAt(double time) {
		return TimeSeries.interpolate(times, thrust, time);
	}
}
