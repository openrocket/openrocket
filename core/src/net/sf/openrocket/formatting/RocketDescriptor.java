package net.sf.openrocket.formatting;

import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.Rocket;

/**
 * Interface for formatting a flight configuration into a
 * textual string.
 */
public interface RocketDescriptor {
	
	/**
	 * Return a string describing a particular flight configuration
	 * of the rocket.  This uses the default flight configuration name
	 * as the basis.
	 */
	public String format(Rocket rocket, FlightConfigurationId configId);
	
	
	/**
	 * Return a string describing a particular flight configuration
	 * of the rocket.  This uses a custom-provided name as the basis.
	 */
	public String format(String name, Rocket rocket, FlightConfigurationId configId);
	
}
