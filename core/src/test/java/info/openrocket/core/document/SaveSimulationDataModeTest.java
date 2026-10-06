package info.openrocket.core.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import info.openrocket.core.util.BaseTestCase;

/**
 * Tests how the saved simulation data choice is applied to the storage options of a design.
 */
public class SaveSimulationDataModeTest extends BaseTestCase {

	/**
	 * Asking leaves the options untouched so the save dialog can ask the user.
	 */
	@Test
	public void testAskDoesNotChangeOptions() {
		StorageOptions options = new StorageOptions();
		options.setSaveSimulationData(true);

		assertFalse(SaveSimulationDataMode.ASK.applyTo(options));

		assertTrue(options.getSaveSimulationData());
		assertFalse(options.isExplicitlySet());
	}

	@Test
	public void testAllDataStoresSimulationData() {
		StorageOptions options = new StorageOptions();

		assertTrue(SaveSimulationDataMode.ALL_DATA.applyTo(options));

		assertTrue(options.getSaveSimulationData());
		assertTrue(options.isExplicitlySet());
	}

	/**
	 * A fixed choice overrides the data storage of a loaded design, which stores all data if the file had it.
	 */
	@Test
	public void testSummaryOnlyOverridesLoadedChoice() {
		StorageOptions options = new StorageOptions();
		options.setSaveSimulationData(true);

		assertTrue(SaveSimulationDataMode.SUMMARY_ONLY.applyTo(options));

		assertFalse(options.getSaveSimulationData());
		assertTrue(options.isExplicitlySet());
	}

	@Test
	public void testModeOfChoice() {
		assertEquals(SaveSimulationDataMode.ALL_DATA, SaveSimulationDataMode.of(true));
		assertEquals(SaveSimulationDataMode.SUMMARY_ONLY, SaveSimulationDataMode.of(false));
	}
}
