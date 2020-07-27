package net.sf.openrocket.simulation;

import java.util.ArrayDeque;
import java.util.Deque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.motor.MotorConfigurationId;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration;
import net.sf.openrocket.simulation.exception.MotorIgnitionException;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.exception.SimulationLaunchException;
import net.sf.openrocket.simulation.listeners.SimulationListenerHelper;
import net.sf.openrocket.simulation.listeners.system.OptimumCoastListener;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Pair;


public class BasicEventSimulationEngine implements SimulationEngine {
	
	private static final Translator trans = Application.getTranslator();
	private static final Logger log = LoggerFactory.getLogger(BasicEventSimulationEngine.class);
	
	// TODO: MEDIUM: Allow selecting steppers
	private SimulationStepper flightStepper  = new RK4SimulationStepper();
	private SimulationStepper landingStepper = new BasicLandingStepper();
	private SimulationStepper tumbleStepper  = new BasicTumbleStepper();
	private SimulationStepper groundStepper  = new GroundStepper();
	
	// Constant holding 20 degrees in radians.  This is the AOA condition
	// necessary to transition to tumbling.
	private final static double AOA_TUMBLE_CONDITION = Math.PI / 9.0;
	
	// The thrust must be below this value for the transition to tumbling.
	// TODO: this is an arbitrary value
	private final static double THRUST_TUMBLE_CONDITION = 0.01;
	
	private SimulationStepper currentStepper;
	
	private SimulationStatus currentStatus;
	
	private FlightConfigurationId fcid;
	
	// this is just a list of simulation branches to 
	Deque<SimulationStatus> toSimulate = new ArrayDeque<SimulationStatus>();
	
	@Override
	public FlightData simulate(SimulationConditions simulationConditions) throws SimulationException {
		
		// Set up flight data
		FlightData flightData = new FlightData();
		
		// Set up rocket configuration
		this.fcid = simulationConditions.getFlightConfigurationID();
		FlightConfiguration simulationConfig = simulationConditions.getRocket().getFlightConfiguration( this.fcid).clone();
		if ( ! simulationConfig.hasMotors() ) {
			throw new MotorIgnitionException(trans.get("BasicEventSimulationEngine.error.noMotorsDefined"));
		}
		
		currentStatus = new SimulationStatus(simulationConfig, simulationConditions);
		currentStatus.getEventQueue().add(new FlightEvent(FlightEvent.Type.LAUNCH, 0, simulationConditions.getRocket()));
		{
			// main simulation branch 
			final String branchName = simulationConfig.getRocket().getTopmostStage().getName();
			currentStatus.setFlightData(new FlightDataBranch( branchName, FlightDataType.TYPE_TIME));
		}
		toSimulate.push(currentStatus);
		
		SimulationListenerHelper.fireStartSimulation(currentStatus);
		do{
			if( null == toSimulate.peek()){
				break;
			}
			currentStatus = toSimulate.pop();
			log.info(">>Starting simulation of branch: "+currentStatus.getFlightData().getBranchName());
			
			FlightDataBranch dataBranch = simulateLoop();
			flightData.addBranch(dataBranch);
			flightData.getWarningSet().addAll(currentStatus.getWarnings());
			
			log.info(String.format("<<Finished simulating branch: %s    curTime:%s    finTime:%s", 
							dataBranch.getBranchName(),
							currentStatus.getSimulationTime(),
							dataBranch.getLast(FlightDataType.TYPE_TIME)));
		}while( ! toSimulate.isEmpty());
		
		SimulationListenerHelper.fireEndSimulation(currentStatus, null);
		
		if (!flightData.getWarningSet().isEmpty()) {
			log.info("Warnings at the end of simulation:  " + flightData.getWarningSet());
		}

		return flightData;
	}
	
