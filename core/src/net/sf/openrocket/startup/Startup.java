package net.sf.openrocket.startup;

import java.io.PrintStream;
import java.util.Locale;
import java.util.prefs.Preferences;

import net.sf.openrocket.database.ComponentPresetDatabase;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.DebugTranslator;
import net.sf.openrocket.l10n.L10N;
import net.sf.openrocket.l10n.ResourceBundleTranslator;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.DelegatorLogger;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.logging.LogLevel;
import net.sf.openrocket.logging.LogLevelBufferLogger;
import net.sf.openrocket.logging.PrintStreamLogger;


/**
 * The first class in the OpenRocket startup sequence.  This class is responsible
 * for setting up the Application class with the statically used subsystems
 * (logging and translation) and then delegating to Startup2 class.
 * <p>
 * This class must be very cautious about what classes it calls.  This is because
 * the loggers/translators for classes are initialized as static final members during
 * class initialization.  For example, this class MUST NOT use the Prefs class, because
 * using it will cause LineStyle to be initialized, which then receives an invalid
 * (not-yet-initialized) translator.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class Startup {
	
	static LogHelper log;
	
	private static final String LOG_STDERR_PROPERTY = "openrocket.log.stderr";
	private static final String LOG_STDOUT_PROPERTY = "openrocket.log.stdout";
	
	private static final int LOG_BUFFER_LENGTH = 50;
	
	
	/**
	 * OpenRocket startup main method.
	 */
	public static void main(final String[] args) throws Exception {
		
		// Check for "openrocket.debug" property before anything else
		checkDebugStatus();
		
		// Initialize logging first so we can use it
		initializeLogging();
		
		Application.setPreferences( new SwingPreferences() );
		
		// Setup the translations
		initializeL10n();

		// Must be done after localization is initialized
		ComponentPresetDatabase componentPresetDao = new ComponentPresetDatabase();
		componentPresetDao.load("datafiles/presets", "(?i).*orc");
		Application.setComponentPresetDao( componentPresetDao );
		
		// Continue startup in Startup2 class (where Application is already set up)
		Startup2.runMain(args);
		
	}
	
	

	/**
	 * Set proper system properties if openrocket.debug is defined.
	 */
	private static void checkDebugStatus() {
		if (System.getProperty("openrocket.debug") != null) {
			setPropertyIfNotSet("openrocket.log.stdout", "VBOSE");
			setPropertyIfNotSet("openrocket.log.tracelevel", "VBOSE");
			setPropertyIfNotSet("openrocket.debug.menu", "true");
			setPropertyIfNotSet("openrocket.debug.mutexlocation", "true");
			setPropertyIfNotSet("openrocket.debug.motordigest", "true");
		}
	}
	
	private static void setPropertyIfNotSet(String key, String value) {
		if (System.getProperty(key) == null) {
			System.setProperty(key, value);
		}
	}
	
	

	/**
	 * Initializes the loggins system.
	 */
	public static void initializeLogging() {
		DelegatorLogger delegator = new DelegatorLogger();
		
		// Log buffer
		LogLevelBufferLogger buffer = new LogLevelBufferLogger(LOG_BUFFER_LENGTH);
		delegator.addLogger(buffer);
		
		// Check whether to log to stdout/stderr
		PrintStreamLogger printer = new PrintStreamLogger();
		boolean logout = setLogOutput(printer, System.out, System.getProperty(LOG_STDOUT_PROPERTY), null);
		boolean logerr = setLogOutput(printer, System.err, System.getProperty(LOG_STDERR_PROPERTY), LogLevel.ERROR);
		if (logout || logerr) {
			delegator.addLogger(printer);
		}
		
		// Set the loggers
		Application.setLogger(delegator);
		Application.setLogBuffer(buffer);
		
		// Initialize the log for this class
		log = Application.getLogger();
		log.info("Logging subsystem initialized");
		String str = "Console logging output:";
		for (LogLevel l : LogLevel.values()) {
			PrintStream ps = printer.getOutput(l);
			str += " " + l.name() + ":";
			if (ps == System.err) {
				str += "stderr";
			} else if (ps == System.out) {
				str += "stdout";
			} else {
				str += "none";
			}
		}
		str += " (" + LOG_STDOUT_PROPERTY + "=" + System.getProperty(LOG_STDOUT_PROPERTY) +
				" " + LOG_STDERR_PROPERTY + "=" + System.getProperty(LOG_STDERR_PROPERTY) + ")";
		log.info(str);
	}
	
	private static boolean setLogOutput(PrintStreamLogger logger, PrintStream stream, String level, LogLevel defaultLevel) {
		LogLevel minLevel = LogLevel.fromString(level, defaultLevel);
		if (minLevel == null) {
			return false;
		}
		
		for (LogLevel l : LogLevel.values()) {
			if (l.atLeast(minLevel)) {
				logger.setOutput(l, stream);
			}
		}
		return true;
	}
	
	


	/**
	 * Initializes the localization system.
	 */
	private static void initializeL10n() {
		
		// Check for locale propery
		String langcode = System.getProperty("openrocket.locale");
		
		if (langcode != null) {
			
			Locale l = L10N.toLocale(langcode);
			log.info("Setting custom locale " + l);
			Locale.setDefault(l);
			
		} else {
			
			// Check user-configured locale
			Locale l = getUserLocale();
			if (l != null) {
				log.info("Setting user-selected locale " + l);
				Locale.setDefault(l);
			} else {
				log.info("Using default locale " + Locale.getDefault());
			}
			
		}
		
		// Setup the translator
		Translator t;
		t = new ResourceBundleTranslator("l10n.messages");
		if (Locale.getDefault().getLanguage().equals("xx")) {
			t = new DebugTranslator(t);
		}
		
		log.info("Set up translation for locale " + Locale.getDefault() +
				", debug.currentFile=" + t.get("debug.currentFile"));
		
		Application.setBaseTranslator(t);
	}
	
	


	private static Locale getUserLocale() {
		/*
		 * This method MUST NOT use the Prefs class, since is causes a multitude
		 * of classes to be initialized.  Therefore this duplicates the functionality
		 * of the Prefs class locally.
		 */

		if (System.getProperty("openrocket.debug.prefs") != null) {
			return null;
		}
		
		return L10N.toLocale(Preferences.userRoot().node("OpenRocket").get("locale", null));
	}
	

}
