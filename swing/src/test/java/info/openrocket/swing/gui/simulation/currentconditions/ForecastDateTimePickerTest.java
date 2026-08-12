package info.openrocket.swing.gui.simulation.currentconditions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

class ForecastDateTimePickerTest {
	private static final ZoneId LOS_ANGELES = ZoneId.of("America/Los_Angeles");

	@Test
	void springDaySkipsTheNonexistentHour() {
		assertEquals(23, ForecastDateTimePicker.hourlyInstants(LocalDate.of(2026, 3, 8), LOS_ANGELES).size());
	}

	@Test
	void fallDayIncludesBothRepeatedHours() {
		assertEquals(25, ForecastDateTimePicker.hourlyInstants(LocalDate.of(2026, 11, 1), LOS_ANGELES).size());
	}
}
