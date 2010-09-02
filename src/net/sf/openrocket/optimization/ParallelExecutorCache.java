package net.sf.openrocket.optimization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A class that evaluates function values in parallel and caches them.
 * This allows pre-calculating possibly required function values beforehand.
 * If values are not required after all, the computation can be aborted assuming
 * the function evaluation supports it.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ParallelExecutorCache implements ParallelFunctionCache {
	
	private final Map<Point, Double> functionCache = new HashMap<Point, Double>();
	private final Map<Point, Future<Double>> futureMap = new HashMap<Point, Future<Double>>();
	
	private ExecutorService executor;
	
	private Function function;
	
	

	public ParallelExecutorCache() {
		this(Runtime.getRuntime().availableProcessors());
	}
	
	public ParallelExecutorCache(int threadCount) {
		executor = new ThreadPoolExecutor(threadCount, threadCount, 60, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(),
				new ThreadFactory() {
					@Override
					public Thread newThread(Runnable r) {
						Thread t = new Thread(r);
						t.setDaemon(true);
						return t;
					}
				});
	}
	
	public ParallelExecutorCache(ExecutorService executor) {
		this.executor = executor;
	}
	
	

	/**
	 * Queue a list of function evaluations at the specified points.
	 * 
	 * @param points	the points at which to evaluate the function.
	 */
	public void compute(Collection<Point> points) {
		for (Point p : points) {
			compute(p);
		}
	}
	
	
	/**
	 * Queue function evaluation for the specified point.
	 * 
	 * @param point		the point at which to evaluate the function.
	 */
	public void compute(Point point) {
		if (functionCache.containsKey(point)) {
			// Function has already been evaluated at the point
			return;
		}
		
		if (futureMap.containsKey(point)) {
			// Function is being evaluated at the point
			return;
		}
		
		double value = function.preComputed(point);
		if (!Double.isNaN(value)) {
			// Function value was in function cache
			functionCache.put(point, value);
			return;
		}
		
		// Submit point for evaluation
		FunctionCallable callable = new FunctionCallable(function, point);
		Future<Double> future = executor.submit(callable);
		futureMap.put(point, future);
	}
	
	
	/**
	 * Wait for a collection of points to be computed.  After calling this method
	 * the function values are available by calling XXX
	 * 
	 * @param points	the points to wait for.
	 * @throws InterruptedException		if this thread was interrupted while waiting.
	 */
	public void waitFor(Collection<Point> points) throws InterruptedException {
		for (Point p : points) {
			waitFor(p);
		}
	}
	
	/**
	 * Wait for a point to be computed.  After calling this method
	 * the function values are available by calling XXX
	 * 
	 * @param point		the point to wait for.
	 * @throws InterruptedException		if this thread was interrupted while waiting.
	 */
	public void waitFor(Point point) throws InterruptedException {
		if (functionCache.containsKey(point)) {
			return;
		}
		
		Future<Double> future = futureMap.get(point);
		if (future == null) {
			throw new IllegalStateException("waitFor called for " + point + " but it is not being computed");
		}
		
		try {
			double value = future.get();
			functionCache.put(point, value);
		} catch (ExecutionException e) {
			throw new IllegalStateException("Function threw exception while processing", e.getCause());
		}
	}
	
	
	/**
	 * Abort the computation of the specified point.  If computation has ended,
	 * the result is stored in the function cache anyway.
	 * 
	 * @param points	the points to abort.
	 * @return			a list of the points that have been computed anyway
	 */
	public List<Point> abort(Collection<Point> points) {
		List<Point> computed = new ArrayList<Point>(Math.min(points.size(), 10));
		
		for (Point p : points) {
			if (abort(p)) {
				computed.add(p);
			}
		}
		
		return computed;
	}
	
	
	/**
	 * Abort the computation of the specified point.  If computation has ended,
	 * the result is stored in the function cache anyway.
	 * 
	 * @param point		the point to abort.
	 * @return			<code>true</code> if the point has been computed anyway, <code>false</code> if not.
	 */
	public boolean abort(Point point) {
		if (functionCache.containsKey(point)) {
			return true;
		}
		
		Future<Double> future = futureMap.remove(point);
		if (future == null) {
			throw new IllegalStateException("abort called for " + point + " but it is not being computed");
		}
		
		if (future.isDone()) {
			// Evaluation has been completed, store value in cache
			try {
				double value = future.get();
				functionCache.put(point, value);
				return true;
			} catch (Exception e) {
				return false;
			}
		} else {
			// Cancel the evaluation
			future.cancel(true);
			return false;
		}
	}
	
	
	public double getValue(Point point) {
		Double d = functionCache.get(point);
		if (d == null) {
			throw new IllegalStateException(point.toString() + " is not in function cache.  " +
					"functionCache=" + functionCache + "  futureMap=" + futureMap);
		}
		return d;
	}
	
	

	@Override
	public Function getFunction() {
		return function;
	}
	
	@Override
	public void setFunction(Function function) {
		this.function = function;
		clearCache();
	}
	
	@Override
	public void clearCache() {
		List<Point> list = new ArrayList<Point>(futureMap.keySet());
		abort(list);
		functionCache.clear();
	}
	
	public ExecutorService getExecutor() {
		return executor;
	}
	
}
