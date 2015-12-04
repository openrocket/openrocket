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
public abstract class MotorInstance implements FlightConfigurableParameter<MotorInstance> {
	
	// deferred to subclasses
	//protected MotorMount mount = null;
	//protected Motor motor = null;
	
	protected MotorInstanceId id = null;
	protected double ejectionDelay = 0.0;
	protected double ignitionDelay = 0.0;
	protected IgnitionEvent ignitionEvent = IgnitionEvent.NEVER;
	protected Coordinate position = Coordinate.ZERO;
	protected double ignitionTime = 0.0;
	
	
	protected int modID = 0;
	private final List<StateChangeListener> listeners = new ArrayList<StateChangeListener>();
	
	/** Immutable configuration with no motor and zero delay. */
	public static final MotorInstance EMPTY_INSTANCE = new MotorInstance(){
		@Override 
		public boolean equals( Object other ){
			return (this==other);
		}
		
		@Override
		public Motor getMotor() {
			throw new UnsupportedOperationException("Retrieve a motor from an immutable no-motors instance");
		}
		
		@Override
		public MotorMount getMount() {
			throw new UnsupportedOperationException("Retrieve a mount from an immutable no-motors instance");
		}
		
		@Override
		public double getThrust() {
			throw new UnsupportedOperationException("Trying to get thrust from an empty motor instance.");
		}
		
		@Override
		public Coordinate getCM() {
			throw new UnsupportedOperationException("Trying to get Center-of-Mass from an empty motor instance.");
		}
		
		@Override
		public double getPropellantMass(){
			throw new UnsupportedOperationException("Trying to get mass from an empty motor instance.");
		}
		
		@Override
		public double getLongitudinalInertia() {
			throw new UnsupportedOperationException("Trying to get inertia from an empty motor instance.");
		}
		
		@Override
		public double getRotationalInertia() {
			throw new UnsupportedOperationException("Trying to get inertia from an empty motor instance.");
		}	        
		
		@Override
		public void step(double time, double acceleration, AtmosphericConditions cond) {
			throw new UnsupportedOperationException("Cannot step an abstract base class");
		}
	};
	
	protected MotorInstance() {
		this.id = MotorInstanceId.EMPTY_ID;
		ejectionDelay = 0.0;
		ignitionEvent = IgnitionEvent.NEVER;
		ignitionDelay = 0.0;
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
	
	public void setMotor(Motor motor){}
		
	public abstract Motor getMotor();
	
	public void setEjectionDelay(double delay) {}
	
	public abstract MotorMount getMount();
	
	public void setMount(final MotorMount _mount){}
	
	public Coordinate getOffset(){
		return this.position;
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
	public abstract void step(double time, double acceleration, AtmosphericConditions cond);
	
	
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
	public abstract double getThrust();
	
	/**
	 * Return the average CG location during the last step.
	 */
	public Coordinate getCG() {	
		return this.getCM(); 
	}
	
	public abstract Coordinate getCM();
	
	public abstract double getPropellantMass();
	
	/**
	 * Return the average longitudinal moment of inertia during the last step.
	 * This is the actual inertia, not the unit inertia!
	 */
	public abstract double getLongitudinalInertia();
	
	/**
	 * Return the average rotational moment of inertia during the last step.
	 * This is the actual inertia, not the unit inertia!
	 */
	public abstract double getRotationalInertia();
	
	/**
	 * Return whether this motor still produces thrust.  If this method returns false
	 * the motor has burnt out, and will not produce any significant thrust anymore.
	 */
	public boolean isActive() {
		return false;
	}
	
	public boolean isEmpty(){
		return true;
	}

	public boolean hasMotor(){
		return ! this.isEmpty();
	}
	
	@Override 
	public boolean equals( Object other ){
		if( null == other ){
			return false;
		}else if( other instanceof MotorInstance ){
			MotorInstance omi = (MotorInstance)other;
			if( this.id.equals( omi.id)){
				return true;
			}
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

	@Override
	public String toString(){
		return MotorInstanceId.EMPTY_ID.getComponentId();
	}
	
	public int getModID() {
		return modID;
	}
	
}
