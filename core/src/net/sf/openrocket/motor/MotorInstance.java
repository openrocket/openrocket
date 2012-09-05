package net.sf.openrocket.motor;

import net.sf.openrocket.models.atmosphere.AtmosphericConditions;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Monitorable;

public interface MotorInstance extends Cloneable, Monitorable {

	/**
	 * Step the motor instance forward in time.
	 * 
	 * @param time			the time to step to, from motor ignition.
	 * @param acceleration	the average acceleration during the step.
	 * @param cond			the average atmospheric conditions during the step.
	 */
	public void step(double time, double acceleration, AtmosphericConditions cond);


	/**
	 * Return the time to which this motor has been stepped.
	 * @return	the current step time.
	 */
	public double getTime();
	
	/**
	 * Return the average thrust during the last step.
	 */
	public double getThrust();
	
	/**
	 * Return the average CG location during the last step.
	 */
	public Coordinate getCG();
	
	/**
	 * Return the average longitudinal moment of inertia during the last step.
	 * This is the actual inertia, not the unit inertia!
	 */
	public double getLongitudinalInertia();
	
	/**
	 * Return the average rotational moment of inertia during the last step.
	 * This is the actual inertia, not the unit inertia!
	 */
	public double getRotationalInertia();

	/**
	 * Return whether this motor still produces thrust.  If this method returns false
	 * the motor has burnt out, and will not produce any significant thrust anymore.
	 */
	public boolean isActive();

	
	/**
	 * Create a new instance of this motor instance.  The state of the motor is
	 * identical to this instance and can be used independently from this one.
	 */
	public MotorInstance clone();


	public Motor getParentMotor();
	
}
