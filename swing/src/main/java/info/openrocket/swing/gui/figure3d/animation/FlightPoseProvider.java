package info.openrocket.swing.gui.figure3d.animation;

import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * First-pass adapter: samples OpenRocket FlightDataBranch and exposes a world-space pose.
 * - Position uses Px (east), Py (north), altitude (meters) -> mapped to engine world & scaled.
 * - Orientation prefers ORIENTATION_THETA (zenith) + ORIENTATION_PHI (azimuth).
 *   If missing, it faces along the instantaneous velocity (finite difference).
 *
 *   How to use:
 *   import animation.info.openrocket.swing.gui.figure3d.FlightPoseProvider;
 *
 * // Build the adapter from OpenRocket data
 * FlightPoseProvider provider = FlightPoseProvider.fromFlightDataBranch(flightBranch);
 *
 * // Tell the orchestrator to animate all rocket component meshes
 * orchestrator.bindFlightPoseToRocket(provider);
 *
 * // Basic controls:
 * orchestrator.getPlaybackClock().setRate(1.0);   // play
 * orchestrator.getPlaybackClock().setRate(0.0);   // pause
 * orchestrator.getPlaybackClock().setTime(12.3);  // scrub to 12.3 s
 */
public final class FlightPoseProvider implements PoseProvider {
	private final double[] t;
	private final double[] east, north, alt;
	private final double[] thetaZenith;     // optional
	private final double[] phiAzimuth;      // optional

	private FlightPoseProvider(double[] t, double[] east, double[] north, double[] alt,
							   double[] thetaZenith, double[] phiAzimuth) {
		this.t = t; this.east = east; this.north = north; this.alt = alt;
		this.thetaZenith = thetaZenith; this.phiAzimuth = phiAzimuth;
	}

	// ---------- Factory ----------
	public static FlightPoseProvider fromFlightDataBranch(FlightDataBranch branch) {
		// Required channels
		List<Double> tL     = branch.get(FlightDataType.TYPE_TIME);
		if (tL == null || tL.isEmpty()) throw new IllegalArgumentException("FlightDataBranch has no TIME channel");

		// Position: prefer explicit east/north; fallback to XY + direction
		List<Double> eastL  = branch.get(FlightDataType.TYPE_POSITION_X);
		List<Double> northL = branch.get(FlightDataType.TYPE_POSITION_Y);

		if (eastL == null || northL == null) {
			List<Double> rXY  = branch.get(FlightDataType.TYPE_POSITION_XY);
			List<Double> dirL = branch.get(FlightDataType.TYPE_POSITION_DIRECTION); // azimuth from east, radians
			if (rXY != null && dirL != null) {
				eastL  = new ArrayList<>(rXY.size());
				northL = new ArrayList<>(rXY.size());
				for (int i = 0; i < Math.min(rXY.size(), dirL.size()); i++) {
					double r = nz(rXY.get(i));
					double th = nz(dirL.get(i)); // measured from east CCW -> engine x/z mapping below
					eastL.add(r * Math.cos(th));
					northL.add(r * Math.sin(th));
				}
			}
		}

		if (eastL == null || northL == null)
			throw new IllegalArgumentException("FlightDataBranch lacks lateral position (Px/Py or XY+dir)");

		List<Double> altL = nvl(branch.get(FlightDataType.TYPE_ALTITUDE),
				branch.get(FlightDataType.TYPE_ALTITUDE_ABOVE_SEA));
		if (altL == null) throw new IllegalArgumentException("FlightDataBranch lacks altitude channel");

		// Optional orientation
		List<Double> thetaL = branch.get(FlightDataType.TYPE_ORIENTATION_THETA); // 0 = up
		List<Double> phiL   = branch.get(FlightDataType.TYPE_ORIENTATION_PHI);   // azimuth from east, CCW

		// Length align (defensive)
		int n = minLen(tL, eastL, northL, altL);
		if (thetaL != null) n = Math.min(n, thetaL.size());
		if (phiL   != null) n = Math.min(n, phiL.size());

		return new FlightPoseProvider(
				toPrimitive(tL, n),
				toPrimitive(eastL, n),
				toPrimitive(northL, n),
				toPrimitive(altL, n),
				(thetaL != null ? toPrimitive(thetaL, n) : null),
				(phiL   != null ? toPrimitive(phiL,   n) : null)
		);
	}

	// ---------- PoseProvider ----------
	@Override
	public Vector3f getPosition(double time) {
		float ex = sample(t, east,  time);
		float no = sample(t, north, time);
		float al = sample(t, alt,   time);
		return SimToWorld.toEngine(ex, no, al);
	}

	@Override
	public Quaternionf getOrientation(double time) {
		// Preferred: explicit zenith/azimuth
		if (thetaZenith != null && phiAzimuth != null) {
			float th = sample(t, thetaZenith, time); // 0 = up (+Y)
			float ph = sample(t, phiAzimuth,  time); // 0 = +X (east), CCW towards +Z in OR -> -Z in engine
			Vector3f horiz = new Vector3f((float)Math.cos(ph), 0f, (float)-Math.sin(ph)); // -Z for engine
			Vector3f dir = new Vector3f(horiz).mul((float)Math.sin(th))
					.add(0f, (float)Math.cos(th), 0f)
					.normalize();
			return new Quaternionf().rotateTo(new Vector3f(0,1,0), dir);
		}

		// Fallback: face the velocity (central difference of position)
		final double eps = 0.02; // 20 ms
		Vector3f p0 = getPosition(Math.max(getStartTime(), time - eps));
		Vector3f p1 = getPosition(Math.min(getEndTime(),   time + eps));
		Vector3f v = p1.sub(p0, new Vector3f());
		if (v.lengthSquared() < 1e-12f) return new Quaternionf(); // no rotation
		v.normalize();
		return new Quaternionf().rotateTo(new Vector3f(0,1,0), v);
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

	private static float sample(double[] ts, double[] vs, double t) {
		if (t <= ts[0]) return (float)vs[0];
		int hi = upperBound(ts, t);
		if (hi <= 0) return (float)vs[0];
		if (hi >= ts.length) return (float)vs[ts.length - 1];
		int lo = hi - 1;
		double a = ts[lo], b = ts[hi];
		double u = (t - a) / Math.max(1e-9, (b - a));
		return (float)((1.0 - u) * vs[lo] + u * vs[hi]);
	}

	private static int upperBound(double[] ts, double t) {
		int lo = 0, hi = ts.length;
		while (lo < hi) {
			int mid = (lo + hi) >>> 1;
			if (t >= ts[mid]) lo = mid + 1; else hi = mid;
		}
		return lo;
	}
}
