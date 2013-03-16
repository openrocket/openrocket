package net.sf.openrocket.rocketcomponent;

/**
 * FlightConfiguration implementation that prevents changing the default value.
 * This is used for motors, where the default value is always no motor.
 */
public class MotorFlightConfigurationImpl<E extends FlightConfigurableParameter<E>> extends FlightConfigurationImpl<E> {
	
	public MotorFlightConfigurationImpl(RocketComponent component, int eventType, E defaultValue) {
		super(component, eventType, defaultValue);
	}
	
	@Override
	public void setDefault(E value) {
		throw new UnsupportedOperationException("Cannot change default value of motor configuration");
	}
	
}
