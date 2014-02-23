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
	private boolean handleEventsReturn = false;
	private int iteration = 0;
	private Configuration configuration = null;
	private MotorInstanceConfiguration motorConfiguration = null;
	private FlightData flightData = null;
	private FlightDataBranch dataBranch = null;
	
	Coordinate origin = null;
	Coordinate originVelocity = null;
	
	double maxAlt = Double.NEGATIVE_INFINITY;
	
	public FlightData initialize(SimulationConditions simulationConditions) throws SimulationException {
		
		// Set up flight data
		flightData = new FlightData();
		
		// Set up rocket configuration
		configuration = setupConfiguration(simulationConditions);
		flightConfigurationId = configuration.getFlightConfigurationID();
		motorConfiguration = setupMotorConfiguration(configuration);
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
	
	public int simulate(int steps) {
		try {
			boolean result = true;
			steps = 1;
			while (result) {
				result = simulateSub(steps);
			}
			if (!simulateSub(steps)) {
				SimulationListenerHelper.fireEndSimulation(status, null);
				
				configuration.release();
				
				if (!flightData.getWarningSet().isEmpty()) {
					log.info("Warnings at the end of simulation:  " + flightData.getWarningSet());
				}
			}
		} catch (Throwable t) {
			return -1;
		}
		return iteration;
	}
	
	protected boolean simulateSub(int steps) {
		
		for (; steps > 0; steps--) {
			//while (true) {
			if (!handleEventsReturn) {
				if (stages.size() < 1) {
					return false;
				}
				SimulationStatus stageStatus = stages.pop();
				if (stageStatus == null) {
					return false;
				}
				status = stageStatus;
				
				// Initialize the simulation
				currentStepper = flightStepper;
				status = currentStepper.initialize(status);
				
				// Get originating position (in case listener has modified launch position)
				origin = status.getRocketPosition();
				originVelocity = status.getRocketVelocity();
				maxAlt = Double.NEGATIVE_INFINITY;
			}
			dataBranch = simulateLoop();
			if (!handleEventsReturn) {
				flightData.addBranch(dataBranch);
				flightData.getWarningSet().addAll(status.getWarnings());
			}
		}
		return true;
	}
	
	protected FlightDataBranch simulateLoop() {
		
		try {
			
			// Start the simulation
			if (branchRunning()) {
				iteration++;
				
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
		
		return status.getFlightData();
	}
	
	private boolean branchRunning() throws SimulationException {
		handleEventsReturn = handleEvents();
		if (handleEventsReturn) {
			return true;
		}
		return false;
	}
	
	public FlightDataBranch getFlightData() {
		if (status != null) {
			return status.getFlightData();
		}
		return null;
	}
	
	public boolean simulationRunning() {
		if (handleEventsReturn || stages.size() > 0) {
			return true;
		}
		return false;
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
