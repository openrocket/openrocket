package info.openrocket.core.simulation.montecarlo;

import java.util.List;

/**
 * Inputs and terminal outputs for one dispersed trajectory.
 */
public record MonteCarloRunResult(MonteCarloSample sample, List<LandingPoint> landingPoints,
		List<LandingBodyFailure> bodyFailures, List<MonteCarloBranchResult> branchResults,
		double maximumAltitude, double flightTime, String failureMessage) {
	public MonteCarloRunResult {
		landingPoints = List.copyOf(landingPoints);
		bodyFailures = List.copyOf(bodyFailures);
		branchResults = List.copyOf(branchResults);
	}

	public MonteCarloRunResult(MonteCarloSample sample, List<LandingPoint> landingPoints,
			List<LandingBodyFailure> bodyFailures, double maximumAltitude, double flightTime,
			String failureMessage) {
		this(sample, landingPoints, bodyFailures, List.of(), maximumAltitude, flightTime, failureMessage);
	}

	public MonteCarloRunResult(MonteCarloSample sample, List<LandingPoint> landingPoints,
			double maximumAltitude, double flightTime, String failureMessage) {
		this(sample, landingPoints, List.of(), maximumAltitude, flightTime, failureMessage);
	}

	public MonteCarloBranchResult getBranchResult(String branchId) {
		for (MonteCarloBranchResult result : branchResults) {
			if (result.branchId().equals(branchId)) {
				return result;
			}
		}
		return null;
	}

	public LandingPoint getLandingPoint(String bodyId) {
		for (LandingPoint point : landingPoints) {
			if (point.bodyId().equals(bodyId)) {
				return point;
			}
		}
		return null;
	}

	/**
	 * Look up a point by its trajectory-local branch index.
	 * Prefer {@link #getLandingPoint(String)} when correlating separate runs.
	 */
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

	public boolean hasLandingPoint(String bodyId) {
		return getLandingPoint(bodyId) != null;
	}

	public LandingBodyFailure getBodyFailure(String bodyId) {
		for (LandingBodyFailure failure : bodyFailures) {
			if (failure.bodyId().equals(bodyId)) {
				return failure;
			}
		}
		return null;
	}

	/**
	 * Return the failure applying to one body, including a trajectory-wide failure.
	 */
	public String getFailureMessage(String bodyId) {
		if (failureMessage != null) {
			return failureMessage;
		}
		LandingBodyFailure failure = getBodyFailure(bodyId);
		return failure == null ? null : failure.message();
	}

	public int getBranchIndex(String bodyId) {
		LandingPoint point = getLandingPoint(bodyId);
		if (point != null) {
			return point.branchIndex();
		}
		LandingBodyFailure failure = getBodyFailure(bodyId);
		return failure == null ? -1 : failure.branchIndex();
	}
}
