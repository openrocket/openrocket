package info.openrocket.core.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import info.openrocket.core.startup.Application;
import info.openrocket.core.startup.ExceptionHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMutex {

	private static final Logger log = LoggerFactory.getLogger(TestMutex.class);

	@BeforeEach
	public void setup() {
		System.setProperty("openrocket.debug.safetycheck", "true");

		// Set exception handler that does nothing (called by SafetyMutex)
		Application.setExceptionHandler(new ExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable throwable) {
			}

			@Override
			public void handleErrorCondition(Throwable exception) {
			}

			@Override
			public void handleErrorCondition(String message, Throwable exception) {
			}

			@Override
			public void handleErrorCondition(String message) {
			}
		});
	}

	@Test
	public void testSingleLocking() {
		SafetyMutex.ConcreteSafetyMutex m = new SafetyMutex.ConcreteSafetyMutex();

		// Test single locking
		assertNull(m.lockingThread);
		m.verify();
		m.lock("here");
		assertNotNull(m.lockingThread);
		assertTrue(m.unlock("here"));

	}

	@Test
	public void testDoubleLocking() {
		SafetyMutex.ConcreteSafetyMutex m = new SafetyMutex.ConcreteSafetyMutex();

		// Test double locking
		m.verify();
		m.lock("foobar");
		m.verify();
		m.lock("bazqux");
		m.verify();
		assertTrue(m.unlock("bazqux"));
		m.verify();
		assertTrue(m.unlock("foobar"));
		m.verify();
	}

	@Test
	public void testDoubleUnlocking() {
		SafetyMutex.ConcreteSafetyMutex m = new SafetyMutex.ConcreteSafetyMutex();
		// Mark error reported to not init exception handler
		SafetyMutex.ConcreteSafetyMutex.errorReported = true;

		m.lock("here");

		boolean unlocked = m.unlock("here");
		assertTrue(unlocked, "First unlock failed but should have succeeded.");

		log.error(
				"***** The following ConcurrencyException in testDoubleUnlocking() is expected, but this test can't prevent it from being logged. *****");
		unlocked = m.unlock("here");
		assertFalse(unlocked, "Second unlock succeeded but should have failed.");
	}

	private volatile int testState = 0;
	private volatile String failure = null;

	@Test
	@Timeout(1000)
	public void testThreadingErrors() {
		final SafetyMutex.ConcreteSafetyMutex m = new SafetyMutex.ConcreteSafetyMutex();

		// Initialize and start the thread
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {

					// Test locking a locked mutex
					waitFor(1);
					try {
						log.error(
								"***** The following ConcurrencyException in testThreadingErrors() is expected, but this test can't prevent it from being logged. *****");
						m.lock("in thread one");
						failure = "Succeeded in locking a mutex locked by a different thread";
						return;
					} catch (ConcurrencyException e) {
						// OK
					}

					// Test unlocking a mutex locked by a different thread
					log.error(
							"***** The following ConcurrencyException in testThreadingErrors() is expected, but this test can't prevent it from being logged. *****");
					if (m.unlock("in thread two")) {
						failure = "Succeeded in unlocking a mutex locked by a different thread";
						return;
					}

					// Test verifying a locked mutex that already has an error
					try {
						log.error(
								"***** The following ConcurrencyException in testThreadingErrors() is expected, but this test can't prevent it from being logged. *****");
						m.verify();
						failure = "Succeeded in verifying a mutex locked by a different thread";
						return;
					} catch (ConcurrencyException e) {
						// OK
					}

					// Test locking a mutex after it's been unlocked
					testState = 2;
					waitFor(3);
					m.lock("in thread three");
					m.verify();

					// Wait for other side to test
					testState = 4;
					waitFor(5);

					// Exit code
					testState = 6;

				} catch (Exception e) {
					e.printStackTrace();
					failure = "Exception occurred in thread: " + e;
					return;
				}

			}
		};
		thread.setDaemon(true);
		thread.start();

		m.lock("one");
		testState = 1;

		waitFor(2);
		assertNull(failure, "Thread error: " + failure);

		m.verify();
		m.unlock("one");
		testState = 3;

		waitFor(4);
		assertNull(failure, "Thread error: " + failure);

		try {
			log.error(
					"***** The following ConcurrencyException in testThreadingErrors() is expected, but this test can't prevent it from being logged. *****");
			m.lock("two");
			fail("Succeeded in locking a locked mutex in main thread");
		} catch (ConcurrencyException e) {
			// OK
		}

		// Test unlocking a mutex locked by a different thread
		log.error(
				"***** The following ConcurrencyException in testThreadingErrors() is expected, but this test can't prevent it from being logged. *****");
		assertFalse(m.unlock("here"));

		try {
			log.error(
					"***** The following ConcurrencyException in testThreadingErrors() is expected, but this test can't prevent it from being logged. *****");
			m.verify();
			fail("Succeeded in verifying a locked mutex in main thread");
		} catch (ConcurrencyException e) {
			// OK
		}

		testState = 5;
		waitFor(6);
		assertNull(failure, "Thread error: " + failure);
	}

	private void waitFor(int state) {
		while (testState != state && failure == null) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
	}

	public void testBogusMutex() {
		SafetyMutex m = new SafetyMutex.BogusSafetyMutex();
		m.lock("foo");
		m.lock("bar");
		m.lock("baz");
		m.verify();
		m.unlock("a");
		m.unlock(null);
		m.unlock("");
		m.unlock("c");
	}

}
