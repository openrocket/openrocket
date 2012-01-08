package net.sf.openrocket.logging;

import static org.junit.Assert.*;

import org.junit.Test;

public class TraceExceptionTest {
	
	private TraceException getViaAccess() {
		
		/*
		 * The SubClass test bases on the fact that getViaAccess method is defined on a row number < 20
		 * and the return statement is on a row number > 20.
		 * 
		 * The JRE sometimes adds an additional "access$NNN" method call between the calls with the
		 * row number equal to the method definition line.
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 */

		return new TraceException(0, 1);
	}
	
	
	@Test
	public void testBasic() {
		TraceException trace = new TraceException();
		assertMatch("\\(TraceExceptionTest.java:[2-9][0-9]\\)", trace);
	}
	
	
	@Test
	public void testOneLevelUp() {
		// @formatter:off - these need to be on the same line number
		TraceException trace = getOneLevelUp(); TraceException ref = new TraceException();
		// @formatter:on
		assertEquals(ref.getMessage(), trace.getMessage());
	}
	
	private TraceException getOneLevelUp() {
		return new TraceException(1);
	}
	
	
	@Test
	public void testTwoLevels() {
		TraceException trace = getTwoLevels();
		assertMatch("\\(TraceExceptionTest.java:[2-9][0-9] TraceExceptionTest.java:[2-9][0-9]\\)", trace);
	}
	
	private TraceException getTwoLevels() {
		return new TraceException(0, 1);
	}
	
	
	@Test
	public void testViaSubclass() {
		/*
		 * This tests that TraceException.getMessage ignores the synthetic "access$0" method calls.
		 */

		TraceException trace = new SubClass().getTrace();
		assertMatch("\\(TraceExceptionTest.java:[2-9][0-9] TraceExceptionTest.java:[2-9][0-9]\\)", trace);
	}
	
	private class SubClass {
		private TraceException getTrace() {
			return getViaAccess();
		}
	}
	
	



	private void assertMatch(String regex, TraceException trace) {
		boolean match = trace.getMessage().matches(regex);
		if (!match) {
			trace.printStackTrace();
			assertTrue("Was: " + trace.getMessage(), match);
		}
	}
	
}
