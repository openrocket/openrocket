package net.sf.openrocket.unit;

import static org.junit.Assert.*;

import org.junit.Test;

public class FractionalUnitTest {

	private final static Unit testUnit = new FractionalUnit(1, "unit", "unit", 4, 0.5);
	private final static Unit testUnitApprox = new FractionalUnit(1, "unit", "unit", 16, 0.5, 0.02);

	private final static Unit inchUnit = new FractionalUnit(0.0254, "in/64", "in", 64, 1d/16d);
	
	@Test
	public void testRound() {

		assertEquals(-1d, testUnit.round(-1.125), 0.0); // rounds to -1 since mod is even
		assertEquals(-1d, testUnit.round(-1.0), 0.0);
		assertEquals(-1d, testUnit.round(-.875), 0.0); // rounds to -1 since mod is even

		assertEquals(-0.75d, testUnit.round(-.874), 0.0);
		assertEquals(-0.75d, testUnit.round(-.75), 0.0);
		assertEquals(-0.75d, testUnit.round(-.626), 0.0);

		assertEquals(-0.5d, testUnit.round(-.625), 0.0); // rounds to -.5 since mod is even
		assertEquals(-0.5d, testUnit.round(-.5), 0.0);
		assertEquals(-0.5d, testUnit.round(-.375), 0.0); // rounds to -.5 since mod is even

		assertEquals(-0.25d, testUnit.round(-.374), 0.0);
		assertEquals(-0.25d, testUnit.round(-.25), 0.0);
		assertEquals(-0.25d, testUnit.round(-.126), 0.0);

		assertEquals(0d, testUnit.round(-.125), 0.0);
		assertEquals(0d, testUnit.round(0), 0.0);
		assertEquals(0d, testUnit.round(.125), 0.0);

		assertEquals(0.25d, testUnit.round(.126), 0.0);
		assertEquals(0.25d, testUnit.round(.25), 0.0);
		assertEquals(0.25d, testUnit.round(.374), 0.0);

		assertEquals(0.5d, testUnit.round(.375), 0.0); // rounds to .5 since mod is even
		assertEquals(0.5d, testUnit.round(.5), 0.0);
		assertEquals(0.5d, testUnit.round(.625), 0.0); // rounds to .5 since mod is even

		assertEquals(0.75d, testUnit.round(.626), 0.0);
		assertEquals(0.75d, testUnit.round(.75), 0.0);
		assertEquals(0.75d, testUnit.round(.874), 0.0);

		assertEquals(1d, testUnit.round(.875), 0.0); // rounds to 1 since mod is even
		assertEquals(1d, testUnit.round(1.0), 0.0);
		assertEquals(1d, testUnit.round(1.125), 0.0); // rounds to 1 since mod is even

	}

	@Test
	public void testIncrement() {

		assertEquals( -1d, testUnit.getNextValue(-1.2), 0.0);
		assertEquals( -1d, testUnit.getNextValue(-1.4), 0.0);

		assertEquals( -0.5d, testUnit.getNextValue(-0.7), 0.0);
		assertEquals( -0.5d, testUnit.getNextValue(-0.9), 0.0);
		assertEquals( -0.5d, testUnit.getNextValue(-1.0), 0.0);

		assertEquals( 0.0d, testUnit.getNextValue(-0.05), 0.0 );
		assertEquals( 0.0d, testUnit.getNextValue(-0.062), 0.0 );
		assertEquals( 0.0d, testUnit.getNextValue(-0.07), 0.0 );
		assertEquals( 0.0d, testUnit.getNextValue(-0.11), 0.0 );

		assertEquals( 0.5d, testUnit.getNextValue(0), 0.0 );
		assertEquals( 0.5d, testUnit.getNextValue(0.01), 0.0 );
		assertEquals( 0.5d, testUnit.getNextValue(0.062), 0.0 );
		assertEquals( 0.5d, testUnit.getNextValue(0.0625), 0.0);

		assertEquals( 1d, testUnit.getNextValue(0.51), 0.0);
		assertEquals( 1d, testUnit.getNextValue(0.7), 0.0);
	}

	@Test
	public void testDecrement() {

		assertEquals( -1.5d, testUnit.getPreviousValue(-1.2), 0.0);
		assertEquals( -1.5d, testUnit.getPreviousValue(-1.4), 0.0);
		assertEquals( -1.5d, testUnit.getPreviousValue(-1.0), 0.0);

		assertEquals( -1d, testUnit.getPreviousValue(-0.7), 0.0);
		assertEquals( -1d, testUnit.getPreviousValue(-0.9), 0.0);

		assertEquals( -0.5d, testUnit.getPreviousValue(-0.01), 0.0 );
		assertEquals( -0.5d, testUnit.getPreviousValue(-0.05), 0.0 );
		assertEquals( -0.5d, testUnit.getPreviousValue(-0.062), 0.0 );
		assertEquals( -0.5d, testUnit.getPreviousValue(-0.07), 0.0 );
		assertEquals( -0.5d, testUnit.getPreviousValue(0), 0.0 );

		assertEquals( 0.0d, testUnit.getPreviousValue(0.49), 0.0 );
		assertEquals( 0.0d, testUnit.getPreviousValue(0.262), 0.0 );
		assertEquals( 0.0d, testUnit.getPreviousValue(0.51), 0.0);

		assertEquals( 0.5d, testUnit.getPreviousValue(0.7), 0.0);

		assertEquals( 1.0d, testUnit.getPreviousValue(1.2), 0.0);
	}

