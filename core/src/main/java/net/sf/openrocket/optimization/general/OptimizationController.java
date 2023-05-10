package net.sf.openrocket.optimization.general;

public interface OptimizationController {
	
	/**
	 * Control for whether to continue the optimization.  This method is called after
	 * every full step taken by the optimization algorithm.
	 * 
	 * @param oldPoint	the old position.
	 * @param oldValue	the value of the function at the old position.
	 * @param newPoint	the new position.
	 * @param newValue	the value of the function at the new position.
	 * @param stepSize	the step length that is used to search for smaller function values when applicable, or NaN.
	 * @return			<code>true</code> to continue optimization, <code>false</code> to stop.
	 */
	public boolean stepTaken(Point oldPoint, double oldValue, Point newPoint, double newValue,
			double stepSize);
	
}
