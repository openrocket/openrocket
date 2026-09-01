package info.openrocket.swing.gui.simulation.currentconditions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;
import java.util.prefs.Preferences;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class WeatherCustomizationPreferencesTest {
	private final Preferences node = Preferences.userRoot().node("openrocket-test/" + UUID.randomUUID());

	@AfterEach
	void cleanUp() throws Exception {
		node.removeNode();
	}

	@Test
	void fieldSettingsAreEmptyUntilExplicitlySaved() {
		assertTrue(new WeatherCustomizationPreferences(node).loadFieldSettings().isEmpty());
	}

	@Test
	void savesAndLoadsFieldSettings() {
		WeatherCustomizationPreferences repository = new WeatherCustomizationPreferences(node);
		WeatherCustomizationPreferences.FieldSettings expected =
				new WeatherCustomizationPreferences.FieldSettings(false, true, false, true, false, true, true);

		repository.saveFieldSettings(expected);

		assertEquals(expected, repository.loadFieldSettings().orElseThrow());
	}

	@Test
	void savesExcludedWindLevelsBySortedZeroBasedPosition() {
		WeatherCustomizationPreferences repository = new WeatherCustomizationPreferences(node);

		repository.saveExcludedWindLevelIndices(Set.of(7, 1, 4));

		assertEquals(Set.of(1, 4, 7), repository.loadExcludedWindLevelIndices());
		assertEquals("1,4,7", node.get("excludedWindLevelIndices", ""));
	}

	@Test
	void ignoresInvalidSavedWindLevelPositions() {
		node.put("excludedWindLevelIndices", "2,-1,nope,5");

		assertEquals(Set.of(2, 5),
				new WeatherCustomizationPreferences(node).loadExcludedWindLevelIndices());
	}
}
