package info.openrocket.swing.gui.simulation.currentconditions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;

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

	@Test
	void calendarStartsOnTheLocalesFirstWeekday() {
		assertEquals(DayOfWeek.SUNDAY, ForecastDateTimePicker.orderedWeekdays(Locale.US).get(0));
		assertEquals(DayOfWeek.MONDAY, ForecastDateTimePicker.orderedWeekdays(Locale.GERMANY).get(0));
	}

	@Test
	void currentDateIncludesHoursBeforeTheCurrentTime() {
		LocalDate date = LocalDate.of(2026, 9, 6);
		Instant minimum = date.atStartOfDay(LOS_ANGELES).toInstant();
		Instant maximum = date.plusDays(1).atStartOfDay(LOS_ANGELES).minusHours(1).toInstant();
		Instant morningHour = date.atTime(8, 0).atZone(LOS_ANGELES).toInstant();

		assertTrue(ForecastDateTimePicker.selectableHourlyInstants(date, LOS_ANGELES, minimum, maximum)
				.contains(morningHour));
	}
}
