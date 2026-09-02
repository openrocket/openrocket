package info.openrocket.swing.gui.simulation.currentconditions;

/** A launch location selected from the device, map, configured site, or saved pads. */
public record DeviceLocation(double latitude, double longitude, double altitude, double horizontalAccuracy,
		String source, String timezoneId) {
	public DeviceLocation(double latitude, double longitude, double altitude, double horizontalAccuracy, String source) {
		this(latitude, longitude, altitude, horizontalAccuracy, source, null);
	}

	public DeviceLocation withTimezone(String timezone) {
		return new DeviceLocation(latitude, longitude, altitude, horizontalAccuracy, source, timezone);
	}
}
