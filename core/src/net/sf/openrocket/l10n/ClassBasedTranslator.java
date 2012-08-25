package net.sf.openrocket.l10n;

import java.util.MissingResourceException;

import net.sf.openrocket.logging.TraceException;
import net.sf.openrocket.util.BugException;

/**
 * A translator that prepends a pre-defined class name in front of a translation key
 * and retrieves the translator for that key, and only if that is missing reverts to
 * the base key name.  The base class name can either be provided to the constructor
 * or retrieved from the stack.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ClassBasedTranslator implements Translator {
	
	
	private final Translator translator;
	private final String className;
	
	/**
	 * Construct a translator using a specified class name.
	 * 
	 * @param translator	the translator from which to obtain the translations.
	 * @param className		the base class name to prepend.
	 */
	public ClassBasedTranslator(Translator translator, String className) {
		this.translator = translator;
		this.className = className;
	}
	
	/**
	 * Construct a translator by obtaining the base class name from the stack.
	 * 
	 * @param translator	the translator from which to obtain the translations.
	 * @param levels		the number of levels to move upwards in the stack from the point where this method is called.
	 */
	public ClassBasedTranslator(Translator translator, int levels) {
		this(translator, getStackClass(levels));
	}
	
	
	
	@Override
	public String get(String key) {
		String classKey = className + "." + key;
		
		try {
			return translator.get(classKey);
		} catch (MissingResourceException e) {
			// Ignore
		}
		
		try {
			return translator.get(key);
		} catch (MissingResourceException e) {
			MissingResourceException mre = new MissingResourceException(
					"Neither key '" + classKey + "' nor '" + key + "' could be found", e.getClassName(), key);
			mre.initCause(e);
			throw mre;
		}
	}
	
	
	
	@Override
	public String get(String base, String text) {
		return translator.get(base, text);
	}
	
	@Override
	public String getBaseText(String base, String translation) {
		return translator.getBaseText(base, translation);
	}
	
	
	
	
	private static String getStackClass(int levels) {
		TraceException trace = new TraceException();
		StackTraceElement stack[] = trace.getStackTrace();
		final int index = levels + 2;
		if (stack.length <= index) {
			throw new BugException("Stack trace is too short, length=" + stack.length + ", expected=" + index, trace);
		}
		
		StackTraceElement element = stack[index];
		String cn = element.getClassName();
		int pos = cn.lastIndexOf('.');
		if (pos >= 0) {
			cn = cn.substring(pos + 1);
		}
		return cn;
	}
	
	
	
	
	// For unit testing purposes
	String getClassName() {
		return className;
	}
}
