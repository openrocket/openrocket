package net.sf.openrocket.motor;

import java.util.UUID;

import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.MotorMount;

/**
 * An immutable identifier for a motor instance in a MotorInstanceConfiguration.
 * The motor is identified by the ID of its mounting component and a 
 * positive motor count number.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public final class MotorConfigurationId {
	
	private final UUID key;
	
	private final static String ERROR_ID_TEXT = "MotorInstance Error Id".intern();
	private final static UUID ERROR_KEY = new UUID( 62274413, 56768908);

	/**
	 * Sole constructor.
	 * 
	 * @param _mount the component ID, must not be null
	 * @param _fcid the key for a
	 */
	public MotorConfigurationId(final MotorMount _mount, final FlightConfigurationId _fcid) {
		if (null == _mount ) {
			throw new NullPointerException("Provided MotorMount was null");
		}
		if (null == _fcid ) {
			throw new NullPointerException("Provided FlightConfigurationId was null");
		}
		
		final long mountHash= ((long)_mount.getID().hashCode()) << 32;
		final long fcidLower = _fcid.key.getMostSignificantBits();
		this.key = new UUID( mountHash, fcidLower);
	}
		
	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
			
		if (!(other instanceof MotorConfigurationId))
			return false;
		
		MotorConfigurationId otherId = (MotorConfigurationId) other;
		return ( this.key.equals( otherId.key));
	}
	
	@Override
	public int hashCode() {
		return key.hashCode();
	}
	
	public String toDebug(){
		return toShortKey();
	}
	
	@Override
	public String toString(){
		if( this.key == MotorConfigurationId.ERROR_KEY){
			return MotorConfigurationId.ERROR_ID_TEXT;
		}else{
			return key.toString();
		}
	}

	public String toShortKey() {
		final String keyString = key.toString();
		final int lastIndex = -1 + keyString.length();
		final int chunkLen = 4;
		
		// return the head + tail of the full 64-character id
		return (keyString.substring(0,chunkLen)+"/"+keyString.substring(lastIndex - chunkLen,lastIndex));
	}
}
