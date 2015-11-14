package net.sf.openrocket.motor;

/**
 * An immutable identifier for a motor instance in a MotorInstanceConfiguration.
 * The motor is identified by the ID of its mounting component and a 
 * positive motor count number.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public final class MotorInstanceId {
	
	private final String componentId;
	private final int number;
	
	private final static String COMPONENT_ERROR_TEXT = "Error Motor Id";
	private final static int ERROR_NUMBER = -1;
	public final static MotorInstanceId ERROR_ID = new MotorInstanceId();
	private final static String EMPTY_COMPONENT_TEXT = "Empty Motor Id";
	private final static int EMPTY_NUMBER = 1;
	public final static MotorInstanceId EMPTY_ID = new MotorInstanceId(EMPTY_COMPONENT_TEXT, EMPTY_NUMBER);
	
	public MotorInstanceId() {
		this.componentId = COMPONENT_ERROR_TEXT;
		this.number = ERROR_NUMBER;
	}
	
	/**
	 * Sole constructor.
	 * 
	 * @param componentId	the component ID, must not be null
	 * @param number		a positive motor number
	 */
	public MotorInstanceId(String componentId, int number) {
		
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
	
	public int getInstanceNumber() {
		return number;
	}
	
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
			
		if (!(o instanceof MotorInstanceId))
			return false;
			
		MotorInstanceId other = (MotorInstanceId) o;
		// Comparison with == ok since string is intern()'ed
		return this.componentId == other.componentId && this.number == other.number;
	}
	
	
	@Override
	public int hashCode() {
		return componentId.hashCode() + (number << 12);
	}
	
	@Override
	public String toString(){
		if( this == ERROR_ID){
			return "ERROR_ID";
		}else if( this == EMPTY_ID){
			return "EMPTY_ID";
		}else{
			return Integer.toString( this.hashCode());
		}
	}
}
