package net.sf.openrocket.util;

import java.util.Locale;

public class TextUtil {

	/**
	 * Return a string of the double value with suitable precision.
	 * The string is the shortest representation of the value including the
	 * required precision.
	 * 
	 * TODO: MEDIUM: Extra zeros are added unnecessarily to the end of the string.
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
		
		
		double abs = Math.abs(d);
		
		if (abs < 0.001) {
			// Compact exponential notation
			int exp = 0;
			
			while (abs < 1.0) {
				abs *= 10;
				exp++;
			}
			
			String sign = (d < 0) ? "-" : "";
			return sign + String.format((Locale)null, "%.4fe-%d", abs, exp);
		}
		if (abs < 0.01)
			return String.format((Locale)null, "%.7f", d);
		if (abs < 0.1)
			return String.format((Locale)null, "%.6f", d);
		if (abs < 1)
			return String.format((Locale)null, "%.5f", d);
		if (abs < 10)
			return String.format((Locale)null, "%.4f", d);
		if (abs < 100)
			return String.format((Locale)null, "%.3f", d);
		if (abs < 1000)
			return String.format((Locale)null, "%.2f", d);
		if (abs < 10000)
			return String.format((Locale)null, "%.1f", d);
		if (abs < 100000000.0)
			return String.format((Locale)null, "%.0f", d);
			
		// Compact exponential notation
		int exp = 0;
		while (abs >= 10.0) {
			abs /= 10;
			exp++;
		}
		
		String sign = (d < 0) ? "-" : "";
		return sign + String.format((Locale)null, "%.4fe%d", abs, exp);
	}

}
