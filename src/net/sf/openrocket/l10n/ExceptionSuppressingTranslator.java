package net.sf.openrocket.l10n;

import java.util.HashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;

import net.sf.openrocket.gui.main.ExceptionHandler;

/**
 * A translator that suppresses MissingResourceExceptions and handles them gracefully.
 * For every missing key this class calls the exception handler exactly once, and
 * returns the key itself as the translation.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ExceptionSuppressingTranslator implements Translator {
	
	private static final Set<String> errors = new HashSet<String>();
	// For unit testing:
	static int failures = 0;
	
	private final Translator translator;
	
	

	/**
	 * Sole constructor.
	 * 
	 * @param translator	the translator to use
	 */
	public ExceptionSuppressingTranslator(Translator translator) {
		this.translator = translator;
	}
	
	

	@Override
	public String get(String key) {
		try {
			return translator.get(key);
		} catch (MissingResourceException e) {
			handleError(key, e);
		}
		
		return key;
	}
	
	

	private static synchronized void handleError(String key, MissingResourceException e) {
		if (errors.add(key)) {
			failures++;
			ExceptionHandler.handleErrorCondition("Can not find translation for '" + key + "' locale=" + Locale.getDefault(), e);
		}
	}
	
}
