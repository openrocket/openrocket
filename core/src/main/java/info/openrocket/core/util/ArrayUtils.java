package info.openrocket.core.util;

import java.lang.reflect.Array;

public class ArrayUtils {

	/**
	 * Returns a double array with values from start to end with given step.
	 * Starts exactly at start and stops at the multiple of step <= stop.
	 * An empty array is returned if the step is not positive, if either bound is
	 * NaN, or if stop is before start.
	 */
	public static double[] range(double start, double stop, double step) {

		if (!(step > 0) || Double.isNaN(start) || Double.isNaN(stop)) {
			return new double[0];
		}

		double count = Math.floor((stop - start) / step) + 1;
		if (!(count > 0)) {
			return new double[0];
		}

		int size = (int) count;

		// System.out.println("Range from "+start+" to "+stop+" step "+step+" has length
		// "+size);

		double[] output = new double[size];
		int i = 0;
		double x = start;
		while (i < size) {
			output[i] = x;
			x = x + step;
			i++;
		}

		return output;
	}

	/**
	 * Return the mean of an array, ignoring any NaN values.
	 * Returns NaN if the array contains no usable values.
	 */
	public static double mean(double[] vals) {
		double subtotal = 0;
		int count = 0;
		for (double val : vals) {
			if (!Double.isNaN(val)) {
				subtotal += val;
				count++;
			}
		}
		if (count == 0) {
			return Double.NaN;
		}
		return subtotal / count;
	}

	/**
	 * Returns the maximum value in the array, or NaN if the array is empty.
	 */

	public static double max(double[] vals) {
		if (vals.length == 0) {
			return Double.NaN;
		}
		double m = vals[0];
		for (int i = 1; i < vals.length; i++)
			m = Math.max(m, vals[i]);
		return m;
	}

	/**
	 * Returns the minimum value in the array, or NaN if the array is empty.
	 */

	public static double min(double[] vals) {
		if (vals.length == 0) {
			return Double.NaN;
		}
		double m = vals[0];
		for (int i = 1; i < vals.length; i++)
			m = Math.min(m, vals[i]);
		return m;
	}

	/**
	 * Returns the variance of the array of doubles, ignoring any NaN values.
	 * Returns NaN if the array contains no usable values.
	 */
	public static double variance(double[] vals) {
		double mu = mean(vals);
		double sumsq = 0.0;
		double temp = 0;
		int count = 0;
		for (double val : vals) {
			if (!Double.isNaN(val)) {
				temp = (mu - val);
				sumsq += temp * temp;
				count++;
			}
		}
		if (count == 0) {
			return Double.NaN;
		}
		return sumsq / count;
	}

	/**
	 * Returns the standard deviation of an array of doubles
	 */
	public static double stdev(double[] vals) {
		return Math.sqrt(variance(vals));
	}

	/**
	 * Returns the RMS value of an array of doubles
	 */
	public static double rms(double[] vals) {
		double m = mean(vals);
		double s = stdev(vals);
		return Math.sqrt(m * m + s * s);
	}

	/**
	 * Returns the integral of a given array calculated by the trapezoidal rule
	 * dt is the time step between each array value. Any NaN values are treated as
	 * zero
	 */
	public static double trapz(double[] y, double dt) {
		double stop = (y.length - 1) * dt;

		if (y.length <= 1 || dt <= 0)
			return 0;

		double[] x = range(0, stop, dt);

		double sum = 0.0;
		for (int i = 1; i < x.length; i++) {
			double temp = (x[i] - x[i - 1]) * (y[i] + y[i - 1]);
			if (!Double.isNaN(temp)) {
				sum += temp;
			}
		}
		return sum * 0.5;
	}

	/**
	 * Find which element in a uniformly sampled array sits closest to the target
	 * Search starts from the lowest array index
	 *
	 * @param range the array to search
	 * @param near  the value to find the nearest to
	 * @param start the starting value corresponding to range[0]
	 * @param step  the step size between each value in range
	 * @return the value in the range array nearest to 'near'
	 */
	public static double tnear(double[] range, double near, double start, double step) {
		// TODO: I think this can be rewritten a lot better, without the start and step, and supporting non-uniform arrays
		double min = Double.POSITIVE_INFINITY;
		int minIdx = 0;

		for (int i = 0; i < range.length; i++) {
			double x = Math.abs(range[i] - near);
			if (x < min) {
				min = x;
				minIdx = i;
			}
		}

		return start + (minIdx * step);
	}

	public static <T> T[] copyOf(T[] original, int length) {
		return copyOfRange(original, 0, length);
	}

	/**
	 * Implementation of java.util.Arrays.copyOfRange
	 * 
	 * Since Froyo does not include this function it must be implemented here.
	 * 
	 * @param original
	 * @param start
	 * @param end
	 * @return
	 */
	public static <T> T[] copyOfRange(T[] original, int start, int end) {

		if (original == null) {
			throw new NullPointerException();
		}

		if (start < 0 || start > original.length) {
			throw new ArrayIndexOutOfBoundsException();
		}

		if (start > end) {
			throw new IllegalArgumentException();
		}

		@SuppressWarnings("unchecked")
		T[] result = (T[]) Array.newInstance(original.getClass().getComponentType(), end - start);

		int index = 0;
		int stop = original.length < end ? original.length : end;
		for (int i = start; i < stop; i++) {
			if (i < original.length) {
				result[index] = original[i];
			}
			index++;
		}

		return result;

	}

	public static double[] copyOf(double[] original, int length) {
		return copyOfRange(original, 0, length);
	}

	public static double[] copyOfRange(double[] original, int start, int end) {

		if (original == null) {
			throw new NullPointerException();
		}

		if (start < 0 || start > original.length) {
			throw new ArrayIndexOutOfBoundsException();
		}

		if (start > end) {
			throw new IllegalArgumentException();
		}

		double[] result = new double[(end - start)];

		int index = 0;
		int stop = original.length < end ? original.length : end;
		for (int i = start; i < stop; i++) {
			if (i < original.length) {
				result[index] = original[i];
			}
			index++;
		}

		return result;

	}

	public static byte[] copyOf(byte[] original, int length) {
		return copyOfRange(original, 0, length);
	}

	public static byte[] copyOfRange(byte[] original, int start, int end) {

		if (original == null) {
			throw new NullPointerException();
		}

		if (start < 0 || start > original.length) {
			throw new ArrayIndexOutOfBoundsException();
		}

		if (start > end) {
			throw new IllegalArgumentException();
		}

		byte[] result = new byte[(end - start)];

		int index = 0;
		int stop = original.length < end ? original.length : end;
		for (int i = start; i < stop; i++) {
			if (i < original.length) {
				result[index] = original[i];
			}
			index++;
		}

		return result;

	}

}
