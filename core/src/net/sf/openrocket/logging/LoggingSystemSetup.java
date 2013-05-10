package net.sf.openrocket.logging;


public class LoggingSystemSetup {
	
	private static final DelegatorLogger delegator = new DelegatorLogger();
	
	private static final int LOG_BUFFER_LENGTH = 50;
	
	private static final LogLevelBufferLogger llbl = new LogLevelBufferLogger(LOG_BUFFER_LENGTH);
	
	static {
		delegator.addLogger(llbl);
		
	}
	
	/**
	 * Get the logger to be used in logging.
	 * 
	 * @return	the logger to be used in all logging.
	 */
	public static DelegatorLogger getInstance() {
		return delegator;
	}
	
	public static LogLevelBufferLogger getBufferLogger() {
		return llbl;
	}
	
}
