package info.openrocket.swing.gui.simulation.currentconditions;

/**
 * A location reported by an operating-system location service.
 */
public record DeviceLocation(double latitude, double longitude, double altitude, double horizontalAccuracy,
		String source) {
}
