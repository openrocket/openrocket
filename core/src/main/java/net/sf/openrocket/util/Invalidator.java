package net.sf.openrocket.util;

import net.sf.openrocket.startup.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that performs object invalidation functions.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class Invalidator implements Invalidatable {
	private static final boolean USE_CHECKS = Application.useSafetyChecks();
	
	private static final Logger log = LoggerFactory.getLogger(Invalidator.class);
	
	private final Object monitorable;
	private Throwable invalidated = null;
	
	
	/**
	 * Sole constructor.  The parameter is used when writing error messages, and
	 * is not referenced otherwise.
	 * 
	 * @param monitorable	the object this invalidator is monitoring (may be null or a descriptive string)
	 */
	public Invalidator(Object monitorable) {
		this.monitorable = monitorable;
	}
	
	
	/**
	 * Check whether the object has been invalidated.  Depending on the parameter either
	 * a BugException is thrown or a warning about the object access is logged.
	 * 
	 * @param throwException	whether to throw an exception or log a warning.
	 * @return	<code>true</code> when the object has not been invalidated, <code>false</code> if it has
	 * @throws	BugException	if the object has been invalidated and <code>throwException</code> is true.
	 */
	public boolean check(boolean throwException) {
		if (invalidated != null) {
			if (throwException) {
				throw new BugException(monitorable + ": This object has been invalidated", invalidated);
			} else {
				log.warn(monitorable + ": This object has been invalidated",
						new Throwable("Usage was attempted here", invalidated));
			}
			return false;
		}
		return true;
	}
	
	
	/**
	 * Check whether the object has been invalidated.
	 * @return	<code>true</code> if the object has been invalidated, <code>false</code> otherwise.
	 */
	public boolean isInvalidated() {
		return invalidated != null;
	}
	
	
	@Override
	public void invalidate() {
		if (USE_CHECKS) {
			if (invalidated != null) {
				log.warn(monitorable + ": This object has already been invalidated, ignoring", invalidated);
			}
			invalidated = new Throwable("Invalidation occurred here");
		}
	}
	
}
