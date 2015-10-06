package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.motor.MotorInstance;

/**
 * FlightConfigurationSet for motors.
 * This is used for motors, where the default value is always no motor.
 */
public class MotorConfigurationSet extends FlightConfigurationSet<MotorInstance> {
	
	public static final int DEFAULT_EVENT_TYPE = ComponentChangeEvent.MOTOR_CHANGE | ComponentChangeEvent.EVENT_CHANGE;
	
	public MotorConfigurationSet(RocketComponent component, MotorInstance _value) {
		super(component, DEFAULT_EVENT_TYPE, _value);
	}
	
	/**
	 * Construct a copy of an existing FlightConfigurationSet.
	 * 
	 * @param flightConfiguration another flightConfiguration to copy data from.
	 * @param component		the rocket component on which events are fired when the parameter values are changed
	 * @param eventType		the event type that will be fired on changes
	 */
	public MotorConfigurationSet(FlightConfigurationSet<MotorInstance> flightConfiguration, RocketComponent component, int eventType) {
		super(flightConfiguration, component, eventType);
	}
	
	
	@Override
	public void setDefault(MotorInstance value) {
		throw new UnsupportedOperationException("Cannot change default value of motor configuration");
	}
	
}
