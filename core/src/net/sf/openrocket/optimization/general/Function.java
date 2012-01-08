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
	 * @throws OptimizationException	if an error occurs that prevents the optimization
	 */
	public double evaluate(Point point) throws InterruptedException, OptimizationException;
	
}
