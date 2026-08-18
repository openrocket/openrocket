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
		Map<Integer, LandingBody> bodies = new LinkedHashMap<>();
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
	public List<LandingPoint> getLandingPoints(int branchIndex) {
		List<LandingPoint> points = new ArrayList<>();
		for (MonteCarloRunResult result : runResults) {
			if (result.failureMessage() != null) {
				continue;
			}
			LandingPoint point = result.getLandingPoint(branchIndex);
			if (point != null) {
				points.add(point);
			}
		}
		return points;
	}

	public int getFailureCount(int branchIndex) {
		int landed = 0;
		for (MonteCarloRunResult result : runResults) {
			if (result.failureMessage() == null && result.hasLandingPoint(branchIndex)) {
				landed++;
			}
		}
		return runResults.size() - landed;
	}

	private static void addBodies(Map<Integer, LandingBody> bodies, MonteCarloRunResult result) {
		for (LandingPoint point : result.landingPoints()) {
			bodies.putIfAbsent(point.branchIndex(), new LandingBody(point.branchIndex(), point.branchName()));
		}
	}
}
