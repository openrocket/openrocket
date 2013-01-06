package net.sf.openrocket.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.sf.openrocket.optimization.general.Function;
import net.sf.openrocket.optimization.general.FunctionOptimizer;
import net.sf.openrocket.optimization.general.OptimizationController;
import net.sf.openrocket.optimization.general.OptimizationException;
import net.sf.openrocket.optimization.general.ParallelExecutorCache;
import net.sf.openrocket.optimization.general.Point;
import net.sf.openrocket.optimization.general.multidim.MultidirectionalSearchOptimizer;
import net.sf.openrocket.util.MathUtil;


public class TestFunctionOptimizerLoop {
	
	private static final double PRECISION = 0.01;
	
	private int stepCount = 0;
	private int evaluations = 0;
	
	private void go(final FunctionOptimizer optimizer, final Point optimum, final int maxSteps, ExecutorService executor) throws OptimizationException {
		
		Function function = new Function() {
			@Override
			public double evaluate(Point p) throws InterruptedException {
				evaluations++;
				return p.sub(optimum).length2();
			}
		};
		
		OptimizationController control = new OptimizationController() {
			
			@Override
			public boolean stepTaken(Point oldPoint, double oldValue, Point newPoint, double newValue, double stepSize) {
				stepCount++;
				if (stepCount % 1000 == 0) {
					System.err.println("WARNING: Over " + stepCount + " steps required for optimum=" + optimum +
								" position=" + newPoint);
				}
				double distance = newPoint.sub(optimum).length();
				return distance >= PRECISION;
			}
		};
		;
		
		ParallelExecutorCache cache = new ParallelExecutorCache(executor);
		cache.setFunction(function);
		optimizer.setFunctionCache(cache);
		optimizer.optimize(new Point(optimum.dim(), 0.5), control);
	}
	
	
	public static void main(String[] args) throws OptimizationException {
		
		System.err.println("PRECISION = " + PRECISION);
		
		ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100));
		
		for (int dim = 1; dim <= 10; dim++) {
			
			List<Integer> stepCount = new ArrayList<Integer>();
			List<Integer> functionCount = new ArrayList<Integer>();
			
			MultidirectionalSearchOptimizer optimizer = new MultidirectionalSearchOptimizer();
			for (int count = 0; count < 200; count++) {
				TestFunctionOptimizerLoop test = new TestFunctionOptimizerLoop();
				double[] point = new double[dim];
				for (int i = 0; i < dim; i++) {
					point[i] = Math.random();
				}
				//				point[0] = 0.7;
				test.go(optimizer, new Point(point), 20, executor);
				stepCount.add(test.stepCount);
				functionCount.add(test.evaluations);
			}
			
			//			System.err.println("StepCount = " + stepCount);
			
			System.out.printf("dim=%d  Steps avg=%5.2f dev=%5.2f median=%.1f  " +
					"Evaluations avg=%5.2f dev=%5.2f median=%.1f\n",
					dim, MathUtil.average(stepCount), MathUtil.stddev(stepCount), MathUtil.median(stepCount),
					MathUtil.average(functionCount), MathUtil.stddev(functionCount), MathUtil.median(functionCount));
			System.out.println("stat: " + optimizer.getStatistics());
			
		}
		
		executor.shutdownNow();
	}
	


}
