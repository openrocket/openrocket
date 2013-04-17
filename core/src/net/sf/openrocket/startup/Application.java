package net.sf.openrocket.startup;

import net.sf.openrocket.database.ComponentPresetDao;
import net.sf.openrocket.database.motor.MotorDatabase;
import net.sf.openrocket.database.motor.ThrustCurveMotorSetDatabase;
import net.sf.openrocket.gui.watcher.WatchService;
import net.sf.openrocket.l10n.ClassBasedTranslator;
import net.sf.openrocket.l10n.DebugTranslator;
import net.sf.openrocket.l10n.ExceptionSuppressingTranslator;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.logging.LogLevel;
import net.sf.openrocket.logging.LogLevelBufferLogger;
import net.sf.openrocket.logging.PrintStreamLogger;

import com.google.inject.Injector;

/**
 * A class that provides singleton instances / beans for other classes to utilize.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public final class Application {
	
	private static LogHelper logger;
	private static LogLevelBufferLogger logBuffer;
	
	private static Translator baseTranslator = new DebugTranslator(null);
	
	private static ComponentPresetDao componentPresetDao;
	
	private static Preferences preferences;
	
	private static ExceptionHandler exceptionHandler;
	
	private static Injector injector;
	
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
	
	public static WatchService getWatchService() {
		return Application.injector.getInstance(WatchService.class);
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
	public static ExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}
	
	/**
	 * @param exceptionHandler the exceptionHandler to set
	 */
	public static void setExceptionHandler(ExceptionHandler exceptionHandler) {
		Application.exceptionHandler = exceptionHandler;
	}
	
	/**
	 * Return the database of all thrust curves loaded into the system.
	 * 
	 * @deprecated Fetch the db from Guice instead.
	 */
	@Deprecated
	public static MotorDatabase getMotorSetDatabase() {
		return injector.getInstance(MotorDatabase.class);
	}
	
	/**
	 * Return the ThrustCurveMotorSetDatabase for the system.
	 * 
	 * @deprecated Fetch the db from Guice instead.
	 */
	@Deprecated
	public static ThrustCurveMotorSetDatabase getThrustCurveMotorSetDatabase() {
		return injector.getInstance(ThrustCurveMotorSetDatabase.class);
	}
	
	
	public static ComponentPresetDao getComponentPresetDao() {
		return componentPresetDao;
		
	}
	
	public static void setComponentPresetDao(ComponentPresetDao componentPresetDao) {
		Application.componentPresetDao = componentPresetDao;
	}
	
	public static Injector getInjector() {
		return injector;
	}
	
	public static void setInjector(Injector injector) {
		Application.injector = injector;
	}
	
	
	
	
}
