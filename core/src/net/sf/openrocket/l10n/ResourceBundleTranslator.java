package net.sf.openrocket.l10n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A translator that obtains translated strings from a resource bundle.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ResourceBundleTranslator implements Translator {
	
	private final ResourceBundle bundle;
	private final ResourceBundle english;
	
	/**
	 * Create a ResourceBundleTranslator using the default Locale.
	 * 
	 * @param baseName	the base name of the resource bundle
	 */
	public ResourceBundleTranslator(String baseName) {
		this(baseName, Locale.getDefault());
	}
	
	/**
	 * Create a ResourceBundleTranslator using the specified Locale.
	 * 
	 * @param baseName	the base name of the resource bundle
	 * @param locale	the locale to use
	 */
	public ResourceBundleTranslator(String baseName, Locale locale) {
		this.bundle = ResourceBundle.getBundle(baseName, locale);
		this.english = ResourceBundle.getBundle(baseName, Locale.ROOT);
	}
	
	
	/*
	 * NOTE:  This method must be thread-safe!
	 */
	@Override
	public synchronized String get(String key) {
		return bundle.getString(key);
	}
	
	@Override
	public synchronized String get(String base, String text) {
		String key = base + "." + L10N.normalize(text);
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return text;
		}
	}
	
	@Override
	public synchronized String getBaseText(String base, String translation) {
		String prefix = base + ".";
		for (String key : bundle.keySet()) {
			if (key.startsWith(prefix)) {
				String value = bundle.getString(key);
				if (value.equals(translation)) {
					return english.getString(key);
				}
			}
		}
		return translation;
	}
}
