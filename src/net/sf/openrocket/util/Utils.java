package net.sf.openrocket.util;

public class Utils {
	
	/**
	 * Null-safe equals method.
	 * 
	 * @param first		the first object to compare
	 * @param second	the second object to compare
	 * @return			whether the two objects are both equal or both <code>null</code>
	 */
	public static boolean equals(Object first, Object second) {
		if (first == null) {
			return second == null;
		} else {
			return first.equals(second);
		}
	}
	
}
