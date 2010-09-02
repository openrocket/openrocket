package net.sf.openrocket.optimization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An OptimizationController that delegates control actions to multiple other controllers.
 * The optimization is stopped if any of the controllers stops it.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class MultipleOptimizationController implements OptimizationController {
	
	private final List<OptimizationController> controllers = new ArrayList<OptimizationController>();
	
	public MultipleOptimizationController(OptimizationController... controllers) {
		for (OptimizationController c : controllers) {
			this.controllers.add(c);
		}
	}
	
	public MultipleOptimizationController(Collection<OptimizationController> controllers) {
		this.controllers.addAll(controllers);
	}
	
	
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
