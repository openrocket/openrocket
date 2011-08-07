package net.sf.openrocket.optimization.rocketoptimization.domains;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.optimization.rocketoptimization.SimulationDomain;
import net.sf.openrocket.unit.Value;
import net.sf.openrocket.util.Pair;

/**
 * A simulation domain that includes all points in the domain.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class IdentitySimulationDomain implements SimulationDomain {
	
	@Override
	public Pair<Double, Value> getDistanceToDomain(Simulation simulation) {
		return new Pair<Double, Value>(-1.0, null);
	}
	
}
