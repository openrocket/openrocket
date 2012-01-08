package net.sf.openrocket.optimization.general;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.sf.openrocket.util.BugException;

/**
 * An implementation of a ParallelFunctionCache that evaluates function values
 * in parallel and caches them.  This allows pre-calculating possibly required
 * function values beforehand.  If values are not required after all, the
 * computation can be aborted assuming the function evaluation supports it.
 * <p>
 * Note that while this class handles threads and abstracts background execution,
 * the public methods themselves are NOT thread-safe and should be called from
 * only one thread at a time.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ParallelExecutorCache implements ParallelFunctionCache {
	
	private final Map<Point, Double> functionCache = new HashMap<Point, Double>();
	private final Map<Point, Future<Double>> futureMap = new HashMap<Point, Future<Double>>();
	
	private ExecutorService executor;
	
	private Function function;
	
	
	/**
	 * Construct a cache that uses the same number of computational threads as there are
	 * processors available.
	 */
	public ParallelExecutorCache() {
		this(Runtime.getRuntime().availableProcessors());
	}
	
	/**
	 * Construct a cache that uses the specified number of computational threads for background
	 * computation.  The threads that are created are marked as daemon threads.
	 * 
	 * @param threadCount	the number of threads to use in the executor.
	 */
	public ParallelExecutorCache(int threadCount) {
		this(new ThreadPoolExecutor(threadCount, threadCount, 60, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(),
				new ThreadFactory() {
					@Override
					public Thread newThread(Runnable r) {
						Thread t = new Thread(r);
						t.setDaemon(true);
						return t;
					}
				}));
	}
	
	/**
	 * Construct a cache that uses the specified ExecutorService for managing
	 * computational threads.
	 * 
	 * @param executor	the executor to use for function evaluations.
	 */
	public ParallelExecutorCache(ExecutorService executor) {
		this.executor = executor;
	}
	
	

	@Override
	public void compute(Collection<Point> points) {
		for (Point p : points) {
			compute(p);
		}
	}
	
	
	@Override
	public void compute(Point point) {
		
		if (isOutsideRange(point)) {
			// Point is outside of range
			return;
		}
		
		if (functionCache.containsKey(point)) {
			// Function has already been evaluated at the point
			return;
		}
		
		if (futureMap.containsKey(point)) {
			// Function is being evaluated at the point
			return;
		}
		
		// Submit point for evaluation
		FunctionCallable callable = new FunctionCallable(function, point);
		Future<Double> future = executor.submit(callable);
		futureMap.put(point, future);
	}
	
	
	@Override
	public void waitFor(Collection<Point> points) throws InterruptedException, OptimizationException {
		for (Point p : points) {
			waitFor(p);
		}
	}
	
	
	@Override
	public void waitFor(Point point) throws InterruptedException, OptimizationException {
		if (isOutsideRange(point)) {
			return;
		}
		
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
			Throwable cause = e.getCause();
			if (cause instanceof InterruptedException) {
				throw (InterruptedException) cause;
			}
			if (cause instanceof OptimizationException) {
				throw (OptimizationException) cause;
			}
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			}
			
			throw new BugException("Function threw unknown exception while processing", e);
		}
	}
	
	

	@Override
	public List<Point> abort(Collection<Point> points) {
		List<Point> computed = new ArrayList<Point>(Math.min(points.size(), 10));
		
		for (Point p : points) {
			if (abort(p)) {
				computed.add(p);
			}
		}
		
		return computed;
	}
	
	

	@Override
	public boolean abort(Point point) {
		if (isOutsideRange(point)) {
			return false;
		}
		
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
	
	
	@Override
	public void abortAll() {
		Iterator<Point> iterator = futureMap.keySet().iterator();
		while (iterator.hasNext()) {
			Point point = iterator.next();
			Future<Double> future = futureMap.get(point);
			iterator.remove();
			
			if (future.isDone()) {
				// Evaluation has been completed, store value in cache
				try {
					double value = future.get();
					functionCache.put(point, value);
				} catch (Exception e) {
					// Ignore
				}
			} else {
				// Cancel the evaluation
				future.cancel(true);
			}
		}
	}
	
	
	@Override
	public double getValue(Point point) {
		if (isOutsideRange(point)) {
			return Double.MAX_VALUE;
		}
		
		Double d = functionCache.get(point);
		if (d == null) {
			throw new IllegalStateException(point + " is not in function cache.  " +
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
	
	
	/**
	 * Check whether a point is outside of the valid optimization range.
	 */
	private boolean isOutsideRange(Point p) {
		int n = p.dim();
		for (int i = 0; i < n; i++) {
			double d = p.get(i);
			// Include NaN in disallowed range
			if (!(d >= 0.0 && d <= 1.0)) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * A Callable that evaluates a function at a specific point and returns the result.
	 */
	private class FunctionCallable implements Callable<Double> {
		private final Function calledFunction;
		private final Point point;
		
		public FunctionCallable(Function function, Point point) {
			this.calledFunction = function;
			this.point = point;
		}
		
		@Override
		public Double call() throws InterruptedException, OptimizationException {
			return calledFunction.evaluate(point);
		}
	}
	

}
