package net.sf.openrocket.simulation;

import java.util.HashSet;
import java.util.Set;

import net.sf.openrocket.aerodynamics.GravityModel;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.aerodynamics.WindSimulator;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.util.Coordinate;


public class SimulationStatus implements Cloneable {
	
	public SimulationConditions startConditions;

	public double time;
	public Configuration configuration;
	public FlightDataBranch flightData;
	
	public Coordinate position;
	public Coordinate velocity;
	
	public WindSimulator windSimulator;
	public GravityModel gravityModel;
	
	public double launchRodLength;
	
	
	/** Nanosecond time when the simulation was started. */
	public long simulationStartTime = Long.MIN_VALUE;
	
	
	/** Set to true when a motor has ignited. */
	public boolean motorIgnited = false;
	
	/** Set to true when the rocket has risen from the ground. */
	public boolean liftoff = false;
	
	/** <code>true</code> while the rocket is on the launch rod. */
	public boolean launchRod = true;

	/** Set to true when apogee has been detected. */
	public boolean apogeeReached = false;
	 
	/** Contains a list of deployed recovery devices. */
	public final Set<RecoveryDevice> deployedRecoveryDevices = new HashSet<RecoveryDevice>();
	
	
	public WarningSet warnings;
	
	
	/** Available for special purposes by the listeners. */
	public Object extra = null;
	
	
	/**
	 * Returns a (shallow) copy of this object.  The general purpose is that the 
	 * conditions, flight data etc. point to the same objects.  However, subclasses 
	 * are allowed to deep-clone specific objects, such as those pertaining to the 
	 * current orientation of the rocket.
	 */
	@Override
	public SimulationStatus clone() {
		try {
			return (SimulationStatus) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("BUG:  CloneNotSupportedException?!?",e);
		}
	}
}
