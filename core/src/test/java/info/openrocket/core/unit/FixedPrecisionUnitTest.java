package info.openrocket.core.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FixedPrecisionUnitTest {
	private static final double EPSILON = 1e-8;

	private final Unit unitPoint1 = new FixedPrecisionUnit("unit", 0.1);
	private final Unit unitPoint25 = new FixedPrecisionUnit("unit", 0.25);
	private final Unit unitPoint5 = new FixedPrecisionUnit("unit", 0.5);
	private final Unit unitNoTrailing = new FixedPrecisionUnit("unit", 0.1, 1.0, false);
	private final Unit unitWithMultiplier = new FixedPrecisionUnit("unit", 0.1, 2.0);

	@BeforeAll
	public static void setUp() {
		// Set locale to ensure consistent formatting
		Locale.setDefault(Locale.US);
	}

	@Test
	public void testRounding() {
		// Test rounding with 0.1 precision
		assertEquals(1.0, unitPoint1.round(1.04), EPSILON);
		assertEquals(1.1, unitPoint1.round(1.05), EPSILON);
		assertEquals(1.1, unitPoint1.round(1.14), EPSILON);
		assertEquals(1.2, unitPoint1.round(1.15), EPSILON);

		// Test rounding with 0.25 precision
		assertEquals(1.0, unitPoint25.round(1.12), EPSILON);
		assertEquals(1.25, unitPoint25.round(1.13), EPSILON);
		assertEquals(1.25, unitPoint25.round(1.37), EPSILON);
		assertEquals(1.5, unitPoint25.round(1.38), EPSILON);

		// Test rounding with 0.5 precision
		assertEquals(1.0, unitPoint5.round(1.24), EPSILON);
		assertEquals(1.5, unitPoint5.round(1.25), EPSILON);
		assertEquals(1.5, unitPoint5.round(1.74), EPSILON);
		assertEquals(2.0, unitPoint5.round(1.75), EPSILON);
	}

	@Test
	public void testNextValue() {
		// Test with 0.1 precision
		assertEquals(1.1, unitPoint1.getNextValue(1.0), EPSILON);
		assertEquals(1.2, unitPoint1.getNextValue(1.1), EPSILON);
		assertEquals(0.1, unitPoint1.getNextValue(0.0), EPSILON);
		assertEquals(-0.9, unitPoint1.getNextValue(-1.0), EPSILON);

		// Test with 0.25 precision
		assertEquals(1.25, unitPoint25.getNextValue(1.0), EPSILON);
		assertEquals(1.5, unitPoint25.getNextValue(1.25), EPSILON);
		assertEquals(0.25, unitPoint25.getNextValue(0.0), EPSILON);

		// Test with 0.5 precision
		assertEquals(1.5, unitPoint5.getNextValue(1.0), EPSILON);
		assertEquals(2.0, unitPoint5.getNextValue(1.5), EPSILON);
		assertEquals(0.5, unitPoint5.getNextValue(0.0), EPSILON);
	}

	@Test
	public void testPreviousValue() {
		// Test with 0.1 precision
		assertEquals(0.9, unitPoint1.getPreviousValue(1.0), EPSILON);
		assertEquals(0.8, unitPoint1.getPreviousValue(0.9), EPSILON);
		assertEquals(-0.1, unitPoint1.getPreviousValue(0.0), EPSILON);
		assertEquals(-1.1, unitPoint1.getPreviousValue(-1.0), EPSILON);

		// Test with 0.25 precision
		assertEquals(0.75, unitPoint25.getPreviousValue(1.0), EPSILON);
		assertEquals(0.5, unitPoint25.getPreviousValue(0.75), EPSILON);
		assertEquals(-0.25, unitPoint25.getPreviousValue(0.0), EPSILON);

		// Test with 0.5 precision
		assertEquals(0.5, unitPoint5.getPreviousValue(1.0), EPSILON);
		assertEquals(0.0, unitPoint5.getPreviousValue(0.5), EPSILON);
		assertEquals(-0.5, unitPoint5.getPreviousValue(0.0), EPSILON);
	}

	@Test
	public void testToString() {
		// Test with trailing zeros (default)
		assertEquals("1.0", unitPoint1.toString(1.0));
		assertEquals("1.1", unitPoint1.toString(1.1));
		assertEquals("-1.0", unitPoint1.toString(-1.0));
		assertEquals("0.0", unitPoint1.toString(0.0));

		// Test without trailing zeros
		assertEquals("1", unitNoTrailing.toString(1.0));
		assertEquals("1.1", unitNoTrailing.toString(1.1));
		assertEquals("-1", unitNoTrailing.toString(-1.0));
		assertEquals("0", unitNoTrailing.toString(0.0));

		// Test with multiplier
		assertEquals("0.5", unitWithMultiplier.toString(1.0));
		assertEquals("0.6", unitWithMultiplier.toString(1.1));
		assertEquals("-0.5", unitWithMultiplier.toString(-1.0));
		assertEquals("0.0", unitWithMultiplier.toString(0.0));
	}

	@Test
	public void testGetTicks() {
		Unit unit = new FixedPrecisionUnit("unit", 0.5);
		Tick[] ticks = unit.getTicks(0.0, 5.0, 0.5, 1.0);

		// Verify we have the expected number of ticks
		assertTrue(ticks.length > 0);

		// Test specific tick values
		for (int i = 0; i < ticks.length - 1; i++) {
			// Verify ticks are in ascending order
			assertTrue(ticks[i].value < ticks[i + 1].value);

			// Verify tick spacing matches precision
			assertEquals(0.5, ticks[i + 1].value - ticks[i].value, 0.001);
		}

		// Test major/minor tick marking
		for (Tick tick : ticks) {
			if (Math.abs(tick.value % 1.0) < 0.001) {
				// Whole numbers should be major ticks
				assertTrue(tick.major);
			}
		}
	}
}