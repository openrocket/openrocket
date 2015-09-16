package net.sf.openrocket.motor;

import net.sf.openrocket.models.atmosphere.AtmosphericConditions;
import net.sf.openrocket.rocketcomponent.IgnitionConfiguration;
import net.sf.openrocket.rocketcomponent.IgnitionConfiguration.IgnitionEvent;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Monitorable;

public abstract class MotorInstance implements Cloneable, Monitorable {
	
	protected MotorId id = null;
	protected Motor parentMotor = null;
	protected MotorMount mount = null;
	protected IgnitionConfiguration.IgnitionEvent ignitionEvent = null;
	protected double ejectionDelay = 0.0;
	protected double ignitionDelay = 0.0;
	protected Coordinate position = null;
	protected double ignitionTime = 0.0;
	
	protected int modID = 0;
	
	public MotorInstance() {
		
		modID++;
	}
	
	/**
	 * Add a motor instance to this configuration.  The motor is placed at
	 * the specified position and with an infinite ignition time (never ignited).
	 * 
	 * @param id			the ID of this motor instance.
	 * @param motor			the motor instance.
	 * @param mount			the motor mount containing this motor
	 * @param ignitionEvent	the ignition event for the motor
	 * @param ignitionDelay	the ignition delay for the motor
	 * @param position		the position of the motor in absolute coordinates.
	 * @throws IllegalArgumentException	if a motor with the specified ID already exists.
	 */
	public MotorInstance(MotorId _id, Motor _motor, MotorMount _mount, double _ejectionDelay,
			IgnitionEvent _ignitionEvent, double _ignitionDelay, Coordinate _position) {
		
		this.id = _id;
		this.parentMotor = _motor;
		this.mount = _mount;
		this.ejectionDelay = _ejectionDelay;
		this.ignitionEvent = _ignitionEvent;
		this.ignitionDelay = _ignitionDelay;
		this.position = _position;
		this.ignitionTime = Double.POSITIVE_INFINITY;
		
		modID++;
	}
	
	public MotorId getID() {
		return this.id;
	}
	
	public void setID(final MotorId _id) {
		this.id = _id;
	}
	
	public double getEjectionDelay() {
		return this.ejectionDelay;
	}
	
	public void setEjectionDelay(final double newDelay) {
		this.ejectionDelay = newDelay;
	}
	
	@Override
	public int getModID() {
		return this.modID;
	}
	
	public Motor getMotor() {
		return this.parentMotor;
	}
	
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
		return ignitionDelay;
	}
	
	public void setIgnitionDelay(final double _delay) {
		this.ignitionDelay = _delay;
	}
	
	public IgnitionEvent getIgnitionEvent() {
		return ignitionEvent;
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
	public abstract double getTime();
	
	/**
	 * Return the average thrust during the last step.
	 */
	public abstract double getThrust();
	
	/**
	 * Return the average CG location during the last step.
	 */
	public abstract Coordinate getCG();
	
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
	public abstract boolean isActive();
	
	
	/**
	 * Create a new instance of this motor instance.  The state of the motor is
	 * identical to this instance and can be used independently from this one.
	 */
	@Override
	public abstract MotorInstance clone();
	
}
