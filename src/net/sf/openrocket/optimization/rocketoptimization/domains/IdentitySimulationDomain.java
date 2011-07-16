package net.sf.openrocket.optimization.rocketoptimization.domains;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.optimization.rocketoptimization.SimulationDomain;
import net.sf.openrocket.util.Pair;

/**
 * A simulation domain that includes all points in the domain.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class IdentitySimulationDomain implements SimulationDomain {
	
	@Override
	public Pair<Double, Double> getDistanceToDomain(Simulation simulation) {
		return new Pair<Double, Double>(-1.0, Double.NaN);
	}
	
}
