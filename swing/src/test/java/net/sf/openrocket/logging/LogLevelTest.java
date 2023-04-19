package net.sf.openrocket.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LogLevelTest {
	
	@Test
	public void testLogLevelSeverityCompares() {
		
		// tests comparing level to itself
		assertTrue(LogLevel.DEBUG.atLeast(LogLevel.DEBUG));
		assertFalse(LogLevel.DEBUG.moreThan(LogLevel.DEBUG));
		
		// tests comparing high severity to lower severity (positive tests)
		assertTrue(LogLevel.ERROR.atLeast(LogLevel.WARN));
		assertTrue(LogLevel.ERROR.moreThan(LogLevel.WARN));
		assertTrue(LogLevel.STDERR.atLeast(LogLevel.VBOSE));
		assertTrue(LogLevel.STDERR.moreThan(LogLevel.VBOSE));
		
		// tests comparing lower severity to higher severity (negative tests)
		assertFalse(LogLevel.WARN.atLeast(LogLevel.ERROR));
		assertFalse(LogLevel.WARN.moreThan(LogLevel.ERROR));
		assertFalse(LogLevel.VBOSE.atLeast(LogLevel.STDERR));
		assertFalse(LogLevel.VBOSE.moreThan(LogLevel.STDERR));
		
	}
	
	@Test
	public void testLogLevelFromString() {
		
		LogLevel logLevelReturned;
		
		// null value should return default
		logLevelReturned = LogLevel.fromString(null, LogLevel.INFO);
		assertTrue(logLevelReturned == LogLevel.INFO);
		
		// invalid value should return default
		logLevelReturned = LogLevel.fromString("not a valid log level", LogLevel.INFO);
		assertTrue(logLevelReturned == LogLevel.INFO);
		
		// lowercase value should be uppercased and return correct same value
		logLevelReturned = LogLevel.fromString("debug", LogLevel.INFO);
		assertTrue(logLevelReturned == LogLevel.DEBUG);
		
		// leading and trailing whitespace in value should be trimmed and return correct same value
		logLevelReturned = LogLevel.fromString("  DEBUG  ", LogLevel.INFO);
		assertTrue(logLevelReturned == LogLevel.DEBUG);
		
		// value of all should return lowest level (most verbose) of logging
		logLevelReturned = LogLevel.fromString("all", LogLevel.INFO);
		assertTrue(logLevelReturned == LogLevel.LOWEST);
		
	}
	
	@Test
	public void testMaxLengthOfAnyLogLevelName() {
		assertEquals("Max length of at least one log level name is different than expected.", 6, LogLevel.LENGTH);
	}
	
}
