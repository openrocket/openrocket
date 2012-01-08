package net.sf.openrocket.optimization.rocketoptimization;

import net.sf.openrocket.optimization.general.Point;
import net.sf.openrocket.unit.Value;

/**
 * A listener for the progress of rocket optimization.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface RocketOptimizationListener {
	
	/**
	 * Called after successful function evaluation.
	 * 
	 * @param point				the optimization point.
	 * @param state				the values to which the rocket has been modified in SI units, in the order of "point".
	 * @param domainReference	the domain reference description (or null if unavailable)
	 * @param parameterValue	the parameter value (or NaN if unavailable)
	 * @param goalValue			the goal value (return value of the function)
	 */
	public void evaluated(Point point, Value[] state, Value domainReference, Value parameterValue, double goalValue);
	
}
