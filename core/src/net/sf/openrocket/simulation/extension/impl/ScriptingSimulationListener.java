package net.sf.openrocket.simulation.extension.impl;

import java.util.HashSet;
import java.util.Set;

import javax.script.Invocable;
import javax.script.ScriptException;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.models.atmosphere.AtmosphericConditions;
import net.sf.openrocket.motor.MotorId;
import net.sf.openrocket.motor.MotorInstance;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.simulation.AccelerationData;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.simulation.MassData;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.listeners.SimulationComputationListener;
import net.sf.openrocket.simulation.listeners.SimulationEventListener;
import net.sf.openrocket.simulation.listeners.SimulationListener;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptingSimulationListener implements SimulationListener, SimulationComputationListener, SimulationEventListener, Cloneable {
	
	private final static Logger logger = LoggerFactory.getLogger(ScriptingSimulationListener.class);
	
	/*
	 * NOTE:  This class is used instead of using the scripting interface API
	 * so that unimplemented script methods are not called unnecessarily.
	 */
	
	private Invocable invocable;
	private Set<String> missing = new HashSet<String>();
	
	
	public ScriptingSimulationListener(Invocable invocable) {
		this.invocable = invocable;
	}
	
	
	@Override
	public boolean isSystemListener() {
		return false;
	}
	
	
	@Override
	public SimulationListener clone() {
		try {
			ScriptingSimulationListener clone = (ScriptingSimulationListener) super.clone();
			clone.missing = new HashSet<String>(missing);
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new BugException(e);
		}
	}
	
	
	
	////  SimulationListener  ////
	
	@Override
	public void startSimulation(SimulationStatus status) throws SimulationException {
		invoke(null, "startSimulation", status);
	}
	
	@Override
	public void endSimulation(SimulationStatus status, SimulationException exception) {
		try {
			invoke(null, "endSimulation", status, exception);
		} catch (SimulationException e) {
		}
	}
	
	@Override
	public boolean preStep(SimulationStatus status) throws SimulationException {
		return invoke(true, "preStep", status);
	}
	
	@Override
	public void postStep(SimulationStatus status) throws SimulationException {
		invoke(null, "postStep", status);
	}
	
	
	
	////  SimulationEventListener  ////
	
	@Override
	public boolean addFlightEvent(SimulationStatus status, FlightEvent event) throws SimulationException {
		return invoke(true, "addFlightEvent", status, event);
	}
	
	@Override
	public boolean handleFlightEvent(SimulationStatus status, FlightEvent event) throws SimulationException {
		return invoke(true, "handleFlightEvent", status, event);
	}
	
	@Override
	public boolean motorIgnition(SimulationStatus status, MotorId motorId, MotorMount mount, MotorInstance instance) throws SimulationException {
		return invoke(true, "motorIgnition", status, motorId, mount, instance);
	}
	
	@Override
	public boolean recoveryDeviceDeployment(SimulationStatus status, RecoveryDevice recoveryDevice) throws SimulationException {
		return invoke(true, "recoveryDeviceDeployment", status, recoveryDevice);
	}
	
	
	
	////  SimulationComputationListener  ////
	
	@Override
	public AccelerationData preAccelerationCalculation(SimulationStatus status) throws SimulationException {
		return invoke(null, "preAccelerationCalculation", status);
	}
	
	@Override
	public AerodynamicForces preAerodynamicCalculation(SimulationStatus status) throws SimulationException {
		return invoke(null, "preAerodynamicCalculation", status);
	}
	
	@Override
	public AtmosphericConditions preAtmosphericModel(SimulationStatus status) throws SimulationException {
		return invoke(null, "preAtmosphericModel", status);
	}
	
	@Override
	public FlightConditions preFlightConditions(SimulationStatus status) throws SimulationException {
		return invoke(null, "preFlightConditions", status);
	}
	
	@Override
	public double preGravityModel(SimulationStatus status) throws SimulationException {
		return invoke(Double.NaN, "preGravityModel", status);
	}
	
	@Override
	public MassData preMassCalculation(SimulationStatus status) throws SimulationException {
		return invoke(null, "preMassCalculation", status);
	}
	
	@Override
	public double preSimpleThrustCalculation(SimulationStatus status) throws SimulationException {
		return invoke(Double.NaN, "preSimpleThrustCalculation", status);
	}
	
	@Override
	public Coordinate preWindModel(SimulationStatus status) throws SimulationException {
		return invoke(null, "preWindModel", status);
	}
	
	@Override
	public AccelerationData postAccelerationCalculation(SimulationStatus status, AccelerationData acceleration) throws SimulationException {
		return invoke(null, "postAccelerationCalculation", status, acceleration);
	}
	
	@Override
	public AerodynamicForces postAerodynamicCalculation(SimulationStatus status, AerodynamicForces forces) throws SimulationException {
		return invoke(null, "postAerodynamicCalculation", status, forces);
	}
	
	@Override
	public AtmosphericConditions postAtmosphericModel(SimulationStatus status, AtmosphericConditions atmosphericConditions) throws SimulationException {
		return invoke(null, "postAtmosphericModel", status, atmosphericConditions);
	}
	
	@Override
	public FlightConditions postFlightConditions(SimulationStatus status, FlightConditions flightConditions) throws SimulationException {
		return invoke(null, "postFlightConditions", status, flightConditions);
	}
	
	@Override
	public double postGravityModel(SimulationStatus status, double gravity) throws SimulationException {
		return invoke(Double.NaN, "postGravityModel", status, gravity);
	}
	
	@Override
	public MassData postMassCalculation(SimulationStatus status, MassData massData) throws SimulationException {
		return invoke(null, "postMassCalculation", status, massData);
	}
	
	@Override
	public double postSimpleThrustCalculation(SimulationStatus status, double thrust) throws SimulationException {
		return invoke(Double.NaN, "postSimpleThrustCalculation", status, thrust);
	}
	
	@Override
	public Coordinate postWindModel(SimulationStatus status, Coordinate wind) throws SimulationException {
		return invoke(null, "postWindModel", status, wind);
	}
	
	
	@SuppressWarnings("unchecked")
	private <T> T invoke(T def, String method, Object... args) throws SimulationException {
		try {
			if (!missing.contains(method)) {
				return (T) invocable.invokeFunction(method, args);
			}
		} catch (NoSuchMethodException e) {
			missing.add(method);
			// fall-through
		} catch (ScriptException e) {
			logger.warn("Script exception in " + method + ": " + e, e);
			throw new SimulationException("Script failed: " + e.getMessage());
		}
		return def;
	}
	
}
