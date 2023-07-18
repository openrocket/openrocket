package net.sf.openrocket.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.l10n.Translator;
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
		AtmosphericConditions atmosphere = modelAtmosphericConditions(status);
		
		//// Local wind speed and direction
		Coordinate windSpeed = modelWindVelocity(status);
		Coordinate airSpeed = status.getRocketVelocity().add(windSpeed);
		
		// Compute drag force
		double mach = airSpeed.length() / atmosphere.getMachSpeed();
		double dynP = (0.5 * atmosphere.getDensity() * airSpeed.length2());
		double dragForce = getCD() * dynP * status.getConfiguration().getReferenceArea();

		double rocketMass = calculateStructureMass(status).getMass();
		double motorMass = calculateMotorMass(status).getMass();
		
		double mass = rocketMass + motorMass;

		if (mass < MathUtil.EPSILON) {
			throw new SimulationException(trans.get("SimulationStepper.error.totalMassZero"));
		}
		
		// Compute drag acceleration
		Coordinate linearAcceleration;
		if (airSpeed.length() > 0.001) {
			linearAcceleration = airSpeed.normalize().multiply(-dragForce / mass);
		} else {
			linearAcceleration = Coordinate.NUL;
		}
		
		// Add effect of gravity
		double gravity = modelGravity(status);
		linearAcceleration = linearAcceleration.sub(0, 0, gravity);
		

		// Add coriolis acceleration
		Coordinate coriolisAcceleration = status.getSimulationConditions().getGeodeticComputation().getCoriolisAcceleration(
				status.getRocketWorldPosition(), status.getRocketVelocity());
		linearAcceleration = linearAcceleration.add(coriolisAcceleration);

		// Select tentative time step
		double timeStep = RECOVERY_TIME_STEP;

		// adjust based on change in acceleration (ie jerk)
		final double jerk = Math.abs(linearAcceleration.sub(status.getRocketAcceleration()).multiply(1.0/status.getPreviousTimeStep()).length());
		if (jerk > MathUtil.EPSILON) {
			timeStep = Math.min(timeStep, 1.0/jerk);
		}

		// but don't let it get *too* small
		timeStep = Math.max(timeStep, MIN_TIME_STEP);
		log.trace("timeStep is " + timeStep);
		
		// Perform Euler integration
		Coordinate newPosition = status.getRocketPosition().add(status.getRocketVelocity().multiply(timeStep)).
			add(linearAcceleration.multiply(MathUtil.pow2(timeStep) / 2));

		// If I've hit the ground, recalculate time step and position
		if (newPosition.z < 0) {

			final double a = linearAcceleration.z;
			final double v = status.getRocketVelocity().z;
			final double z0 = status.getRocketPosition().z;

			// The new timestep is the solution of
			// 1/2 at^2 + vt + z0 = 0
			timeStep = (-v - Math.sqrt(v*v - 2*a*z0))/a;
			log.trace("ground hit changes timeStep to " + timeStep);
			
			newPosition = status.getRocketPosition().add(status.getRocketVelocity().multiply(timeStep)).
				add(linearAcceleration.multiply(MathUtil.pow2(timeStep) / 2));

			// avoid rounding error in new altitude
			newPosition = newPosition.setZ(0);
		}

		status.setSimulationTime(status.getSimulationTime() + timeStep);
		status.setPreviousTimeStep(timeStep);

		status.setRocketPosition(newPosition);
		status.setRocketVelocity(status.getRocketVelocity().add(linearAcceleration.multiply(timeStep)));
		status.setRocketAcceleration(linearAcceleration);

		// Update the world coordinate
		WorldCoordinate w = status.getSimulationConditions().getLaunchSite();
		w = status.getSimulationConditions().getGeodeticComputation().addCoordinate(w, status.getRocketPosition());
		status.setRocketWorldPosition(w);

		// Store data
		FlightDataBranch data = status.getFlightData();
		boolean extra = status.getSimulationConditions().isCalculateExtras();
		data.addPoint();
		
		data.setValue(FlightDataType.TYPE_TIME, status.getSimulationTime());
		data.setValue(FlightDataType.TYPE_ALTITUDE, status.getRocketPosition().z);
		data.setValue(FlightDataType.TYPE_POSITION_X, status.getRocketPosition().x);
		data.setValue(FlightDataType.TYPE_POSITION_Y, status.getRocketPosition().y);

		airSpeed = status.getRocketVelocity().add(windSpeed);
		if (extra) {
			data.setValue(FlightDataType.TYPE_POSITION_XY,
					MathUtil.hypot(status.getRocketPosition().x, status.getRocketPosition().y));
			data.setValue(FlightDataType.TYPE_POSITION_DIRECTION,
					Math.atan2(status.getRocketPosition().y, status.getRocketPosition().x));
			
			data.setValue(FlightDataType.TYPE_VELOCITY_XY,
					MathUtil.hypot(status.getRocketVelocity().x, status.getRocketVelocity().y));
			data.setValue(FlightDataType.TYPE_ACCELERATION_XY,
					MathUtil.hypot(linearAcceleration.x, linearAcceleration.y));
			
			data.setValue(FlightDataType.TYPE_ACCELERATION_TOTAL, linearAcceleration.length());
			
			double Re = airSpeed.length() *
					status.getConfiguration().getLengthAerodynamic() /
					atmosphere.getKinematicViscosity();
			data.setValue(FlightDataType.TYPE_REYNOLDS_NUMBER, Re);
		}
		

		data.setValue(FlightDataType.TYPE_LATITUDE, status.getRocketWorldPosition().getLatitudeRad());
		data.setValue(FlightDataType.TYPE_LONGITUDE, status.getRocketWorldPosition().getLongitudeRad());
		data.setValue(FlightDataType.TYPE_GRAVITY, gravity);
		
		if (status.getSimulationConditions().getGeodeticComputation() != GeodeticComputationStrategy.FLAT) {
			data.setValue(FlightDataType.TYPE_CORIOLIS_ACCELERATION, coriolisAcceleration.length());
		}
		

		data.setValue(FlightDataType.TYPE_VELOCITY_Z, status.getRocketVelocity().z);
		data.setValue(FlightDataType.TYPE_ACCELERATION_Z, linearAcceleration.z);
		
		data.setValue(FlightDataType.TYPE_VELOCITY_TOTAL, airSpeed.length());
		data.setValue(FlightDataType.TYPE_MACH_NUMBER, mach);
		
		data.setValue(FlightDataType.TYPE_MASS, mass);
		data.setValue(FlightDataType.TYPE_MOTOR_MASS, motorMass);
		
		data.setValue(FlightDataType.TYPE_THRUST_FORCE, 0);
		data.setValue(FlightDataType.TYPE_DRAG_FORCE, dragForce);
		
		data.setValue(FlightDataType.TYPE_WIND_VELOCITY, windSpeed.length());
		data.setValue(FlightDataType.TYPE_AIR_TEMPERATURE, atmosphere.getTemperature());
		data.setValue(FlightDataType.TYPE_AIR_PRESSURE, atmosphere.getPressure());
		data.setValue(FlightDataType.TYPE_SPEED_OF_SOUND, atmosphere.getMachSpeed());
		
		data.setValue(FlightDataType.TYPE_TIME_STEP, timeStep);
		data.setValue(FlightDataType.TYPE_COMPUTATION_TIME,
				(System.nanoTime() - status.getSimulationStartWallTime()) / 1000000000.0);
		log.trace("time " + data.getLast(FlightDataType.TYPE_TIME) + ", altitude " + data.getLast(FlightDataType.TYPE_ALTITUDE) + ", velocity " + data.getLast(FlightDataType.TYPE_VELOCITY_Z));
	}
	
}
