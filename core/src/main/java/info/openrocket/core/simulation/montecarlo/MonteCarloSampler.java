package info.openrocket.core.simulation.montecarlo;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

/**
 * Deterministically produces independent samples.
 * <p>
 * Each parameter and the per-flight simulation seed use separate random streams derived
 * from the master seed. Changing one parameter therefore does not shift other samples.
 */
public final class MonteCarloSampler {
	private final MonteCarloSettings settings;
	private final Map<MonteCarloParameter, Random> parameterStreams;
	private final Random simulationSeedStream;

	public MonteCarloSampler(MonteCarloSettings settings) {
		this.settings = settings;

		Random seedSource = new Random(settings.getSeed());
		EnumMap<MonteCarloParameter, Random> streams = new EnumMap<>(MonteCarloParameter.class);
		for (MonteCarloParameter parameter : MonteCarloParameter.values()) {
			streams.put(parameter, new Random(seedSource.nextLong()));
		}
		this.parameterStreams = Collections.unmodifiableMap(streams);
		this.simulationSeedStream = new Random(seedSource.nextLong());
	}

	public MonteCarloSample nextSample(int runNumber) {
		if (runNumber < 1) {
			throw new IllegalArgumentException("Monte Carlo run numbers start at one");
		}

		EnumMap<MonteCarloParameter, Double> values = new EnumMap<>(MonteCarloParameter.class);
		for (MonteCarloParameter parameter : MonteCarloParameter.values()) {
			UncertaintySpec uncertainty = settings.getUncertainty(parameter);
			double variation = uncertainty.distribution().sample(parameterStreams.get(parameter),
					uncertainty.spread());
			values.put(parameter, variation);
		}
		return new MonteCarloSample(runNumber, simulationSeedStream.nextInt(), values);
	}
}
