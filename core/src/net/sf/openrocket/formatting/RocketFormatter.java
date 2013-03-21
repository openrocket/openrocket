package net.sf.openrocket.formatting;

import net.sf.openrocket.rocketcomponent.Rocket;

/**
 * Interface for formatting a flight configuration into a
 * textual string.
 */
public interface RocketFormatter {
	
	/**
	 * Return a string describing a particular flight configuration
	 * of the rocket.
	 */
	public String format(Rocket rocket, String configId);
	
}