	private FlightDataBranch simulateLoop() {
		
		// Initialize the simulation.  We'll use the flight stepper unless we're already on the ground
		if (currentStatus.isLanded())
			currentStepper = groundStepper;
		else
			currentStepper = flightStepper;
		
		currentStatus = currentStepper.initialize(currentStatus);
		
		// Get originating position (in case listener has modified launch position)
		Coordinate origin = currentStatus.getRocketPosition();
		Coordinate originVelocity = currentStatus.getRocketVelocity();
		
		try {
			
			// Start the simulation
			while (handleEvents()) {
				// Take the step
				double oldAlt = currentStatus.getRocketPosition().z;
				
				if (SimulationListenerHelper.firePreStep(currentStatus)) {
					// Step at most to the next event
					double maxStepTime = Double.MAX_VALUE;
					FlightEvent nextEvent = currentStatus.getEventQueue().peek();
					if (nextEvent != null) {
						maxStepTime = MathUtil.max(nextEvent.getTime() - currentStatus.getSimulationTime(), 0.001);
					}
					log.trace("Taking simulation step at t=" + currentStatus.getSimulationTime() + " altitude " + oldAlt);
					currentStepper.step(currentStatus, maxStepTime);
				}
				SimulationListenerHelper.firePostStep(currentStatus);
				
				
				// Check for NaN values in the simulation status
				checkNaN();
				
				// If we haven't hit the ground, add altitude event
				if (!currentStatus.isLanded())
					addEvent(new FlightEvent(FlightEvent.Type.ALTITUDE, currentStatus.getSimulationTime(),
											 currentStatus.getConfiguration().getRocket(),
											 new Pair<Double, Double>(oldAlt, currentStatus.getRocketPosition().z)));
				
				if (currentStatus.getRocketPosition().z > currentStatus.getMaxAlt()) {
					currentStatus.setMaxAlt(currentStatus.getRocketPosition().z);
				}
				
				// Position relative to start location
				Coordinate relativePosition = currentStatus.getRocketPosition().sub(origin);
				
				// Add appropriate events
				if (!currentStatus.isLiftoff()) {
					
					// Avoid sinking into ground before liftoff
					if (relativePosition.z < 0) {
						currentStatus.setRocketPosition(origin);
						relativePosition = Coordinate.ZERO;
						currentStatus.setRocketVelocity(originVelocity);
					}
					// Detect lift-off
					if (relativePosition.z > 0.02) {
						addEvent(new FlightEvent(FlightEvent.Type.LIFTOFF, currentStatus.getSimulationTime()));
					}
					
				} else {
					
					// Check ground hit after liftoff
					if ((currentStatus.getRocketPosition().z < 0) && !currentStatus.isLanded()) {
						addEvent(new FlightEvent(FlightEvent.Type.GROUND_HIT, currentStatus.getSimulationTime()));
						
						// addEvent(new FlightEvent(FlightEvent.Type.SIMULATION_END, currentStatus.getSimulationTime()));
					}
					
				}
				
				// Check for launch guide clearance
				if (currentStatus.isLiftoff() &&
					!currentStatus.isLaunchRodCleared() &&
						relativePosition.length() > currentStatus.getSimulationConditions().getLaunchRodLength()) {
					addEvent(new FlightEvent(FlightEvent.Type.LAUNCHROD, currentStatus.getSimulationTime(), null));
				}
				
				
				// Check for apogee
				if (!currentStatus.isApogeeReached() && currentStatus.getRocketPosition().z < currentStatus.getMaxAlt() - 0.01) {
					currentStatus.setMaxAltTime(currentStatus.getSimulationTime());
					addEvent(new FlightEvent(FlightEvent.Type.APOGEE, currentStatus.getSimulationTime(),
							currentStatus.getConfiguration().getRocket()));
				}
				
//				//@Obsolete
//				//@Redundant
//				// Check for burnt out motors
//				for( MotorClusterState state : currentStatus.getActiveMotors()){
//					if ( state.isSpent()){
//						addEvent(new FlightEvent(FlightEvent.Type.BURNOUT, currentStatus.getSimulationTime(),
//								(RocketComponent) state.getMount(), state));
//					}
//				}
				
				// Check for Tumbling
				// Conditions for transision are:
				//  apogee reached (if sustainer stage)
				// and is not already tumbling
				// and not stable (cg > cp)
				// and aoa > AOA_TUMBLE_CONDITION threshold
				// and thrust < THRUST_TUMBLE_CONDITION threshold
				
				if (!currentStatus.isTumbling()) {
					final double t = currentStatus.getFlightData().getLast(FlightDataType.TYPE_THRUST_FORCE);
					final double cp = currentStatus.getFlightData().getLast(FlightDataType.TYPE_CP_LOCATION);
					final double cg = currentStatus.getFlightData().getLast(FlightDataType.TYPE_CG_LOCATION);
					final double aoa = currentStatus.getFlightData().getLast(FlightDataType.TYPE_AOA);
					
					final boolean wantToTumble = (cg > cp && aoa > AOA_TUMBLE_CONDITION);
					
					if (wantToTumble) {
						final boolean tooMuchThrust = t > THRUST_TUMBLE_CONDITION;
						final boolean isSustainer = currentStatus.getConfiguration().isStageActive(0);
						final boolean isApogee = currentStatus.isApogeeReached();
						if (tooMuchThrust) {
							currentStatus.getWarnings().add(Warning.TUMBLE_UNDER_THRUST);
						} else if (isApogee || !isSustainer) {
							addEvent(new FlightEvent(FlightEvent.Type.TUMBLE, currentStatus.getSimulationTime()));
							currentStatus.setTumbling(true);
						}
					}
					
				}

				// If I'm on the ground and have no events in the queue, I'm done
				if (currentStatus.isLanded() && currentStatus.getEventQueue().isEmpty())
					addEvent(new FlightEvent(FlightEvent.Type.SIMULATION_END, currentStatus.getSimulationTime()));
			}
			
		} catch (SimulationException e) {
			SimulationListenerHelper.fireEndSimulation(currentStatus, e);
			// Add FlightEvent for Abort.
			currentStatus.getFlightData().addEvent(new FlightEvent(FlightEvent.Type.EXCEPTION, currentStatus.getSimulationTime(), currentStatus.getConfiguration().getRocket(), e.getLocalizedMessage()));
			currentStatus.getWarnings().add(e.getLocalizedMessage());
		}
		
		return currentStatus.getFlightData();
	}	
	
