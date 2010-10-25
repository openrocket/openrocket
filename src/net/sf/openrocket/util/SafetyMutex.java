package net.sf.openrocket.util;

import java.util.LinkedList;

import net.sf.openrocket.gui.main.ExceptionHandler;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;

/**
 * A mutex that can be used for verifying thread safety.  This class cannot be
 * used to perform synchronization, only to detect concurrency issues.  This
 * class can be used by the main methods of non-thread-safe classes to ensure
 * the class is not wrongly used from multiple threads concurrently.
 * <p>
 * This mutex is not reentrant even for the same thread that has locked it.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SafetyMutex {
	private static final LogHelper log = Application.getLogger();
	
	// Package-private for unit testing
	static volatile boolean errorReported = false;
	
	// lockingThread is set when this mutex is locked.
	Thread lockingThread = null;
	// Stack of places that have locked this mutex
	final LinkedList<String> locations = new LinkedList<String>();
	
	
	/**
	 * Verify that this mutex is unlocked, but don't lock it.  This has the same effect
	 * as <code>mutex.lock(); mutex.unlock();</code> and is useful for methods that return
	 * immediately (e.g. getters).
	 * 
	 * @throws ConcurrencyException	if this mutex is already locked.
	 */
	public synchronized void verify() {
		checkState(true);
		if (lockingThread != null && lockingThread != Thread.currentThread()) {
			error("Mutex is already locked", true);
		}
	}
	
	
	/**
	 * Lock this mutex.  If this mutex is already locked an error is raised and
	 * a ConcurrencyException is thrown.  The location parameter is used to distinguish
	 * the locking location, and it should be e.g. the method name.
	 * 
	 * @param location	a string describing the location where this mutex was locked (cannot be null).
	 * 
	 * @throws ConcurrencyException		if this mutex is already locked.
	 */
	public synchronized void lock(String location) {
		if (location == null) {
			throw new IllegalArgumentException("location is null");
		}
		checkState(true);
		
		Thread currentThread = Thread.currentThread();
		if (lockingThread != null && lockingThread != currentThread) {
			error("Mutex is already locked", true);
		}
		
		lockingThread = currentThread;
		locations.push(location);
	}
	
	

	/**
	 * Unlock this mutex.  If this mutex is not locked at the position of the parameter
	 * or was locked by another thread than the current thread an error is raised,
	 * but an exception is not thrown.
	 * <p>
	 * This method is guaranteed never to throw an exception, so it can safely be used in finally blocks.
	 * 
	 * @param location	a location string matching that which locked the mutex
	 * @return 			whether the unlocking was successful (this normally doesn't need to be checked)
	 */
	public synchronized boolean unlock(String location) {
		try {
			
			if (location == null) {
				ExceptionHandler.handleErrorCondition("location is null");
				location = "";
			}
			checkState(false);
			

			// Check that the mutex is locked
			if (lockingThread == null) {
				error("Mutex was not locked", false);
				return false;
			}
			
			// Check that the mutex is locked by the current thread
			if (lockingThread != Thread.currentThread()) {
				error("Mutex is being unlocked from differerent thread than where it was locked", false);
				return false;
			}
			
			// Check that the unlock location is correct
			String lastLocation = locations.pop();
			if (!location.equals(lastLocation)) {
				locations.push(lastLocation);
				error("Mutex unlocking location does not match locking location, location=" + location, false);
				return false;
			}
			
			// Unlock the mutex if the last one
			if (locations.isEmpty()) {
				lockingThread = null;
			}
			return true;
		} catch (Exception e) {
			ExceptionHandler.handleErrorCondition("An exception occurred while unlocking a mutex, " +
					"locking thread=" + lockingThread + " locations=" + locations, e);
			return false;
		}
	}
	
	

	/**
	 * Check that the internal state of the mutex (lockingThread vs. locations) is correct.
	 */
	private void checkState(boolean throwException) {
		/*
		 * Disallowed states:
		 *   lockingThread == null  &&  !locations.isEmpty()
		 *   lockingThread != null  &&  locations.isEmpty()
		 */
		if ((lockingThread == null) ^ (locations.isEmpty())) {
			// Clear the mutex only after error() has executed (and possibly thrown an exception)
			try {
				error("Mutex data inconsistency occurred - unlocking mutex", throwException);
			} finally {
				lockingThread = null;
				locations.clear();
			}
		}
	}
	
	
	/**
	 * Raise an error.  The first occurrence is passed directly to the exception handler,
	 * later errors are simply logged.
	 */
	private void error(String message, boolean throwException) {
		message = message +
				", current thread = " + Thread.currentThread() +
				", locking thread=" + lockingThread +
				", locking locations=" + locations;
		
		ConcurrencyException ex = new ConcurrencyException(message);
		
		if (!errorReported) {
			errorReported = true;
			ExceptionHandler.handleErrorCondition(ex);
		} else {
			log.error(message, ex);
		}
		
		if (throwException) {
			throw ex;
		}
	}
	
}
