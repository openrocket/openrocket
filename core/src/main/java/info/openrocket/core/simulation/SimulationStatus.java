package info.openrocket.core.simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.logging.SimulationAbort;
import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.motor.MotorConfigurationId;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.LaunchLug;
import info.openrocket.core.rocketcomponent.RecoveryDevice;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.listeners.SimulationListenerHelper;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.ModID;
import info.openrocket.core.util.Monitorable;
import info.openrocket.core.util.MonitorableSet;
import info.openrocket.core.util.Quaternion;
import info.openrocket.core.util.WorldCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A holder class for the dynamic status during the rocket's flight.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 *
 * The initial SimulationStatus is saved to the FlightDataBranch when it is
 * created.
 * The updated SimulationStatus is saved to the FlightDataBranch at the very
 * end of each step.
 */

public class SimulationStatus implements Cloneable, Monitorable {

	// time after leaving launch rod before recording flight event warnings
	private final double WARNINGS_WAIT = 0.25;

	// when our z velocity decreases to this proportion of max z velocity, stop recording
	// most flight event warnings
	private final double WARNINGS_VEL = 0.2;
	
	private static final Logger log = LoggerFactory.getLogger(BasicEventSimulationEngine.class);

	private SimulationConditions simulationConditions;
	private FlightConfiguration configuration;
	private FlightDataBranch flightDataBranch;

	private double time;

	private Coordinate position;
	private WorldCoordinate worldPosition;
	private Coordinate velocity;

	private Quaternion orientation;
	private Coordinate rotationVelocity;

	private double maxZVelocity = Double.NEGATIVE_INFINITY;
	private double startWarningsTime = RK4SimulationStepper.RECOMMENDED_MAX_TIME;
	
	private double effectiveLaunchRodLength;

	// Set of all motors
	private final List<MotorClusterState> motorStateList = new ArrayList<>();

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
	private final MonitorableSet<RecoveryDevice> deployedRecoveryDevices = new MonitorableSet<>();

	/** The flight event queue */
	private final EventQueue eventQueue = new EventQueue();

	private WarningSet warnings;

	/** Available for special purposes by the listeners. */
	private final Map<String, Object> extraData = new HashMap<>();

	double maxAlt = Double.NEGATIVE_INFINITY;
	double maxAltTime = 0;

	private ModID modID = ModID.INVALID;
	private ModID modIDadd = ModID.INVALID;

