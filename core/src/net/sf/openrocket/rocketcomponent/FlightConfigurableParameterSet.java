package net.sf.openrocket.rocketcomponent;

import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.StateChangeListener;
import net.sf.openrocket.util.Utils;

/**
 * Represents a value or parameter that can vary based on the
 * flight configuration ID.
 * <p>
 * The parameter value is always defined, and null is not a valid
 * parameter value.
 *
 * @param <E>	the parameter type
 */
public class FlightConfigurableParameterSet<E extends FlightConfigurableParameter<E>> implements Iterable<E> {
	
	//private static final Logger log = LoggerFactory.getLogger(ParameterSet.class);
	protected final HashMap<FlightConfigurationId, E> map = new HashMap<FlightConfigurationId, E>();
	
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
	public FlightConfigurableParameterSet(RocketComponent component, int eventType, E _defaultValue) {
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
	public FlightConfigurableParameterSet(FlightConfigurableParameterSet<E> configSet, RocketComponent component, int eventType) {
		this.component = component;
		this.eventType = eventType;
		
		this.defaultValue= configSet.getDefault().clone();
		for (FlightConfigurationId key : configSet.map.keySet()) {
			E cloneConfig = configSet.map.get(key).clone();
			this.map.put(key, cloneConfig);
		}
	}
	
	public boolean containsKey( final FlightConfigurationId fcid ){
		return this.map.containsKey(fcid);
	}
	
	/**
	 * Return the default parameter value for this FlightConfiguration.
	 * This is used in case a per-flight configuration override
	 * has not been defined.
	 * 
	 * @return the default parameter value (never null)
	 */
	public E getDefault(){
		return this.defaultValue;
	}
	
	/**
	 * Set the default parameter value for this FlightConfiguration.
	 *This is used in case a per-flight configuration override
	 * has not been defined.
	 *  
	 * @param value		the parameter value (null not allowed)
	 */
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
	
	
	/**
	 * Return the number of specific flight configurations other than the default.
	 * @return
	 */
	public int size() {
		return map.size();
	}

	/**
	 * Return the parameter value for the provided flight configuration ID.
	 * This returns either the value specified for this flight config ID,
	 * or the default value.
	 * 
	 * @param    value the parameter to find
	 * @return   the flight configuration ID
	 */
	public FlightConfigurationId get(E testValue) {
		if( null == testValue ){
			return null;
		}
		for( Entry<FlightConfigurationId, E> curEntry : this.map.entrySet()){
			FlightConfigurationId curKey = curEntry.getKey();
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
		
		List<FlightConfigurationId> ids = this.getSortedConfigurationIDs();
		FlightConfigurationId selectedId = ids.get(index);
		return this.map.get(selectedId);
	}
	
	/**
	 * Return the parameter value for the provided flight configuration ID.
	 * This returns either the value specified for this flight config ID,
	 * or the default value.
	 * 
	 * @param id	the flight configuration ID
	 * @return		the parameter to use (never null)
	 */
	public E get(FlightConfigurationId id) {
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

	/**
	 * 
	 * @return a sorted list of all the contained FlightConfigurationIDs
	 */
	public List<FlightConfigurationId> getSortedConfigurationIDs(){
		ArrayList<FlightConfigurationId> toReturn = new ArrayList<FlightConfigurationId>(); 
		
		toReturn.addAll( this.map.keySet() );
		// Java 1.8:
		//toReturn.sort( null );
		
		// Java 1.7: 
	    Collections.sort(toReturn);
			
		return toReturn;
	}
	
	public List<FlightConfigurationId> getIDs(){
		return this.getSortedConfigurationIDs();
	}
    
	/**
	 * Set the parameter value for the provided flight configuration ID.
	 * This sets the override for this flight configuration ID.
	 * 
	 * @param id		the flight configuration ID
	 * @param value		the parameter value (null not allowed)
	 */
	public void set(FlightConfigurationId fcid, E nextValue) {
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
	
	/**
	 * Return whether a specific flight configuration ID is using the
	 * default value.
	 * 
	 * @param id	the flight configuration ID
	 * @return		whether the default is being used
	 */
	public boolean isDefault( FlightConfigurationId fcid) {
		return ( this.getDefault() == this.map.get(fcid));
	}
	
	/**
	 * Reset a specific flight configuration ID to use the default parameter value.
	 * 
	 * @param id	the flight configuration ID
	 */
	public void reset( FlightConfigurationId fcid) {
		if( fcid.isValid() ){
			set( fcid, null);
		}
	}

	/* 
	 * Clears all configuration-specific settings -- meaning querying the parameter for any configuration will return the default value.
	 * 
	 */
	public void reset() {
		E tempValue = this.getDefault();
		this.map.clear();
		setDefault(tempValue);
	}
	
	
	private void fireEvent() {
		component.fireComponentChangeEvent(eventType);
	}
	
	public FlightConfigurationId cloneFlightConfiguration(FlightConfigurationId oldConfigId, FlightConfigurationId newConfigId) {
		// clones the ENTRIES for the given fcid's.		
		E oldValue = this.get(oldConfigId);
		this.set(newConfigId, oldValue.clone());
		fireEvent();
		return newConfigId;
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
		buf.append(String.format("    >> ParameterSet<%s> (%d configurations)\n", this.defaultValue.getClass().getSimpleName(), this.size() ));

		buf.append(String.format("        >> [%s]= %s\n", "DEFAULT", this.getDefault().toString() ));		
		for( FlightConfigurationId loopFCID : this.getSortedConfigurationIDs()){
			String shortKey = loopFCID.toShortKey();
			
			E inst = this.map.get(loopFCID);
			if( this.isDefault(inst)){
				shortKey = "*"+shortKey+"*";
			}
			buf.append(String.format("              >> [%s]= %s\n", shortKey, inst ));
		}
		return buf.toString();
	}


	public void update(){
		this.defaultValue.update();
		for( E curValue: this.map.values() ){
			curValue.update();
		}
	}
			
	
}
