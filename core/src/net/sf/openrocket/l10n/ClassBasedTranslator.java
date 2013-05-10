package net.sf.openrocket.l10n;

import java.util.MissingResourceException;

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
	private String className;
	private int levels;
	
	
	/**
	 * Construct a translator by obtaining the base class name from the stack
	 * on the first execution of the get() method.
	 * 
	 * @param translator	the translator from which to obtain the translations.
	 */
	public ClassBasedTranslator(Translator translator, int levels) {
		this.translator = translator;
		this.levels = levels;
	}
	
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
	
	
	
	@Override
	public String get(String key) {
		if (className == null) {
			className = findClassName();
		}
		
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
	
	
	
	private String findClassName() {
		Throwable trace = new Throwable();
		StackTraceElement stack[] = trace.getStackTrace();
		final int index = 2 + levels;
		if (stack.length <= index) {
			throw new BugException("Stack trace is too short, length=" + stack.length + ", expected=" + index, trace);
		}
		
		StackTraceElement element = stack[index];
		String cn = element.getClassName();
		int pos = cn.lastIndexOf('.');
		if (pos >= 0) {
			cn = cn.substring(pos + 1);
		}
		
		pos = cn.indexOf('$');
		if (pos >= 0) {
			cn = cn.substring(0, pos);
		}
		
		return cn;
	}
	
	
	
	// For unit testing purposes
	String getClassName() {
		return className;
	}
}
