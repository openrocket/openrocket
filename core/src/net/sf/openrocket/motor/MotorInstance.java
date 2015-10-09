package net.sf.openrocket.motor;

import java.util.EventObject;
import java.util.List;

import net.sf.openrocket.models.atmosphere.AtmosphericConditions;
import net.sf.openrocket.rocketcomponent.FlightConfigurableParameter;
import net.sf.openrocket.rocketcomponent.IgnitionEvent;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.StateChangeListener;

/**
 * A single motor configuration.  This includes the selected motor
 * and the ejection charge delay.
 */
public class MotorInstance implements FlightConfigurableParameter<MotorInstance> {
	
	protected MotorInstanceId id = null;	
	protected MotorMount mount = null;
	//protected Motor motor = null;  // deferred to subclasses
	protected double ejectionDelay = 0.0;
	protected double ignitionDelay = 0.0;
	protected IgnitionEvent ignitionEvent = IgnitionEvent.NEVER;
	protected Coordinate position = Coordinate.ZERO;
	protected double ignitionTime = 0.0;
	
	// comparison threshold
	private static final double EPSILON = 0.01;
	
	protected int modID = 0;
	private final List<StateChangeListener> listeners = new ArrayList<StateChangeListener>();
	
	/** Immutable configuration with no motor and zero delay. */
	public static final MotorInstance EMPTY_INSTANCE = new MotorInstance();
	
	protected MotorInstance() {
		this.id = MotorInstanceId.EMPTY_ID;
		modID++;
	}
	
	public MotorInstanceId getID() {
		return this.id;
	}
	
	public void setID(final MotorInstanceId _id) {
		this.id = _id;
	}
	
	public double getEjectionDelay() {
		return this.ejectionDelay;
	}
	
	public void setMotor(Motor motor) {
		throw new UnsupportedOperationException("Retrieve a motor from an immutable no-motors instance");
	}

	public Motor getMotor() {
		throw new UnsupportedOperationException("Retrieve a motor from an immutable no-motors instance");
	}
	
	public void setEjectionDelay(double delay) {
		throw new UnsupportedOperationException("Trying to modify immutable no-motors configuration");
	};
	
	public MotorMount getMount() {
		return this.mount;
	}
	
	public void setMount(final MotorMount _mount) {
		this.mount = _mount;
	}
	
	public Coordinate getPosition() {
		return this.position;
	}
	
	public void setPosition(Coordinate _position) {
		this.position = _position;
		modID++;
	}
		
	public double getIgnitionTime() {
		return this.ignitionTime;
	}
	
	public void setIgnitionTime(double _time) {
		this.ignitionTime = _time;
		modID++;
	}
	
	public double getIgnitionDelay() {
		return this.ignitionDelay;
	}
	
	public void setIgnitionDelay(final double _delay) {
		this.ignitionDelay = _delay;
	}
	
	public IgnitionEvent getIgnitionEvent() {
		return this.ignitionEvent;
	}
	
	public void setIgnitionEvent(final IgnitionEvent _event) {
		this.ignitionEvent = _event;
	}
	
	/**
	 * Step the motor instance forward in time.
	 * 
	 * @param time			the time to step to, from motor ignition.
	 * @param acceleration	the average acceleration during the step.
	 * @param cond			the average atmospheric conditions during the step.
	 */
	public void step(double time, double acceleration, AtmosphericConditions cond) {
		// no-op
	}
	
	
	/**
	 * Return the time to which this motor has been stepped.
	 * @return	the current step time.
	 */
	public double getTime() {
		return 0;
	}
	
	/**
	 * Return the average thrust during the last step.
	 */
	public double getThrust() {
		return Double.NaN;
	}
	
	/**
	 * Return the average CG location during the last step.
	 */
	public Coordinate getCG() {
		return Coordinate.NaN;
	}
	
	/**
	 * Return the average longitudinal moment of inertia during the last step.
	 * This is the actual inertia, not the unit inertia!
	 */
	public double getLongitudinalInertia() {
		return Double.NaN;
	}
	
	/**
	 * Return the average rotational moment of inertia during the last step.
	 * This is the actual inertia, not the unit inertia!
	 */
	public double getRotationalInertia() {
		return Double.NaN;
	}
	
	/**
	 * Return whether this motor still produces thrust.  If this method returns false
	 * the motor has burnt out, and will not produce any significant thrust anymore.
	 */
	public boolean isActive() {
		return false;
	}
	
	public boolean isEmpty(){
		return this == MotorInstance.EMPTY_INSTANCE;
	}
	
	@Override 
	public boolean equals( Object other ){
		if( other == null )
			return false;
		if( other instanceof MotorInstance ){
			MotorInstance omi = (MotorInstance)other;
			if( this.id.equals( omi.id)){
				return true;
			}else if( this.mount != omi.mount ){
				return false;
			}else if( this.ignitionEvent == omi.ignitionEvent ){
				return false;
			}else if( EPSILON < Math.abs(this.ignitionDelay - omi.ignitionDelay )){
				return false;
			}else if( EPSILON < Math.abs( this.ejectionDelay - omi.ejectionDelay )){
				return false;
			}else if( ! this.position.equals( omi.position )){
				return false;
			}else if( EPSILON < Math.abs( this.ignitionTime - omi.ignitionTime )){
				return false;
			}
			
			return true;	
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}
	
	/**
	 * Create a new instance of this motor instance.  The state of the motor is
	 * identical to this instance and can be used independently from this one.
	 */
	@Override
	public MotorInstance clone( ){
		return EMPTY_INSTANCE;
	}
	
	@Override
	public void addChangeListener(StateChangeListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public void removeChangeListener(StateChangeListener listener) {
		listeners.remove(listener);
	}
	
	protected void fireChangeEvent() {
		EventObject event = new EventObject(this);
		Object[] list = listeners.toArray();
		for (Object l : list) {
			((StateChangeListener) l).stateChanged(event);
		}
	}

	
	public int getModID() {
		return modID;
	}
	
}
