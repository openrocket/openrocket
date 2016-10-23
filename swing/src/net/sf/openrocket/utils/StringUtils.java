package net.sf.openrocket.utils;

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

}
