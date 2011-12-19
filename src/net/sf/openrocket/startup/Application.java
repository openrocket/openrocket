package net.sf.openrocket.startup;

import net.sf.openrocket.database.ThrustCurveMotorSetDatabase;
import net.sf.openrocket.gui.main.SwingExceptionHandler;
import net.sf.openrocket.l10n.ClassBasedTranslator;
import net.sf.openrocket.l10n.DebugTranslator;
import net.sf.openrocket.l10n.ExceptionSuppressingTranslator;
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
	
	private static Translator baseTranslator = new DebugTranslator(null);
	
	private static ThrustCurveMotorSetDatabase motorSetDatabase;

	private static Preferences preferences;
	
	private static SwingExceptionHandler exceptionHandler;

	// Initialize the logger to something sane for testing without executing Startup
	static {
		setLogOutputLevel(LogLevel.DEBUG);
	}
	
	/**
	 * Return whether to use additional safety code checks.
	 */
	public static boolean useSafetyChecks() {
		// Currently default to false unless openrocket.debug.safetycheck is defined
		String s = System.getProperty("openrocket.debug.safetycheck");
		if (s != null && !(s.equalsIgnoreCase("false") || s.equalsIgnoreCase("off"))) {
			return true;
		}
		return false;
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
		Translator t = baseTranslator;
		t = new ClassBasedTranslator(t, 1);
		t = new ExceptionSuppressingTranslator(t);
		return t;
	}
	
	/**
	 * Set the translator used in obtaining translated strings.
	 * @param translator	the translator to set.
	 */
	public static void setBaseTranslator(Translator translator) {
		Application.baseTranslator = translator;
	}
	

	/**
	 * @return the preferences
	 */
	public static Preferences getPreferences() {
		return preferences;
	}

	/**
	 * @param preferences the preferences to set
	 */
	public static void setPreferences(Preferences preferences) {
		Application.preferences = preferences;
	}

	/**
	 * @return the exceptionHandler
	 */
	public static SwingExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}

	/**
	 * @param exceptionHandler the exceptionHandler to set
	 */
	public static void setExceptionHandler(SwingExceptionHandler exceptionHandler) {
		Application.exceptionHandler = exceptionHandler;
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
