package net.sf.openrocket.simulation.listeners;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.Warning;
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
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;

/**
 * Helper methods for firing events to simulation listeners.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SimulationListenerHelper {
	
	private static final Logger log = LoggerFactory.getLogger(SimulationListenerHelper.class);
	
	////////  SimulationListener methods  ////////
	

	/**
	 * Fire startSimulation event.
	 */
	public static void fireStartSimulation(SimulationStatus status)
			throws SimulationException {
		int modID = status.getModID();
		
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
		int modID = status.getModID();
		
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
	 * @return	<code>true</code> to handle step normally, <code>false</code> to skip the step.
	 */
	public static boolean firePreStep(SimulationStatus status)
			throws SimulationException {
		boolean b;
		int modID = status.getModID();
		
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
		int modID = status.getModID();
		
		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			l.postStep(status);
			if (modID != status.getModID()) {
				warn(status, l);
				modID = status.getModID();
			}
		}
	}
	
	

	////////  SimulationEventListener methods  ////////
	
	/**
	 * Fire an add flight event event.
	 * 
	 * @return	<code>true</code> to add the event normally, <code>false</code> to skip adding the event.
	 */
	public static boolean fireAddFlightEvent(SimulationStatus status, FlightEvent event) throws SimulationException {
		boolean b;
		int modID = status.getModID();
		
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
	 * @return	<code>true</code> to handle the event normally, <code>false</code> to skip event.
	 */
	public static boolean fireHandleFlightEvent(SimulationStatus status, FlightEvent event) throws SimulationException {
		boolean b;
		int modID = status.getModID();
		
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
	 * @return	<code>true</code> to handle the event normally, <code>false</code> to skip event.
	 */
	public static boolean fireMotorIgnition(SimulationStatus status, MotorId motorId, MotorMount mount,
			MotorInstance instance) throws SimulationException {
		boolean b;
		int modID = status.getModID(); // Contains also motor instance
		
		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			if (l instanceof SimulationEventListener) {
				b = ((SimulationEventListener) l).motorIgnition(status, motorId, mount, instance);
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
	 * Fire recovery device deployment event.
	 * 
	 * @return	<code>true</code> to handle the event normally, <code>false</code> to skip event.
	 */
	public static boolean fireRecoveryDeviceDeployment(SimulationStatus status, RecoveryDevice device)
			throws SimulationException {
		boolean b;
		int modID = status.getModID(); // Contains also motor instance
		
		for (SimulationListener l : status.getSimulationConditions().getSimulationListenerList()) {
			if (l instanceof SimulationEventListener) {
				b = ((SimulationEventListener) l).recoveryDeviceDeployment(status, device);
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
	
	
	////////  SimulationComputationalListener methods  ////////
	
	/**
	 * Fire preAtmosphericModel event.
	 * 
	 * @return	<code>null</code> normally, or overriding atmospheric conditions.
	 */
	public static AtmosphericConditions firePreAtmosphericModel(SimulationStatus status)
			throws SimulationException {
		AtmosphericConditions conditions;
		int modID = status.getModID();
		
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
	 * @return	the atmospheric conditions to use.
	 */
	public static AtmosphericConditions firePostAtmosphericModel(SimulationStatus status, AtmosphericConditions conditions)
			throws SimulationException {
		AtmosphericConditions c;
		AtmosphericConditions clone = conditions.clone();
		int modID = status.getModID();
		
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
	 * @return	<code>null</code> normally, or overriding wind.
	 */
	public static Coordinate firePreWindModel(SimulationStatus status)
			throws SimulationException {
		Coordinate wind;
		int modID = status.getModID();
		
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
	 * @return	the wind to use.
	 */
	public static Coordinate firePostWindModel(SimulationStatus status, Coordinate wind) throws SimulationException {
		Coordinate w;
		int modID = status.getModID();
		
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
	 * @return	<code>NaN</code> normally, or overriding gravity.
	 */
	public static double firePreGravityModel(SimulationStatus status)
			throws SimulationException {
		double gravity;
		int modID = status.getModID();
		
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
	 * @return	the gravity to use.
	 */
	public static double firePostGravityModel(SimulationStatus status, double gravity) throws SimulationException {
		double g;
		int modID = status.getModID();
		
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
	 * @return	<code>null</code> normally, or overriding flight conditions.
	 */
	public static FlightConditions firePreFlightConditions(SimulationStatus status)
			throws SimulationException {
		FlightConditions conditions;
		int modID = status.getModID();
		
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
	 * @return	the flight conditions to use: either <code>conditions</code> or a new object
	 * 			containing the modified conditions.
	 */
	public static FlightConditions firePostFlightConditions(SimulationStatus status, FlightConditions conditions)
			throws SimulationException {
		FlightConditions c;
		FlightConditions clone = conditions.clone();
		int modID = status.getModID();
		
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
	 * @return	<code>null</code> normally, or overriding aerodynamic forces.
	 */
	public static AerodynamicForces firePreAerodynamicCalculation(SimulationStatus status)
			throws SimulationException {
		AerodynamicForces forces;
		int modID = status.getModID();
		
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
	 * @return	the aerodynamic forces to use.
	 */
	public static AerodynamicForces firePostAerodynamicCalculation(SimulationStatus status, AerodynamicForces forces)
			throws SimulationException {
		AerodynamicForces f;
		AerodynamicForces clone = forces.clone();
		int modID = status.getModID();
		
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
	 * @return	<code>null</code> normally, or overriding mass data.
	 */
	public static MassData firePreMassCalculation(SimulationStatus status)
			throws SimulationException {
		MassData mass;
		int modID = status.getModID();
		
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
	 * @return	the aerodynamic forces to use.
	 */
	public static MassData firePostMassCalculation(SimulationStatus status, MassData mass) throws SimulationException {
		MassData m;
		int modID = status.getModID();
		
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
	 * @return	<code>NaN</code> normally, or overriding thrust.
	 */
	public static double firePreThrustCalculation(SimulationStatus status)
			throws SimulationException {
		double thrust;
		int modID = status.getModID();
		
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
	 * @return	the thrust value to use.
	 */
	public static double firePostThrustCalculation(SimulationStatus status, double thrust) throws SimulationException {
		double t;
		int modID = status.getModID();
		
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
	 * @return	<code>null</code> normally, or overriding mass data.
	 */
	public static AccelerationData firePreAccelerationCalculation(SimulationStatus status) throws SimulationException {
		AccelerationData acceleration;
		int modID = status.getModID();
		
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
	 * @return	the aerodynamic forces to use.
	 */
	public static AccelerationData firePostAccelerationCalculation(SimulationStatus status,
			AccelerationData acceleration) throws SimulationException {
		AccelerationData a;
		int modID = status.getModID();
		
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
			status.getWarnings().add(Warning.LISTENERS_AFFECTED);
		}
	}
}
