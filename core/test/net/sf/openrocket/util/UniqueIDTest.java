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
	
}
