package net.sf.openrocket.rocketcomponent;


/**
 * Interface for a parameter object that can be configured per 
 * flight configuration.
 *
 * @param <E>	the parameter type
 */
public interface FlightConfigurableParameter<E> {
	
	/**
	 * return an exact copy of this object
	 */
	E clone();

    /**
     * return a copy of this object, corresponding to the specified Id
     *
     * @param fcid id to attach the new object to
     * @return the desired copy
     */
    E copy( final FlightConfigurationId fcid );

	void update();
	
}
