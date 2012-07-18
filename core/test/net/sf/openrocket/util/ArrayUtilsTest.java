package net.sf.openrocket.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ArrayUtilsTest {

	@Test(expected=NullPointerException.class)
	public void testCopyOfRange_NullArg() {
		ArrayUtils.copyOfRange( (Byte[]) null, 0 , 14);
	}
	
	@Test(expected=ArrayIndexOutOfBoundsException.class)
	public void testCopyOfRange_StartTooBig() {
		Integer[] original = new Integer[5];
		ArrayUtils.copyOfRange( original, 8 , 14);
	}
	
	@Test(expected=ArrayIndexOutOfBoundsException.class)
	public void testCopyOfRange_StartTooSmall() {
		Integer[] original = new Integer[5];
		ArrayUtils.copyOfRange( original, -1 , 14);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCopyOfRange_IllegalRange() {
		Integer[] original = new Integer[5];
		ArrayUtils.copyOfRange( original, 5, 0 );
	}
	
	@Test
	public void testCopyOfRange() {
		Integer[] original = new Integer[5];
		for ( int i =0; i < 5; i++ ) {
			original[i] = i;
		}
		Integer[] copy = ArrayUtils.copyOfRange( original, 0, 0 );
		assertEquals( 0, copy.length );
		
		copy = ArrayUtils.copyOfRange( original, 2, 2 );
		assertEquals( 0, copy.length );
		
		copy = ArrayUtils.copyOfRange( original, 0, 2 );
		assertEquals( 2, copy.length );
		for( int i =0; i< 2; i++ ) {
			assertEquals( original[i], copy[i] );
		}

		copy = ArrayUtils.copyOfRange( original, 2, 5 );
		assertEquals( 3, copy.length );
		for( int i =0; i< 3; i++ ) {
			assertEquals( original[i+2], copy[i] );
		}

		copy = ArrayUtils.copyOfRange( original, 2, 15 );
		assertEquals( 13, copy.length );
		for( int i =0; i< 3; i++ ) {
			assertEquals( original[i+2], copy[i] );
		}
		for ( int i=3; i< 13; i++ ) {
			assertNull(copy[i]);
		}

	}

	@Test
	public void testCopyOfRange_ZeroSize() {
		Integer[] original = new Integer[0];

		Integer[] copy = ArrayUtils.copyOfRange( original, 0, 0 );
		assertEquals( 0, copy.length );
		
		copy = ArrayUtils.copyOfRange( original, 0, 2 );
		assertEquals( 2, copy.length );
		for( int i =0; i< 2; i++ ) {
			assertEquals( null, copy[i] );
		}

	}
	

}
