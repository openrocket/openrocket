package info.openrocket.core.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.DecimalFormat;

import org.junit.jupiter.api.Test;

public class UnitToStringTest {
	private boolean isPointDecimalSeparator() {
		return ((DecimalFormat) DecimalFormat.getInstance()).getDecimalFormatSymbols().getDecimalSeparator() == '.';
	}

	@Test
	public void testPositiveToString() {
		if (isPointDecimalSeparator()) {
			testPositiveWithPoint();
		} else {
			testPositiveWithComma();
		}

		// very small positive numbers ( < 0.0005) are returned as "0"
		assertEquals(Unit.NOUNIT.toString(0.00040), "0");
		assertEquals(Unit.NOUNIT.toString(0.00050), "0"); // check boundary of change in format

		// positive numbers <= 1E6
		assertEquals(Unit.NOUNIT.toString(123.20), "123");
		assertEquals(Unit.NOUNIT.toString(123.50), "124"); // round to even
		assertEquals(Unit.NOUNIT.toString(123.55), "124");
		assertEquals(Unit.NOUNIT.toString(124.20), "124");
		assertEquals(Unit.NOUNIT.toString(124.50), "124"); // round to even
		assertEquals(Unit.NOUNIT.toString(124.55), "125");

		assertEquals(Unit.NOUNIT.toString(1234.2), "1234");
		assertEquals(Unit.NOUNIT.toString(1234.5), "1234"); // round to even
		assertEquals(Unit.NOUNIT.toString(1234.6), "1235");
		assertEquals(Unit.NOUNIT.toString(1235.2), "1235");
		assertEquals(Unit.NOUNIT.toString(1235.5), "1236"); // round to even
		assertEquals(Unit.NOUNIT.toString(1235.6), "1236");

		assertEquals(Unit.NOUNIT.toString(123456.789), "123457");

		assertEquals(Unit.NOUNIT.toString(1000000), "1000000"); // boundary check

	}

	private void testPositiveWithPoint() {

		// positive number < 0.095 use 3 digit decimal format
		assertEquals(Unit.NOUNIT.toString(0.00051), "0.001"); // check boundary of change in format
		assertEquals(Unit.NOUNIT.toString(0.00060), "0.001");

		// rounding at third digit.
		assertEquals(Unit.NOUNIT.toString(0.0014), "0.001");
		assertEquals(Unit.NOUNIT.toString(0.0015), "0.002"); // round to even
		assertEquals(Unit.NOUNIT.toString(0.0016), "0.002");
		assertEquals(Unit.NOUNIT.toString(0.0024), "0.002");
		assertEquals(Unit.NOUNIT.toString(0.0025), "0.002"); // round to even
		assertEquals(Unit.NOUNIT.toString(0.0026), "0.003");
		assertEquals(Unit.NOUNIT.toString(0.0094), "0.009");

		assertEquals(Unit.NOUNIT.toString(0.0095), "0.01"); // no trailing zeros after rounding

		assertEquals(Unit.NOUNIT.toString(0.0114), "0.011");
		assertEquals(Unit.NOUNIT.toString(0.0115), "0.012"); // round to even
		assertEquals(Unit.NOUNIT.toString(0.0119), "0.012");
		assertEquals(Unit.NOUNIT.toString(0.0124), "0.012");
		assertEquals(Unit.NOUNIT.toString(0.0125), "0.012"); // round to even
		assertEquals(Unit.NOUNIT.toString(0.0129), "0.013");

		assertEquals(Unit.NOUNIT.toString(0.0949), "0.095"); // boundary check

		// positive numbers < 100
		assertEquals(Unit.NOUNIT.toString(0.0095), "0.01"); // boundary check

		assertEquals(Unit.NOUNIT.toString(0.1111), "0.111");
		assertEquals(Unit.NOUNIT.toString(0.1115), "0.112"); // round to even
		assertEquals(Unit.NOUNIT.toString(0.1117), "0.112");
		assertEquals(Unit.NOUNIT.toString(0.1121), "0.112");
		assertEquals(Unit.NOUNIT.toString(0.1125), "0.112"); // round to even
		assertEquals(Unit.NOUNIT.toString(0.1127), "0.113");

		assertEquals(Unit.NOUNIT.toString(1.113), "1.11");
		assertEquals(Unit.NOUNIT.toString(1.115), "1.12"); // round to even
		assertEquals(Unit.NOUNIT.toString(1.117), "1.12");
		assertEquals(Unit.NOUNIT.toString(1.123), "1.12");
		assertEquals(Unit.NOUNIT.toString(1.125), "1.12"); // round to even
		assertEquals(Unit.NOUNIT.toString(1.127), "1.13");

		assertEquals(Unit.NOUNIT.toString(12.320), "12.3");
		assertEquals(Unit.NOUNIT.toString(12.350), "12.4"); // round to even
		assertEquals(Unit.NOUNIT.toString(12.355), "12.4");
		assertEquals(Unit.NOUNIT.toString(12.420), "12.4");
		assertEquals(Unit.NOUNIT.toString(12.450), "12.4"); // round to even
		assertEquals(Unit.NOUNIT.toString(12.455), "12.5");
		// positive numbers > 1E6
		assertEquals(Unit.NOUNIT.toString(1234567.89), "1.23E6");
		assertEquals(Unit.NOUNIT.toString(12345678.9), "1.23E7");

		// Inch precision
		assertEquals(UnitGroup.UNITS_LENGTH.findApproximate("in").toString(25.125 * 25.4 / 1000), "25.125");
	}

