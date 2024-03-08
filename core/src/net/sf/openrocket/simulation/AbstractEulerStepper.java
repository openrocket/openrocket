package net.sf.openrocket.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.SimulationAbort;
import net.sf.openrocket.models.atmosphere.AtmosphericConditions;
import net.sf.openrocket.rocketcomponent.InstanceMap;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.GeodeticComputationStrategy;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.WorldCoordinate;

public abstract class AbstractEulerStepper extends AbstractSimulationStepper {
	private static final Logger log = LoggerFactory.getLogger(AbstractEulerStepper.class);
	private static final Translator trans = Application.getTranslator();
	
	private static final double RECOVERY_TIME_STEP = 0.5;

	protected double cd;
	
	@Override
	public SimulationStatus initialize(SimulationStatus status) {
		this.cd = computeCD(status);
		return status;
	}

	private double getCD() {
		return cd;
	}

	protected abstract double computeCD(SimulationStatus status);
	
	@Override
	public void step(SimulationStatus status, double maxTimeStep) throws SimulationException {
		
		// Get the atmospheric conditions
		final AtmosphericConditions atmosphere = modelAtmosphericConditions(status);
		
		//// Local wind speed and direction
		final Coordinate windSpeed = modelWindVelocity(status);
		Coordinate airSpeed = status.getRocketVelocity().add(windSpeed);
		
		// Compute drag force
		final double mach = airSpeed.length() / atmosphere.getMachSpeed();
		final double CdA = getCD() * status.getConfiguration().getReferenceArea();
		final double dragForce = 0.5 * CdA * atmosphere.getDensity() * airSpeed.length2();

		final double rocketMass = calculateStructureMass(status).getMass();
		final double motorMass = calculateMotorMass(status).getMass();
		
		final double mass = rocketMass + motorMass;
		if (mass < MathUtil.EPSILON) {
			status.abortSimulation(SimulationAbort.Cause.ACTIVE_MASS_ZERO);
		}
		
		// Compute drag acceleration
		Coordinate linearAcceleration = airSpeed.normalize().multiply(-dragForce / mass);
		
		// Add effect of gravity
		final double gravity = modelGravity(status);
		linearAcceleration = linearAcceleration.sub(0, 0, gravity);
		

		// Add coriolis acceleration
		final Coordinate coriolisAcceleration = status.getSimulationConditions().getGeodeticComputation().getCoriolisAcceleration(
				status.getRocketWorldPosition(), status.getRocketVelocity());
		linearAcceleration = linearAcceleration.add(coriolisAcceleration);

		// Select tentative time step
		double timeStep = RECOVERY_TIME_STEP;

		// adjust based on acceleration
		final double absAccel = linearAcceleration.length();
		if (absAccel > MathUtil.EPSILON) {
			timeStep = Math.min(timeStep, 1.0/absAccel);
		}

		// Honor max step size passed in.  If the time to next time step is greater than our minimum
		// we'll set our next step to just before it in order to better capture discontinuities in things like chute opening
		if (maxTimeStep < timeStep) {
			if (maxTimeStep > MIN_TIME_STEP) {
				timeStep = maxTimeStep - MIN_TIME_STEP;
			} else {
				timeStep = maxTimeStep;
			}
		}

		// but don't let it get *too* small
		timeStep = Math.max(timeStep, MIN_TIME_STEP);
		log.trace("timeStep is " + timeStep);
		
		// Perform Euler integration
		EulerValues newVals = eulerIntegrate(status.getRocketPosition(), status.getRocketVelocity(), linearAcceleration, timeStep);

		// Check to see if z or either of its first two derivatives have changed sign and recalculate
		// time step to point of change if so
		// z   -- ground hit
		// z'  -- apogee
		// z'' -- possible oscillation building up in descent rate
		// Note that it's virtually impossible for apogee to occur on the same
		// step as either ground hit or descent rate inflection, and if we get a ground hit
		// any descent rate inflection won't matter
		final double a = linearAcceleration.z;
		final double v = status.getRocketVelocity().z;
		final double z = status.getRocketPosition().z;
		double t = timeStep;
		if (newVals.pos.z < 0) {
			// If I've hit the ground, the new timestep is the solution of
			// 1/2 at^2 + vt + z = 0
			t = (-v - Math.sqrt(v*v - 2*a*z))/a;
			log.trace("ground hit changes timeStep to " + t);
		} else if (v * newVals.vel.z < 0) {
			// If I've got apogee, the new timestep is the solution of
			// v + at = 0
			t = Math.abs(v / a);
			log.trace("apogee changes timeStep to " + t);
		} else {
			// Use jerk to estimate accleration at end of time step.  Don't really need to redo all the atmospheric
			// calculations to get it "right"; this will be close enough for our purposes.
			// use chain rule to compute jerk
			// dA/dT = dA/dV * dV/dT
			final double dFdV = CdA * atmosphere.getDensity() * airSpeed.length();
			final Coordinate dAdV = airSpeed.normalize().multiply(dFdV / mass);
			final Coordinate jerk = linearAcceleration.multiply(dAdV);
			final Coordinate newAcceleration = linearAcceleration.add(jerk.multiply(timeStep));

			// Only do this one if acceleration is appreciably different from 0
			if (newAcceleration.z * linearAcceleration.z < -MathUtil.EPSILON) {
				// If acceleration oscillation is building up, the new timestep is the solution of
				// a + j*t = 0
				t = Math.abs(a / jerk.z);
				log.trace("oscillation avoidance changes timeStep to " + t);
			}
		}
		
		// once again, make sure new timestep isn't *too* small
		t = Math.max(t, MIN_TIME_STEP);

		// recalculate Euler integration for position and velocity if necessary.
		if (Math.abs(t - timeStep) > MathUtil.EPSILON) {
			timeStep = t;
			
			if (maxTimeStep - timeStep < MIN_TIME_STEP) {
				timeStep = maxTimeStep;
			}

			newVals = eulerIntegrate(status.getRocketPosition(), status.getRocketVelocity(), linearAcceleration, timeStep);

			// If we just landed chop off rounding error
			if (Math.abs(newVals.pos.z) < MathUtil.EPSILON) {
				newVals.pos = newVals.pos.setZ(0);
			}
		}

		status.setSimulationTime(status.getSimulationTime() + timeStep);

		status.setRocketPosition(newVals.pos);
		status.setRocketVelocity(newVals.vel);
		status.setRocketAcceleration(linearAcceleration);

		// Update the world coordinate
		WorldCoordinate w = status.getSimulationConditions().getLaunchSite();
		w = status.getSimulationConditions().getGeodeticComputation().addCoordinate(w, status.getRocketPosition());
		status.setRocketWorldPosition(w);

		// Store data
		final FlightDataBranch data = status.getFlightData();

		// Values looked up or calculated at start of time step
		data.setValue(FlightDataType.TYPE_REFERENCE_LENGTH, status.getConfiguration().getReferenceLength());
		data.setValue(FlightDataType.TYPE_REFERENCE_AREA, status.getConfiguration().getReferenceArea());
		data.setValue(FlightDataType.TYPE_WIND_VELOCITY, windSpeed.length());
		data.setValue(FlightDataType.TYPE_AIR_TEMPERATURE, atmosphere.getTemperature());
		data.setValue(FlightDataType.TYPE_AIR_PRESSURE, atmosphere.getPressure());
		data.setValue(FlightDataType.TYPE_SPEED_OF_SOUND, atmosphere.getMachSpeed());
		data.setValue(FlightDataType.TYPE_MACH_NUMBER, mach);
		
		if (status.getSimulationConditions().getGeodeticComputation() != GeodeticComputationStrategy.FLAT) {
			data.setValue(FlightDataType.TYPE_CORIOLIS_ACCELERATION, coriolisAcceleration.length());
		}
		data.setValue(FlightDataType.TYPE_GRAVITY, gravity);
		
		data.setValue(FlightDataType.TYPE_DRAG_COEFF, getCD());
		data.setValue(FlightDataType.TYPE_PRESSURE_DRAG_COEFF, getCD());
		data.setValue(FlightDataType.TYPE_FRICTION_DRAG_COEFF, 0);
		data.setValue(FlightDataType.TYPE_BASE_DRAG_COEFF, 0);
		data.setValue(FlightDataType.TYPE_AXIAL_DRAG_COEFF, getCD());
		data.setValue(FlightDataType.TYPE_THRUST_FORCE, 0);
		data.setValue(FlightDataType.TYPE_DRAG_FORCE, dragForce);
		
		data.setValue(FlightDataType.TYPE_MASS, mass);
		data.setValue(FlightDataType.TYPE_MOTOR_MASS, motorMass);
		data.setValue(FlightDataType.TYPE_THRUST_WEIGHT_RATIO, 0);

		data.setValue(FlightDataType.TYPE_ACCELERATION_XY,
					  MathUtil.hypot(linearAcceleration.x, linearAcceleration.y));
		data.setValue(FlightDataType.TYPE_ACCELERATION_Z, linearAcceleration.z);
		data.setValue(FlightDataType.TYPE_ACCELERATION_TOTAL, linearAcceleration.length());

		data.setValue(FlightDataType.TYPE_TIME_STEP, timeStep);

		// Values calculated on this step
		data.addPoint();
		data.setValue(FlightDataType.TYPE_TIME, status.getSimulationTime());
		data.setValue(FlightDataType.TYPE_ALTITUDE, status.getRocketPosition().z);
		data.setValue(FlightDataType.TYPE_POSITION_X, status.getRocketPosition().x);
		data.setValue(FlightDataType.TYPE_POSITION_Y, status.getRocketPosition().y);

		data.setValue(FlightDataType.TYPE_POSITION_XY,
					  MathUtil.hypot(status.getRocketPosition().x, status.getRocketPosition().y));
		data.setValue(FlightDataType.TYPE_POSITION_DIRECTION,
					  Math.atan2(status.getRocketPosition().y, status.getRocketPosition().x));
		data.setValue(FlightDataType.TYPE_LATITUDE, status.getRocketWorldPosition().getLatitudeRad());
		data.setValue(FlightDataType.TYPE_LONGITUDE, status.getRocketWorldPosition().getLongitudeRad());
		
		data.setValue(FlightDataType.TYPE_VELOCITY_XY,
					  MathUtil.hypot(status.getRocketVelocity().x, status.getRocketVelocity().y));
		data.setValue(FlightDataType.TYPE_VELOCITY_Z, status.getRocketVelocity().z);
		data.setValue(FlightDataType.TYPE_VELOCITY_TOTAL, airSpeed.length());
		
		airSpeed = status.getRocketVelocity().add(windSpeed);
		final double Re = airSpeed.length() *
			status.getConfiguration().getLengthAerodynamic() /
			atmosphere.getKinematicViscosity();
		data.setValue(FlightDataType.TYPE_REYNOLDS_NUMBER, Re);
		
		data.setValue(FlightDataType.TYPE_COMPUTATION_TIME,
				(System.nanoTime() - status.getSimulationStartWallTime()) / 1000000000.0);
		log.trace("time " + data.getLast(FlightDataType.TYPE_TIME) + ", altitude " + data.getLast(FlightDataType.TYPE_ALTITUDE) + ", velocity " + data.getLast(FlightDataType.TYPE_VELOCITY_Z));
	}

	private static class EulerValues {
		/** linear velocity */
		public Coordinate vel;
		/** position */
		public Coordinate pos;
	}

	private EulerValues eulerIntegrate (Coordinate pos, Coordinate v, Coordinate a, double timeStep) {
		EulerValues result = new EulerValues();

		result.vel = v.add(a.multiply(timeStep));
		result.pos = pos.add(v.multiply(timeStep)).add(a.multiply(MathUtil.pow2(timeStep) / 2.0));

		return result;
	}
}
