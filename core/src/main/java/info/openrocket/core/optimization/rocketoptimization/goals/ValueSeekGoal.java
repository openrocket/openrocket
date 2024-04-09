package info.openrocket.core.optimization.rocketoptimization.goals;

import info.openrocket.core.optimization.rocketoptimization.OptimizationGoal;

/**
 * An optimization goal that seeks for a specific function value.
 * The method returns the Euclidic distance from the desired value.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ValueSeekGoal implements OptimizationGoal {

	private final double goal;

	/**
	 * Sole constructor.
	 * 
	 * @param goal the function value to optimize towards.
	 */
	public ValueSeekGoal(double goal) {
		this.goal = goal;
	}

	@Override
	public double getMinimizationParameter(double value) {
		return Math.abs(value - goal);
	}

}
