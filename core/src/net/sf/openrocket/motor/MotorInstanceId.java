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
public final class MotorInstanceId {
	
	private final String name ;
	private final UUID key;
	
	private final static String ERROR_ID_TEXT = "MotorInstance Error Id";
	private final static UUID ERROR_KEY = new UUID( 6227, 5676);
	public final static MotorInstanceId ERROR_ID = new MotorInstanceId();

	private MotorInstanceId( ) {
		this.name = MotorInstanceId.ERROR_ID_TEXT;
		this.key = MotorInstanceId.ERROR_KEY;	
	}
	
	/**
	 * Sole constructor.
	 * 
	 * @param componentName	the component ID, must not be null
	 * @param number		a positive motor number
	 */
	public MotorInstanceId(final MotorMount _mount, final FlightConfigurationId _fcid ) {
		if (null == _mount ) {
			throw new IllegalArgumentException("Provided MotorConfiguration was null");
		}
		if (null == _fcid ) {
			throw new IllegalArgumentException("Provided MotorConfiguration was null");
		}
		
		// Use intern so comparison can be done using == instead of equals()
		this.name = _mount.getID()+"-"+_fcid.toShortKey();
		final long upper = _mount.getID().hashCode() << 32;
		final long lower = _fcid.key.getMostSignificantBits();
		this.key = new UUID( upper, lower);
	}

		
	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
			
		if (!(other instanceof MotorInstanceId))
			return false;
		
		MotorInstanceId otherId = (MotorInstanceId) other;
		return ( this.key.equals( otherId.key));
	}
	
	@Override
	public int hashCode() {
		return key.hashCode();
	}
	
	@Override
	public String toString(){
		if( this.key == MotorInstanceId.ERROR_KEY){
			return MotorInstanceId.ERROR_ID_TEXT;
		}else{
			return name;
		}
	}
}
