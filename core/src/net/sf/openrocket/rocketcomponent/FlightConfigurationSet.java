package net.sf.openrocket.rocketcomponent;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

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
	protected final HashMap<FlightConfigurationID, E> map = new HashMap<FlightConfigurationID, E>();
	protected final static FlightConfigurationID DEFAULT_VALUE_FCID = FlightConfigurationID.DEFAULT_VALUE_FCID;
	
	protected final RocketComponent component;
	protected final int eventType;
	
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
		
		this.map.put( DEFAULT_VALUE_FCID, _defaultValue );
		
		addListener(_defaultValue);
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
		
		this.map.put( DEFAULT_VALUE_FCID, flightConfiguration.getDefault().clone());
		for (FlightConfigurationID key : flightConfiguration.map.keySet()) {
			this.map.put(key, flightConfiguration.map.get(key).clone());
		}
	}
	
	public boolean containsKey( final FlightConfigurationID fcid ){
		return this.map.containsKey(fcid);
	}
	
	@Override
	public E getDefault(){
		return this.map.get(DEFAULT_VALUE_FCID);
	}
	
	@Override
	public void setDefault(E nextDefaultValue) {
		if (nextDefaultValue == null) {
			throw new NullPointerException("new Default Value is null");
		}
		if( this.isDefault(nextDefaultValue)){
			return;
		}
		this.set( DEFAULT_VALUE_FCID, nextDefaultValue);
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
	
	@Override
	public E get(FlightConfigurationID id) {
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
		Vector<FlightConfigurationID> toReturn = new Vector<FlightConfigurationID>(); 
		
		toReturn.addAll( this.getIDs() );
		toReturn.sort( null );
			
		return toReturn;
	}
	
	public Set<FlightConfigurationID> getIDs(){
		return this.map.keySet();
	}
	
	@Override
	public void set(FlightConfigurationID fcid, E nextValue) {
		if (null == fcid) {
			throw new NullPointerException("id is null");
		}else if( !fcid.isValid()){
			throw new IllegalStateException("  Attempt to reset the default value on with an invalid key: "+fcid.toString());
		}
		if ( nextValue == null) {
			// null value means to delete this fcid
			if ( DEFAULT_VALUE_FCID == fcid ) {
				// NEVER delete the default value....
				return;
			}
   
			E previousValue = map.remove(fcid);
			removeListener(previousValue);
		}else{
			E previousValue = map.put(fcid, nextValue);
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
		return (getDefault() == map.get(fcid));
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
	
	public void printDebug(){
		System.err.println("====== Dumping ConfigurationSet for comp: '"+this.component.getName()+"' of type: "+this.component.getClass().getSimpleName()+" ======");
		System.err.println("        >> FlightConfigurationSet ("+this.size()+ " configurations)");
		
		for( FlightConfigurationID loopFCID : this.map.keySet()){
			String shortKey = loopFCID.toShortKey();
			
			
			E inst = this.map.get(loopFCID);
			if( this.isDefault(inst)){
				shortKey = "*"+shortKey+"*";
			}
			String designation;
			if( inst instanceof FlightConfiguration){
				FlightConfiguration fc = (FlightConfiguration) inst;
				designation = fc.getName();
			}else{
				designation = inst.toString();
			}
			System.err.println("              >> ["+shortKey+"]= "+designation);
		}
					
	}
	
}
