package info.openrocket.core.util;

import static java.lang.Math.PI;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.junit.jupiter.api.Test;

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
		assertEquals(TextUtil.hexString(new byte[0]), "");
		assertEquals(TextUtil.hexString(new byte[] { 0x00 }), "00");
		assertEquals(TextUtil.hexString(new byte[] { (byte) 0xff }), "ff");

		for (int i = 0; i <= 0xff; i++) {
			assertEquals(String.format("%02x", i), TextUtil.hexString(new byte[] { (byte) i }));
		}

		assertEquals(TextUtil.hexString(new byte[] {
				0x0f, 0x1e, 0x2d, 0x3c, 0x4b, 0x5a, 0x69, 0x78
		}), "0f1e2d3c4b5a6978");

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
		assertEquals(TextUtil.doubleToString(Double.NaN), "NaN");
		assertEquals(TextUtil.doubleToString(Double.POSITIVE_INFINITY), "Inf");
		assertEquals(TextUtil.doubleToString(Double.NEGATIVE_INFINITY), "-Inf");
		assertEquals(TextUtil.doubleToString(0.0), "0");
		assertEquals(TextUtil.doubleToString(MathUtil.EPSILON / 3), "0");
		assertEquals(TextUtil.doubleToString(-MathUtil.EPSILON / 3), "0");
	}

	@Test
	public void longTest() {

		assertEquals(TextUtil.doubleToString(PI * 1e-5), "3.142e-5");
		assertEquals(TextUtil.doubleToString(PI * 1e-4), "3.142e-4");
		assertEquals(TextUtil.doubleToString(PI * 1e-3), "0.003");
		assertEquals(TextUtil.doubleToString(PI * 1e-2), "0.031");
		assertEquals(TextUtil.doubleToString(PI * 1e-1), "0.314");
		assertEquals(TextUtil.doubleToString(PI), "3.142");
		assertEquals(TextUtil.doubleToString(PI * 1e1), "31.416");
		assertEquals(TextUtil.doubleToString(PI * 1e2), "314.159");
		assertEquals(TextUtil.doubleToString(PI * 1e3), "3141.593");
		assertEquals(TextUtil.doubleToString(PI * 1e4), "3.142e4");
		assertEquals(TextUtil.doubleToString(PI * 1e5), "3.142e5");
		assertEquals(TextUtil.doubleToString(PI * 1e6), "3.142e6");
		assertEquals(TextUtil.doubleToString(PI * 1e7), "3.142e7");
		assertEquals(TextUtil.doubleToString(PI * 1e8), "3.142e8");
		assertEquals(TextUtil.doubleToString(PI * 1e9), "3.142e9");
		assertEquals(TextUtil.doubleToString(PI * 1e10), "3.142e10");

		assertEquals(TextUtil.doubleToString(-PI * 1e-5), "-3.142e-5");
		assertEquals(TextUtil.doubleToString(-PI * 1e-4), "-3.142e-4");
		assertEquals(TextUtil.doubleToString(-PI * 1e-3), "-0.003");
		assertEquals(TextUtil.doubleToString(-PI * 1e-2), "-0.031");
		assertEquals(TextUtil.doubleToString(-PI * 1e-1), "-0.314");
		assertEquals(TextUtil.doubleToString(-PI), "-3.142");
		assertEquals(TextUtil.doubleToString(-PI * 1e1), "-31.416");
		assertEquals(TextUtil.doubleToString(-PI * 1e2), "-314.159");
		assertEquals(TextUtil.doubleToString(-PI * 1e3), "-3141.593");
		assertEquals(TextUtil.doubleToString(-PI * 1e4), "-3.142e4");
		assertEquals(TextUtil.doubleToString(-PI * 1e5), "-3.142e5");
		assertEquals(TextUtil.doubleToString(-PI * 1e6), "-3.142e6");
		assertEquals(TextUtil.doubleToString(-PI * 1e7), "-3.142e7");
		assertEquals(TextUtil.doubleToString(-PI * 1e8), "-3.142e8");
		assertEquals(TextUtil.doubleToString(-PI * 1e9), "-3.142e9");
		assertEquals(TextUtil.doubleToString(-PI * 1e10), "-3.142e10");

	}

	@Test
	public void shortTest() {
		double p = 3.1;
		assertEquals(TextUtil.doubleToString(p * 1e-5), "3.1e-5");
		assertEquals(TextUtil.doubleToString(p * 1e-4), "3.1e-4");
		assertEquals(TextUtil.doubleToString(p * 1e-3), "0.003");
		assertEquals(TextUtil.doubleToString(p * 1e-2), "0.031");
		assertEquals(TextUtil.doubleToString(p * 1e-1), "0.31");
		assertEquals(TextUtil.doubleToString(p), "3.1");
		assertEquals(TextUtil.doubleToString(p * 1e1), "31");
		assertEquals(TextUtil.doubleToString(p * 1e2), "310");
		assertEquals(TextUtil.doubleToString(p * 1e3), "3100");
		assertEquals(TextUtil.doubleToString(p * 1e4), "3.1e4");
		assertEquals(TextUtil.doubleToString(p * 1e5), "3.1e5");
		assertEquals(TextUtil.doubleToString(p * 1e6), "3.1e6");
		assertEquals(TextUtil.doubleToString(p * 1e7), "3.1e7");
		assertEquals(TextUtil.doubleToString(p * 1e8), "3.1e8");
		assertEquals(TextUtil.doubleToString(p * 1e9), "3.1e9");
		assertEquals(TextUtil.doubleToString(p * 1e10), "3.1e10");

		assertEquals(TextUtil.doubleToString(-p * 1e-5), "-3.1e-5");
		assertEquals(TextUtil.doubleToString(-p * 1e-4), "-3.1e-4");
		assertEquals(TextUtil.doubleToString(-p * 1e-3), "-0.003");
		assertEquals(TextUtil.doubleToString(-p * 1e-2), "-0.031");
		assertEquals(TextUtil.doubleToString(-p * 1e-1), "-0.31");
		assertEquals(TextUtil.doubleToString(-p), "-3.1");
		assertEquals(TextUtil.doubleToString(-p * 1e1), "-31");
		assertEquals(TextUtil.doubleToString(-p * 1e2), "-310");
		assertEquals(TextUtil.doubleToString(-p * 1e3), "-3100");
		assertEquals(TextUtil.doubleToString(-p * 1e4), "-3.1e4");
		assertEquals(TextUtil.doubleToString(-p * 1e5), "-3.1e5");
		assertEquals(TextUtil.doubleToString(-p * 1e6), "-3.1e6");
		assertEquals(TextUtil.doubleToString(-p * 1e7), "-3.1e7");
		assertEquals(TextUtil.doubleToString(-p * 1e8), "-3.1e8");
		assertEquals(TextUtil.doubleToString(-p * 1e9), "-3.1e9");
		assertEquals(TextUtil.doubleToString(-p * 1e10), "-3.1e10");

		p = 3;
		assertEquals(TextUtil.doubleToString(p * 1e-5), "3e-5");
		assertEquals(TextUtil.doubleToString(p * 1e-4), "3e-4");
		assertEquals(TextUtil.doubleToString(p * 1e-3), "0.003");
		assertEquals(TextUtil.doubleToString(p * 1e-2), "0.03");
		assertEquals(TextUtil.doubleToString(p * 1e-1), "0.3");
		assertEquals(TextUtil.doubleToString(p), "3");
		assertEquals(TextUtil.doubleToString(p * 1e1), "30");
		assertEquals(TextUtil.doubleToString(p * 1e2), "300");
		assertEquals(TextUtil.doubleToString(p * 1e3), "3000");
		assertEquals(TextUtil.doubleToString(p * 1e4), "3e4");
		assertEquals(TextUtil.doubleToString(p * 1e5), "3e5");
		assertEquals(TextUtil.doubleToString(p * 1e6), "3e6");
		assertEquals(TextUtil.doubleToString(p * 1e7), "3e7");
		assertEquals(TextUtil.doubleToString(p * 1e8), "3e8");
		assertEquals(TextUtil.doubleToString(p * 1e9), "3e9");
		assertEquals(TextUtil.doubleToString(p * 1e10), "3e10");

		assertEquals(TextUtil.doubleToString(-p * 1e-5), "-3e-5");
		assertEquals(TextUtil.doubleToString(-p * 1e-4), "-3e-4");
		assertEquals(TextUtil.doubleToString(-p * 1e-3), "-0.003");
		assertEquals(TextUtil.doubleToString(-p * 1e-2), "-0.03");
		assertEquals(TextUtil.doubleToString(-p * 1e-1), "-0.3");
		assertEquals(TextUtil.doubleToString(-p), "-3");
		assertEquals(TextUtil.doubleToString(-p * 1e1), "-30");
		assertEquals(TextUtil.doubleToString(-p * 1e2), "-300");
		assertEquals(TextUtil.doubleToString(-p * 1e3), "-3000");
		assertEquals(TextUtil.doubleToString(-p * 1e4), "-3e4");
		assertEquals(TextUtil.doubleToString(-p * 1e5), "-3e5");
		assertEquals(TextUtil.doubleToString(-p * 1e6), "-3e6");
		assertEquals(TextUtil.doubleToString(-p * 1e7), "-3e7");
		assertEquals(TextUtil.doubleToString(-p * 1e8), "-3e8");
		assertEquals(TextUtil.doubleToString(-p * 1e9), "-3e9");
		assertEquals(TextUtil.doubleToString(-p * 1e10), "-3e10");

	}

	@Test
	public void roundingTest() {

		assertEquals(TextUtil.doubleToString(1.00096, 3), "1.001");
		assertEquals(TextUtil.doubleToString(1.0001500001e-5, 4), "1.0002e-5");
		assertEquals(TextUtil.doubleToString(1.0001499999e-5, 4), "1.0001e-5");
		assertEquals(TextUtil.doubleToString(1.0001500001e-4, 4), "1.0002e-4");
		assertEquals(TextUtil.doubleToString(1.0001499999e-4, 4), "1.0001e-4");

		assertEquals(TextUtil.doubleToString(-1.00096, 3), "-1.001");
		assertEquals(TextUtil.doubleToString(-1.0001500001e-5, 4), "-1.0002e-5");
		assertEquals(TextUtil.doubleToString(-1.0001499999e-5, 4), "-1.0001e-5");
		assertEquals(TextUtil.doubleToString(-1.0001500001e-4, 4), "-1.0002e-4");
		assertEquals(TextUtil.doubleToString(-1.0001499999e-4, 4), "-1.0001e-4");

		// Sorry but I really don't feel like rewriting the whole thing
		/*
		 * assertEquals(TextUtil.doubleToString(1.0001499999e-3), "0.0010001");
		 * assertEquals(TextUtil.doubleToString(1.0001500001e-2), "0.010002");
		 * assertEquals(TextUtil.doubleToString(1.0001499999e-2), "0.010001");
		 * assertEquals(TextUtil.doubleToString(1.0001500001e-1), "0.10002");
		 * assertEquals(TextUtil.doubleToString(1.0001499999e-1), "0.10001");
		 * assertEquals(TextUtil.doubleToString(1.0001500001), "1.0002");
		 * assertEquals(TextUtil.doubleToString(1.0001499999), "1.0001");
		 * assertEquals(TextUtil.doubleToString(1.0001500001e1), "10.002");
		 * assertEquals(TextUtil.doubleToString(1.0001499999e1), "10.001");
		 * assertEquals(TextUtil.doubleToString(1.0001500001e2), "100.02");
		 * assertEquals(TextUtil.doubleToString(1.0001499999e2), "100.01");
		 * assertEquals(TextUtil.doubleToString(1.0001500001e3), "1000.2");
		 * assertEquals(TextUtil.doubleToString(1.0001499999e3), "1000.1");
		 * assertEquals(TextUtil.doubleToString(1.0001500001e4), "10002");
		 * assertEquals(TextUtil.doubleToString(1.0001499999e4), "10001");
		 * assertEquals(TextUtil.doubleToString(1.00011500001e5), "100012");
		 * assertEquals(TextUtil.doubleToString(1.00011499999e5), "100011");
		 * assertEquals(TextUtil.doubleToString(1.000111500001e6), "1000112");
		 * assertEquals(TextUtil.doubleToString(1.000111499999e6), "1000111");
		 * assertEquals(TextUtil.doubleToString(1.0001111500001e7), "10001112");
		 * assertEquals(TextUtil.doubleToString(1.0001111499999e7), "10001111");
		 * assertEquals(TextUtil.doubleToString(1.0001500001e8), "1.0002e8");
		 * assertEquals(TextUtil.doubleToString(1.0001499999e8), "1.0001e8");
		 * assertEquals(TextUtil.doubleToString(1.0001500001e9), "1.0002e9");
		 * assertEquals(TextUtil.doubleToString(1.0001499999e9), "1.0001e9");
		 * assertEquals(TextUtil.doubleToString(1.0001500001e10), "1.0002e10");
		 * assertEquals(TextUtil.doubleToString(1.0001499999e10), "1.0001e10");
		 * 
		 * 
		 * assertEquals(TextUtil.doubleToString(-1.0001500001e-5), "-1.0002e-5");
		 * assertEquals(TextUtil.doubleToString(-1.0001499999e-5), "-1.0001e-5");
		 * assertEquals(TextUtil.doubleToString(-1.0001500001e-4), "-1.0002e-4");
		 * assertEquals(TextUtil.doubleToString(-1.0001499999e-4), "-1.0001e-4");
		 * assertEquals(TextUtil.doubleToString(-1.0001500001e-3), "-0.0010002");
		 * assertEquals(TextUtil.doubleToString(-1.0001499999e-3), "-0.0010001");
		 * assertEquals(TextUtil.doubleToString(-1.0001500001e-2), "-0.010002");
		 * assertEquals(TextUtil.doubleToString(-1.0001499999e-2), "-0.010001");
		 * assertEquals(TextUtil.doubleToString(-1.0001500001e-1), "-0.10002");
		 * assertEquals(TextUtil.doubleToString(-1.0001499999e-1), "-0.10001");
		 * assertEquals(TextUtil.doubleToString(-1.0001500001), "-1.0002");
		 * assertEquals(TextUtil.doubleToString(-1.0001499999), "-1.0001");
		 * assertEquals(TextUtil.doubleToString(-1.0001500001e1), "-10.002");
		 * assertEquals(TextUtil.doubleToString(-1.0001499999e1), "-10.001");
		 * assertEquals(TextUtil.doubleToString(-1.0001500001e2), "-100.02");
		 * assertEquals(TextUtil.doubleToString(-1.0001499999e2), "-100.01");
		 * assertEquals(TextUtil.doubleToString(-1.0001500001e3), "-1000.2");
		 * assertEquals(TextUtil.doubleToString(-1.0001499999e3), "-1000.1");
		 * assertEquals(TextUtil.doubleToString(-1.0001500001e4), "-10002");
		 * assertEquals(TextUtil.doubleToString(-1.0001499999e4), "-10001");
		 * assertEquals(TextUtil.doubleToString(-1.00011500001e5), "-100012");
		 * assertEquals(TextUtil.doubleToString(-1.00011499999e5), "-100011");
		 * assertEquals(TextUtil.doubleToString(-1.000111500001e6), "-1000112");
		 * assertEquals(TextUtil.doubleToString(-1.000111499999e6), "-1000111");
		 * assertEquals(TextUtil.doubleToString(-1.0001111500001e7), "-10001112");
		 * assertEquals(TextUtil.doubleToString(-1.0001111499999e7), "-10001111");
		 * assertEquals(TextUtil.doubleToString(-1.0001500001e8), "-1.0002e8");
		 * assertEquals(TextUtil.doubleToString(-1.0001499999e8), "-1.0001e8");
		 * assertEquals(TextUtil.doubleToString(-1.0001500001e9), "-1.0002e9");
		 * assertEquals(TextUtil.doubleToString(-1.0001499999e9), "-1.0001e9");
		 * assertEquals(TextUtil.doubleToString(-1.0001500001e10), "-1.0002e10");
		 * assertEquals(TextUtil.doubleToString(-1.0001499999e10), "-1.0001e10");
		 */

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
		assertEquals(TextUtil.escapeXML(""), "");
		assertEquals(TextUtil.escapeXML("foo&bar"), "foo&amp;bar");
		assertEquals(TextUtil.escapeXML("<html>&"), "&lt;html&gt;&amp;");
		assertEquals(TextUtil.escapeXML("\"'"), "&quot;&#39;");
		assertEquals(TextUtil.escapeXML("foo\n\r\tbar"), "foo\n\r\tbar");
		assertEquals(TextUtil.escapeXML("foo" + ((char) 0) + ((char) 1) + ((char) 31) + ((char) 127) + "bar"), "foo&#0;&#1;&#31;&#127;bar");
	}

}
