package net.sf.openrocket.rocketcomponent;

import java.util.EventObject;
import java.util.List;

import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.StateChangeListener;
import net.sf.openrocket.util.Utils;

/**
 * A single motor configuration.  This includes the selected motor
 * and the ejection charge delay.
 */
public class MotorConfiguration implements FlightConfigurableParameter<MotorConfiguration> {
	
	/** Immutable configuration with no motor and zero delay. */
	public static final MotorConfiguration NO_MOTORS = new MotorConfiguration() {
		@Override
		public void setMotor(Motor motor) {
			throw new UnsupportedOperationException("Trying to modify immutable no-motors configuration");
		};
		
		@Override
		public void setEjectionDelay(double delay) {
			throw new UnsupportedOperationException("Trying to modify immutable no-motors configuration");
		};
	};
	
	private final List<StateChangeListener> listeners = new ArrayList<StateChangeListener>();
	
	private Motor motor;
	private double ejectionDelay;
	
	
	public Motor getMotor() {
		return motor;
	}
	
	public void setMotor(Motor motor) {
		if (Utils.equals(this.motor, motor)) {
			return;
		}
		this.motor = motor;
		fireChangeEvent();
	}
	
	public double getEjectionDelay() {
		return ejectionDelay;
	}
	
	public void setEjectionDelay(double delay) {
		if (MathUtil.equals(ejectionDelay, delay)) {
			return;
		}
		this.ejectionDelay = delay;
		fireChangeEvent();
	}
	
	
	@Override
	public MotorConfiguration clone() {
		MotorConfiguration copy = new MotorConfiguration();
		copy.motor = this.motor;
		copy.ejectionDelay = this.ejectionDelay;
		return copy;
	}
	
	
	@Override
	public void addChangeListener(StateChangeListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public void removeChangeListener(StateChangeListener listener) {
		listeners.remove(listener);
	}
	
	private void fireChangeEvent() {
		EventObject event = new EventObject(this);
		Object[] list = listeners.toArray();
		for (Object l : list) {
			((StateChangeListener) l).stateChanged(event);
		}
	}
	
}
