package net.sf.openrocket.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MathUtil {
	private static final Logger log = LoggerFactory.getLogger(MathUtil.class);
	
	public static final double EPSILON = 0.00000001; // 10mm^3 in m^3
	
	/**
	 * The square of x (x^2).  On Sun's JRE using this method is as fast as typing x*x. 
	 * @param x  x
	 * @return   x^2
	 */
	public static double pow2(double x) {
		return x * x;
	}
	
	/**
	 * The cube of x (x^3).
	 * @param x  x
	 * @return   x^3
	 */
	public static double pow3(double x) {
		return x * x * x;
	}
	
	public static double pow4(double x) {
		return (x * x) * (x * x);
	}
	
	/**
	 * Clamps the value x to the range min - max.  
	 * @param x    Original value.
	 * @param min  Minimum value to return.
	 * @param max  Maximum value to return.
	 * @return     The clamped value.
	 */
	public static double clamp(double x, double min, double max) {
		if (x < min)
			return min;
		if (x > max)
			return max;
		return x;
	}
	
	public static float clamp(float x, float min, float max) {
		if (x < min)
			return min;
		if (x > max)
			return max;
		return x;
	}
	
	public static int clamp(int x, int min, int max) {
		if (x < min)
			return min;
		if (x > max)
			return max;
		return x;
	}
	
	
	/**
	 * Maps a value from one value range to another.
	 * 
	 * @param value		the value to map.
	 * @param fromMin	the minimum of the starting range.
	 * @param fromMax	the maximum of the starting range.
	 * @param toMin		the minimum of the destination range.
	 * @param toMax		the maximum of the destination range.
	 * @return			the mapped value.
	 * @throws	IllegalArgumentException  if fromMin == fromMax, but toMin != toMax.
	 */
	public static double map(double value, double fromMin, double fromMax,
			double toMin, double toMax) {
		if (equals(toMin, toMax))
			return toMin;
		if (equals(fromMin, fromMax)) {
			throw new IllegalArgumentException("from range is singular and to range is not: " +
					"value=" + value + " fromMin=" + fromMin + " fromMax=" + fromMax +
					"toMin=" + toMin + " toMax=" + toMax);
		}
		return (value - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin;
	}
	
	
	/**
	 * Maps a coordinate from one value range to another.
	 * 
	 * @param value		the value to map.
	 * @param fromMin	the minimum of the starting range.
	 * @param fromMax	the maximum of the starting range.
	 * @param toMin		the minimum coordinate of the destination;
	 * @param toMax		the maximum coordinate of the destination;
	 * @return			the mapped value.
	 * @throws	IllegalArgumentException  if fromMin == fromMax, but toMin != toMax.
	 */
	public static Coordinate map(double value, double fromMin, double fromMax,
			Coordinate toMin, Coordinate toMax) {
		if (toMin.equals(toMax))
			return toMin;
		if (equals(fromMin, fromMax)) {
			throw new IllegalArgumentException("from range is singular and to range is not: " +
					"value=" + value + " fromMin=" + fromMin + " fromMax=" + fromMax +
					"toMin=" + toMin + " toMax=" + toMax);
		}
		double a = (value - fromMin) / (fromMax - fromMin);
		return toMax.multiply(a).add(toMin.multiply(1 - a));
	}
	
	
	/**
	 * Compute the minimum of two values.  This is performed by direct comparison. 
	 * However, if one of the values is NaN and the other is not, the non-NaN value is
	 * returned.
	 */
	public static double min(double x, double y) {
		if (Double.isNaN(y))
			return x;
		return (x < y) ? x : y;
	}
	
	/**
	 * Compute the maximum of two values.  This is performed by direct comparison. 
	 * However, if one of the values is NaN and the other is not, the non-NaN value is
	 * returned.
	 */
	public static double max(double x, double y) {
		if (Double.isNaN(x))
			return y;
		return (x < y) ? y : x;
	}
	
	/**
	 * Compute the minimum of three values.  This is performed by direct comparison. 
	 * However, if one of the values is NaN and the other is not, the non-NaN value is
	 * returned.
	 */
	public static double min(double x, double y, double z) {
		if (x < y || Double.isNaN(y)) {
			return min(x, z);
		} else {
			return min(y, z);
		}
	}
	
	
	
	/**
	 * Compute the minimum of three values.  This is performed by direct comparison. 
	 * However, if one of the values is NaN and the other is not, the non-NaN value is
	 * returned.
	 */
	public static double min(double w, double x, double y, double z) {
		return min(min(w, x), min(y, z));
	}
	
	
	/**
	 * Compute the maximum of three values.  This is performed by direct comparison. 
	 * However, if one of the values is NaN and the other is not, the non-NaN value is
	 * returned.
	 */
	public static double max(double x, double y, double z) {
		if (x > y || Double.isNaN(y)) {
			return max(x, z);
		} else {
			return max(y, z);
		}
	}
	
	/**
	 * Calculates the hypotenuse <code>sqrt(x^2+y^2)</code>.  This method is SIGNIFICANTLY
	 * faster than <code>Math.hypot(x,y)</code>.
	 */
	public static double hypot(double x, double y) {
		return Math.sqrt(x * x + y * y);
	}
	
	/**
	 * Reduce the angle x to the range 0 - 2*PI.
	 * @param x  Original angle.
	 * @return   The equivalent angle in the range 0 ... 2*PI.
	 */
	public static double reduce360(double x) {
		double d = Math.floor(x / (2 * Math.PI));
		return x - d * 2 * Math.PI;
	}
	
	/**
	 * Reduce the angle x to the range -PI - PI.
	 * 
	 * Either -PI and PI might be returned, depending on the rounding function. 
	 * 
	 * @param x  Original angle.
	 * @return   The equivalent angle in the range -PI ... PI.
	 */
	public static double reduce180(double x) {
		double d = Math.rint(x / (2 * Math.PI));
		return x - d * 2 * Math.PI;
	}
	
	
	/**
	 * Return the square root of a value.  If the value is negative, zero is returned.
	 * This is safer in cases where rounding errors might make a value slightly negative.
	 * 
	 * @param d		the value of which the square root is to be taken.
	 * @return		the square root of the value.
	 */
	public static double safeSqrt(double d) {
		if (d < 0) {
			if (d < 0.01) {
				log.warn("Attempting to compute sqrt(" + d + ")");
			}
			return 0;
		}
		return Math.sqrt(d);
	}
	
	
	public static boolean equals(double a, double b, double epsilon) {
		double absb = Math.abs(b);
		
		if (absb < epsilon / 2) {
			// Near zero
			return Math.abs(a) < epsilon / 2;
		}
		return Math.abs(a - b) < epsilon * absb;
	}
	
	public static boolean equals(double a, double b) {
		return equals(a, b, EPSILON);
	}
	
	/**
	 * Return the sign of the number.  This corresponds to Math.signum, but ignores
	 * the special cases of zero and NaN.  The value returned for those is arbitrary.
	 * <p>
	 * This method is about 4 times faster than Math.signum().
	 * 
	 * @param x		the checked value.
	 * @return		-1.0 if x<0; 1.0 if x>0; otherwise either -1.0 or 1.0.
	 */
	public static double sign(double x) {
		return (x < 0) ? -1.0 : 1.0;
	}
	
	/* Math.abs() is about 3x as fast as this:

	public static double abs(double x) {
		return (x<0) ? -x : x;
	}
	 */
	
	
	public static double average(Collection<? extends Number> values) {
		if (values.isEmpty()) {
			return Double.NaN;
		}
		
		double avg = 0.0;
		int count = 0;
		for (Number n : values) {
			avg += n.doubleValue();
			count++;
		}
		return avg / count;
	}
	
	public static double stddev(Collection<? extends Number> values) {
		if (values.size() < 2) {
			return Double.NaN;
		}
		
		double avg = average(values);
		double stddev = 0.0;
		int count = 0;
		for (Number n : values) {
			stddev += pow2(n.doubleValue() - avg);
			count++;
		}
		stddev = Math.sqrt(stddev / (count - 1));
		return stddev;
	}
	
	public static double median(Collection<? extends Number> values) {
		if (values.isEmpty()) {
			return Double.NaN;
		}
		
		List<Number> sorted = new ArrayList<Number>(values);
		Collections.sort(sorted, new Comparator<Number>() {
			@Override
			public int compare(Number o1, Number o2) {
				return Double.compare(o1.doubleValue(), o2.doubleValue());
			}
		});
		
		int n = sorted.size();
		if (n % 2 == 0) {
			return (sorted.get(n / 2).doubleValue() + sorted.get(n / 2 - 1).doubleValue()) / 2;
		} else {
			return sorted.get(n / 2).doubleValue();
		}
	}
	
	/**
	 * Use interpolation to determine the value of the function at point t.
	 * Current implementation uses simple linear interpolation.   The domain
	 * and range lists must include the same number of values, t must be within
	 * the domain, and the domain list must be sorted.
	 * 
	 * @param domain list containing domain samples
	 * @param range list of corresponding range samples
	 * @param t domain value at which to interpolate
	 * @return returns Double.NaN if either list is null or empty or different size, or if t is outsize the domain.
	 */
	public static double interpolate(List<Double> domain, List<Double> range, double t) {
		
		if (domain == null || range == null || domain.size() != range.size()) {
			return Double.NaN;
		}
		
		int length = domain.size();
		if (length <= 1 || t < domain.get(0) || t > domain.get(length - 1)) {
			return Double.NaN;
		}
		
		// Look for the index of the right end point.
		int right = 1;
		while (t > domain.get(right)) {
			right++;
		}
		int left = right - 1;
		
		// Points are:
		
		double deltax = domain.get(right) - domain.get(left);
		double deltay = range.get(right) - range.get(left);
		
		// For numerical stability, if deltax is small,
		if (Math.abs(deltax) < EPSILON) {
			if (deltay < -1.0 * EPSILON) {
				// return neg infinity if deltay is negative
				return Double.NEGATIVE_INFINITY;
			}
			else if (deltay > EPSILON) {
				// return infinity if deltay is large
				return Double.POSITIVE_INFINITY;
			} else {
				// otherwise return 0
				return 0.0d;
			}
		}
		
		return range.get(left) + (t - domain.get(left)) * deltay / deltax;
		
	}
	
}
