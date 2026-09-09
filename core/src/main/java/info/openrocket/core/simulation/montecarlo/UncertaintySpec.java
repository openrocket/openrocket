package info.openrocket.core.simulation.montecarlo;

import java.util.Objects;

/**
 * Distribution and spread used to sample one uncertain parameter.
 *
 * @param distribution probability distribution to sample
 * @param spread one standard deviation for a normal distribution, the symmetric half-range
 *        for a uniform distribution, or the standard deviation of the log-multiplier for
 *        a log-normal distribution
 */
public record UncertaintySpec(MonteCarloDistribution distribution, double spread) {
	public static final UncertaintySpec NONE = new UncertaintySpec(MonteCarloDistribution.NORMAL, 0);

	public UncertaintySpec {
		Objects.requireNonNull(distribution, "distribution");
		if (!Double.isFinite(spread) || spread < 0) {
			throw new IllegalArgumentException("Uncertainty spread must be finite and non-negative");
		}
	}
}
