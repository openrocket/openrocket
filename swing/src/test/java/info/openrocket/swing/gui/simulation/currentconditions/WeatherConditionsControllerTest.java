package info.openrocket.swing.gui.simulation.currentconditions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import javax.swing.JCheckBox;

import org.junit.jupiter.api.Test;

import com.formdev.flatlaf.FlatClientProperties;

class WeatherConditionsControllerTest {
	@Test
	void turbulenceIntensityComesFromTheFirstMovingWindLayer() {
		List<CurrentConditions.WindLayer> layers = List.of(
				new CurrentConditions.WindLayer(10, 0, 0, 0),
				new CurrentConditions.WindLayer(80, 12, 0, 1.8));

		assertEquals(0.15, WeatherConditionsController.turbulenceIntensityFor(layers), 0.000_001);
	}

	@Test
	void previewCrossesOutFieldsThatWillNotBeApplied() {
		assertEquals("Pressure: 12.3 hPa",
				WeatherConditionsController.formatPreviewField(true, "Pressure: 12.3 hPa"));
		assertEquals("<font color='#808080'><strike>Pressure: 12.3 hPa</strike></font>",
				WeatherConditionsController.formatPreviewField(false, "Pressure: 12.3 hPa"));
	}

	@Test
	void windImportCheckboxReflectsAllMixedAndNoneSelected() {
		JCheckBox checkbox = new JCheckBox();

		WeatherConditionsController.updateWindImportCheckbox(checkbox, 4, Set.of());
		assertTrue(checkbox.isSelected());
		assertNull(checkbox.getClientProperty(FlatClientProperties.SELECTED_STATE));

		WeatherConditionsController.updateWindImportCheckbox(checkbox, 4, Set.of(1, 3));
		assertTrue(checkbox.isSelected());
		assertEquals(FlatClientProperties.SELECTED_STATE_INDETERMINATE,
				checkbox.getClientProperty(FlatClientProperties.SELECTED_STATE));

		WeatherConditionsController.updateWindImportCheckbox(checkbox, 4, Set.of(0, 1, 2, 3));
		assertFalse(checkbox.isSelected());
		assertNull(checkbox.getClientProperty(FlatClientProperties.SELECTED_STATE));
	}
}
