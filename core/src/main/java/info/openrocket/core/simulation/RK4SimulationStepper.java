package info.openrocket.core.simulation;

import java.util.Arrays;
import java.util.Random;

import info.openrocket.core.logging.SimulationAbort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.aerodynamics.AerodynamicForces;
import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.masscalc.RigidBody;
import info.openrocket.core.models.atmosphere.AtmosphericConditions;
import info.openrocket.core.simulation.exception.SimulationCalculationException;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.listeners.SimulationListenerHelper;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.GeodeticComputationStrategy;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.Quaternion;
import info.openrocket.core.util.Rotation2D;
import info.openrocket.core.util.WorldCoordinate;

public class RK4SimulationStepper extends AbstractSimulationStepper {
	
	private static final Logger log = LoggerFactory.getLogger(RK4SimulationStepper.class);
	private static final Translator trans = Application.getTranslator();
	

	/** Random value with which to XOR the random seed value */
	private static final int SEED_RANDOMIZATION = 0x23E3A01F;
	

	/**
	 * A recommended reasonably accurate time step.
	 */
	public static final double RECOMMENDED_TIME_STEP = 0.05;
	
	/**
	 * A recommended maximum angle step value.
	 */
	public static final double RECOMMENDED_ANGLE_STEP = 3 * Math.PI / 180;
	
	/**
	 * A random amount that is added to pitch and yaw coefficients, plus or minus.
	 */
	public static final double PITCH_YAW_RANDOM = 0.0005;
	
	/**
	 * Maximum roll step allowed.  This is selected as an uneven division of the full
	 * circle so that the simulation will sample the most wind directions
	 */
	private static final double MAX_ROLL_STEP_ANGLE = 2 * 28.32 * Math.PI / 180;
	//	private static final double MAX_ROLL_STEP_ANGLE = 8.32 * Math.PI/180;
	
	private static final double MAX_ROLL_RATE_CHANGE = 2 * Math.PI / 180;
	private static final double MAX_PITCH_CHANGE = 4 * Math.PI / 180;
	
	private Random random;
	DataStore store = new DataStore();
	
	@Override
	public SimulationStatus initialize(SimulationStatus original) {
		
		SimulationStatus status = new SimulationStatus(original);
		// Copy the existing warnings
		status.setWarnings(original.getWarnings());
		
		SimulationConditions sim = original.getSimulationConditions();

		store.launchRodDirection = new Coordinate(
												  Math.sin(sim.getLaunchRodAngle()) * Math.cos(Math.PI / 2.0 - sim.getLaunchRodDirection()),
												  Math.sin(sim.getLaunchRodAngle()) * Math.sin(Math.PI / 2.0 - sim.getLaunchRodDirection()),
												  Math.cos(sim.getLaunchRodAngle()));

		this.random = new Random(original.getSimulationConditions().getRandomSeed() ^ SEED_RANDOMIZATION);
		
		return status;
	}
	
	


