package net.sf.openrocket.util;

/**
 * A WorldCoordinate contains the latitude, longitude and altitude position of a rocket.
 */
public class WorldCoordinate {
	
	/** Mean Earth radius */
	public static final double REARTH = 6371000.0;
	/** Sidearial Earth rotation rate  */
	public static final double EROT = 7.2921150e-5;
	

	private final double lat, lon, alt;
	
	/**
	 * Constructs a new WorldCoordinate
	 * 
	 * @param lat latitude in degrees north. From -90 to 90, values outside are clamped.
	 * @param lon longitude in degrees east. From -180 to 180, values outside are reduced to the range.
	 * @param alt altitude in meters. Unbounded.
	 */
	public WorldCoordinate(double lat, double lon, double alt) {
		this.lat = MathUtil.clamp(Math.toRadians(lat), -Math.PI / 2, Math.PI / 2);
		this.lon = MathUtil.reduce180(Math.toRadians(lon));
		this.alt = alt;
	}
	
	
	/**
	 * Returns the altitude.
	 */
	public double getAltitude() {
		return this.alt;
	}
	
	/**
	 * Returns Longitude in radians
	 */
	public double getLongitudeRad() {
		return this.lon;
	}
	
	/**
	 * Returns Longitude in degrees
	 */
	public double getLongitudeDeg() {
		return Math.toDegrees(this.lon);
	}
	
	/**
	 * Returns latitude in radians
	 */
	public double getLatitudeRad() {
		return this.lat;
	}
	
	/**
	 * Returns latitude in degrees
	 */
	public double getLatitudeDeg() {
		return Math.toDegrees(this.lat);
	}
	
	

	@Override
	public String toString() {
		return "WorldCoordinate[lat=" + getLatitudeDeg() + ", lon=" + getLongitudeDeg() + ", alt=" + getAltitude() + "]";
	}
	
	

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof WorldCoordinate)) {
			return false;
		}
		WorldCoordinate other = (WorldCoordinate) obj;
		return (MathUtil.equals(this.lat, other.lat) &&
				MathUtil.equals(this.lon, other.lon) && MathUtil.equals(this.alt, other.alt));
	}
	
	@Override
	public int hashCode() {
		return ((int) (1000 * (lat + lon + alt)));
	}
	
}
