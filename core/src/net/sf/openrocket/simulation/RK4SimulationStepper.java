package net.sf.openrocket.simulation;

import java.util.Arrays;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.models.atmosphere.AtmosphericConditions;
import net.sf.openrocket.simulation.exception.SimulationCalculationException;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.listeners.SimulationListenerHelper;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.GeodeticComputationStrategy;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Quaternion;
import net.sf.openrocket.util.Rotation2D;
import net.sf.openrocket.util.WorldCoordinate;

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
	
	private static final double MIN_TIME_STEP = 0.001;
	
	
	private Random random;
	
	
	
	
	@Override
	public RK4SimulationStatus initialize(SimulationStatus original) {
		
		RK4SimulationStatus status = new RK4SimulationStatus(original);
		// Copy the existing warnings
		status.setWarnings(original.getWarnings());
		
		SimulationConditions sim = original.getSimulationConditions();
		
		status.setLaunchRodDirection(new Coordinate(
				Math.sin(sim.getLaunchRodAngle()) * Math.cos(Math.PI / 2.0 - sim.getLaunchRodDirection()),
				Math.sin(sim.getLaunchRodAngle()) * Math.sin(Math.PI / 2.0 - sim.getLaunchRodDirection()),
				Math.cos(sim.getLaunchRodAngle())
				));
		
		this.random = new Random(original.getSimulationConditions().getRandomSeed() ^ SEED_RANDOMIZATION);
		
		return status;
	}
	
	


	@Override
	public void step(SimulationStatus simulationStatus, double maxTimeStep) throws SimulationException {
		
		RK4SimulationStatus status = (RK4SimulationStatus) simulationStatus;
		DataStore store = new DataStore();
		
		////////  Perform RK4 integration:  ////////
		
		RK4SimulationStatus status2;
		RK4Parameters k1, k2, k3, k4;
		
		/*
		 * Start with previous time step which is used to compute the initial thrust estimate.
		 * Don't make it longer than maxTimeStep, but at least MIN_TIME_STEP.
		 */
		store.timestep = status.getPreviousTimeStep();
		store.timestep = MathUtil.max(MathUtil.min(store.timestep, maxTimeStep), MIN_TIME_STEP);
		checkNaN(store.timestep);
		
		/*
		 * Compute the initial thrust estimate.  This is used for the first time step computation.
		 */
		store.thrustForce = calculateThrust(status, store.timestep, status.getPreviousAcceleration(),
				status.getPreviousAtmosphericConditions(), false);
		

		/*
		 * Perform RK4 integration.  Decide the time step length after the first step.
		 */

		//// First position, k1 = f(t, y)
		
		k1 = computeParameters(status, store);
		
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
		dt[0] = MathUtil.max(status.getSimulationConditions().getTimeStep(),MIN_TIME_STEP);
		dt[1] = maxTimeStep;
		dt[2] = status.getSimulationConditions().getMaximumAngleStep() / store.lateralPitchRate;
		dt[3] = Math.abs(MAX_ROLL_STEP_ANGLE / store.flightConditions.getRollRate());
		dt[4] = Math.abs(MAX_ROLL_RATE_CHANGE / store.rollAcceleration);
		dt[5] = Math.abs(MAX_PITCH_CHANGE / store.lateralPitchAcceleration);
		if (!status.isLaunchRodCleared()) {
			dt[0] /= 5.0;
			dt[6] = status.getSimulationConditions().getLaunchRodLength() / k1.v.length() / 10;
		}
		dt[7] = 1.5 * status.getPreviousTimeStep();
		
		store.timestep = Double.MAX_VALUE;
		int limitingValue = -1;
		for (int i = 0; i < dt.length; i++) {
			if (dt[i] < store.timestep) {
				store.timestep = dt[i];
				limitingValue = i;
			}
		}

		double minTimeStep = status.getSimulationConditions().getTimeStep() / 20;
		if (store.timestep < minTimeStep) {
			log.trace("Too small time step " + store.timestep + " (limiting factor " + limitingValue + "), using " +
					minTimeStep + " instead.");
			store.timestep = minTimeStep;
		} else {
			log.trace("Selected time step " + store.timestep + " (limiting factor " + limitingValue + ")");
		}
		checkNaN(store.timestep);
		
		/*
		 * Compute the correct thrust for this time step.  If the original thrust estimate differs more
		 * than 10% from the true value then recompute the RK4 step 1.  The 10% error in step 1 is
		 * diminished by it affecting only 1/6th of the total, so it's an acceptable error.
		 */
		double thrustEstimate = store.thrustForce;
		store.thrustForce = calculateThrust(status, store.timestep, store.longitudinalAcceleration,
				store.atmosphericConditions, true);
		double thrustDiff = Math.abs(store.thrustForce - thrustEstimate);
		// Log if difference over 1%, recompute if over 10%
		if (thrustDiff > 0.01 * thrustEstimate) {
			if (thrustDiff > 0.1 * thrustEstimate + 0.001) {
				log.debug("Thrust estimate differs from correct value by " +
						(Math.rint(1000 * (thrustDiff + 0.000001) / thrustEstimate) / 10.0) + "%," +
						" estimate=" + thrustEstimate +
						" correct=" + store.thrustForce +
						" timestep=" + store.timestep +
						", recomputing k1 parameters");
				k1 = computeParameters(status, store);
			} else {
				log.trace("Thrust estimate differs from correct value by " +
						(Math.rint(1000 * (thrustDiff + 0.000001) / thrustEstimate) / 10.0) + "%," +
						" estimate=" + thrustEstimate +
						" correct=" + store.thrustForce +
						" timestep=" + store.timestep +
						", error acceptable");
			}
		}
		
		// Store data
		// TODO: MEDIUM: Store acceleration etc of entire RK4 step, store should be cloned or something...
		storeData(status, store);
		

		//// Second position, k2 = f(t + h/2, y + k1*h/2)
		
		status2 = status.clone();
		status2.setSimulationTime(status.getSimulationTime() + store.timestep / 2);
		status2.setRocketPosition(status.getRocketPosition().add(k1.v.multiply(store.timestep / 2)));
		status2.setRocketVelocity(status.getRocketVelocity().add(k1.a.multiply(store.timestep / 2)));
		status2.setRocketOrientationQuaternion(status.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(k1.rv.multiply(store.timestep / 2))));
		status2.setRocketRotationVelocity(status.getRocketRotationVelocity().add(k1.ra.multiply(store.timestep / 2)));
		
		k2 = computeParameters(status2, store);
		

		//// Third position, k3 = f(t + h/2, y + k2*h/2)
		
		status2 = status.clone();
		status2.setSimulationTime(status.getSimulationTime() + store.timestep / 2);
		status2.setRocketPosition(status.getRocketPosition().add(k2.v.multiply(store.timestep / 2)));
		status2.setRocketVelocity(status.getRocketVelocity().add(k2.a.multiply(store.timestep / 2)));
		status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(k2.rv.multiply(store.timestep / 2))));
		status2.setRocketRotationVelocity(status.getRocketRotationVelocity().add(k2.ra.multiply(store.timestep / 2)));
		
		k3 = computeParameters(status2, store);
		

		//// Fourth position, k4 = f(t + h, y + k3*h)
		
		status2 = status.clone();
		status2.setSimulationTime(status.getSimulationTime() + store.timestep);
		status2.setRocketPosition(status.getRocketPosition().add(k3.v.multiply(store.timestep)));
		status2.setRocketVelocity(status.getRocketVelocity().add(k3.a.multiply(store.timestep)));
		status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(k3.rv.multiply(store.timestep))));
		status2.setRocketRotationVelocity(status.getRocketRotationVelocity().add(k3.ra.multiply(store.timestep)));
		
		k4 = computeParameters(status2, store);
		

		//// Sum all together,  y(n+1) = y(n) + h*(k1 + 2*k2 + 2*k3 + k4)/6
		


		Coordinate deltaV, deltaP, deltaR, deltaO;
		deltaV = k2.a.add(k3.a).multiply(2).add(k1.a).add(k4.a).multiply(store.timestep / 6);
		deltaP = k2.v.add(k3.v).multiply(2).add(k1.v).add(k4.v).multiply(store.timestep / 6);
		deltaR = k2.ra.add(k3.ra).multiply(2).add(k1.ra).add(k4.ra).multiply(store.timestep / 6);
		deltaO = k2.rv.add(k3.rv).multiply(2).add(k1.rv).add(k4.rv).multiply(store.timestep / 6);
		


		status.setRocketVelocity(status.getRocketVelocity().add(deltaV));
		status.setRocketPosition(status.getRocketPosition().add(deltaP));
		status.setRocketRotationVelocity(status.getRocketRotationVelocity().add(deltaR));
		status.setRocketOrientationQuaternion(status.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(deltaO)).normalizeIfNecessary());
		
		WorldCoordinate w = status.getSimulationConditions().getLaunchSite();
		w = status.getSimulationConditions().getGeodeticComputation().addCoordinate(w, status.getRocketPosition());
		status.setRocketWorldPosition(w);
		
		status.setSimulationTime(status.getSimulationTime() + store.timestep);
		
		status.setPreviousTimeStep(store.timestep);
		
		// Verify that values don't run out of range
		if (status.getRocketVelocity().length2() > 1e18 ||
				status.getRocketPosition().length2() > 1e18 ||
				status.getRocketRotationVelocity().length2() > 1e18) {
			throw new SimulationCalculationException(trans.get("error.valuesTooLarge"));
		}
	}
	
	



	private RK4Parameters computeParameters(RK4SimulationStatus status, DataStore dataStore)
			throws SimulationException {
		RK4Parameters params = new RK4Parameters();
		
		//		if (dataStore == null) {
		//			dataStore = new DataStore();
		//		}
		
		calculateAcceleration(status, dataStore);
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
	private void calculateAcceleration(RK4SimulationStatus status, DataStore store) throws SimulationException {
		
		// Call pre-listeners
		store.accelerationData = SimulationListenerHelper.firePreAccelerationCalculation(status);
		if (store.accelerationData != null) {
			return;
		}
		
		// Compute the forces affecting the rocket
		calculateForces(status, store);
		
		// Calculate mass data
		store.massData = calculateMassData(status);
		

		// Calculate the forces from the aerodynamic coefficients
		
		double dynP = (0.5 * store.flightConditions.getAtmosphericConditions().getDensity() *
					MathUtil.pow2(store.flightConditions.getVelocity()));
		double refArea = store.flightConditions.getRefArea();
		double refLength = store.flightConditions.getRefLength();
		

		// Linear forces in rocket coordinates
		store.dragForce = store.forces.getCaxial() * dynP * refArea;
		double fN = store.forces.getCN() * dynP * refArea;
		double fSide = store.forces.getCside() * dynP * refArea;
		
		double forceZ = store.thrustForce - store.dragForce;
		
		store.linearAcceleration = new Coordinate(-fN / store.massData.getCG().weight,
					-fSide / store.massData.getCG().weight,
					forceZ / store.massData.getCG().weight);
		
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
			
			store.linearAcceleration = status.getLaunchRodDirection().multiply(
						store.linearAcceleration.dot(status.getLaunchRodDirection()));
			store.angularAcceleration = Coordinate.NUL;
			store.rollAcceleration = 0;
			store.lateralPitchAcceleration = 0;
			
		} else {
			
			// Shift moments to CG
			double Cm = store.forces.getCm() - store.forces.getCN() * store.massData.getCG().x / refLength;
			double Cyaw = store.forces.getCyaw() - store.forces.getCside() * store.massData.getCG().x / refLength;
			
			// Compute moments
			double momX = -Cyaw * dynP * refArea * refLength;
			double momY = Cm * dynP * refArea * refLength;
			double momZ = store.forces.getCroll() * dynP * refArea * refLength;
			
			// Compute acceleration in rocket coordinates
			store.angularAcceleration = new Coordinate(momX / store.massData.getLongitudinalInertia(),
						momY / store.massData.getLongitudinalInertia(),
						momZ / store.massData.getRotationalInertia());
			
			store.rollAcceleration = store.angularAcceleration.z;
			// TODO: LOW: This should be hypot, but does it matter?
			store.lateralPitchAcceleration = MathUtil.max(Math.abs(store.angularAcceleration.x),
						Math.abs(store.angularAcceleration.y));
			
			store.angularAcceleration = store.thetaRotation.rotateZ(store.angularAcceleration);
			
			// Convert to world coordinates
			store.angularAcceleration = status.getRocketOrientationQuaternion().rotate(store.angularAcceleration);
			
		}
		
		// Call post-listeners
		store.accelerationData = SimulationListenerHelper.firePostAccelerationCalculation(status, store.accelerationData);
	}
	
	
	/**
	 * Calculate the aerodynamic forces into the data store.  This method also handles
	 * whether to include aerodynamic computation warnings or not.
	 */
	private void calculateForces(RK4SimulationStatus status, DataStore store) throws SimulationException {
		
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
		status.setMaxZVelocity(MathUtil.max(status.getMaxZVelocity(), status.getRocketVelocity().z));
		
		if (!status.isLaunchRodCleared()) {
			warnings = null;
		} else {
			if (status.getRocketVelocity().z < 0.2 * status.getMaxZVelocity())
				warnings = null;
			if (status.getStartWarningTime() < 0)
				status.setStartWarningTime(status.getSimulationTime() + 0.25);
		}
		if (status.getSimulationTime() < status.getStartWarningTime())
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
	private void calculateFlightConditions(RK4SimulationStatus status, DataStore store)
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
		Coordinate windSpeed = modelWindVelocity(status);
		Coordinate airSpeed = status.getRocketVelocity().add(windSpeed);
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
	
	

	private void storeData(RK4SimulationStatus status, DataStore store) {
		
		FlightDataBranch data = status.getFlightData();
		boolean extra = status.getSimulationConditions().isCalculateExtras();
		
		data.addPoint();
		data.setValue(FlightDataType.TYPE_TIME, status.getSimulationTime());
		data.setValue(FlightDataType.TYPE_ALTITUDE, status.getRocketPosition().z);
		data.setValue(FlightDataType.TYPE_POSITION_X, status.getRocketPosition().x);
		data.setValue(FlightDataType.TYPE_POSITION_Y, status.getRocketPosition().y);
		
		data.setValue(FlightDataType.TYPE_LATITUDE, status.getRocketWorldPosition().getLatitudeRad());
		data.setValue(FlightDataType.TYPE_LONGITUDE, status.getRocketWorldPosition().getLongitudeRad());
		if (status.getSimulationConditions().getGeodeticComputation() != GeodeticComputationStrategy.FLAT) {
			data.setValue(FlightDataType.TYPE_CORIOLIS_ACCELERATION, store.coriolisAcceleration.length());
		}
		
		if (extra) {
			data.setValue(FlightDataType.TYPE_POSITION_XY,
					MathUtil.hypot(status.getRocketPosition().x, status.getRocketPosition().y));
			data.setValue(FlightDataType.TYPE_POSITION_DIRECTION,
					Math.atan2(status.getRocketPosition().y, status.getRocketPosition().x));
			
			data.setValue(FlightDataType.TYPE_VELOCITY_XY,
					MathUtil.hypot(status.getRocketVelocity().x, status.getRocketVelocity().y));
			
			if (store.linearAcceleration != null) {
				data.setValue(FlightDataType.TYPE_ACCELERATION_XY,
						MathUtil.hypot(store.linearAcceleration.x, store.linearAcceleration.y));
				
				data.setValue(FlightDataType.TYPE_ACCELERATION_TOTAL, store.linearAcceleration.length());
			}
			
			if (store.flightConditions != null) {
				double Re = (store.flightConditions.getVelocity() *
						status.getConfiguration().getLength() /
						store.flightConditions.getAtmosphericConditions().getKinematicViscosity());
				data.setValue(FlightDataType.TYPE_REYNOLDS_NUMBER, Re);
			}
		}
		
		data.setValue(FlightDataType.TYPE_VELOCITY_Z, status.getRocketVelocity().z);
		if (store.linearAcceleration != null) {
			data.setValue(FlightDataType.TYPE_ACCELERATION_Z, store.linearAcceleration.z);
		}
		
		if (store.flightConditions != null) {
			data.setValue(FlightDataType.TYPE_VELOCITY_TOTAL, status.getRocketVelocity().length());
			data.setValue(FlightDataType.TYPE_MACH_NUMBER, store.flightConditions.getMach());
		}
		
		if (store.massData != null) {
			data.setValue(FlightDataType.TYPE_CG_LOCATION, store.massData.getCG().x);
		}
		if (status.isLaunchRodCleared()) {
			// Don't include CP and stability with huge launch AOA
			if (store.forces != null) {
				data.setValue(FlightDataType.TYPE_CP_LOCATION, store.forces.getCP().x);
			}
			if (store.forces != null && store.flightConditions != null && store.massData != null) {
				data.setValue(FlightDataType.TYPE_STABILITY,
						(store.forces.getCP().x - store.massData.getCG().x) / store.flightConditions.getRefLength());
			}
		}
		if (store.massData != null) {
			data.setValue(FlightDataType.TYPE_MASS, store.massData.getCG().weight);
			data.setValue(FlightDataType.TYPE_PROPELLANT_MASS, store.massData.getPropellantMass());
			data.setValue(FlightDataType.TYPE_LONGITUDINAL_INERTIA, store.massData.getLongitudinalInertia());
			data.setValue(FlightDataType.TYPE_ROTATIONAL_INERTIA, store.massData.getRotationalInertia());
		}
		
		data.setValue(FlightDataType.TYPE_THRUST_FORCE, store.thrustForce);
		data.setValue(FlightDataType.TYPE_DRAG_FORCE, store.dragForce);
		data.setValue(FlightDataType.TYPE_GRAVITY, store.gravity);
		
		if (status.isLaunchRodCleared() && store.forces != null) {
			if (store.massData != null && store.flightConditions != null) {
				data.setValue(FlightDataType.TYPE_PITCH_MOMENT_COEFF,
						store.forces.getCm() - store.forces.getCN() * store.massData.getCG().x / store.flightConditions.getRefLength());
				data.setValue(FlightDataType.TYPE_YAW_MOMENT_COEFF,
						store.forces.getCyaw() - store.forces.getCside() * store.massData.getCG().x / store.flightConditions.getRefLength());
			}
			data.setValue(FlightDataType.TYPE_NORMAL_FORCE_COEFF, store.forces.getCN());
			data.setValue(FlightDataType.TYPE_SIDE_FORCE_COEFF, store.forces.getCside());
			data.setValue(FlightDataType.TYPE_ROLL_MOMENT_COEFF, store.forces.getCroll());
			data.setValue(FlightDataType.TYPE_ROLL_FORCING_COEFF, store.forces.getCrollForce());
			data.setValue(FlightDataType.TYPE_ROLL_DAMPING_COEFF, store.forces.getCrollDamp());
			data.setValue(FlightDataType.TYPE_PITCH_DAMPING_MOMENT_COEFF,
					store.forces.getPitchDampingMoment());
		}
		
		if (store.forces != null) {
			data.setValue(FlightDataType.TYPE_DRAG_COEFF, store.forces.getCD());
			data.setValue(FlightDataType.TYPE_AXIAL_DRAG_COEFF, store.forces.getCaxial());
			data.setValue(FlightDataType.TYPE_FRICTION_DRAG_COEFF, store.forces.getFrictionCD());
			data.setValue(FlightDataType.TYPE_PRESSURE_DRAG_COEFF, store.forces.getPressureCD());
			data.setValue(FlightDataType.TYPE_BASE_DRAG_COEFF, store.forces.getBaseCD());
		}
		
		if (store.flightConditions != null) {
			data.setValue(FlightDataType.TYPE_REFERENCE_LENGTH, store.flightConditions.getRefLength());
			data.setValue(FlightDataType.TYPE_REFERENCE_AREA, store.flightConditions.getRefArea());
			
			data.setValue(FlightDataType.TYPE_PITCH_RATE, store.flightConditions.getPitchRate());
			data.setValue(FlightDataType.TYPE_YAW_RATE, store.flightConditions.getYawRate());
			data.setValue(FlightDataType.TYPE_ROLL_RATE, store.flightConditions.getRollRate());
			
			data.setValue(FlightDataType.TYPE_AOA, store.flightConditions.getAOA());
		}
		

		if (extra) {
			Coordinate c = status.getRocketOrientationQuaternion().rotateZ();
			double theta = Math.atan2(c.z, MathUtil.hypot(c.x, c.y));
			double phi = Math.atan2(c.y, c.x);
			if (phi < -(Math.PI - 0.0001))
				phi = Math.PI;
			data.setValue(FlightDataType.TYPE_ORIENTATION_THETA, theta);
			data.setValue(FlightDataType.TYPE_ORIENTATION_PHI, phi);
		}
		
		data.setValue(FlightDataType.TYPE_WIND_VELOCITY, store.windSpeed);
		
		if (store.flightConditions != null) {
			data.setValue(FlightDataType.TYPE_AIR_TEMPERATURE,
					store.flightConditions.getAtmosphericConditions().getTemperature());
			data.setValue(FlightDataType.TYPE_AIR_PRESSURE,
					store.flightConditions.getAtmosphericConditions().getPressure());
			data.setValue(FlightDataType.TYPE_SPEED_OF_SOUND,
					store.flightConditions.getAtmosphericConditions().getMachSpeed());
		}
		

		data.setValue(FlightDataType.TYPE_TIME_STEP, store.timestep);
		data.setValue(FlightDataType.TYPE_COMPUTATION_TIME,
				(System.nanoTime() - status.getSimulationStartWallTime()) / 1000000000.0);
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
	
	private static class DataStore {
		public double timestep = Double.NaN;
		
		public AccelerationData accelerationData;
		
		public AtmosphericConditions atmosphericConditions;
		
		public FlightConditions flightConditions;
		
		public double longitudinalAcceleration = Double.NaN;
		
		public MassData massData;
		
		public Coordinate coriolisAcceleration;
		
		public Coordinate linearAcceleration;
		public Coordinate angularAcceleration;
		
		// set by calculateFlightConditions and calculateAcceleration:
		public AerodynamicForces forces;
		public double windSpeed = Double.NaN;
		public double gravity = Double.NaN;
		public double thrustForce = Double.NaN;
		public double dragForce = Double.NaN;
		public double lateralPitchRate = Double.NaN;
		
		public double rollAcceleration = Double.NaN;
		public double lateralPitchAcceleration = Double.NaN;
		
		public Rotation2D thetaRotation;
		
	}
	
}
