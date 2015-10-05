package net.sf.openrocket.rocketcomponent;

import java.util.UUID;

/*
 * FlightConfigurationID is a very minimal wrapper class used to identify a given flight configuration for various components and options.  
 * It is intended to provide better visibility and traceability by more specific type safety -- this class replaces a 
 * straight-up <code>String</code> Key in previous implementations. 
 */
public final class FlightConfigurationID implements Comparable<FlightConfigurationID> {
	final public String key;
	
	private final static String ERROR_CONFIGURATION_KEY = "j567uryk2489yfjbr8i1fi";
	private final static String DEFAULT_CONFIGURATION_KEY = "default_configuration_662002";
	
	public final static FlightConfigurationID ERROR_CONFIGURATION_ID = new FlightConfigurationID( FlightConfigurationID.ERROR_CONFIGURATION_KEY);
	public final static FlightConfigurationID DEFAULT_CONFIGURATION_ID = new FlightConfigurationID( FlightConfigurationID.DEFAULT_CONFIGURATION_KEY );
	
	public FlightConfigurationID() {
		this(UUID.randomUUID().toString());
	}
	
	public FlightConfigurationID(final String _val) {
		if (null == _val){
			this.key = FlightConfigurationID.ERROR_CONFIGURATION_KEY;
		}else if (5 >_val.length()){
			this.key = FlightConfigurationID.ERROR_CONFIGURATION_KEY;
		} else {
			this.key = _val;
		}
	}
	
	@Override
	public boolean equals(Object anObject) {
		if (!(anObject instanceof FlightConfigurationID)) {
			return false;
		}
		
		FlightConfigurationID otherFCID = (FlightConfigurationID) anObject;
		return this.key.equals(otherFCID.key);
	}
	
	@Override
	public int hashCode() {
		return this.key.hashCode();
	}
	
	public String intern() {
		return this.key.intern();
	}
	
	public boolean isValid() {
		if (this.key.intern() == FlightConfigurationID.ERROR_CONFIGURATION_KEY) {
			return false;
		}
		
		return true;
	}
	
	public int length() {
		return this.key.length();
	}
	
	@Override
	public String toString() {
		return ("key: "+this.key);
	}

	@Override
	public int compareTo(FlightConfigurationID other) {
		return (this.key.compareTo( other.key));
	}
	
}
