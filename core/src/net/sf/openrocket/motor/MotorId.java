package net.sf.openrocket.motor;

/**
 * An immutable identifier for a motor instance in a MotorInstanceConfiguration.
 * The motor is identified by the ID of its mounting component and a 
 * positive motor count number.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public final class MotorId {

	private final String componentId;
	private final int number;
	
	
	/**
	 * Sole constructor.
	 * 
	 * @param componentId	the component ID, must not be null
	 * @param number		a positive motor doun5 number
	 */
	public MotorId(String componentId, int number) {
		super();
		
		if (componentId == null) {
			throw new IllegalArgumentException("Component ID was null");
		}
		if (number <= 0) {
			throw new IllegalArgumentException("Number must be positive, n=" + number);
		}
		
		// Use intern so comparison can be done using == instead of equals()
		this.componentId = componentId.intern();
		this.number = number;
	}
	
	
	public String getComponentId() {
		return componentId;
	}
	
	public int getNumber() {
		return number;
	}
	
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof MotorId))
			return false;
		
		MotorId other = (MotorId)o;
		// Comparison with == ok since string is intern()'ed
		return this.componentId == other.componentId && this.number == other.number;
	}
	
	
	@Override
	public int hashCode() {
		return componentId.hashCode() + (number << 12);
	}
	
	// TODO: toString()
}
