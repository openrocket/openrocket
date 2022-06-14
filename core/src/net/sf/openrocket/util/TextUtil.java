package net.sf.openrocket.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Locale;


public class TextUtil {
	public static final int DEFAULT_DECIMAL_PLACES = 3;
	
	private static final char[] HEX = {
			'0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
	};
	
	/**
	 * Return the byte array for the string (in US-ASCII charset).
	 * 
	 * This function is implemented because Froyo (Android API 8) does not support
	 * String.getBytes(Charset)
	 */
	public static byte[] asciiBytes(String string) {
		ByteBuffer encoded = StandardCharsets.US_ASCII.encode(string);
		return encoded.array();
	}
	
	
	/**
	 * Return the bytes formatted as a hexadecimal string.  The length of the
	 * string will be twice the number of bytes, with no spacing between the bytes
	 * and lowercase letters utilized.
	 * 
	 * @param bytes		the bytes to convert.
	 * @return			the bytes in hexadecimal notation.
	 */
	public static final String hexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for (byte b : bytes) {
			sb.append(HEX[(b >>> 4) & 0xF]);
			sb.append(HEX[b & 0xF]);
		}
		return sb.toString();
	}

	/**
	 * Return a string of the double value with suitable precision for storage.
	 * If exponential notation is used, values smaller than 0.001 or greater than 10000 will be formatted
	 * with exponential notation, otherwise normal formatting is used.
	 *
	 * @param d		the value to present.
	 * @param decimalPlaces the number of decimal places to save the value with.
	 * @param isExponentialNotation if true, the value is presented in exponential notation.
	 * @return		a representation with suitable precision.
	 */
	public static String doubleToString(double d, int decimalPlaces, boolean isExponentialNotation) {
		// Check for special cases
		if (MathUtil.equals(d, 0))
			return "0";

		if (Double.isNaN(d))
			return "NaN";

		if (Double.isInfinite(d)) {
			if (d < 0)
				return "-Inf";
			else
				return "Inf";
		}

		String format = "%." + decimalPlaces + "f";

		// Print in exponential notation if value < 0.001 or >= 10000
		if (isExponentialNotation && (Math.abs(d) < 0.001 || Math.abs(d) >= 10000)) {
			format = "%." + decimalPlaces + "e";
		}

		String formatted = String.format(Locale.ENGLISH, format, d);
		return reformatExponent(trimTrailingZeros(formatted));
	}

	/**
	 * Return a string of the double value with suitable precision for storage.
	 * The string is the shortest representation of the value including at least
	 * 5 digits of precision.
	 * Saves with exponential notation by default.
	 *
	 * @param d		the value to present.
	 * @param decimalPlaces the number of decimal places to save the value with.
	 * @return		a representation with suitable precision.
	 */
	public static String doubleToString(double d, int decimalPlaces) {
		return doubleToString(d, decimalPlaces, true);
	}
	
	/**
	 * Return a string of the double value with suitable precision for storage.
	 * The string is the shortest representation of the value including at least
	 * 5 digits of precision.
	 * Saves with exponential notation by default & decimal places based on the value's size.
	 * 
	 * @param d		the value to present.
	 * @return		a representation with suitable precision.
	 */
	public static String doubleToString(double d) {
		return doubleToString(d, DEFAULT_DECIMAL_PLACES, true);
	}

	/**
	 * Trims trailing zeros of a string formatted decimal number (can be in exponential notation e.g. 1.2000E+06).
	 * @param number the String formatted decimal number.
	 * @return the String formatted decimal number without trailing zeros.
	 */
	private static String trimTrailingZeros(String number) {
		if (number == null)
			return null;

		if (!number.contains(".")) {
			return number;
		}

		// Deal with exponential notation
		if (number.contains("e")) {
			String[] split = number.split("e");
			number = split[0];
			String exponent = split[1];
			return number.replaceAll("\\.?0*$", "") + "e" + exponent;
		}

		return number.replaceAll("\\.?0*$", "");
	}

	/**
	 * Replaces Java's default exponential notation (e.g. e+06 or e-06) with a custom notation (e.g. e6 or e-6).
	 * @param number exponential formatted number (e.g. 3.1415927e+06).
	 * @return the exponential formatted number, with custom exponential notation (e.g. 3.1415927e6).
	 */
	private static String reformatExponent(String number) {
		// I don't wanna become an expert in regex to get this in one nice expression, leave me be.
		if (number.contains("e+")) {
			return number.replaceAll("e\\+?0*", "e");
		} else if (number.contains("e-")) {
			return number.replaceAll("e-?0*", "e-");
		}
		return number;
	}
	
	/**
	 * Escape a string as XML or HTML.  Encodes the following characters:
	 * <ul>
	 *  <li>less than, greater than
	 *  <li>quotation mark, apostrophe
	 *  <li>ampersand
	 *  <li>all control characters except newline, carriage return and tab
	 * </ul>
	 * 
	 * The result is both valid XML and HTML 2.0.  The majority of characters are left unchanged.
	 */
	public static String escapeXML(String s) {
		StringBuilder sb = new StringBuilder(s.length());
		
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			
			if (c == '&') {
				sb.append("&amp;");
			} else if (c == '<') {
				sb.append("&lt;");
			} else if (c == '>') {
				sb.append("&gt;");
			} else if (c == '"') {
				sb.append("&quot;");
			} else if (((c < 32) && (c != '\t') && (c != '\n') && (c != '\r')) || (c == '\'') || (c == 127)) {
				// &apos; is not used since it's not standard HTML, use numerical escape instead
				sb.append("&#").append((int) c).append(';');
			} else {
				sb.append(c);
			}
		}
		
		return sb.toString();
	}
	
	
	
}
