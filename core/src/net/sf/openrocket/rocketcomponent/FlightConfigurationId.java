package net.sf.openrocket.rocketcomponent;

import java.util.UUID;

/**
 * FlightConfigurationID is a very minimal wrapper class used to identify a given flight configuration for various components and options.  
 * It is intended to provide better visibility and traceability by more specific type safety -- this class replaces a 
 * straight-up <code>String</code> Key in previous implementations. 
 */
public final class FlightConfigurationId implements Comparable<FlightConfigurationId> {
	final public UUID key;
	
	private final static long DEFAULT_MOST_SIG_BITS = 0xF4F2F1F0;
	private final static UUID ERROR_UUID = new UUID( DEFAULT_MOST_SIG_BITS, 2489);
	private final static String ERROR_KEY_NAME = "ErrorKey".intern();
	private final static UUID DEFAULT_VALUE_UUID = new UUID( DEFAULT_MOST_SIG_BITS, 5676);
	private final static String DEFAULT_VALUE_NAME = "DefaultKey".intern();
	
	public final static FlightConfigurationId ERROR_FCID = new FlightConfigurationId( FlightConfigurationId.ERROR_UUID);
	public final static FlightConfigurationId DEFAULT_VALUE_FCID = new FlightConfigurationId( FlightConfigurationId.DEFAULT_VALUE_UUID ); 
	
	/**
	 * default constructor, builds with an unique random ID
	 */
	public FlightConfigurationId() {
		this(UUID.randomUUID());
	}
	
	/**
	 * builds the id with the given String
	 * @param _str	te string to be made into the id
	 */
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
	
	/**
	 * builds he id with the given UUID object
	 * @param _val	the UUID to be made into the id
	 */
	public FlightConfigurationId(final UUID _val) {
		if (null == _val){
			this.key = FlightConfigurationId.ERROR_UUID;
		} else {
			this.key = _val;
		}
	}
	
	/**
	 * {@inheritDoc}
	 * considers equals ids with the same key
	 */
	@Override
	public boolean equals(Object anObject) {
		if (!(anObject instanceof FlightConfigurationId)) {
			return false;
		}
		
		FlightConfigurationId otherFCID = (FlightConfigurationId) anObject;
		return this.key.equals(otherFCID.key);
	}
	
	/**
	 * 
	 * @return
	 */
	public String toShortKey(){
		if( hasError() )
			return FlightConfigurationId.ERROR_KEY_NAME;
		if( isDefaultId())
			return FlightConfigurationId.DEFAULT_VALUE_NAME;
		return this.key.toString().substring(0,8);
		
	}

	//extracted this method because maybe, just maybe, this info could be used somewhere else
	/**
	 * gets if the id is the default
	 * @return	if the id is default
	 */
	private boolean isDefaultId() {
		return this.key == FlightConfigurationId.DEFAULT_VALUE_UUID;
	}
	
	/**
	 * returns the whole key in the id
	 * @return	the full key of the id
	 */
	public String toFullKey(){
		return this.toString();
	}
	
	/**
	 * {@inheritDoc}
	 * uses the key hash code
	 */
	@Override
	public int hashCode() {
		return this.key.hashCode();
	}

	/**
	 * checks if the key is the ERROR_UUID flag
	 * @return	if the id has error
	 */
	public boolean hasError(){
		return (ERROR_UUID == this.key);
	}
	
	/**
	 * checks if the key from the id is valid
	 * @return	if the id is valid or not
	 */
	public boolean isValid() {
		return !hasError();
	}
	
	/**
	 * {@inheritDoc}
	 * same as get full id
	 */
	@Override
	public String toString() {
		return this.key.toString();
	}
	
	@Override
	public int compareTo(FlightConfigurationId other) {
		return this.key.compareTo( other.key);
	}
	
	/**
	 * used for debuggin, gets the short key
	 * @return	the short key version of the id
	 */
	public String toDebug(){
		return this.toShortKey();
	}
	
	
}
