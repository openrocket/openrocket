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
		return s.trim().isEmpty();
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
	 * The method follows standard CSV escaping rules:
	 * 1. If the input contains any double quotes, commas, newlines, or carriage returns,
	 *    the entire string is surrounded by double quotes
	 * 2. Any double quotes within the string are escaped by doubling them
	 *
	 * @param input the string to escape
	 * @return the escaped string that can be safely used in a CSV file
	 */
	public static String escapeCSV(String input) {
		if (input == null) {
			return "";
		}

		// Check if we need to quote the string
		boolean needsQuoting = input.contains("\"") ||
				input.contains(",") ||
				input.contains("\r") ||
				input.contains("\n");

		if (!needsQuoting) {
			return input;
		}

		// Replace all double quotes with doubled double quotes
		String escaped = input.replace("\"", "\"\"");

		// Surround with quotes
		return "\"" + escaped + "\"";
	}

	public static String removeHTMLTags(String input) {
		if (input == null) {
			return null;
		}
		return input.replaceAll("<[^>]*>", "");
	}
}
