package info.openrocket.core.simulation.montecarlo;

import java.util.Random;

/**
 * Probability distributions supported by the landing-dispersion sampler.
 */
public enum MonteCarloDistribution {
	/** The configured spread is one standard deviation. */
	NORMAL,

	/** The configured spread is the symmetric half-range around the nominal value. */
	UNIFORM,

	/**
	 * Multiplicative distribution for quantities that cannot become negative, such as a
	 * drag or thrust multiplier. The configured spread is the standard deviation of the
	 * natural logarithm of the multiplier, so the multiplier stays strictly positive and
	 * its median is one. Unlike the symmetric distributions the mean is slightly above
	 * the nominal value, which is inherent to a log-normal and not a sampling error.
	 */
	LOG_NORMAL;

	/**
	 * Draw a zero-centered variation from this distribution.
	 *
	 * @param random random source for the analysis
	 * @param spread one standard deviation for {@link #NORMAL}, the half-range for
	 *        {@link #UNIFORM}, or the standard deviation of the log-multiplier for
	 *        {@link #LOG_NORMAL}
	 * @return sampled variation around the nominal value
	 */
	public double sample(Random random, double spread) {
		if (spread == 0) {
			return 0;
		}

		return switch (this) {
			case NORMAL -> random.nextGaussian() * spread;
			case UNIFORM -> (2 * random.nextDouble() - 1) * spread;
			// expm1 keeps the result above -1, so 1 + variation is always positive.
			case LOG_NORMAL -> Math.expm1(random.nextGaussian() * spread);
		};
	}

	/**
	 * Whether this distribution may only be applied to {@linkplain MonteCarloParameter#isRelative()
	 * relative} parameters. A multiplicative distribution is meaningless for an additive
	 * offset such as a wind direction or a deployment delay.
	 *
	 * @return {@code true} if this distribution requires a relative parameter
	 */
	public boolean requiresRelativeParameter() {
		return this == LOG_NORMAL;
	}
}
