package net.sf.openrocket.optimization.general;

/**
 * An interface defining an optimizable function.
 * <p>
 * Some function optimizers require that the function is thread-safe.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface Function {
	
	/**
	 * Evaluate the function at the specified point.
	 * <p>
	 * If the function evaluation is slow, then this method should abort the computation if
	 * the thread is interrupted.
	 * 
	 * @param point		the point at which to evaluate the function.
	 * @return			the function value.
	 * @throws InterruptedException		if the thread was interrupted before function evaluation was completed.
	 */
	public double evaluate(Point point) throws InterruptedException;
	
	
	/**
	 * Return a cached value of the function at the specified point.  This allows efficient
	 * caching of old values even between calls to optimization methods.  This method should
	 * NOT evaluate the function except in special cases (e.g. the point is outside of the
	 * function domain).
	 * <p>
	 * Note that it is allowed to always allowed to return <code>Double.NaN</code>, especially
	 * for functions that are fast to evaluate.
	 * 
	 * @param point		the point of function evaluation.
	 * @return			the function value, or <code>Double.NaN</code> if the function value has not been
	 * 					evaluated at this point.
	 */
	public double preComputed(Point point);
	
}
