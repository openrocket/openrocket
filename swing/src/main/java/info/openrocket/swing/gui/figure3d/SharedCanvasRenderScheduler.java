package info.openrocket.swing.gui.figure3d;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Drives Swing AWTGLCanvas rendering on background threads, one thread per client.
 *
 * <p>A client keeps its thread until its canvas is disposed through
 * {@link #disposeOnRenderThread}, so each GL context is only ever used by one thread. Some
 * drivers depend on that: with NVIDIA's threaded optimizations, a thread that has used one
 * context breaks the next context it uses (#3374).</p>
 *
 * <p>Work for different clients still never overlaps, because JAWT drawing-surface access must
 * be serialized across canvases: every frame and disposal runs under one shared lock.</p>
 */
public final class SharedCanvasRenderScheduler {
	private static final Logger log = LoggerFactory.getLogger(SharedCanvasRenderScheduler.class);
	private static final int FRAME_INTERVAL_MS = 16;
	private static final AtomicInteger THREAD_COUNTER = new AtomicInteger();
	private static final SharedCanvasRenderScheduler INSTANCE = new SharedCanvasRenderScheduler();

	public interface Client {
		boolean isRenderActive();

		boolean shouldRenderOnTick();

		void renderScheduledFrame();

		default String getRenderDebugName() {
			return getClass().getSimpleName();
		}
	}

	private final Map<Client, RenderThread> threads = new HashMap<>();
	private final Set<Client> immediatePending = ConcurrentHashMap.newKeySet();
	// Fair, so a client that renders on every tick cannot starve the others.
	private final ReentrantLock frameLock = new ReentrantLock(true);

	private SharedCanvasRenderScheduler() {
	}

	public static SharedCanvasRenderScheduler getInstance() {
		return INSTANCE;
	}

	/**
	 * Starts rendering the client on every tick, on the client's own thread.
	 */
	public void register(Client client) {
		synchronized (threads) {
			threads.computeIfAbsent(client, RenderThread::new).startTicking();
		}
	}

	/**
	 * Stops ticking the client. Its thread is kept, so a later {@link #register} renders on
	 * the same thread again.
	 */
	public void unregister(Client client) {
		synchronized (threads) {
			RenderThread thread = threads.get(client);
			if (thread != null) {
				thread.stopTicking();
			}
		}
		immediatePending.remove(client);
	}

	public void requestImmediate(Client client) {
		synchronized (threads) {
			RenderThread thread = threads.get(client);
			// Without a thread the client was never registered or has just been disposed,
			// so there is nothing to render.
			if (thread == null || !immediatePending.add(client)) {
				return;
			}
			thread.execute(() -> {
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
	}

	/**
	 * Waits until the work already queued on the client's thread has run.
	 *
	 * @return true if that happened within the timeout
	 */
	public boolean awaitQuiescence(Client client, long timeoutMs) {
		RenderThread thread;
		synchronized (threads) {
			thread = threads.get(client);
		}
		if (thread == null || thread.isCurrentThread()) {
			return true;
		}
		return await(thread.submitBarrier(), timeoutMs, client, "quiesce");
	}

	/**
	 * Disposes the client's canvas on the thread that rendered it, waits for that to finish,
	 * and then retires the thread. A client that was still registered continues on a new
	 * thread, so its next canvas never shares a thread with the destroyed context.
	 *
	 * <p>Call this before removing the canvas from the component tree, while its native peer
	 * still exists.</p>
	 *
	 * @param client the client that owns the canvas
	 * @param disposal disposes the canvas; it must be safe to run again if it times out here
	 *                 and the canvas is disposed elsewhere
	 * @param timeoutMs maximum time to wait
	 * @return true if the disposal finished within the timeout without throwing
	 */
	public boolean disposeOnRenderThread(Client client, Runnable disposal, long timeoutMs) {
		RenderThread thread;
		boolean wasTicking;
		synchronized (threads) {
			thread = threads.remove(client);
			wasTicking = thread != null && thread.stopTicking();
		}
		boolean completed;
		if (thread == null) {
			// The client never rendered, so no thread has used its context.
			completed = runDisposalInline(client, disposal);
		} else if (thread.isCurrentThread()) {
			completed = runDisposalInline(client, disposal);
			thread.shutdown();
		} else {
			Future<?> future = thread.submit(disposal);
			thread.shutdown();
			completed = await(future, timeoutMs, client, "dispose its canvas");
		}
		immediatePending.remove(client);
		if (wasTicking) {
			register(client);
		}
		return completed;
	}

	private boolean runDisposalInline(Client client, Runnable disposal) {
		try {
			runSerialized(disposal);
			return true;
		} catch (RuntimeException e) {
			log.warn("Canvas disposal failed for {}", client.getRenderDebugName(), e);
			return false;
		}
	}

	private void runSerialized(Runnable task) {
		frameLock.lock();
		try {
			task.run();
		} finally {
			frameLock.unlock();
		}
	}

	private static boolean await(Future<?> future, long timeoutMs, Client client, String action) {
		try {
			future.get(timeoutMs, TimeUnit.MILLISECONDS);
			return true;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		} catch (ExecutionException e) {
			log.warn("Render thread failed to {} for {}", action, client.getRenderDebugName(),
					e.getCause());
			return false;
		} catch (TimeoutException e) {
			log.warn("Timed out waiting {} ms for the render thread to {} for {}", timeoutMs, action,
					client.getRenderDebugName());
			return false;
		}
	}

	/**
	 * The render thread of one client. Every task for that client runs on this one thread.
	 */
	private final class RenderThread {
		private final Client client;
		private final ScheduledThreadPoolExecutor executor;
		private volatile Thread thread;
		// Guarded by the threads map.
		private ScheduledFuture<?> tick;

		private RenderThread(Client client) {
			this.client = client;
			executor = new ScheduledThreadPoolExecutor(1, runnable -> {
				Thread renderThread = new Thread(runnable,
						"figure3d-render-" + THREAD_COUNTER.incrementAndGet());
				renderThread.setDaemon(true);
				thread = renderThread;
				return renderThread;
			});
		}

		private boolean isCurrentThread() {
			return Thread.currentThread() == thread;
		}

		private void startTicking() {
			if (tick == null) {
				tick = executor.scheduleAtFixedRate(this::tick, 0, FRAME_INTERVAL_MS,
						TimeUnit.MILLISECONDS);
			}
		}

		/**
		 * @return true if the client was ticking
		 */
		private boolean stopTicking() {
			if (tick == null) {
				return false;
			}
			tick.cancel(false);
			tick = null;
			return true;
		}

		private void execute(Runnable task) {
			executor.execute(() -> runSerialized(task));
		}

		private Future<?> submit(Runnable task) {
			return executor.submit(() -> runSerialized(task));
		}

		private Future<?> submitBarrier() {
			return executor.submit(() -> {
			});
		}

		private void shutdown() {
			executor.shutdown();
		}

		private void tick() {
			if (!client.isRenderActive()) {
				synchronized (threads) {
					if (threads.get(client) == this) {
						stopTicking();
					}
				}
				return;
			}
			if (!client.shouldRenderOnTick()) {
				return;
			}
			runSerialized(() -> {
				try {
					client.renderScheduledFrame();
				} catch (Throwable t) {
					log.error("Render scheduler tick failed for {}", client.getRenderDebugName(),
							t);
				}
			});
		}
	}
}
