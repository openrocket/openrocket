package info.openrocket.core.database;

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
	 * The startupDelay parameter defines a time to delay after calling startLoading
	 * before
	 * actually starting the loading. This allows other actions such as GUI opening
	 * to proceed faster. The delay can be cancelled by calling setInUse().
	 * 
	 * @param startupDelay number of milliseconds to delay before starting actual
	 *                     loading.
	 */
	public AsynchronousDatabaseLoader(long startupDelay) {
		this.startupDelay = startupDelay;
	}

	/**
	 * Start loading the database. Creates a new thread for the loading and returns
	 * immediately.
	 * 
	 * @throws IllegalStateException if this method has already been called.
	 */
	public synchronized void startLoading() {
		if (startedLoading) {
			throw new IllegalStateException("Already called startLoading");
		}
		startedLoading = true;
		new LoadingThread().start();
	}

	/**
	 * Return whether background loading has already been started.
	 * This is used by Swing startup code to avoid deadlocks when modal dialogs
	 * pump the EDT before the normal startup sequence reaches startLoading().
	 *
	 * @return whether startLoading() has already been called
	 */
	public boolean hasStartedLoading() {
		return startedLoading;
	}

	/**
	 * Mark the database as loaded. This method is called by the loading thread when it has finished loading the database.
	 * You can also use this to bypass the database loading, but still mark it as loaded.
	 */
	public void markAsLoaded() {
		synchronized (this) {
			startedLoading = true;
			endedLoading = true;
			this.notifyAll();
		}
	}

	/**
	 * @return whether loading the database has ended.
	 */
	public boolean isLoaded() {
		return endedLoading;
	}

	/**
	 * Cancel the startup delay (if still ongoing), and start loading the database
	 * immediately.
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
	 * @throws IllegalStateException if startLoading() has not been called.
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

	/**
	 * 
	 */
	private void doLoad() {
		try {
			// Pause for indicated startup time
			pauseForStartupTime();
			loadDatabase();
		} finally {
			/*
			 * Always release waiting threads, even if loading fails. Otherwise callers such
			 * as the Swing motor loading dialog can block forever after a background loading
			 * exception.
			 */
			markAsLoaded();
		}
	}

	/**
	 * waits the startup time before loading the database
	 */
	private void pauseForStartupTime() {
		long startLoading = System.currentTimeMillis() + startupDelay;
		while (!inUse && System.currentTimeMillis() < startLoading) {
			synchronized (this) {
				try {
					this.wait(startLoading - System.currentTimeMillis());
				} catch (InterruptedException e) {
				}
			}
		}
	}

	/**
	 * method that actually load the database
	 */
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
