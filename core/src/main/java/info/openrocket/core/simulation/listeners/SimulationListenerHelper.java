package info.openrocket.core.simulation.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.aerodynamics.AerodynamicForces;
import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.logging.Warning;
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
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.ModID;

/**
 * Helper methods for firing events to simulation listeners.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SimulationListenerHelper {

	private static final Logger log = LoggerFactory.getLogger(SimulationListenerHelper.class);

	//////// SimulationListener methods ////////

	/**
	 * Fire startSimulation event.
	 */
	public static void fireStartSimulation(SimulationStatus status)
			throws SimulationException {
		ModID modID = status.getModID();

		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			l.startSimulation(status);
			if (modID != status.getModID()) {
				warn(status, l);
				modID = status.getModID();
			}
		}
	}

	/**
	 * Fire endSimulation event.
	 */
	public static void fireEndSimulation(SimulationStatus status, SimulationException exception) {
		ModID modID = status.getModID();

		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			l.endSimulation(status, exception);
			if (modID != status.getModID()) {
				warn(status, l);
				modID = status.getModID();
			}
		}
	}

	/**
	 * Fire preStep event.
	 * 
	 * @return <code>true</code> to handle step normally, <code>false</code> to skip
	 *         the step.
	 */
	public static boolean firePreStep(SimulationStatus status)
			throws SimulationException {
		boolean b;
		ModID modID = status.getModID();

		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			b = l.preStep(status);
			if (modID != status.getModID()) {
				warn(status, l);
				modID = status.getModID();
			}
			if (b == false) {
				warn(status, l);
				return false;
			}
		}
		return true;
	}

	/**
	 * Fire postStep event.
	 */
	public static void firePostStep(SimulationStatus status)
			throws SimulationException {
		ModID modID = status.getModID();

		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			l.postStep(status);
			if (modID != status.getModID()) {
				warn(status, l);
				modID = status.getModID();
			}
		}
	}

	//////// SimulationEventListener methods ////////

	/**
	 * Fire an add flight event event.
	 * 
	 * @return <code>true</code> to add the event normally, <code>false</code> to
	 *         skip adding the event.
	 */
	public static boolean fireAddFlightEvent(SimulationStatus status, FlightEvent event) throws SimulationException {
		boolean b;
		ModID modID = status.getModID();

		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			if (l instanceof SimulationEventListener) {
				b = ((SimulationEventListener) l).addFlightEvent(status, event);
				if (modID != status.getModID()) {
					warn(status, l);
					modID = status.getModID();
				}
				if (b == false) {
					warn(status, l);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Fire a handle flight event event.
	 * 
	 * @return <code>true</code> to handle the event normally, <code>false</code> to
	 *         skip event.
	 */
	public static boolean fireHandleFlightEvent(SimulationStatus status, FlightEvent event) throws SimulationException {
		boolean b;
		ModID modID = status.getModID();

		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			if (l instanceof SimulationEventListener) {
				b = ((SimulationEventListener) l).handleFlightEvent(status, event);
				if (modID != status.getModID()) {
					warn(status, l);
					modID = status.getModID();
				}
				if (b == false) {
					warn(status, l);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Fire motor ignition event.
	 * 
	 * @return <code>true</code> to handle the event normally, <code>false</code> to
	 *         skip event.
	 */
	public static boolean fireMotorIgnition(SimulationStatus status, MotorConfigurationId motorId, MotorMount mount,
			MotorClusterState instance) throws SimulationException {
		boolean result;
		ModID modID = status.getModID(); // Contains also motor instance

		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			if (l instanceof SimulationEventListener) {
				result = ((SimulationEventListener) l).motorIgnition(status, motorId, mount, instance);
				if (modID != status.getModID()) {
					warn(status, l);
					modID = status.getModID();
				}
				if (false == result) {
					warn(status, l);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Fire recovery device deployment event.
	 * 
	 * @return <code>true</code> to handle the event normally, <code>false</code> to
	 *         skip event.
	 */
	public static boolean fireRecoveryDeviceDeployment(SimulationStatus status, RecoveryDevice device)
			throws SimulationException {
		boolean result;
		ModID modID = status.getModID(); // Contains also motor instance

		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			if (l instanceof SimulationEventListener) {
				result = ((SimulationEventListener) l).recoveryDeviceDeployment(status, device);
				if (modID != status.getModID()) {
					warn(status, l);
					modID = status.getModID();
				}
				if (false == result) {
					warn(status, l);
					return false;
				}
			}
		}
		return true;
	}

	//////// SimulationComputationalListener methods ////////

	/**
	 * Fire preAtmosphericModel event.
	 * 
	 * @return <code>null</code> normally, or overriding atmospheric conditions.
	 */
	public static AtmosphericConditions firePreAtmosphericModel(SimulationStatus status)
			throws SimulationException {
		AtmosphericConditions conditions;
		ModID modID = status.getModID();

		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			if (l instanceof SimulationComputationListener) {
				conditions = ((SimulationComputationListener) l).preAtmosphericModel(status);
				if (modID != status.getModID()) {
					warn(status, l);
					modID = status.getModID();
				}
				if (conditions != null) {
					warn(status, l);
					return conditions;
				}
			}
		}
		return null;
	}

	/**
	 * Fire postAtmosphericModel event.
	 * 
	 * @return the atmospheric conditions to use.
	 */
	public static AtmosphericConditions firePostAtmosphericModel(SimulationStatus status,
			AtmosphericConditions conditions)
			throws SimulationException {
		AtmosphericConditions c;
		AtmosphericConditions clone = conditions.clone();
		ModID modID = status.getModID();

		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			if (l instanceof SimulationComputationListener) {
				c = ((SimulationComputationListener) l).postAtmosphericModel(status, clone);
				if (modID != status.getModID()) {
					warn(status, l);
					modID = status.getModID();
				}
				if (c != null && !c.equals(conditions)) {
					warn(status, l);
					conditions = c;
					clone = conditions.clone();
				}
			}
		}
		return conditions;
	}

	/**
	 * Fire preWindModel event.
	 * 
	 * @return <code>null</code> normally, or overriding wind.
	 */
	public static Coordinate firePreWindModel(SimulationStatus status)
			throws SimulationException {
		Coordinate wind;
		ModID modID = status.getModID();

		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			if (l instanceof SimulationComputationListener) {
				wind = ((SimulationComputationListener) l).preWindModel(status);
				if (modID != status.getModID()) {
					warn(status, l);
					modID = status.getModID();
				}
				if (wind != null) {
					warn(status, l);
					return wind;
				}
			}
		}
		return null;
	}

	/**
	 * Fire postWindModel event.
	 * 
	 * @return the wind to use.
	 */
	public static Coordinate firePostWindModel(SimulationStatus status, Coordinate wind) throws SimulationException {
		Coordinate w;
		ModID modID = status.getModID();

		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			if (l instanceof SimulationComputationListener) {
				w = ((SimulationComputationListener) l).postWindModel(status, wind);
				if (modID != status.getModID()) {
					warn(status, l);
					modID = status.getModID();
				}
				if (w != null && !w.equals(wind)) {
					warn(status, l);
					wind = w;
				}
			}
		}
		return wind;
	}

	/**
	 * Fire preGravityModel event.
	 * 
	 * @return <code>NaN</code> normally, or overriding gravity.
	 */
	public static double firePreGravityModel(SimulationStatus status)
			throws SimulationException {
		double gravity;
		ModID modID = status.getModID();

		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			if (l instanceof SimulationComputationListener) {
				gravity = ((SimulationComputationListener) l).preGravityModel(status);
				if (modID != status.getModID()) {
					warn(status, l);
					modID = status.getModID();
				}
				if (!Double.isNaN(gravity)) {
					warn(status, l);
					return gravity;
				}
			}
		}
		return Double.NaN;
	}

	/**
	 * Fire postGravityModel event.
	 * 
	 * @return the gravity to use.
	 */
	public static double firePostGravityModel(SimulationStatus status, double gravity) throws SimulationException {
		double g;
		ModID modID = status.getModID();

		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			if (l instanceof SimulationComputationListener) {
				g = ((SimulationComputationListener) l).postGravityModel(status, gravity);
				if (modID != status.getModID()) {
					warn(status, l);
					modID = status.getModID();
				}
				if (!Double.isNaN(g) && !MathUtil.equals(g, gravity)) {
					warn(status, l);
					gravity = g;
				}
			}
		}
		return gravity;
	}

	/**
	 * Fire preFlightConditions event.
	 * 
	 * @return <code>null</code> normally, or overriding flight conditions.
	 */
	public static FlightConditions firePreFlightConditions(SimulationStatus status)
			throws SimulationException {
		FlightConditions conditions;
		ModID modID = status.getModID();

		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			if (l instanceof SimulationComputationListener) {
				conditions = ((SimulationComputationListener) l).preFlightConditions(status);
				if (modID != status.getModID()) {
					warn(status, l);
					modID = status.getModID();
				}
				if (conditions != null) {
					warn(status, l);
					return conditions;
				}
			}
		}
		return null;
	}

	/**
	 * Fire postFlightConditions event.
	 * 
	 * @return the flight conditions to use: either <code>conditions</code> or a new
	 *         object
	 *         containing the modified conditions.
	 */
	public static FlightConditions firePostFlightConditions(SimulationStatus status, FlightConditions conditions)
			throws SimulationException {
		FlightConditions c;
		FlightConditions clone = conditions.clone();
		ModID modID = status.getModID();

		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			if (l instanceof SimulationComputationListener) {
				c = ((SimulationComputationListener) l).postFlightConditions(status, clone);
				if (modID != status.getModID()) {
					warn(status, l);
					modID = status.getModID();
				}
				if (c != null && !c.equals(conditions)) {
					warn(status, l);
					conditions = c;
					clone = conditions.clone();
				}
			}
		}
		return conditions;
	}

	/**
	 * Fire preAerodynamicCalculation event.
	 * 
	 * @return <code>null</code> normally, or overriding aerodynamic forces.
	 */
	public static AerodynamicForces firePreAerodynamicCalculation(SimulationStatus status)
			throws SimulationException {
		AerodynamicForces forces;
		ModID modID = status.getModID();

		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			if (l instanceof SimulationComputationListener) {
				forces = ((SimulationComputationListener) l).preAerodynamicCalculation(status);
				if (modID != status.getModID()) {
					warn(status, l);
					modID = status.getModID();
				}
				if (forces != null) {
					warn(status, l);
					return forces;
				}
			}
		}
		return null;
	}

	/**
	 * Fire postAerodynamicCalculation event.
	 * 
	 * @return the aerodynamic forces to use.
	 */
	public static AerodynamicForces firePostAerodynamicCalculation(SimulationStatus status, AerodynamicForces forces)
			throws SimulationException {
		AerodynamicForces f;
		AerodynamicForces clone = forces.clone();
		ModID modID = status.getModID();

		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			if (l instanceof SimulationComputationListener) {
				f = ((SimulationComputationListener) l).postAerodynamicCalculation(status, clone);
				if (modID != status.getModID()) {
					warn(status, l);
					modID = status.getModID();
				}
				if (f != null && !f.equals(forces)) {
					warn(status, l);
					forces = f;
					clone = forces.clone();
				}
			}
		}
		return forces;
	}

	/**
	 * Fire preMassCalculation event.
	 * 
	 * @return <code>null</code> normally, or overriding mass data.
	 */
	public static RigidBody firePreMassCalculation(SimulationStatus status)
			throws SimulationException {
		RigidBody mass;
		ModID modID = status.getModID();

		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			if (l instanceof SimulationComputationListener) {
				mass = ((SimulationComputationListener) l).preMassCalculation(status);
				if (modID != status.getModID()) {
					warn(status, l);
					modID = status.getModID();
				}
				if (mass != null) {
					warn(status, l);
					return mass;
				}
			}
		}
		return null;
	}

	/**
	 * Fire postMassCalculation event.
	 * 
	 * @return the resultant mass data
	 */
	public static RigidBody firePostMassCalculation(SimulationStatus status, RigidBody mass)
			throws SimulationException {
		RigidBody m;
		ModID modID = status.getModID();

		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			if (l instanceof SimulationComputationListener) {
				m = ((SimulationComputationListener) l).postMassCalculation(status, mass);
				if (modID != status.getModID()) {
					warn(status, l);
					modID = status.getModID();
				}
				if (m != null && !m.equals(mass)) {
					warn(status, l);
					mass = m;
				}
			}
		}
		return mass;
	}

	/**
	 * Fire preThrustComputation event.
	 * 
	 * @return <code>NaN</code> normally, or overriding thrust.
	 */
	public static double firePreThrustCalculation(SimulationStatus status)
			throws SimulationException {
		double thrust;
		ModID modID = status.getModID();

		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			if (l instanceof SimulationComputationListener) {
				thrust = ((SimulationComputationListener) l).preSimpleThrustCalculation(status);
				if (modID != status.getModID()) {
					warn(status, l);
					modID = status.getModID();
				}
				if (!Double.isNaN(thrust)) {
					warn(status, l);
					return thrust;
				}
			}
		}
		return Double.NaN;
	}

	/**
	 * Fire postThrustComputation event.
	 * 
	 * @return the thrust value to use.
	 */
	public static double firePostThrustCalculation(SimulationStatus status, double thrust) throws SimulationException {
		double t;
		ModID modID = status.getModID();

		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			if (l instanceof SimulationComputationListener) {
				t = ((SimulationComputationListener) l).postSimpleThrustCalculation(status, thrust);
				if (modID != status.getModID()) {
					warn(status, l);
					modID = status.getModID();
				}
				if (!Double.isNaN(t) && !MathUtil.equals(t, thrust)) {
					warn(status, l);
					thrust = t;
				}
			}
		}
		return thrust;
	}

	/**
	 * Fire preMassCalculation event.
	 * 
	 * @return <code>null</code> normally, or overriding mass data.
	 */
	public static AccelerationData firePreAccelerationCalculation(SimulationStatus status) throws SimulationException {
		AccelerationData acceleration;
		ModID modID = status.getModID();

		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			if (l instanceof SimulationComputationListener) {
				acceleration = ((SimulationComputationListener) l).preAccelerationCalculation(status);
				if (modID != status.getModID()) {
					warn(status, l);
					modID = status.getModID();
				}
				if (acceleration != null) {
					warn(status, l);
					return acceleration;
				}
			}
		}
		return null;
	}

	/**
	 * Fire postMassCalculation event.
	 * 
	 * @return the aerodynamic forces to use.
	 */
	public static AccelerationData firePostAccelerationCalculation(SimulationStatus status,
			AccelerationData acceleration) throws SimulationException {
		AccelerationData a;
		ModID modID = status.getModID();

		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			if (l instanceof SimulationComputationListener) {
				a = ((SimulationComputationListener) l).postAccelerationCalculation(status, acceleration);
				if (modID != status.getModID()) {
					warn(status, l);
					modID = status.getModID();
				}
				if (a != null && !a.equals(acceleration)) {
					warn(status, l);
					acceleration = a;
				}
			}
		}
		return acceleration;
	}

	private static void warn(SimulationStatus status, SimulationListener listener) {
		if (!listener.isSystemListener()) {
			log.info("Non-system listener " + listener + " affected the simulation");
			status.addWarning(Warning.LISTENERS_AFFECTED);
		}
	}
}
