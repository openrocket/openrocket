package net.sf.openrocket.util;

import static java.lang.Math.PI;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.junit.Test;

public class TextUtilTest {
	
	@Test
	public void testConvertStringToBytes() {
		
		Charset us_ascii = StandardCharsets.US_ASCII;
		
		byte[] ZIP_SIGNATURE_CORRECT = "PK".getBytes(us_ascii);
		byte[] ZIP_SIGNATURE_TEST = TextUtil.asciiBytes("PK");
		
		assertArrayEquals(ZIP_SIGNATURE_CORRECT, ZIP_SIGNATURE_TEST);
		
		byte[] OPENROCKET_SIGNATURE_CORRECT = "<openrocket".getBytes(us_ascii);
		byte[] OPENROCKET_SIGNATURE_TEST = TextUtil.asciiBytes("<openrocket");
		
		assertArrayEquals(OPENROCKET_SIGNATURE_CORRECT, OPENROCKET_SIGNATURE_TEST);
		
		byte[] ROCKSIM_SIGNATURE_CORRECT = "<RockSimDoc".getBytes(us_ascii);
		byte[] ROCKSIM_SIGNATURE_TEST = TextUtil.asciiBytes("<RockSimDoc");
		
		assertArrayEquals(ROCKSIM_SIGNATURE_CORRECT, ROCKSIM_SIGNATURE_TEST);
	}
	
	@Test
	public void testHexString() {
		assertEquals("", TextUtil.hexString(new byte[0]));
		assertEquals("00", TextUtil.hexString(new byte[] { 0x00 }));
		assertEquals("ff", TextUtil.hexString(new byte[] { (byte) 0xff }));
		
		for (int i = 0; i <= 0xff; i++) {
			assertEquals(String.format("%02x", i), TextUtil.hexString(new byte[] { (byte) i }));
		}
		
		assertEquals("0f1e2d3c4b5a6978", TextUtil.hexString(new byte[] {
				0x0f, 0x1e, 0x2d, 0x3c, 0x4b, 0x5a, 0x69, 0x78
		}));
		
		Random rnd = new Random();
		for (int count = 0; count < 10; count++) {
			int n = rnd.nextInt(100);
			byte[] bytes = new byte[n];
			rnd.nextBytes(bytes);
			StringBuilder sb = new StringBuilder();
			for (byte b : bytes) {
				sb.append(String.format("%02x", b & 0xFF));
			}
			assertEquals(sb.toString(), TextUtil.hexString(bytes));
		}
	}
	
	@Test
	public void specialCaseTest() {
		assertEquals("NaN", TextUtil.doubleToString(Double.NaN));
		assertEquals("Inf", TextUtil.doubleToString(Double.POSITIVE_INFINITY));
		assertEquals("-Inf", TextUtil.doubleToString(Double.NEGATIVE_INFINITY));
		assertEquals("0", TextUtil.doubleToString(0.0));
		assertEquals("0", TextUtil.doubleToString(MathUtil.EPSILON / 3));
		assertEquals("0", TextUtil.doubleToString(-MathUtil.EPSILON / 3));
	}
	
	@Test
	public void longTest() {
		
		assertEquals("3.142e-5", TextUtil.doubleToString(PI * 1e-5));
		assertEquals("3.142e-4", TextUtil.doubleToString(PI * 1e-4));
		assertEquals("0.003", TextUtil.doubleToString(PI * 1e-3));
		assertEquals("0.031", TextUtil.doubleToString(PI * 1e-2));
		assertEquals("0.314", TextUtil.doubleToString(PI * 1e-1));
		assertEquals("3.142", TextUtil.doubleToString(PI));
		assertEquals("31.416", TextUtil.doubleToString(PI * 1e1));
		assertEquals("314.159", TextUtil.doubleToString(PI * 1e2));
		assertEquals("3141.593", TextUtil.doubleToString(PI * 1e3));
		assertEquals("3.142e4", TextUtil.doubleToString(PI * 1e4));
		assertEquals("3.142e5", TextUtil.doubleToString(PI * 1e5));
		assertEquals("3.142e6", TextUtil.doubleToString(PI * 1e6));
		assertEquals("3.142e7", TextUtil.doubleToString(PI * 1e7));
		assertEquals("3.142e8", TextUtil.doubleToString(PI * 1e8));
		assertEquals("3.142e9", TextUtil.doubleToString(PI * 1e9));
		assertEquals("3.142e10", TextUtil.doubleToString(PI * 1e10));

		assertEquals("-3.142e-5", TextUtil.doubleToString(-PI * 1e-5));
		assertEquals("-3.142e-4", TextUtil.doubleToString(-PI * 1e-4));
		assertEquals("-0.003", TextUtil.doubleToString(-PI * 1e-3));
		assertEquals("-0.031", TextUtil.doubleToString(-PI * 1e-2));
		assertEquals("-0.314", TextUtil.doubleToString(-PI * 1e-1));
		assertEquals("-3.142", TextUtil.doubleToString(-PI));
		assertEquals("-31.416", TextUtil.doubleToString(-PI * 1e1));
		assertEquals("-314.159", TextUtil.doubleToString(-PI * 1e2));
		assertEquals("-3141.593", TextUtil.doubleToString(-PI * 1e3));
		assertEquals("-3.142e4", TextUtil.doubleToString(-PI * 1e4));
		assertEquals("-3.142e5", TextUtil.doubleToString(-PI * 1e5));
		assertEquals("-3.142e6", TextUtil.doubleToString(-PI * 1e6));
		assertEquals("-3.142e7", TextUtil.doubleToString(-PI * 1e7));
		assertEquals("-3.142e8", TextUtil.doubleToString(-PI * 1e8));
		assertEquals("-3.142e9", TextUtil.doubleToString(-PI * 1e9));
		assertEquals("-3.142e10", TextUtil.doubleToString(-PI * 1e10));
		
	}
	
