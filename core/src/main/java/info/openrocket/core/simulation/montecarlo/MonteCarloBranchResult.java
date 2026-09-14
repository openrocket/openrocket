package info.openrocket.core.simulation.montecarlo;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Scalar outputs and status for one flight-data branch in one trajectory. */
public record MonteCarloBranchResult(String branchId, int branchIndex, String branchName,
		Map<MonteCarloMetric, Double> metrics, String failureMessage) {
	public MonteCarloBranchResult {
		EnumMap<MonteCarloMetric, Double> copy = new EnumMap<>(MonteCarloMetric.class);
		copy.putAll(metrics);
		metrics = Collections.unmodifiableMap(copy);
	}

	public double getMetric(MonteCarloMetric metric) {
		return metrics.getOrDefault(metric, Double.NaN);
	}
}
