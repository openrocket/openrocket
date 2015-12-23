package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.util.ChangeSource;

/**
 * Interface for a parameter object that can be configured per 
 * flight configuration.
 *
 * @param <E>	the parameter type
 */
public interface FlightConfigurableParameter<E> {
	
	/**
	 * Return a copy of this object.  The listeners must not be copied
	 * to the new object.
	 */
	public E clone();
	
	public void update();
	
}
