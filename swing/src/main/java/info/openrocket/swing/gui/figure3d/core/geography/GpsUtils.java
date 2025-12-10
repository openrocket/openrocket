package info.openrocket.swing.gui.figure3d.core.geography;

/**
 * A utility class for GPS and map tile calculations.
 * Uses standard Web Mercator projection calculations.
 */
public class GpsUtils {

	/**
	 * Converts GPS coordinates to the X tile number for a given zoom level.
	 */
	public static int long2tilex(double lon, int zoom) {
		return (int) (Math.floor((lon + 180) / 360 * Math.pow(2, zoom)));
	}

	/**
	 * Converts GPS coordinates to the Y tile number for a given zoom level.
	 */
	public static int lat2tiley(double lat, int zoom) {
		return (int) (Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * Math.pow(2, zoom)));
	}
}