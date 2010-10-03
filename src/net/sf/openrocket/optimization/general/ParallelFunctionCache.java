package net.sf.openrocket.optimization.general;

import java.util.Collection;
import java.util.List;

/**
 * A FunctionCache that allows queuing points to be computed in the background,
 * waiting for specific points to become computed or aborting the computation of
 * points.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface ParallelFunctionCache extends FunctionCache {
	
	/**
	 * Queue a list of function evaluations at the specified points.
	 * 
	 * @param points	the points at which to evaluate the function.
	 */
	public void compute(Collection<Point> points);
	
	/**
	 * Queue function evaluation for the specified point.
	 * 
	 * @param point		the point at which to evaluate the function.
	 */
	public void compute(Point point);
	
	/**
	 * Wait for a collection of points to be computed.  After calling this method
	 * the function values are available by calling XXX
	 * 
	 * @param points	the points to wait for.
	 * @throws InterruptedException		if this thread was interrupted while waiting.
	 */
	public void waitFor(Collection<Point> points) throws InterruptedException;
	
	/**
	 * Wait for a point to be computed.  After calling this method
	 * the function values are available by calling XXX
	 * 
	 * @param point		the point to wait for.
	 * @throws InterruptedException		if this thread was interrupted while waiting.
	 */
	public void waitFor(Point point) throws InterruptedException;
	
	
	/**
	 * Abort the computation of the specified point.  If computation has ended,
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
}
