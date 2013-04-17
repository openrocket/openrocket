package net.sf.openrocket.rocketcomponent;

/**
 * An interface implemented by components supporting
 * per flight configuration configurable parameters.
 */
public interface FlightConfigurableComponent {
	
	/**
	 * Clone a flight configuration to a new flight configuration ID.
	 * This functions also in case there is no overridden values for the
	 * configuration ID.
	 * 
	 * @param oldConfigId	the old configuration ID
	 * @param newConfigId	the new configuration ID
	 */
	public void cloneFlightConfiguration(String oldConfigId, String newConfigId);
	
}
