package net.sf.openrocket.l10n;

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
	 * @throws NullPointerException	if key is null.
	 */
	public String get(String key);
	
}
