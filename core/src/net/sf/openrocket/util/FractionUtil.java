package net.sf.openrocket.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class FractionUtil {

	
	private final static Pattern fractionPattern = Pattern.compile("(-?\\d+)/(\\d+)");
	private final static Pattern mixedFractionPattern = Pattern.compile("(-?\\d+)\\s+(\\d+)/(\\d+)");
	/**
	 * Parse a double from a string supporting fraction formats.
	 * 
	 * Will parse fractions specified with '/'.  Mixed numbers separated by ' ' (space).
	 * If no fraction is found in the input string, it is parsed with Double.parseDouble()
	 * which my throw the runtime exception java.lang.NumberFormatException.
	 * 
	 * Valid input may look like:
	 * 
	 * "1/4" = 0.25d
	 * "-1/4" = -0.25d
	 * "1 1/4" = 1.25d
	 * "-1 1/4" = 1.25d
	 * "1.25" = 1.25d
	 * 
	 * @param str
	 * @return
	 */
	public static Double parseFraction( String str ) {
		
		if ( str == null ) {
			throw new java.lang.NumberFormatException("null String");
		}
		
		Matcher m1 = mixedFractionPattern.matcher(str);
		if ( m1.find() ) {
			double wholepart = Double.parseDouble(m1.group(1));
			double num = Double.parseDouble(m1.group(2));
			double den = Double.parseDouble(m1.group(3));
			return wholepart + Math.copySign(num,wholepart) / den;
		}
		
		Matcher m2 = fractionPattern.matcher(str);
		if( m2.find() ) {
			double num = Double.parseDouble(m2.group(1));
			double den = Double.parseDouble(m2.group(2));
			return num / den;
		}
		
		return Double.parseDouble(str);
	}

}
