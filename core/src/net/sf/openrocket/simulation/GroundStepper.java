package net.sf.openrocket.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.models.atmosphere.AtmosphericConditions;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Coordinate;

public class GroundStepper extends AbstractSimulationStepper {
	private static final Logger log = LoggerFactory.getLogger(GroundStepper.class);
	
	@Override
	public SimulationStatus initialize(SimulationStatus original) {
		log.trace("initializing GroundStepper");
		SimulationStatus status = new SimulationStatus(original);

		return status;
	}

	@Override
	public void step(SimulationStatus status, double timeStep) throws SimulationException {
		log.trace("step:  position=" + status.getRocketPosition() + ", velocity=" + status.getRocketVelocity());
				
		status.setRocketVelocity(Coordinate.ZERO);
		status.setRocketRotationVelocity(Coordinate.ZERO);
		status.setRocketPosition(status.getRocketPosition().setZ(0));
		
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
			data.setValue(FlightDataType.TYPE_ACCELERATION_XY, 0.0);
			
			data.setValue(FlightDataType.TYPE_ACCELERATION_TOTAL, 0.0);
			
			data.setValue(FlightDataType.TYPE_REYNOLDS_NUMBER, Double.POSITIVE_INFINITY);
		}
		
		data.setValue(FlightDataType.TYPE_LATITUDE, status.getRocketWorldPosition().getLatitudeRad());
		data.setValue(FlightDataType.TYPE_LONGITUDE, status.getRocketWorldPosition().getLongitudeRad());
		data.setValue(FlightDataType.TYPE_GRAVITY, modelGravity(status));
		
		data.setValue(FlightDataType.TYPE_CORIOLIS_ACCELERATION, 0.0);
		
		data.setValue(FlightDataType.TYPE_VELOCITY_Z, status.getRocketVelocity().z);
		data.setValue(FlightDataType.TYPE_ACCELERATION_Z, 0.0);
		
		data.setValue(FlightDataType.TYPE_VELOCITY_TOTAL, 0.0);
		data.setValue(FlightDataType.TYPE_MACH_NUMBER, 0.0);
		
		data.setValue(FlightDataType.TYPE_MASS, calculateStructureMass(status).getMass());
		data.setValue(FlightDataType.TYPE_PROPELLANT_MASS, 0.0); // Is this a reasonable assumption? Probably.
		
		data.setValue(FlightDataType.TYPE_THRUST_FORCE, 0.0);
		data.setValue(FlightDataType.TYPE_DRAG_FORCE, 0.0);
		
		data.setValue(FlightDataType.TYPE_WIND_VELOCITY, modelWindVelocity(status).length());
		
		AtmosphericConditions atmosphere = modelAtmosphericConditions(status);
		data.setValue(FlightDataType.TYPE_AIR_TEMPERATURE, atmosphere.getTemperature());
		data.setValue(FlightDataType.TYPE_AIR_PRESSURE, atmosphere.getPressure());
		data.setValue(FlightDataType.TYPE_SPEED_OF_SOUND, atmosphere.getMachSpeed());
		
		data.setValue(FlightDataType.TYPE_TIME_STEP, timeStep);
		data.setValue(FlightDataType.TYPE_COMPUTATION_TIME,
					  (System.nanoTime() - status.getSimulationStartWallTime()) / 1000000000.0);

		status.setSimulationTime(status.getSimulationTime() + timeStep);		
	}
}
