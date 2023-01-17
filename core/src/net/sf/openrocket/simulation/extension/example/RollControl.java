package net.sf.openrocket.simulation.extension.example;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.SimulationConditions;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.extension.AbstractSimulationExtension;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.MathUtil;

/**
 * An example listener that applies a PI-controller to adjust the cant of fins
 * to control the rocket's roll rate
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 *
 * One note: making aerodynamic changes during the simulation results in the simulation
 * running *extremely* slowly
 */
public class RollControl extends AbstractSimulationExtension {
	
	// save fin cant angle as a FlightDataType
	private static final ArrayList<FlightDataType> types = new ArrayList<FlightDataType>();
	private static final FlightDataType FIN_CANT_TYPE = FlightDataType.getType("Control fin cant", "\u03B1fc", UnitGroup.UNITS_ANGLE);

	@Override
	public void initialize(SimulationConditions conditions) throws SimulationException {
		conditions.getSimulationListenerList().add(new RollControlListener());
	}

	@Override
	public String getName() {
		return "Roll Control";
	}

	@Override
	public String getDescription() {
		return "Use a PID control to control a rocket's roll.  The current cant angle of the control finset is published to flight data as \u03B1fc. "
			+ "Since this extension modifies design parameters during the simulation, it causes the simulation to run <b>much</b> more slowly.";
	}

	@Override
	public List<FlightDataType> getFlightDataTypes() {
		return types;
	}
	
	RollControl() {
		types.add(FIN_CANT_TYPE);
	}

	public String getControlFinName() {
		return config.getString("controlFinName", "CONTROL");
	}

	public void setControlFinName(String name) {
		config.put("controlFinName", name);
		fireChangeEvent();
	}

	public double getStartTime() {
		return config.getDouble("startTime", 0.5);
	}

	public void setStartTime(double startTime) {
		config.put("startTime", startTime);
		fireChangeEvent();
	}

	// Desired roll rate (rad/sec)
	public double getSetPoint() {
		return config.getDouble("setPoint", 0.0);
	}

	public void setSetPoint(double rollRate) {
		config.put("setPoint", rollRate);
		fireChangeEvent();
	}

	// Maximum control fin turn rate (rad/sec)	
	public double getFinRate() {
		return config.getDouble("finRate", 10 * Math.PI/180);
	}

	public void setFinRate(double finRate) {
		config.put("finRate", finRate);
		fireChangeEvent();
	}

	// Maximum control fin angle (rad)
	public double getMaxFinAngle() {
		return config.getDouble("maxFinAngle", 15 * Math.PI / 180);
	}

	public void setMaxFinAngle(double maxFin) {
		config.put("maxFinAngle", maxFin);
		fireChangeEvent();
	}

	public double getKP() {
		return config.getDouble("KP", 0.007);
	}

	public void setKP(double KP) {
		config.put("KP", KP);
		fireChangeEvent();
	}

	public double getKI() {
		return config.getDouble("KI", 0.2);
	}

	public void setKI(double KI) {
		config.put("KI", KI);
		fireChangeEvent();
	}

	private class RollControlListener extends AbstractSimulationListener {
		private FinSet finset;
		
		private double rollRate;

		private double prevTime = 0;
		private double intState = 0;
		
		private double initialFinPosition;
		private double finPosition = 0;

		@Override
		public void startSimulation(SimulationStatus status) throws SimulationException {

			// Find the fin set
			finset = null;
			for (RocketComponent c : status.getConfiguration().getActiveComponents()) {
				if ((c instanceof FinSet) && c.getName().equals(getControlFinName())) {
					finset = (FinSet) c;
					break;
				}
			}
			if (finset == null) {
				throw new SimulationException("A fin set with name '" + getControlFinName() + "' was not found");
			}

			// remember the initial fin position so we can set it back after running the simulation
			initialFinPosition = finset.getCantAngle();
		}
		
		@Override
		public FlightConditions postFlightConditions(SimulationStatus status, FlightConditions flightConditions) {
			// Store the current roll rate for later use
			rollRate = flightConditions.getRollRate();
			return null;
		}
		
		@Override
		public void postStep(SimulationStatus status) throws SimulationException {
			// Activate PID controller only after a specific time
			if (status.getSimulationTime() < getStartTime()) {
				prevTime = status.getSimulationTime();
				return;
			}
			
			// Determine time step
			double deltaT = status.getSimulationTime() - prevTime;
			prevTime = status.getSimulationTime();
			
			// PID controller
			double error = getSetPoint() - rollRate;
			
			double p = getKP() * error;
			intState += error * deltaT;
			double i = getKI() * intState;
			
			double value = p + i;
			
			// Limit the fin turn rate
			if (finPosition < value) {
				finPosition = Math.min(finPosition + getFinRate() * deltaT, value);
			} else {
				finPosition = Math.max(finPosition - getFinRate() * deltaT, value);
			}
			
			// Clamp the fin angle between bounds
			if (Math.abs(finPosition) > getMaxFinAngle()) {
				System.err.printf("Attempting to set angle %.1f at t=%.3f, clamping.\n",
								  finPosition * 180 / Math.PI, status.getSimulationTime());
				finPosition = MathUtil.clamp(finPosition, -getMaxFinAngle(), getMaxFinAngle());
			}
			
			// Set the control fin cant and store the data
			finset.setCantAngle(finPosition);
			status.getFlightData().setValue(FIN_CANT_TYPE, finPosition);
		}

		@Override
		public void endSimulation(SimulationStatus status, SimulationException exception) {
			finset.setCantAngle(initialFinPosition);
		}
	}
}
