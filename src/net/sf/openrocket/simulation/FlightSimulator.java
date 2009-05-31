package net.sf.openrocket.simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import net.sf.openrocket.aerodynamics.AerodynamicCalculator;
import net.sf.openrocket.aerodynamics.AtmosphericConditions;
import net.sf.openrocket.aerodynamics.AtmosphericModel;
import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.Clusterable;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.Motor;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.exception.SimulationLaunchException;
import net.sf.openrocket.simulation.exception.SimulationNotSupportedException;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Pair;




/**
 * Abstract class that implements a flight simulation using a specific
 * {@link AerodynamicCalculator}.  The simulation methods are the <code>simulate</code>
 * methods.
 * <p>
 * This class contains the event flight event handling mechanisms common to all
 * simulations.  The simulator calls the {@link #step(SimulationConditions, SimulationStatus)}
 * method periodically to take time steps.  Concrete subclasses of this class specify 
 * how the actual time steps are taken (e.g. Euler or Runge-Kutta integration).
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class FlightSimulator {
	
	public static final double RECOVERY_TIME_STEP = 0.5;
	
	/** The {@link AerodynamicCalculator} to use to calculate the aerodynamic forces. */
	protected AerodynamicCalculator calculator = null;

	/** The {@link AtmosphericModel} used to model the atmosphere. */
	protected AtmosphericModel atmosphericModel;
	
	/** Listener list. */
	protected final List<SimulationListener> listeners = new ArrayList<SimulationListener>();

	
	private PriorityQueue<FlightEvent> eventQueue;
	private WarningSet warnings;
	
	
	public FlightSimulator() {
		
	}
		
	public FlightSimulator(AerodynamicCalculator calculator) {
		this.calculator = calculator;
	}
	
	
	
	
	public AerodynamicCalculator getCalculator() {
		return calculator;
	}

	public void setCalculator(AerodynamicCalculator calc) {
		this.calculator = calc;
	}
	
	
	/**
	 * Will be removed!  Use {@link #simulate(SimulationConditions)} instead.
	 */
	@Deprecated
	public FlightData simulate(SimulationConditions simulation, 
			boolean simulateBranches, WarningSet warnings) 
			throws SimulationNotSupportedException {
		try {
			return simulate(simulation);
		} catch (SimulationException e) {
			throw new SimulationNotSupportedException(e);
		}
	}
	

	public FlightData simulate(SimulationConditions simulation) 
			throws SimulationException {

		// Set up flight data
		FlightData flightData = new FlightData();
		
		// Set up rocket configuration
		Configuration configuration = calculator.getConfiguration();
		configuration.setAllStages();
		configuration.setMotorConfigurationID(simulation.getMotorConfigurationID());
		
		if (!configuration.hasMotors()) {
			throw new SimulationLaunchException("No motors defined.");
		}
		
		// Set up the event queue
		eventQueue = new PriorityQueue<FlightEvent>();
		eventQueue.add(new FlightEvent(FlightEvent.Type.LAUNCH, 0, simulation.getRocket()));

		// Initialize the simulation
		SimulationStatus status = initializeSimulation(configuration, simulation);
		status.warnings = flightData.getWarningSet();
		warnings = flightData.getWarningSet();
		

		// Start the simulation
		while (handleEvents(eventQueue, status)) {
			
			// Take the step
			double oldAlt = status.position.z;
			
			if (status.deployedRecoveryDevices.isEmpty()) {
				step(simulation, status);
			} else {
				recoveryStep(simulation, status);
			}
			
			
			// Add appropriate events
						
			if (!status.liftoff) {
				
				// Avoid sinking into ground before liftoff
				if (status.position.z < 0) {
					status.position = Coordinate.NUL;
					status.velocity = Coordinate.NUL;
				}
				// Detect liftoff
				if (status.position.z > 0.01) {
					eventQueue.add(new FlightEvent(FlightEvent.Type.LIFTOFF, status.time));
					status.liftoff = true;
				}
				
			} else {

				// Check ground hit after liftoff
				if (status.position.z < 0) {
					status.position = status.position.setZ(0);
					eventQueue.add(new FlightEvent(FlightEvent.Type.GROUND_HIT, status.time));
					eventQueue.add(new FlightEvent(FlightEvent.Type.SIMULATION_END, status.time));
				}

			}

			
			// Add altitude event
			eventQueue.add(new FlightEvent(FlightEvent.Type.ALTITUDE, status.time, 
					status.configuration.getRocket(), 
					new Pair<Double,Double>(oldAlt,status.position.z)));


			// Check for launch guide clearance
			if (status.launchRod && status.position.length() > status.launchRodLength) {
				eventQueue.add(new FlightEvent(FlightEvent.Type.LAUNCHROD, status.time, null));
				status.launchRod = false;
			}
			
			
			// Check for apogee
			if (!status.apogeeReached && status.position.z < oldAlt - 0.001) {
				eventQueue.add(new FlightEvent(FlightEvent.Type.APOGEE, status.time,
						status.configuration.getRocket()));
				status.apogeeReached = true;
			}

			
			// Call listeners
			SimulationListener[] array = listeners.toArray(new SimulationListener[0]);
			for (SimulationListener l: array) {
				addListenerEvents(l.stepTaken(status));
			}
		}
		
		flightData.addBranch(status.flightData);
		
		System.out.println("Warnings at the end:  "+flightData.getWarningSet());
		
		// TODO: HIGH: Simulate branches
		return flightData;
	}
	
	
	

	/**
	 * Handles events occurring during the flight from the <code>eventQueue</code>.
	 * Each event that has occurred before or at the current simulation time is
	 * processed.  Suitable events are also added to the flight data.
	 * 
	 * @param data				the FlightData to add events to.
	 * @param endEvent			the event at which to end this simulation.
	 * @param simulateBranches	whether to invoke a separate simulation of separated lower
	 * 							stages
	 * @throws SimulationException 
	 */
	private boolean handleEvents(PriorityQueue<FlightEvent> queue, SimulationStatus status)
	throws SimulationException {
		FlightEvent e;
		boolean ret = true;
		
		e = queue.peek();
		// Skip to events if no motor has ignited yet
		if (!status.motorIgnited) {
			if (e == null || Double.isNaN(e.getTime()) || e.getTime() > 1000000) {
				throw new SimulationLaunchException("No motors ignited.");
			}
			status.time = e.getTime();
		}
		
		while ((e != null) && (e.getTime() <= status.time)) {
			e = queue.poll();

			// If no motor has ignited and no events are occurring, abort
			if (!status.motorIgnited) {
				if (e == null || Double.isNaN(e.getTime()) || e.getTime() > 1000000) {
					throw new SimulationLaunchException("No motors ignited.");
				}
			}
			
			// If event is motor burnout without liftoff, abort
			if (e.getType() == FlightEvent.Type.BURNOUT  &&  !status.liftoff) {
				throw new SimulationLaunchException("Motor burnout without liftoff.");
			}

			// Add event to flight data
			if (e.getType() != FlightEvent.Type.ALTITUDE) {
				status.flightData.addEvent(status.time, e.resetSource());
			}
			
			// Check for motor ignition events, add ignition events to queue
			Iterator<MotorMount> iterator = status.configuration.motorIterator();
			while (iterator.hasNext()) {
				MotorMount mount = iterator.next();
				if (mount.getIgnitionEvent().isActivationEvent(e, (RocketComponent)mount)) {
					queue.add(new FlightEvent(FlightEvent.Type.IGNITION, 
							status.time + mount.getIgnitionDelay(), (RocketComponent)mount));
				}
			}
			
			// Handle motor ignition events, add burnout events
			if (e.getType() == FlightEvent.Type.IGNITION) {
				status.motorIgnited = true;
				
				String id = status.configuration.getMotorConfigurationID();
				MotorMount mount = (MotorMount) e.getSource();
				Motor motor = mount.getMotor(id);
				
				status.configuration.setIgnitionTime(mount, e.getTime());
				queue.add(new FlightEvent(FlightEvent.Type.BURNOUT, 
						e.getTime() + motor.getTotalTime(), (RocketComponent)mount));
				queue.add(new FlightEvent(FlightEvent.Type.EJECTION_CHARGE,
						e.getTime() + motor.getTotalTime() + mount.getMotorDelay(id), 
						(RocketComponent)mount));
			}
			
			
			// Handle stage separation on motor ignition
			if (e.getType() == FlightEvent.Type.IGNITION) {
				RocketComponent mount = (RocketComponent) e.getSource();
				int n = mount.getStageNumber();
				if (n < mount.getRocket().getStageCount()-1) {
					if (status.configuration.isStageActive(n+1)) {
						queue.add(new FlightEvent(FlightEvent.Type.STAGE_SEPARATION, e.getTime(),
								mount.getStage()));
					}
				}
			}
			if (e.getType() == FlightEvent.Type.STAGE_SEPARATION) {
				RocketComponent stage = (RocketComponent) e.getSource();
				int n = stage.getStageNumber();
				status.configuration.setToStage(n);
			}
			
			
			// Handle recovery device deployment
			Iterator<RocketComponent> iterator1 = status.configuration.iterator();
			while (iterator1.hasNext()) {
				RocketComponent c = iterator1.next();
				if (!(c instanceof RecoveryDevice))
					continue;
				if (((RecoveryDevice)c).getDeployEvent().isActivationEvent(e, c)) {
					// Delay event by at least 1ms to allow stage separation to occur first
					queue.add(new FlightEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT,
							e.getTime() + Math.max(0.001, ((RecoveryDevice)c).getDeployDelay()), c));
				}
			}
			if (e.getType() == FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT) {
				RocketComponent c = e.getSource();
				int n = c.getStageNumber();
				// Ignore event if stage not active
				if (status.configuration.isStageActive(n)) {
					
					// Check whether any motor is active anymore
					iterator = status.configuration.motorIterator();
					while (iterator.hasNext()) {
						MotorMount mount = iterator.next();
						Motor motor = mount.getMotor(status.configuration.getMotorConfigurationID());
						if (motor == null)
							continue;
						if (status.configuration.getIgnitionTime(mount) + motor.getAverageTime()
								> status.time) {
							warnings.add(Warning.RECOVERY_DEPLOYMENT_WHILE_BURNING);
						}
					}
					
					// Check for launch rod
					if (status.launchRod) {
						warnings.add(Warning.fromString("Recovery device device deployed while on " +
								"the launch guide."));
					}
					
					// Check current velocity
					if (status.velocity.length() > 20) {
						// TODO: LOW: Custom warning.
						warnings.add(Warning.fromString("Recovery device deployment at high " +
								"speed (" 
								+ UnitGroup.UNITS_VELOCITY.toStringUnit(status.velocity.length())
								+ ")."));
					}
					
					status.liftoff = true;
					status.deployedRecoveryDevices.add((RecoveryDevice)c);
				}
			}
			
			
			
			// Simulation end
			if (e.getType() == FlightEvent.Type.SIMULATION_END) {
				ret = false;
			}
			
			
			// Call listeners
			SimulationListener[] array = listeners.toArray(new SimulationListener[0]);
			for (SimulationListener l: array) {
				addListenerEvents(l.handleEvent(e, status));
			}
			
			
			e = queue.peek();
			// Skip to events if no motor has ignited yet
			if (!status.motorIgnited) {
				if (e == null || Double.isNaN(e.getTime()) || e.getTime() > 1000000) {
					throw new SimulationLaunchException("No motors ignited.");
				}
				status.time = e.getTime();
			}
		}
		return ret;
	}
	
	
	// TODO: MEDIUM: Create method storeData() which is overridden by simulators
	
	
	/**
	 * Perform a step during recovery.  This is a 3-DOF simulation using simple Euler
	 * integration.
	 * 
	 * @param conditions	the simulation conditions.
	 * @param status		the current simulation status.
	 */
	protected void recoveryStep(SimulationConditions conditions, SimulationStatus status) {
		double totalCD = 0;
		double refArea = status.configuration.getReferenceArea();
		
		// TODO: MEDIUM: Call listeners during recovery phase
		
		// Get the atmospheric conditions
		AtmosphericConditions atmosphere = conditions.getAtmosphericModel().getConditions(
				conditions.getLaunchAltitude() + status.position.z);

		//// Local wind speed and direction
		double windSpeed = status.windSimulator.getWindSpeed(status.time);
		Coordinate airSpeed = status.velocity.add(windSpeed, 0, 0);

		// Get total CD
		double mach = airSpeed.length() / atmosphere.getMachSpeed();
		for (RecoveryDevice c: status.deployedRecoveryDevices) {
			totalCD += c.getCD(mach) * c.getArea() / refArea;
		}
		
		// Compute drag force
		double dynP = (0.5 * atmosphere.getDensity() * airSpeed.length2());
		double dragForce = totalCD * dynP * refArea;
		double mass = calculator.getCG(status.time).weight;
		
		
		// Compute drag acceleration
		Coordinate linearAcceleration;
		if (airSpeed.length() > 0.001) {
			linearAcceleration = airSpeed.normalize().multiply(-dragForce/mass);
		} else {
			linearAcceleration = Coordinate.NUL;
		}
		
		// Add effect of gravity
		linearAcceleration = linearAcceleration.sub(0, 0, status.gravityModel.getGravity());

		
		// Select time step
		double timeStep = MathUtil.min(0.5/linearAcceleration.length(), RECOVERY_TIME_STEP);
		
		// Perform Euler integration
		status.position = (status.position.add(status.velocity.multiply(timeStep)).
				add(linearAcceleration.multiply(MathUtil.pow2(timeStep)/2)));
		status.velocity = status.velocity.add(linearAcceleration.multiply(timeStep));
		status.time += timeStep;

		
		// Store data
		FlightDataBranch data = status.flightData;
		boolean extra = status.startConditions.getCalculateExtras();
		data.addPoint();

		data.setValue(FlightDataBranch.TYPE_TIME, status.time);
		data.setValue(FlightDataBranch.TYPE_ALTITUDE, status.position.z);
		data.setValue(FlightDataBranch.TYPE_POSITION_X, status.position.x);
		data.setValue(FlightDataBranch.TYPE_POSITION_Y, status.position.y);
		if (extra) {
			data.setValue(FlightDataBranch.TYPE_POSITION_XY, 
					MathUtil.hypot(status.position.x, status.position.y));
			data.setValue(FlightDataBranch.TYPE_POSITION_DIRECTION, 
					Math.atan2(status.position.y, status.position.x));
			
			data.setValue(FlightDataBranch.TYPE_VELOCITY_XY, 
					MathUtil.hypot(status.velocity.x, status.velocity.y));
			data.setValue(FlightDataBranch.TYPE_ACCELERATION_XY, 
					MathUtil.hypot(linearAcceleration.x, linearAcceleration.y));
			
			data.setValue(FlightDataBranch.TYPE_ACCELERATION_TOTAL,linearAcceleration.length());
			
			double Re = airSpeed.length() * 
					calculator.getConfiguration().getLength() / 
					atmosphere.getKinematicViscosity();
			data.setValue(FlightDataBranch.TYPE_REYNOLDS_NUMBER, Re);
		}
		
		data.setValue(FlightDataBranch.TYPE_VELOCITY_Z, status.velocity.z);
		data.setValue(FlightDataBranch.TYPE_ACCELERATION_Z, linearAcceleration.z);
		
		data.setValue(FlightDataBranch.TYPE_VELOCITY_TOTAL, airSpeed.length());
		data.setValue(FlightDataBranch.TYPE_MACH_NUMBER, mach);
		
		data.setValue(FlightDataBranch.TYPE_MASS, mass);

		data.setValue(FlightDataBranch.TYPE_THRUST_FORCE, 0);
		data.setValue(FlightDataBranch.TYPE_DRAG_FORCE, dragForce);

		data.setValue(FlightDataBranch.TYPE_WIND_VELOCITY, windSpeed);
		data.setValue(FlightDataBranch.TYPE_AIR_TEMPERATURE, atmosphere.temperature);
		data.setValue(FlightDataBranch.TYPE_AIR_PRESSURE, atmosphere.pressure);
		data.setValue(FlightDataBranch.TYPE_SPEED_OF_SOUND, atmosphere.getMachSpeed());
		
		data.setValue(FlightDataBranch.TYPE_TIME_STEP, timeStep);
		if (status.simulationStartTime != Long.MIN_VALUE)
			data.setValue(FlightDataBranch.TYPE_COMPUTATION_TIME,
					(System.nanoTime() - status.simulationStartTime)/1000000000.0);
	}
	
	
	
	
	/**
	 * Add events that listeners have returned, and add a Warning to the 
	 * simulation if necessary.
	 * 
	 * @param events	a collection of the events, or <code>null</code>.
	 */
	protected final void addListenerEvents(Collection<FlightEvent> events) {
		if (events == null)
			return;
		for (FlightEvent e: events) {
			if (e != null && e.getTime() < 1000000) {
				warnings.add(Warning.LISTENERS_AFFECTED);
				eventQueue.add(e);
			}
		}
	}
	
	
	
	/**
	 * Calculate the average thrust produced by the motors in the current configuration.
	 * The average is taken between <code>status.time</code> and 
	 * <code>status.time + timestep</code>.
	 * <p>
	 * Note:  Using this method does not take into account any moments generated by
	 * off-center motors.
	 *  
	 * @param status	the current simulation status.
	 * @param timestep	the time step of the current iteration.
	 * @return			the average thrust during the time step.
	 */
	protected double calculateThrust(SimulationStatus status, double timestep) {
		double thrust = 0;
		Iterator<MotorMount> iterator = status.configuration.motorIterator();
		
		while (iterator.hasNext()) {
			MotorMount mount = iterator.next();
			
			// Count the number of motors in a cluster
			int count = 1;
			for (RocketComponent c = (RocketComponent)mount; c != null; c = c.getParent()) {
				if (c instanceof Clusterable) 
					count *= ((Clusterable)c).getClusterConfiguration().getClusterCount();
			}
			
			Motor motor = mount.getMotor(status.configuration.getMotorConfigurationID());
			double ignitionTime = status.configuration.getIgnitionTime(mount);
			double time = status.time - ignitionTime;
			thrust += count * motor.getThrust(time, time + timestep);
			// TODO: MEDIUM: Moment generated by motors
		}
		
		return thrust;
	}
	
	
	
	/**
	 * Initialize a new {@link SimulationStatus} object for simulation using this simulator.
	 * 
	 * @param configuration	the starting configuration of the rocket.
	 * @param simulation	the simulation conditions.
	 * @return				a {@link SimulationStatus} object for the simulation.
	 */
	protected abstract SimulationStatus initializeSimulation(Configuration configuration, 
			SimulationConditions simulation);
	
	/**
	 * Make a time step.  The current status of the simulation is stored in the
	 * variable <code>status</code> and must be updated by this call.
	 *
	 * @param simulation	the simulation conditions.
	 * @param status		the current simulation status, received originally from
	 * 						{@link #initializeSimulation(Configuration, SimulationConditions)}
	 * @return	a collection of flight events to handle, or null for none.
	 */
	
	
	protected abstract Collection<FlightEvent> step(SimulationConditions simulation, 
			SimulationStatus status) throws SimulationException;


	
	public void addSimulationListener(SimulationListener l) {
		listeners.add(l);
	}
	public void removeSimulationListener(SimulationListener l) {
		listeners.remove(l);
	}
	public void resetSimulationListeners() {
		listeners.clear();
	}

}
