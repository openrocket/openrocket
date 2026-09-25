package info.openrocket.core.simulation.montecarlo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete nominal and dispersed result set for one analysis.
 */
public final class MonteCarloResult {
	private final MonteCarloSettings settings;
	private final MonteCarloRunResult nominalResult;
	private final List<MonteCarloRunResult> runResults;
	private final long elapsedMillis;

	public MonteCarloResult(MonteCarloSettings settings, MonteCarloRunResult nominalResult,
			List<MonteCarloRunResult> runResults, long elapsedMillis) {
		this.settings = settings;
		this.nominalResult = nominalResult;
		this.runResults = List.copyOf(runResults);
		this.elapsedMillis = elapsedMillis;
	}

	public MonteCarloSettings getSettings() {
		return settings;
	}

	public MonteCarloRunResult getNominalResult() {
		return nominalResult;
	}

	public List<MonteCarloRunResult> getRunResults() {
		return runResults;
	}

	public long getElapsedMillis() {
		return elapsedMillis;
	}

	/**
	 * Return all landing bodies observed in either the nominal or dispersed runs.
	 */
	public List<LandingBody> getLandingBodies() {
		Map<String, LandingBody> bodies = new LinkedHashMap<>();
		addBodies(bodies, nominalResult);
		for (MonteCarloRunResult result : runResults) {
			addBodies(bodies, result);
		}
		return List.copyOf(bodies.values());
	}

	/**
	 * Return landing points from trajectories that completed without an error or
	 * simulation abort. Failed trajectories remain available in {@link #getRunResults()}
	 * but are deliberately excluded from dispersion statistics.
	 */
	public List<LandingPoint> getLandingPoints(String bodyId) {
		List<LandingPoint> points = new ArrayList<>();
		for (MonteCarloRunResult result : runResults) {
			if (result.getFailureMessage(bodyId) != null) {
				continue;
			}
			LandingPoint point = result.getLandingPoint(bodyId);
			if (point != null) {
				points.add(point);
			}
		}
		return points;
	}

	public int getFailureCount(String bodyId) {
		int landed = 0;
		for (MonteCarloRunResult result : runResults) {
			if (result.getFailureMessage(bodyId) == null && result.hasLandingPoint(bodyId)) {
				landed++;
			}
		}
		return runResults.size() - landed;
	}

	/** Return all flight-data branches observed in the nominal or dispersed runs. */
	public List<MonteCarloFlightBranch> getFlightBranches() {
		Map<String, MonteCarloFlightBranch> branches = new LinkedHashMap<>();
		addFlightBranches(branches, nominalResult);
		for (MonteCarloRunResult result : runResults) {
			addFlightBranches(branches, result);
		}
		return List.copyOf(branches.values());
	}

	/** Return valid dispersed values for one branch and scalar output metric. */
	public List<Double> getMetricValues(String branchId, MonteCarloMetric metric) {
		List<Double> values = new ArrayList<>();
		for (MonteCarloRunResult result : runResults) {
			if (result.failureMessage() != null) {
				continue;
			}
			MonteCarloBranchResult branch = result.getBranchResult(branchId);
			if (branch == null || branch.failureMessage() != null) {
				continue;
			}
			double value = branch.getMetric(metric);
			if (Double.isFinite(value)) {
				values.add(value);
			}
		}
		return values;
	}

	public int getMetricMissingCount(String branchId, MonteCarloMetric metric) {
		return runResults.size() - getMetricValues(branchId, metric).size();
	}

	private static void addFlightBranches(Map<String, MonteCarloFlightBranch> branches,
			MonteCarloRunResult result) {
		for (MonteCarloBranchResult branch : result.branchResults()) {
			branches.putIfAbsent(branch.branchId(), new MonteCarloFlightBranch(branch.branchId(),
					branch.branchIndex(), branch.branchName()));
		}
	}

	private static void addBodies(Map<String, LandingBody> bodies, MonteCarloRunResult result) {
		for (LandingPoint point : result.landingPoints()) {
			bodies.putIfAbsent(point.bodyId(),
					new LandingBody(point.bodyId(), point.branchIndex(), point.branchName()));
		}
		for (LandingBodyFailure failure : result.bodyFailures()) {
			bodies.putIfAbsent(failure.bodyId(),
					new LandingBody(failure.bodyId(), failure.branchIndex(), failure.branchName()));
		}
	}
}
