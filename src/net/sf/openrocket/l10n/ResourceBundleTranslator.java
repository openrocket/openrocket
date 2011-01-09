package net.sf.openrocket.l10n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;

/**
 * A translator that obtains translated strings from a resource bundle.
 * <p>
 * If a message is not found in any resource bundle, an error is logged and the key itself
 * is returned.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ResourceBundleTranslator implements Translator {
	private static final LogHelper log = Application.getLogger();
	
	private final ResourceBundle bundle;
	private final String baseName;
	private final Locale locale;
	
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
		this.baseName = baseName;
		this.locale = locale;
	}
	
	
	/*
	 * NOTE:  This method must be thread-safe!
	 */
	@Override
	public synchronized String get(String key) {
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			log.error("String not found for key '" + key + "' in bundle '" + baseName + "' with locale " + locale, e);
		}
		return key;
	}
	
}
