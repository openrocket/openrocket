package net.sf.openrocket.rocketcomponent;

import java.util.Iterator;

import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.motor.MotorConfigurationSet;
import net.sf.openrocket.util.ChangeSource;
import net.sf.openrocket.util.Coordinate;

public interface MotorMount extends ChangeSource, FlightConfigurableComponent {
	

	/**
	 * Does this mount contain at least one motor?  
	 * 
	 * @return  whether the component holds a motor
	 */
	public boolean hasMotor();

    /**
     * Programmatically : implementing classes will always be <code>(x instanceof MotorMount)</code>
     * The component may potentially act as a mount, or just a structural component.
     * This flag indicates how the component behaves. 
     * 
     *  @param acting if the component should behave like a motor mount.  False if it's structural only. 
     */
    public void setMotorMount(boolean acting);
    
	/**
     * Programmatically : implementing classes will always be <code>(x instanceof MotorMount)</code>
     * This flag indicates whether the component is acting as a motor mount, or just a structural component
     *  
	 * @return true if the component is acting as a motor mount
	 */
	public boolean isMotorMount();
	
	/**
	 * Get all motors configured for this mount.
	 * 
	 * @return an iterator to all motors configured for this component
	 */
	public Iterator<MotorConfiguration> getMotorIterator();

	/**
	 *   Returns the Default Motor Instance for this mount.
	 *   
	 *    @return The default MotorInstance
	 */
	public MotorConfiguration getDefaultMotorConfig();
	
	/** 
	 * Default implementatino supplied by RocketComponent (returns 1);
	 * 
	 * @return number of times this component is instanced
	 */
	public int getInstanceCount();


	/**
	 * Get the current cluster configuration.
	 * @return  The current cluster configuration.
	 */ 
	public ClusterConfiguration getClusterConfiguration();
	
	/** 
	 * Get the length of this motor mount.  Synonymous with the RocketComponent method. 
	 * 
	 * @return
	 */
	public double getLength();

	// duplicate of RocketComponent
	public String getID();

	// duplicate of RocketComponent 
	public String getDebugName();
	
	// duplicate of RocketComponent 
	public AxialStage getStage();
	
	// duplicate of RocketComponent 
	public Coordinate[] getLocations();
	
	/**
	 * Returns the set of motors configured for flight/simulation in this motor mount.
	 * @return the MotorConfigurationSet containing the set of motors configured in
	 *         this motor mount.
	 */
	public MotorConfigurationSet getMotorConfigurationSet();
	
	/**
	 * 
	 * @param fcid  id for which to return the motor (null retrieves the default)
	 * @return  requested motorInstance (which may also be the default motor instance)
	 */
	public MotorConfiguration getMotorConfig( final FlightConfigurationId fcid);

	/**
	 * 
	 * @param fcid index the supplied motor against this flight configuration 
	 * @param newMotorInstance  motor instance to store
	 */
	public void setMotorConfig( final MotorConfiguration newMotorConfig, final FlightConfigurationId fcid);
	
	/**
	 * Get the number of motors available for all flight configurations
	 * 
	 * @return  the number of motors.
	 */
	public int getMotorCount();
	
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
	public Coordinate getMotorPosition(FlightConfigurationId id);
	
	/**
	 * Development / Debug method.
	 * 
	 * @return table describing all the motors configured for this mount.
	 */
	public String toMotorDebug( );
}
