package net.sf.openrocket.simulation;

import net.sf.openrocket.models.atmosphere.AtmosphericConditions;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.GeodeticComputationStrategy;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.WorldCoordinate;

public class BasicLandingStepper extends AbstractSimulationStepper {
	
	private static final double RECOVERY_TIME_STEP = 0.5;
	
	@Override
	public SimulationStatus initialize(SimulationStatus status) {
		return status;
	}
	
	@Override
	public void step(SimulationStatus status, double maxTimeStep) throws SimulationException {
		double totalCD = 0;
		double refArea = status.getConfiguration().getReferenceArea();
		
		// Get the atmospheric conditions
		AtmosphericConditions atmosphere = modelAtmosphericConditions(status);
		
		//// Local wind speed and direction
		Coordinate windSpeed = modelWindVelocity(status);
		Coordinate airSpeed = status.getRocketVelocity().add(windSpeed);
		
		// Get total CD
		double mach = airSpeed.length() / atmosphere.getMachSpeed();
		for (RecoveryDevice c : status.getDeployedRecoveryDevices()) {
			totalCD += c.getCD(mach) * c.getArea() / refArea;
		}
		
		// Compute drag force
		double dynP = (0.5 * atmosphere.getDensity() * airSpeed.length2());
		double dragForce = totalCD * dynP * refArea;
		MassData massData = calculateMassData(status);
		double mass = massData.getCG().weight;
		

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
		


		// Select time step
		double timeStep = MathUtil.min(0.5 / linearAcceleration.length(), RECOVERY_TIME_STEP);
		
		// Perform Euler integration
		status.setRocketPosition(status.getRocketPosition().add(status.getRocketVelocity().multiply(timeStep)).
				add(linearAcceleration.multiply(MathUtil.pow2(timeStep) / 2)));
		status.setRocketVelocity(status.getRocketVelocity().add(linearAcceleration.multiply(timeStep)));
		status.setSimulationTime(status.getSimulationTime() + timeStep);
		

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
					status.getConfiguration().getLength() /
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
		data.setValue(FlightDataType.TYPE_PROPELLANT_MASS, 0.0); // Is this a reasonable assumption? Probably.
		
		data.setValue(FlightDataType.TYPE_THRUST_FORCE, 0);
		data.setValue(FlightDataType.TYPE_DRAG_FORCE, dragForce);
		
		data.setValue(FlightDataType.TYPE_WIND_VELOCITY, windSpeed.length());
		data.setValue(FlightDataType.TYPE_AIR_TEMPERATURE, atmosphere.getTemperature());
		data.setValue(FlightDataType.TYPE_AIR_PRESSURE, atmosphere.getPressure());
		data.setValue(FlightDataType.TYPE_SPEED_OF_SOUND, atmosphere.getMachSpeed());
		
		data.setValue(FlightDataType.TYPE_TIME_STEP, timeStep);
		data.setValue(FlightDataType.TYPE_COMPUTATION_TIME,
				(System.nanoTime() - status.getSimulationStartWallTime()) / 1000000000.0);
	}
	
}
