package info.openrocket.swing.gui.figure3d.animation;

import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Samples a {@link FlightDataBranch} as an engine-space position and orientation.
 * Position uses east, north, and altitude data; orientation uses elevation and
 * azimuth when available and otherwise follows the sampled velocity. When the roll angle is
 * recorded, the rocket is turned about its long axis by it while standing upright, then tilted
 * straight to where it points, which is how the simulator defines that angle.
 */
public final class FlightPoseProvider implements PoseProvider {
	private static final Vector3f LOCAL_NOSE_AXIS = new Vector3f(-1, 0, 0);
	private static final Vector3f UP = new Vector3f(0, 1, 0);
	// Stands the unposed rocket, which lies along the X axis, upright.
	private static final Quaternionf STAND_UPRIGHT = new Quaternionf().rotateTo(LOCAL_NOSE_AXIS, UP);

	private final double[] t;
	private final double[] east, north, alt;
	private final double[] thetaElevation;  // optional
	private final double[] phiAzimuth;      // optional
	private final double[] rollAngle;       // optional

	private FlightPoseProvider(double[] t, double[] east, double[] north, double[] alt,
							   double[] thetaElevation, double[] phiAzimuth, double[] rollAngle) {
		this.t = t;
		this.east = east;
		this.north = north;
		this.alt = alt;
		this.thetaElevation = thetaElevation;
		this.phiAzimuth = phiAzimuth;
		this.rollAngle = rollAngle;
	}

	/**
	 * Reads a branch's time, position and (when recorded) attitude.
	 *
	 * @throws IllegalArgumentException if the branch lacks time, lateral position or altitude data
	 */
	public static FlightPoseProvider fromFlightDataBranch(FlightDataBranch branch) {
		// Required channels
		List<Double> tL     = branch.get(FlightDataType.TYPE_TIME);
		if (tL == null || tL.isEmpty()) throw new IllegalArgumentException("FlightDataBranch has no TIME channel");

		// Position: prefer explicit east/north; fallback to XY + direction
		List<Double> eastL  = branch.get(FlightDataType.TYPE_POSITION_X);
		List<Double> northL = branch.get(FlightDataType.TYPE_POSITION_Y);

		if (eastL == null || northL == null) {
			List<Double> rXY  = branch.get(FlightDataType.TYPE_POSITION_XY);
			List<Double> dirL = branch.get(FlightDataType.TYPE_POSITION_DIRECTION); // azimuth from north, radians
			if (rXY != null && dirL != null) {
				eastL  = new ArrayList<>(rXY.size());
				northL = new ArrayList<>(rXY.size());
				for (int i = 0; i < Math.min(rXY.size(), dirL.size()); i++) {
					double r = nz(rXY.get(i));
					double th = nz(dirL.get(i)); // 0 = north, positive toward east
					eastL.add(r * Math.sin(th));
					northL.add(r * Math.cos(th));
				}
			}
		}

		if (eastL == null || northL == null)
			throw new IllegalArgumentException("FlightDataBranch lacks lateral position (Px/Py or XY+dir)");

		List<Double> altL = nvl(branch.get(FlightDataType.TYPE_ALTITUDE),
				branch.get(FlightDataType.TYPE_ALTITUDE_ABOVE_SEA));
		if (altL == null) throw new IllegalArgumentException("FlightDataBranch lacks altitude channel");

		// Optional orientation
		List<Double> thetaL = branch.get(FlightDataType.TYPE_ORIENTATION_THETA); // elevation: 0 = horizontal, pi/2 = up
		List<Double> phiL   = branch.get(FlightDataType.TYPE_ORIENTATION_PHI);   // 0 = north, positive toward east
		List<Double> rollL  = branch.get(FlightDataType.TYPE_ORIENTATION_ROLL);  // about the long axis

		// Length align (defensive)
		int n = minLen(tL, eastL, northL, altL);
		if (thetaL != null) n = Math.min(n, thetaL.size());
		if (phiL   != null) n = Math.min(n, phiL.size());
		if (rollL  != null && rollL.size() < n) rollL = null;

		double[] times = toPrimitive(tL, n);
		return new FlightPoseProvider(
				times,
				toPrimitive(eastL, n),
				toPrimitive(northL, n),
				toPrimitive(altL, n),
				(thetaL != null ? toPrimitive(thetaL, n) : null),
				(phiL   != null ? unwrapAngles(toPrimitive(phiL, n)) : null),
				(rollL  != null ? unwrapAngles(toPrimitive(rollL, n)) : null)
		);
	}

