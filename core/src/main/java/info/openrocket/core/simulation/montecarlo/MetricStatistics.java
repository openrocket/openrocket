package info.openrocket.core.simulation.montecarlo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Descriptive statistics for one scalar Monte Carlo output. */
public final class MetricStatistics {
	private final List<Double> sortedValues;
	private final double mean;
	private final double standardDeviation;

	private MetricStatistics(List<Double> values) {
		if (values.isEmpty()) {
			throw new IllegalArgumentException("At least one finite value is required");
		}
		List<Double> finiteValues = new ArrayList<>(values.size());
		for (double value : values) {
			if (Double.isFinite(value)) {
				finiteValues.add(value);
			}
		}
		if (finiteValues.isEmpty()) {
			throw new IllegalArgumentException("At least one finite value is required");
		}
		finiteValues.sort(Comparator.naturalOrder());
		this.sortedValues = List.copyOf(finiteValues);

		double total = 0;
		for (double value : sortedValues) {
			total += value;
		}
		this.mean = total / sortedValues.size();

		double squaredDeviations = 0;
		for (double value : sortedValues) {
			double deviation = value - mean;
			squaredDeviations += deviation * deviation;
		}
		this.standardDeviation = sortedValues.size() > 1
				? Math.sqrt(squaredDeviations / (sortedValues.size() - 1.0)) : 0;
	}

	public static MetricStatistics from(List<Double> values) {
		return new MetricStatistics(values);
	}

	public int getSampleCount() {
		return sortedValues.size();
	}

	public double getMinimum() {
		return sortedValues.get(0);
	}

	public double getMaximum() {
		return sortedValues.get(sortedValues.size() - 1);
	}

	public double getMean() {
		return mean;
	}

	public double getMedian() {
		return getQuantile(0.5);
	}

	public double getStandardDeviation() {
		return standardDeviation;
	}

	/** Return an empirical nearest-rank quantile. */
	public double getQuantile(double probability) {
		if (!(probability > 0) || probability > 1) {
			throw new IllegalArgumentException("Probability must be in the range (0, 1]");
		}
		int index = Math.max(0, (int) Math.ceil(probability * sortedValues.size()) - 1);
		return sortedValues.get(index);
	}
}
