package net.sf.openrocket.startup;

import net.sf.openrocket.logging.DelegatorLogger;
import net.sf.openrocket.logging.LogHelper;

/**
 * A class that provides singleton instances / beans for other
 * classes to utilize.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public final class Application {
	
	private static DelegatorLogger logger = null;

	public static LogHelper getLogHelper() {
		if (logger == null) {
			initializeLogging();
		}
		return logger;
	}
	
	
	/**
	 * Initializes the logging system and populates logHelper.
	 */
	private static void initializeLogging() {
		logger = new DelegatorLogger();
		
		
	}
	
}
