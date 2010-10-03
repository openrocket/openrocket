package net.sf.openrocket.optimization.rocketoptimization;

import net.sf.openrocket.document.Simulation;

/**
 * An interface what modifies a single parameter in a rocket simulation
 * based on a double value in the range [0...1].
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface SimulationModifier {
	
	/**
	 * Modify the specified simulation to the corresponding parameter value.
	 * 
	 * @param simulation	the simulation to modify
	 * @param value			a value in the range [0...1]
	 */
	public void modify(Simulation simulation, double value);
	
}
