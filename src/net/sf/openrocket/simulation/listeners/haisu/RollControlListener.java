package net.sf.openrocket.simulation.listeners.haisu;

import java.util.Collection;

import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;
import net.sf.openrocket.util.MathUtil;


public class RollControlListener extends AbstractSimulationListener {

	private static final double DELTA_T = 0.01;
	private static final double START_TIME = 0.5;
	
	private static final double MACH = 0.9;
	
	private static final double SETPOINT = 0.0;
	
	private static final double TURNRATE = 10 * Math.PI/180;  // per second

	
	/*
	 * At M=0.3 KP oscillation threshold between 0.35 and 0.4.    Good KI=3
	 * At M=0.6 KP oscillation threshold between 0.07 and 0.08    Good KI=2
	 * At M=0.9 KP oscillation threshold between 0.013 and 0.014  Good KI=0.5
	 */
	private static final double KP = 0.007;
	private static final double KI = 0.2;
	
	
	private static final double MAX_ANGLE = 15 * Math.PI/180;
	
	
	
	
	private double rollrate;
	
	private double intState = 0;
	
	private double finPosition = 0;
	
	
	public RollControlListener() {
	}

	@Override
	public void flightConditions(SimulationStatus status, FlightConditions conditions) {
		rollrate = conditions.getRollRate();
	}
	
	@Override
	public Collection<FlightEvent> stepTaken(SimulationStatus status) {
		
		if (status.time < START_TIME)
			return null;

		// PID controller
		FinSet finset = null;
		for (RocketComponent c: status.configuration) {
			if ((c instanceof FinSet) && (c.getName().equals("CONTROL"))) {
				finset = (FinSet)c;
				break;
			}
		}
		if (finset==null) {
			throw new RuntimeException("CONTROL fin not found");
		}
		
		
		double error = SETPOINT - rollrate;
		
		
		error = Math.signum(error) * error * error;    ////  pow2(error)

		double p = KP * error;
		intState += error * DELTA_T;
		double i = KI * intState;
		
		double value = p+i;
		
				
		if (Math.abs(value) > MAX_ANGLE) {
			System.err.printf("Attempting to set angle %.1f at t=%.3f, clamping.\n", 
					value*180/Math.PI, status.time);
			value = MathUtil.clamp(value, -MAX_ANGLE, MAX_ANGLE);
		}
		
		
		if (finPosition < value) {
			finPosition = Math.min(finPosition + TURNRATE*DELTA_T, value);
		} else {
			finPosition = Math.max(finPosition - TURNRATE*DELTA_T, value);
		}

		if (MathUtil.equals(status.time*10, Math.rint(status.time*10))) {
			System.err.printf("t=%.3f  angle=%.1f  current=%.1f\n",status.time, 
					value*180/Math.PI, finPosition*180/Math.PI);
		}

		finset.setCantAngle(finPosition);
				
		return null;
	}
	
	
}
