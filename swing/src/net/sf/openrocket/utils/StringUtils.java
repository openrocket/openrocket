package net.sf.openrocket.utils;

import java.util.List;

public class StringUtils {

	public static String join(String sep, Object[] values) {
		if ( values == null || values.length == 0 ) {
			return "";
		}
		StringBuilder value = new StringBuilder();
		for( Object v : values ) {
			if( value.length() > 0 ) {
				value.append(sep);
			}
			value.append(String.valueOf(v));
		}
		return value.toString();
	}
	
	/**
	 * Join starting with a list of strings rather than an array
	 * @param sep
	 * @param listValues
	 * @return
	 */
	public static String join(String sep, List<String> listValues) {
		String[] values = listValues.toArray(new String[listValues.size()]);
		return join(sep, values);
	}


}