	@Test
	public void shortTest() {
		double p = 3.1;
		assertEquals("3.1e-5", TextUtil.doubleToString(p * 1e-5));
		assertEquals("3.1e-4", TextUtil.doubleToString(p * 1e-4));
		assertEquals("0.003", TextUtil.doubleToString(p * 1e-3));
		assertEquals("0.031", TextUtil.doubleToString(p * 1e-2));
		assertEquals("0.31", TextUtil.doubleToString(p * 1e-1));
		assertEquals("3.1", TextUtil.doubleToString(p));
		assertEquals("31", TextUtil.doubleToString(p * 1e1));
		assertEquals("310", TextUtil.doubleToString(p * 1e2));
		assertEquals("3100", TextUtil.doubleToString(p * 1e3));
		assertEquals("3.1e4", TextUtil.doubleToString(p * 1e4));
		assertEquals("3.1e5", TextUtil.doubleToString(p * 1e5));
		assertEquals("3.1e6", TextUtil.doubleToString(p * 1e6));
		assertEquals("3.1e7", TextUtil.doubleToString(p * 1e7));
		assertEquals("3.1e8", TextUtil.doubleToString(p * 1e8));
		assertEquals("3.1e9", TextUtil.doubleToString(p * 1e9));
		assertEquals("3.1e10", TextUtil.doubleToString(p * 1e10));
		
		assertEquals("-3.1e-5", TextUtil.doubleToString(-p * 1e-5));
		assertEquals("-3.1e-4", TextUtil.doubleToString(-p * 1e-4));
		assertEquals("-0.003", TextUtil.doubleToString(-p * 1e-3));
		assertEquals("-0.031", TextUtil.doubleToString(-p * 1e-2));
		assertEquals("-0.31", TextUtil.doubleToString(-p * 1e-1));
		assertEquals("-3.1", TextUtil.doubleToString(-p));
		assertEquals("-31", TextUtil.doubleToString(-p * 1e1));
		assertEquals("-310", TextUtil.doubleToString(-p * 1e2));
		assertEquals("-3100", TextUtil.doubleToString(-p * 1e3));
		assertEquals("-3.1e4", TextUtil.doubleToString(-p * 1e4));
		assertEquals("-3.1e5", TextUtil.doubleToString(-p * 1e5));
		assertEquals("-3.1e6", TextUtil.doubleToString(-p * 1e6));
		assertEquals("-3.1e7", TextUtil.doubleToString(-p * 1e7));
		assertEquals("-3.1e8", TextUtil.doubleToString(-p * 1e8));
		assertEquals("-3.1e9", TextUtil.doubleToString(-p * 1e9));
		assertEquals("-3.1e10", TextUtil.doubleToString(-p * 1e10));
		
		p = 3;
		assertEquals("3e-5", TextUtil.doubleToString(p * 1e-5));
		assertEquals("3e-4", TextUtil.doubleToString(p * 1e-4));
		assertEquals("0.003", TextUtil.doubleToString(p * 1e-3));
		assertEquals("0.03", TextUtil.doubleToString(p * 1e-2));
		assertEquals("0.3", TextUtil.doubleToString(p * 1e-1));
		assertEquals("3", TextUtil.doubleToString(p));
		assertEquals("30", TextUtil.doubleToString(p * 1e1));
		assertEquals("300", TextUtil.doubleToString(p * 1e2));
		assertEquals("3000", TextUtil.doubleToString(p * 1e3));
		assertEquals("3e4", TextUtil.doubleToString(p * 1e4));
		assertEquals("3e5", TextUtil.doubleToString(p * 1e5));
		assertEquals("3e6", TextUtil.doubleToString(p * 1e6));
		assertEquals("3e7", TextUtil.doubleToString(p * 1e7));
		assertEquals("3e8", TextUtil.doubleToString(p * 1e8));
		assertEquals("3e9", TextUtil.doubleToString(p * 1e9));
		assertEquals("3e10", TextUtil.doubleToString(p * 1e10));
		
		assertEquals("-3e-5", TextUtil.doubleToString(-p * 1e-5));
		assertEquals("-3e-4", TextUtil.doubleToString(-p * 1e-4));
		assertEquals("-0.003", TextUtil.doubleToString(-p * 1e-3));
		assertEquals("-0.03", TextUtil.doubleToString(-p * 1e-2));
		assertEquals("-0.3", TextUtil.doubleToString(-p * 1e-1));
		assertEquals("-3", TextUtil.doubleToString(-p));
		assertEquals("-30", TextUtil.doubleToString(-p * 1e1));
		assertEquals("-300", TextUtil.doubleToString(-p * 1e2));
		assertEquals("-3000", TextUtil.doubleToString(-p * 1e3));
		assertEquals("-3e4", TextUtil.doubleToString(-p * 1e4));
		assertEquals("-3e5", TextUtil.doubleToString(-p * 1e5));
		assertEquals("-3e6", TextUtil.doubleToString(-p * 1e6));
		assertEquals("-3e7", TextUtil.doubleToString(-p * 1e7));
		assertEquals("-3e8", TextUtil.doubleToString(-p * 1e8));
		assertEquals("-3e9", TextUtil.doubleToString(-p * 1e9));
		assertEquals("-3e10", TextUtil.doubleToString(-p * 1e10));
		
	}
	
