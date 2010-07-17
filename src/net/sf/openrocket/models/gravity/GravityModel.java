package net.sf.openrocket.models.gravity;

import net.sf.openrocket.util.Monitorable;

/**
 * An interface to modelling gravitational acceleration.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface GravityModel extends Monitorable {

	/**
	 * Compute the gravity at a specific altitude.
	 * 
	 * @param altitude	the altitude at which to compute the gravity
	 * @return			the gravitational acceleration
	 */
	public double getGravity(double altitude);
	
}
