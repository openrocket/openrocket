package net.sf.openrocket.l10n;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Helper methods for localization needs.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public final class L10N {
	
	private L10N() {
		// Prevent instantiation
	}
	
	
	/**
	 * Replace a text token by a replacement value.
	 * <p>
	 * A text token is a string portion that should be surrounded by
	 * braces, "{text}".
	 * 
	 * @param original		the original string.
	 * @param token			the text token to replace.
	 * @param replacement	the replacement text.
	 * @return				the modified string.
	 */
	public static String replace(String original, String token, String replacement) {
		return Pattern.compile(token, Pattern.LITERAL).matcher(original).replaceAll(replacement);
	}
	
	
	/**
	 * Convert a language code into a Locale.
	 * 
	 * @param langcode	the language code (<code>null</code> ok).
	 * @return			the corresponding locale (or <code>null</code> if the input was <code>null</code>)
	 */
	public static Locale toLocale(String langcode) {
		if (langcode == null) {
			return null;
		}
		
		Locale l;
		String[] split = langcode.split("[_-]", 3);
		if (split.length == 1) {
			l = new Locale(split[0]);
		} else if (split.length == 2) {
			l = new Locale(split[0], split[1]);
		} else {
			l = new Locale(split[0], split[1], split[2]);
		}
		return l;
	}
	
}
