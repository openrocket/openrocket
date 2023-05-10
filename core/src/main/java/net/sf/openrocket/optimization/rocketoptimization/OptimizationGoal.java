package net.sf.openrocket.optimization.rocketoptimization;

/**
 * A goal for an optimization process, for example minimizing, maximizing or seeking
 * a specific parameter value.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface OptimizationGoal {
	
	/**
	 * Compute a value which, when minimized, yields the desired goal of the 
	 * optimization problem.
	 * 
	 * @param value		the function actual value
	 * @return			the value to minimize to reach the goal
	 */
	double getMinimizationParameter(double value);
	
}
