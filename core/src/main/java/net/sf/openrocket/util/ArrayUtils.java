package net.sf.openrocket.util;

import java.lang.reflect.Array;

public class ArrayUtils {

	/**
	 * Returns a double array with values from start to end with given step.
	 * Starts exactly at start and stops at the multiple of step <= stop. 
	 */
	public static double[] range(double start, double stop, double step){
		
		int size = (int) Math.floor(((stop - start) / step)) + 1;
		
		//System.out.println("Range from "+start+" to "+stop+" step "+step+" has length "+size);
		
		double[] output = new double[size];
		int i = 0;
		double x = start;
		while (i<size){
			output[i] = x;
			x = x+step;
			i++;
		}
		
		return output;
	}

	/**
	 * Return the mean of an array
	 */
	public static double mean(double[] vals){
		double subtotal = 0;
		for (int i = 0; i < vals.length; i++ ){
			if (!Double.isNaN(vals[i])){
				subtotal += vals[i];
			}
		}
		subtotal = subtotal / vals.length;
		return subtotal;
	}

	/**
	 * Returns the maximum value in the array.
	 */

	public static double max(double[] vals) {
		double m = vals[0];
		for (int i = 1; i < vals.length; i++)
			m = Math.max(m, vals[i]);
		return m;
	}

	/**
	 * Returns the minimum value in the array.
	 */

	public static double min(double[] vals) {
		double m = vals[0];
		for (int i = 1; i < vals.length; i++)
			m = Math.min(m, vals[i]);
		return m;
	}

	/**
	 * Returns the variance of the array of doubles
	 */
	public static double variance(double[] vals) {
		double mu = mean(vals);
		double sumsq = 0.0;
		double temp = 0;
		for (int i = 0; i < vals.length; i++){
			if (!Double.isNaN(vals[i])){
				temp = (mu - vals[i]);
				sumsq += temp*temp;
			}
		}
		return sumsq / (vals.length);
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
		return Math.sqrt( m*m + s*s );
	}
	
	/**
	 * Returns the integral of a given array calculated by the trapezoidal rule
	 * dt is the time step between each array value. Any NaN values are treated as zero
	 */
	public static double trapz(double[] y, double dt){
		double stop = (y.length -1) * dt;
		
		if (y.length <= 1 || dt <= 0) return 0;
		
		double[] x = range(0, stop, dt);
	    
	    double sum = 0.0;
	    for (int i = 1; i < x.length; i++) {
	        double temp = (x[i] - x[i-1]) * (y[i] + y[i-1]);
	        if (!Double.isNaN(temp)){
	        	sum += temp;
	        }
	    }
	    return sum * 0.5;
	}
	
	/**
	 * Returns the nearest value in an array to a given value
	 * Search starts from the lowest array index
	 */
	public static double tnear(double[] range, double near, double start, double step){
		double min = Double.POSITIVE_INFINITY;
		int mini = 0;
		
		//System.out.println("Nearest to "+near+" in range length "+range.length);
		for (int i=0; i < range.length; i++){
			double x = Math.abs(range[i] - near);
			if (x < min){
				min = x;
				mini = i;
			}
		}
		
		//System.out.println("Found nearest at i="+mini);		
		return start + (mini*step);
	}
	
	
	public static <T> T[] copyOf( T[] original, int length ) {
		return copyOfRange(original,0,length);
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
	public static <T> T[] copyOfRange( T[] original, int start, int end ) {
		
		if ( original == null ) {
			throw new NullPointerException();
		}
		
		if ( start < 0 || start > original.length ) {
			throw new ArrayIndexOutOfBoundsException();
		}
		
		if ( start > end ) {
			throw new IllegalArgumentException();
		}
		
		@SuppressWarnings("unchecked")
		T[] result = (T[]) Array.newInstance( original.getClass().getComponentType(), end-start );
		
		int index = 0;
		int stop = original.length < end ? original.length : end;
		for ( int i = start; i < stop; i ++ ) {
			if ( i < original.length ) {
				result[index] = original[i];
			}
			index++;
		}
		
		return result;
		
	}

	public static double[] copyOf( double[] original, int length ) {
		return copyOfRange(original,0,length);
	}
	
	public static double[] copyOfRange( double[] original, int start, int end ) {
		
		if ( original == null ) {
			throw new NullPointerException();
		}
		
		if ( start < 0 || start > original.length ) {
			throw new ArrayIndexOutOfBoundsException();
		}
		
		if ( start > end ) {
			throw new IllegalArgumentException();
		}
		
		double[] result = new double[(end-start)];
		
		int index = 0;
		int stop = original.length < end ? original.length : end;
		for ( int i = start; i < stop; i ++ ) {
			if ( i < original.length ) {
				result[index] = original[i];
			}
			index++;
		}
		
		return result;
		
	}

	public static byte[] copyOf( byte[] original, int length ) {
		return copyOfRange(original,0,length);
	}
	
	public static byte[] copyOfRange( byte[] original, int start, int end ) {
		
		if ( original == null ) {
			throw new NullPointerException();
		}
		
		if ( start < 0 || start > original.length ) {
			throw new ArrayIndexOutOfBoundsException();
		}
		
		if ( start > end ) {
			throw new IllegalArgumentException();
		}
		
		byte[] result = new byte[(end-start)];
		
		int index = 0;
		int stop = original.length < end ? original.length : end;
		for ( int i = start; i < stop; i ++ ) {
			if ( i < original.length ) {
				result[index] = original[i];
			}
			index++;
		}
		
		return result;
		
	}

}

