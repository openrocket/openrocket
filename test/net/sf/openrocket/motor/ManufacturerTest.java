package net.sf.openrocket.motor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ManufacturerTest {

	@Test
	public void testExisting() {
		
		Manufacturer m1, m2, m3, m4, m5;
		
		m1 = Manufacturer.getManufacturer("aerotech");
		m2 = Manufacturer.getManufacturer("a ");
		m3 = Manufacturer.getManufacturer("-isp-");
		m4 = Manufacturer.getManufacturer("at/rcs");
		m5 = Manufacturer.getManufacturer("e");
		
		assertEquals(m1, m2);
		assertEquals(m1, m3);
		assertEquals(m1, m4);
		assertNotSame(m1, m5);
		
	}
	
	@Test
	public void testNew() {
		
		Manufacturer m1, m2, m3;
		
		m1 = Manufacturer.getManufacturer("Unknown");
		m2 = Manufacturer.getManufacturer(" Unknown/ ");
		m3 = Manufacturer.getManufacturer("Unknown/a");
		
		assertEquals(m1.getDisplayName(), "Unknown");
		assertEquals(m2.getDisplayName(), "Unknown");
		assertEquals(m1, m2);
		
		assertEquals(m3.getDisplayName(), "Unknown/a");
		assertNotSame(m1, m3);
		
	}
	
	@Test
	public void simpleNameTest() {
		
		Manufacturer m1, m2, m3, m4;
		
		m1 = Manufacturer.getManufacturer("cs");
		m2 = Manufacturer.getManufacturer("Cesaroni Technology");
		m3 = Manufacturer.getManufacturer("Cesaroni Technology Inc");
		m4 = Manufacturer.getManufacturer("Cesaroni Technology Inc.");

		assertEquals(m1.getDisplayName(), "Cesaroni Technology Inc.");
		assertEquals(m1.toString(), "Cesaroni Technology Inc.");
		assertEquals(m1.getSimpleName(), "Cesaroni Technology");
		
		assertEquals(m1, m2);
		assertEquals(m1, m3);
		assertEquals(m1, m4);
		
	}
	
	@Test
	public void matchesTest() {
		
		Manufacturer m1;
		
		m1 = Manufacturer.getManufacturer("aerotech");
		
		assertTrue(m1.matches("a"));
		assertTrue(m1.matches("a/"));
		assertTrue(m1.matches("a/rcs"));
		assertTrue(m1.matches("a/rms"));
		assertTrue(m1.matches("aerotech  ...-/%¤#_!"));
		assertTrue(m1.matches(" .isp/"));
		
		assertFalse(m1.matches("aero/tech"));
		assertFalse(m1.matches("aero.tech"));
		assertFalse(m1.matches("aero_tech"));
		assertFalse(m1.matches("aero tech"));
	}
	
}
