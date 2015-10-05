package net.sf.openrocket.rocketcomponent;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.util.StateChangeListener;
import net.sf.openrocket.util.Utils;

/**
 * An implementation of FlightConfiguration that fires off events
 * to the rocket components when the parameter value is changed.
 *
 * @param <E>	the parameter type
 */
public class FlightConfigurationSet<E extends FlightConfigurableParameter<E>> implements FlightConfigurable<E> {
	
	private static final Logger log = LoggerFactory.getLogger(FlightConfigurationSet.class);
	private final HashMap<FlightConfigurationID, E> map = new HashMap<FlightConfigurationID, E>();
	private E defaultValue = null;
	
	private final RocketComponent component;
	private final int eventType;
	
	private final Listener listener = new Listener();
	
	
	/**
	 * Construct a FlightConfiguration that has no overrides.
	 * 
	 * @param component		the rocket component on which events are fired when the parameter values are changed
	 * @param eventType		the event type that will be fired on changes
	 */
	public FlightConfigurationSet(RocketComponent component, int eventType, E _defaultValue) {
		this.component = component;
		this.eventType = eventType;
		
		this.defaultValue = _defaultValue;
		if ( null == defaultValue ) {
			throw new NullPointerException("defaultValue is null");
		}
		this.map.put( FlightConfigurationID.DEFAULT_CONFIGURATION_ID, defaultValue );
		
		add(defaultValue);
	}
	
	
	/**
	 * Construct a copy of an existing FlightConfigurationImpl.
	 * 
	 * @param component		the rocket component on which events are fired when the parameter values are changed
	 * @param eventType		the event type that will be fired on changes
	 */
	public FlightConfigurationSet(FlightConfigurationSet<E> flightConfiguration, RocketComponent component, int eventType) {
		this.component = component;
		this.eventType = eventType;
		
		this.defaultValue = flightConfiguration.defaultValue.clone();
		for (FlightConfigurationID key : flightConfiguration.map.keySet()) {
			this.map.put(key, flightConfiguration.map.get(key).clone());
		}
	}
	
	public boolean containsKey( final FlightConfigurationID fcid ){
		return this.map.containsKey(fcid);
	}
	
	@Override
	public E getDefault(){
		return defaultValue;
	}

	@Override
	public void setDefault(E value) {
		if (value == null) {
			throw new NullPointerException("value is null");
		}
		if( this.isDefault(value)){
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
	public E get(FlightConfigurationID id) {
		E toReturn;
		if (map.containsKey(id)) {
			toReturn = map.get(id);
		} else {
			toReturn = defaultValue;
		}
		return toReturn;
	}

	public Set<FlightConfigurationID> getIDs(){
		return this.map.keySet();
	}
	
	@Override
	public void set(FlightConfigurationID fcid, E nextValue) {
		if (null == fcid) {
			throw new NullPointerException("id is null");
		}
		if (nextValue == null) {
			// null value means to delete this fcid
			this.remove(fcid);	
		}else{
			E previousValue = map.put(fcid, nextValue);
			remove(previousValue);
			if (previousValue == this.defaultValue) {
				this.defaultValue = nextValue;
			}
			add(nextValue);
		}

		fireEvent();
	}
	
	public boolean isDefault(E _value) {
		return (Utils.equals(this.defaultValue, _value));
	}
	
	@Override
	public boolean isDefault(FlightConfigurationID id) {
		return (this.defaultValue == map.get(id));
	}
	
	@Override
	public void resetDefault(FlightConfigurationID id) {
		if( null == id){
			this.resetDefault();
		}else if( !id.isValid()){
			throw new IllegalStateException("  Attempt to reset the default value on with an invalid key: "+id.toString());
		}
		
		E previous = map.get(id);
		remove(previous);
		
		if ( previous == this.defaultValue ) {
			this.defaultValue = null;
			resetDefault();
		}
		fireEvent();
	}
	
	private void resetDefault(){
		if( 0 == this.map.keySet().size()){
			throw new IllegalStateException("  Attempt to reset the default value on an empty configurationSet.");
		}
		
		FlightConfigurationID firstFCID = map.keySet().iterator().next();
		this.defaultValue = map.get( firstFCID);
	}
	
	private void fireEvent() {
		component.fireComponentChangeEvent(eventType);
	}
	
	
	@Override
	public void cloneFlightConfiguration(FlightConfigurationID oldConfigId, FlightConfigurationID newConfigId) {
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
	
	public void remove(FlightConfigurationID fcid) {
		// enforce at least one value in the set
		if( 1 < this.map.size() ){
			this.map.remove(fcid);
			if( this.isDefault(fcid)){
				this.defaultValue = map.values().iterator().next();
			}
		}else{
			log.warn(" attempted to remove last element from the FlightConfigurationSet<"+this.defaultValue.getClass().getSimpleName()+">.  Action not allowed. ");
			return;
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
