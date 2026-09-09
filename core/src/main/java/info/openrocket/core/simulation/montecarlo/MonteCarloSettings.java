package info.openrocket.core.simulation.montecarlo;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable configuration for a landing-dispersion analysis.
 * <p>
 * The master seed controls both uncertainty sampling and the per-flight simulation
 * seed, making a complete analysis reproducible.
 */
public final class MonteCarloSettings {
	public static final int DEFAULT_RUN_COUNT = 500;
	public static final int MIN_RUN_COUNT = 2;
	public static final int MAX_RUN_COUNT = 100_000;

	private final int runCount;
	private final int seed;
	private final int threadCount;
	private final Map<MonteCarloParameter, UncertaintySpec> uncertainties;

	private MonteCarloSettings(Builder builder) {
		if (builder.runCount < MIN_RUN_COUNT || builder.runCount > MAX_RUN_COUNT) {
			throw new IllegalArgumentException("Run count must be between " + MIN_RUN_COUNT
					+ " and " + MAX_RUN_COUNT);
		}
		if (builder.threadCount < 1) {
			throw new IllegalArgumentException("Thread count must be at least one");
		}

		this.runCount = builder.runCount;
		this.seed = builder.seed;
		this.threadCount = builder.threadCount;
		EnumMap<MonteCarloParameter, UncertaintySpec> copy = new EnumMap<>(MonteCarloParameter.class);
		copy.putAll(builder.uncertainties);
		this.uncertainties = Collections.unmodifiableMap(copy);
	}

	public int getRunCount() {
		return runCount;
	}

	public int getSeed() {
		return seed;
	}

	/**
	 * Number of trajectories to simulate concurrently. Sampling and result ordering do not
	 * depend on this value.
	 *
	 * @return the concurrency level for the analysis
	 */
	public int getThreadCount() {
		return threadCount;
	}

	/**
	 * Return the uncertainty for a parameter. Parameters absent from the map are fixed
	 * at their nominal values.
	 *
	 * @param parameter parameter to inspect
	 * @return configured uncertainty, or {@link UncertaintySpec#NONE}
	 */
	public UncertaintySpec getUncertainty(MonteCarloParameter parameter) {
		return uncertainties.getOrDefault(parameter, UncertaintySpec.NONE);
	}

	public Map<MonteCarloParameter, UncertaintySpec> getUncertainties() {
		return uncertainties;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof MonteCarloSettings other)) {
			return false;
		}
		return runCount == other.runCount
				&& seed == other.seed
				&& threadCount == other.threadCount
				&& uncertainties.equals(other.uncertainties);
	}

	@Override
	public int hashCode() {
		return Objects.hash(runCount, seed, threadCount, uncertainties);
	}

	public static Builder builder() {
		return new Builder();
	}

	/** Builder for immutable Monte Carlo settings. */
	public static final class Builder {
		private int runCount = DEFAULT_RUN_COUNT;
		private int seed = (int) System.nanoTime();
		private int threadCount = Runtime.getRuntime().availableProcessors();
		private final EnumMap<MonteCarloParameter, UncertaintySpec> uncertainties =
				new EnumMap<>(MonteCarloParameter.class);

		public Builder runCount(int runCount) {
			this.runCount = runCount;
			return this;
		}

		public Builder seed(int seed) {
			this.seed = seed;
			return this;
		}

		public Builder threadCount(int threadCount) {
			this.threadCount = threadCount;
			return this;
		}

		public Builder uncertainty(MonteCarloParameter parameter, UncertaintySpec uncertainty) {
			if (uncertainty.distribution().requiresRelativeParameter() && !parameter.isRelative()) {
				throw new IllegalArgumentException(uncertainty.distribution()
						+ " can only be applied to a relative parameter, not " + parameter);
			}
			if (uncertainty.spread() == 0) {
				uncertainties.remove(parameter);
			} else {
				uncertainties.put(parameter, uncertainty);
			}
			return this;
		}

		public Builder uncertainty(MonteCarloParameter parameter, MonteCarloDistribution distribution,
				double spread) {
			return uncertainty(parameter, new UncertaintySpec(distribution, spread));
		}

		public MonteCarloSettings build() {
			return new MonteCarloSettings(this);
		}
	}
}
