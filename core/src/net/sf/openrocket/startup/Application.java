package net.sf.openrocket.startup;

import net.sf.openrocket.l10n.ClassBasedTranslator;
import net.sf.openrocket.l10n.ExceptionSuppressingTranslator;
import net.sf.openrocket.l10n.Translator;

import com.google.inject.Injector;

/**
 * A class that provides singleton instances / beans for other classes to utilize.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public final class Application {
	
	private static ExceptionHandler exceptionHandler;
	
	private static Injector injector;
	
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
	
	private static Translator getBaseTranslator() {
		return injector.getInstance(Translator.class);
	}
	
	/**
	 * Return the translator to use for obtaining translated strings.
	 * @return	a translator.
	 */
	public static Translator getTranslator() {
		Translator t = getBaseTranslator();
		t = new ClassBasedTranslator(t, 1);
		t = new ExceptionSuppressingTranslator(t);
		return t;
	}
	
	/**
	 * @return the preferences
	 */
	public static Preferences getPreferences() {
		return injector.getInstance(Preferences.class);
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
	
	public static Injector getInjector() {
		return injector;
	}
	
	public static void setInjector(Injector injector) {
		Application.injector = injector;
	}
	
	
	
	
}
