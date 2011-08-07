package net.sf.openrocket.unit;

import static org.junit.Assert.*;

import org.junit.Test;

public class ValueTest {
	
	@Test
	public void testValues() {
		Value v1, v2;
		
		v1 = new Value(273.15, UnitGroup.UNITS_TEMPERATURE.findApproximate("F"));
		v2 = new Value(283.15, UnitGroup.UNITS_TEMPERATURE.findApproximate("C"));
		
		assertTrue(v1.compareTo(v2) > 0);
		assertTrue(v2.compareTo(v1) < 0);
		assertTrue(v1.compareTo(v1) == 0);
		assertTrue(v2.compareTo(v2) == 0);
		
		v2 = new Value(283.15, UnitGroup.UNITS_TEMPERATURE.findApproximate("K"));
		assertTrue(v1.compareTo(v2) > 0);
		assertTrue(v2.compareTo(v1) < 0);
		assertEquals("283 K", v2.toString());
		
		v2 = new Value(283.15, UnitGroup.UNITS_TEMPERATURE.findApproximate("F"));
		assertTrue(v1.compareTo(v2) < 0);
		assertTrue(v2.compareTo(v1) > 0);
		

		v1 = new Value(Double.NaN, UnitGroup.UNITS_TEMPERATURE.findApproximate("F"));
		assertTrue(v1.compareTo(v2) > 0);
		assertTrue(v2.compareTo(v1) < 0);
		
		v2 = new Value(Double.NaN, UnitGroup.UNITS_TEMPERATURE.findApproximate("F"));
		assertTrue(v1.compareTo(v2) == 0);
		assertTrue(v1.compareTo(v2) == 0);
		assertEquals("N/A", v1.toString());
		assertEquals("N/A", v2.toString());
		
	}
	
}
