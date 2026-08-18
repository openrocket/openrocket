package info.openrocket.core.simulation.montecarlo;

/**
 * Horizontal landing position in the launch-pad coordinate system.
 *
 * @param branchIndex simulation branch/body index
 * @param branchName display name of the simulated body
 * @param east eastward displacement from the pad in metres
 * @param north northward displacement from the pad in metres
 */
public record LandingPoint(int branchIndex, String branchName, double east, double north) {
	public double rangeFromPad() {
		return Math.hypot(east, north);
	}
}
