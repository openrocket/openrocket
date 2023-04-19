package net.sf.openrocket.models.gravity;

import net.sf.openrocket.util.Monitorable;
import net.sf.openrocket.util.WorldCoordinate;

/**
 * An interface for modeling gravitational acceleration.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface GravityModel extends Monitorable {
	
	/**
	 * Compute the gravitational acceleration at a given world coordinate
	 * 
	 * @param wc	the world coordinate location
	 * @return		gravitational acceleration in m/s/s
	 */
	public double getGravity(WorldCoordinate wc);
	
}