	private void testPositiveWithComma() {
		// positive number < 0.095 use 3 digit decimal format
		assertEquals(Unit.NOUNIT.toString(0.00051), "0,001"); // check boundary of change in format
		assertEquals(Unit.NOUNIT.toString(0.00060), "0,001");

		// rounding at third digit.
		assertEquals(Unit.NOUNIT.toString(0.0014), "0,001");
		assertEquals(Unit.NOUNIT.toString(0.0015), "0,002"); // round to even
		assertEquals(Unit.NOUNIT.toString(0.0016), "0,002");
		assertEquals(Unit.NOUNIT.toString(0.0024), "0,002");
		assertEquals(Unit.NOUNIT.toString(0.0025), "0,002"); // round to even
		assertEquals(Unit.NOUNIT.toString(0.0026), "0,003");
		assertEquals(Unit.NOUNIT.toString(0.0094), "0,009");

		assertEquals(Unit.NOUNIT.toString(0.0095), "0,01"); // no trailing zeros after rounding

		assertEquals(Unit.NOUNIT.toString(0.0114), "0,011");
		assertEquals(Unit.NOUNIT.toString(0.0115), "0,012"); // round to even
		assertEquals(Unit.NOUNIT.toString(0.0119), "0,012");
		assertEquals(Unit.NOUNIT.toString(0.0124), "0,012");
		assertEquals(Unit.NOUNIT.toString(0.0125), "0,012"); // round to even
		assertEquals(Unit.NOUNIT.toString(0.0129), "0,013");

		assertEquals(Unit.NOUNIT.toString(0.0949), "0,095"); // boundary check

		// positive numbers < 100
		assertEquals(Unit.NOUNIT.toString(0.0095), "0,01"); // boundary check

		assertEquals(Unit.NOUNIT.toString(0.1111), "0,111");
		assertEquals(Unit.NOUNIT.toString(0.1115), "0,112"); // round to even
		assertEquals(Unit.NOUNIT.toString(0.1117), "0,112");
		assertEquals(Unit.NOUNIT.toString(0.1121), "0,112");
		assertEquals(Unit.NOUNIT.toString(0.1125), "0,112"); // round to even
		assertEquals(Unit.NOUNIT.toString(0.1127), "0,113");

		assertEquals(Unit.NOUNIT.toString(1.113), "1,11");
		assertEquals(Unit.NOUNIT.toString(1.115), "1,12"); // round to even
		assertEquals(Unit.NOUNIT.toString(1.117), "1,12");
		assertEquals(Unit.NOUNIT.toString(1.123), "1,12");
		assertEquals(Unit.NOUNIT.toString(1.125), "1,12"); // round to even
		assertEquals(Unit.NOUNIT.toString(1.127), "1,13");

		assertEquals(Unit.NOUNIT.toString(12.320), "12,3");
		assertEquals(Unit.NOUNIT.toString(12.350), "12,4"); // round to even
		assertEquals(Unit.NOUNIT.toString(12.355), "12,4");
		assertEquals(Unit.NOUNIT.toString(12.420), "12,4");
		assertEquals(Unit.NOUNIT.toString(12.450), "12,4"); // round to even
		assertEquals(Unit.NOUNIT.toString(12.455), "12,5");
		// positive numbers > 1E6
		assertEquals(Unit.NOUNIT.toString(1234567.89), "1,23E6");
		assertEquals(Unit.NOUNIT.toString(12345678.9), "1,23E7");

		// Inch precision
		assertEquals(UnitGroup.UNITS_LENGTH.findApproximate("in").toString(25.125 * 25.4 / 1000), "25,125");
	}

