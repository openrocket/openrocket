package info.openrocket.swing.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class CustomClickCountListenerTest {

    @Test
    void accumulatesClicksWithinInterval() {
        TestTimer timer = new TestTimer();
        CustomClickCountListener listener = new CustomClickCountListener(500, timer);

        try {
            listener.click();
            listener.click();

            assertEquals(2, listener.getClickCount());
        } finally {
            timer.cancel();
        }
    }

    @Test
    void resetsClickCountAfterIntervalElapses() {
        TestTimer timer = new TestTimer();
        CustomClickCountListener listener = new CustomClickCountListener(50, timer);

        try {
            listener.click();
            timer.runScheduledTask();

            assertEquals(0, listener.getClickCount());
        } finally {
            timer.cancel();
        }
    }

    @Test
    void rapidSequenceOfClicksKeepsAccumulating() {
        TestTimer timer = new TestTimer();
        CustomClickCountListener listener = new CustomClickCountListener(200, timer);

        try {
            listener.click();
            listener.click();
            listener.click();

            assertEquals(3, listener.getClickCount());
        } finally {
            timer.cancel();
        }
    }

    @Test
    void clickAfterIntervalStartsNewSequence() {
        TestTimer timer = new TestTimer();
        CustomClickCountListener listener = new CustomClickCountListener(75, timer);

        try {
            listener.click();
            timer.runScheduledTask();
            listener.click();

            assertEquals(1, listener.getClickCount());
        } finally {
            timer.cancel();
        }
    }

    @Test
    void parallelClicksDoNotThrowAndKeepCounterPositive() throws Exception {
        TestTimer timer = new TestTimer();
        CustomClickCountListener listener = new CustomClickCountListener(200, timer);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(4);

        try {
            for (int i = 0; i < 4; i++) {
                executor.submit(() -> {
                    await(start);
                    listener.click();
                    done.countDown();
                });
            }

            start.countDown();
            assertTrue(done.await(1, TimeUnit.SECONDS));
            assertTrue(listener.getClickCount() > 0);
        } finally {
            executor.shutdownNow();
            timer.cancel();
        }
    }

    @Test
    void timerCancellationPreventsFurtherScheduling() {
        Timer timer = new Timer("click-count-listener-test", true);
        CustomClickCountListener listener = new CustomClickCountListener(100, timer);

        try {
            listener.click();
            timer.cancel();

            assertThrows(IllegalStateException.class,
                    () -> timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                        }
                    }, 10));
        } finally {
            timer.cancel();
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Timer that records reset tasks so tests can trigger them without relying on wall-clock timing.
     */
    private static final class TestTimer extends Timer {
        private TimerTask scheduledTask;

        private TestTimer() {
            super("click-count-listener-test", true);
        }

        @Override
        public synchronized void schedule(TimerTask task, long delay) {
            scheduledTask = task;
        }

        private synchronized void runScheduledTask() {
            TimerTask task = scheduledTask;
            scheduledTask = null;
            assertNotNull(task, "Expected a click-count reset task to be scheduled");
            task.run();
        }
    }
}
