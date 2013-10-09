package net.sf.openrocket.rocketcomponent;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;

import net.sf.openrocket.util.StateChangeListener;
import net.sf.openrocket.util.Utils;

/**
 * An implementation of FlightConfiguration that fires off events
 * to the rocket components when the parameter value is changed.
 *
 * @param <E>	the parameter type
 */
class FlightConfigurationImpl<E extends FlightConfigurableParameter<E>> implements FlightConfiguration<E> {
	
	private final HashMap<String, E> map = new HashMap<String, E>();
	private E defaultValue = null;
	
	private final RocketComponent component;
	private final int eventType;
	
	private final Listener listener = new Listener();
	
	
	/**
	 * Construct a FlightConfiguration that has no overrides.
	 * 
	 * @param component		the rocket component on which events are fired when the parameter values are changed
	 * @param eventType		the event type that will be fired on changes
	 * @param defaultValue	the default value (null not allowed)
	 */
	public FlightConfigurationImpl(RocketComponent component, int eventType, E defaultValue) {
		this.component = component;
		this.eventType = eventType;
		this.defaultValue = defaultValue;
		
		if (defaultValue == null) {
			throw new NullPointerException("defaultValue is null");
		}
		add(defaultValue);
	}
	
	
	/**
	 * Construct a copy of an existing FlightConfigurationImpl.
	 * 
	 * @param component		the rocket component on which events are fired when the parameter values are changed
	 * @param eventType		the event type that will be fired on changes
	 */
	public FlightConfigurationImpl(FlightConfigurationImpl<E> flightConfiguration, RocketComponent component, int eventType) {
		this.component = component;
		this.eventType = eventType;
		
		this.defaultValue = flightConfiguration.defaultValue.clone();
		for (String key : flightConfiguration.map.keySet()) {
			this.map.put(key, flightConfiguration.map.get(key).clone());
		}
	}
	
	
	
	@Override
	public E getDefault() {
		return defaultValue;
	}
	
	@Override
	public void setDefault(E value) {
		if (value == null) {
			throw new NullPointerException("value is null");
		}
		if (Utils.equals(this.defaultValue, value)) {
			return;
		}
		remove(this.defaultValue);
		this.defaultValue = value;
		add(value);
		fireEvent();
	}
	
	@Override
	public Iterator<E> iterator() {
		return map.values().iterator();
	}
	
	
	@Override
	public int size() {
		return map.size();
	}
	
	
	@Override
	public E get(String id) {
		if (map.containsKey(id)) {
			return map.get(id);
		} else {
			return defaultValue;
		}
	}
	
	@Override
	public void set(String id, E value) {
		if (value == null) {
			throw new NullPointerException("value is null");
		}
		E previous = map.put(id, value);
		remove(previous);
		add(value);
		fireEvent();
	}
	
	@Override
	public boolean isDefault(String id) {
		return !map.containsKey(id);
	}
	
	@Override
	public void resetDefault(String id) {
		E previous = map.remove(id);
		remove(previous);
		fireEvent();
	}
	
	
	
	private void fireEvent() {
		component.fireComponentChangeEvent(eventType);
	}
	
	
	@Override
	public void cloneFlightConfiguration(String oldConfigId, String newConfigId) {
		if (isDefault(oldConfigId)) {
			this.resetDefault(newConfigId);
		} else {
			E original = this.get(oldConfigId);
			this.set(newConfigId, original.clone());
		}
	}
	
	
	
	private void add(E value) {
		if (value != null) {
			value.addChangeListener(listener);
		}
	}
	
	
	private void remove(E value) {
		if (value != null) {
			value.removeChangeListener(listener);
		}
	}
	
	
	private class Listener implements StateChangeListener {
		@Override
		public void stateChanged(EventObject e) {
			fireEvent();
		}
	}
	
	
}
