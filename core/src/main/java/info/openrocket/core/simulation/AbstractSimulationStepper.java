package info.openrocket.core.simulation;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.aerodynamics.AerodynamicForces;
import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.masscalc.MassCalculator;
import info.openrocket.core.masscalc.RigidBody;
import info.openrocket.core.models.atmosphere.AtmosphericConditions;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.listeners.SimulationListenerHelper;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.GeodeticComputationStrategy;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.Quaternion;
import info.openrocket.core.util.Rotation2D;

public abstract class AbstractSimulationStepper implements SimulationStepper {
	private static final Logger log = LoggerFactory.getLogger(AbstractSimulationStepper.class);

	protected static final double MIN_TIME_STEP = 0.001;
	
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

	/**
	 * Compute the atmospheric conditions, allowing listeners to override.
	 * 
	 * @param status	the simulation status
	 * @return			the atmospheric conditions to use
	 * @throws SimulationException	if a listener throws SimulationException
	 */
	protected AtmosphericConditions modelAtmosphericConditions(SimulationStatus status) throws SimulationException {
		AtmosphericConditions conditions;

		// Call pre-listener
		conditions = SimulationListenerHelper.firePreAtmosphericModel(status);
		if (conditions != null) {
			return conditions;
		}

		// Compute conditions
		double altitude = status.getRocketPosition().z + status.getSimulationConditions().getLaunchSite().getAltitude();
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
	 * @param status	the simulation status
	 * @return			the wind conditions to use
	 * @throws SimulationException	if a listener throws SimulationException
	 */
	protected Coordinate modelWindVelocity(SimulationStatus status) throws SimulationException {
		Coordinate wind;

		// Call pre-listener
		wind = SimulationListenerHelper.firePreWindModel(status);
		if (wind != null) {
			return wind;
		}

		// Compute conditions
		double altitudeAGL = status.getRocketPosition().z;
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
	 * @param status	the simulation status
	 * @return			the gravitational acceleration to use
	 * @throws SimulationException	if a listener throws SimulationException
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
	 * @param status	the simulation status
	 * @return			the mass data to use
	 * @throws SimulationException	if a listener throws SimulationException
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
	 * @param d					the double value to check.
	 * @throws BugException		if the value is NaN.
	 */
	protected void checkNaN(double d, String var) {
		if (Double.isNaN(d)) {
			throw new BugException("Simulation resulted in not-a-number (NaN) value for " + var + ", please report a bug.");
		}
	}
	
	/**
	 * Check that the provided coordinate is not NaN.
	 * 
	 * @param c					the coordinate value to check.
	 * @throws BugException		if the value is NaN.
	 */
	protected void checkNaN(Coordinate c, String var) {
		if (c.isNaN()) {
			throw new BugException("Simulation resulted in not-a-number (NaN) value for " + var + ", please report a bug, c=" + c);
		}
	}
	
	
	/**
	 * Check that the provided quaternion is not NaN.
	 * 
	 * @param q					the quaternion value to check.
	 * @throws BugException		if the value is NaN.
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
		store.rocketMass = structureMassData.add( store.motorMass );
		store.gravity = modelGravity(status);
		store.thrustForce = 0.0;
		store.dragForce = 0.0;
		store.coriolisAcceleration = Coordinate.ZERO;
		
		store.accelerationData = new AccelerationData(Coordinate.ZERO, Coordinate.ZERO, null, null,
													  new Quaternion());

		status.setRocketPosition(new Coordinate(status.getRocketPosition().x, status.getRocketPosition().y, 0));
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
		
		public Coordinate coriolisAcceleration;

		public Coordinate launchRodDirection = null;
		
		// set by calculateFlightConditions and calculateAcceleration:
		public AerodynamicForces forces;
		public Coordinate windVelocity = new Coordinate(Double.NaN, Double.NaN, Double.NaN);
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
									MathUtil.hypot(accelerationData.getLinearAccelerationWC().x, accelerationData.getLinearAccelerationWC().y));
				
				dataBranch.setValue(FlightDataType.TYPE_ACCELERATION_TOTAL, accelerationData.getLinearAccelerationWC().length());
				dataBranch.setValue(FlightDataType.TYPE_ACCELERATION_Z, accelerationData.getLinearAccelerationWC().z);
			}
			
			if (null != rocketMass) {
				double weight = rocketMass.getMass() * gravity;
				dataBranch.setValue(FlightDataType.TYPE_THRUST_WEIGHT_RATIO, thrustForce / weight);
				dataBranch.setValue(FlightDataType.TYPE_CG_LOCATION, rocketMass.getCM().x);
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
			
			if (null != forces) {
				dataBranch.setValue(FlightDataType.TYPE_DRAG_COEFF, forces.getCD());
				dataBranch.setValue(FlightDataType.TYPE_AXIAL_DRAG_COEFF, forces.getCDaxial());
				dataBranch.setValue(FlightDataType.TYPE_FRICTION_DRAG_COEFF, forces.getFrictionCD());
				dataBranch.setValue(FlightDataType.TYPE_PRESSURE_DRAG_COEFF, forces.getPressureCD());
				dataBranch.setValue(FlightDataType.TYPE_BASE_DRAG_COEFF, forces.getBaseCD());
			}
			
			if (status.isLaunchRodCleared() && null != forces) {
				if (null != forces.getCP()) {
					dataBranch.setValue(FlightDataType.TYPE_CP_LOCATION, forces.getCP().x);
				}
				dataBranch.setValue(FlightDataType.TYPE_NORMAL_FORCE_COEFF, forces.getCN());
				dataBranch.setValue(FlightDataType.TYPE_SIDE_FORCE_COEFF, forces.getCside());
				dataBranch.setValue(FlightDataType.TYPE_ROLL_MOMENT_COEFF, forces.getCroll());
				dataBranch.setValue(FlightDataType.TYPE_ROLL_FORCING_COEFF, forces.getCrollForce());
				dataBranch.setValue(FlightDataType.TYPE_ROLL_DAMPING_COEFF, forces.getCrollDamp());
				dataBranch.setValue(FlightDataType.TYPE_PITCH_DAMPING_MOMENT_COEFF,	forces.getPitchDampingMoment());
				
				if (null != rocketMass && null != flightConditions) {
					if (null != forces.getCP()) {
						dataBranch.setValue(FlightDataType.TYPE_STABILITY,
											(forces.getCP().x - rocketMass.getCM().x) / flightConditions.getRefLength());
					}
					dataBranch.setValue(FlightDataType.TYPE_PITCH_MOMENT_COEFF,
										forces.getCm() - forces.getCN() * rocketMass.getCM().x / flightConditions.getRefLength());
					dataBranch.setValue(FlightDataType.TYPE_YAW_MOMENT_COEFF,
										forces.getCyaw() - forces.getCside() * rocketMass.getCM().x / flightConditions.getRefLength());
				}
			}
		}

		/**
		 * Calculate the wind direction in the horizontal (X-Y) plane
		 * @param windVector The wind vector as a Coordinate object
		 * @return The angle in radians, where 0 is North, Pi/2 is East, etc.
		 */
		private static double getWindDirection(Coordinate windVector) {
			// Math.atan2(y, x) returns the angle in radians measured counterclockwise from the positive x-axis
			// But we want the angle clockwise from North (positive y-axis)
			double angle = Math.atan2(windVector.x, windVector.y);
			return MathUtil.reduce2Pi(angle);
		}
	}
}
