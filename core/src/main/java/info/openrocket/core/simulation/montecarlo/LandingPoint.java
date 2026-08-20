package info.openrocket.core.simulation.montecarlo;

/**
 * Horizontal landing position in the launch-pad coordinate system.
 *
 * @param bodyId stable source-component identity across trajectory simulations
 * @param branchIndex branch index in this trajectory's simulation data
 * @param branchName display name of the simulated body
 * @param east eastward displacement from the pad in metres
 * @param north northward displacement from the pad in metres
 */
public record LandingPoint(String bodyId, int branchIndex, String branchName, double east, double north) {
	public LandingPoint(int branchIndex, String branchName, double east, double north) {
		this(LandingBody.legacyBodyId(branchIndex), branchIndex, branchName, east, north);
	}

	public double rangeFromPad() {
		return Math.hypot(east, north);
	}
}
