package net.sf.openrocket.l10n;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import net.sf.openrocket.util.Chars;

/**
 * Helper methods for localization needs.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public final class L10N {
	
	/**
	 * Unicode character normalization map.  This is used because Android does
	 * not support the java.text.Normalize class.
	 */
	private static final Map<Character, String> NORMALIZATION_MAP;
	
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
	
	
	public static String normalize(String text) {
		text = unicodeNormalize(text);
		text = text.toLowerCase();
		text = text.replaceAll("\\s+", " ");
		text = text.trim();
		
		StringBuilder sb = new StringBuilder(text.length());
		for (char c : text.toCharArray()) {
			if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
				sb.append(c);
			} else if (c == ' ' || c == '/' || c == Chars.FRACTION) {
				sb.append('_');
			}
		}
		text = sb.toString();
		
		text = text.replaceAll("^_+", "");
		text = text.replaceAll("_+$", "");
		
		return text;
	}
	
	private static String unicodeNormalize(String text) {
		StringBuilder sb = new StringBuilder(text.length());
		for (char c : text.toCharArray()) {
			String s = NORMALIZATION_MAP.get(c);
			if (s != null) {
				sb.append(s);
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	
	static {
		/*
		 * This list is generated using the L10NGenerator utility.
		 */
		Map<Character, String> m = new HashMap<Character, String>();
		m.put('\u00aa', "a");
		m.put('\u00b2', "2");
		m.put('\u00b3', "3");
		m.put('\u00b9', "1");
		m.put('\u00ba', "o");
		m.put('\u00bc', "1/4");
		m.put('\u00bd', "1/2");
		m.put('\u00be', "3/4");
		m.put('\u00c0', "A");
		m.put('\u00c1', "A");
		m.put('\u00c2', "A");
		m.put('\u00c3', "A");
		m.put('\u00c4', "A");
		m.put('\u00c5', "A");
		m.put('\u00c7', "C");
		m.put('\u00c8', "E");
		m.put('\u00c9', "E");
		m.put('\u00ca', "E");
		m.put('\u00cb', "E");
		m.put('\u00cc', "I");
		m.put('\u00cd', "I");
		m.put('\u00ce', "I");
		m.put('\u00cf', "I");
		m.put('\u00d1', "N");
		m.put('\u00d2', "O");
		m.put('\u00d3', "O");
		m.put('\u00d4', "O");
		m.put('\u00d5', "O");
		m.put('\u00d6', "O");
		m.put('\u00d9', "U");
		m.put('\u00da', "U");
		m.put('\u00db', "U");
		m.put('\u00dc', "U");
		m.put('\u00dd', "Y");
		m.put('\u00e0', "a");
		m.put('\u00e1', "a");
		m.put('\u00e2', "a");
		m.put('\u00e3', "a");
		m.put('\u00e4', "a");
		m.put('\u00e5', "a");
		m.put('\u00e7', "c");
		m.put('\u00e8', "e");
		m.put('\u00e9', "e");
		m.put('\u00ea', "e");
		m.put('\u00eb', "e");
		m.put('\u00ec', "i");
		m.put('\u00ed', "i");
		m.put('\u00ee', "i");
		m.put('\u00ef', "i");
		m.put('\u00f1', "n");
		m.put('\u00f2', "o");
		m.put('\u00f3', "o");
		m.put('\u00f4', "o");
		m.put('\u00f5', "o");
		m.put('\u00f6', "o");
		m.put('\u00f9', "u");
		m.put('\u00fa', "u");
		m.put('\u00fb', "u");
		m.put('\u00fc', "u");
		m.put('\u00fd', "y");
		m.put('\u00ff', "y");
		m.put('\u0100', "A");
		m.put('\u0101', "a");
		m.put('\u0102', "A");
		m.put('\u0103', "a");
		m.put('\u0104', "A");
		m.put('\u0105', "a");
		m.put('\u0106', "C");
		m.put('\u0107', "c");
		m.put('\u0108', "C");
		m.put('\u0109', "c");
		m.put('\u010a', "C");
		m.put('\u010b', "c");
		m.put('\u010c', "C");
		m.put('\u010d', "c");
		m.put('\u010e', "D");
		m.put('\u010f', "d");
		m.put('\u0112', "E");
		m.put('\u0113', "e");
		m.put('\u0114', "E");
		m.put('\u0115', "e");
		m.put('\u0116', "E");
		m.put('\u0117', "e");
		m.put('\u0118', "E");
		m.put('\u0119', "e");
		m.put('\u011a', "E");
		m.put('\u011b', "e");
		m.put('\u011c', "G");
		m.put('\u011d', "g");
		m.put('\u011e', "G");
		m.put('\u011f', "g");
		m.put('\u0120', "G");
		m.put('\u0121', "g");
		m.put('\u0122', "G");
		m.put('\u0123', "g");
		m.put('\u0124', "H");
		m.put('\u0125', "h");
		m.put('\u0128', "I");
		m.put('\u0129', "i");
		m.put('\u012a', "I");
		m.put('\u012b', "i");
		m.put('\u012c', "I");
		m.put('\u012d', "i");
		m.put('\u012e', "I");
		m.put('\u012f', "i");
		m.put('\u0130', "I");
		m.put('\u0132', "IJ");
		m.put('\u0133', "ij");
		m.put('\u0134', "J");
		m.put('\u0135', "j");
		m.put('\u0136', "K");
		m.put('\u0137', "k");
		m.put('\u0139', "L");
		m.put('\u013a', "l");
		m.put('\u013b', "L");
		m.put('\u013c', "l");
		m.put('\u013d', "L");
		m.put('\u013e', "l");
		m.put('\u013f', "L");
		m.put('\u0140', "l");
		m.put('\u0143', "N");
		m.put('\u0144', "n");
		m.put('\u0145', "N");
		m.put('\u0146', "n");
		m.put('\u0147', "N");
		m.put('\u0148', "n");
		m.put('\u0149', "n");
		m.put('\u014c', "O");
		m.put('\u014d', "o");
		m.put('\u014e', "O");
		m.put('\u014f', "o");
		m.put('\u0150', "O");
		m.put('\u0151', "o");
		m.put('\u0154', "R");
		m.put('\u0155', "r");
		m.put('\u0156', "R");
		m.put('\u0157', "r");
		m.put('\u0158', "R");
		m.put('\u0159', "r");
		m.put('\u015a', "S");
		m.put('\u015b', "s");
		m.put('\u015c', "S");
		m.put('\u015d', "s");
		m.put('\u015e', "S");
		m.put('\u015f', "s");
		m.put('\u0160', "S");
		m.put('\u0161', "s");
		m.put('\u0162', "T");
		m.put('\u0163', "t");
		m.put('\u0164', "T");
		m.put('\u0165', "t");
		m.put('\u0168', "U");
		m.put('\u0169', "u");
		m.put('\u016a', "U");
		m.put('\u016b', "u");
		m.put('\u016c', "U");
		m.put('\u016d', "u");
		m.put('\u016e', "U");
		m.put('\u016f', "u");
		m.put('\u0170', "U");
		m.put('\u0171', "u");
		m.put('\u0172', "U");
		m.put('\u0173', "u");
		m.put('\u0174', "W");
		m.put('\u0175', "w");
		m.put('\u0176', "Y");
		m.put('\u0177', "y");
		m.put('\u0178', "Y");
		m.put('\u0179', "Z");
		m.put('\u017a', "z");
		m.put('\u017b', "Z");
		m.put('\u017c', "z");
		m.put('\u017d', "Z");
		m.put('\u017e', "z");
		m.put('\u017f', "s");
		m.put('\u01a0', "O");
		m.put('\u01a1', "o");
		m.put('\u01af', "U");
		m.put('\u01b0', "u");
		m.put('\u01c4', "DZ");
		m.put('\u01c5', "Dz");
		m.put('\u01c6', "dz");
		m.put('\u01c7', "LJ");
		m.put('\u01c8', "Lj");
		m.put('\u01c9', "lj");
		m.put('\u01ca', "NJ");
		m.put('\u01cb', "Nj");
		m.put('\u01cc', "nj");
		m.put('\u01cd', "A");
		m.put('\u01ce', "a");
		m.put('\u01cf', "I");
		m.put('\u01d0', "i");
		m.put('\u01d1', "O");
		m.put('\u01d2', "o");
		m.put('\u01d3', "U");
		m.put('\u01d4', "u");
		m.put('\u01d5', "U");
		m.put('\u01d6', "u");
		m.put('\u01d7', "U");
		m.put('\u01d8', "u");
		m.put('\u01d9', "U");
		m.put('\u01da', "u");
		m.put('\u01db', "U");
		m.put('\u01dc', "u");
		m.put('\u01de', "A");
		m.put('\u01df', "a");
		m.put('\u01e0', "A");
		m.put('\u01e1', "a");
		m.put('\u01e6', "G");
		m.put('\u01e7', "g");
		m.put('\u01e8', "K");
		m.put('\u01e9', "k");
		m.put('\u01ea', "O");
		m.put('\u01eb', "o");
		m.put('\u01ec', "O");
		m.put('\u01ed', "o");
		m.put('\u01f0', "j");
		m.put('\u01f1', "DZ");
		m.put('\u01f2', "Dz");
		m.put('\u01f3', "dz");
		m.put('\u01f4', "G");
		m.put('\u01f5', "g");
		m.put('\u01f8', "N");
		m.put('\u01f9', "n");
		m.put('\u01fa', "A");
		m.put('\u01fb', "a");
		m.put('\u0200', "A");
		m.put('\u0201', "a");
		m.put('\u0202', "A");
		m.put('\u0203', "a");
		m.put('\u0204', "E");
		m.put('\u0205', "e");
		m.put('\u0206', "E");
		m.put('\u0207', "e");
		m.put('\u0208', "I");
		m.put('\u0209', "i");
		m.put('\u020a', "I");
		m.put('\u020b', "i");
		m.put('\u020c', "O");
		m.put('\u020d', "o");
		m.put('\u020e', "O");
		m.put('\u020f', "o");
		m.put('\u0210', "R");
		m.put('\u0211', "r");
		m.put('\u0212', "R");
		m.put('\u0213', "r");
		m.put('\u0214', "U");
		m.put('\u0215', "u");
		m.put('\u0216', "U");
		m.put('\u0217', "u");
		m.put('\u0218', "S");
		m.put('\u0219', "s");
		m.put('\u021a', "T");
		m.put('\u021b', "t");
		m.put('\u021e', "H");
		m.put('\u021f', "h");
		m.put('\u0226', "A");
		m.put('\u0227', "a");
		m.put('\u0228', "E");
		m.put('\u0229', "e");
		m.put('\u022a', "O");
		m.put('\u022b', "o");
		m.put('\u022c', "O");
		m.put('\u022d', "o");
		m.put('\u022e', "O");
		m.put('\u022f', "o");
		m.put('\u0230', "O");
		m.put('\u0231', "o");
		m.put('\u0232', "Y");
		m.put('\u0233', "y");
		m.put('\u2070', "0");
		m.put('\u2071', "i");
		m.put('\u2074', "4");
		m.put('\u2075', "5");
		m.put('\u2076', "6");
		m.put('\u2077', "7");
		m.put('\u2078', "8");
		m.put('\u2079', "9");
		m.put('\u2080', "0");
		m.put('\u2081', "1");
		m.put('\u2082', "2");
		m.put('\u2083', "3");
		m.put('\u2084', "4");
		m.put('\u2085', "5");
		m.put('\u2086', "6");
		m.put('\u2087', "7");
		m.put('\u2088', "8");
		m.put('\u2089', "9");
		m.put('\u2044', "/");
		m.put('\u200b', " ");
		m.put('\u00a0', " ");
		NORMALIZATION_MAP = Collections.unmodifiableMap(m);
	}
}