	@Test
	public void testNegativeToString() {
		if (isPointDecimalSeparator()) {
			testNegativeWithPoint();
		} else {
			testNegativeWithComma();
		}

		// very small negative numbers ( < 0.0005) are returned as "0");
		assertEquals(Unit.NOUNIT.toString(-0.00050), "0"); // check boundary of change in format

		// negative numbers <= 1E6
		assertEquals(Unit.NOUNIT.toString(-123.20), "-123");
		assertEquals(Unit.NOUNIT.toString(-123.50), "-124"); // round to even
		assertEquals(Unit.NOUNIT.toString(-123.55), "-124");
		assertEquals(Unit.NOUNIT.toString(-124.20), "-124");
		assertEquals(Unit.NOUNIT.toString(-124.50), "-124"); // round to even
		assertEquals(Unit.NOUNIT.toString(-124.55), "-125");

		assertEquals(Unit.NOUNIT.toString(-1234.2), "-1234");
		assertEquals(Unit.NOUNIT.toString(-1234.5), "-1234"); // round to even
		assertEquals(Unit.NOUNIT.toString(-1234.6), "-1235");
		assertEquals(Unit.NOUNIT.toString(-1235.2), "-1235");
		assertEquals(Unit.NOUNIT.toString(-1235.5), "-1236"); // round to even
		assertEquals(Unit.NOUNIT.toString(-1235.6), "-1236");

		assertEquals(Unit.NOUNIT.toString(-123456.789), "-123457");

		assertEquals(Unit.NOUNIT.toString(-1000000), "-1000000"); // boundary check
	}

	private void testNegativeWithComma() {
		// negative number < 0.095 use 3 digit decimal format
		assertEquals(Unit.NOUNIT.toString(-0.00051), "-0,001"); // check boundary of change in format
		assertEquals(Unit.NOUNIT.toString(-0.00060), "-0,001");

		// rounding at third digit.
		assertEquals(Unit.NOUNIT.toString(-0.0014), "-0,001");
		assertEquals(Unit.NOUNIT.toString(-0.0015), "-0,002"); // round to even
		assertEquals(Unit.NOUNIT.toString(-0.0016), "-0,002");
		assertEquals(Unit.NOUNIT.toString(-0.0024), "-0,002");
		assertEquals(Unit.NOUNIT.toString(-0.0025), "-0,002"); // round to even
		assertEquals(Unit.NOUNIT.toString(-0.0026), "-0,003");
		assertEquals(Unit.NOUNIT.toString(-0.0094), "-0,009");

		assertEquals(Unit.NOUNIT.toString(-0.0095), "-0,01"); // no trailing zeros after rounding

		assertEquals(Unit.NOUNIT.toString(-0.0114), "-0,011");
		assertEquals(Unit.NOUNIT.toString(-0.0115), "-0,012"); // round to even
		assertEquals(Unit.NOUNIT.toString(-0.0119), "-0,012");
		assertEquals(Unit.NOUNIT.toString(-0.0124), "-0,012");
		assertEquals(Unit.NOUNIT.toString(-0.0125), "-0,012"); // round to even
		assertEquals(Unit.NOUNIT.toString(-0.0129), "-0,013");

		assertEquals(Unit.NOUNIT.toString(-0.0949), "-0,095"); // boundary check

		// negative numbers < 100
		assertEquals(Unit.NOUNIT.toString(-0.0095), "-0,01"); // boundary check

		assertEquals(Unit.NOUNIT.toString(-0.1111), "-0,111");
		assertEquals(Unit.NOUNIT.toString(-0.1115), "-0,112"); // round to even
		assertEquals(Unit.NOUNIT.toString(-0.1117), "-0,112");
		assertEquals(Unit.NOUNIT.toString(-0.1121), "-0,112");
		assertEquals(Unit.NOUNIT.toString(-0.1125), "-0,112"); // round to even
		assertEquals(Unit.NOUNIT.toString(-0.1127), "-0,113");

		assertEquals(Unit.NOUNIT.toString(-1.113), "-1,11");
		assertEquals(Unit.NOUNIT.toString(-1.115), "-1,12"); // round to even
		assertEquals(Unit.NOUNIT.toString(-1.117), "-1,12");
		assertEquals(Unit.NOUNIT.toString(-1.123), "-1,12");
		assertEquals(Unit.NOUNIT.toString(-1.125), "-1,12"); // round to even
		assertEquals(Unit.NOUNIT.toString(-1.127), "-1,13");

		assertEquals(Unit.NOUNIT.toString(-12.320), "-12,3");
		assertEquals(Unit.NOUNIT.toString(-12.350), "-12,4"); // round to even
		assertEquals(Unit.NOUNIT.toString(-12.355), "-12,4");
		assertEquals(Unit.NOUNIT.toString(-12.420), "-12,4");
		assertEquals(Unit.NOUNIT.toString(-12.450), "-12,4"); // round to even
		assertEquals(Unit.NOUNIT.toString(-12.455), "-12,5");
		// negative numbers > 1E6
		assertEquals(Unit.NOUNIT.toString(-1234567.89), "-1,23E6");
		assertEquals(Unit.NOUNIT.toString(-12345678.9), "-1,23E7");

	}

