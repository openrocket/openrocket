package info.openrocket.core.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.DecimalFormat;

import org.junit.jupiter.api.Test;

public class FractionalUnitTest {

	private final static Unit testUnit = new FractionalUnit(1, "unit", "unit", 4, 0.5);
	private final static Unit testUnitApprox = new FractionalUnit(1, "unit", "unit", 16, 0.5, 0.02);

	private final static Unit inchUnit = new FractionalUnit(0.0254, "in/64", "in", 64, 1d / 16d);

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

		assertEquals(-1d, testUnit.getNextValue(-1.2), 0.0);
		assertEquals(-1d, testUnit.getNextValue(-1.4), 0.0);

		assertEquals(-0.5d, testUnit.getNextValue(-0.7), 0.0);
		assertEquals(-0.5d, testUnit.getNextValue(-0.9), 0.0);
		assertEquals(-0.5d, testUnit.getNextValue(-1.0), 0.0);

		assertEquals(0.0d, testUnit.getNextValue(-0.05), 0.0);
		assertEquals(0.0d, testUnit.getNextValue(-0.062), 0.0);
		assertEquals(0.0d, testUnit.getNextValue(-0.07), 0.0);
		assertEquals(0.0d, testUnit.getNextValue(-0.11), 0.0);

		assertEquals(0.5d, testUnit.getNextValue(0), 0.0);
		assertEquals(0.5d, testUnit.getNextValue(0.01), 0.0);
		assertEquals(0.5d, testUnit.getNextValue(0.062), 0.0);
		assertEquals(0.5d, testUnit.getNextValue(0.0625), 0.0);

