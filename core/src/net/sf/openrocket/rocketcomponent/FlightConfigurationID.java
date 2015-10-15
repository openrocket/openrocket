package net.sf.openrocket.rocketcomponent;

import java.util.UUID;

/*
 * FlightConfigurationID is a very minimal wrapper class used to identify a given flight configuration for various components and options.  
 * It is intended to provide better visibility and traceability by more specific type safety -- this class replaces a 
 * straight-up <code>String</code> Key in previous implementations. 
 */
public final class FlightConfigurationID implements Comparable<FlightConfigurationID> {
	final public String key;
	
	private final static String ERROR_CONFIGURATION_KEYTEXT = "error_key_2489";
	private final static String DEFAULT_CONFIGURATION_KEYTEXT = "default_configuration_6602";
	private final static String DEFAULT_VALUE_KEYTEXT = "default_value_5676";
	
	public final static FlightConfigurationID ERROR_CONFIGURATION_FCID = new FlightConfigurationID( FlightConfigurationID.ERROR_CONFIGURATION_KEYTEXT);
	public final static FlightConfigurationID DEFAULT_CONFIGURATION_FCID = new FlightConfigurationID( FlightConfigurationID.DEFAULT_CONFIGURATION_KEYTEXT );
	public final static FlightConfigurationID DEFAULT_VALUE_FCID = new FlightConfigurationID( FlightConfigurationID.DEFAULT_VALUE_KEYTEXT ); 
	
	public FlightConfigurationID() {
		this(UUID.randomUUID().toString());
	}
	
	public FlightConfigurationID(final String _val) {
		if (null == _val){
			this.key = FlightConfigurationID.ERROR_CONFIGURATION_KEYTEXT;
		}else if (5 >_val.length()){
			this.key = FlightConfigurationID.ERROR_CONFIGURATION_KEYTEXT;
		} else {
			// vv temp vv
			String temp_val = _val;
			final String extra = "key: ";
			if( _val.contains(extra)){
				int index = temp_val.lastIndexOf(extra);
				temp_val = _val.substring(index+extra.length());
				System.err.println("  correcting FCID from \""+_val+"\" to \""+temp_val+"\".");
			}
			// ^^ temp ^^
			
			this.key = temp_val;
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
		if (this.key.intern() == FlightConfigurationID.ERROR_CONFIGURATION_KEYTEXT) {
			return false;
		}
		
		return true;
	}
	
	public int length() {
		return this.key.length();
	}

	public String toShortKey(){
		if( this == DEFAULT_VALUE_FCID ){
			return "DEFVAL";
		}else if( this == FlightConfigurationID.DEFAULT_CONFIGURATION_FCID){
			return "DEFCONFIG";
		}
		return this.key.substring(0,8);
	}
	
	@Override
	public String toString() {
		return this.key;
	}

	@Override
	public int compareTo(FlightConfigurationID other) {
		return (this.key.compareTo( other.key));
	}
	
}