	@Test
	public void testToStringDefaultPrecision() {

		// default epsilon is 0.025
		assertEquals("-1.2", testUnit.toString(-1.2)); 
		assertEquals("-1 \u00B9\u2044\u2084", testUnit.toString(-1.225));
		assertEquals("-1 \u00B9\u2044\u2084", testUnit.toString(-1.227));
		assertEquals("-1 \u00B9\u2044\u2084", testUnit.toString(-1.25));
		assertEquals("-1 \u00B9\u2044\u2084", testUnit.toString(-1.25));
		assertEquals("-1 \u00B9\u2044\u2084", testUnit.toString(-1.275));
		assertEquals("-1.3", testUnit.toString(-1.3));

		assertEquals("-0.2", testUnit.toString(-.2));
		assertEquals("-\u00B9\u2044\u2084", testUnit.toString(-.225));
		assertEquals("-\u00B9\u2044\u2084", testUnit.toString(-.25));
		assertEquals("-\u00B9\u2044\u2084", testUnit.toString(-.274));
		//assertEquals("-1/4", testUnit.toString(-.275)); // this has roundoff error which pushes it over epsilon
		assertEquals("-0.3", testUnit.toString(-.3));

		assertEquals("-0.1", testUnit.toString(-.1));
		assertEquals("0", testUnit.toString(-0.024));
		assertEquals("0", testUnit.toString(0));
		assertEquals("0", testUnit.toString(.024));
		assertEquals("0.1", testUnit.toString(.1));

		assertEquals("0.2", testUnit.toString(.2));
		assertEquals("\u00B9\u2044\u2084", testUnit.toString(.225));
		assertEquals("\u00B9\u2044\u2084", testUnit.toString(.25));
		assertEquals("\u00B9\u2044\u2084", testUnit.toString(.274));
		assertEquals("0.3", testUnit.toString(.3));

		assertEquals("1.2", testUnit.toString(1.2));
		assertEquals("1 \u00B9\u2044\u2084", testUnit.toString(1.225));
		assertEquals("1 \u00B9\u2044\u2084", testUnit.toString(1.25));
		assertEquals("1 \u00B9\u2044\u2084", testUnit.toString(1.275));
		assertEquals("1.3", testUnit.toString(1.3));

	}

	@Test
	public void testToStringWithPrecision() {

		// epsilon is .02
		assertEquals("-1 \u00B3\u2044\u2081\u2086", testUnitApprox.toString(-1.2));
		assertEquals("-1.225", testUnitApprox.toString(-1.225));
		assertEquals("-1 \u00B9\u2044\u2084", testUnitApprox.toString(-1.25));
		assertEquals("-1.275", testUnitApprox.toString(-1.275));
		assertEquals("-1 \u2075\u2044\u2081\u2086", testUnitApprox.toString(-1.3));

		assertEquals("-\u00B3\u2044\u2081\u2086", testUnitApprox.toString(-.2));
		assertEquals("-0.225", testUnitApprox.toString(-.225));
		assertEquals("-\u00B9\u2044\u2084", testUnitApprox.toString(-.25));
		assertEquals("-0.275", testUnitApprox.toString(-.275));
		assertEquals("-\u2075\u2044\u2081\u2086", testUnitApprox.toString(-.3));

		assertEquals("-0.1", testUnitApprox.toString(-.1));
		assertEquals("-0.024", testUnitApprox.toString(-0.024));
		assertEquals("0", testUnitApprox.toString(0));
		assertEquals("0.024", testUnitApprox.toString(.024));
		assertEquals("0.1", testUnitApprox.toString(.1));

		assertEquals("\u00B3\u2044\u2081\u2086", testUnitApprox.toString(.2));
		assertEquals("0.225", testUnitApprox.toString(.225));
		assertEquals("\u00B9\u2044\u2084", testUnitApprox.toString(.25));
		assertEquals("0.275", testUnitApprox.toString(.275));
		assertEquals("\u2075\u2044\u2081\u2086", testUnitApprox.toString(.3));

		assertEquals("1 \u00B3\u2044\u2081\u2086", testUnitApprox.toString(1.2));
		assertEquals("1.225", testUnitApprox.toString(1.225));
		assertEquals("1 \u00B9\u2044\u2084", testUnitApprox.toString(1.25));
		assertEquals("1.275", testUnitApprox.toString(1.275));
		assertEquals("1 \u2075\u2044\u2081\u2086", testUnitApprox.toString(1.3));

	}
	
	@Test
	public void testInchToString() {
		
		// Just some random test points.
		assertEquals( "\u00B9\u2044\u2086\u2084", inchUnit.toString( 1d/64d*0.0254));
		
		assertEquals( "-\u2075\u2044\u2086\u2084", inchUnit.toString( -5d/64d*0.0254));
		
		assertEquals( "4 \u00B9\u2044\u2082", inchUnit.toString( 9d/2d*0.0254));
		
		assertEquals( "0.002", inchUnit.toString( 0.002*0.0254));
		
		// default body tube length:
		double length = 8d * 0.025;
		
		assertEquals( "7 \u2077\u2044\u2088", inchUnit.toString( length) );
		
		// had problems with roundoff in decrement.
		
		double v = inchUnit.toUnit(length);
		for ( int i = 0; i< 15; i++ ) {
			assertTrue( v > inchUnit.getPreviousValue(v) );
			v = inchUnit.getPreviousValue(v);
		}
		
	}

}
