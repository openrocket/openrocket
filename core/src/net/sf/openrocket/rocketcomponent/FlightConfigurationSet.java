package net.sf.openrocket.rocketcomponent;

/**
 * An implementation of FlightConfiguration that fires off events
 * to the rocket components when the parameter value is changed.
 *
 * @param <E>	the parameter type
 */
public class FlightConfigurationSet extends ParameterSet<FlightConfiguration> {
	
	/**
	 * Construct a FlightConfiguration that has no overrides.
	 * 
	 * @param component		the rocket component on which events are fired when the parameter values are changed
	 * @param eventType		the event type that will be fired on changes
	 */
	public FlightConfigurationSet( RocketComponent component, int eventType, FlightConfiguration _defaultValue) {
		super( component, eventType, _defaultValue);
	}
	
	
	/**
	 * Construct a copy of an existing FlightConfigurationSet
	 * 
	 * @param component		the rocket component on which events are fired when the parameter values are changed
	 * @param eventType		the event type that will be fired on changes
	 */
	public FlightConfigurationSet(FlightConfigurationSet configSet, RocketComponent component, int eventType) {
		super( configSet, component, eventType );
	}
			
}
