package net.sf.openrocket.database;

/**
 * A class that manages calling a DatabaseLoader in the background.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class AsynchronousDatabaseLoader {
	
	private final long startupDelay;
	
	private volatile boolean startedLoading = false;
	private volatile boolean endedLoading = false;
	private volatile boolean inUse = false;
	
	/**
	 * Sole constructor.
	 * <p>
	 * The startupDelay parameter defines a time to delay after calling startLoading before
	 * actually starting the loading.  This allows other actions such as GUI opening
	 * to proceed faster.  The delay can be cancelled by calling setInUse().
	 * 
	 * @param startupDelay	number of milliseconds to delay before starting actual loading.
	 */
	public AsynchronousDatabaseLoader(long startupDelay) {
		this.startupDelay = startupDelay;
	}
	
	
	
	/**
	 * Start loading the database.  Creates a new thread for the loading and returns immediately.
	 * 
	 * @throws  IllegalStateException	if this method has already been called.
	 */
	public void startLoading() {
		if (startedLoading) {
			throw new IllegalStateException("Already called startLoading");
		}
		startedLoading = true;
		new LoadingThread().start();
	}
	
	
	/**
	 * Return whether loading the database has ended.
	 */
	public boolean isLoaded() {
		return endedLoading;
	}
	
	
	/**
	 * Cancel the startup delay (if still ongoing), and start loading the database immediately.
	 */
	public void cancelStartupDelay() {
		if (!inUse) {
			synchronized (this) {
				inUse = true;
				this.notifyAll();
			}
		}
	}
	
	
	/**
	 * Block the current thread until loading of the motors has been completed.
	 * This also cancels any ongoing startup delay.
	 * 
	 * @throws IllegalStateException	if startLoading() has not been called.
	 */
	public void blockUntilLoaded() {
		if (!startedLoading) {
			throw new IllegalStateException("startLoading() has not been called");
		}
		if (!endedLoading) {
			cancelStartupDelay();
			synchronized (this) {
				while (!endedLoading) {
					try {
						this.wait();
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}
	
	
	private void doLoad() {
		
		// Pause for indicated startup time
		long startLoading = System.currentTimeMillis() + startupDelay;
		while (!inUse && System.currentTimeMillis() < startLoading) {
			synchronized (this) {
				try {
					this.wait(startLoading - System.currentTimeMillis());
				} catch (InterruptedException e) {
				}
			}
		}
		
		loadDatabase();
		
		synchronized (this) {
			endedLoading = true;
			this.notifyAll();
		}
	}
	
	
	protected abstract void loadDatabase();
	
	
	/**
	 * Background thread for loading the database.
	 */
	private class LoadingThread extends Thread {
		private LoadingThread() {
			this.setName("DatabaseLoadingThread");
			this.setPriority(MIN_PRIORITY);
		}
		
		@Override
		public void run() {
			doLoad();
		}
	}
	
}
