package net.sf.openrocket.util;

import static java.lang.Math.PI;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TextUtilTest {

	@Test
	public void specialCaseTest() {
		assertEquals("NaN",TextUtil.doubleToString(Double.NaN));
		assertEquals("Inf",TextUtil.doubleToString(Double.POSITIVE_INFINITY));
		assertEquals("-Inf",TextUtil.doubleToString(Double.NEGATIVE_INFINITY));
		assertEquals("0",TextUtil.doubleToString(0.0));
		assertEquals("0",TextUtil.doubleToString(MathUtil.EPSILON/3));
		assertEquals("0",TextUtil.doubleToString(-MathUtil.EPSILON/3));
	}
	
	@Test
	public void longTest() {
		
		assertEquals("3.1416e-5", TextUtil.doubleToString(PI*1e-5));
		assertEquals("3.1416e-4", TextUtil.doubleToString(PI*1e-4));
		assertEquals("0.0031416", TextUtil.doubleToString(PI*1e-3));
		assertEquals("0.031416",  TextUtil.doubleToString(PI*1e-2));
		assertEquals("0.31416",   TextUtil.doubleToString(PI*1e-1));
		assertEquals("3.1416",    TextUtil.doubleToString(PI));
		assertEquals("31.416",    TextUtil.doubleToString(PI*1e1));
		assertEquals("314.16",    TextUtil.doubleToString(PI*1e2));
		assertEquals("3141.6",    TextUtil.doubleToString(PI*1e3));
		assertEquals("31416",     TextUtil.doubleToString(PI*1e4));
		assertEquals("314159",    TextUtil.doubleToString(PI*1e5));
		assertEquals("3141593",   TextUtil.doubleToString(PI*1e6));
		assertEquals("31415927",  TextUtil.doubleToString(PI*1e7));
		assertEquals("3.1416e8",  TextUtil.doubleToString(PI*1e8));
		assertEquals("3.1416e9",  TextUtil.doubleToString(PI*1e9));
		assertEquals("3.1416e10", TextUtil.doubleToString(PI*1e10));

		assertEquals("-3.1416e-5", TextUtil.doubleToString(-PI*1e-5));
		assertEquals("-3.1416e-4", TextUtil.doubleToString(-PI*1e-4));
		assertEquals("-0.0031416", TextUtil.doubleToString(-PI*1e-3));
		assertEquals("-0.031416",  TextUtil.doubleToString(-PI*1e-2));
		assertEquals("-0.31416",   TextUtil.doubleToString(-PI*1e-1));
		assertEquals("-3.1416",    TextUtil.doubleToString(-PI));
		assertEquals("-31.416",    TextUtil.doubleToString(-PI*1e1));
		assertEquals("-314.16",    TextUtil.doubleToString(-PI*1e2));
		assertEquals("-3141.6",    TextUtil.doubleToString(-PI*1e3));
		assertEquals("-31416",     TextUtil.doubleToString(-PI*1e4));
		assertEquals("-314159",    TextUtil.doubleToString(-PI*1e5));
		assertEquals("-3141593",   TextUtil.doubleToString(-PI*1e6));
		assertEquals("-31415927",  TextUtil.doubleToString(-PI*1e7));
		assertEquals("-3.1416e8",  TextUtil.doubleToString(-PI*1e8));
		assertEquals("-3.1416e9",  TextUtil.doubleToString(-PI*1e9));
		assertEquals("-3.1416e10", TextUtil.doubleToString(-PI*1e10));

	}
	
	@Test
	public void shortTest() {
		double p = 3.1;
		assertEquals("3.1e-5", TextUtil.doubleToString(p*1e-5));
		assertEquals("3.1e-4", TextUtil.doubleToString(p*1e-4));
		assertEquals("0.0031", TextUtil.doubleToString(p*1e-3));
		assertEquals("0.031",  TextUtil.doubleToString(p*1e-2));
		assertEquals("0.31",   TextUtil.doubleToString(p*1e-1));
		assertEquals("3.1",    TextUtil.doubleToString(p));
		assertEquals("31",     TextUtil.doubleToString(p*1e1));
		assertEquals("310",    TextUtil.doubleToString(p*1e2));
		assertEquals("3100",   TextUtil.doubleToString(p*1e3));
		assertEquals("31000",  TextUtil.doubleToString(p*1e4));
		assertEquals("3.1e5",  TextUtil.doubleToString(p*1e5));
		assertEquals("3.1e6",  TextUtil.doubleToString(p*1e6));
		assertEquals("3.1e7",  TextUtil.doubleToString(p*1e7));
		assertEquals("3.1e8",  TextUtil.doubleToString(p*1e8));
		assertEquals("3.1e9",  TextUtil.doubleToString(p*1e9));
		assertEquals("3.1e10", TextUtil.doubleToString(p*1e10));

		assertEquals("-3.1e-5", TextUtil.doubleToString(-p*1e-5));
		assertEquals("-3.1e-4", TextUtil.doubleToString(-p*1e-4));
		assertEquals("-0.0031", TextUtil.doubleToString(-p*1e-3));
		assertEquals("-0.031",  TextUtil.doubleToString(-p*1e-2));
		assertEquals("-0.31",   TextUtil.doubleToString(-p*1e-1));
		assertEquals("-3.1",    TextUtil.doubleToString(-p));
		assertEquals("-31",     TextUtil.doubleToString(-p*1e1));
		assertEquals("-310",    TextUtil.doubleToString(-p*1e2));
		assertEquals("-3100",   TextUtil.doubleToString(-p*1e3));
		assertEquals("-31000",  TextUtil.doubleToString(-p*1e4));
		assertEquals("-3.1e5",  TextUtil.doubleToString(-p*1e5));
		assertEquals("-3.1e6",  TextUtil.doubleToString(-p*1e6));
		assertEquals("-3.1e7",  TextUtil.doubleToString(-p*1e7));
		assertEquals("-3.1e8",  TextUtil.doubleToString(-p*1e8));
		assertEquals("-3.1e9",  TextUtil.doubleToString(-p*1e9));
		assertEquals("-3.1e10", TextUtil.doubleToString(-p*1e10));

		p = 3;
		assertEquals("3e-5", TextUtil.doubleToString(p*1e-5));
		assertEquals("3e-4", TextUtil.doubleToString(p*1e-4));
		assertEquals("3e-3", TextUtil.doubleToString(p*1e-3));
		assertEquals("0.03", TextUtil.doubleToString(p*1e-2));
		assertEquals("0.3",  TextUtil.doubleToString(p*1e-1));
		assertEquals("3",    TextUtil.doubleToString(p));
		assertEquals("30",   TextUtil.doubleToString(p*1e1));
		assertEquals("300",  TextUtil.doubleToString(p*1e2));
		assertEquals("3e3",  TextUtil.doubleToString(p*1e3));
		assertEquals("3e4",  TextUtil.doubleToString(p*1e4));
		assertEquals("3e5",  TextUtil.doubleToString(p*1e5));
		assertEquals("3e6",  TextUtil.doubleToString(p*1e6));
		assertEquals("3e7",  TextUtil.doubleToString(p*1e7));
		assertEquals("3e8",  TextUtil.doubleToString(p*1e8));
		assertEquals("3e9",  TextUtil.doubleToString(p*1e9));
		assertEquals("3e10", TextUtil.doubleToString(p*1e10));

		assertEquals("-3e-5", TextUtil.doubleToString(-p*1e-5));
		assertEquals("-3e-4", TextUtil.doubleToString(-p*1e-4));
		assertEquals("-3e-3", TextUtil.doubleToString(-p*1e-3));
		assertEquals("-0.03", TextUtil.doubleToString(-p*1e-2));
		assertEquals("-0.3",  TextUtil.doubleToString(-p*1e-1));
		assertEquals("-3",    TextUtil.doubleToString(-p));
		assertEquals("-30",   TextUtil.doubleToString(-p*1e1));
		assertEquals("-300",  TextUtil.doubleToString(-p*1e2));
		assertEquals("-3e3",  TextUtil.doubleToString(-p*1e3));
		assertEquals("-3e4",  TextUtil.doubleToString(-p*1e4));
		assertEquals("-3e5",  TextUtil.doubleToString(-p*1e5));
		assertEquals("-3e6",  TextUtil.doubleToString(-p*1e6));
		assertEquals("-3e7",  TextUtil.doubleToString(-p*1e7));
		assertEquals("-3e8",  TextUtil.doubleToString(-p*1e8));
		assertEquals("-3e9",  TextUtil.doubleToString(-p*1e9));
		assertEquals("-3e10", TextUtil.doubleToString(-p*1e10));

	}
	
	@Test
	public void roundingTest() {
		
		assertEquals("1.001", TextUtil.doubleToString(1.00096));
		
		
		/*
		 * Not testing with 1.00015 because it might be changed during number formatting
		 * calculations.  Its rounding is basically arbitrary anyway.
		 */
		
		assertEquals("1.0002e-5", TextUtil.doubleToString(1.0001500001e-5));
		assertEquals("1.0001e-5", TextUtil.doubleToString(1.0001499999e-5));
		assertEquals("1.0002e-4", TextUtil.doubleToString(1.0001500001e-4));
		assertEquals("1.0001e-4", TextUtil.doubleToString(1.0001499999e-4));
		assertEquals("0.0010002", TextUtil.doubleToString(1.0001500001e-3));
		assertEquals("0.0010001", TextUtil.doubleToString(1.0001499999e-3));
		assertEquals("0.010002",  TextUtil.doubleToString(1.0001500001e-2));
		assertEquals("0.010001",  TextUtil.doubleToString(1.0001499999e-2));
		assertEquals("0.10002",   TextUtil.doubleToString(1.0001500001e-1));
		assertEquals("0.10001",   TextUtil.doubleToString(1.0001499999e-1));
		assertEquals("1.0002",    TextUtil.doubleToString(1.0001500001));
		assertEquals("1.0001",    TextUtil.doubleToString(1.0001499999));
		assertEquals("10.002",    TextUtil.doubleToString(1.0001500001e1));
		assertEquals("10.001",    TextUtil.doubleToString(1.0001499999e1));
		assertEquals("100.02",    TextUtil.doubleToString(1.0001500001e2));
		assertEquals("100.01",    TextUtil.doubleToString(1.0001499999e2));
		assertEquals("1000.2",    TextUtil.doubleToString(1.0001500001e3));
		assertEquals("1000.1",    TextUtil.doubleToString(1.0001499999e3));
		assertEquals("10002",     TextUtil.doubleToString(1.0001500001e4));
		assertEquals("10001",     TextUtil.doubleToString(1.0001499999e4));
		assertEquals("100012",    TextUtil.doubleToString(1.00011500001e5));
		assertEquals("100011",    TextUtil.doubleToString(1.00011499999e5));
		assertEquals("1000112",   TextUtil.doubleToString(1.000111500001e6));
		assertEquals("1000111",   TextUtil.doubleToString(1.000111499999e6));
		assertEquals("10001112",  TextUtil.doubleToString(1.0001111500001e7));
		assertEquals("10001111",  TextUtil.doubleToString(1.0001111499999e7));
		assertEquals("1.0002e8",  TextUtil.doubleToString(1.0001500001e8));
		assertEquals("1.0001e8",  TextUtil.doubleToString(1.0001499999e8));
		assertEquals("1.0002e9",  TextUtil.doubleToString(1.0001500001e9));
		assertEquals("1.0001e9",  TextUtil.doubleToString(1.0001499999e9));
		assertEquals("1.0002e10", TextUtil.doubleToString(1.0001500001e10));
		assertEquals("1.0001e10", TextUtil.doubleToString(1.0001499999e10));
		

		assertEquals("-1.0002e-5", TextUtil.doubleToString(-1.0001500001e-5));
		assertEquals("-1.0001e-5", TextUtil.doubleToString(-1.0001499999e-5));
		assertEquals("-1.0002e-4", TextUtil.doubleToString(-1.0001500001e-4));
		assertEquals("-1.0001e-4", TextUtil.doubleToString(-1.0001499999e-4));
		assertEquals("-0.0010002", TextUtil.doubleToString(-1.0001500001e-3));
		assertEquals("-0.0010001", TextUtil.doubleToString(-1.0001499999e-3));
		assertEquals("-0.010002",  TextUtil.doubleToString(-1.0001500001e-2));
		assertEquals("-0.010001",  TextUtil.doubleToString(-1.0001499999e-2));
		assertEquals("-0.10002",   TextUtil.doubleToString(-1.0001500001e-1));
		assertEquals("-0.10001",   TextUtil.doubleToString(-1.0001499999e-1));
		assertEquals("-1.0002",    TextUtil.doubleToString(-1.0001500001));
		assertEquals("-1.0001",    TextUtil.doubleToString(-1.0001499999));
		assertEquals("-10.002",    TextUtil.doubleToString(-1.0001500001e1));
		assertEquals("-10.001",    TextUtil.doubleToString(-1.0001499999e1));
		assertEquals("-100.02",    TextUtil.doubleToString(-1.0001500001e2));
		assertEquals("-100.01",    TextUtil.doubleToString(-1.0001499999e2));
		assertEquals("-1000.2",    TextUtil.doubleToString(-1.0001500001e3));
		assertEquals("-1000.1",    TextUtil.doubleToString(-1.0001499999e3));
		assertEquals("-10002",     TextUtil.doubleToString(-1.0001500001e4));
		assertEquals("-10001",     TextUtil.doubleToString(-1.0001499999e4));
		assertEquals("-100012",    TextUtil.doubleToString(-1.00011500001e5));
		assertEquals("-100011",    TextUtil.doubleToString(-1.00011499999e5));
		assertEquals("-1000112",   TextUtil.doubleToString(-1.000111500001e6));
		assertEquals("-1000111",   TextUtil.doubleToString(-1.000111499999e6));
		assertEquals("-10001112",  TextUtil.doubleToString(-1.0001111500001e7));
		assertEquals("-10001111",  TextUtil.doubleToString(-1.0001111499999e7));
		assertEquals("-1.0002e8",  TextUtil.doubleToString(-1.0001500001e8));
		assertEquals("-1.0001e8",  TextUtil.doubleToString(-1.0001499999e8));
		assertEquals("-1.0002e9",  TextUtil.doubleToString(-1.0001500001e9));
		assertEquals("-1.0001e9",  TextUtil.doubleToString(-1.0001499999e9));
		assertEquals("-1.0002e10", TextUtil.doubleToString(-1.0001500001e10));
		assertEquals("-1.0001e10", TextUtil.doubleToString(-1.0001499999e10));
		
	}
	
	
	@Test
	public void randomTest() {
		for (int i=0; i<10000; i++) {
			double orig = Math.random();
			double result;
			double expected = Math.rint(orig*100000) / 100000.0;
			
			if (orig < 0.1)
				continue;
			String s = TextUtil.doubleToString(orig);
			result = Double.parseDouble(s);
			assertEquals(expected, result, 0.00000001);
		}
	}
	
}
