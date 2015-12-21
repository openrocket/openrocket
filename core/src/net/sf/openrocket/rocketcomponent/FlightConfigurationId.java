package net.sf.openrocket.rocketcomponent;

import java.util.UUID;

/*
 * FlightConfigurationID is a very minimal wrapper class used to identify a given flight configuration for various components and options.  
 * It is intended to provide better visibility and traceability by more specific type safety -- this class replaces a 
 * straight-up <code>String</code> Key in previous implementations. 
 */
public final class FlightConfigurationId implements Comparable<FlightConfigurationId> {
	final public UUID key;
	
	private final static long DEFAULT_MOST_SIG_BITS = 0xF4F2F1F0;
	private final static UUID ERROR_UUID = new UUID( DEFAULT_MOST_SIG_BITS, 2489);
	private final static String ERROR_KEY_NAME = "ErrorKey";
	private final static UUID DEFAULT_VALUE_UUID = new UUID( DEFAULT_MOST_SIG_BITS, 5676);
	private final static String DEFAULT_VALUE_NAME = "DefaultKey";
	
	public final static FlightConfigurationId ERROR_FCID = new FlightConfigurationId( FlightConfigurationId.ERROR_UUID);
	public final static FlightConfigurationId DEFAULT_VALUE_FCID = new FlightConfigurationId( FlightConfigurationId.DEFAULT_VALUE_UUID ); 
	
	public FlightConfigurationId() {
		this(UUID.randomUUID());
	}
	
	public FlightConfigurationId(final String _str) {
		UUID candidate;
		if(_str == null || "".equals(_str)){
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
	
	public FlightConfigurationId(final UUID _val) {
		if (null == _val){
			this.key = FlightConfigurationId.ERROR_UUID;
		} else {
			this.key = _val;
		}
	}
	
	@Override
	public boolean equals(Object anObject) {
		if (!(anObject instanceof FlightConfigurationId)) {
			return false;
		}
		
		FlightConfigurationId otherFCID = (FlightConfigurationId) anObject;
		return this.key.equals(otherFCID.key);
	}
	
	public String toShortKey(){
		if( hasError() ){
			return FlightConfigurationId.ERROR_KEY_NAME;
		}else if( this.key == FlightConfigurationId.DEFAULT_VALUE_UUID){
			return FlightConfigurationId.DEFAULT_VALUE_NAME;
		}else{
			return this.key.toString().substring(0,8);
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
		return (ERROR_UUID == this.key);
	}
	public boolean isValid() {
		return !hasError();
	}
	
	@Override
	public String toString() {
		return this.key.toString();
	}
	
	@Override
	public int compareTo(FlightConfigurationId other) {
		return this.key.compareTo( other.key);
	}
	
	public String toDebug(){
		return this.toShortKey();
	}
	
	
}
