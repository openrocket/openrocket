package info.openrocket.core.optimization.general;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import info.openrocket.core.utils.TestFunctionOptimizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FunctionOptimizerTest {

	private Method loopMethod;

	@BeforeEach
	void setUp() throws Exception {
		loopMethod = TestFunctionOptimizer.class.getDeclaredMethod("loop", int.class);
		loopMethod.setAccessible(true);
	}

	@Test
	void loopReturnsTrueWhenThreadNotInterrupted() throws Exception {
		boolean result = invokeLoop(16);
		assertTrue(result);
	}

	@Test
	void loopReturnsFalseWhenThreadInterrupted() throws Exception {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			Future<Boolean> future = executor.submit(new InterruptingLoop());
			assertFalse(future.get());
		} finally {
			executor.shutdownNow();
		}
	}

	private boolean invokeLoop(int count) throws Exception {
		try {
			return (Boolean) loopMethod.invoke(null, count);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getCause());
		}
	}

	private class InterruptingLoop implements Callable<Boolean> {
		@Override
		public Boolean call() throws Exception {
			Thread.currentThread().interrupt();
			return invokeLoop(2048);
		}
	}
}
