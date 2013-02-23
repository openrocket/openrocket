package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.util.ChangeSource;
import net.sf.openrocket.util.Coordinate;

public interface MotorMount extends ChangeSource, FlightConfigurableComponent {
	
	/**
	 * Is the component currently a motor mount.
	 * 
	 * @return  whether the component holds a motor.
	 */
	public boolean isMotorMount();
	
	/**
	 * Set whether the component is currently a motor mount.
	 */
	public void setMotorMount(boolean mount);
	
	
	/**
	 * Return the motor configurations for this motor mount.
	 */
	public FlightConfiguration<MotorConfiguration> getMotorConfiguration();
	
	/**
	 * Return the ignition configurations for this motor mount.
	 */
	public FlightConfiguration<IgnitionConfiguration> getIgnitionConfiguration();
	
	
	/**
	 * Return the motor for the motor configuration.  May return <code>null</code>
	 * if no motor has been set.  This method must return <code>null</code> if ID
	 * is <code>null</code> or if the ID is not valid for the current rocket
	 * (or if the component is not part of any rocket).
	 * 
	 * @param id	the motor configuration ID
	 * @return  	the motor, or <code>null</code> if not set.
	 * @deprecated	Use getMotorConfiguration().get(id).getMotor() instead.
	 */
	@Deprecated
	public Motor getMotor(String id);
	
	/**
	 * Get the number of similar motors clustered.
	 * 
	 * TODO: HIGH: This should not be used, since the components themselves can be clustered
	 * 
	 * @return  the number of motors.
	 */
	@Deprecated
	public int getMotorCount();
	
	
	
	/**
	 * Return the ejection charge delay of given motor configuration.
	 * A "plugged" motor without an ejection charge is given by
	 * {@link Motor#PLUGGED} (<code>Double.POSITIVE_INFINITY</code>).
	 * 
	 * @param id	the motor configuration ID
	 * @return  	the ejection charge delay.
	 * @deprecated	Use getMotorConfiguration().get(id).getMotor() instead.
	 */
	@Deprecated
	public double getMotorDelay(String id);
	
	
	
	/**
	 * Return the distance that the motors hang outside this motor mount.
	 * 
	 * @return  the overhang length.
	 */
	public double getMotorOverhang();
	
	/**
	 * Sets the distance that the motors hang outside this motor mount.
	 * 
	 * @param overhang   the overhang length.
	 */
	public void setMotorOverhang(double overhang);
	
	
	
	/**
	 * Return the inner diameter of the motor mount.
	 * 
	 * @return  the inner diameter of the motor mount.
	 */
	public double getMotorMountDiameter();
	
	
	/**
	 * Return the position of the motor relative to this component.  The coordinate
	 * is that of the front cap of the motor.
	 * 
	 * @return	the position of the motor relative to this component.
	 * @throws  IllegalArgumentException if a motor with the specified ID does not exist.
	 */
	public Coordinate getMotorPosition(String id);
	
}
