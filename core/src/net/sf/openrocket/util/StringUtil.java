package net.sf.openrocket.util;

public class StringUtil {

	/**
	 * Returns true if the argument is null or empty.
	 * 
	 * This is implemented without using String.isEmpty() because that method
	 * is not available in Froyo.
	 * 
	 * @param s string to check
	 * @return true iff s is null or trims to
	 * an empty string, where trim is defined
	 * by {@link java.lang.String#trim}
	 */
	public static boolean isEmpty( String s ) {
		if ( s == null ) {
			return true;
		}
		return "".equals(s.trim());
	}

	/**
	 * Converts a string to a double, but with a more robust locale handling.
	 * Some systems use a comma as a decimal separator, some a dot. This method
	 * should work for both cases
	 * @param input string to convert
	 * @return double converted from string
	 * @throws NumberFormatException   if the string cannot be parsed.
	 */
	public static double convertToDouble(String input) {
		input = input.replace(',', '.');
		int decimalSeparator = input.lastIndexOf('.');

		if (decimalSeparator > -1) {
			input = input.substring(0, decimalSeparator).replace(".", "") + input.substring(decimalSeparator);
		}

		return Double.parseDouble(input);
	}
	
}
