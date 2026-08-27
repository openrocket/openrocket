package info.openrocket.swing.gui.figure3d;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SharedCanvasRenderSchedulerTest {

	@Test
	void coalescesImmediateRequestsAndConsumesDirtyState() {
		SharedCanvasRenderScheduler scheduler = SharedCanvasRenderScheduler.getInstance();
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

		for (int i = 0; i < 10; i++) {
			scheduler.requestImmediate(client);
		}

		assertTrue(scheduler.awaitQuiescence(1_000));
		assertEquals(1, renders.get());
		assertFalse(dirty.get());
	}
}
