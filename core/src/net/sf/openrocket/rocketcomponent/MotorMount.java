package net.sf.openrocket.rocketcomponent;

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