	// ---------- PoseProvider ----------
	@Override
	public Vector3f getPosition(double time) {
		return SimToWorld.toEngine(sample(t, east, time), sample(t, north, time), sample(t, alt, time));
	}

	@Override
	public Quaternionf getOrientation(double time) {
		// Preferred: explicit elevation/azimuth
		if (thetaElevation != null && phiAzimuth != null) {
			float th = sample(t, thetaElevation, time);
			float ph = sample(t, phiAzimuth, time);
			float horizontal = (float) Math.cos(th);
			Vector3f dir = new Vector3f(
					horizontal * (float) Math.sin(ph),
					(float) Math.sin(th),
					-horizontal * (float) Math.cos(ph))
					.normalize();
			return pointAt(dir, time);
		}

		// Fallback: face the velocity (central difference of position)
		final double eps = 0.02; // 20 ms
		Vector3f p0 = getPosition(Math.max(getStartTime(), time - eps));
		Vector3f p1 = getPosition(Math.min(getEndTime(),   time + eps));
		Vector3f v = p1.sub(p0, new Vector3f());
		if (v.lengthSquared() < 1e-12f) return new Quaternionf(); // no rotation
		v.normalize();
		return pointAt(v, time);
	}

	/** Points the nose along the given direction, applying the recorded roll if there is one. */
	private Quaternionf pointAt(Vector3f direction, double time) {
		if (rollAngle == null) {
			return new Quaternionf().rotateTo(LOCAL_NOSE_AXIS, direction);
		}
		return new Quaternionf().rotateTo(UP, direction)
				.rotateY(sample(t, rollAngle, time))
				.mul(STAND_UPRIGHT);
	}

	@Override
	public Vector3f getLinearVelocity(double time) {
		// Central finite difference of world position. The sample interval is clamped
		// to the flight's time range so the divisor matches the actual span used, which
		// yields a finite (and zero) velocity right at the start and end of the flight.
		final double eps = 0.02; // 20 ms
		double lo = Math.max(getStartTime(), time - eps);
		double hi = Math.min(getEndTime(), time + eps);
		double span = hi - lo;
		if (span <= 1e-9) {
			return new Vector3f();
		}
		Vector3f p0 = getPosition(lo);
		Vector3f p1 = getPosition(hi);
		return p1.sub(p0, p1).div((float) span);
	}

	@Override public double getStartTime() { return t[0]; }
	@Override public double getEndTime()   { return t[t.length - 1]; }

	// ---------- helpers ----------
	private static int minLen(List<?>... lists) {
		int n = Integer.MAX_VALUE;
		for (List<?> l : lists) n = Math.min(n, l.size());
		return n;
	}

	private static List<Double> nvl(List<Double> a, List<Double> b) { return (a != null ? a : b); }
	private static double nz(Double d) { return (d != null ? d : Double.NaN); }

	private static double[] toPrimitive(List<Double> src, int n) {
		double[] out = new double[n];
		for (int i = 0; i < n; i++) out[i] = nz(src.get(i));
		return out;
	}

	/** Keeps interpolation on the shortest path across the wrap-around of an angle. */
	private static double[] unwrapAngles(double[] angles) {
		double[] result = angles.clone();
		double previous = Double.NaN;
		for (int i = 0; i < result.length; i++) {
			double angle = result[i];
			if (!Double.isFinite(angle)) {
				previous = Double.NaN;
				continue;
			}
			if (Double.isFinite(previous)) {
				angle -= Math.rint((angle - previous) / (2.0 * Math.PI)) * (2.0 * Math.PI);
				result[i] = angle;
			}
			previous = angle;
		}
		return result;
	}

	private static float sample(double[] ts, double[] vs, double t) {
		return (float) TimeSeries.interpolate(ts, vs, t);
	}
}
