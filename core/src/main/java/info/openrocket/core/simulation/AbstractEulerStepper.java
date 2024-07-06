package info.openrocket.core.simulation;

import info.openrocket.core.logging.SimulationAbort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.aerodynamics.AerodynamicForces;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.masscalc.RigidBody;
import info.openrocket.core.models.atmosphere.AtmosphericConditions;
import info.openrocket.core.rocketcomponent.InstanceMap;
import info.openrocket.core.rocketcomponent.RecoveryDevice;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.GeodeticComputationStrategy;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.WorldCoordinate;

public abstract class AbstractEulerStepper extends AbstractSimulationStepper {
	private static final Logger log = LoggerFactory.getLogger(AbstractEulerStepper.class);
	private static final Translator trans = Application.getTranslator();

	private static final double RECOVERY_TIME_STEP = 0.5;

	DataStore store = new DataStore();
	
	@Override
	public SimulationStatus initialize(SimulationStatus status) {
		store.thrustForce = 0;
		
		// note most of our forces don't end up getting set, so they're all NaN.
		AerodynamicForces forces = new AerodynamicForces();

		double cd = computeCD(status);
		forces.setCD(cd);
		forces.setCDaxial(cd);
		forces.setFrictionCD(0);
		forces.setPressureCD(cd);
		forces.setBaseCD(0);
		
		store.forces = forces;

		return status;
	}

	protected abstract double computeCD(SimulationStatus status);
	
	@Override
	public void step(SimulationStatus status, double maxTimeStep) throws SimulationException {
		
		// Get the atmospheric conditions
		store.atmosphericConditions = modelAtmosphericConditions(status);
		
		//// Local wind speed and direction
		store.windVelocity = modelWindVelocity(status);
		Coordinate airSpeed = status.getRocketVelocity().add(store.windVelocity);
		
		// Compute drag force
		final double mach = airSpeed.length() / store.atmosphericConditions.getMachSpeed();
		final double CdA = store.forces.getCD() * status.getConfiguration().getReferenceArea();
		store.dragForce = 0.5 * CdA * store.atmosphericConditions.getDensity() * airSpeed.length2();

		RigidBody structureMassData = calculateStructureMass(status);
		store.motorMass = calculateMotorMass(status);
		store.rocketMass = structureMassData.add( store.motorMass );

		if (store.rocketMass.getMass() < MathUtil.EPSILON) {
			status.abortSimulation(SimulationAbort.Cause.ACTIVE_MASS_ZERO);
		}

		// Compute drag acceleration
		store.linearAcceleration = airSpeed.normalize().multiply(-store.dragForce / store.rocketMass.getMass());
		
		// Add effect of gravity
		store.gravity = modelGravity(status);
		store.linearAcceleration = store.linearAcceleration.sub(0, 0, store.gravity);

		// Add coriolis acceleration
		store.coriolisAcceleration = status.getSimulationConditions().getGeodeticComputation().getCoriolisAcceleration(
				status.getRocketWorldPosition(), status.getRocketVelocity());
		store.linearAcceleration = store.linearAcceleration.add(store.coriolisAcceleration);

		// Select tentative time step
		store.timeStep = RECOVERY_TIME_STEP;

		// adjust based on acceleration
		final double absAccel = store.linearAcceleration.length();
		if (absAccel > MathUtil.EPSILON) {
			store.timeStep = Math.min(store.timeStep, 1.0/absAccel);
		}

		// Honor max step size passed in.  If the time to next time step is greater than our minimum
		// we'll set our next step to just before it in order to better capture discontinuities in things like chute opening
		if (maxTimeStep < store.timeStep) {
			if (maxTimeStep > MIN_TIME_STEP) {
				store.timeStep = maxTimeStep - MIN_TIME_STEP;
			} else {
				store.timeStep = maxTimeStep;
			}
		}

		// but don't let it get *too* small
		store.timeStep = Math.max(store.timeStep, MIN_TIME_STEP);
		log.trace("timeStep is " + store.timeStep);
		
		// Perform Euler integration
		EulerValues newVals = eulerIntegrate(status.getRocketPosition(), status.getRocketVelocity(), store.linearAcceleration, store.timeStep);

		// Check to see if z or either of its first two derivatives have changed sign and recalculate
		// time step to point of change if so
		// z   -- ground hit
		// z'  -- apogee
		// z'' -- possible oscillation building up in descent rate
		// Note that it's virtually impossible for apogee to occur on the same
		// step as either ground hit or descent rate inflection, and if we get a ground hit
		// any descent rate inflection won't matter
		final double a = store.linearAcceleration.z;
		final double v = status.getRocketVelocity().z;
		final double z = status.getRocketPosition().z;
		double t = store.timeStep;
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
			final double dFdV = CdA * store.atmosphericConditions.getDensity() * airSpeed.length();
			final Coordinate dAdV = airSpeed.normalize().multiply(dFdV / store.rocketMass.getMass());
			final Coordinate jerk = store.linearAcceleration.multiply(dAdV);
			final Coordinate newAcceleration = store.linearAcceleration.add(jerk.multiply(store.timeStep));

			// Only do this one if acceleration is appreciably different from 0
			if (newAcceleration.z * store.linearAcceleration.z < -MathUtil.EPSILON) {
				// If acceleration oscillation is building up, the new timestep is the solution of
				// a + j*t = 0
				t = Math.abs(a / jerk.z);
				log.trace("oscillation avoidance changes timeStep to " + t);
			}
		}

		// once again, make sure new timestep isn't *too* small
		t = Math.max(t, MIN_TIME_STEP);

		// recalculate Euler integration for position and velocity if necessary.
		if (Math.abs(t - store.timeStep) > MathUtil.EPSILON) {
			store.timeStep = t;

			if (maxTimeStep - store.timeStep < MIN_TIME_STEP) {
				store.timeStep = maxTimeStep;
			}

			newVals = eulerIntegrate(status.getRocketPosition(), status.getRocketVelocity(), store.linearAcceleration, store.timeStep);

			// If we just landed chop off rounding error
			if (Math.abs(newVals.pos.z) < MathUtil.EPSILON) {
				newVals.pos = newVals.pos.setZ(0);
			}
		}

		status.setSimulationTime(status.getSimulationTime() + store.timeStep);

		status.setRocketPosition(newVals.pos);
		status.setRocketVelocity(newVals.vel);
		status.setRocketAcceleration(store.linearAcceleration);

		// Update the world coordinate
		WorldCoordinate w = status.getSimulationConditions().getLaunchSite();
		w = status.getSimulationConditions().getGeodeticComputation().addCoordinate(w, status.getRocketPosition());
		status.setRocketWorldPosition(w);

		// Store data
		final FlightDataBranch dataBranch = status.getFlightDataBranch();

		// Values looked up or calculated at start of time step
		store.storeData(status);
		
		// Values calculated on this step
		dataBranch.addPoint();
		status.storeData();

		airSpeed = status.getRocketVelocity().add(store.windVelocity);
		final double Re = airSpeed.length() *
			status.getConfiguration().getLengthAerodynamic() /
			store.atmosphericConditions.getKinematicViscosity();
		dataBranch.setValue(FlightDataType.TYPE_REYNOLDS_NUMBER, Re);

		log.trace("time " + dataBranch.getLast(FlightDataType.TYPE_TIME) + ", altitude " + dataBranch.getLast(FlightDataType.TYPE_ALTITUDE) + ", velocity " + dataBranch.getLast(FlightDataType.TYPE_VELOCITY_Z));
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
