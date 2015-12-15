package net.sf.openrocket.rocketcomponent;

import java.util.EventObject;
import java.util.List;

import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.StateChangeListener;

public class MotorConfiguration implements FlightConfigurableParameter<MotorConfiguration> {
	
	protected Coordinate position = Coordinate.ZERO;
	protected double ejectionDelay = 0.0;
	protected Motor motor = null;
	
	private final List<StateChangeListener> listeners = new ArrayList<StateChangeListener>();
	
	public Coordinate getPosition() {
		return position;
	}
	
	public void setPosition(Coordinate position) {
		this.position = position;
		fireChangeEvent();
	}
	
	public double getEjectionDelay() {
		return ejectionDelay;
	}
	
	public void setEjectionDelay(double ejectionDelay) {
		this.ejectionDelay = ejectionDelay;
		fireChangeEvent();
	}
	
	public Motor getMotor() {
		return motor;
	}
	
	public void setMotor(Motor motor) {
		this.motor = motor;
		fireChangeEvent();
	}
	
	@Override
	public MotorConfiguration clone() {
		MotorConfiguration clone = new MotorConfiguration();
		clone.position = this.position;
		clone.ejectionDelay = this.ejectionDelay;
		return clone;
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
