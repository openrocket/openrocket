package net.sf.openrocket.rocketcomponent;

import java.util.HashMap;

import net.sf.openrocket.motor.Motor;

public class BaseMotorMount implements SupportsFlightConfiguration<MotorConfiguration>, Cloneable {

	private HashMap<String, MotorConfiguration > motors = new HashMap<String,MotorConfiguration>();
	private MotorConfiguration defaultConfiguration = new MotorConfiguration();
	
	@Override
	public MotorConfiguration getFlightConfiguration(String configId) {
		return motors.get(configId);
	}

	@Override
	public void setFlightConfiguration(String configId,	MotorConfiguration config) {
		if ( config == null ) {
			motors.remove(configId);
		} else {
			motors.put(configId,  config);
		}
	}

	@Override
	public void cloneFlightConfiguration(String oldConfigId, String newConfigId) {
		MotorConfiguration oldConfig = getFlightConfiguration(oldConfigId);
		setFlightConfiguration(newConfigId, oldConfig.clone());
	}

	@Override
	public MotorConfiguration getDefaultFlightConfiguration() {
		return defaultConfiguration;
	}

	@Override
	public void setDefaultFlightConfiguration(MotorConfiguration config) {
		defaultConfiguration = config;
	}

	public Motor getMotor(String id) {
		if (id == null)
			return null;
		
		MotorConfiguration motorConfig =getFlightConfiguration(id);
		if ( motorConfig == null ) {
			return null;
		}
		return motorConfig.getMotor();
	}
	
	/**
	 * Change the motor used in this mount for the given flight configuration.
	 * 
	 * @param id
	 * @param motor
	 * @return true if the new motor is different from the old motor.
	 */
	public boolean setMotor(String id, Motor motor) {
		if (id == null) {
			if (motor != null) {
				throw new IllegalArgumentException("Cannot set non-null motor for id null");
			}
		}
		if ( motor == null ) {
			setFlightConfiguration(id, null);
		}
		MotorConfiguration current = getFlightConfiguration(id);
		if ( current == null ) {
			current = new MotorConfiguration();
			setFlightConfiguration(id, current);
		}
		Motor currentMotor = current.getMotor();
		if (motor.equals(currentMotor)) {
			return false;
		}
		current.setMotor(motor);
		return true;
	}
	
	public double getMotorDelay(String id) {
		MotorConfiguration current = getFlightConfiguration(id);
		Double delay = ( current == null ) ? null : current.getEjectionDelay();
		if (delay == null)
			return Motor.PLUGGED;
		return delay;
	}
	
	/**
	 * Change the motor ejection delay for the given flight configuration
	 * 
	 * @param id
	 * @param delay
	 * @return true if the new value for the delay is different from the old
	 */
	public boolean setMotorDelay(String id, double delay) {
		MotorConfiguration current = getFlightConfiguration(id);
		if ( current == null ) {
			current = new MotorConfiguration();
			setFlightConfiguration(id, current);
		}
		if ( current.getEjectionDelay() != delay ) {
			current.setEjectionDelay(delay);
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected BaseMotorMount clone() {
		BaseMotorMount clone = new BaseMotorMount();
		clone.defaultConfiguration = defaultConfiguration.clone();
		clone.motors = (HashMap<String,MotorConfiguration>) motors.clone();
		return clone;
	}

}
