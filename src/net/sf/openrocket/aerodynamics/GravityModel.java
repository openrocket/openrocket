package net.sf.openrocket.aerodynamics;

/**
 * A gravity model based on the International Gravity Formula of 1967.  The gravity
 * value is computed when the object is constructed and later returned as a static
 * value.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class GravityModel {
	
	private final double g;
	
	/**
	 * Construct the static gravity model at the specific latitude (in degrees).
	 * @param latitude	the latitude in degrees (-90 ... 90)
	 */
	public GravityModel(double latitude) {
		double sin = Math.sin(latitude * Math.PI/180);
		double sin2 = Math.sin(2 * latitude * Math.PI/180);
		g = 9.780327 * (1 + 0.0053024 * sin - 0.0000058 * sin2);
	}

	public double getGravity() {
		return g;
	}

}
