package info.openrocket.core.simulation;

import java.util.Collection;

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

	protected static final double MIN_TIME_STEP = 0.001;

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

		checkNaN(conditions.getPressure());
		checkNaN(conditions.getTemperature());

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
		double altitude = status.getRocketPosition().z + status.getSimulationConditions().getLaunchSite().getAltitude();
		wind = status.getSimulationConditions().getWindModel().getWindVelocity(status.getSimulationTime(), altitude);

		// Call post-listener
		wind = SimulationListenerHelper.firePostWindModel(status, wind);

		checkNaN(wind);

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

		checkNaN(gravity);

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

		checkNaN(structureMass.getCenterOfMass());
		checkNaN(structureMass.getLongitudinalInertia());
		checkNaN(structureMass.getRotationalInertia());

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

		checkNaN(motorMass.getCenterOfMass());
		checkNaN(motorMass.getLongitudinalInertia());
		checkNaN(motorMass.getRotationalInertia());

		return motorMass;
	}

	/**
	 * Calculate the average thrust produced by the motors in the current configuration, allowing
	 * listeners to override.  The average is taken between <code>status.time</code> and 
	 * <code>status.time + timestep</code>.
	 * <p>
	 * TODO: HIGH:  This method does not take into account any moments generated by off-center motors.
	 *  
	 * @param status					the current simulation status.
	 * @param timestep					the time step of the current iteration.
	 * @param acceleration				the current (approximate) acceleration
	 * @param atmosphericConditions		the current atmospheric conditions
	 * @param stepMotors				whether to step the motors forward or work on a clone object
	 * @return							the average thrust during the time step.
	 */
	protected double calculateThrust(SimulationStatus status,
			double acceleration, AtmosphericConditions atmosphericConditions,
			boolean stepMotors) throws SimulationException {
		double thrust;

		// Pre-listeners
		thrust = SimulationListenerHelper.firePreThrustCalculation(status);
		if (!Double.isNaN(thrust)) {
			return thrust;
		}

		thrust = 0;
		Collection<MotorClusterState> activeMotorList = status.getActiveMotors();
		for (MotorClusterState currentMotorState : activeMotorList ) {
			thrust += currentMotorState.getThrust( status.getSimulationTime() );
		}

		// Post-listeners
		thrust = SimulationListenerHelper.firePostThrustCalculation(status, thrust);

		checkNaN(thrust);

		return thrust;
	}

	/**
	 * Check that the provided value is not NaN.
	 * 
	 * @param d					the double value to check.
	 * @throws BugException		if the value is NaN.
	 */
	protected void checkNaN(double d) {
		if (Double.isNaN(d)) {
			throw new BugException("Simulation resulted in not-a-number (NaN) value, please report a bug.");
		}
	}
	
	/**
	 * Check that the provided coordinate is not NaN.
	 * 
	 * @param c					the coordinate value to check.
	 * @throws BugException		if the value is NaN.
	 */
	protected void checkNaN(Coordinate c) {
		if (c.isNaN()) {
			throw new BugException("Simulation resulted in not-a-number (NaN) value, please report a bug, c=" + c);
		}
	}
	
	
	/**
	 * Check that the provided quaternion is not NaN.
	 * 
	 * @param q					the quaternion value to check.
	 * @throws BugException		if the value is NaN.
	 */
	protected void checkNaN(Quaternion q) {
		if (q.isNaN()) {
			throw new BugException("Simulation resulted in not-a-number (NaN) value, please report a bug, q=" + q);
		}
	}

	protected static class DataStore {
	
		public double timeStep = Double.NaN;
		
		public AccelerationData accelerationData;
		
		public AtmosphericConditions atmosphericConditions;
		
		public FlightConditions flightConditions;
		
		public double longitudinalAcceleration = Double.NaN;
		
		public RigidBody rocketMass;
		
		public RigidBody motorMass;
		
		public Coordinate coriolisAcceleration;
		
		public Coordinate linearAcceleration;
		public Coordinate angularAcceleration;

		public Coordinate launchRodDirection = null;
		
		public double maxZvelocity = Double.NaN;
		public double startWarningTime = Double.NaN;
		
		// set by calculateFlightConditions and calculateAcceleration:
		public AerodynamicForces forces;
		public Coordinate windVelocity = new Coordinate(Double.NaN, Double.NaN, Double.NaN);
		public double gravity = Double.NaN;
		public double thrustForce = Double.NaN;
		public double dragForce = Double.NaN;
		public double lateralPitchRate = Double.NaN;
		
		public double rollAcceleration = Double.NaN;
		public double lateralPitchAcceleration = Double.NaN;
		
		public Rotation2D thetaRotation;

		void storeData(SimulationStatus status) {
		
			FlightDataBranch dataBranch = status.getFlightDataBranch();

			dataBranch.setValue(FlightDataType.TYPE_THRUST_FORCE, thrustForce);
			dataBranch.setValue(FlightDataType.TYPE_GRAVITY, gravity);
			double weight = rocketMass.getMass() * gravity;
			dataBranch.setValue(FlightDataType.TYPE_THRUST_WEIGHT_RATIO, thrustForce / weight);
			dataBranch.setValue(FlightDataType.TYPE_DRAG_FORCE, dragForce);
		
			dataBranch.setValue(FlightDataType.TYPE_WIND_VELOCITY, windVelocity.length());
			dataBranch.setValue(FlightDataType.TYPE_TIME_STEP, timeStep);
			
			if (GeodeticComputationStrategy.FLAT != status.getSimulationConditions().getGeodeticComputation()) {
				dataBranch.setValue(FlightDataType.TYPE_CORIOLIS_ACCELERATION, coriolisAcceleration.length());
			}
			
			if (null != linearAcceleration) {
				dataBranch.setValue(FlightDataType.TYPE_ACCELERATION_XY,
									MathUtil.hypot(linearAcceleration.x, linearAcceleration.y));
				
				dataBranch.setValue(FlightDataType.TYPE_ACCELERATION_TOTAL, linearAcceleration.length());
				dataBranch.setValue(FlightDataType.TYPE_ACCELERATION_Z, linearAcceleration.z);
			}
			
			if (null != rocketMass) {
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
					dataBranch.setValue(FlightDataType.TYPE_STABILITY,
										(forces.getCP().x - rocketMass.getCM().x) / flightConditions.getRefLength());
					dataBranch.setValue(FlightDataType.TYPE_PITCH_MOMENT_COEFF,
										forces.getCm() - forces.getCN() * rocketMass.getCM().x / flightConditions.getRefLength());
					dataBranch.setValue(FlightDataType.TYPE_YAW_MOMENT_COEFF,
										forces.getCyaw() - forces.getCside() * rocketMass.getCM().x / flightConditions.getRefLength());
				}
			}
		}
	}		
}
