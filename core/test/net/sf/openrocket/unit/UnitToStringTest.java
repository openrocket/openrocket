package net.sf.openrocket.unit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UnitToStringTest {
	
	@Test
	public void testPositiveToString() {
		// very small positive numbers ( < 0.0005) are returned as "0"
		assertEquals("0", Unit.NOUNIT.toString(0.00040));
		assertEquals("0", Unit.NOUNIT.toString(0.00050)); // check boundary of change in format
		
		// positive number < 0.095 use 3 digit decimal format
		assertEquals("0.001", Unit.NOUNIT.toString(0.00051)); // check boundary of change in format
		assertEquals("0.001", Unit.NOUNIT.toString(0.00060));
		
		// rounding at third digit.
		assertEquals("0.001", Unit.NOUNIT.toString(0.0014));
		assertEquals("0.002", Unit.NOUNIT.toString(0.0015)); // round to even
		assertEquals("0.002", Unit.NOUNIT.toString(0.0016));
		assertEquals("0.002", Unit.NOUNIT.toString(0.0024));
		assertEquals("0.002", Unit.NOUNIT.toString(0.0025)); // round to even
		assertEquals("0.003", Unit.NOUNIT.toString(0.0026));
		assertEquals("0.009", Unit.NOUNIT.toString(0.0094));
		
		assertEquals("0.01", Unit.NOUNIT.toString(0.0095)); // no trailing zeros after rounding
		
		assertEquals("0.011", Unit.NOUNIT.toString(0.0114));
		assertEquals("0.012", Unit.NOUNIT.toString(0.0115)); // round to even
		assertEquals("0.012", Unit.NOUNIT.toString(0.0119));
		assertEquals("0.012", Unit.NOUNIT.toString(0.0124));
		assertEquals("0.012", Unit.NOUNIT.toString(0.0125)); // round to even
		assertEquals("0.013", Unit.NOUNIT.toString(0.0129));
		
		assertEquals("0.095", Unit.NOUNIT.toString(0.0949)); // boundary check
		
		// positive numbers < 100 
		assertEquals("0.01", Unit.NOUNIT.toString(0.0095)); // boundary check
		
		assertEquals("0.111", Unit.NOUNIT.toString(0.1111));
		assertEquals("0.112", Unit.NOUNIT.toString(0.1115)); // round to even
		assertEquals("0.112", Unit.NOUNIT.toString(0.1117));
		assertEquals("0.112", Unit.NOUNIT.toString(0.1121));
		assertEquals("0.112", Unit.NOUNIT.toString(0.1125)); // round to even
		assertEquals("0.113", Unit.NOUNIT.toString(0.1127));
		
		assertEquals("1.11", Unit.NOUNIT.toString(1.113));
		assertEquals("1.12", Unit.NOUNIT.toString(1.115)); // round to even
		assertEquals("1.12", Unit.NOUNIT.toString(1.117));
		assertEquals("1.12", Unit.NOUNIT.toString(1.123));
		assertEquals("1.12", Unit.NOUNIT.toString(1.125)); // round to even
		assertEquals("1.13", Unit.NOUNIT.toString(1.127));
		
		assertEquals("12.3", Unit.NOUNIT.toString(12.320));
		assertEquals("12.4", Unit.NOUNIT.toString(12.350)); // round to even
		assertEquals("12.4", Unit.NOUNIT.toString(12.355));
		assertEquals("12.4", Unit.NOUNIT.toString(12.420));
		assertEquals("12.4", Unit.NOUNIT.toString(12.450)); // round to even
		assertEquals("12.5", Unit.NOUNIT.toString(12.455));
		
		// positive numbers <= 1E6
		assertEquals("123", Unit.NOUNIT.toString(123.20));
		assertEquals("124", Unit.NOUNIT.toString(123.50)); // round to even
		assertEquals("124", Unit.NOUNIT.toString(123.55));
		assertEquals("124", Unit.NOUNIT.toString(124.20));
		assertEquals("124", Unit.NOUNIT.toString(124.50)); // round to even
		assertEquals("125", Unit.NOUNIT.toString(124.55));
		
		assertEquals("1234", Unit.NOUNIT.toString(1234.2));
		assertEquals("1234", Unit.NOUNIT.toString(1234.5)); // round to even
		assertEquals("1235", Unit.NOUNIT.toString(1234.6));
		assertEquals("1235", Unit.NOUNIT.toString(1235.2));
		assertEquals("1236", Unit.NOUNIT.toString(1235.5)); // round to even
		assertEquals("1236", Unit.NOUNIT.toString(1235.6));
		
		assertEquals("123457", Unit.NOUNIT.toString(123456.789));
		
		assertEquals("1000000", Unit.NOUNIT.toString(1000000)); // boundary check
		
		// positive numbers > 1E6
		assertEquals("1.23E6", Unit.NOUNIT.toString(1234567.89));
		assertEquals("1.23E7", Unit.NOUNIT.toString(12345678.9));
		
		
		// Inch precision
		assertEquals("25.125", UnitGroup.UNITS_LENGTH.findApproximate("in").toString(25.125 * 25.4 / 1000));
		
	}
	
	@Test
	public void testNegativeToString() {
		// very small negative numbers ( < 0.0005) are returned as "0"
		assertEquals("0", Unit.NOUNIT.toString(-0.00040));
		assertEquals("0", Unit.NOUNIT.toString(-0.00050)); // check boundary of change in format
		
		// negative number < 0.095 use 3 digit decimal format
		assertEquals("-0.001", Unit.NOUNIT.toString(-0.00051)); // check boundary of change in format
		assertEquals("-0.001", Unit.NOUNIT.toString(-0.00060));
		
		// rounding at third digit.
		assertEquals("-0.001", Unit.NOUNIT.toString(-0.0014));
		assertEquals("-0.002", Unit.NOUNIT.toString(-0.0015)); // round to even
		assertEquals("-0.002", Unit.NOUNIT.toString(-0.0016));
		assertEquals("-0.002", Unit.NOUNIT.toString(-0.0024));
		assertEquals("-0.002", Unit.NOUNIT.toString(-0.0025)); // round to even
		assertEquals("-0.003", Unit.NOUNIT.toString(-0.0026));
		assertEquals("-0.009", Unit.NOUNIT.toString(-0.0094));
		
		assertEquals("-0.01", Unit.NOUNIT.toString(-0.0095)); // no trailing zeros after rounding
		
		assertEquals("-0.011", Unit.NOUNIT.toString(-0.0114));
		assertEquals("-0.012", Unit.NOUNIT.toString(-0.0115)); // round to even
		assertEquals("-0.012", Unit.NOUNIT.toString(-0.0119));
		assertEquals("-0.012", Unit.NOUNIT.toString(-0.0124));
		assertEquals("-0.012", Unit.NOUNIT.toString(-0.0125)); // round to even
		assertEquals("-0.013", Unit.NOUNIT.toString(-0.0129));
		
		assertEquals("-0.095", Unit.NOUNIT.toString(-0.0949)); // boundary check
		
		// negative numbers < 100 
		assertEquals("-0.01", Unit.NOUNIT.toString(-0.0095)); // boundary check
		
		assertEquals("-0.111", Unit.NOUNIT.toString(-0.1111));
		assertEquals("-0.112", Unit.NOUNIT.toString(-0.1115)); // round to even
		assertEquals("-0.112", Unit.NOUNIT.toString(-0.1117));
		assertEquals("-0.112", Unit.NOUNIT.toString(-0.1121));
		assertEquals("-0.112", Unit.NOUNIT.toString(-0.1125)); // round to even
		assertEquals("-0.113", Unit.NOUNIT.toString(-0.1127));
		
		assertEquals("-1.11", Unit.NOUNIT.toString(-1.113));
		assertEquals("-1.12", Unit.NOUNIT.toString(-1.115)); // round to even
		assertEquals("-1.12", Unit.NOUNIT.toString(-1.117));
		assertEquals("-1.12", Unit.NOUNIT.toString(-1.123));
		assertEquals("-1.12", Unit.NOUNIT.toString(-1.125)); // round to even
		assertEquals("-1.13", Unit.NOUNIT.toString(-1.127));
		
		assertEquals("-12.3", Unit.NOUNIT.toString(-12.320));
		assertEquals("-12.4", Unit.NOUNIT.toString(-12.350)); // round to even
		assertEquals("-12.4", Unit.NOUNIT.toString(-12.355));
		assertEquals("-12.4", Unit.NOUNIT.toString(-12.420));
		assertEquals("-12.4", Unit.NOUNIT.toString(-12.450)); // round to even
		assertEquals("-12.5", Unit.NOUNIT.toString(-12.455));
		
		// negative numbers <= 1E6
		assertEquals("-123", Unit.NOUNIT.toString(-123.20));
		assertEquals("-124", Unit.NOUNIT.toString(-123.50)); // round to even
		assertEquals("-124", Unit.NOUNIT.toString(-123.55));
		assertEquals("-124", Unit.NOUNIT.toString(-124.20));
		assertEquals("-124", Unit.NOUNIT.toString(-124.50)); // round to even
		assertEquals("-125", Unit.NOUNIT.toString(-124.55));
		
		assertEquals("-1234", Unit.NOUNIT.toString(-1234.2));
		assertEquals("-1234", Unit.NOUNIT.toString(-1234.5)); // round to even
		assertEquals("-1235", Unit.NOUNIT.toString(-1234.6));
		assertEquals("-1235", Unit.NOUNIT.toString(-1235.2));
		assertEquals("-1236", Unit.NOUNIT.toString(-1235.5)); // round to even
		assertEquals("-1236", Unit.NOUNIT.toString(-1235.6));
		
		assertEquals("-123457", Unit.NOUNIT.toString(-123456.789));
		
		assertEquals("-1000000", Unit.NOUNIT.toString(-1000000)); // boundary check
		
		// negative numbers > 1E6
		assertEquals("-1.23E6", Unit.NOUNIT.toString(-1234567.89));
		assertEquals("-1.23E7", Unit.NOUNIT.toString(-12345678.9));
		
		
	}
	
	
}