		assertEquals(1d, testUnit.getNextValue(0.51), 0.0);
		assertEquals(1d, testUnit.getNextValue(0.7), 0.0);
	}

	@Test
	public void testDecrement() {

		assertEquals(-1.5d, testUnit.getPreviousValue(-1.2), 0.0);
		assertEquals(-1.5d, testUnit.getPreviousValue(-1.4), 0.0);
		assertEquals(-1.5d, testUnit.getPreviousValue(-1.0), 0.0);

		assertEquals(-1d, testUnit.getPreviousValue(-0.7), 0.0);
		assertEquals(-1d, testUnit.getPreviousValue(-0.9), 0.0);

		assertEquals(-0.5d, testUnit.getPreviousValue(-0.01), 0.0);
		assertEquals(-0.5d, testUnit.getPreviousValue(-0.05), 0.0);
		assertEquals(-0.5d, testUnit.getPreviousValue(-0.062), 0.0);
		assertEquals(-0.5d, testUnit.getPreviousValue(-0.07), 0.0);
		assertEquals(-0.5d, testUnit.getPreviousValue(0), 0.0);

		assertEquals(0.0d, testUnit.getPreviousValue(0.49), 0.0);
		assertEquals(0.0d, testUnit.getPreviousValue(0.262), 0.0);
		assertEquals(0.0d, testUnit.getPreviousValue(0.51), 0.0);

		assertEquals(0.5d, testUnit.getPreviousValue(0.7), 0.0);

		assertEquals(1.0d, testUnit.getPreviousValue(1.2), 0.0);
	}

	@Test
	public void testToStringDefaultPrecision() {

		if (isPointDecimalSeparator()) {
			assertEquals(testUnit.toString(-1.2), "-1.2");
			assertEquals(testUnit.toString(-1.3), "-1.3");

			assertEquals(testUnit.toString(-.2), "-0.2");
			assertEquals(testUnit.toString(-.3), "-0.3");

			assertEquals(testUnit.toString(-.1), "-0.1");
			assertEquals(testUnit.toString(.1), "0.1");

			assertEquals(testUnit.toString(.2), "0.2");
			assertEquals(testUnit.toString(.3), "0.3");

			assertEquals(testUnit.toString(1.2), "1.2");
			assertEquals(testUnit.toString(1.3), "1.3");
		} else {
			assertEquals(testUnit.toString(-1.2), "-1,2");
			assertEquals(testUnit.toString(-1.3), "-1,3");

			assertEquals(testUnit.toString(-.2), "-0,2");
			assertEquals(testUnit.toString(-.3), "-0,3");

			assertEquals(testUnit.toString(-.1), "-0,1");
			assertEquals(testUnit.toString(.1), "0,1");

			assertEquals(testUnit.toString(.2), "0,2");
			assertEquals(testUnit.toString(.3), "0,3");

			assertEquals(testUnit.toString(1.2), "1,2");
			assertEquals(testUnit.toString(1.3), "1,3");
		}
		// default epsilon is 0.025

		assertEquals(testUnit.toString(-1.225), "-1 \u00B9\u2044\u2084");
		assertEquals(testUnit.toString(-1.227), "-1 \u00B9\u2044\u2084");
		assertEquals(testUnit.toString(-1.25), "-1 \u00B9\u2044\u2084");
		assertEquals(testUnit.toString(-1.25), "-1 \u00B9\u2044\u2084");
		assertEquals(testUnit.toString(-1.275), "-1 \u00B9\u2044\u2084");

		assertEquals(testUnit.toString(-.225), "-\u00B9\u2044\u2084");
		assertEquals(testUnit.toString(-.25), "-\u00B9\u2044\u2084");
		assertEquals(testUnit.toString(-.274), "-\u00B9\u2044\u2084");
		// assertEquals(testUnit.toString(-.275), "-1/4"); // this has round-off error
		// which pushes it over epsilon

		assertEquals(testUnit.toString(-0.024), "0");
		assertEquals(testUnit.toString(0), "0");
		assertEquals(testUnit.toString(.024), "0");

		assertEquals(testUnit.toString(.225), "\u00B9\u2044\u2084");
		assertEquals(testUnit.toString(.25), "\u00B9\u2044\u2084");
		assertEquals(testUnit.toString(.274), "\u00B9\u2044\u2084");

		assertEquals(testUnit.toString(1.225), "1 \u00B9\u2044\u2084");
		assertEquals(testUnit.toString(1.25), "1 \u00B9\u2044\u2084");
		assertEquals(testUnit.toString(1.275), "1 \u00B9\u2044\u2084");
	}

	@Test
	public void testToStringWithPrecision() {
		if (isPointDecimalSeparator()) {
			assertEquals(testUnitApprox.toString(-1.225), "-1.225");
			assertEquals(testUnitApprox.toString(-1.275), "-1.275");

			assertEquals(testUnitApprox.toString(-.225), "-0.225");
			assertEquals(testUnitApprox.toString(-.275), "-0.275");

			assertEquals(testUnitApprox.toString(-.1), "-0.1");

			assertEquals(testUnitApprox.toString(-0.024), "-0.024");
			assertEquals(testUnitApprox.toString(.024), "0.024");

			assertEquals(testUnitApprox.toString(.1), "0.1");

			assertEquals(testUnitApprox.toString(.275), "0.275");
			assertEquals(testUnitApprox.toString(.225), "0.225");

			assertEquals(testUnitApprox.toString(1.225), "1.225");
			assertEquals(testUnitApprox.toString(1.275), "1.275");
		} else {
			assertEquals(testUnitApprox.toString(-1.225), "-1,225");
			assertEquals(testUnitApprox.toString(-1.275), "-1,275");

			assertEquals(testUnitApprox.toString(-.225), "-0,225");
			assertEquals(testUnitApprox.toString(-.275), "-0,275");

			assertEquals(testUnitApprox.toString(-.1), "-0,1");

			assertEquals(testUnitApprox.toString(-0.024), "-0,024");
			assertEquals(testUnitApprox.toString(.024), "0,024");

			assertEquals(testUnitApprox.toString(.1), "0,1");

			assertEquals(testUnitApprox.toString(.275), "0,275");
			assertEquals(testUnitApprox.toString(.225), "0,225");

			assertEquals(testUnitApprox.toString(1.225), "1,225");
			assertEquals(testUnitApprox.toString(1.275), "1,275");
		}
		// epsilon is .02

		assertEquals(testUnitApprox.toString(-1.2), "-1 \u00B3\u2044\u2081\u2086");
		assertEquals(testUnitApprox.toString(-1.25), "-1 \u00B9\u2044\u2084");
		assertEquals(testUnitApprox.toString(-1.3), "-1 \u2075\u2044\u2081\u2086");

		assertEquals(testUnitApprox.toString(-.2), "-\u00B3\u2044\u2081\u2086");
		assertEquals(testUnitApprox.toString(-.25), "-\u00B9\u2044\u2084");
		assertEquals(testUnitApprox.toString(-.3), "-\u2075\u2044\u2081\u2086");

		assertEquals(testUnitApprox.toString(0), "0");

		assertEquals(testUnitApprox.toString(.2), "\u00B3\u2044\u2081\u2086");
		assertEquals(testUnitApprox.toString(.25), "\u00B9\u2044\u2084");
		assertEquals(testUnitApprox.toString(.3), "\u2075\u2044\u2081\u2086");

		assertEquals(testUnitApprox.toString(1.2), "1 \u00B3\u2044\u2081\u2086");
		assertEquals(testUnitApprox.toString(1.25), "1 \u00B9\u2044\u2084");
		assertEquals(testUnitApprox.toString(1.3), "1 \u2075\u2044\u2081\u2086");
	}

	private boolean isPointDecimalSeparator() {
		return ((DecimalFormat) DecimalFormat.getInstance()).getDecimalFormatSymbols().getDecimalSeparator() == '.';
	}

	@Test
	public void testInchToString() {

		// Just some random test points.
		assertEquals(inchUnit.toString(1d / 64d * 0.0254), "\u00B9\u2044\u2086\u2084");

		assertEquals(inchUnit.toString(-5d / 64d * 0.0254), "-\u2075\u2044\u2086\u2084");

		assertEquals(inchUnit.toString(9d / 2d * 0.0254), "4 \u00B9\u2044\u2082");

		if (isPointDecimalSeparator()) {
			assertEquals(inchUnit.toString(0.002 * 0.0254), "0.002");
		} else {
			assertEquals(inchUnit.toString(0.002 * 0.0254), "0,002");
		}

		// default body tube length:
		double length = 8d * 0.025;

		assertEquals(inchUnit.toString(length), "7 \u2077\u2044\u2088");

		// had problems with round-off in decrement.

		double v = inchUnit.toUnit(length);
		for (int i = 0; i < 15; i++) {
			assertTrue(v > inchUnit.getPreviousValue(v));
			v = inchUnit.getPreviousValue(v);
		}

	}

}
