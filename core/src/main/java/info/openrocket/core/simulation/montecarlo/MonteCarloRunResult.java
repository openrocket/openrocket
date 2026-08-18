package info.openrocket.core.simulation.montecarlo;

import java.util.List;

/**
 * Inputs and terminal outputs for one dispersed trajectory.
 */
public record MonteCarloRunResult(MonteCarloSample sample, List<LandingPoint> landingPoints,
		double maximumAltitude, double flightTime, String failureMessage) {
	public MonteCarloRunResult {
		landingPoints = List.copyOf(landingPoints);
	}

	public LandingPoint getLandingPoint(int branchIndex) {
		for (LandingPoint point : landingPoints) {
			if (point.branchIndex() == branchIndex) {
				return point;
			}
		}
		return null;
	}

	public boolean hasLandingPoint(int branchIndex) {
		return getLandingPoint(branchIndex) != null;
	}
}
