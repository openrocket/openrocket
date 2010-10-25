package net.sf.openrocket.optimization.general;

/**
 * An interface for a function optimization algorithm.  The function is evaluated
 * via a function cache.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface FunctionOptimizer {
	
	/**
	 * Perform optimization on the function.  The optimization control is called to control
	 * when optimization is stopped.
	 * 
	 * @param initial	the initial start point of the optimization.
	 * @param control	the optimization control.
	 * @throws OptimizationException	if an error occurs that prevents optimization
	 */
	public void optimize(Point initial, OptimizationController control) throws OptimizationException;
	
	
	/**
	 * Return the optimum point computed by {@link #optimize(Point, OptimizationController)}.
	 * 
	 * @return	the optimum point value.
	 * @throws IllegalStateException	if {@link #optimize(Point, OptimizationController)} has not been called.
	 */
	public Point getOptimumPoint();
	
	/**
	 * Return the function value at the optimum point.
	 * 
	 * @return	the value at the optimum point.
	 * @throws IllegalStateException	if {@link #optimize(Point, OptimizationController)} has not been called.
	 */
	public double getOptimumValue();
	
	
	/**
	 * Return the function cache used by this optimization algorithm.
	 * 
	 * @return	the function cache.
	 */
	public FunctionCache getFunctionCache();
	
	/**
	 * Set the function cache that provides the function values for this algorithm.
	 * Some algorithms may require the function cache to be an instance of
	 * ParallelFunctionCache.
	 * 
	 * @param functionCache		the function cache.
	 */
	public void setFunctionCache(FunctionCache functionCache);
	

}
