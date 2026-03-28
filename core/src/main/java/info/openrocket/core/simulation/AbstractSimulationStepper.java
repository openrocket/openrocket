package info.openrocket.core.simulation;

import java.util.List;
import java.util.Map;

import info.openrocket.core.aerodynamics.AerodynamicCalculator;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.aerodynamics.AerodynamicForces;
import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.masscalc.MassCalculator;
import info.openrocket.core.masscalc.RigidBody;
import info.openrocket.core.models.atmosphere.AtmosphericConditions;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.listeners.SimulationListenerHelper;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.Quaternion;
import info.openrocket.core.util.Rotation2D;
import info.openrocket.core.util.MutableCoordinate;

public abstract class AbstractSimulationStepper implements SimulationStepper {
	private static final Logger log = LoggerFactory.getLogger(AbstractSimulationStepper.class);

	protected static final double MIN_TIME_STEP = 0.001;

	private final MutableCoordinate tempVelocity = new MutableCoordinate();
	private final MutableCoordinate tempRotation = new MutableCoordinate();

	/*
	 * calculate acceleration at a given point in time
	 *
	 */
	abstract void calculateAcceleration(SimulationStatus status, DataStore store) throws SimulationException;

	/**
	 * Calculate the flight conditions for the current rocket status.
	 * Listeners can override these if necessary.
	 * <p>
	 * Additionally the fields thetaRotation and lateralPitchRate are defined in
	 * the data store, and can be used after calling this method.
	 */
	protected void calculateFlightConditions(SimulationStatus status, DataStore store)
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
		MutableCoordinate airSpeed = tempVelocity;
		airSpeed.set(status.getRocketVelocity());
		airSpeed.add(store.windVelocity);
		status.getRocketOrientationQuaternion().invRotateInPlace(airSpeed);

		// Lateral direction:
		double len = MathUtil.hypot(airSpeed.getX(), airSpeed.getY());
		if (len > 0.0001) {
			store.thetaRotation = new Rotation2D(airSpeed.getY() / len, airSpeed.getX() / len);
			store.flightConditions.setTheta(Math.atan2(airSpeed.getY(), airSpeed.getX()));
		} else {
			store.thetaRotation = Rotation2D.ID;
			store.flightConditions.setTheta(0);
		}

		double velocity = airSpeed.length();
		store.flightConditions.setVelocity(velocity);
		if (velocity > 0.01) {
			// aoa must be calculated from the monotonous cosine
			// sine can be calculated by a simple division
			store.flightConditions.setAOA(Math.acos(airSpeed.getZ() / velocity), len / velocity);
		} else {
			store.flightConditions.setAOA(0);
		}

		// Roll, pitch and yaw rate
		MutableCoordinate rot = tempRotation;
		rot.set(status.getRocketRotationVelocity());
		status.getRocketOrientationQuaternion().invRotateInPlace(rot);
		store.thetaRotation.invRotateZInPlace(rot);

