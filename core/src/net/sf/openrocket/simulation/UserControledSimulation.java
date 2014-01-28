package net.sf.openrocket.simulation;

import net.sf.openrocket.motor.MotorId;
import net.sf.openrocket.motor.MotorInstance;
import net.sf.openrocket.motor.MotorInstanceConfiguration;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.exception.MotorIgnitionException;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.listeners.SimulationListenerHelper;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Pair;

public class UserControledSimulation extends BasicEventSimulationEngine {
	
	protected Configuration configuration = null;
	protected boolean m_bSimulationRunning = false;
	
	public UserControledSimulation() {
		// TODO Auto-generated constructor stub
		super();
	}
	
	public RK4SimulationStatus firstInitialize(SimulationConditions sim, RK4SimulationStatus Status, FlightData flight) throws SimulationException {
		if (flight == null)
			return null;
		Status = InitializeSimulation(sim, Status);
		if (Status == null)
			return null;
		Status = FirstPartofWhileTrue(flight, Status);
		if (Status == null)
			return null;
		Status = InitializeSimulationloop(Status);
		if (Status == null)
			return null;
		m_bSimulationRunning = true;
		
		return Status;
	}
	
	public RK4SimulationStatus step(SimulationStatus Status, FlightData flight) {
		
		return RunSimulationLoop(Status, flight);
		
	}
	
	public RK4SimulationStatus stagestep(FlightData flight, SimulationStatus Status) {
		//status = Status; //Status might be null due to a return value
		EndSimulationLoop();
		//EndOfWhileTrue(flight);
		return FirstPartofWhileTrue(flight, status);
	}
	
	private int EndOfWhileTrue(FlightData flightData) {
		flightData.addBranch(status.getFlightData());
		flightData.getWarningSet().addAll(status.getWarnings());
		
		return 0;
	}
	
	private RK4SimulationStatus InitializeSimulationloop(SimulationStatus Status) {
		
		// Initialize the simulation
		currentStepper = flightStepper;
		status = currentStepper.initialize(Status);
		
		// Get originating position (in case listener has modified launch position)
		//origin = status.getRocketPosition();
		//originVelocity = status.getRocketVelocity();
		return (RK4SimulationStatus) status;
	}
	
	private RK4SimulationStatus RunSimulationLoop(SimulationStatus Status, FlightData flight) {
		double maxAlt = Double.NEGATIVE_INFINITY;
		
		//status = new SimulationStatus(Status);
		
		// Get originating position (in case listener has modified launch position)
		Coordinate origin = status.getRocketPosition();
		Coordinate originVelocity = status.getRocketVelocity();
		
		try {
			
			if (handleEvents() == false)
			{
				EndOfWhileTrue(flight);
				return null;
			}
			// Take the step
			double oldAlt = status.getRocketPosition().z;
			
			if (SimulationListenerHelper.firePreStep(status)) {
				// Step at most to the next event
				double maxStepTime = Double.MAX_VALUE;
				FlightEvent nextEvent = status.getEventQueue().peek();
				if (nextEvent != null) {
					maxStepTime = MathUtil.max(nextEvent.getTime() - status.getSimulationTime(), 0.001);
				}
				log.trace("BasicEventSimulationEngine: Taking simulation step at t=" + status.getSimulationTime());
				currentStepper.step(status, maxStepTime);
			}
			SimulationListenerHelper.firePostStep(status);
			
			
			// Check for NaN values in the simulation status
			checkNaN();
			
			// Add altitude event
			addEvent(new FlightEvent(FlightEvent.Type.ALTITUDE, status.getSimulationTime(),
					status.getConfiguration().getRocket(),
					new Pair<Double, Double>(oldAlt, status.getRocketPosition().z)));
			
			if (status.getRocketPosition().z > maxAlt) {
				maxAlt = status.getRocketPosition().z;
			}
			
			
			// Position relative to start location
			Coordinate relativePosition = status.getRocketPosition().sub(origin);
			
			// Add appropriate events
			if (!status.isLiftoff()) {
				
				// Avoid sinking into ground before liftoff
				if (relativePosition.z < 0) {
					status.setRocketPosition(origin);
					status.setRocketVelocity(originVelocity);
				}
				// Detect lift-off
				if (relativePosition.z > 0.02) {
					addEvent(new FlightEvent(FlightEvent.Type.LIFTOFF, status.getSimulationTime()));
				}
				
			} else {
				
				// Check ground hit after liftoff
				if (status.getRocketPosition().z < 0) {
					status.setRocketPosition(status.getRocketPosition().setZ(0));
					addEvent(new FlightEvent(FlightEvent.Type.GROUND_HIT, status.getSimulationTime()));
					addEvent(new FlightEvent(FlightEvent.Type.SIMULATION_END, status.getSimulationTime()));
				}
				
			}
			
			// Check for launch guide clearance
			if (!status.isLaunchRodCleared() &&
					relativePosition.length() > status.getSimulationConditions().getLaunchRodLength()) {
				addEvent(new FlightEvent(FlightEvent.Type.LAUNCHROD, status.getSimulationTime(), null));
			}
			
			
			// Check for apogee
			if (!status.isApogeeReached() && status.getRocketPosition().z < maxAlt - 0.01) {
				addEvent(new FlightEvent(FlightEvent.Type.APOGEE, status.getSimulationTime(),
						status.getConfiguration().getRocket()));
			}
			
			
			// Check for burnt out motors
			for (MotorId motorId : status.getMotorConfiguration().getMotorIDs()) {
				MotorInstance motor = status.getMotorConfiguration().getMotorInstance(motorId);
				if (!motor.isActive() && status.addBurntOutMotor(motorId)) {
					addEvent(new FlightEvent(FlightEvent.Type.BURNOUT, status.getSimulationTime(),
							(RocketComponent) status.getMotorConfiguration().getMotorMount(motorId), motorId));
				}
			}
			
			// Check for Tumbling
			// Conditions for transision are:
			//  apogee reached
			// and is not already tumbling
			// and not stable (cg > cp)
			// and aoa > 30
			
			if (status.isApogeeReached() && !status.isTumbling()) {
				double cp = status.getFlightData().getLast(FlightDataType.TYPE_CP_LOCATION);
				double cg = status.getFlightData().getLast(FlightDataType.TYPE_CG_LOCATION);
				double aoa = status.getFlightData().getLast(FlightDataType.TYPE_AOA);
				if (cg > cp && aoa > AOA_TUMBLE_CONDITION) {
					addEvent(new FlightEvent(FlightEvent.Type.TUMBLE, status.getSimulationTime()));
					status.setTumbling(true);
				}
				
			}
			
		} catch (SimulationException e) {
			SimulationListenerHelper.fireEndSimulation(status, e);
			// Add FlightEvent for Abort.
			status.getFlightData().addEvent(new FlightEvent(FlightEvent.Type.EXCEPTION, status.getSimulationTime(), status.getConfiguration().getRocket(), e.getLocalizedMessage()));
			status.getWarnings().add(e.getLocalizedMessage());
		}
		
		return (RK4SimulationStatus) status;
	}
	
