package info.openrocket.swing.gui.figure3d;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Serializes all Swing AWTGLCanvas rendering onto one shared background thread.
 */
public final class SharedCanvasRenderScheduler {
	private static final Logger log = LoggerFactory.getLogger(SharedCanvasRenderScheduler.class);
	private static final int FRAME_INTERVAL_MS = 16;
	private static final SharedCanvasRenderScheduler INSTANCE = new SharedCanvasRenderScheduler();

	public interface Client {
		boolean isRenderActive();

		boolean shouldRenderOnTick();

		void renderScheduledFrame();

		default String getRenderDebugName() {
			return getClass().getSimpleName();
		}
	}

	private final ArrayList<Client> active = new ArrayList<>();
	private final Set<Client> immediatePending = ConcurrentHashMap.newKeySet();
	private final ScheduledExecutorService executor;

	private SharedCanvasRenderScheduler() {
		executor = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread thread = new Thread(r, "figure3d-render");
			thread.setDaemon(true);
			return thread;
		});
		executor.scheduleAtFixedRate(this::tick, 0, FRAME_INTERVAL_MS, TimeUnit.MILLISECONDS);
	}

	public static SharedCanvasRenderScheduler getInstance() {
		return INSTANCE;
	}

	public void register(Client client) {
		synchronized (active) {
			if (!active.contains(client)) {
				active.add(client);
			}
		}
	}

	public void unregister(Client client) {
		synchronized (active) {
			active.remove(client);
		}
		immediatePending.remove(client);
	}

	public void requestImmediate(Client client) {
		if (!immediatePending.add(client)) {
			return;
		}
		executor.execute(() -> {
			immediatePending.remove(client);
			if (!client.isRenderActive()) {
				return;
			}
			if (!client.shouldRenderOnTick()) {
				return;
			}
			try {
				client.renderScheduledFrame();
			} catch (Throwable t) {
				log.error("Immediate render failed for {}", client.getRenderDebugName(), t);
			}
		});
	}

	public boolean awaitQuiescence(long timeoutMs) {
		Future<?> barrier = executor.submit(() -> {
		});
		try {
			barrier.get(timeoutMs, TimeUnit.MILLISECONDS);
			return true;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		} catch (ExecutionException e) {
			log.warn("Render scheduler barrier failed", e.getCause());
			return false;
		} catch (TimeoutException e) {
			barrier.cancel(true);
			log.warn("Timed out waiting {} ms for render scheduler to quiesce", timeoutMs);
			return false;
		}
	}

	private void tick() {
		List<Client> snapshot;
		synchronized (active) {
			active.removeIf(client -> !client.isRenderActive());
			if (active.isEmpty()) {
				return;
			}
			snapshot = new ArrayList<>(active);
		}
		for (Client client : snapshot) {
			if (!client.shouldRenderOnTick()) {
				continue;
			}
			try {
				client.renderScheduledFrame();
			} catch (Throwable t) {
				log.error("Render scheduler tick failed for {}", client.getRenderDebugName(), t);
			}
		}
	}
}
