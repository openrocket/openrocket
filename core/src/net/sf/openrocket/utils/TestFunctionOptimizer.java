package net.sf.openrocket.utils;

import net.sf.openrocket.optimization.general.Function;
import net.sf.openrocket.optimization.general.FunctionOptimizer;
import net.sf.openrocket.optimization.general.OptimizationController;
import net.sf.openrocket.optimization.general.OptimizationException;
import net.sf.openrocket.optimization.general.ParallelExecutorCache;
import net.sf.openrocket.optimization.general.ParallelFunctionCache;
import net.sf.openrocket.optimization.general.Point;
import net.sf.openrocket.optimization.general.multidim.MultidirectionalSearchOptimizer;




public class TestFunctionOptimizer {
	
	private static final int LOOP_COUNT = 1000000;
	
	private volatile int evaluations = 0;
	private volatile int aborted = 0;
	private volatile int stepCount = 0;
	
	

	private void go(final ParallelFunctionCache functionCache,
			final FunctionOptimizer optimizer, final Point optimum, final int maxSteps) throws OptimizationException {
		
		Function function = new Function() {
			@Override
			public double evaluate(Point p) throws InterruptedException {
				if (loop(LOOP_COUNT)) {
					evaluations++;
					return p.sub(optimum).length2();
				} else {
					aborted++;
					return Double.NaN;
				}
			}
		};
		
		OptimizationController control = new OptimizationController() {
			
			@Override
			public boolean stepTaken(Point oldPoint, double oldValue, Point newPoint, double newValue, double stepSize) {
				stepCount++;
				//				System.out.println("CSV " + count + ", " + evaluations + ", " + newPoint.sub(optimum).length());
				//				System.out.println("Steps: " + count + "  Function evaluations: " + evaluations);
				//				System.out.println("Distance: " + newPoint.sub(optimum).length() + "   " + newPoint + "  value=" + newValue);
				return stepCount < maxSteps;
			}
		};
		;
		
		functionCache.setFunction(function);
		optimizer.setFunctionCache(functionCache);
		optimizer.optimize(new Point(optimum.dim(), 0.5), control);
		System.err.println("Result: " + optimizer.getOptimumPoint() + "  value=" + optimizer.getOptimumValue());
		System.err.println("Steps: " + stepCount + " Evaluations: " + evaluations);
	}
	
	
	public static double counter;
	
	private static boolean loop(int count) {
		counter = 1.0;
		for (int i = 0; i < count; i++) {
			counter += Math.sin(counter);
			if (i % 1024 == 0) {
				if (Thread.interrupted()) {
					return false;
				}
			}
		}
		return true;
	}
	
	
	public static void main(String[] args) throws InterruptedException, OptimizationException {
		
		System.err.println("Number of processors: " + Runtime.getRuntime().availableProcessors());
		
		for (int i = 0; i < 20; i++) {
			long t0 = System.currentTimeMillis();
			loop(LOOP_COUNT);
			long t1 = System.currentTimeMillis();
			System.err.println("Loop delay at startup: " + (t1 - t0) + "ms");
		}
		System.err.println();
		
		for (int threadCount = 1; threadCount <= 10; threadCount++) {
			
			System.err.println("THREAD COUNT:  " + threadCount);
			TestFunctionOptimizer test = new TestFunctionOptimizer();
			
			ParallelExecutorCache executor = new ParallelExecutorCache(threadCount);
			MultidirectionalSearchOptimizer optimizer = new MultidirectionalSearchOptimizer();
			long t0 = System.currentTimeMillis();
			test.go(executor, optimizer, new Point(0.2, 0.3, 0.85), 30);
			long t1 = System.currentTimeMillis();
			
			System.err.println("Optimization took " + (t1 - t0) + "ms");
			System.err.println("" + test.stepCount + " steps, " + test.evaluations +
					" function evaluations, " + test.aborted + " aborted evaluations");
			System.err.println("Statistics: " + optimizer.getStatistics());
			
			executor.getExecutor().shutdownNow();
			Thread.sleep(1000);
			
			t0 = System.currentTimeMillis();
			loop(LOOP_COUNT);
			t1 = System.currentTimeMillis();
			System.err.println("Loop delay afterwards: " + (t1 - t0) + "ms");
			System.err.println();
		}
	}
	


}
