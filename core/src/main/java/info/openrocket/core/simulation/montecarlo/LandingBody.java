package info.openrocket.core.simulation.montecarlo;

/**
 * Identifies one independently simulated landing body/branch.
 *
 * @param branchIndex branch index in the simulation data
 * @param branchName display name for the branch
 */
public record LandingBody(int branchIndex, String branchName) {
	@Override
	public String toString() {
		return branchName;
	}
}
