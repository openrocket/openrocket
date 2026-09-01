package info.openrocket.core.simulation.montecarlo;

/** A flight-data branch observed in at least one Monte Carlo trajectory. */
public record MonteCarloFlightBranch(String branchId, int branchIndex, String branchName) {
	@Override
	public String toString() {
		return branchName;
	}
}
