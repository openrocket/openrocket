package net.sf.openrocket.simulation.listeners.example;

import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.MathUtil;

/**
 * An example listener that applies a PI-controller to adjust the cant of fins
 * named "CONTROL" to stop the rocket from rolling.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class RollControlListener extends AbstractSimulationListener {
	
	// Name of control fin set
	private static final String CONTROL_FIN_NAME = "CONTROL";
	
	// Define custom flight data type
	private static final FlightDataType FIN_CANT_TYPE = FlightDataType.getType("Control fin cant", "\u03B1fc", UnitGroup.UNITS_ANGLE);
	
	// Simulation time at which PID controller is activated
	private static final double START_TIME = 0.5;
	
	// Desired roll rate (rad/sec)
	private static final double SETPOINT = 0.0;
	
	// Maximum control fin turn rate (rad/sec)
	private static final double TURNRATE = 10 * Math.PI / 180;
	
	// Maximum control fin angle (rad)
	private static final double MAX_ANGLE = 15 * Math.PI / 180;
	

	/*
	 * PID parameters
	 * 
	 * At M=0.3 KP oscillation threshold between 0.35 and 0.4.    Good KI=3
	 * At M=0.6 KP oscillation threshold between 0.07 and 0.08    Good KI=2
	 * At M=0.9 KP oscillation threshold between 0.013 and 0.014  Good KI=0.5
	 */
	private static final double KP = 0.007;
	private static final double KI = 0.2;
	



	private double rollrate;
	
	private double prevTime = 0;
	private double intState = 0;
	
	private double finPosition = 0;
	
	

	@Override
	public FlightConditions postFlightConditions(SimulationStatus status, FlightConditions flightConditions) {
		// Store the current roll rate for later use
		rollrate = flightConditions.getRollRate();
		return null;
	}
	
	
	@Override
	public void postStep(SimulationStatus status) throws SimulationException {
		
		// Activate PID controller only after a specific time
		if (status.getSimulationTime() < START_TIME) {
			prevTime = status.getSimulationTime();
			return;
		}
		
		// Find the fin set named CONTROL
		FinSet finset = null;
		for (RocketComponent c : status.getConfiguration()) {
			if ((c instanceof FinSet) && (c.getName().equals(CONTROL_FIN_NAME))) {
				finset = (FinSet) c;
				break;
			}
		}
		if (finset == null) {
			throw new SimulationException("A fin set with name '" + CONTROL_FIN_NAME + "' was not found");
		}
		

		// Determine time step
		double deltaT = status.getSimulationTime() - prevTime;
		prevTime = status.getSimulationTime();
		

		// PID controller
		double error = SETPOINT - rollrate;
		
		double p = KP * error;
		intState += error * deltaT;
		double i = KI * intState;
		
		double value = p + i;
		

		// Clamp the fin angle between -MAX_ANGLE and MAX_ANGLE
		if (Math.abs(value) > MAX_ANGLE) {
			System.err.printf("Attempting to set angle %.1f at t=%.3f, clamping.\n",
					value * 180 / Math.PI, status.getSimulationTime());
			value = MathUtil.clamp(value, -MAX_ANGLE, MAX_ANGLE);
		}
		

		// Limit the fin turn rate
		if (finPosition < value) {
			finPosition = Math.min(finPosition + TURNRATE * deltaT, value);
		} else {
			finPosition = Math.max(finPosition - TURNRATE * deltaT, value);
		}
		
		// Set the control fin cant and store the data
		finset.setCantAngle(finPosition);
		status.getFlightData().setValue(FIN_CANT_TYPE, finPosition);
		
	}
}
