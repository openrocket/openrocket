package net.sf.openrocket.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FractionUtilTest {
	
	@Test
	public void testParseFractions() {

		// zeros
		assertEquals( 0.0d, FractionUtil.parseFraction("0"), 0.0);
		assertEquals( 0.0d, FractionUtil.parseFraction("-0"), 0.0);
		assertEquals( 0.0d, FractionUtil.parseFraction("0/2"), 0.0);
		assertEquals( 0.0d, FractionUtil.parseFraction("-0/2"), 0.0);
		assertEquals( 0.0d, FractionUtil.parseFraction("0 0/4"), 0.0);
		assertEquals( 0.0d, FractionUtil.parseFraction("0 -0/4"), 0.0);
		
		// Simple fraction.
		assertEquals( 0.25, FractionUtil.parseFraction("1/4"),0.0);
		
		// ignores leading & trailing spaces
		assertEquals( 0.25, FractionUtil.parseFraction("  1/4  "),0.0);

		// non reduced fraction
		assertEquals( 0.25, FractionUtil.parseFraction("2/8"),0.0);
		
		// negative number
		assertEquals( -0.25, FractionUtil.parseFraction("-1/4"),0.0);

		// improper fraction
		assertEquals( 1.75, FractionUtil.parseFraction("7/4"),0.0);
		
		// negative improper fraction
		assertEquals( -1.75, FractionUtil.parseFraction("-7/4"),0.0);
		
		// two digit numerator & denominators
		assertEquals( 11d/16d, FractionUtil.parseFraction("11/16)"),0.0);
		assertEquals( -11d/16d, FractionUtil.parseFraction("-11/16)"),0.0);
		
		// Mixed fractions
		assertEquals( 1.25d, FractionUtil.parseFraction("1 1/4"),0.0);
		assertEquals( -1.25d, FractionUtil.parseFraction("-1 1/4"),0.0);
		
		// extra spaces
		assertEquals( 1.25d, FractionUtil.parseFraction("   1 1/4"),0.0);
		assertEquals( 1.25d, FractionUtil.parseFraction("1    1/4"),0.0);
		assertEquals( 1.25d, FractionUtil.parseFraction("1 1/4  "),0.0);
		
		assertEquals( 2.75d, FractionUtil.parseFraction("2 3/4"),0.0);
		assertEquals( 15.75d, FractionUtil.parseFraction("15 3/4"),0.0);
		assertEquals( 2.75d, FractionUtil.parseFraction("1 7/4"),0.0);

		assertEquals( 69d/64d, FractionUtil.parseFraction("1 5/64"),0.0);
		assertEquals( -69d/64d, FractionUtil.parseFraction("-1 5/64"),0.0);
	}
}
