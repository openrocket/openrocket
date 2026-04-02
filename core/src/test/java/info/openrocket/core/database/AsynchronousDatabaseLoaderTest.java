package info.openrocket.core.database;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class AsynchronousDatabaseLoaderTest {

	@Test
	public void testBlockUntilLoadedReturnsAfterLoadFailure() throws InterruptedException {
		CountDownLatch failureHandled = new CountDownLatch(1);
		Thread.UncaughtExceptionHandler originalHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable throwable) {
				failureHandled.countDown();
			}
		});

		try {
			FailingLoader loader = new FailingLoader();
			CountDownLatch waiterReleased = new CountDownLatch(1);

			loader.startLoading();

			Thread waiter = new Thread(new Runnable() {
				@Override
				public void run() {
					loader.blockUntilLoaded();
					waiterReleased.countDown();
				}
			});
			waiter.start();

			assertTrue(failureHandled.await(2, TimeUnit.SECONDS),
					"The background loading failure should reach the uncaught exception handler");
			assertTrue(waiterReleased.await(2, TimeUnit.SECONDS),
					"Waiting threads should be released even when loading fails");
			assertTrue(loader.isLoaded(), "Loader should report completion after a failed load");
			waiter.join(2_000);
		} finally {
			Thread.setDefaultUncaughtExceptionHandler(originalHandler);
		}
	}

	private static final class FailingLoader extends AsynchronousDatabaseLoader {
		private FailingLoader() {
			super(0);
		}

		@Override
		protected void loadDatabase() {
			throw new IllegalStateException("Simulated background load failure");
		}
	}
}