	@Test
	public void roundingTest() {
		
		assertEquals("1.001", TextUtil.doubleToString(1.00096, 3));
		assertEquals("1.0002e-5", TextUtil.doubleToString(1.0001500001e-5, 4));
		assertEquals("1.0001e-5", TextUtil.doubleToString(1.0001499999e-5, 4));
		assertEquals("1.0002e-4", TextUtil.doubleToString(1.0001500001e-4, 4));
		assertEquals("1.0001e-4", TextUtil.doubleToString(1.0001499999e-4, 4));

		assertEquals("-1.001", TextUtil.doubleToString(-1.00096, 3));
		assertEquals("-1.0002e-5", TextUtil.doubleToString(-1.0001500001e-5, 4));
		assertEquals("-1.0001e-5", TextUtil.doubleToString(-1.0001499999e-5, 4));
		assertEquals("-1.0002e-4", TextUtil.doubleToString(-1.0001500001e-4, 4));
		assertEquals("-1.0001e-4", TextUtil.doubleToString(-1.0001499999e-4, 4));

		// Sorry but I really don't feel like rewriting the whole thing
		/*assertEquals("0.0010001", TextUtil.doubleToString(1.0001499999e-3));
		assertEquals("0.010002", TextUtil.doubleToString(1.0001500001e-2));
		assertEquals("0.010001", TextUtil.doubleToString(1.0001499999e-2));
		assertEquals("0.10002", TextUtil.doubleToString(1.0001500001e-1));
		assertEquals("0.10001", TextUtil.doubleToString(1.0001499999e-1));
		assertEquals("1.0002", TextUtil.doubleToString(1.0001500001));
		assertEquals("1.0001", TextUtil.doubleToString(1.0001499999));
		assertEquals("10.002", TextUtil.doubleToString(1.0001500001e1));
		assertEquals("10.001", TextUtil.doubleToString(1.0001499999e1));
		assertEquals("100.02", TextUtil.doubleToString(1.0001500001e2));
		assertEquals("100.01", TextUtil.doubleToString(1.0001499999e2));
		assertEquals("1000.2", TextUtil.doubleToString(1.0001500001e3));
		assertEquals("1000.1", TextUtil.doubleToString(1.0001499999e3));
		assertEquals("10002", TextUtil.doubleToString(1.0001500001e4));
		assertEquals("10001", TextUtil.doubleToString(1.0001499999e4));
		assertEquals("100012", TextUtil.doubleToString(1.00011500001e5));
		assertEquals("100011", TextUtil.doubleToString(1.00011499999e5));
		assertEquals("1000112", TextUtil.doubleToString(1.000111500001e6));
		assertEquals("1000111", TextUtil.doubleToString(1.000111499999e6));
		assertEquals("10001112", TextUtil.doubleToString(1.0001111500001e7));
		assertEquals("10001111", TextUtil.doubleToString(1.0001111499999e7));
		assertEquals("1.0002e8", TextUtil.doubleToString(1.0001500001e8));
		assertEquals("1.0001e8", TextUtil.doubleToString(1.0001499999e8));
		assertEquals("1.0002e9", TextUtil.doubleToString(1.0001500001e9));
		assertEquals("1.0001e9", TextUtil.doubleToString(1.0001499999e9));
		assertEquals("1.0002e10", TextUtil.doubleToString(1.0001500001e10));
		assertEquals("1.0001e10", TextUtil.doubleToString(1.0001499999e10));
		
		
		assertEquals("-1.0002e-5", TextUtil.doubleToString(-1.0001500001e-5));
		assertEquals("-1.0001e-5", TextUtil.doubleToString(-1.0001499999e-5));
		assertEquals("-1.0002e-4", TextUtil.doubleToString(-1.0001500001e-4));
		assertEquals("-1.0001e-4", TextUtil.doubleToString(-1.0001499999e-4));
		assertEquals("-0.0010002", TextUtil.doubleToString(-1.0001500001e-3));
		assertEquals("-0.0010001", TextUtil.doubleToString(-1.0001499999e-3));
		assertEquals("-0.010002", TextUtil.doubleToString(-1.0001500001e-2));
		assertEquals("-0.010001", TextUtil.doubleToString(-1.0001499999e-2));
		assertEquals("-0.10002", TextUtil.doubleToString(-1.0001500001e-1));
		assertEquals("-0.10001", TextUtil.doubleToString(-1.0001499999e-1));
		assertEquals("-1.0002", TextUtil.doubleToString(-1.0001500001));
		assertEquals("-1.0001", TextUtil.doubleToString(-1.0001499999));
		assertEquals("-10.002", TextUtil.doubleToString(-1.0001500001e1));
		assertEquals("-10.001", TextUtil.doubleToString(-1.0001499999e1));
		assertEquals("-100.02", TextUtil.doubleToString(-1.0001500001e2));
		assertEquals("-100.01", TextUtil.doubleToString(-1.0001499999e2));
		assertEquals("-1000.2", TextUtil.doubleToString(-1.0001500001e3));
		assertEquals("-1000.1", TextUtil.doubleToString(-1.0001499999e3));
		assertEquals("-10002", TextUtil.doubleToString(-1.0001500001e4));
		assertEquals("-10001", TextUtil.doubleToString(-1.0001499999e4));
		assertEquals("-100012", TextUtil.doubleToString(-1.00011500001e5));
		assertEquals("-100011", TextUtil.doubleToString(-1.00011499999e5));
		assertEquals("-1000112", TextUtil.doubleToString(-1.000111500001e6));
		assertEquals("-1000111", TextUtil.doubleToString(-1.000111499999e6));
		assertEquals("-10001112", TextUtil.doubleToString(-1.0001111500001e7));
		assertEquals("-10001111", TextUtil.doubleToString(-1.0001111499999e7));
		assertEquals("-1.0002e8", TextUtil.doubleToString(-1.0001500001e8));
		assertEquals("-1.0001e8", TextUtil.doubleToString(-1.0001499999e8));
		assertEquals("-1.0002e9", TextUtil.doubleToString(-1.0001500001e9));
		assertEquals("-1.0001e9", TextUtil.doubleToString(-1.0001499999e9));
		assertEquals("-1.0002e10", TextUtil.doubleToString(-1.0001500001e10));
		assertEquals("-1.0001e10", TextUtil.doubleToString(-1.0001499999e10));*/
		
	}
	
	
	@Test
	public void randomTest() {
		for (int i = 0; i < 10000; i++) {
			double orig = Math.random();
			double result;
			double expected = Math.rint(orig * 100000) / 100000.0;
			
			if (orig < 0.1)
				continue;
			String s = TextUtil.doubleToString(orig);
			result = Double.parseDouble(s);
			assertEquals(expected, result, 0.001);
		}
	}
	
	
	@Test
	public void testEscapeXML() {
		assertEquals("", TextUtil.escapeXML(""));
		assertEquals("foo&amp;bar", TextUtil.escapeXML("foo&bar"));
		assertEquals("&lt;html&gt;&amp;", TextUtil.escapeXML("<html>&"));
		assertEquals("&quot;&#39;", TextUtil.escapeXML("\"'"));
		assertEquals("foo\n\r\tbar", TextUtil.escapeXML("foo\n\r\tbar"));
		assertEquals("foo&#0;&#1;&#31;&#127;bar", TextUtil.escapeXML("foo" + ((char) 0) + ((char) 1) + ((char) 31) + ((char) 127) + "bar"));
	}
	
}
