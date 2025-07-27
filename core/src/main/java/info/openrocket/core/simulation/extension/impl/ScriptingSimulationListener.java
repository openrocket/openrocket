package info.openrocket.core.simulation.extension.impl;

import java.util.HashSet;
import java.util.Set;

import javax.script.Invocable;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.aerodynamics.AerodynamicForces;
import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.masscalc.RigidBody;
import info.openrocket.core.models.atmosphere.AtmosphericConditions;
import info.openrocket.core.motor.MotorConfigurationId;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.RecoveryDevice;
import info.openrocket.core.simulation.AccelerationData;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.simulation.MotorClusterState;
import info.openrocket.core.simulation.SimulationStatus;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.exception.SimulationListenerException;
import info.openrocket.core.simulation.listeners.SimulationComputationListener;
import info.openrocket.core.simulation.listeners.SimulationEventListener;
import info.openrocket.core.simulation.listeners.SimulationListener;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.Coordinate;

public class ScriptingSimulationListener
		implements SimulationListener, SimulationComputationListener, SimulationEventListener, Cloneable {

	private final static Logger logger = LoggerFactory.getLogger(ScriptingSimulationListener.class);

	/*
	 * NOTE: This class is used instead of using the scripting interface API
	 * so that unimplemented script methods are not called unnecessarily.
	 */

	private final Invocable invocable;
	private Set<String> missing = new HashSet<>();

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
			clone.missing = new HashSet<>(missing);
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new BugException(e);
		}
	}

	//// SimulationListener ////

	@Override
	public void startSimulation(SimulationStatus status) throws SimulationException {
		invoke(Void.class, null, "startSimulation", status);
	}

	@Override
	public void endSimulation(SimulationStatus status, SimulationException exception) {
		try {
			invoke(Void.class, null, "endSimulation", status, exception);
		} catch (SimulationException e) {
		}
	}

	@Override
	public boolean preStep(SimulationStatus status) throws SimulationException {
		return invoke(Boolean.class, true, "preStep", status);
	}

	@Override
	public void postStep(SimulationStatus status) throws SimulationException {
		invoke(Void.class, null, "postStep", status);
	}

	//// SimulationEventListener ////

	@Override
	public boolean addFlightEvent(SimulationStatus status, FlightEvent event) throws SimulationException {
		return invoke(Boolean.class, true, "addFlightEvent", status, event);
	}

	@Override
	public boolean handleFlightEvent(SimulationStatus status, FlightEvent event) throws SimulationException {
		return invoke(Boolean.class, true, "handleFlightEvent", status, event);
	}

	@Override
	public boolean motorIgnition(SimulationStatus status, MotorConfigurationId motorId, MotorMount mount,
			MotorClusterState instance) throws SimulationException {
		return invoke(Boolean.class, true, "motorIgnition", status, motorId, mount, instance);
	}

	@Override
	public boolean recoveryDeviceDeployment(SimulationStatus status, RecoveryDevice recoveryDevice)
			throws SimulationException {
		return invoke(Boolean.class, true, "recoveryDeviceDeployment", status, recoveryDevice);
	}

	//// SimulationComputationListener ////

	@Override
	public AccelerationData preAccelerationCalculation(SimulationStatus status) throws SimulationException {
		return invoke(AccelerationData.class, null, "preAccelerationCalculation", status);
	}

	@Override
	public AerodynamicForces preAerodynamicCalculation(SimulationStatus status) throws SimulationException {
		return invoke(AerodynamicForces.class, null, "preAerodynamicCalculation", status);
	}

	@Override
	public AtmosphericConditions preAtmosphericModel(SimulationStatus status) throws SimulationException {
		return invoke(AtmosphericConditions.class, null, "preAtmosphericModel", status);
	}

	@Override
	public FlightConditions preFlightConditions(SimulationStatus status) throws SimulationException {
		return invoke(FlightConditions.class, null, "preFlightConditions", status);
	}

	@Override
	public double preGravityModel(SimulationStatus status) throws SimulationException {
		return invoke(Double.class, Double.NaN, "preGravityModel", status);
	}

	@Override
	public RigidBody preMassCalculation(SimulationStatus status) throws SimulationException {
		return invoke(RigidBody.class, null, "preMassCalculation", status);
	}

	@Override
	public double preSimpleThrustCalculation(SimulationStatus status) throws SimulationException {
		return invoke(Double.class, Double.NaN, "preSimpleThrustCalculation", status);
	}

	@Override
	public Coordinate preWindModel(SimulationStatus status) throws SimulationException {
		return invoke(Coordinate.class, null, "preWindModel", status);
	}

	@Override
	public AccelerationData postAccelerationCalculation(SimulationStatus status, AccelerationData acceleration)
			throws SimulationException {
		return invoke(AccelerationData.class, null, "postAccelerationCalculation", status, acceleration);
	}

	@Override
	public AerodynamicForces postAerodynamicCalculation(SimulationStatus status, AerodynamicForces forces)
			throws SimulationException {
		return invoke(AerodynamicForces.class, null, "postAerodynamicCalculation", status, forces);
	}

	@Override
	public AtmosphericConditions postAtmosphericModel(SimulationStatus status,
			AtmosphericConditions atmosphericConditions) throws SimulationException {
		return invoke(AtmosphericConditions.class, null, "postAtmosphericModel", status, atmosphericConditions);
	}

	@Override
	public FlightConditions postFlightConditions(SimulationStatus status, FlightConditions flightConditions)
			throws SimulationException {
		return invoke(FlightConditions.class, null, "postFlightConditions", status, flightConditions);
	}

	@Override
	public double postGravityModel(SimulationStatus status, double gravity) throws SimulationException {
		return invoke(Double.class, Double.NaN, "postGravityModel", status, gravity);
	}

	@Override
	public RigidBody postMassCalculation(SimulationStatus status, RigidBody RigidBody) throws SimulationException {
		return invoke(RigidBody.class, null, "postMassCalculation", status, RigidBody);
	}

	@Override
	public double postSimpleThrustCalculation(SimulationStatus status, double thrust) throws SimulationException {
		return invoke(Double.class, Double.NaN, "postSimpleThrustCalculation", status, thrust);
	}

	@Override
	public Coordinate postWindModel(SimulationStatus status, Coordinate wind) throws SimulationException {
		return invoke(Coordinate.class, null, "postWindModel", status, wind);
	}

	@SuppressWarnings("unchecked")
	private <T> T invoke(Class<T> retType, T def, String method, Object... args) throws SimulationException {
		try {
			if (!missing.contains(method)) {
				Object o = invocable.invokeFunction(method, args);
				if (o == null) {
					// Use default/null if function returns nothing
					return def;
				} else if (!o.getClass().equals(retType)) {
					throw new SimulationListenerException("Custom script function " + method + " returned type " +
							o.getClass().getSimpleName() + ", expected " + retType.getSimpleName());
				} else {
					return (T) o;
				}
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
