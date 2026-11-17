package info.openrocket.core.simulation.montecarlo;

import java.util.ArrayList;
import java.util.HashMap;
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
		Map<String, String> names = getDisplayNames();
		List<LandingBody> named = new ArrayList<>(bodies.size());
		for (LandingBody body : bodies.values()) {
			named.add(new LandingBody(body.bodyId(), body.branchIndex(), names.get(body.bodyId())));
		}
		return List.copyOf(named);
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
		Map<String, MonteCarloFlightBranch> branches = collectFlightBranches();
		Map<String, String> names = getDisplayNames();
		List<MonteCarloFlightBranch> named = new ArrayList<>(branches.size());
		for (MonteCarloFlightBranch branch : branches.values()) {
			named.add(new MonteCarloFlightBranch(branch.branchId(), branch.branchIndex(),
					names.get(branch.branchId())));
		}
		return List.copyOf(named);
	}

	private Map<String, MonteCarloFlightBranch> collectFlightBranches() {
		Map<String, MonteCarloFlightBranch> branches = new LinkedHashMap<>();
		addFlightBranches(branches, nominalResult);
		for (MonteCarloRunResult result : runResults) {
			addFlightBranches(branches, result);
		}
		return branches;
	}

	/**
	 * Map each branch/body id to a display name. Separate stages often share a name (for example
	 * two stages both called "Booster stage"), so repeated names are numbered in branch order.
	 * Landing bodies and flight branches share the numbering because both use the same ids.
	 */
	private Map<String, String> getDisplayNames() {
		Map<String, String> rawNames = new LinkedHashMap<>();
		for (MonteCarloFlightBranch branch : collectFlightBranches().values()) {
			rawNames.putIfAbsent(branch.branchId(), branch.branchName());
		}
		Map<String, LandingBody> bodies = new LinkedHashMap<>();
		addBodies(bodies, nominalResult);
		for (MonteCarloRunResult result : runResults) {
			addBodies(bodies, result);
		}
		for (LandingBody body : bodies.values()) {
			rawNames.putIfAbsent(body.bodyId(), body.branchName());
		}

		Map<String, Integer> counts = new HashMap<>();
		for (String name : rawNames.values()) {
			counts.merge(name, 1, Integer::sum);
		}
		Map<String, Integer> seen = new HashMap<>();
		Map<String, String> names = new LinkedHashMap<>();
		for (Map.Entry<String, String> entry : rawNames.entrySet()) {
			String name = entry.getValue();
			if (counts.get(name) > 1) {
				int number = seen.merge(name, 1, Integer::sum);
				name = name + " (" + number + ")";
			}
			names.put(entry.getKey(), name);
		}
		return names;
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
