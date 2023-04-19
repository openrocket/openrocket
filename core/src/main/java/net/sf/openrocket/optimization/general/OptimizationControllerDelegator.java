package net.sf.openrocket.optimization.general;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An OptimizationController that delegates control actions to multiple other controllers.
 * The optimization is stopped if any of the controllers stops it.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class OptimizationControllerDelegator implements OptimizationController {
	
	private final List<OptimizationController> controllers = new ArrayList<OptimizationController>();
	
	/**
	 * Construct the controlled based on an array of controllers.
	 * 
	 * @param controllers	the controllers to use.
	 */
	public OptimizationControllerDelegator(OptimizationController... controllers) {
		for (OptimizationController c : controllers) {
			this.controllers.add(c);
		}
	}
	
	/**
	 * Construct the controller based on a collection of controllers.
	 * 
	 * @param controllers	the controllers to use.
	 */
	public OptimizationControllerDelegator(Collection<OptimizationController> controllers) {
		this.controllers.addAll(controllers);
	}
	
	
	/**
	 * Control whether to continue optimization.  This method returns false if any of the
	 * used controllers returns false.  However, all controllers will be called even if
	 * an earlier one stops the optimization.
	 */
	@Override
	public boolean stepTaken(Point oldPoint, double oldValue, Point newPoint, double newValue, double stepSize) {
		boolean ret = true;
		
		for (OptimizationController c : controllers) {
			if (!c.stepTaken(oldPoint, oldValue, newPoint, newValue, stepSize)) {
				ret = false;
			}
		}
		
		return ret;
	}
	
}