	private void testNegativeWithPoint() {
		// negative number < 0.095 use 3 digit decimal format
		assertEquals(Unit.NOUNIT.toString(-0.00051), "-0.001"); // check boundary of change in format
		assertEquals(Unit.NOUNIT.toString(-0.00060), "-0.001");

		// rounding at third digit.
		assertEquals(Unit.NOUNIT.toString(-0.0014), "-0.001");
		assertEquals(Unit.NOUNIT.toString(-0.0015), "-0.002"); // round to even
		assertEquals(Unit.NOUNIT.toString(-0.0016), "-0.002");
		assertEquals(Unit.NOUNIT.toString(-0.0024), "-0.002");
		assertEquals(Unit.NOUNIT.toString(-0.0025), "-0.002"); // round to even
		assertEquals(Unit.NOUNIT.toString(-0.0026), "-0.003");
		assertEquals(Unit.NOUNIT.toString(-0.0094), "-0.009");

		assertEquals(Unit.NOUNIT.toString(-0.0095), "-0.01"); // no trailing zeros after rounding

		assertEquals(Unit.NOUNIT.toString(-0.0114), "-0.011");
		assertEquals(Unit.NOUNIT.toString(-0.0115), "-0.012"); // round to even
		assertEquals(Unit.NOUNIT.toString(-0.0119), "-0.012");
		assertEquals(Unit.NOUNIT.toString(-0.0124), "-0.012");
		assertEquals(Unit.NOUNIT.toString(-0.0125), "-0.012"); // round to even
		assertEquals(Unit.NOUNIT.toString(-0.0129), "-0.013");

		assertEquals(Unit.NOUNIT.toString(-0.0949), "-0.095"); // boundary check

		// negative numbers < 100
		assertEquals(Unit.NOUNIT.toString(-0.0095), "-0.01"); // boundary check

		assertEquals(Unit.NOUNIT.toString(-0.1111), "-0.111");
		assertEquals(Unit.NOUNIT.toString(-0.1115), "-0.112"); // round to even
		assertEquals(Unit.NOUNIT.toString(-0.1117), "-0.112");
		assertEquals(Unit.NOUNIT.toString(-0.1121), "-0.112");
		assertEquals(Unit.NOUNIT.toString(-0.1125), "-0.112"); // round to even
		assertEquals(Unit.NOUNIT.toString(-0.1127), "-0.113");

		assertEquals(Unit.NOUNIT.toString(-1.113), "-1.11");
		assertEquals(Unit.NOUNIT.toString(-1.115), "-1.12"); // round to even
		assertEquals(Unit.NOUNIT.toString(-1.117), "-1.12");
		assertEquals(Unit.NOUNIT.toString(-1.123), "-1.12");
		assertEquals(Unit.NOUNIT.toString(-1.125), "-1.12"); // round to even
		assertEquals(Unit.NOUNIT.toString(-1.127), "-1.13");

		assertEquals(Unit.NOUNIT.toString(-12.320), "-12.3");
		assertEquals(Unit.NOUNIT.toString(-12.350), "-12.4"); // round to even
		assertEquals(Unit.NOUNIT.toString(-12.355), "-12.4");
		assertEquals(Unit.NOUNIT.toString(-12.420), "-12.4");
		assertEquals(Unit.NOUNIT.toString(-12.450), "-12.4"); // round to even
		assertEquals(Unit.NOUNIT.toString(-12.455), "-12.5");
		// negative numbers > 1E6
		assertEquals(Unit.NOUNIT.toString(-1234567.89), "-1.23E6");
		assertEquals(Unit.NOUNIT.toString(-12345678.9), "-1.23E7");
	}

}
