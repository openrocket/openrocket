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
	
}
