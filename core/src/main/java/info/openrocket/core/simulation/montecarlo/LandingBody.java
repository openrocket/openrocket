package info.openrocket.core.simulation.montecarlo;

/**
 * Identifies one independently simulated landing body/branch.
 *
 * @param bodyId stable source-component identity across trajectory simulations
 * @param branchIndex branch index in the first simulation where this body was observed
 * @param branchName display name for the branch
 */
public record LandingBody(String bodyId, int branchIndex, String branchName) {
	public LandingBody(int branchIndex, String branchName) {
		this(legacyBodyId(branchIndex), branchIndex, branchName);
	}

	static String legacyBodyId(int branchIndex) {
		return "legacy-branch:" + branchIndex;
	}

	@Override
	public String toString() {
		return branchName;
	}
}
