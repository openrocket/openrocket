package net.sf.openrocket.rocketcomponent;

import java.util.UUID;

/*
 * FlightConfigurationID is a very minimal wrapper class used to identify a given flight configuration for various components and options.  
 * It is intended to provide better visibility and traceability by more specific type safety -- this class replaces a 
 * straight-up <code>String</code> Key in previous implementations. 
 */
public final class FlightConfigurationID implements Comparable<FlightConfigurationID> {
	final public UUID key;
	
	private final static long DEFAULT_MOST_SIG_BITS = 0xF4F2F1F0;
	private final static UUID ERROR_CONFIGURATION_UUID = new UUID( DEFAULT_MOST_SIG_BITS, 2489);
//	private final static String DEFAULT_CONFIGURATION_KEYTEXT = "default_configuration_6602";
	private final static UUID DEFAULT_VALUE_UUID = new UUID( DEFAULT_MOST_SIG_BITS, 5676);
	
	public final static FlightConfigurationID ERROR_CONFIGURATION_FCID = new FlightConfigurationID( FlightConfigurationID.ERROR_CONFIGURATION_UUID);
//	public final static FlightConfigurationID DEFAULT_CONFIGURATION_FCID = new FlightConfigurationID( FlightConfigurationID.DEFAULT_CONFIGURATION_KEYTEXT );
	public final static FlightConfigurationID DEFAULT_VALUE_FCID = new FlightConfigurationID( FlightConfigurationID.DEFAULT_VALUE_UUID ); 
	
	public FlightConfigurationID() {
		this(UUID.randomUUID());
	}
	
	public FlightConfigurationID(final String _str) {
		this.key = UUID.fromString( _str);
	}
	
	public FlightConfigurationID(final UUID _val) {
		if (null == _val){
			this.key = FlightConfigurationID.ERROR_CONFIGURATION_UUID;
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
	
	public String toShortKey(){
		return this.key.toString().substring(0,8);
	}

	public String getFullKeyText(){
		return this.key.toString();
	}
	
	@Override
	public int hashCode() {
		return this.key.hashCode();
	}
	
	public UUID intern() {
		return this.key;
	}
	
	public boolean isValid() {
		return (this.key != ERROR_CONFIGURATION_UUID);
	}
	
	@Override
	public String toString() {
		return this.key.toString();
	}

	@Override
	public int compareTo(FlightConfigurationID other) {
		return (this.key.compareTo( other.key));
	}
	
}
