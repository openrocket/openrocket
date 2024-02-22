package info.openrocket.core.optimization.rocketoptimization;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.optimization.general.OptimizationException;
import info.openrocket.core.unit.UnitGroup;

/**
 * A parameter of a rocket or simulation that can be optimized
 * (for example max. altitude or velocity).
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface OptimizableParameter {

	/**
	 * Return the label name for this optimization parameter.
	 * 
	 * @return the name for the optimization parameter (e.g. "Flight altitude")
	 */
	public String getName();

	/**
	 * Compute the value for this optimization parameter for the simulation.
	 * The return value can be any double value.
	 * <p>
	 * This method can return NaN in case of a problem computing
	 * 
	 * @param simulation the simulation
	 * @return the parameter value (any double value)
	 * @throws OptimizationException if an error occurs preventing the optimization
	 *                               from continuing
	 */
	public double computeValue(Simulation simulation) throws OptimizationException, InterruptedException;

	/**
	 * Return the unit group associated with the computed value.
	 * 
	 * @return the unit group of the computed value.
	 */
	public UnitGroup getUnitGroup();

}
