package net.sf.openrocket.util;

import static org.junit.Assert.*;
import net.sf.openrocket.logging.TraceException;

import org.junit.Test;

public class MutableTest {
	
	@Test
	public void testMutable() {
		Mutable m = new Mutable();
		Throwable t = null;
		
		m.check();
		m.check();
		assertTrue(m.isMutable());
		
		m.immute();
		
		try {
			m.check();
			fail();
		} catch (IllegalStateException e) {
			// Success
			t = e.getCause();
			assertTrue(t instanceof TraceException);
		}
		
		m.immute();
		
		try {
			m.check();
			fail();
		} catch (IllegalStateException e) {
			// Success
			assertTrue(e.getCause() == t);
		}
	}
	
	
	@Test
	public void testClone() {
		Mutable m1 = new Mutable();
		Mutable m2 = m1.clone();
		
		assertTrue(m1.isMutable());
		assertTrue(m2.isMutable());
		
		m1.immute();
		
		assertFalse(m1.isMutable());
		assertTrue(m2.isMutable());
	}
	
}
