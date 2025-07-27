package info.openrocket.core.unit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ValueTest {

	@Test
	public void testValues() {
		Value v1, v2;

		v1 = new Value(273.15, UnitGroup.UNITS_TEMPERATURE.findApproximate("F"));
		v2 = new Value(283.153, UnitGroup.UNITS_TEMPERATURE.findApproximate("C"));

		assertTrue(v1.compareTo(v2) > 0);
		assertTrue(v2.compareTo(v1) < 0);
		assertTrue(v1.compareTo(v1) == 0);
		assertTrue(v2.compareTo(v2) == 0);

		v2 = new Value(283.15, UnitGroup.UNITS_TEMPERATURE.findApproximate("K"));
		assertTrue(v1.compareTo(v2) > 0);
		assertTrue(v2.compareTo(v1) < 0);
		assertEquals(v2.toString(), "283.15 K");

		v2 = new Value(283.15, UnitGroup.UNITS_TEMPERATURE.findApproximate("F"));
		assertTrue(v1.compareTo(v2) < 0);
		assertTrue(v2.compareTo(v1) > 0);

		v1 = new Value(Double.NaN, UnitGroup.UNITS_TEMPERATURE.findApproximate("F"));
		assertTrue(v1.compareTo(v2) > 0);
		assertTrue(v2.compareTo(v1) < 0);

		v2 = new Value(Double.NaN, UnitGroup.UNITS_TEMPERATURE.findApproximate("F"));
		assertTrue(v1.compareTo(v2) == 0);
		assertTrue(v1.compareTo(v2) == 0);
		assertEquals(v1.toString(), "N/A");
		assertEquals(v2.toString(), "N/A");

	}

}
