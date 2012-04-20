package net.sf.openrocket.unit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UnitToStringTest {

	@Test
	public void testToString() {
		assertEquals("0",Unit.NOUNIT2.toString(0.00040));
		assertEquals("0",Unit.NOUNIT2.toString(0.00050));
		assertEquals("0",Unit.NOUNIT2.toString(0.00051));
		assertEquals("0",Unit.NOUNIT2.toString(0.00060));
		assertEquals("0",Unit.NOUNIT2.toString(0.0049));
		assertEquals("0",Unit.NOUNIT2.toString(0.0050));
		assertEquals("0.01",Unit.NOUNIT2.toString(0.0051));
		assertEquals("0",Unit.NOUNIT2.toString(0.00123));
		assertEquals("0.01",Unit.NOUNIT2.toString(0.0123));
		assertEquals("0.12",Unit.NOUNIT2.toString(0.1234));
		assertEquals("1.23",Unit.NOUNIT2.toString(1.2345));
		assertEquals("12.3",Unit.NOUNIT2.toString(12.345));
		assertEquals("123",Unit.NOUNIT2.toString(123.456));
		assertEquals("1235",Unit.NOUNIT2.toString(1234.5678));
		assertEquals("12346",Unit.NOUNIT2.toString(12345.6789));
		assertEquals("123457",Unit.NOUNIT2.toString(123456.789));
		assertEquals("1.23E6",Unit.NOUNIT2.toString(1234567.89));
		assertEquals("1.23E7",Unit.NOUNIT2.toString(12345678.9));
		assertEquals("0",Unit.NOUNIT2.toString(-0.00040));
		assertEquals("0",Unit.NOUNIT2.toString(-0.00050));
		assertEquals("0",Unit.NOUNIT2.toString(-0.00051));
		assertEquals("0",Unit.NOUNIT2.toString(-0.00060));
		assertEquals("0",Unit.NOUNIT2.toString(-0.0049));
		assertEquals("0",Unit.NOUNIT2.toString(-0.0050));
		assertEquals("-0.01",Unit.NOUNIT2.toString(-0.0051));
		assertEquals("0",Unit.NOUNIT2.toString(-0.00123));
		assertEquals("-0.01",Unit.NOUNIT2.toString(-0.0123));
		assertEquals("-0.12",Unit.NOUNIT2.toString(-0.1234));
		assertEquals("-1.23",Unit.NOUNIT2.toString(-1.2345));
		assertEquals("-12.3",Unit.NOUNIT2.toString(-12.345));
		assertEquals("-123",Unit.NOUNIT2.toString(-123.456));
		assertEquals("-1235",Unit.NOUNIT2.toString(-1234.5678));
		assertEquals("-12346",Unit.NOUNIT2.toString(-12345.6789));
		assertEquals("-123457",Unit.NOUNIT2.toString(-123456.789));
		assertEquals("-1.23E6",Unit.NOUNIT2.toString(-1234567.89));
		assertEquals("-1.23E7",Unit.NOUNIT2.toString(-12345678.9));

	}
}
