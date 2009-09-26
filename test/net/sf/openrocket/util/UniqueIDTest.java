package net.sf.openrocket.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class UniqueIDTest {

	@Test
	public void integerTest() {
		
		int n = UniqueID.next();
		assertTrue(n > 0);
		assertEquals(n+1, UniqueID.next());
		assertEquals(n+2, UniqueID.next());
		assertEquals(n+3, UniqueID.next());
		
	}
	
	
	@Test
	public void stringTest() {
		String id = UniqueID.uuid();
		assertNotNull(id);
		assertNotSame(id, UniqueID.uuid());
		assertNotSame(id, UniqueID.uuid());
	}
	
	@Test
	public void hashedTest() {
		String id = UniqueID.generateHashedID();
		assertNotNull(id);
		
		boolean matchhigh = false;
		boolean matchlow = false;
		for (int i=0; i<100; i++) {
			String newid = UniqueID.generateHashedID();
			assertNotNull(newid);
			assertNotSame(id, newid);
			assertTrue(newid.matches("^[0-9a-fA-F]{32}$"));
			
			// Check that both high and low values occur
			matchhigh = matchhigh || newid.matches("^([0-9a-fA-F][0-9a-fA-F])*[A-F].*");
			matchlow = matchlow || newid.matches("^([0-9a-fA-F][0-9a-fA-F])*[0-4].*");
		}
		assertTrue(matchhigh);
		assertTrue(matchlow);
	}
	
}