	/**
	 * Handles events occurring during the flight from the event queue.
	 * Each event that has occurred before or at the current simulation time is
	 * processed.  Suitable events are also added to the flight data.
	 */
	private boolean handleEvents() throws SimulationException {
		boolean ret = true;
		FlightEvent event;
		
		log.trace("HandleEvents: current branch = " + currentStatus.getFlightData().getBranchName());
		for (event = nextEvent(); event != null; event = nextEvent()) {
			log.trace("Obtained event from queue:  " + event.toString());
			log.trace("Remaining EventQueue = " + currentStatus.getEventQueue().toString());

			// If I get an event other than ALTITUDE and SIMULATION_END after I'm on the ground, there's a problem
			if (currentStatus.isLanded() &&
				(event.getType() != FlightEvent.Type.ALTITUDE) &&
				(event.getType() != FlightEvent.Type.SIMULATION_END))
				currentStatus.getWarnings().add(new Warning.EventAfterLanding(event));
			
			// Check for motor ignition events, add ignition events to queue
			for (MotorClusterState state : currentStatus.getActiveMotors() ){
				if( state.testForIgnition(event )){
					final double simulationTime = currentStatus.getSimulationTime() ;

					MotorClusterState sourceState = (MotorClusterState) event.getData();
					double ignitionDelay = 0;
					if (event.getType() == FlightEvent.Type.BURNOUT)
						ignitionDelay = 0;
					else if (event.getType() == FlightEvent.Type.EJECTION_CHARGE)
						ignitionDelay = sourceState.getEjectionDelay();

					MotorMount mount = state.getMount();
					MotorConfiguration motorInstance = mount.getMotorConfig(this.fcid);
					ignitionDelay += motorInstance.getIgnitionDelay();
					
					final double ignitionTime = currentStatus.getSimulationTime() + ignitionDelay; 
					
					// TODO:  this event seems to get enqueue'd multiple times ... 
					log.info("Queueing Ignition Event for: "+state.toDescription()+" @: "+ignitionTime);
					//log.info("     Because of "+event.getType().name()+" @"+event.getTime()+" from: "+event.getSource().getName());
					
					addEvent(new FlightEvent(FlightEvent.Type.IGNITION, ignitionTime, (RocketComponent) mount, state ));
				}
			}
			
			// Ignore events for components that are no longer attached to the rocket
			if (event.getSource() != null && event.getSource().getParent() != null &&
					!currentStatus.getConfiguration().isComponentActive(event.getSource())) {
				log.trace("Ignoring event from unattached componenent");
				continue;
			}
			
			// Call simulation listeners, allow aborting event handling
			if (!SimulationListenerHelper.fireHandleFlightEvent(currentStatus, event)) {
				continue;
			}
			
			if (event.getType() == FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT) {
				RecoveryDevice device = (RecoveryDevice) event.getSource();
				if (!SimulationListenerHelper.fireRecoveryDeviceDeployment(currentStatus, device)) {
					continue;
				}
			}
			
			
			// Check for stage separation event
			for (AxialStage stage : currentStatus.getConfiguration().getActiveStages()) {
				int stageNo = stage.getStageNumber();
				if (stageNo == 0)
					continue;
				
				StageSeparationConfiguration separationConfig = stage.getSeparationConfigurations().get(this.fcid);
				if (separationConfig.getSeparationEvent().isSeparationEvent(event, stage)) {
					addEvent(new FlightEvent(FlightEvent.Type.STAGE_SEPARATION,
							event.getTime() + separationConfig.getSeparationDelay(), stage));
				}
			}
			
			
			// Check for recovery device deployment, add events to queue
			for (RocketComponent c : currentStatus.getConfiguration().getActiveComponents()) {
				if (!(c instanceof RecoveryDevice))
					continue;
				DeploymentConfiguration deployConfig = ((RecoveryDevice) c).getDeploymentConfigurations().get(this.fcid);
				if (deployConfig.isActivationEvent(event, c)) {
					// Delay event by at least 1ms to allow stage separation to occur first
					addEvent(new FlightEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT,
							event.getTime() + Math.max(0.001, deployConfig.getDeployDelay()), c));
				}
			}

			// Handle event
			log.trace("Handling event " + event);			
			switch (event.getType()) {
			
			case LAUNCH: {
				currentStatus.getFlightData().addEvent(event);
				break;
			}
			
			case IGNITION: {
				MotorClusterState motorState = (MotorClusterState) event.getData();
				
				log.info("  Igniting motor: "+motorState.toDescription()+" @"+currentStatus.getSimulationTime());
				motorState.ignite( event.getTime());

				// Ignite the motor
				currentStatus.setMotorIgnited(true);
				currentStatus.getFlightData().addEvent(event);
				
				// ... ignite ...uhh, again? 
				// TBH, I'm not sure what this call is for. It seems to be mostly a bunch of event distribution.
				MotorConfigurationId motorId = motorState.getID(); 
				MotorMount mount = (MotorMount) event.getSource();
				if (!SimulationListenerHelper.fireMotorIgnition(currentStatus, motorId, mount, motorState)) {
					continue;
				}

				// and queue up the burnout for this motor, as well. 
//				double duration = motorState.getMotor().getBurnTimeEstimate();
				double duration = motorState.getBurnTime();
				double burnout = currentStatus.getSimulationTime() + duration;
				addEvent(new FlightEvent(FlightEvent.Type.BURNOUT, burnout,
							event.getSource(), motorState ));
				break;
			}
			
			case LIFTOFF: {
				// Mark lift-off as occurred
				currentStatus.setLiftoff(true);
				currentStatus.getFlightData().addEvent(event);
				break;
			}
			
			case LAUNCHROD: {
				// Mark launch rod as cleared
				currentStatus.setLaunchRodCleared(true);
				currentStatus.getFlightData().addEvent(event);
				break;
			}
			
			case BURNOUT: {
				// If motor burnout occurs without lift-off, abort
				if (!currentStatus.isLiftoff()) {
					throw new SimulationLaunchException(trans.get("BasicEventSimulationEngine.error.earlyMotorBurnout"));
				}
				
				// Add ejection charge event
				MotorClusterState motorState = (MotorClusterState) event.getData();
				motorState.burnOut( event.getTime() );
				
				AxialStage stage = motorState.getMount().getStage();
				//log.debug( " adding EJECTION_CHARGE event for motor "+motorState.getMotor().getDesignation()+" on stage "+stage.getStageNumber()+": "+stage.getName());
				log.debug( " detected Motor Burnout for motor "+motorState.getMotor().getDesignation()+"@ "+event.getTime()+"  on stage "+stage.getStageNumber()+": "+stage.getName());
				
				double delay = motorState.getEjectionDelay();
				if ( motorState.hasEjectionCharge() ){
					addEvent(new FlightEvent(FlightEvent.Type.EJECTION_CHARGE, currentStatus.getSimulationTime() + delay,
							stage, event.getData()));
				}
				currentStatus.getFlightData().addEvent(event);
				break;
			}
			
			case EJECTION_CHARGE: {
				MotorClusterState motorState = (MotorClusterState) event.getData();
				motorState.expend( event.getTime() );
				currentStatus.getFlightData().addEvent(event);
				break;
			}
			
			case STAGE_SEPARATION: {
				// Record the event.
				currentStatus.getFlightData().addEvent(event);
				
				RocketComponent boosterStage = event.getSource();
				final int stageNumber = boosterStage.getStageNumber();
	
				// Mark the status as having dropped the booster
				currentStatus.getConfiguration().clearStage( stageNumber);
						  
				// Prepare the simulation branch
				SimulationStatus boosterStatus = new SimulationStatus(currentStatus);
				boosterStatus.setFlightData(new FlightDataBranch(boosterStage.getName(), FlightDataType.TYPE_TIME));
				// Mark the booster status as only having the booster.
				boosterStatus.getConfiguration().setOnlyStage(stageNumber);
				toSimulate.push(boosterStatus);
				log.info(String.format("==>> @ %g; from Branch: %s ---- Branching: %s ---- \n",
						currentStatus.getSimulationTime(), 
						currentStatus.getFlightData().getBranchName(), boosterStatus.getFlightData().getBranchName()));
				
				break;
			}
			
			case APOGEE:
				// Mark apogee as reached
				currentStatus.setApogeeReached(true);
				currentStatus.getFlightData().addEvent(event);
				// This apogee event might be the optimum if recovery has not already happened.
				if (currentStatus.getSimulationConditions().isCalculateExtras() && currentStatus.getDeployedRecoveryDevices().size() == 0) {
					currentStatus.getFlightData().setOptimumAltitude(currentStatus.getMaxAlt());
					currentStatus.getFlightData().setTimeToOptimumAltitude(currentStatus.getMaxAltTime());
				}
				break;
			
			case RECOVERY_DEVICE_DEPLOYMENT:
				RocketComponent c = event.getSource();
				int n = c.getStageNumber();
				// Ignore event if stage not active
				if (currentStatus.getConfiguration().isStageActive(n)) {
					// TODO: HIGH: Check stage activeness for other events as well?
					
					// Check whether any motor in the active stages is active anymore
					for (MotorClusterState state : currentStatus.getActiveMotors() ) {
						if ( state.isSpent() ) {
							continue;
						}
						currentStatus.getWarnings().add(Warning.RECOVERY_DEPLOYMENT_WHILE_BURNING);
					}
					
					// Check for launch rod
					if (!currentStatus.isLaunchRodCleared()) {
						currentStatus.getWarnings().add(Warning.RECOVERY_LAUNCH_ROD);
					}
					
					// Check current velocity
					if (currentStatus.getRocketVelocity().length() > 20) {
						currentStatus.getWarnings().add(new Warning.HighSpeedDeployment(currentStatus.getRocketVelocity().length()));
					}
					
					currentStatus.setLiftoff(true);
					currentStatus.getDeployedRecoveryDevices().add((RecoveryDevice) c);
					
					// If we haven't already reached apogee, then we need to compute the actual coast time
					// to determine the optimum altitude.
					if (currentStatus.getSimulationConditions().isCalculateExtras() && !currentStatus.isApogeeReached()) {
						FlightData coastStatus = computeCoastTime();

						currentStatus.getFlightData().setOptimumAltitude(coastStatus.getMaxAltitude());
						currentStatus.getFlightData().setTimeToOptimumAltitude(coastStatus.getTimeToApogee());
					}

					// switch to landing stepper (unless we're already on the ground)
					if (!currentStatus.isLanded()) {
						currentStepper = landingStepper;
						currentStatus = currentStepper.initialize(currentStatus);
					}
					
					currentStatus.getFlightData().addEvent(event);
				}
				break;
			
			case GROUND_HIT:
				currentStatus.setLanded(true);
				
				currentStepper = groundStepper;
				currentStatus = currentStepper.initialize(currentStatus);
				
				currentStatus.getFlightData().addEvent(event);
				break;
			
			case SIMULATION_END:
				ret = false;
				currentStatus.getFlightData().addEvent(event);
				break;
			
			case ALTITUDE:
				// nothing special needs to be done for this event
				break;
			
			case TUMBLE:
				if (!currentStatus.isLanded()) {
					currentStepper = tumbleStepper;
					currentStatus = currentStepper.initialize(currentStatus);
				}
				currentStatus.getFlightData().addEvent(event);
				break;
			}
			
		}
		
