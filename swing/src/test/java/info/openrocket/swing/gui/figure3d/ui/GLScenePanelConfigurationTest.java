package info.openrocket.swing.gui.figure3d.ui;

import org.junit.jupiter.api.Test;
import org.lwjgl.opengl.awt.GLData;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GLScenePanelConfigurationTest {

	@Test
	void requestsTheHighestDesktopContextAtOrAboveOpenGL31() {
		GLData data = GLScenePanel.createGLData();

		assertEquals(GLData.API.GL, data.api);
		assertEquals(3, data.majorVersion);
		assertEquals(1, data.minorVersion);
		assertEquals(GLData.VersionPolicy.AT_LEAST, data.versionPolicy);
		assertNull(data.profile, "OpenGL 3.1 predates profile selection");
	}

	@Test
	void retriesContextCreationWithoutOptionalRobustness() {
		GLData data = robustContextData();
		AtomicLong context = new AtomicLong();
		AtomicInteger attempts = new AtomicInteger();

		GLScenePanel.runWithOptionalRobustnessFallback(data, context::get, () -> {
			if (attempts.incrementAndGet() == 1) {
				throw new RuntimeException("robust context unavailable");
			}
			context.set(42L);
		});

		assertEquals(2, attempts.get());
		assertEquals(42L, context.get());
		assertFalse(data.robustness);
		assertFalse(data.loseContextOnReset);
		assertFalse(data.contextResetIsolation);
	}

	@Test
	void doesNotRetryFailureAfterAContextWasCreated() {
		GLData data = robustContextData();
		AtomicLong context = new AtomicLong();
		AtomicInteger attempts = new AtomicInteger();
		RuntimeException failure = new RuntimeException("could not make context current");

		RuntimeException thrown = assertThrows(RuntimeException.class,
				() -> GLScenePanel.runWithOptionalRobustnessFallback(data, context::get, () -> {
					attempts.incrementAndGet();
					context.set(42L);
					throw failure;
				}));

		assertSame(failure, thrown);
		assertEquals(1, attempts.get());
		assertEquals(42L, context.get());
		assertTrue(data.robustness);
	}

	@Test
	void reportsFallbackFailureAndRetainsRobustAttempt() {
		GLData data = robustContextData();
		AtomicInteger attempts = new AtomicInteger();
		RuntimeException robustFailure = new RuntimeException("robust context unavailable");
		RuntimeException fallbackFailure = new RuntimeException("OpenGL 3.1 unavailable");

		RuntimeException thrown = assertThrows(RuntimeException.class,
				() -> GLScenePanel.runWithOptionalRobustnessFallback(data, () -> 0L, () -> {
					if (attempts.incrementAndGet() == 1) {
						throw robustFailure;
					}
					throw fallbackFailure;
				}));

		assertSame(fallbackFailure, thrown);
		assertEquals(2, attempts.get());
		assertEquals(1, thrown.getSuppressed().length);
		assertSame(robustFailure, thrown.getSuppressed()[0]);
	}

	private static GLData robustContextData() {
		GLData data = new GLData();
		data.robustness = true;
		data.loseContextOnReset = true;
		data.contextResetIsolation = true;
		return data;
	}
}
