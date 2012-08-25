package net.sf.openrocket.l10n;

import java.util.MissingResourceException;

/**
 * An interface for obtaining translations from logical keys.
 * <p>
 * Translator implementations must be thread-safe.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface Translator {
	
	/**
	 * Retrieve a translated string based on a logical key.  This always returns
	 * some string, potentially falling back to the key itself.
	 * 
	 * @param key	the logical string key.
	 * @return		the translated string.
	 * @throws MissingResourceException if the translation corresponding to the key is not found.
	 * @throws NullPointerException	if key is null.
	 */
	public String get(String key);
	
	
	/**
	 * Retrieve a translated string based on a base key and base (English) version of the text.
	 * The base text is normalized before using as a key.
	 * <p>
	 * This is meant to be used in very specific cases where the English name is
	 * used as a key for translation and storage.  If a translation is not found,
	 * the base text is used instead.
	 * 
	 * @param base		the base for the logical key
	 * @param text		the base (English) text to translate
	 * @return			the translated string, or "text" if not found
	 */
	public String get(String base, String text);
	
	
	/**
	 * Find the base (English) version of a translated text.
	 * <p>
	 * This is the opposite operation of {@link #get(String, String)}, and
	 * meant for use in very specific cases when storing the values of
	 * translated texts.
	 * 
	 * @param base			the base for the logical key
	 * @param translation	the translated string
	 * @return				the base text, or the translation if not found.
	 */
	public String getBaseText(String base, String translation);
	
}
