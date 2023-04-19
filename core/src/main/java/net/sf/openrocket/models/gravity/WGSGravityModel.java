package net.sf.openrocket.models.gravity;

import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.WorldCoordinate;

/**
 * A gravity model based on the WGS84 ellipsoid.
 * 
 * @author Richard Graham <richard@rdg.cc>
 */
public class WGSGravityModel implements GravityModel {
	
	// Cache the previously computed value
	private WorldCoordinate lastWorldCoordinate;
	private double lastg;
	
	
	@Override
	public double getGravity(WorldCoordinate wc) {
		
		// This is a proxy method to calcGravity, to avoid repeated calculation
		if (wc != this.lastWorldCoordinate) {
			this.lastg = calcGravity(wc);
			this.lastWorldCoordinate = wc;
		}
		
		return this.lastg;
		
	}
	
	
	@Override
	public int getModID() {
		// The model is immutable, so it can return a constant mod ID
		return 0;
	}
	
	
	private double calcGravity(WorldCoordinate wc) {
		
		double sin2lat = MathUtil.pow2(Math.sin(wc.getLatitudeRad()));
		double g_0 = 9.7803267714 * ((1.0 + 0.00193185138639 * sin2lat) / Math.sqrt(1.0 - 0.00669437999013 * sin2lat));
		
		// Apply correction due to altitude. Note this assumes a spherical earth, but it is a small correction
		// so it probably doesn't really matter. Also does not take into account gravity of the atmosphere, again
		// correction could be done but not really necessary.
		double g_alt = g_0 * MathUtil.pow2(WorldCoordinate.REARTH / (WorldCoordinate.REARTH + wc.getAltitude()));
		
		return g_alt;
	}
	
}
