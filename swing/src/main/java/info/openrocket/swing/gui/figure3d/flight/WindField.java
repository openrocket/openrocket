package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.util.MathUtil;
import info.openrocket.swing.gui.figure3d.animation.SimToWorld;
import info.openrocket.swing.gui.figure3d.animation.TimeSeries;
import org.joml.Vector3f;

import java.util.List;

/**
 * The wind a simulation branch recorded along its flight, as an engine-space velocity over
 * time. The simulator stores the wind speed and, like the launch conditions, the direction the
 * wind blows from, clockwise from north (a descending parachute drifts the opposite way).
 * Exhaust smoke released at a moment drifts with the wind recorded then, at the altitude the
 * rocket was flying.
 */
final class WindField {
	/** Below this speed (m/s) the wind has no direction worth showing. */
	static final double CALM_SPEED = 0.1;

	private static final WindField CALM = new WindField(new double[] { 0.0 }, new double[] { 0.0 },
			new double[] { 0.0 });

	private final double[] times;
	private final double[] east;
	private final double[] north;

	private WindField(double[] times, double[] east, double[] north) {
		this.times = times;
		this.east = east;
		this.north = north;
	}

	static WindField calm() {
		return CALM;
	}

	static WindField fromBranch(FlightDataBranch branch) {
		if (branch == null) {
			return CALM;
		}
		List<Double> time = branch.get(FlightDataType.TYPE_TIME);
		List<Double> speed = branch.get(FlightDataType.TYPE_WIND_VELOCITY);
		List<Double> direction = branch.get(FlightDataType.TYPE_WIND_DIRECTION);
		if (time == null || speed == null || direction == null) {
			return CALM;
		}
		int count = Math.min(time.size(), Math.min(speed.size(), direction.size()));
		if (count == 0) {
			return CALM;
		}
		double[] times = new double[count];
		double[] east = new double[count];
		double[] north = new double[count];
		for (int i = 0; i < count; i++) {
			times[i] = finiteOr(time.get(i), i > 0 ? times[i - 1] : 0.0);
			double v = finiteOr(speed.get(i), 0.0);
			double heading = finiteOr(direction.get(i), 0.0);
			// The air moves away from the heading. Components rather than angles, so
			// interpolating across north cannot swing the wind.
			east[i] = -v * Math.sin(heading);
			north[i] = -v * Math.cos(heading);
		}
		return new WindField(times, east, north);
	}

	/** Wind velocity at the given flight time in engine units per second. */
	Vector3f velocityAt(double time) {
		return toEngine(TimeSeries.interpolate(times, east, time), TimeSeries.interpolate(times, north, time));
	}

	/** Wind speed at the given flight time in meters per second. */
	double speedAt(double time) {
		return Math.hypot(TimeSeries.interpolate(times, east, time), TimeSeries.interpolate(times, north, time));
	}

	/**
	 * The direction the wind blows from at the given flight time, in radians clockwise from north
	 * within [0, 2 pi), as the simulator records it. Meaningless when the air is calm.
	 */
	double directionAt(double time) {
		double e = TimeSeries.interpolate(times, east, time);
		double n = TimeSeries.interpolate(times, north, time);
		return MathUtil.reduce2Pi(Math.atan2(-e, -n));
	}

	private static Vector3f toEngine(double east, double north) {
		return SimToWorld.toEngine((float) east, (float) north, 0.0f);
	}

	private static double finiteOr(Double value, double fallback) {
		return value != null && Double.isFinite(value) ? value : fallback;
	}
}
