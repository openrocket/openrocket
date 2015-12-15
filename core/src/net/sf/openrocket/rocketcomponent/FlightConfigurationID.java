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
	private final static String ERROR_KEY_NAME = "<Error_Key>";
	private final static UUID DEFAULT_VALUE_UUID = new UUID( DEFAULT_MOST_SIG_BITS, 5676);
	
	public final static FlightConfigurationID ERROR_CONFIGURATION_FCID = new FlightConfigurationID( FlightConfigurationID.ERROR_CONFIGURATION_UUID);
	public final static FlightConfigurationID DEFAULT_VALUE_FCID = new FlightConfigurationID( FlightConfigurationID.DEFAULT_VALUE_UUID ); 
	
	public FlightConfigurationID() {
		this(UUID.randomUUID());
	}
	
	public FlightConfigurationID(final String _str) {
		UUID candidate;
		if("".equals(_str)){
			candidate = UUID.randomUUID();
		}else{
			try{
				candidate = UUID.fromString( _str);
			}catch( IllegalArgumentException iae ){
				candidate = new UUID( 0, _str.hashCode() );
			}
		}
		this.key = candidate;
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
		if( isValid()){
			return this.key.toString().substring(0,8);
		}else{
			return ERROR_KEY_NAME;
		}
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
	
	public boolean hasError(){
		return (ERROR_CONFIGURATION_UUID == this.key);
	}
	public boolean isValid() {
		return !hasError();
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
