package net.sf.openrocket.util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;


public class TextUtil {

	
	private static final char[] HEX = {
			'0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
	};

	/**
	 * Return the byte array for the string in the given charset.
	 * 
	 * This function is implemented because Froyo (Android API 8) does not support
	 * String.getBytes(Charset)
	 * 
	 * @param string
	 * @param charSet
	 * @return
	 */
	public static byte[] convertStringToBytes( String string, Charset charSet ) {
		ByteBuffer encoded = charSet.encode(string);
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
	 * Return a string of the double value with suitable precision (5 digits).
	 * The string is the shortest representation of the value including the
	 * required precision.
	 * 
	 * @param d		the value to present.
	 * @return		a representation with suitable precision.
	 */
	public static final String doubleToString(double d) {
		
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
		

		final String sign = (d < 0) ? "-" : "";
		double abs = Math.abs(d);
		
		// Small and large values always in exponential notation
		if (abs < 0.001 || abs >= 100000000) {
			return sign + exponentialFormat(abs);
		}
		
		// Check whether decimal or exponential notation is shorter
		
		String exp = exponentialFormat(abs);
		String dec = decimalFormat(abs);
		
		if (dec.length() <= exp.length())
			return sign + dec;
		else
			return sign + exp;
	}
	
	
	/*
	 * value must be positive and not zero!
	 */
	private static String exponentialFormat(double value) {
		int exp;
		
		exp = 0;
		while (value < 1.0) {
			value *= 10;
			exp--;
		}
		while (value >= 10.0) {
			value /= 10;
			exp++;
		}
		
		return shortDecimal(value, 4) + "e" + exp;
	}
	
	
	/*
	 * value must be positive and not zero!
	 */
	private static String decimalFormat(double value) {
		if (value >= 10000)
			return "" + (int) (value + 0.5);
		
		int decimals = 1;
		double v = value;
		while (v < 1000) {
			v *= 10;
			decimals++;
		}
		
		return shortDecimal(value, decimals);
	}
	
	


	/*
	 * value must be positive!
	 */
	private static String shortDecimal(double value, int decimals) {
		
		// Calculate rounding and limit values (rounding slightly smaller)
		int rounding = 1;
		double limit = 0.5;
		for (int i = 0; i < decimals; i++) {
			rounding *= 10;
			limit /= 10;
		}
		
		// Round value
		value = (Math.rint(value * rounding) + 0.1) / rounding;
		

		int whole = (int) value;
		value -= whole;
		

		if (value < limit)
			return "" + whole;
		limit *= 10;
		
		StringBuilder sb = new StringBuilder();
		sb.append("" + whole);
		sb.append('.');
		

		for (int i = 0; i < decimals; i++) {
			
			value *= 10;
			whole = (int) value;
			value -= whole;
			sb.append((char) ('0' + whole));
			
			if (value < limit)
				return sb.toString();
			limit *= 10;
			
		}
		
		return sb.toString();
	}
	
	
	public static String htmlEncode(String s) {
		s = s.replace("&", "&amp;");
		s = s.replace("\"", "&quot;");
		s = s.replace("<", "&lt;");
		s = s.replace(">", "&gt;");
		return s;
	}
}
