package info.openrocket.core.simulation.montecarlo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class MonteCarloSamplerTest {
	@Test
	public void testSamplingIsReproducibleFromMasterSeed() {
		MonteCarloSettings settings = MonteCarloSettings.builder()
				.runCount(20)
				.seed(0x5EED)
				.uncertainty(MonteCarloParameter.WIND_SPEED, MonteCarloDistribution.NORMAL, 2.0)
				.uncertainty(MonteCarloParameter.WIND_DIRECTION, MonteCarloDistribution.UNIFORM, 0.5)
				.build();

		MonteCarloSampler first = new MonteCarloSampler(settings);
		MonteCarloSampler repeat = new MonteCarloSampler(settings);
		for (int run = 1; run <= 10; run++) {
			MonteCarloSample firstSample = first.nextSample(run);
			MonteCarloSample repeatSample = repeat.nextSample(run);
			assertEquals(firstSample.getSimulationSeed(), repeatSample.getSimulationSeed());
			assertEquals(firstSample.getVariations(), repeatSample.getVariations());
			assertTrue(Math.abs(firstSample.getVariation(MonteCarloParameter.WIND_DIRECTION)) <= 0.5);
		}
	}

	@Test
	public void testDifferentMasterSeedsProduceDifferentSamples() {
		MonteCarloSettings firstSettings = MonteCarloSettings.builder()
				.runCount(2)
				.seed(1)
				.uncertainty(MonteCarloParameter.TOTAL_MASS, MonteCarloDistribution.NORMAL, 0.05)
				.build();
		MonteCarloSettings secondSettings = MonteCarloSettings.builder()
				.runCount(2)
				.seed(2)
				.uncertainty(MonteCarloParameter.TOTAL_MASS, MonteCarloDistribution.NORMAL, 0.05)
				.build();

		MonteCarloSample first = new MonteCarloSampler(firstSettings).nextSample(1);
		MonteCarloSample second = new MonteCarloSampler(secondSettings).nextSample(1);
		assertNotEquals(first.getVariation(MonteCarloParameter.TOTAL_MASS),
				second.getVariation(MonteCarloParameter.TOTAL_MASS));
	}

	@Test
	public void testSettingsValidateRunCountAndSpread() {
		assertThrows(IllegalArgumentException.class,
				() -> MonteCarloSettings.builder().runCount(1).build());
		assertThrows(IllegalArgumentException.class,
				() -> new UncertaintySpec(MonteCarloDistribution.NORMAL, -0.1));
	}

	/**
	 * Enabling an extra parameter must not disturb the parameters that were already
	 * enabled, otherwise a one-parameter-at-a-time study at a fixed seed is meaningless.
	 */
	@Test
	public void testEnablingAnotherParameterLeavesOtherStreamsUntouched() {
		MonteCarloSettings.Builder base = MonteCarloSettings.builder()
				.runCount(10)
				.seed(4242)
				.uncertainty(MonteCarloParameter.WIND_SPEED, MonteCarloDistribution.NORMAL, 2.0);
		MonteCarloSettings withoutExtra = base.build();
		MonteCarloSettings withExtra = MonteCarloSettings.builder()
				.runCount(10)
				.seed(4242)
				.uncertainty(MonteCarloParameter.WIND_SPEED, MonteCarloDistribution.NORMAL, 2.0)
				.uncertainty(MonteCarloParameter.THRUST, MonteCarloDistribution.UNIFORM, 0.05)
				.build();

		MonteCarloSampler plain = new MonteCarloSampler(withoutExtra);
		MonteCarloSampler extended = new MonteCarloSampler(withExtra);
		for (int run = 1; run <= 10; run++) {
			MonteCarloSample plainSample = plain.nextSample(run);
			MonteCarloSample extendedSample = extended.nextSample(run);
			assertEquals(plainSample.getVariation(MonteCarloParameter.WIND_SPEED),
					extendedSample.getVariation(MonteCarloParameter.WIND_SPEED));
			assertEquals(plainSample.getSimulationSeed(), extendedSample.getSimulationSeed());
			assertNotEquals(0.0, extendedSample.getVariation(MonteCarloParameter.THRUST));
		}
	}

	@Test
	public void testLogNormalKeepsMultiplierPositiveAndIsRestrictedToRelativeParameters() {
		MonteCarloSettings settings = MonteCarloSettings.builder()
				.runCount(500)
				.seed(31337)
				.uncertainty(MonteCarloParameter.RECOVERY_DRAG, MonteCarloDistribution.LOG_NORMAL, 0.30)
				.build();

		MonteCarloSampler sampler = new MonteCarloSampler(settings);
		double logTotal = 0;
		for (int run = 1; run <= 500; run++) {
			double variation = sampler.nextSample(run).getVariation(MonteCarloParameter.RECOVERY_DRAG);
			assertTrue(variation > -1, "log-normal multiplier must stay positive, was 1 + " + variation);
			logTotal += Math.log1p(variation);
		}
		// The log of the multiplier is normal with zero mean, so its sample mean is ~0.
		assertEquals(0.0, logTotal / 500, 0.05);

		assertThrows(IllegalArgumentException.class, () -> MonteCarloSettings.builder()
				.uncertainty(MonteCarloParameter.WIND_DIRECTION, MonteCarloDistribution.LOG_NORMAL, 0.1));
	}
}
