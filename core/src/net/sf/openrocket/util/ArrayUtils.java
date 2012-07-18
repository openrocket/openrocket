package net.sf.openrocket.util;

import java.lang.reflect.Array;

public class ArrayUtils {

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

}
