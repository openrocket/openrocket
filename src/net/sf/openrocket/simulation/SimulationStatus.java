package net.sf.openrocket.simulation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.motor.MotorInstanceConfiguration;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Monitorable;
import net.sf.openrocket.util.MonitorableSet;
import net.sf.openrocket.util.Quaternion;

/**
 * A holder class for the dynamic status during the rocket's flight.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SimulationStatus implements Cloneable, Monitorable {
	
	/*
	 * NOTE!  All fields must be added to copyFrom() method!!
	 */

	private SimulationConditions simulationConditions;
	private Configuration configuration;
	private MotorInstanceConfiguration motorConfiguration;
	private FlightDataBranch flightData;
	
	private double time;
	
	private double previousTimeStep;
	
	private Coordinate position;
	private Coordinate velocity;
	
	private Quaternion orientation;
	private Coordinate rotationVelocity;
	
	private double effectiveLaunchRodLength;
	

	/** Nanosecond time when the simulation was started. */
	private long simulationStartWallTime = Long.MIN_VALUE;
	

	/** Set to true when a motor has ignited. */
	private boolean motorIgnited = false;
	
	/** Set to true when the rocket has risen from the ground. */
	private boolean liftoff = false;
	
	/** Set to true when the launch rod has been cleared. */
	private boolean launchRodCleared = false;
	
	/** Set to true when apogee has been detected. */
	private boolean apogeeReached = false;
	
	/** Contains a list of deployed recovery devices. */
	private MonitorableSet<RecoveryDevice> deployedRecoveryDevices = new MonitorableSet<RecoveryDevice>();
	
	/** The flight event queue */
	private final EventQueue eventQueue = new EventQueue();
	
	private WarningSet warnings;
	
	/** Available for special purposes by the listeners. */
	private final Map<String, Object> extraData = new HashMap<String, Object>();
	

	private int modID = 0;
	private int modIDadd = 0;
	
	
	public void setSimulationTime(double time) {
		this.time = time;
		this.modID++;
	}
	
	
	public double getSimulationTime() {
		return time;
	}
	
	
	public void setConfiguration(Configuration configuration) {
		if (this.configuration != null)
			this.modIDadd += this.configuration.getModID();
		this.modID++;
		this.configuration = configuration;
	}
	
	
	public Configuration getConfiguration() {
		return configuration;
	}
	
	
	public void setMotorConfiguration(MotorInstanceConfiguration motorConfiguration) {
		if (this.motorConfiguration != null)
			this.modIDadd += this.motorConfiguration.getModID();
		this.modID++;
		this.motorConfiguration = motorConfiguration;
	}
	
	
	public MotorInstanceConfiguration getMotorConfiguration() {
		return motorConfiguration;
	}
	
	
	public void setFlightData(FlightDataBranch flightData) {
		if (this.flightData != null)
			this.modIDadd += this.flightData.getModID();
		this.modID++;
		this.flightData = flightData;
	}
	
	
	public FlightDataBranch getFlightData() {
		return flightData;
	}
	
	
	public double getPreviousTimeStep() {
		return previousTimeStep;
	}
	
	
	public void setPreviousTimeStep(double previousTimeStep) {
		this.previousTimeStep = previousTimeStep;
		this.modID++;
	}
	
	
	public void setRocketPosition(Coordinate position) {
		this.position = position;
		this.modID++;
	}
	
	
	public Coordinate getRocketPosition() {
		return position;
	}
	
	
	public void setRocketVelocity(Coordinate velocity) {
		this.velocity = velocity;
		this.modID++;
	}
	
	
	public Coordinate getRocketVelocity() {
		return velocity;
	}
	
	



	public Quaternion getRocketOrientationQuaternion() {
		return orientation;
	}
	
	
	public void setRocketOrientationQuaternion(Quaternion orientation) {
		this.orientation = orientation;
		this.modID++;
	}
	
	
	public Coordinate getRocketRotationVelocity() {
		return rotationVelocity;
	}
	
	
	public void setRocketRotationVelocity(Coordinate rotation) {
		this.rotationVelocity = rotation;
	}
	
	
	public void setEffectiveLaunchRodLength(double effectiveLaunchRodLength) {
		this.effectiveLaunchRodLength = effectiveLaunchRodLength;
		this.modID++;
	}
	
	
	public double getEffectiveLaunchRodLength() {
		return effectiveLaunchRodLength;
	}
	
	
	public void setSimulationStartWallTime(long simulationStartWallTime) {
		this.simulationStartWallTime = simulationStartWallTime;
		this.modID++;
	}
	
	
	public long getSimulationStartWallTime() {
		return simulationStartWallTime;
	}
	
	
	public void setMotorIgnited(boolean motorIgnited) {
		this.motorIgnited = motorIgnited;
		this.modID++;
	}
	
	
	public boolean isMotorIgnited() {
		return motorIgnited;
	}
	
	
	public void setLiftoff(boolean liftoff) {
		this.liftoff = liftoff;
		this.modID++;
	}
	
	
	public boolean isLiftoff() {
		return liftoff;
	}
	
	
	public void setLaunchRodCleared(boolean launchRod) {
		this.launchRodCleared = launchRod;
		this.modID++;
	}
	
	
	public boolean isLaunchRodCleared() {
		return launchRodCleared;
	}
	
	
	public void setApogeeReached(boolean apogeeReached) {
		this.apogeeReached = apogeeReached;
		this.modID++;
	}
	
	
	public boolean isApogeeReached() {
		return apogeeReached;
	}
	
	
	public Set<RecoveryDevice> getDeployedRecoveryDevices() {
		return deployedRecoveryDevices;
	}
	
	
	public void setWarnings(WarningSet warnings) {
		if (this.warnings != null)
			this.modIDadd += this.warnings.getModID();
		this.modID++;
		this.warnings = warnings;
	}
	
	
	public WarningSet getWarnings() {
		return warnings;
	}
	
	
	public EventQueue getEventQueue() {
		return eventQueue;
	}
	
	
	public void setSimulationConditions(SimulationConditions simulationConditions) {
		if (this.simulationConditions != null)
			this.modIDadd += this.simulationConditions.getModID();
		this.modID++;
		this.simulationConditions = simulationConditions;
	}
	
	
	public SimulationConditions getSimulationConditions() {
		return simulationConditions;
	}
	
	
	/**
	 * Store extra data available for use by simulation listeners.  The data can be retrieved
	 * using {@link #getExtraData(String)}.
	 * 
	 * @param key		the data key
	 * @param value		the value to store
	 */
	public void putExtraData(String key, Object value) {
		extraData.put(key, value);
	}
	
	/**
	 * Retrieve extra data stored by simulation listeners.  This data map is initially empty.
	 * Data can be stored using {@link #putExtraData(String, Object)}.
	 * 
	 * @param key		the data key to retrieve
	 * @return			the data, or <code>null</code> if nothing has been set for the key
	 */
	public Object getExtraData(String key) {
		return extraData.get(key);
	}
	
	
	/**
	 * Returns a copy of this object.  The general purpose is that the conditions,
	 * rocket configuration, flight data etc. point to the same objects.  However,
	 * subclasses are allowed to deep-clone specific objects, such as those pertaining
	 * to the current orientation of the rocket.  The purpose is to allow creating intermediate
	 * copies of this object used during step computation.
	 * 
	 * TODO: HIGH: Deep cloning required for branch saving.
	 */
	@Override
	public SimulationStatus clone() {
		try {
			SimulationStatus clone = (SimulationStatus) super.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new BugException("CloneNotSupportedException?!?", e);
		}
	}
	
	
	/**
	 * Copies the data from the provided object to this object.  Most included object are
	 * deep-cloned, except for the flight data object.
	 * 
	 * @param orig	the object from which to copy
	 */
	public void copyFrom(SimulationStatus orig) {
		this.simulationConditions = orig.simulationConditions.clone();
		this.configuration = orig.configuration.clone();
		this.motorConfiguration = orig.motorConfiguration.clone();
		this.flightData = orig.flightData;
		this.time = orig.time;
		this.previousTimeStep = orig.previousTimeStep;
		this.position = orig.position;
		this.velocity = orig.velocity;
		this.orientation = orig.orientation;
		this.rotationVelocity = orig.rotationVelocity;
		this.effectiveLaunchRodLength = orig.effectiveLaunchRodLength;
		this.simulationStartWallTime = orig.simulationStartWallTime;
		this.motorIgnited = orig.motorIgnited;
		this.liftoff = orig.liftoff;
		this.launchRodCleared = orig.launchRodCleared;
		this.apogeeReached = orig.apogeeReached;
		
		this.deployedRecoveryDevices.clear();
		this.deployedRecoveryDevices.addAll(orig.deployedRecoveryDevices);
		
		this.eventQueue.clear();
		this.eventQueue.addAll(orig.eventQueue);
		
		this.warnings = orig.warnings;
		
		this.extraData.clear();
		this.extraData.putAll(orig.extraData);
		
		this.modID = orig.modID;
		this.modIDadd = orig.modIDadd;
	}
	
	
	@Override
	public int getModID() {
		return (modID + modIDadd + simulationConditions.getModID() + configuration.getModID() +
				motorConfiguration.getModID() + flightData.getModID() + deployedRecoveryDevices.getModID() +
				eventQueue.getModID() + warnings.getModID());
	}
	

}
