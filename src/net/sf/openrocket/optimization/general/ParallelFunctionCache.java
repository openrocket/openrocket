package net.sf.openrocket.optimization.general;

import java.util.Collection;
import java.util.List;

/**
 * A FunctionCache that allows scheduling points to be computed in the background,
 * waiting for specific points to become computed or aborting the computation of
 * points.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface ParallelFunctionCache extends FunctionCache {
	
	/**
	 * Schedule a list of function evaluations at the specified points.
	 * The points are added to the end of the computation queue in the order
	 * they are returned by the iterator.
	 * 
	 * @param points	the points at which to evaluate the function.
	 */
	public void compute(Collection<Point> points);
	
	/**
	 * Schedule function evaluation for the specified point.  The point is
	 * added to the end of the computation queue.
	 * 
	 * @param point		the point at which to evaluate the function.
	 */
	public void compute(Point point);
	
	/**
	 * Wait for a collection of points to be computed.  After calling this method
	 * the function values are available by calling {@link #getValue(Point)}.
	 * 
	 * @param points	the points to wait for.
	 * @throws InterruptedException		if this thread or the computing thread was interrupted while waiting.
	 * @throws OptimizationException 	if an error preventing continuing the optimization occurs.
	 */
	public void waitFor(Collection<Point> points) throws InterruptedException, OptimizationException;
	
	/**
	 * Wait for a point to be computed.  After calling this method
	 * the function value is available by calling {@link #getValue(Point)}.
	 * 
	 * @param point		the point to wait for.
	 * @throws InterruptedException		if this thread or the computing thread was interrupted while waiting.
	 * @throws OptimizationException 	if an error preventing continuing the optimization occurs.
	 */
	public void waitFor(Point point) throws InterruptedException, OptimizationException;
	
	
	/**
	 * Abort the computation of the specified points.  If computation has ended,
	 * the result is stored in the function cache anyway.
	 * 
	 * @param points	the points to abort.
	 * @return			a list of the points that have been computed anyway
	 */
	public List<Point> abort(Collection<Point> points);
	
	
	/**
	 * Abort the computation of the specified point.  If computation has ended,
	 * the result is stored in the function cache anyway.
	 * 
	 * @param point		the point to abort.
	 * @return			<code>true</code> if the point has been computed anyway, <code>false</code> if not.
	 */
	public boolean abort(Point point);
	
	
	/**
	 * Abort the computation of all still unexecuted points.
	 */
	public void abortAll();
}
