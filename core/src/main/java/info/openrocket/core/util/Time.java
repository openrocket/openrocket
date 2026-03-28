package info.openrocket.core.util;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Time {
	/**
	 * Returns the current date and time in UTC as a long in the format YYYYMMDDHHMMSS.
	 *
	 * @return the current date time in UTC as a long in the format YYYYMMDDHHMMSS
	 */
	public static long getCurrentDateTimeUTC() {
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		return Long.parseLong(
				ZonedDateTime.now(ZoneOffset.UTC).format(fmt)
		);
	}
}
