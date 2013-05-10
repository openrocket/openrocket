package net.sf.openrocket.logging;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;


public class LoggingSystemSetup {
	
	// This class is highly dependent on the logback.xml file.
	// It assumes logback is the logging backend and that
	// there is an appender named "buffer" and that appender
	// is the LogbackBufferLoggerAdapter.
	
	/**
	 * Get the logger to be used in logging.
	 * 
	 * @return	the logger to be used in all logging.
	 */
	public static DelegatorLogger getInstance() {
		Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		LogbackBufferLoggerAdaptor adapater = (LogbackBufferLoggerAdaptor) logger.getAppender("buffer");
		return adapater.getLogHelper();
	}
	
	public static LogLevelBufferLogger getBufferLogger() {
		Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		LogbackBufferLoggerAdaptor adapater = (LogbackBufferLoggerAdaptor) logger.getAppender("buffer");
		return adapater.getLogBuffer();
	}
	
}
