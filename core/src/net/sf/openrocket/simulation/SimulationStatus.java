package net.sf.openrocket.simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.openrocket.aerodynamics.AerodynamicCalculator;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.motor.MotorConfigurationId;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Monitorable;
import net.sf.openrocket.util.MonitorableSet;
import net.sf.openrocket.util.Quaternion;
import net.sf.openrocket.util.WorldCoordinate;

/**
 * A holder class for the dynamic status during the rocket's flight.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class SimulationStatus implements Monitorable {
	
	private SimulationConditions simulationConditions;
	private FlightConfiguration configuration;
	private FlightDataBranch flightData;
	
	private double time;
	
	private double previousTimeStep;
	
	private Coordinate position;
	private WorldCoordinate worldPosition;
	private Coordinate velocity;
	
	private Quaternion orientation;
	private Coordinate rotationVelocity;
	
	private double effectiveLaunchRodLength;
	
	// Set of all motors	
	private List<MotorClusterState> motorStateList = new ArrayList<MotorClusterState>();
	 
	
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
	
	/** Set to true to indicate the rocket is tumbling. */
	private boolean tumbling = false;

	/** Set to true to indicate rocket has landed */
	private boolean landed = false;
	
	/** Contains a list of deployed recovery devices. */
	private MonitorableSet<RecoveryDevice> deployedRecoveryDevices = new MonitorableSet<RecoveryDevice>();
	
	/** The flight event queue */
	private final EventQueue eventQueue = new EventQueue();
	
	private WarningSet warnings;
	
	/** Available for special purposes by the listeners. */
	private final Map<String, Object> extraData = new HashMap<String, Object>();
	
	double maxAlt = Double.NEGATIVE_INFINITY;
	double maxAltTime = 0;
	
	private int modID = 0;
	private int modIDadd = 0;
	
	public SimulationStatus(FlightConfiguration configuration, SimulationConditions simulationConditions) {
		
		this.simulationConditions = simulationConditions;
		this.configuration = configuration;
		
		this.time = 0;
		this.previousTimeStep = this.simulationConditions.getTimeStep();
		this.position = this.simulationConditions.getLaunchPosition();
		this.velocity = this.simulationConditions.getLaunchVelocity();
		this.worldPosition = this.simulationConditions.getLaunchSite();
		
		// Initialize to roll angle with least stability w.r.t. the wind
		Quaternion o;
		FlightConditions cond = new FlightConditions(this.configuration);
		double angle = -cond.getTheta() - (Math.PI / 2.0 - this.simulationConditions.getLaunchRodDirection());
		o = Quaternion.rotation(new Coordinate(0, 0, angle));
		
		// Launch rod angle and direction
		o = o.multiplyLeft(Quaternion.rotation(new Coordinate(0, this.simulationConditions.getLaunchRodAngle(), 0)));
		o = o.multiplyLeft(Quaternion.rotation(new Coordinate(0, 0, Math.PI / 2.0 - this.simulationConditions.getLaunchRodDirection())));
		
		this.orientation = o;
		this.rotationVelocity = Coordinate.NUL;
		
		/*
		 * Calculate the effective launch rod length taking into account launch lugs.
		 * If no lugs are found, assume a tower launcher of full length.
		 */
		double length = this.simulationConditions.getLaunchRodLength();
		double lugPosition = Double.NaN;
		for (RocketComponent c : this.configuration.getActiveComponents()) {
			if (c instanceof LaunchLug) {
				double pos = c.toAbsolute(new Coordinate(c.getLength()))[0].x;
				if (Double.isNaN(lugPosition) || pos > lugPosition) {
					lugPosition = pos;
				}
			}
		}
		if (!Double.isNaN(lugPosition)) {
			double maxX = 0;
			for (Coordinate c : this.configuration.getBounds()) {
				if (c.x > maxX)
					maxX = c.x;
			}
			if (maxX >= lugPosition) {
				length = Math.max(0, length - (maxX - lugPosition));
			}
		}
		this.effectiveLaunchRodLength = length;
		
		this.simulationStartWallTime = System.nanoTime();
		
		this.motorIgnited = false;
		this.liftoff = false;
		this.launchRodCleared = false;
		this.apogeeReached = false;
		
		this.populateMotors();
		this.warnings = new WarningSet();
	}
	
	/**
	 * Performs a deep copy of the on SimulationStatus object.
	 * Most included object are deep-cloned, except for the flight data object (which is shallow copied)
	 * and the WarningSet (which is initialized to a new WarningSet).
	 * The intention of this constructor is to be used for conversion from one type
	 * of SimulationStatus to another, or when simulating multiple stages.
	 * When used for simulating multiple stages, a new FlightDataBranch object
	 * needs to be associated with the new object.
	 * 
	 * @param orig	the object from which to copy
	 */
	public SimulationStatus(SimulationStatus orig) {
		this.simulationConditions = orig.simulationConditions.clone();
		this.configuration = orig.configuration.clone();
		// FlightData is not cloned.
		this.flightData = orig.flightData;
		this.time = orig.time;
		this.previousTimeStep = orig.previousTimeStep;
		this.position = orig.position;
		this.worldPosition = orig.worldPosition;
		this.velocity = orig.velocity;
		this.orientation = orig.orientation;
		this.rotationVelocity = orig.rotationVelocity;
		this.effectiveLaunchRodLength = orig.effectiveLaunchRodLength;
		this.simulationStartWallTime = orig.simulationStartWallTime;
		this.motorIgnited = orig.motorIgnited;
		this.liftoff = orig.liftoff;
		this.launchRodCleared = orig.launchRodCleared;
		this.apogeeReached = orig.apogeeReached;
		this.tumbling = orig.tumbling;
		this.landed = orig.landed;
		
		this.configuration.copyStages(orig.configuration);
		
		this.deployedRecoveryDevices.clear();
		this.deployedRecoveryDevices.addAll(orig.deployedRecoveryDevices);
		
		this.motorStateList.clear();
		this.motorStateList.addAll(orig.motorStateList);
		
		this.eventQueue.clear();
		this.eventQueue.addAll(orig.eventQueue);
		
		// WarningSet is not cloned.
		this.warnings = new WarningSet();
		
		this.extraData.clear();
		this.extraData.putAll(orig.extraData);
		
		this.modID = orig.modID;
		this.modIDadd = orig.modIDadd;
	}
	
	public void setSimulationTime(double time) {
		this.time = time;
		this.modID++;
	}
	
	
	public double getSimulationTime() {
		return time;
	}
		
	public void setConfiguration(FlightConfiguration configuration) {
		if (this.configuration != null)
			this.modIDadd += this.configuration.getModID();
		this.modID++;
		this.configuration = configuration;
	}
	
	public Collection<MotorClusterState> getMotors() {
		return motorStateList;
	}
	
	public Collection<MotorClusterState> getActiveMotors() {
		List<MotorClusterState> activeList = new ArrayList<MotorClusterState>();
		for( MotorClusterState state: this.motorStateList ){
			if(( ! state.isSpent()) && (this.configuration.isComponentActive( state.getMount()))){
				activeList.add( state );
			}
		}
		
		return activeList;
	}
	
	public FlightConfiguration getConfiguration() {
		return configuration;
	}
	
	public FlightConfiguration getFlightConfiguration() {
		return configuration;
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
	
	public void setRocketWorldPosition(WorldCoordinate wc) {
		this.worldPosition = wc;
		this.modID++;
	}
	
	public WorldCoordinate getRocketWorldPosition() {
		return worldPosition;
	}
	
	public void setRocketVelocity(Coordinate velocity) {
		this.velocity = velocity;
		this.modID++;
	}
	
	
	public Coordinate getRocketVelocity() {
		return velocity;
	}
	
	
	public boolean moveBurntOutMotor( final MotorConfigurationId motor) {
		// get motor from normal list
		// remove motor from 'normal' list
		// add to spent list
		return false;
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
	
	
	public void setTumbling(boolean tumbling) {
		this.tumbling = tumbling;
		this.modID++;
	}
	
	public boolean isTumbling() {
		return tumbling;
	}

	public void setLanded(boolean landed) {
		this.landed = landed;
		this.modID++;
	}

	public boolean isLanded() {
		return landed;
	}
	
	public double getMaxAlt() {
		return maxAlt;
	}
	
	public void setMaxAlt(double maxAlt) {
		this.maxAlt = maxAlt;
		this.modID++;
	}
	
	public double getMaxAltTime() {
		return maxAltTime;
	}
	
	public void setMaxAltTime(double maxAltTime) {
		this.maxAltTime = maxAltTime;
		this.modID++;
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
	
	@Override
	public int getModID() {
		return (modID + modIDadd + simulationConditions.getModID() + configuration.getModID() +
				flightData.getModID() + deployedRecoveryDevices.getModID() +
				eventQueue.getModID() + warnings.getModID());
	}
	
	public String toEventDebug(){
		final StringBuilder buf = new StringBuilder("");
		for ( FlightEvent event : this.eventQueue){
			buf.append("      [t:"+event.getType()+" @"+ event.getTime());
			if( null != event.getSource()){
				buf.append("  src:"+event.getSource().getName());
			}
			if( null != event.getData()){
				buf.append("  data:"+event.getData().getClass().getSimpleName());
			}
			buf.append("]\n");
		}
		return buf.toString();
	}
	
	public String toMotorsDebug(){
		final StringBuilder buf = new StringBuilder("MotorState list:\n");
		for ( MotorClusterState state : this.motorStateList){
			buf.append("          ["+state.toDescription()+"]\n");
		}
		return buf.toString();
	}
	
	private void populateMotors(){
		motorStateList.clear();
		for( MotorConfiguration motorConfig : this.configuration.getAllMotors() ) {
			MotorClusterState simMotor  = new MotorClusterState( motorConfig);
			this.motorStateList.add( simMotor); 
		}
	}

}
