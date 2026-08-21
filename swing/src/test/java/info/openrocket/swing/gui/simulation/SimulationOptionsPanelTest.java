package info.openrocket.swing.gui.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.swing.util.BaseTestCase;

/**
 * Tests validation of the user-editable random seed field.
 */
public class SimulationOptionsPanelTest extends BaseTestCase {

	@Test
	public void testValidSignedIntegerEnablesFixedSeed() {
		SimulationOptions options = new SimulationOptions();

		boolean fixed = SimulationOptionsPanel.applyRandomSeedInput(options, true, " -2147483648 ");

		assertTrue(fixed);
		assertTrue(options.isRandomSeedFixed());
		assertEquals(Integer.MIN_VALUE, options.getRandomSeed());
	}

	@Test
	public void testBlankSeedFallsBackToRandomRuns() {
		SimulationOptions options = fixedOptions();

		boolean fixed = SimulationOptionsPanel.applyRandomSeedInput(options, true, "  ");

		assertFalse(fixed);
		assertFalse(options.isRandomSeedFixed());
	}

	@Test
	public void testOutOfRangeSeedFallsBackToRandomRuns() {
		SimulationOptions options = fixedOptions();

		boolean fixed = SimulationOptionsPanel.applyRandomSeedInput(options, true, "2147483648");

		assertFalse(fixed);
		assertFalse(options.isRandomSeedFixed());
	}

	@Test
	public void testUncheckedSeedUsesRandomRuns() {
		SimulationOptions options = fixedOptions();

		boolean fixed = SimulationOptionsPanel.applyRandomSeedInput(options, false, "12345");

		assertFalse(fixed);
		assertFalse(options.isRandomSeedFixed());
	}

	private SimulationOptions fixedOptions() {
		SimulationOptions options = new SimulationOptions();
		options.setRandomSeed(12345);
		options.setRandomSeedFixed(true);
		return options;
	}
}