	public SimulationStatus(FlightConfiguration configuration, SimulationConditions simulationConditions) {

		this.simulationConditions = simulationConditions;
		this.configuration = configuration;

		this.time = 0;
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
	 * @param orig the object from which to copy
	 */
	public SimulationStatus(SimulationStatus orig) {
		this.simulationConditions = orig.simulationConditions.clone();
		this.configuration = orig.configuration.clone();
		// FlightDataBranch is not cloned.
		this.flightDataBranch = orig.flightDataBranch;
		this.time = orig.time;
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
		this.maxZVelocity = orig.maxZVelocity;
		this.startWarningsTime = orig.startWarningsTime;
		
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
		this.modID = new ModID();
	}

	public double getSimulationTime() {
		return time;
	}

	public void setConfiguration(FlightConfiguration configuration) {
		if (this.configuration != null)
			this.modIDadd = new ModID();
		this.configuration = configuration;
	}

	public Collection<MotorClusterState> getMotors() {
		return motorStateList;
	}

	public Collection<MotorClusterState> getActiveMotors() {
		List<MotorClusterState> activeList = new ArrayList<>();
		for (MotorClusterState state : this.motorStateList) {
			if (this.configuration.isComponentActive(state.getMount())) {
				activeList.add(state);
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

	public void setFlightDataBranch(FlightDataBranch flightDataBranch) {
		if (this.flightDataBranch != null)
			this.modIDadd = new ModID();
		this.flightDataBranch = flightDataBranch;
	}

	public FlightDataBranch getFlightDataBranch() {
		return flightDataBranch;
	}

	/**
	 * Set the rocket position relative to the launch site; at t = 0s, equals (0, 0, 0).
	 * @param position the rocket position
	 */
	public void setRocketPosition(Coordinate position) {
		this.position = position;
		modID = new ModID();
	}

	/**
	 * Get the rocket position relative to the launch site; at t = 0s, equals (0, 0, 0).
	 * @return the rocket position
	 */
	public Coordinate getRocketPosition() {
		return position;
	}

	/**
	 * Set the rocket position in world coordinates (including the launch site altitude, longitude, and latitude).
	 * @param wc the rocket position in world coordinates
	 */
	public void setRocketWorldPosition(WorldCoordinate wc) {
		this.worldPosition = wc;
		modID = new ModID();
	}

	/**
	 * Get the rocket position in world coordinates (including the launch site altitude, longitude, and latitude).
	 * @return the rocket position in world coordinates
	 */
	public WorldCoordinate getRocketWorldPosition() {
		return worldPosition;
	}

	public void setRocketVelocity(Coordinate velocity) {
		this.velocity = velocity;
		modID = new ModID();
	}

	public Coordinate getRocketVelocity() {
		return velocity;
	}

	public boolean moveBurntOutMotor(final MotorConfigurationId motor) {
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
		modID = new ModID();
	}

	public Coordinate getRocketRotationVelocity() {
		return rotationVelocity;
	}

	public void setRocketRotationVelocity(Coordinate rotation) {
		this.rotationVelocity = rotation;
	}

	public void setEffectiveLaunchRodLength(double effectiveLaunchRodLength) {
		this.effectiveLaunchRodLength = effectiveLaunchRodLength;
		modID = new ModID();
	}

	public double getEffectiveLaunchRodLength() {
		return effectiveLaunchRodLength;
	}

	public void setSimulationStartWallTime(long simulationStartWallTime) {
		this.simulationStartWallTime = simulationStartWallTime;
		modID = new ModID();
	}

	public long getSimulationStartWallTime() {
		return simulationStartWallTime;
	}

	public void setMotorIgnited(boolean motorIgnited) {
		this.motorIgnited = motorIgnited;
		modID = new ModID();
	}

	public boolean isMotorIgnited() {
		return motorIgnited;
	}

	public void setLiftoff(boolean liftoff) {
		this.liftoff = liftoff;
		modID = new ModID();
	}

	public boolean isLiftoff() {
		return liftoff;
	}

	public void setLaunchRodCleared(boolean launchRod) {
		this.launchRodCleared = launchRod;
		if (launchRod) {
			startWarningsTime = getSimulationTime() + WARNINGS_WAIT;
		}
		modID = new ModID();
	}

	public boolean isLaunchRodCleared() {
		return launchRodCleared;
	}

	public void setApogeeReached(boolean apogeeReached) {
		this.apogeeReached = apogeeReached;
		modID = new ModID();
	}

	public boolean isApogeeReached() {
		return apogeeReached;
	}

	public void setTumbling(boolean tumbling) {
		this.tumbling = tumbling;
		modID = new ModID();
	}

	public boolean isTumbling() {
		return tumbling;
	}

	public void setLanded(boolean landed) {
		this.landed = landed;
		modID = new ModID();
	}

	public boolean isLanded() {
		return landed;
	}

	public double getMaxAlt() {
		return maxAlt;
	}

	public void setMaxAlt(double maxAlt) {
		this.maxAlt = maxAlt;
		modID = new ModID();
	}

	public double getMaxAltTime() {
		return maxAltTime;
	}

	public void setMaxAltTime(double maxAltTime) {
		this.maxAltTime = maxAltTime;
		modID = new ModID();
	}

	public Set<RecoveryDevice> getDeployedRecoveryDevices() {
		return deployedRecoveryDevices;
	}

	public void setWarnings(WarningSet warnings) {
		if (this.warnings != null)
			this.modIDadd = new ModID();
		this.warnings = warnings;
	}

	public void addWarning(Warning warning) {
		log.trace("Add warning: \"" + warning + "\"");
		
		if (null == warnings) {
			setWarnings(new WarningSet());
		}

		// Only add a new SIM_WARN event if warning wasn't already present
		if (warnings.add(warning)) {
			// For a variety of reasons, the Warning actually added to
			// the set may not be the one passed in. So we add the Warning
			// to the set, then read it again.
			warning = (Warning) warnings.get(warning);

			getFlightDataBranch().addEvent(new FlightEvent(FlightEvent.Type.SIM_WARN, getSimulationTime(), null, warning));
		}
	}

	public void addWarnings(WarningSet warnings) {
		for (Warning warning : warnings) {
			addWarning(warning);
		}
	}

	public WarningSet getWarnings() {
		return warnings;
	}

	public EventQueue getEventQueue() {
		return eventQueue;
	}

	/**
	 * Remove all events that came from components which are no longer
	 * attached from the event queue.
	 */
	public void removeUnattachedEvents() {
		Iterator<FlightEvent> i = getEventQueue().iterator();
		while (i.hasNext()) {
			if (!isAttached(i.next())) {
				i.remove();
			}
		}
	}

	/**
	 * Determine whether a FlightEvent came from a RocketComponent that is
	 * still attached to the current stage
	 *
	 * @param event the event to be tested
	 * return true if attached, false if not
	 */
	private boolean isAttached(FlightEvent event) {
		if ((null == event.getSource()) ||
			(null == event.getSource().getParent()) ||
			getConfiguration().isComponentActive(event.getSource())) {
			return true;
			}
		return false;
	}
	
	public void setSimulationConditions(SimulationConditions simulationConditions) {
		if (this.simulationConditions != null)
			this.modIDadd = new ModID();
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
	public ModID getModID() {
		return modID;
	}

	public String toEventDebug() {
		final StringBuilder buf = new StringBuilder();
		for (FlightEvent event : this.eventQueue) {
			buf.append("      [t:" + event.getType() + " @" + event.getTime());
			if (null != event.getSource()) {
				buf.append("  src:" + event.getSource().getName());
			}
			if (null != event.getData()) {
				buf.append("  data:" + event.getData().getClass().getSimpleName());
			}
			buf.append("]\n");
		}
		return buf.toString();
	}

	public String toMotorsDebug() {
		final StringBuilder buf = new StringBuilder("MotorState list:\n");
		for (MotorClusterState state : this.motorStateList) {
			buf.append("          [" + state.toDescription() + "]\n");
		}
		return buf.toString();
	}

	private void populateMotors() {
		motorStateList.clear();
		for (MotorConfiguration motorConfig : this.configuration.getAllMotors()) {
			MotorClusterState simMotor = new MotorClusterState(motorConfig);
			this.motorStateList.add(simMotor);
		}
	}

	/**
	 * Store data from current sim status
	 */
	public void storeData() {
		flightDataBranch.addPoint();
		flightDataBranch.setValue(FlightDataType.TYPE_TIME, getSimulationTime());
		flightDataBranch.setValue(FlightDataType.TYPE_ALTITUDE, getRocketPosition().z);
		flightDataBranch.setValue(FlightDataType.TYPE_ALTITUDE_ABOVE_SEA, getRocketWorldPosition().getAltitude());
		flightDataBranch.setValue(FlightDataType.TYPE_POSITION_X, getRocketPosition().x);
		flightDataBranch.setValue(FlightDataType.TYPE_POSITION_Y, getRocketPosition().y);
		
		flightDataBranch.setValue(FlightDataType.TYPE_LATITUDE, getRocketWorldPosition().getLatitudeRad());
		flightDataBranch.setValue(FlightDataType.TYPE_LONGITUDE, getRocketWorldPosition().getLongitudeRad());
		
		flightDataBranch.setValue(FlightDataType.TYPE_POSITION_XY,
					  MathUtil.hypot(getRocketPosition().x, getRocketPosition().y));
		flightDataBranch.setValue(FlightDataType.TYPE_POSITION_DIRECTION,
					  Math.atan2(getRocketPosition().y, getRocketPosition().x));

		flightDataBranch.setValue(FlightDataType.TYPE_VELOCITY_XY,
					  MathUtil.hypot(getRocketVelocity().x, getRocketVelocity().y));
		flightDataBranch.setValue(FlightDataType.TYPE_VELOCITY_Z, getRocketVelocity().z);
		setMaxZVelocity(Math.max(getRocketVelocity().z, getMaxZVelocity()));
		
		flightDataBranch.setValue(FlightDataType.TYPE_VELOCITY_TOTAL, getRocketVelocity().length());
		
		Coordinate c = getRocketOrientationQuaternion().rotateZ();
		double theta = Math.atan2(c.z, MathUtil.hypot(c.x, c.y));
		double phi = Math.atan2(c.y, c.x);
		if (phi < -(Math.PI - 0.0001))
			phi = Math.PI;
		flightDataBranch.setValue(FlightDataType.TYPE_ORIENTATION_THETA, theta);
		flightDataBranch.setValue(FlightDataType.TYPE_ORIENTATION_PHI, phi);
		flightDataBranch.setValue(FlightDataType.TYPE_COMPUTATION_TIME,
				(System.nanoTime() - getSimulationStartWallTime()) / 1000000000.0);
	}		

	/**
	 * Get max Z velocity so far in flight
	 * @return max Z velocity so far
	 */
	public double getMaxZVelocity() {
		return maxZVelocity;
	}

	/**
	 * Set max Z velocity so far
	 * @param zVel current z velocity
	 */
	private void setMaxZVelocity(double zVel) {
		if (zVel > maxZVelocity) {
			maxZVelocity = zVel;
			modID = new ModID();
		}
	}
	
	/**
	 * Determine whether (most) flight event warnings are currently being saved.
	 * Warnings are not saved until 0.25 seconds after leaving the rail, and again
	 * after Z velocity is reduced to 20% of the max.
	 */
	boolean recordWarnings() {
		if (!launchRodCleared) {
			return false;
		}
		
		if (getSimulationTime() < startWarningsTime) {
			return false;
		}

		if (getRocketVelocity().z < getMaxZVelocity() * 0.2) {
			return false;
		}

		return true;
	}
		
	/**
	 * Add a flight event to the event queue unless a listener aborts adding it.
	 *
	 * @param event		the event to add to the queue.
	 */
	public void addEvent(FlightEvent event) throws SimulationException {
		if (SimulationListenerHelper.fireAddFlightEvent(this, event)) {
			
			if (event.getType() != FlightEvent.Type.ALTITUDE) {
				log.trace("Adding event to queue:  " + event);
			}
			getEventQueue().add(event);
		}
	}

	/**
	 * Abort the current simulation branch
	 */
	public void abortSimulation(SimulationAbort.Cause cause) throws SimulationException {
		FlightEvent abortEvent = new FlightEvent(FlightEvent.Type.SIM_ABORT, getSimulationTime(), null, new SimulationAbort(cause));
		addEvent(abortEvent);
	}

}