	private int EndSimulationLoop() {
		return 0;
	}
	
	public int EndSimulation() {
		m_bSimulationRunning = false;
		return 0;
	}
	
	
	//will not handel multiple stages in a rocket
	//
	public RK4SimulationStatus InitializeSimulation(SimulationConditions sim, RK4SimulationStatus Status) throws SimulationException {
		if (sim == null)
			return null;
		
		// Set up flight data
		//flight = new FlightData();
		
		// Set up rocket configuration
		configuration = setupConfiguration(sim);
		flightConfigurationId = configuration.getFlightConfigurationID();
		MotorInstanceConfiguration motorConfiguration = setupMotorConfiguration(configuration);
		if (motorConfiguration.getMotorIDs().isEmpty()) {
			throw new MotorIgnitionException(trans.get("BasicEventSimulationEngine.error.noMotorsDefined"));
		}
		
		//status = new SimulationStatus(configuration, motorConfiguration, sim);
		status = new RK4SimulationStatus(configuration, motorConfiguration, sim);
		status.getEventQueue().add(new FlightEvent(FlightEvent.Type.LAUNCH, 0, sim.getRocket()));
		{
			// main sustainer stage
			RocketComponent sustainer = configuration.getRocket().getChild(0);
			status.setFlightData(new FlightDataBranch(sustainer.getName(), FlightDataType.TYPE_TIME));
		}
		stages.add(status);
		
		SimulationListenerHelper.fireStartSimulation(status);
		
		return (RK4SimulationStatus) status;
	}
	
	/* 
	 *  0 everythings fine
	 * -1 sim null
	 * -2 flightdata null
	 * -3 timestep not reasonable range
	 * -4 simulation finished
	 * */
	
	public RK4SimulationStatus FirstPartofWhileTrue(FlightData flightData, SimulationStatus Status) {
		if (flightData == null)
			return null;
		if (stages.size() == 0) {
			EndSimulation(flightData);
			return null;
		}
		
		status = Status;
		
		SimulationStatus stageStatus = stages.pop();
		if (stageStatus == null) {
			EndSimulation(flightData);
			return null;
		}
		status = stageStatus;
		
		return (RK4SimulationStatus) Status;
	}
	
	
	public int EndSimulation(FlightData flightData) {
		SimulationListenerHelper.fireEndSimulation(status, null);
		
		configuration.release();
		
		warnmeifidosomthingwrong(flightData);
		
		return -4;
	}
	
	protected int warnmeifidosomthingwrong(FlightData flightData) {
		flightData.getWarningSet().addAll(status.getWarnings());
		if (!flightData.getWarningSet().isEmpty()) {
			log.info("Warnings during simulation:  " + flightData.getWarningSet());
		}
		return 0;
		
	}
	
};