	@Override
	public void step(SimulationStatus simulationStatus, double maxTimeStep) throws SimulationException {
		
		SimulationStatus status = simulationStatus;

		////////  Perform RK4 integration:  ////////
		
		SimulationStatus status2;
		RK4Parameters k1, k2, k3, k4;

		/*
		 * Get the current atmospheric conditions
		 */
		calculateFlightConditions(status, store);
		store.atmosphericConditions = store.flightConditions.getAtmosphericConditions();

		/*
		 * Perform RK4 integration.  Decide the time step length after the first step.
		 */

		//// First position, k1 = f(t, y)
		
		k1 = computeParameters(status, store);
		store.storeData(status);
		
		/*
		 * Select the actual time step to use.  It is the minimum of the following:
		 *  dt[0]:  the user-specified time step (or 1/5th of it if still on the launch rod)
		 *  dt[1]:  the value of maxTimeStep
		 *  dt[2]:  the maximum pitch step angle limit
		 *  dt[3]:  the maximum roll step angle limit
		 *  dt[4]:  the maximum roll rate change limit
		 *  dt[5]:  the maximum pitch change limit
		 *  dt[6]:  1/10th of the launch rod length if still on the launch rod
		 *  dt[7]:  1.50 times the previous time step
		 * 
		 * The limits #5 and #6 are required since near the steady-state roll rate the roll rate
		 * may oscillate significantly even between the sub-steps of the RK4 integration.
		 * 
		 * The step is still at least 1/20th of the user-selected time step.
		 */
		double[] dt = new double[8];
		Arrays.fill(dt, Double.MAX_VALUE);

		// If the user selected a really small timestep, use MIN_TIME_STEP instead.
		dt[0] = MathUtil.max(status.getSimulationConditions().getTimeStep(), MIN_TIME_STEP);
		dt[1] = maxTimeStep;
		dt[2] = status.getSimulationConditions().getMaximumAngleStep() / store.lateralPitchRate;
		dt[3] = Math.abs(MAX_ROLL_STEP_ANGLE / store.flightConditions.getRollRate());
		dt[4] = Math.abs(MAX_ROLL_RATE_CHANGE / store.rollAcceleration);
		dt[5] = Math.abs(MAX_PITCH_CHANGE / store.lateralPitchAcceleration);
		if (!status.isLaunchRodCleared()) {
			dt[0] /= 5.0;
			dt[6] = status.getSimulationConditions().getLaunchRodLength() / k1.v.length() / 10;
		}
		dt[7] = 1.5 * store.timeStep;
		
		store.timeStep = Double.MAX_VALUE;
		int limitingValue = -1;
		for (int i = 0; i < dt.length; i++) {
			if (dt[i] < store.timeStep) {
				store.timeStep = dt[i];
				limitingValue = i;
			}
		}

		log.trace("Selected time step " + store.timeStep + " (limiting factor " + limitingValue + ")");

		// If we have a scheduled event coming up before the end of our timestep, truncate step
		// else if the time from the end of our timestep to the next scheduled event time is less than
		// minTimeStep, stretch it
		double minTimeStep = status.getSimulationConditions().getTimeStep() / 20;
		FlightEvent nextEvent = status.getEventQueue().peek();
		if (nextEvent != null) {
			double nextEventTime = nextEvent.getTime();
			if (status.getSimulationTime() + store.timeStep > nextEventTime) {
				store.timeStep = nextEventTime - status.getSimulationTime();
				log.trace("scheduled event at " + nextEventTime + " truncates timestep to " + store.timeStep);
			} else if ((status.getSimulationTime() + store.timeStep < nextEventTime) &&
					   (status.getSimulationTime() + store.timeStep + minTimeStep > nextEventTime)) {
				store.timeStep = nextEventTime - status.getSimulationTime();
				log.trace("Scheduled event at " + nextEventTime + " stretches timestep to " + store.timeStep);
			}
		}

		// If we've wound up with a too-small timestep, increase it avoid numerical instability even at the
		// cost of not being *quite* on an event
		if (store.timeStep < minTimeStep) {
			log.trace("Too small time step " + store.timeStep + " (limiting factor " + limitingValue + "), using " +
					minTimeStep + " instead.");
			store.timeStep = minTimeStep;
		}

		checkNaN(store.timeStep);



		//// Second position, k2 = f(t + h/2, y + k1*h/2)
		
		status2 = status.clone();
		status2.setSimulationTime(status.getSimulationTime() + store.timeStep / 2);
		status2.setRocketPosition(status.getRocketPosition().add(k1.v.multiply(store.timeStep / 2)));
		status2.setRocketVelocity(status.getRocketVelocity().add(k1.a.multiply(store.timeStep / 2)));
		status2.setRocketOrientationQuaternion(status.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(k1.rv.multiply(store.timeStep / 2))));
		status2.setRocketRotationVelocity(status.getRocketRotationVelocity().add(k1.ra.multiply(store.timeStep / 2)));
		
		k2 = computeParameters(status2, store);
		

		//// Third position, k3 = f(t + h/2, y + k2*h/2)
		
