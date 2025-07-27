package info.openrocket.swing.logging;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;


public class LoggingSystemSetup {
	
	// This class is highly dependent on the logback.xml file.
	// It assumes logback is the logging backend and that
	// there is an appender named "buffer" and that appender
	// is the LogbackBufferLoggerAdapter.
	
	/**
	 * Add a Console Appender.  We do this programmatically so the application does not need
	 * to include additional jars required for the logback <if> syntax to function. 
	 */
	public static void addConsoleAppender() {
		Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
		appender.setName("console");
		appender.setContext(context);
		PatternLayoutEncoder layout = new PatternLayoutEncoder();
		layout.setContext(context);
		layout.setPattern("%-8relative %-5marker %-5level [%thread] %logger{2} - %message%n");
		layout.start();
		appender.setEncoder(layout);
		appender.start();
		logger.addAppender(appender);
	}

	public static void setupLoggingAppender(Logger logger) {
		logger.detachAndStopAllAppenders();
		logger.setLevel(Level.TRACE);
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		LogbackBufferLoggerAdaptor appender = new LogbackBufferLoggerAdaptor();
		appender.setName("buffer");
		appender.setContext(context);
		appender.start();
		logger.addAppender(appender);
	}

	public static void setupLoggingAppender() {
		Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		setupLoggingAppender(logger);
	}
	
	/**
	 * Get the logger to be used in logging.
	 * 
	 * @return	the logger to be used in all logging.
	 */
	public static DelegatorLogger getInstance() {
		Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		LogbackBufferLoggerAdaptor adaptor = (LogbackBufferLoggerAdaptor) logger.getAppender("buffer");
		return adaptor.getLogHelper();
	}
	
	public static LogLevelBufferLogger getBufferLogger() {
		Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		return getBufferLogger(logger);
	}

	public static LogLevelBufferLogger getBufferLogger(Logger logger) {
		LogbackBufferLoggerAdaptor adaptor = (LogbackBufferLoggerAdaptor) logger.getAppender("buffer");
		return adaptor.getLogBuffer();
	}
	
}
