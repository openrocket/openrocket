package net.sf.openrocket.logging;

import static net.sf.openrocket.logging.LogLevel.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class LogLevelTest {

	@Test
	public void testLevels() {
		assertTrue(DEBUG.atLeast(DEBUG));
		assertFalse(DEBUG.atLeast(INFO));
		assertTrue(ERROR.atLeast(WARN));
		
		assertTrue(ERROR.moreThan(WARN));
		assertFalse(ERROR.moreThan(ERROR));
		
		assertEquals(5, LENGTH);
	}
	
}
