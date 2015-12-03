package net.sf.openrocket.rocketcomponent;

import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.StateChangeListener;
import net.sf.openrocket.util.Utils;

/**
 * An implementation of FlightConfiguration that fires off events
 * to the rocket components when the parameter value is changed.
 *
 * @param <E>	the parameter type
 */
public class ParameterSet<E extends FlightConfigurableParameter<E>> implements FlightConfigurable<E> {
	
	private static final Logger log = LoggerFactory.getLogger(ParameterSet.class);
	protected final HashMap<FlightConfigurationID, E> map = new HashMap<FlightConfigurationID, E>();
	
	protected E defaultValue;
	protected final RocketComponent component;
	protected final int eventType;
	
	private final Listener listener = new Listener();
	
	
	/**
	 * Construct a FlightConfiguration that has no overrides.
	 * 
	 * @param component		the rocket component on which events are fired when the parameter values are changed
	 * @param eventType		the event type that will be fired on changes
	 */
	public ParameterSet(RocketComponent component, int eventType, E _defaultValue) {
		this.component = component;
		this.eventType = eventType;
		
		this.defaultValue= _defaultValue;
		
		addListener(_defaultValue);
	}
	
	
	/**
	 * Construct a copy of an existing FlightConfigurationImpl.
	 * 
	 * @param component		the rocket component on which events are fired when the parameter values are changed
	 * @param eventType		the event type that will be fired on changes
	 */
	public ParameterSet(ParameterSet<E> flightConfiguration, RocketComponent component, int eventType) {
		this.component = component;
		this.eventType = eventType;
		
		this.defaultValue= flightConfiguration.getDefault().clone();
		for (FlightConfigurationID key : flightConfiguration.map.keySet()) {
			this.map.put(key, flightConfiguration.map.get(key).clone());
		}
	}
	
	public boolean containsKey( final FlightConfigurationID fcid ){
		return this.map.containsKey(fcid);
	}
	
	@Override
	public E getDefault(){
		return this.defaultValue;
	}
	
	@Override
	public void setDefault(E nextDefaultValue) {
		if (nextDefaultValue == null) {
			throw new NullPointerException("new Default Value is null");
		}
		if( this.isDefault(nextDefaultValue)){
			return;
		}
		this.defaultValue = nextDefaultValue;
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
	public FlightConfigurationID get(E testValue) {
		if( null == testValue ){
			return null;
		}
		for( Entry<FlightConfigurationID, E> curEntry : this.map.entrySet()){
			FlightConfigurationID curKey = curEntry.getKey();
			E curValue = curEntry.getValue();
			
			if( testValue.equals(curValue)){
				return curKey;
			}
		}
		
		return null;
	}

	public E get(final int index) {
		if( 0 > index){
			throw new ArrayIndexOutOfBoundsException("Attempt to retrieve a configurable parameter by an index less than zero: "+index);
		}
		if(( 0 > index) || ( this.map.size() <= index )){
			throw new ArrayIndexOutOfBoundsException("Attempt to retrieve a configurable parameter with an index larger "
													+" than the stored values: "+index+"/"+this.map.size());
		}
		
		List<FlightConfigurationID> ids = this.getSortedConfigurationIDs();
		FlightConfigurationID selectedId = ids.get(index);
		return this.map.get(selectedId);
	}
	
	@Override
	public E get(FlightConfigurationID id) {
		if( id.hasError() ){
			throw new NullPointerException("Attempted to retrieve a parameter with an error key!");
		}
		E toReturn;
		if (map.containsKey(id)) {
			toReturn = map.get(id);
		} else {
			toReturn = this.getDefault();
		}
		return toReturn;
	}

	@Override
	public List<FlightConfigurationID> getSortedConfigurationIDs(){
		ArrayList<FlightConfigurationID> toReturn = new ArrayList<FlightConfigurationID>(); 
		
		toReturn.addAll( this.map.keySet() );
		// Java 1.8:
		//toReturn.sort( null );
		
		// Java 1.7: 
	    Collections.sort(toReturn);
			
		return toReturn;
	}
	
	public List<FlightConfigurationID> getIDs(){
		return this.getSortedConfigurationIDs();
	}
    
	@Override
	public void set(FlightConfigurationID fcid, E nextValue) {
		if ( nextValue == null) {
			// null value means to delete this fcid
			E previousValue = this.map.remove(fcid);
			removeListener(previousValue);
		}else{
			E previousValue = this.map.put(fcid, nextValue);
			removeListener(previousValue);
			addListener(nextValue);
		}

		fireEvent();
	}
	
	public boolean isDefault(E testVal) {
		 return (Utils.equals( this.getDefault(), testVal));
	}
	
	@Override
	public boolean isDefault( FlightConfigurationID fcid) {
		return ( this.getDefault() == this.map.get(fcid));
	}
	
	@Override
	public void reset( FlightConfigurationID fcid) {
		// enforce at least one value in the set
		if( 1 < this.map.size() ){
			set( fcid, null);
		}else{
			log.warn(" attempted to remove last element from the FlightConfigurationSet<"+this.getDefault().getClass().getSimpleName()+"> attached to: "+component.getName()+".  Ignoring. ");
			return;
		}
	}
	
	private void fireEvent() {
		component.fireComponentChangeEvent(eventType);
	}
	
 
	@Override
	public void cloneFlightConfiguration(FlightConfigurationID oldConfigId, FlightConfigurationID newConfigId) {
		// clones the ENTRIES for the given fcid's.		
		E oldValue = this.get(oldConfigId);
		this.set(newConfigId, oldValue.clone());
		fireEvent();
	}
	
	private void addListener(E value) {
		if (value != null) {
			value.addChangeListener(listener);
		}
	}
	
	private void removeListener(E value) {
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
	
	public String toDebug(){
		StringBuilder buf = new StringBuilder();
		buf.append(String.format("====== Dumping ConfigurationSet for: '%s' of type: %s ======\n", this.component.getName(), this.component.getClass().getSimpleName() ));
		buf.append(String.format("        >> FlightConfigurationSet (%d configurations)\n", this.size() ));

		if( 0 == this.map.size() ){
			buf.append(String.format("              >> [%s]= %s\n", "*DEFAULT*", this.getDefault().toString() ));		
		}else{
			for( FlightConfigurationID loopFCID : this.getSortedConfigurationIDs()){
				String shortKey = loopFCID.toShortKey();
				
				E inst = this.map.get(loopFCID);
				if( this.isDefault(inst)){
					shortKey = "*"+shortKey+"*";
				}
				buf.append(String.format("              >> [%s]= %s\n", shortKey, inst ));
			}
		}
		return buf.toString();
	}
	
}
