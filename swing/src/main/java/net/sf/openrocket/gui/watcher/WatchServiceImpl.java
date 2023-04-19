package net.sf.openrocket.gui.watcher;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class WatchServiceImpl implements WatchService {
	
	private final static int INTERVAL_MS = 1000;
	
	private static AtomicInteger threadcount = new AtomicInteger(0);
	
	private ScheduledExecutorService executor = Executors.newScheduledThreadPool(2, new ThreadFactory() {
		
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setName("WatchService-" + threadcount.getAndIncrement());
			return t;
		}
	});
	
	public WatchServiceImpl() {
	}
	
	/* (non-Javadoc)
	 * @see net.sf.openrocket.gui.watcher.WatchService#register(net.sf.openrocket.gui.watcher.Watchable)
	 */
	@Override
	public WatchKey register(Watchable w) {
		ScheduledFuture<?> future = executor.scheduleWithFixedDelay(new WatchableRunner(w), 0, INTERVAL_MS, TimeUnit.MILLISECONDS);
		
		return new WatchKeyImpl(future);
	}
	
	public class WatchKeyImpl implements WatchKey {
		
		ScheduledFuture<?> future;
		
		private WatchKeyImpl(ScheduledFuture<?> future) {
			this.future = future;
		}
		
		@Override
		public void cancel() {
			future.cancel(true);
		}
		
	}
	
	private class WatchableRunner implements Runnable {
		
		private Watchable w;
		
		private WatchableRunner(Watchable w) {
			this.w = w;
		}
		
		@Override
		public void run() {
			
			WatchEvent evt = w.monitor();
			if (evt != null) {
				w.handleEvent(evt);
			}
		}
	}
	
}
