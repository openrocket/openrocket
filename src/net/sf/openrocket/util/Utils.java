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
	
	
	/**
	 * Check whether an array contains a specified object.
	 * 
	 * @param array		the array to search
	 * @param search	the object to search for
	 * @return			whether the object was in the array
	 */
	public static boolean contains(Object[] array, Object search) {
		for (Object o : array) {
			if (equals(o, search)) {
				return true;
			}
		}
		return false;
	}
	
}
