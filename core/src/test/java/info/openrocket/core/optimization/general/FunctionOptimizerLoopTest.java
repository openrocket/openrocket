package info.openrocket.core.optimization.general;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import info.openrocket.core.utils.TestFunctionOptimizerLoop;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FunctionOptimizerLoopTest {

	private ExecutorService executorService;
	private Method goMethod;

	@BeforeEach
	void setUp() throws Exception {
		executorService = Executors.newSingleThreadExecutor();
		goMethod = TestFunctionOptimizerLoop.class.getDeclaredMethod("go",
				info.openrocket.core.optimization.general.FunctionOptimizer.class,
				Point.class, int.class, ExecutorService.class);
		goMethod.setAccessible(true);
	}

	@AfterEach
	void tearDown() {
		executorService.shutdownNow();
	}

	@Test
	void goMethodTakesStepsTowardsOptimum() throws Exception {
		TestFunctionOptimizerLoop harness = new TestFunctionOptimizerLoop();
		Point optimum = new Point(0.2, 0.4);
		StubFunctionOptimizer optimizer = new StubFunctionOptimizer(optimum);

		invokeGo(harness, optimizer, optimum, 50);

		Point resultingOptimum = optimizer.getOptimumPoint();
		double distance = resultingOptimum.sub(optimum).length();
		assertTrue(distance < 0.01);

		int stepCount = getIntField(harness, "stepCount");
		int evaluations = getIntField(harness, "evaluations");

		assertTrue(stepCount > 0);
		assertTrue(evaluations >= stepCount);
	}

	private void invokeGo(TestFunctionOptimizerLoop harness, FunctionOptimizer optimizer,
			Point optimum, int maxSteps)
			throws IllegalAccessException, InvocationTargetException, OptimizationException {
		try {
			goMethod.invoke(harness, optimizer, optimum, maxSteps, executorService);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			if (cause instanceof OptimizationException) {
				throw (OptimizationException) cause;
			}
			throw e;
		}
	}

	private int getIntField(Object target, String name) throws NoSuchFieldException, IllegalAccessException {
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		return field.getInt(target);
	}

	private static class StubFunctionOptimizer implements FunctionOptimizer {
		private ParallelExecutorCache cache;
		private Point optimumPoint;
		private double optimumValue;
		private final Point target;

		StubFunctionOptimizer(Point target) {
			this.target = target;
		}

		@Override
		public void optimize(Point initial, OptimizationController control) throws OptimizationException {
			if (cache == null) {
				throw new IllegalStateException("Function cache not set");
			}
			Point current = initial;
			double currentValue = evaluate(cache, current);
			boolean keepGoing = true;

			while (keepGoing) {
				Point direction = target.sub(current);
				if (direction.length() == 0) {
					keepGoing = false;
					continue;
				}
				Point next = current.add(direction.mul(0.5));
				double nextValue = evaluate(cache, next);
				double stepSize = direction.length();
				keepGoing = control.stepTaken(current, currentValue, next, nextValue, stepSize);
				current = next;
				currentValue = nextValue;
			}

			this.optimumPoint = current;
			this.optimumValue = currentValue;
		}

		private double evaluate(ParallelExecutorCache cache, Point point) throws OptimizationException {
			try {
				return cache.getFunction().evaluate(point);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new OptimizationException(e);
			}
		}

		@Override
		public Point getOptimumPoint() {
			return optimumPoint;
		}

		@Override
		public double getOptimumValue() {
			return optimumValue;
		}

		@Override
		public FunctionCache getFunctionCache() {
			return cache;
		}

		@Override
		public void setFunctionCache(FunctionCache functionCache) {
			this.cache = (ParallelExecutorCache) functionCache;
		}
	}
}
