package net.sf.openrocket.startup;

import net.sf.openrocket.database.ThrustCurveMotorSetDatabase;
import net.sf.openrocket.l10n.DebugTranslator;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.logging.LogLevel;
import net.sf.openrocket.logging.LogLevelBufferLogger;
import net.sf.openrocket.logging.PrintStreamLogger;

/**
 * A class that provides singleton instances / beans for other classes to utilize.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public final class Application {
	
	private static LogHelper logger;
	private static LogLevelBufferLogger logBuffer;
	
	private static Translator translator = new DebugTranslator();
	
	private static ThrustCurveMotorSetDatabase motorSetDatabase;
	

	// Initialize the logger to something sane for testing without executing Startup
	static {
		setLogOutputLevel(LogLevel.DEBUG);
	}
	
	
	/**
	 * Retrieve the logger to be used in logging.  By default this returns
	 * a logger that outputs to stdout/stderr even if not separately initialized,
	 * useful for development and debugging.
	 */
	public static LogHelper getLogger() {
		return logger;
	}
	
	/**
	 * Set the logger to be used in logging.  Note that calling this will only have effect
	 * on not-yet loaded classes, as the instance is stored in a static variable.
	 */
	public static void setLogger(LogHelper logger) {
		Application.logger = logger;
	}
	
	

	/**
	 * Return the log buffer.
	 * 
	 * @return the logBuffer or null if not initialized
	 */
	public static LogLevelBufferLogger getLogBuffer() {
		return logBuffer;
	}
	
	/**
	 * Set the log buffer logger.  The logger must be separately configured
	 * to receive the logging.
	 */
	public static void setLogBuffer(LogLevelBufferLogger logBuffer) {
		Application.logBuffer = logBuffer;
	}
	
	
	/**
	 * Set the logging to output the specified log level and upwards to standard output.
	 * 
	 * @param level		the minimum logging level to output.
	 */
	public static void setLogOutputLevel(LogLevel level) {
		logger = new PrintStreamLogger();
		for (LogLevel l : LogLevel.values()) {
			if (l.atLeast(level)) {
				((PrintStreamLogger) logger).setOutput(l, System.out);
			}
		}
		
	}
	
	
	/**
	 * Return the translator to use for obtaining translated strings.
	 * @return	a translator.
	 */
	public static Translator getTranslator() {
		return translator;
	}
	
	/**
	 * Set the translator used in obtaining translated strings.
	 * @param translator	the translator to set.
	 */
	public static void setTranslator(Translator translator) {
		Application.translator = translator;
	}
	
	

	/**
	 * Return the database of all thrust curves loaded into the system.
	 */
	public static ThrustCurveMotorSetDatabase getMotorSetDatabase() {
		return motorSetDatabase;
	}
	
	/**
	 * Set the database of thrust curves loaded into the system.
	 */
	public static void setMotorSetDatabase(ThrustCurveMotorSetDatabase motorSetDatabase) {
		Application.motorSetDatabase = motorSetDatabase;
	}
	

}
