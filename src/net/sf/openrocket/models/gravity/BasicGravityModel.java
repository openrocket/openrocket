package net.sf.openrocket.models.gravity;

import net.sf.openrocket.util.WorldCoordinate;

@Deprecated

/**
 * A gravity model based on the International Gravity Formula of 1967.  The gravity
 * value is computed when the object is constructed and later returned as a static
 * value.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class BasicGravityModel implements GravityModel {
	
	private final double g;
	
	/**
	 * Construct the static gravity model at the specific latitude (in degrees).
	 * @param latitude	the latitude in degrees (-90 ... 90)
	 */
	public BasicGravityModel(double latitude) {
		// TODO: HIGH: This model is wrong!!  Increases monotonically from -90 to 90
		double sin = Math.sin(latitude * Math.PI / 180);
		double sin2 = Math.sin(2 * latitude * Math.PI / 180);
		g = 9.780327 * (1 + 0.0053024 * sin - 0.0000058 * sin2);
	}
	
	//@Override
	public double getGravity(double altitude) {
		return g;
	}
	
	//@Override
	public int getModID() {
		// Return constant mod ID
		return (int) (g * 1000000);
	}

	@Override
	public double getGravity(WorldCoordinate wc) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
