package info.openrocket.swing.gui.figure3d;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class SharedCanvasRenderSchedulerTest {
	private static final long TIMEOUT_MS = 5_000;

	private final SharedCanvasRenderScheduler scheduler = SharedCanvasRenderScheduler.getInstance();

	@Test
	void coalescesImmediateRequestsAndConsumesDirtyState() {
		AtomicBoolean dirty = new AtomicBoolean(true);
		AtomicInteger renders = new AtomicInteger();
		SharedCanvasRenderScheduler.Client client = new SharedCanvasRenderScheduler.Client() {
			@Override
			public boolean isRenderActive() {
				return true;
			}

			@Override
			public boolean shouldRenderOnTick() {
				return dirty.getAndSet(false);
			}

			@Override
			public void renderScheduledFrame() {
				renders.incrementAndGet();
			}
		};

		scheduler.register(client);
		try {
			for (int i = 0; i < 10; i++) {
				scheduler.requestImmediate(client);
			}

			assertTrue(scheduler.awaitQuiescence(client, 1_000));
			assertEquals(1, renders.get());
			assertFalse(dirty.get());
		} finally {
			dispose(client);
		}
	}

	@Test
	void eachClientKeepsItsOwnRenderThread() throws Exception {
		TestClient first = new TestClient();
		TestClient second = new TestClient();
		scheduler.register(first);
		scheduler.register(second);
		try {
			Thread firstThread = first.renderOneFrame();
			Thread secondThread = second.renderOneFrame();
			assertNotSame(firstThread, secondThread);
			for (int i = 0; i < 3; i++) {
				assertSame(firstThread, first.renderOneFrame());
				assertSame(secondThread, second.renderOneFrame());
			}

			// Pausing rendering, as the design view does when switching to 2D, keeps the thread.
			scheduler.unregister(first);
			scheduler.register(first);
			assertSame(firstThread, first.renderOneFrame());
		} finally {
			dispose(first);
			dispose(second);
		}
	}

	@Test
	void disposalRunsOnTheRenderThreadAndTheNextCanvasGetsANewThread() throws Exception {
		TestClient client = new TestClient();
		scheduler.register(client);
		Thread oldThread = client.renderOneFrame();
		scheduler.unregister(client);

		AtomicReference<Thread> disposalThread = new AtomicReference<>();
		assertTrue(scheduler.disposeOnRenderThread(client,
				() -> disposalThread.set(Thread.currentThread()), TIMEOUT_MS));
		assertSame(oldThread, disposalThread.get(), "Canvas must be disposed on the thread that rendered it");
		oldThread.join(TIMEOUT_MS);
		assertFalse(oldThread.isAlive(), "Retired render thread should end");

		scheduler.register(client);
		try {
			assertNotSame(oldThread, client.renderOneFrame(),
					"Next canvas must not reuse the retired thread");
		} finally {
			dispose(client);
		}
	}

	@Test
	void disposingARegisteredClientContinuesRenderingOnANewThread() throws Exception {
		TestClient client = new TestClient();
		scheduler.register(client);
		try {
			Thread oldThread = client.renderOneFrame();
			assertTrue(scheduler.disposeOnRenderThread(client, () -> {
			}, TIMEOUT_MS));
			assertNotSame(oldThread, client.renderOneFrame());
		} finally {
			dispose(client);
		}
	}

	@Test
	void framesOfDifferentClientsNeverOverlap() throws Exception {
		AtomicInteger inside = new AtomicInteger();
		AtomicInteger maxInside = new AtomicInteger();
		Runnable slowFrame = () -> {
			maxInside.accumulateAndGet(inside.incrementAndGet(), Math::max);
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			inside.decrementAndGet();
		};
		TestClient first = new TestClient(slowFrame);
		TestClient second = new TestClient(slowFrame);
		scheduler.register(first);
		scheduler.register(second);
		try {
			for (int i = 0; i < 20; i++) {
				int firstFrames = first.requestFrame();
				int secondFrames = second.requestFrame();
				first.awaitFrameAfter(firstFrames);
				second.awaitFrameAfter(secondFrames);
			}
			assertEquals(1, maxInside.get(), "Frames of different canvases must be serialized");
		} finally {
			dispose(first);
			dispose(second);
		}
	}

	private void dispose(SharedCanvasRenderScheduler.Client client) {
		scheduler.unregister(client);
		scheduler.disposeOnRenderThread(client, () -> {
		}, TIMEOUT_MS);
	}

	private static final class TestClient implements SharedCanvasRenderScheduler.Client {
		private final AtomicBoolean dirty = new AtomicBoolean();
		private final List<Thread> renderThreads = new CopyOnWriteArrayList<>();
		private final Runnable onRender;

		private TestClient() {
			this(() -> {
			});
		}

		private TestClient(Runnable onRender) {
			this.onRender = onRender;
		}

		@Override
		public boolean isRenderActive() {
			return true;
		}

		@Override
		public boolean shouldRenderOnTick() {
			return dirty.getAndSet(false);
		}

		@Override
		public void renderScheduledFrame() {
			onRender.run();
			renderThreads.add(Thread.currentThread());
		}

		/**
		 * @return the number of frames rendered before this request
		 */
		private int requestFrame() {
			int rendered = renderThreads.size();
			dirty.set(true);
			return rendered;
		}

		/**
		 * @return the thread that rendered the frame
		 */
		private Thread awaitFrameAfter(int rendered) throws InterruptedException {
			long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(TIMEOUT_MS);
			while (renderThreads.size() <= rendered) {
				if (System.nanoTime() > deadline) {
					fail("Timed out waiting for a frame");
				}
				Thread.sleep(2);
			}
			return renderThreads.get(rendered);
		}

		private Thread renderOneFrame() throws InterruptedException {
			return awaitFrameAfter(requestFrame());
		}
	}
}