		if( 1200 < currentStatus.getSimulationTime() ){
			ret = false;
			log.error("Simulation hit max time (1200s): aborting.");
			currentStatus.getFlightData().addEvent(new FlightEvent( FlightEvent.Type.SIMULATION_END, currentStatus.getSimulationTime()));
		}
		
		
		// If no motor has ignited, abort
		if (!currentStatus.isMotorIgnited()) {
			throw new MotorIgnitionException(trans.get("BasicEventSimulationEngine.error.noIgnition"));
		}
		
		return ret;
	}
	
	/**
	 * Add a flight event to the event queue unless a listener aborts adding it.
	 *
	 * @param event		the event to add to the queue.
	 */
	private void addEvent(FlightEvent event) throws SimulationException {
		if (SimulationListenerHelper.fireAddFlightEvent(currentStatus, event)) {
			currentStatus.getEventQueue().add(event);
		}
	}
	
	
	
	/**
	 * Return the next flight event to handle, or null if no more events should be handled.
	 * This method jumps the simulation time forward in case no motors have been ignited
	 * The flight event is removed from the event queue.
	 *
	 * @return			the flight event to handle, or null
	 */
	private FlightEvent nextEvent() {
		EventQueue queue = currentStatus.getEventQueue();
		FlightEvent event = queue.peek();
		if (event == null)
			return null;
		
		// Jump to event if no motors have been ignited
		if (!currentStatus.isMotorIgnited() &&
			event.getTime() > currentStatus.getSimulationTime()) {
			currentStatus.setSimulationTime(event.getTime());
		}
		if (event.getTime() <= currentStatus.getSimulationTime()) {
			return queue.poll();
		} else {
			return null;
		}
	}
	
	
	
	private void checkNaN() throws SimulationException {
		double d = 0;
		boolean b = false;
		d += currentStatus.getSimulationTime();
		d += currentStatus.getPreviousTimeStep();
		b |= currentStatus.getRocketPosition().isNaN();
		b |= currentStatus.getRocketVelocity().isNaN();
		b |= currentStatus.getRocketOrientationQuaternion().isNaN();
		b |= currentStatus.getRocketRotationVelocity().isNaN();
		d += currentStatus.getEffectiveLaunchRodLength();
		
		if (Double.isNaN(d) || b) {
			log.error("Simulation resulted in NaN value:" +
					" simulationTime=" + currentStatus.getSimulationTime() +
					" previousTimeStep=" + currentStatus.getPreviousTimeStep() +
					" rocketPosition=" + currentStatus.getRocketPosition() +
					" rocketVelocity=" + currentStatus.getRocketVelocity() +
					" rocketOrientationQuaternion=" + currentStatus.getRocketOrientationQuaternion() +
					" rocketRotationVelocity=" + currentStatus.getRocketRotationVelocity() +
					" effectiveLaunchRodLength=" + currentStatus.getEffectiveLaunchRodLength());
			throw new SimulationException(trans.get("BasicEventSimulationEngine.error.NaNResult"));
		}
	}
	
	private FlightData computeCoastTime() {
		try {
			SimulationConditions conds = currentStatus.getSimulationConditions().clone();
			conds.getSimulationListenerList().add(OptimumCoastListener.INSTANCE);
			BasicEventSimulationEngine e = new BasicEventSimulationEngine();
		
			FlightData d = e.simulate(conds);
			return d;
		} catch (Exception e) {
			log.warn("Exception computing coast time: ", e);
			return null;
		}
	}
}