		status2 = status.clone();
		status2.setSimulationTime(status.getSimulationTime() + store.timeStep / 2);
		status2.setRocketPosition(status.getRocketPosition().add(k2.v.multiply(store.timeStep / 2)));
		status2.setRocketVelocity(status.getRocketVelocity().add(k2.a.multiply(store.timeStep / 2)));
		status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(k2.rv.multiply(store.timeStep / 2))));
		status2.setRocketRotationVelocity(status.getRocketRotationVelocity().add(k2.ra.multiply(store.timeStep / 2)));
		
		k3 = computeParameters(status2, store);
		

		//// Fourth position, k4 = f(t + h, y + k3*h)
		
		status2 = status.clone();
		status2.setSimulationTime(status.getSimulationTime() + store.timeStep);
		status2.setRocketPosition(status.getRocketPosition().add(k3.v.multiply(store.timeStep)));
		status2.setRocketVelocity(status.getRocketVelocity().add(k3.a.multiply(store.timeStep)));
		status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(k3.rv.multiply(store.timeStep))));
		status2.setRocketRotationVelocity(status.getRocketRotationVelocity().add(k3.ra.multiply(store.timeStep)));
		
		k4 = computeParameters(status2, store);
		

		//// Sum all together,  y(n+1) = y(n) + h*(k1 + 2*k2 + 2*k3 + k4)/6
		Coordinate deltaV, deltaP, deltaR, deltaO;
		deltaV = k2.a.add(k3.a).multiply(2).add(k1.a).add(k4.a).multiply(store.timeStep / 6);
		deltaP = k2.v.add(k3.v).multiply(2).add(k1.v).add(k4.v).multiply(store.timeStep / 6);
		deltaR = k2.ra.add(k3.ra).multiply(2).add(k1.ra).add(k4.ra).multiply(store.timeStep / 6);
		deltaO = k2.rv.add(k3.rv).multiply(2).add(k1.rv).add(k4.rv).multiply(store.timeStep / 6);
		

		status.setRocketVelocity(status.getRocketVelocity().add(deltaV));
		status.setRocketPosition(status.getRocketPosition().add(deltaP));
		status.setRocketRotationVelocity(status.getRocketRotationVelocity().add(deltaR));
		status.setRocketOrientationQuaternion(status.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(deltaO)).normalizeIfNecessary());
		
		WorldCoordinate w = status.getSimulationConditions().getLaunchSite();
		w = status.getSimulationConditions().getGeodeticComputation().addCoordinate(w, status.getRocketPosition());
		status.setRocketWorldPosition(w);
		
		if (!(0 <= store.timeStep)) {
			// Also catches NaN
			throw new IllegalArgumentException("Stepping backwards in time, timestep=" + store.timeStep);
		}
		status.setSimulationTime(status.getSimulationTime() + store.timeStep);
		
		// Store data
		// TODO: MEDIUM: Store acceleration etc of entire RK4 step, store should be cloned or something...
		status.getFlightDataBranch().addPoint();
		status.storeData();
		store.storeData(status);
		
		// Verify that values don't run out of range
		if (status.getRocketVelocity().length2() > 1e18 ||
				status.getRocketPosition().length2() > 1e18 ||
				status.getRocketRotationVelocity().length2() > 1e18) {
			throw new SimulationCalculationException(trans.get("error.valuesTooLarge"), status.getFlightDataBranch());
		}
	}
	
	



	private RK4Parameters computeParameters(SimulationStatus status, DataStore dataStore)
			throws SimulationException {
		RK4Parameters params = new RK4Parameters();
		
		// Call pre-listeners
		store.accelerationData = SimulationListenerHelper.firePreAccelerationCalculation(status);

		// Calculate acceleration (if not overridden by pre-listeners)
		if (store.accelerationData == null) {
			store.accelerationData = calculateAcceleration(status, dataStore);
		}

		// Call post-listeners
		store.accelerationData = SimulationListenerHelper.firePostAccelerationCalculation(status, store.accelerationData);

		params.a = dataStore.linearAcceleration;
		params.ra = dataStore.angularAcceleration;
		params.v = status.getRocketVelocity();
		params.rv = status.getRocketRotationVelocity();
		
		checkNaN(params.a);
		checkNaN(params.ra);
		checkNaN(params.v);
		checkNaN(params.rv);
		
		return params;
	}
	
	



	/**
	 * Calculate the linear and angular acceleration at the given status.  The results
	 * are stored in the fields {@link #linearAcceleration} and {@link #angularAcceleration}.
	 *  
	 * @param status   the status of the rocket.
	 * @throws SimulationException 
	 */
	private AccelerationData calculateAcceleration(SimulationStatus status, DataStore store) throws SimulationException {
		
		// Compute the forces affecting the rocket
		calculateForces(status, store);
		
		// Calculate mass data
		RigidBody structureMassData = calculateStructureMass(status);
		
		store.motorMass = calculateMotorMass(status);
		store.rocketMass = structureMassData.add( store.motorMass );

		if (store.rocketMass.getMass() < MathUtil.EPSILON) {
			status.abortSimulation(SimulationAbort.Cause.ACTIVE_MASS_ZERO);
		}

		// Calculate the forces from the aerodynamic coefficients
		
		double dynP = (0.5 * store.flightConditions.getAtmosphericConditions().getDensity() *
					MathUtil.pow2(store.flightConditions.getVelocity()));
		double refArea = store.flightConditions.getRefArea();
		double refLength = store.flightConditions.getRefLength();
		

		// Linear forces in rocket coordinates
		store.dragForce = store.forces.getCDaxial() * dynP * refArea;
		double fN = store.forces.getCN() * dynP * refArea;
		double fSide = store.forces.getCside() * dynP * refArea;

		store.thrustForce = calculateThrust(status, store.longitudinalAcceleration, store.flightConditions.getAtmosphericConditions(), false);
		double forceZ =  store.thrustForce - store.dragForce;
		
		store.linearAcceleration = new Coordinate(-fN / store.rocketMass.getMass(),
					-fSide / store.rocketMass.getMass(),
					forceZ / store.rocketMass.getMass());
		
		store.linearAcceleration = store.thetaRotation.rotateZ(store.linearAcceleration);
		
		// Convert into rocket world coordinates
		store.linearAcceleration = status.getRocketOrientationQuaternion().rotate(store.linearAcceleration);
		
		// add effect of gravity
		store.gravity = modelGravity(status);
		store.linearAcceleration = store.linearAcceleration.sub(0, 0, store.gravity);
		
		// add effect of Coriolis acceleration
		store.coriolisAcceleration = status.getSimulationConditions().getGeodeticComputation()
				.getCoriolisAcceleration(status.getRocketWorldPosition(), status.getRocketVelocity());
		store.linearAcceleration = store.linearAcceleration.add(store.coriolisAcceleration);
		
		// If still on the launch rod, project acceleration onto launch rod direction and
		// set angular acceleration to zero.
		if (!status.isLaunchRodCleared()) {
			
			store.linearAcceleration = store.launchRodDirection.multiply(store.linearAcceleration.dot(store.launchRodDirection));
			store.angularAcceleration = Coordinate.NUL;
			store.rollAcceleration = 0;
			store.lateralPitchAcceleration = 0;
			
		} else {
			
			// Shift moments to CG
			double Cm = store.forces.getCm() - store.forces.getCN() * store.rocketMass.getCM().x / refLength;
			double Cyaw = store.forces.getCyaw() - store.forces.getCside() * store.rocketMass.getCM().x / refLength;
			
			// Compute moments
			double momX = -Cyaw * dynP * refArea * refLength;
			double momY = Cm * dynP * refArea * refLength;
			double momZ = store.forces.getCroll() * dynP * refArea * refLength;
			
			// Compute acceleration in rocket coordinates
			store.angularAcceleration = new Coordinate(momX / store.rocketMass.getLongitudinalInertia(),
						momY / store.rocketMass.getLongitudinalInertia(),
						momZ / store.rocketMass.getRotationalInertia());
			
			store.rollAcceleration = store.angularAcceleration.z;
			// TODO: LOW: This should be hypot, but does it matter?
			store.lateralPitchAcceleration = MathUtil.max(Math.abs(store.angularAcceleration.x),
						Math.abs(store.angularAcceleration.y));
			
			store.angularAcceleration = store.thetaRotation.rotateZ(store.angularAcceleration);
			
			// Convert to world coordinates
			store.angularAcceleration = status.getRocketOrientationQuaternion().rotate(store.angularAcceleration);
			
		}

		return new AccelerationData(null, null, store.linearAcceleration, store.angularAcceleration, status.getRocketOrientationQuaternion());
	}
	
	
	/**
	 * Calculate the aerodynamic forces into the data store.  This method also handles
	 * whether to include aerodynamic computation warnings or not.
	 */
	private void calculateForces(SimulationStatus status, DataStore store) throws SimulationException {
		
		// Call pre-listeners
		store.forces = SimulationListenerHelper.firePreAerodynamicCalculation(status);
		if (store.forces != null) {
			return;
		}
		
		// Compute flight conditions
		calculateFlightConditions(status, store);
		
		/*
		 * Check whether to store warnings or not.  Warnings are ignored when on the 
		 * launch rod or 0.25 seconds after departure, and when the velocity has dropped
		 * below 20% of the max. velocity.
		 */
		WarningSet warnings = status.getWarnings();
		store.maxZvelocity = MathUtil.max(store.maxZvelocity, status.getRocketVelocity().z);
		
		if (!status.isLaunchRodCleared()) {
			warnings = null;
		} else {
			if (status.getRocketVelocity().z < 0.2 * store.maxZvelocity) {
				warnings = null;
			}
			if (Double.isNaN(store.startWarningTime)) {
				store.startWarningTime = status.getSimulationTime() + 0.25;
			}
		}

		if (!(status.getSimulationTime() > store.startWarningTime))
			warnings = null;

		// Calculate aerodynamic forces
		store.forces = status.getSimulationConditions().getAerodynamicCalculator()
				.getAerodynamicForces(status.getConfiguration(), store.flightConditions, warnings);
		

		// Add very small randomization to yaw & pitch moments to prevent over-perfect flight
		// TODO: HIGH: This should rather be performed as a listener
		store.forces.setCm(store.forces.getCm() + (PITCH_YAW_RANDOM * 2 * (random.nextDouble() - 0.5)));
		store.forces.setCyaw(store.forces.getCyaw() + (PITCH_YAW_RANDOM * 2 * (random.nextDouble() - 0.5)));
		

		// Call post-listeners
		store.forces = SimulationListenerHelper.firePostAerodynamicCalculation(status, store.forces);
	}
	
	

	/**
	 * Calculate and return the flight conditions for the current rocket status.
	 * Listeners can override these if necessary.
	 * <p>
	 * Additionally the fields thetaRotation and lateralPitchRate are defined in
	 * the data store, and can be used after calling this method.
	 */
	private void calculateFlightConditions(SimulationStatus status, DataStore store)
			throws SimulationException {
		
		// Call pre listeners, allow complete override
		store.flightConditions = SimulationListenerHelper.firePreFlightConditions(
				status);
		if (store.flightConditions != null) {
			// Compute the store values
			store.thetaRotation = new Rotation2D(store.flightConditions.getTheta());
			store.lateralPitchRate = Math.hypot(store.flightConditions.getPitchRate(), store.flightConditions.getYawRate());
			return;
		}
		


		//// Atmospheric conditions
		AtmosphericConditions atmosphere = modelAtmosphericConditions(status);
		store.flightConditions = new FlightConditions(status.getConfiguration());
		store.flightConditions.setAtmosphericConditions(atmosphere);
		

		//// Local wind speed and direction
		store.windVelocity = modelWindVelocity(status);
		Coordinate airSpeed = status.getRocketVelocity().add(store.windVelocity);
		airSpeed = status.getRocketOrientationQuaternion().invRotate(airSpeed);
		

		// Lateral direction:
		double len = MathUtil.hypot(airSpeed.x, airSpeed.y);
		if (len > 0.0001) {
			store.thetaRotation = new Rotation2D(airSpeed.y / len, airSpeed.x / len);
			store.flightConditions.setTheta(Math.atan2(airSpeed.y, airSpeed.x));
		} else {
			store.thetaRotation = Rotation2D.ID;
			store.flightConditions.setTheta(0);
		}
		
		double velocity = airSpeed.length();
		store.flightConditions.setVelocity(velocity);
		if (velocity > 0.01) {
			// aoa must be calculated from the monotonous cosine
			// sine can be calculated by a simple division
			store.flightConditions.setAOA(Math.acos(airSpeed.z / velocity), len / velocity);
		} else {
			store.flightConditions.setAOA(0);
		}
		

		// Roll, pitch and yaw rate
		Coordinate rot = status.getRocketOrientationQuaternion().invRotate(status.getRocketRotationVelocity());
		rot = store.thetaRotation.invRotateZ(rot);
		
		store.flightConditions.setRollRate(rot.z);
		if (len < 0.001) {
			store.flightConditions.setPitchRate(0);
			store.flightConditions.setYawRate(0);
			store.lateralPitchRate = 0;
		} else {
			store.flightConditions.setPitchRate(rot.y);
			store.flightConditions.setYawRate(rot.x);
			// TODO: LOW: set this as power of two?
			store.lateralPitchRate = MathUtil.hypot(rot.x, rot.y);
		}
		

		// Call post listeners
		FlightConditions c = SimulationListenerHelper.firePostFlightConditions(
				status, store.flightConditions);
		if (c != store.flightConditions) {
			// Listeners changed the values, recalculate data store
			store.flightConditions = c;
			store.thetaRotation = new Rotation2D(store.flightConditions.getTheta());
			store.lateralPitchRate = Math.hypot(store.flightConditions.getPitchRate(), store.flightConditions.getYawRate());
		}
		
	}

	private static class RK4Parameters {
		/** Linear acceleration */
		public Coordinate a;
		/** Linear velocity */
		public Coordinate v;
		/** Rotational acceleration */
		public Coordinate ra;
		/** Rotational velocity */
		public Coordinate rv;
	}
}
