package info.openrocket.core.simulation.listeners;

import info.openrocket.core.aerodynamics.AerodynamicForces;
import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.masscalc.RigidBody;
import info.openrocket.core.models.atmosphere.AtmosphericConditions;
import info.openrocket.core.simulation.AccelerationData;
import info.openrocket.core.simulation.SimulationStatus;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.util.Coordinate;

/**
 * An interface containing listener callbacks relating to different
 * computational aspects performed
 * during flight.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface SimulationComputationListener extends SimulationListener {

	//////// Computation/modeling related callbacks ////////

	public AccelerationData preAccelerationCalculation(SimulationStatus status) throws SimulationException;

	public AccelerationData postAccelerationCalculation(SimulationStatus status, AccelerationData acceleration)
			throws SimulationException;

	public AtmosphericConditions preAtmosphericModel(SimulationStatus status)
			throws SimulationException;

	public AtmosphericConditions postAtmosphericModel(SimulationStatus status,
			AtmosphericConditions atmosphericConditions)
			throws SimulationException;

	public Coordinate preWindModel(SimulationStatus status) throws SimulationException;

	public Coordinate postWindModel(SimulationStatus status, Coordinate wind) throws SimulationException;

	public double preGravityModel(SimulationStatus status) throws SimulationException;

	public double postGravityModel(SimulationStatus status, double gravity) throws SimulationException;

	public FlightConditions preFlightConditions(SimulationStatus status)
			throws SimulationException;

	public FlightConditions postFlightConditions(SimulationStatus status, FlightConditions flightConditions)
			throws SimulationException;

	public AerodynamicForces preAerodynamicCalculation(SimulationStatus status)
			throws SimulationException;

	public AerodynamicForces postAerodynamicCalculation(SimulationStatus status, AerodynamicForces forces)
			throws SimulationException;

	public RigidBody preMassCalculation(SimulationStatus status) throws SimulationException;

	public RigidBody postMassCalculation(SimulationStatus status, RigidBody massData) throws SimulationException;

	public double preSimpleThrustCalculation(SimulationStatus status) throws SimulationException;

	public double postSimpleThrustCalculation(SimulationStatus status, double thrust) throws SimulationException;

}
