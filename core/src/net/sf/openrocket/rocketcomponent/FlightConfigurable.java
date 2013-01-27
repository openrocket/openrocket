package net.sf.openrocket.rocketcomponent;

/**
 * An interface defining that specific parameters of a component are
 * configurable on a per-flight-configuration basis.
 * 
 * TODO:  This should be refactored and redesigned.  In the current
 * implementation there can be only one object that is overridable,
 * while it should be possible to have independent variables being
 * overridden.  For example:
 *  - motor selection and ignition selection
 *  - parachute diameter and parachute deployment
 *
 * @param <T>  The configuration variable type
 */
public interface FlightConfigurable<T> {
	
	/**
	 * Return the parameters for the provided flight configuration, or
	 * <code>null</code> if they are not overridden.
	 * 
	 * @param configId	the flight configuration ID
	 * @return			the overridden parameters or <code>null</code>
	 */
	public T getFlightConfiguration(String configId);
	
	/**
	 * Set or removing the overriding parameters for a specific flight configuration.
	 * If the configuration is <code>null</code> the overriding parameters are removed.
	 * 
	 * @param configId	the flight configuration ID.
	 * @param config	the configuration which will override the default values.
	 */
	public void setFlightConfiguration(String configId, T config);
	
	/**
	 * Clone a flight configuration to a new flight configuration ID.
	 * This functions also in case there is no overridden values for the
	 * configuration ID.
	 * 
	 * @param oldConfigId	the old configuration ID
	 * @param newConfigId	the new configuration ID
	 */
	public void cloneFlightConfiguration(String oldConfigId, String newConfigId);
	
	/**
	 * Return the default flight configuration to use if the values are not
	 * overridden.
	 * 
	 * @return	the default configuration (never <code>null</code>)
	 */
	public T getDefaultFlightConfiguration();
	
	/**
	 * Set the default flight configuration to use if the values are not overridden.
	 * 
	 * @param config	the default configuration (must not be <code>null</code>)
	 */
	public void setDefaultFlightConfiguration(T config);
	
}
