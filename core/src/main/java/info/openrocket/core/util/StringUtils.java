package info.openrocket.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringUtils {

	public static String join(String sep, Object[] values) {
		if (values == null || values.length == 0) {
			return "";
		}
		StringBuilder value = new StringBuilder();
		for (Object v : values) {
			if (value.length() > 0) {
				value.append(sep);
			}
			value.append(String.valueOf(v));
		}
		return value.toString();
	}

	/**
	 * Join starting with a list of strings rather than an array
	 * 
	 * @param sep        separator
	 * @param listValues list of values
	 * @return joined string
	 */
	public static String join(String sep, List<String> listValues) {
		String[] values = listValues.toArray(new String[0]);
		return join(sep, values);
	}

	/**
	 * Returns true if the argument is null or empty.
	 * 
	 * This is implemented without using String.isEmpty() because that method
	 * is not available in Froyo.
	 * 
	 * @param s string to check
	 * @return true iff s is null or trims to
	 *         an empty string, where trim is defined
	 *         by {@link java.lang.String#trim}
	 */
	public static boolean isEmpty(String s) {
		if (s == null) {
			return true;
		}
		return "".equals(s.trim());
	}

	/**
	 * Converts a string to a double, but with a more robust locale handling.
	 * Some systems use a comma as a decimal separator, some a dot. This method
	 * should work for both cases
	 * 
	 * @param input string to convert
	 * @return double converted from string
	 * @throws NumberFormatException if the string cannot be parsed.
	 */
	public static double convertToDouble(String input) {
		input = input.replace(',', '.');
		int decimalSeparator = input.lastIndexOf('.');

		if (decimalSeparator > -1) {
			input = input.substring(0, decimalSeparator).replace(".", "") + input.substring(decimalSeparator);
		}

		return Double.parseDouble(input);
	}

	/**
	 * Returns an escaped version of the String so that it can be safely used as a
	 * value in a CSV file.
	 * The goal is to surround the input string in double quotes if it contains any
	 * double quotes, commas,
	 * newlines, or carriage returns, and to escape any double quotes within the
	 * string by doubling them up.
	 * 
	 * @param input the string to escape
	 * @return the escaped string that can be safely used in a CSV file
	 */
	public static String escapeCSV(String input) {
		final List<Character> CSV_SEARCH_CHARS = new ArrayList<>(Arrays.asList(',', '"', '\r', '\n'));

		StringBuilder sb = new StringBuilder();
		boolean quoted = false;
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (CSV_SEARCH_CHARS.contains(c)) {
				quoted = true;
				sb.append('\"');
			}
			if (c == '\"') {
				sb.append('\"');
			}
			sb.append(c);
		}
		if (quoted) {
			sb.insert(0, '\"');
			sb.append('\"');
		}
		return sb.toString();
	}

}
