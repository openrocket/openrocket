package net.sf.openrocket.optimization.rocketoptimization;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.unit.Value;
import net.sf.openrocket.util.Pair;

/**
 * An interface defining a function domain which limits allowed function values.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface SimulationDomain {
	
	/**
	 * Return a value determining whether the simulation is within the domain limits
	 * of an optimization process.  If the returned value is negative or zero, the
	 * simulation is within the domain; if the value is positive, the returned value
	 * is an indication of how far from the domain the value is; if the returned value
	 * is NaN, the simulation is outside of the domain.
	 * 
	 * @param simulation	the simulation to check.
	 * @return				A pair indicating the status.  The first element is the reference value,
	 * 						a negative value or zero if the simulation is in the domain,
	 * 						a positive value or NaN if not.  The second is the value
	 * 						indication of the domain (may be null).
	 */
	public Pair<Double, Value> getDistanceToDomain(Simulation simulation);
	

}
