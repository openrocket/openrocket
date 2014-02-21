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

public class SteppingEventSimulationEngine extends BasicEventSimulationEngine {
	private boolean branchRunning = false;
	private int iteration = 0;
	private Configuration configuration = null;
	private FlightData flightData = null;
	private FlightDataBranch dataBranch = null;
	
	/**
	 * Initializes a FlightData object that will be used for the simulation.
	 * @param   SimulationConditions  necessary to initialize a new simulation engine.
	 * @return  FlightData  This object contains all the data for the simulation.
	 * @throws  SimulationException
	 */
	public FlightData initialize(SimulationConditions simulationConditions) throws SimulationException {
		
		// Set up flight data
		flightData = new FlightData();
		
		// Set up rocket configuration
		configuration = setupConfiguration(simulationConditions);
		flightConfigurationId = configuration.getFlightConfigurationID();
		MotorInstanceConfiguration motorConfiguration = setupMotorConfiguration(configuration);
		if (motorConfiguration.getMotorIDs().isEmpty()) {
			throw new MotorIgnitionException(trans.get("BasicEventSimulationEngine.error.noMotorsDefined"));
		}
		
		status = new SimulationStatus(configuration, motorConfiguration, simulationConditions);
		status.getEventQueue().add(new FlightEvent(FlightEvent.Type.LAUNCH, 0, simulationConditions.getRocket()));
		{
			// main sustainer stage
			RocketComponent sustainer = configuration.getRocket().getChild(0);
			FlightDataType fdt = FlightDataType.TYPE_TIME;
			FlightDataBranch fdb = new FlightDataBranch(sustainer.getName(), fdt);
			status.setFlightData(fdb);
		}
		stages.add(status);
		
		SimulationListenerHelper.fireStartSimulation(status);
		
		return flightData;
	}
	
	/**
	 * Status of the current simulation
	 * @return  boolean  true = the simulation is running.
	 */
	public boolean simulationRunning() {
		if (stages.size() > 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * @return  returns the currently active flightDataBranch
	 */
	public FlightDataBranch getFlightData() {
		return dataBranch;
	}
	
	/**
	 * Executes the specified number of simulation steps.
	 * @param   int  number of steps to iterate
	 * @return  int  number of iterations this simulation
	 *               engine has done since it was initialized.
	 * @throws SimulationException
	 */
	public int simulate(int steps) throws SimulationException {
		while (steps > 0) {
			if (!simulationRunning()) {
				break;
			}
			if (!branchRunning) {
				SimulationStatus stageStatus = stages.pop();
				if (stageStatus == null) {
					throw new SimulationException("SimulationStatus inconsitency");
				}
				flightData.addBranch(status.getFlightData());
			}
			dataBranch = simulateLoop();
			iteration++;
		}
		if (!branchRunning) {
			flightData.getWarningSet().addAll(status.getWarnings());
		}
		if (!simulationRunning()) {
			SimulationListenerHelper.fireEndSimulation(status, null);
			configuration.release();
			
			if (!flightData.getWarningSet().isEmpty()) {
				log.info("Warnings at the end of simulation:  " + flightData.getWarningSet());
			}
		}
		return iteration;
	}
	
	/**
	 * Iterates a single simulation step, very UN-loop like
	 * 
	 * @return  returns junk
	 */
	@Override
	protected FlightDataBranch simulateLoop() {
		
		// Initialize the simulation
		currentStepper = flightStepper;
		status = currentStepper.initialize(status);
		
		// Get originating position (in case listener has modified launch position)
		Coordinate origin = status.getRocketPosition();
		Coordinate originVelocity = status.getRocketVelocity();
		
		try {
			double maxAlt = Double.NEGATIVE_INFINITY;
			
			// Start the simulation
			branchRunning = handleEvents();
			if (branchRunning) {
				
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
				
			}
			
		} catch (SimulationException e) {
			SimulationListenerHelper.fireEndSimulation(status, e);
			// Add FlightEvent for Abort.
			status.getFlightData().addEvent(new FlightEvent(FlightEvent.Type.EXCEPTION, status.getSimulationTime(), status.getConfiguration().getRocket(), e.getLocalizedMessage()));
			status.getWarnings().add(e.getLocalizedMessage());
		}
		
		return dataBranch;
	}
	
	/**
	 * To iterate a limited number of steps we need different parameters.
	 * This function is overridden to disable it. 
	 * Use initialize first to setup a FlightData object
	 * Then use simulate(int) to iterate some number of steps.
	 */
	@Override
	public FlightData simulate(SimulationConditions simulationConditions) throws SimulationException {
		throw new SimulationException("simulate(int) must be used instead");
	}
}
