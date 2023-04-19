package net.sf.openrocket.motor;

import static org.junit.Assert.*;

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
		
		assertTrue(m1 == m2);
		assertTrue(m1 == m3);
		assertTrue(m1 == m4);
		assertFalse(m1 == m5);
		
	}
	
	public void testContrail() {
		Manufacturer c1, c2;
		
		c1 = Manufacturer.getManufacturer("Contrail" );
		
		// Used in rsp files.
		c2 = Manufacturer.getManufacturer("Contrail_Rockets");
		
		assertNotNull(c1);
		assertEquals(c1,c2);
	}
	
	@Test
	public void testNew() {
		
		Manufacturer m1, m2, m3;
		
		m1 = Manufacturer.getManufacturer("Unknown");
		m2 = Manufacturer.getManufacturer(" Unknown/ ");
		m3 = Manufacturer.getManufacturer("Unknown/a");
		
		assertEquals(m1.getDisplayName(), "Unknown");
		assertEquals(m2.getDisplayName(), "Unknown");
		assertTrue(m1 == m2);
		
		assertEquals(m3.getDisplayName(), "Unknown/a");
		assertFalse(m1 == m3);
		
	}
	
	@Test
	public void testSimpleName() {
		
		Manufacturer m1, m2, m3, m4;
		
		m1 = Manufacturer.getManufacturer("cs");
		m2 = Manufacturer.getManufacturer("Cesaroni Technology");
		m3 = Manufacturer.getManufacturer("Cesaroni Technology Inc");
		m4 = Manufacturer.getManufacturer("Cesaroni Technology Inc.");

		assertEquals(m1.getDisplayName(), "Cesaroni Technology Inc.");
		assertEquals(m1.toString(), "Cesaroni Technology Inc.");
		assertEquals(m1.getSimpleName(), "Cesaroni Technology");
		
		assertTrue(m1 == m2);
		assertTrue(m1 == m3);
		assertTrue(m1 == m4);
		
	}
	
	@Test
	public void testMatches() {
		
		Manufacturer m1;
		
		m1 = Manufacturer.getManufacturer("aerotech");
		
		assertTrue(m1.matches("a"));
		assertTrue(m1.matches("a/"));
		assertTrue(m1.matches("a/rcs"));
		assertTrue(m1.matches("a/rms"));
		assertTrue(m1.matches("aerotech  ...-/%#_!"));
		assertTrue(m1.matches(" .isp/"));
		
		assertFalse(m1.matches("aero/tech"));
		assertFalse(m1.matches("aero.tech"));
		assertFalse(m1.matches("aero_tech"));
		assertFalse(m1.matches("aero tech"));
	}
	
}
