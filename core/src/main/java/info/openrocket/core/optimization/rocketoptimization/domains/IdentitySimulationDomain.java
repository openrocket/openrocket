package info.openrocket.core.optimization.rocketoptimization.domains;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.optimization.rocketoptimization.SimulationDomain;
import info.openrocket.core.unit.Value;
import info.openrocket.core.util.Pair;

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
