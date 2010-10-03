package net.sf.openrocket.optimization.rocketoptimization;

import net.sf.openrocket.document.Simulation;

/**
 * A parameter of a rocket or simulation that can be optimized
 * (for example max. altitude or velocity).
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface RocketOptimizationParameter {
	
	/**
	 * Return the label name for this optimization parameter.
	 * 
	 * @return	the name for the optimization parameter (e.g. "Flight altitude")
	 */
	public String getName();
	
	/**
	 * Compute the value for this optimization parameter for the simulation.
	 * The return value can be any double value.
	 * 
	 * @param simulation	the simulation
	 * @return				the parameter value (any double value)
	 */
	public double computeValue(Simulation simulation);
	
}
