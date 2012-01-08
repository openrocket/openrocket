package net.sf.openrocket.optimization.general;

/**
 * A storage of cached values of a function.  The purpose of this class is to
 * cache function values between optimization runs.  Subinterfaces may provide
 * additional functionality.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface FunctionCache {
	
	/**
	 * Compute and return the value of the function at the specified point.
	 * 
	 * @param point		the point at which to evaluate.
	 * @return			the value of the function at that point.
	 */
	public double getValue(Point point);
	
	/**
	 * Clear the cache.
	 */
	public void clearCache();
	
	/**
	 * Return the function that is evaluated by this cache implementation.
	 * 
	 * @return	the function that is being evaluated.
	 */
	public Function getFunction();
	
	/**
	 * Set the function that is evaluated by this cache implementation.
	 * 
	 * @param function	the function that is being evaluated.
	 */
	public void setFunction(Function function);
	
}
