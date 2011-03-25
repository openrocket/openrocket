package net.sf.openrocket.l10n;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A translator that obtains translated strings from a resource bundle.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ResourceBundleTranslator implements Translator {
	
	private final ResourceBundle bundle;
	
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
	}
	
	
	/*
	 * NOTE:  This method must be thread-safe!
	 */
	@Override
	public synchronized String get(String key) {
		return bundle.getString(key);
	}
	
}
