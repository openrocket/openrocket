package net.sf.openrocket.rocketcomponent;

import java.util.EventObject;
import java.util.List;

import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.StateChangeListener;

public class IgnitionConfiguration implements FlightConfigurableParameter<IgnitionConfiguration> {

	protected double ignitionDelay = 0.0;
	protected IgnitionEvent ignitionEvent = IgnitionEvent.NEVER;
	protected double ignitionTime = 0.0;

	private final List<StateChangeListener> listeners = new ArrayList<StateChangeListener>();

	public double getIgnitionDelay() {
		return ignitionDelay;
	}

	public void setIgnitionDelay(double ignitionDelay) {
		this.ignitionDelay = ignitionDelay;
		fireChangeEvent();
	}

	public IgnitionEvent getIgnitionEvent() {
		return ignitionEvent;
	}

	public void setIgnitionEvent(IgnitionEvent ignitionEvent) {
		this.ignitionEvent = ignitionEvent;
		fireChangeEvent();
	}

	public double getIgnitionTime() {
		return ignitionTime;
	}

	public void setIgnitionTime(double ignitionTime) {
		this.ignitionTime = ignitionTime;
		fireChangeEvent();
	}

	@Override
	public IgnitionConfiguration clone() {
		IgnitionConfiguration clone = new IgnitionConfiguration();
		clone.ignitionDelay = this.ignitionDelay;
		clone.ignitionEvent = this.ignitionEvent;
		clone.ignitionTime = this.ignitionTime;
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
	

	@Override
	public void update(){
	}

	
}
