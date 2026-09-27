package info.openrocket.core.simulation.montecarlo;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * One reproducible set of sampled deviations from a nominal simulation.
 */
public final class MonteCarloSample {
	private final int runNumber;
	private final int simulationSeed;
	private final Map<MonteCarloParameter, Double> variations;

	MonteCarloSample(int runNumber, int simulationSeed, Map<MonteCarloParameter, Double> variations) {
		this.runNumber = runNumber;
		this.simulationSeed = simulationSeed;
		EnumMap<MonteCarloParameter, Double> copy = new EnumMap<>(MonteCarloParameter.class);
		copy.putAll(variations);
		this.variations = Collections.unmodifiableMap(copy);
	}

	static MonteCarloSample nominal(int simulationSeed) {
		return new MonteCarloSample(0, simulationSeed, Collections.emptyMap());
	}

	public int getRunNumber() {
		return runNumber;
	}

	public int getSimulationSeed() {
		return simulationSeed;
	}

	public double getVariation(MonteCarloParameter parameter) {
		return variations.getOrDefault(parameter, 0.0);
	}

	public Map<MonteCarloParameter, Double> getVariations() {
		return variations;
	}
}
