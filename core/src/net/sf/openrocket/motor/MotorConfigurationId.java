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
	public final static MotorConfigurationId ERROR_ID = new MotorConfigurationId();

	private MotorConfigurationId( ) {
		this.key = MotorConfigurationId.ERROR_KEY;	
	}
	
	/**
	 * Sole constructor.
	 * 
	 * @param componentName	the component ID, must not be null
	 * @param number		a positive motor number
	 */
	public MotorConfigurationId(final MotorMount _mount, final FlightConfigurationId _fcid) {
		if (null == _mount ) {
			throw new NullPointerException("Provided MotorMount was null");
		}
		if (null == _fcid ) {
			throw new NullPointerException("Provided FlightConfigurationId was null");
		}
		
		// Use intern so comparison can be done using == instead of equals()
		final long upper = ((long)_mount.getID().hashCode()) << 32;
		final long lower = _fcid.key.getLeastSignificantBits();
		this.key = new UUID( upper, lower);
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
	
	@Override
	public String toString(){
		if( this.key == MotorConfigurationId.ERROR_KEY){
			return MotorConfigurationId.ERROR_ID_TEXT;
		}else{
			return key.toString();
		}
	}
}
