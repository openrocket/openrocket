package info.openrocket.core.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import info.openrocket.core.startup.MockPreferences;
import info.openrocket.core.util.BaseTestCase;

/**
 * Tests fixed and per-run random seed behavior in {@link SimulationOptions}.
 */
public class SimulationOptionsRandomSeedTest extends BaseTestCase {

	@Test
	public void testRandomSeedIsNotFixedByDefault() {
		SimulationOptions options = new SimulationOptions();

		assertFalse(options.isRandomSeedFixed());
	}

	@Test
	public void testFixedSeedIsNotRandomizedBeforeRun() {
		SimulationOptions options = new SimulationOptions();
		options.setRandomSeed(12345);
		options.setRandomSeedFixed(true);

		options.randomizeSeedIfNotFixed();

		assertEquals(12345, options.getRandomSeed());
	}

	@Test
	public void testDefaultFactoryAppliesFixedSeedPreference() {
		MockPreferences preferences = new MockPreferences();
		preferences.setRandomSeed(246813579);
		preferences.setRandomSeedFixed(true);
		DefaultSimulationOptionFactory factory = new DefaultSimulationOptionFactory(preferences);

		SimulationOptions options = factory.getDefault();

		assertTrue(options.isRandomSeedFixed());
		assertEquals(246813579, options.getRandomSeed());
	}

	@Test
	public void testCopyConditionsCopiesFixedSeed() {
		SimulationOptions source = new SimulationOptions();
		source.setRandomSeed(-987654321);
		source.setRandomSeedFixed(true);
		SimulationOptions target = new SimulationOptions();

		target.copyConditionsFrom(source);

		assertTrue(target.isRandomSeedFixed());
		assertEquals(-987654321, target.getRandomSeed());
	}

	@Test
	public void testEqualityIncludesOnlyFixedSeeds() {
		SimulationOptions first = new SimulationOptions();
		SimulationOptions second = first.clone();
		second.setRandomSeed(first.getRandomSeed() + 1);

		assertEquals(first, second, "Generated seed values must not make simulations outdated");

		first.setRandomSeedFixed(true);
		second.setRandomSeedFixed(true);
		assertNotEquals(first, second, "Different user-selected seeds must make simulations unequal");

		second.setRandomSeed(first.getRandomSeed());
		assertEquals(first, second);
	}
}