		store.flightConditions.setRollRate(rot.getZ());
		if (len < 0.001) {
			store.flightConditions.setPitchRate(0);
			store.flightConditions.setYawRate(0);
			store.lateralPitchRate = 0;
		} else {
			store.flightConditions.setPitchRate(rot.getY());
			store.flightConditions.setYawRate(rot.getX());
			// TODO: LOW: set this as power of two?
			store.lateralPitchRate = MathUtil.hypot(rot.getX(), rot.getY());
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

	/**
	 * Compute the atmospheric conditions, allowing listeners to override.
	 *
	 * @param status the simulation status
	 * @throws SimulationException if a listener throws SimulationException
	 * @return            the atmospheric conditions to use
	 */
	protected AtmosphericConditions modelAtmosphericConditions(SimulationStatus status) throws SimulationException {
		AtmosphericConditions conditions;

		// Call pre-listener
		conditions = SimulationListenerHelper.firePreAtmosphericModel(status);
		if (conditions != null) {
			return conditions;
		}

		// Compute conditions
		double altitude = status.getRocketPosition().getZ() + status.getSimulationConditions().getLaunchSite().getAltitude();
		conditions = status.getSimulationConditions().getAtmosphericModel().getConditions(altitude);

		// Call post-listener
		conditions = SimulationListenerHelper.firePostAtmosphericModel(status, conditions);

		checkNaN(conditions.getPressure(), "conditions.getPressure()");
		checkNaN(conditions.getTemperature(), "conditions.getTemperature()");

		return conditions;
	}

	/**
	 * Compute the wind to use, allowing listeners to override.
	 *
	 * @param status the simulation status
	 * @throws SimulationException if a listener throws SimulationException
	 * @return            the wind conditions to use
	 */
	protected CoordinateIF modelWindVelocity(SimulationStatus status) throws SimulationException {
		CoordinateIF wind;

		// Call pre-listener
		wind = SimulationListenerHelper.firePreWindModel(status);
		if (wind != null) {
			return wind;
		}

		// Compute conditions
		double altitudeAGL = status.getRocketPosition().getZ();
		double altitudeMSL = altitudeAGL + status.getSimulationConditions().getLaunchSite().getAltitude();
		wind = status.getSimulationConditions().getWindModel().getWindVelocity(status.getSimulationTime(), altitudeMSL, altitudeAGL);

		// Call post-listener
		wind = SimulationListenerHelper.firePostWindModel(status, wind);

		checkNaN(wind, "wind");

		return wind;
	}

	/**
	 * Compute the gravity to use, allowing listeners to override.
	 *
	 * @param status the simulation status
	 * @throws SimulationException if a listener throws SimulationException
	 * @return            the gravitational acceleration to use
	 */
	protected double modelGravity(SimulationStatus status) throws SimulationException {
		double gravity;

		// Call pre-listener
		gravity = SimulationListenerHelper.firePreGravityModel(status);
		if (!Double.isNaN(gravity)) {
			return gravity;
		}

		// Compute conditions
		gravity = status.getSimulationConditions().getGravityModel().getGravity(status.getRocketWorldPosition());

		// Call post-listener
		gravity = SimulationListenerHelper.firePostGravityModel(status, gravity);

		checkNaN(gravity, "gravity");

		return gravity;
	}

	/**
	 * Compute the mass data to use, allowing listeners to override.
	 *
	 * @param status the simulation status
	 * @throws SimulationException if a listener throws SimulationException
	 * @return            the mass data to use
	 */
	protected RigidBody calculateStructureMass(SimulationStatus status) throws SimulationException {
		RigidBody structureMass;

		// Call pre-listener
		structureMass = SimulationListenerHelper.firePreMassCalculation(status);
		if (structureMass != null) {
			return structureMass;
		}

		structureMass = MassCalculator.calculateStructure(status.getConfiguration());

		// Call post-listener
		structureMass = SimulationListenerHelper.firePostMassCalculation(status, structureMass);

		checkNaN(structureMass.getCenterOfMass(), "structureMass.getCenterOfMass()");
		checkNaN(structureMass.getLongitudinalInertia(), "structureMass.getLongitudinalInertia()");
		checkNaN(structureMass.getRotationalInertia(), "structureMass.getRotationalInertia()");

		return structureMass;
	}

	protected RigidBody calculateMotorMass(SimulationStatus status) throws SimulationException {
		RigidBody motorMass;

		// Call pre-listener
		motorMass = SimulationListenerHelper.firePreMassCalculation(status);
		if (motorMass != null) {
			return motorMass;
		}

		motorMass = MassCalculator.calculateMotor(status);


		// Call post-listener
		motorMass = SimulationListenerHelper.firePostMassCalculation(status, motorMass);

		checkNaN(motorMass.getCenterOfMass(), "motorMass.getCenterOfMass()");
		checkNaN(motorMass.getLongitudinalInertia(), "motorMass.getLongitudinalInertia()");
		checkNaN(motorMass.getRotationalInertia(), "motorMass.getRotationalInertia()");

		return motorMass;
	}

	/**
	 * Check that the provided value is not NaN.
	 *
	 * @param d the double value to check.
	 * @throws BugException if the value is NaN.
	 */
	protected void checkNaN(double d, String var) {
		if (Double.isNaN(d)) {
			throw new BugException("Simulation resulted in not-a-number (NaN) value for " + var + ", please report a bug.");
		}
	}

	/**
	 * Check that the provided coordinate is not NaN.
	 *
	 * @param c the coordinate value to check.
	 * @throws BugException if the value is NaN.
	 */
	protected void checkNaN(CoordinateIF c, String var) {
		if (c.isNaN()) {
			throw new BugException("Simulation resulted in not-a-number (NaN) value for " + var + ", please report a bug, c=" + c);
		}
	}


	/**
	 * Check that the provided quaternion is not NaN.
	 *
	 * @param q the quaternion value to check.
	 * @throws BugException if the value is NaN.
	 */
	protected void checkNaN(Quaternion q, String var) {
		if (q.isNaN()) {
			throw new BugException("Simulation resulted in not-a-number (NaN) value for " + var + ", please report a bug, q=" + q);
		}
	}

	/**
	 * Set status and store to values consistent with sitting on the ground
	 *
	 */
	protected void landedValues(SimulationStatus status, DataStore store) throws SimulationException {
		store.timeStep = Double.NaN;

		// get flight conditions
		calculateFlightConditions(status, store);
		FlightConditions flightConditions = store.flightConditions;
		flightConditions.setAOA(Double.NaN);
		flightConditions.setRollRate(0);
		flightConditions.setPitchRate(0);
		flightConditions.setYawRate(0);

		// note most of our forces don't end up getting set, so they're all NaN.
		AerodynamicForces forces = new AerodynamicForces();
		forces.setCD(Double.NaN);
		forces.setCDaxial(Double.NaN);
		forces.setFrictionCD(Double.NaN);
		forces.setPressureCD(Double.NaN);
		forces.setBaseCD(Double.NaN);
		store.forces = forces;

		RigidBody structureMassData = calculateStructureMass(status);
		store.motorMass = calculateMotorMass(status);
		store.rocketMass = structureMassData.add(store.motorMass);
		store.gravity = modelGravity(status);
		store.thrustForce = 0.0;
		store.dragForce = 0.0;
		store.coriolisAcceleration = Coordinate.ZERO;

		store.accelerationData = new AccelerationData(Coordinate.ZERO, Coordinate.ZERO, null, null,
				new Quaternion());

		status.setRocketPosition(new Coordinate(status.getRocketPosition().getX(), status.getRocketPosition().getY(), 0));
		status.setRocketVelocity(Coordinate.ZERO);
	}

	/*
	 * The DataStore holds calculated data to be used in computing a simulation step.
	 * It is saved to the FlightDataBranch at the beginning of the time step, and one
	 * extra time following the final simulation step so we have a full set of data for
	 * the final step.

	 * Note that it's a little shady to save this data only at the start of an RK4SimulationStepper
	 * step, since the contents change over the course of a step.
	 */
	protected static class DataStore {

		public double timeStep = Double.NaN;

		public AccelerationData accelerationData;

		public FlightConditions flightConditions;

		public RigidBody rocketMass;

		public RigidBody motorMass;

		public CoordinateIF coriolisAcceleration;

		public CoordinateIF launchRodDirection = null;

		// set by calculateFlightConditions and calculateAcceleration:
		public AerodynamicForces forces;
		public CoordinateIF windVelocity = new Coordinate(Double.NaN, Double.NaN, Double.NaN);
		public double gravity = Double.NaN;
		public double thrustForce = Double.NaN;
		public double dragForce = Double.NaN;
		public double lateralPitchRate = Double.NaN;

		public Rotation2D thetaRotation;

		void storeData(SimulationStatus status) {

			FlightDataBranch dataBranch = status.getFlightDataBranch();

			dataBranch.setValue(FlightDataType.TYPE_THRUST_FORCE, thrustForce);
			dataBranch.setValue(FlightDataType.TYPE_GRAVITY, gravity);
			dataBranch.setValue(FlightDataType.TYPE_DRAG_FORCE, dragForce);

			dataBranch.setValue(FlightDataType.TYPE_WIND_VELOCITY, windVelocity.length());
			dataBranch.setValue(FlightDataType.TYPE_WIND_DIRECTION, getWindDirection(windVelocity));
			dataBranch.setValue(FlightDataType.TYPE_TIME_STEP, timeStep);

			if (null != coriolisAcceleration) {
				dataBranch.setValue(FlightDataType.TYPE_CORIOLIS_ACCELERATION, coriolisAcceleration.length());
			}

			if (null != accelerationData) {
				dataBranch.setValue(FlightDataType.TYPE_ACCELERATION_XY,
						MathUtil.hypot(accelerationData.getLinearAccelerationWC().getX(), accelerationData.getLinearAccelerationWC().getY()));

				dataBranch.setValue(FlightDataType.TYPE_ACCELERATION_TOTAL, accelerationData.getLinearAccelerationWC().length());
				dataBranch.setValue(FlightDataType.TYPE_ACCELERATION_Z, accelerationData.getLinearAccelerationWC().getZ());
			}

			if (null != rocketMass) {
				double weight = rocketMass.getMass() * gravity;
				dataBranch.setValue(FlightDataType.TYPE_THRUST_WEIGHT_RATIO, thrustForce / weight);
				dataBranch.setValue(FlightDataType.TYPE_CG_LOCATION, rocketMass.getCM().getX());
				dataBranch.setValue(FlightDataType.TYPE_MASS, rocketMass.getMass());
				dataBranch.setValue(FlightDataType.TYPE_LONGITUDINAL_INERTIA, rocketMass.getLongitudinalInertia());
				dataBranch.setValue(FlightDataType.TYPE_ROTATIONAL_INERTIA, rocketMass.getRotationalInertia());
			}

			if (null != motorMass) {
				dataBranch.setValue(FlightDataType.TYPE_MOTOR_MASS, motorMass.getMass());
			}

			if (null != flightConditions) {
				double Re = (flightConditions.getVelocity() *
						status.getConfiguration().getLengthAerodynamic() /
						flightConditions.getAtmosphericConditions().getKinematicViscosity());
				dataBranch.setValue(FlightDataType.TYPE_REYNOLDS_NUMBER, Re);
				dataBranch.setValue(FlightDataType.TYPE_MACH_NUMBER, flightConditions.getMach());
				dataBranch.setValue(FlightDataType.TYPE_REFERENCE_LENGTH, flightConditions.getRefLength());
				dataBranch.setValue(FlightDataType.TYPE_REFERENCE_AREA, flightConditions.getRefArea());

				dataBranch.setValue(FlightDataType.TYPE_PITCH_RATE, flightConditions.getPitchRate());
				dataBranch.setValue(FlightDataType.TYPE_YAW_RATE, flightConditions.getYawRate());
				dataBranch.setValue(FlightDataType.TYPE_ROLL_RATE, flightConditions.getRollRate());

				dataBranch.setValue(FlightDataType.TYPE_AOA, flightConditions.getAOA());
				dataBranch.setValue(FlightDataType.TYPE_AIR_TEMPERATURE,
						flightConditions.getAtmosphericConditions().getTemperature());
				dataBranch.setValue(FlightDataType.TYPE_AIR_PRESSURE,
						flightConditions.getAtmosphericConditions().getPressure());
				dataBranch.setValue(FlightDataType.TYPE_AIR_DENSITY,
						flightConditions.getAtmosphericConditions().getDensity());
				dataBranch.setValue(FlightDataType.TYPE_SPEED_OF_SOUND,
						flightConditions.getAtmosphericConditions().getMachSpeed());
			}

			DampingMomentComponents dampingMoment = computeDampingMomentCoefficientComponents(status, dataBranch);
			dataBranch.setValue(FlightDataType.TYPE_DAMPING_MOMENT_COEFF, dampingMoment.total);
			dataBranch.setValue(FlightDataType.TYPE_DAMPING_MOMENT_COEFF_AERODYNAMIC, dampingMoment.aerodynamic);
			dataBranch.setValue(FlightDataType.TYPE_DAMPING_MOMENT_COEFF_PROPULSIVE, dampingMoment.propulsive);

			double correctiveMomentCoefficient = computeCorrectiveMomentCoefficient(status);
			dataBranch.setValue(FlightDataType.TYPE_CORRECTIVE_MOMENT_COEFF, correctiveMomentCoefficient);

			dataBranch.setValue(FlightDataType.TYPE_DAMPING_RATIO,
					computeDampingRatio(status, dampingMoment.total, correctiveMomentCoefficient));
			dataBranch.setValue(FlightDataType.TYPE_NATURAL_FREQUENCY,
					computeNaturalFrequency(status, correctiveMomentCoefficient));

			if (null != forces) {
				dataBranch.setValue(FlightDataType.TYPE_DRAG_COEFF, forces.getCD());
				dataBranch.setValue(FlightDataType.TYPE_AXIAL_DRAG_COEFF, forces.getCDaxial());
				dataBranch.setValue(FlightDataType.TYPE_FRICTION_DRAG_COEFF, forces.getFrictionCD());
				dataBranch.setValue(FlightDataType.TYPE_PRESSURE_DRAG_COEFF, forces.getPressureCD());
				dataBranch.setValue(FlightDataType.TYPE_BASE_DRAG_COEFF, forces.getBaseCD());
			}

			if (status.isLaunchRodCleared() && null != forces) {
				if (null != forces.getCP()) {
					dataBranch.setValue(FlightDataType.TYPE_CP_LOCATION, forces.getCP().getX());
					dataBranch.setValue(FlightDataType.TYPE_CNA, forces.getCP().getWeight());
				}
				dataBranch.setValue(FlightDataType.TYPE_NORMAL_FORCE_COEFF, forces.getCN());
				dataBranch.setValue(FlightDataType.TYPE_SIDE_FORCE_COEFF, forces.getCside());
				dataBranch.setValue(FlightDataType.TYPE_ROLL_MOMENT_COEFF, forces.getCroll());
				dataBranch.setValue(FlightDataType.TYPE_ROLL_FORCING_COEFF, forces.getCrollForce());
				dataBranch.setValue(FlightDataType.TYPE_ROLL_DAMPING_COEFF, forces.getCrollDamp());
				dataBranch.setValue(FlightDataType.TYPE_PITCH_DAMPING_MOMENT_COEFF, forces.getPitchDampingMoment());

				if (null != rocketMass && null != flightConditions) {
					if (null != forces.getCP()) {
						dataBranch.setValue(FlightDataType.TYPE_STABILITY,
								(forces.getCP().getX() - rocketMass.getCM().getX()) / flightConditions.getRefLength());
					}
					dataBranch.setValue(FlightDataType.TYPE_PITCH_MOMENT_COEFF,
							forces.getCm() - forces.getCN() * rocketMass.getCM().getX() / flightConditions.getRefLength());
					dataBranch.setValue(FlightDataType.TYPE_YAW_MOMENT_COEFF,
							forces.getCyaw() - forces.getCside() * rocketMass.getCM().getX() / flightConditions.getRefLength());
				}
			}
		}

		/**
		 * Calculate the damping moment coefficient (Cdm).
		 * Note: despite the name, this quantity has units of angular momentum (e.g. N·m·s / kg·m²/s),
		 * and is intended for post-processing/analysis (mirrors the former example sim extension).
		 * See peak of flight issue 195, October 23, 2007 for more information about the calculation.
		 * <p>
		 * The aerodynamic and propulsive contributions are also stored separately as
		 * {@code FlightDataType.TYPE_DAMPING_MOMENT_COEFF_AERODYNAMIC} and
		 * {@code FlightDataType.TYPE_DAMPING_MOMENT_COEFF_PROPULSIVE}.
		 * <p>
		 * This is computed from:
		 * - a propulsive/jet damping part based on the time-derivative of motor mass, and
		 * - an aerodynamic part based on component-wise force analysis (CNa and Cp distance to CG).
		 *
		 * @param status     the simulation status
		 * @param dataBranch the flight data branch
		 * @return the damping moment coefficient, or NaN if it cannot be computed
		 */
		private DampingMomentComponents computeDampingMomentCoefficientComponents(SimulationStatus status,
																				  FlightDataBranch dataBranch) {
			if (flightConditions == null || rocketMass == null) {
				return DampingMomentComponents.nan();
			}

			if (!status.isLaunchRodCleared()) {
				return new DampingMomentComponents(0.0, 0.0, 0.0);
			}

			// Keep ground/landed values consistent with other aero-derived quantities that are not computed.
			// (When the rocket is on the ground, FlightConditions.AOA is set to NaN by landedValues().)
			if (Double.isNaN(flightConditions.getAOA())) {
				return DampingMomentComponents.nan();
			}

			double aerodynamic = computeAerodynamicDampingMomentCoefficient(status, dataBranch);
			double propulsive = computePropulsiveDampingMomentCoefficient(status, dataBranch);
			return new DampingMomentComponents(aerodynamic + propulsive, aerodynamic, propulsive);
		}

		/**
		 * Compute the aerodynamic part of the damping moment coefficient.
		 *
		 * @param status     the simulation status
		 * @param dataBranch the flight data branch
		 * @return the aerodynamic part of the damping moment coefficient
		 */
		private double computeAerodynamicDampingMomentCoefficient(SimulationStatus status, FlightDataBranch dataBranch) {
			double aerodynamicPart = 0;
			double cg = rocketMass.getCM().getX();
			AerodynamicCalculator aerocalc = status.getSimulationConditions().getAerodynamicCalculator();
			Map<RocketComponent, AerodynamicForces> forceAnalysis = aerocalc.getForceAnalysis(status.getConfiguration(),
					flightConditions, null);
			for (Map.Entry<RocketComponent, AerodynamicForces> entry : forceAnalysis.entrySet()) {
				RocketComponent comp = entry.getKey();
				if (comp == null || !comp.isAerodynamic()) {
					continue;
				}
				AerodynamicForces componentForces = entry.getValue();
				if (componentForces == null || componentForces.getCP() == null) {
					continue;
				}

				double cna = componentForces.getCP().getWeight();    // TODO: replace with getCNa() when available
				double z = componentForces.getCP().getX();            // Distance from rocket tip to component CP
				aerodynamicPart += cna * MathUtil.pow2(z - cg);
			}

			double v = flightConditions.getVelocity();
			double rho = flightConditions.getAtmosphericConditions().getDensity();
			double ar = flightConditions.getRefArea();
			aerodynamicPart = 0.5 * rho * v * ar * aerodynamicPart;

			return aerodynamicPart;
		}

		/**
		 * Compute the propulsive/jet part of the damping moment coefficient.
		 *
		 * @param status     the simulation status
		 * @param dataBranch the flight data branch
		 * @return the propulsive part of the damping moment coefficient
		 */
		private double computePropulsiveDampingMomentCoefficient(SimulationStatus status, FlightDataBranch dataBranch) {
			// mdot := d(motor mass)/dt, estimated using the last two stored points.
			// Avoids reaching into motor internals.
			double mdot = computeMotorMassDerivative(dataBranch);
			if (Double.isNaN(mdot)) {
				return 0;
			}

			double cg = rocketMass.getCM().getX();

			// Find the furthest-aft nozzle location in the current configuration, measured from the rocket tip.
			double nozzleDistance = 0;
			FlightConfiguration config = status.getConfiguration();
			for (MotorConfiguration inst : config.getActiveMotors()) {
				double x = inst.getX() + inst.getMotor().getLaunchCGx();
				if (x > nozzleDistance) {
					nozzleDistance = x;
				}
			}

			return mdot * MathUtil.pow2(nozzleDistance - cg);
		}

		/**
		 * Estimate the motor mass time-derivative using the last two points in the data branch.
		 * Returns NaN when insufficient history exists (e.g. at t=0), or when dt is invalid.
		 *
		 * @param dataBranch the flight data branch
		 * @return the motor mass time-derivative, or NaN if it cannot be computed
		 */
		private static double computeMotorMassDerivative(FlightDataBranch dataBranch) {
			List<Double> motorMass = dataBranch.get(FlightDataType.TYPE_MOTOR_MASS);
			List<Double> time = dataBranch.get(FlightDataType.TYPE_TIME);
			if (motorMass == null || time == null) {
				return Double.NaN;
			}

			// Check that we have enough samples
			int n = Math.min(motorMass.size(), time.size());
			if (n < 2) {
				return Double.NaN;
			}

			double dt = time.get(n - 1) - time.get(n - 2);
			if (!(dt > 0)) {
				return Double.NaN;
			}

			// This isn't as accurate as I would like
			// Using polynomial interpolator for derivative. Doesn't help much
			// double[] x = { time.get(len-5), time.get(len-4), time.get(len-3),
			// time.get(len-2), time.get(len-1) };
			// double[] y = { mpAll.get(len-5), mpAll.get(len-4), mpAll.get(len-3),
			// mpAll.get(len-2), mpAll.get(len-1) };
			// PolyInterpolator interp = new PolyInterpolator(x);
			// double[] coeff = interp.interpolator(y);
			// double dt = .01;
			// mdot = (interp.eval(x[4], coeff) - interp.eval(x[4]-dt, coeff))/dt;
			return (motorMass.get(n - 1) - motorMass.get(n - 2)) / dt;    // Note: peak of flight mentions gram/s, but we use kg/s
		}

		/**
		 * Calculate the corrective moment coefficient (Ccm).
		 * <p>
		 * Despite the name, this quantity has units of torque (N·m).
		 * See peak of flight issue 193, September 25, 2007 for more information about the calculation.
		 *
		 * @param status the simulation status
		 * @return Ccm, or NaN if it cannot be computed (e.g. on the ground/rail)
		 */
		private double computeCorrectiveMomentCoefficient(SimulationStatus status) {
			if (flightConditions == null || rocketMass == null || forces == null || forces.getCP() == null) {
				return Double.NaN;
			}

			if (!status.isLaunchRodCleared()) {
				return 0.0;
			}

			if (Double.isNaN(flightConditions.getAOA())) {
				return Double.NaN;
			}

			double rho = flightConditions.getAtmosphericConditions().getDensity();
			double v = flightConditions.getVelocity();
			double ar = flightConditions.getRefArea();

			double cna = forces.getCP().getWeight(); // TODO: replace with getCNa() when available
			double cp = forces.getCP().getX();
			double cg = rocketMass.getCM().getX();

			return 0.5 * rho * MathUtil.pow2(v) * ar * cna * (cp - cg);
		}

		/**
		 * Calculate the damping ratio (zeta), a dimensionless measure of stability.
		 * See peak of flight issue 197, November 20, 2007 for more information about the calculation.
		 * @param status the simulation status
		 * @param dampingMomentCoefficient    Cdm
		 * @param correctiveMomentCoefficient Ccm
		 * @return zeta, or NaN if it cannot be computed (e.g. missing inputs, unstable lever arm, zero inertia)
		 */
		private double computeDampingRatio(SimulationStatus status, double dampingMomentCoefficient, double correctiveMomentCoefficient) {
			if (rocketMass == null) {
				return Double.NaN;
			}

			if (!status.isLaunchRodCleared()) {
				return 0.0;
			}

			if (Double.isNaN(dampingMomentCoefficient) || Double.isNaN(correctiveMomentCoefficient)) {
				return Double.NaN;
			}

			double longitudinalInertia = rocketMass.getLongitudinalInertia();
			if (longitudinalInertia <= 0) {
				return Double.NaN;
			}

			// For a stable rocket Ccm > 0 (CP behind CG).  If the product is non-positive, zeta is undefined.
			double product = correctiveMomentCoefficient * longitudinalInertia;
			if (Double.isNaN(product) || product <= 0) {
				return Double.NaN;
			}

			double denominator = 2.0 * Math.sqrt(product);
			if (Double.isNaN(denominator) || denominator <= 0) {
				return Double.NaN;
			}

				return dampingMomentCoefficient / denominator;
			}

			/**
			 * Calculate the natural frequency (wn), i.e. how fast the rocket oscillates.
			 * See peak of flight issue 196, November 06, 2007 for more information about the calculation.
			 * @param correctiveMomentCoefficient Ccm
			 * @return wn (rad/s), or NaN if it cannot be computed
			 */
			private double computeNaturalFrequency(SimulationStatus status, double correctiveMomentCoefficient) {
				if (!status.isLaunchRodCleared()) {
					return 0.0;
				}

				if (rocketMass == null || Double.isNaN(correctiveMomentCoefficient)) {
					return Double.NaN;
				}

				double longitudinalInertia = rocketMass.getLongitudinalInertia();
				if (longitudinalInertia <= 0) {
					return Double.NaN;
				}

				double ratio = correctiveMomentCoefficient / longitudinalInertia;
				if (Double.isNaN(ratio) || ratio < 0) {
					return Double.NaN;
				}

				return Math.sqrt(ratio);
			}

		/**
		 * Calculate the wind direction in the horizontal (X-Y) plane
		 *
		 * @param windVector The wind vector as a Coordinate object
		 * @return The angle in radians, where 0 is North, Pi/2 is East, etc.
		 */
		private static double getWindDirection(CoordinateIF windVector) {
			// Math.atan2(y, x) returns the angle in radians measured counterclockwise from the positive x-axis
			// But we want the angle clockwise from North (positive y-axis)
			double angle = Math.atan2(windVector.getX(), windVector.getY());
			return MathUtil.reduce2Pi(angle);
		}

		/**
		 * Damping moment coefficient components.
		 *
		 * @param total       the total damping moment coefficient
		 * @param aerodynamic the aerodynamic component
		 * @param propulsive  the propulsive component
		 */
		private record DampingMomentComponents(double total, double aerodynamic, double propulsive) {
			private static final DampingMomentComponents NAN = new DampingMomentComponents(Double.NaN, Double.NaN, Double.NaN);

			private static DampingMomentComponents nan() {
				return NAN;
			}
		}
	}
}
