package net.sf.openrocket.logging;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class LoggingTest {

	@Test
	public void testLoggers() {
		// Ensure a sane stack trace
		actualTest();
	}
	
	private void actualTest() {
		BufferLogger log3 = new BufferLogger(3);
		BufferLogger log4 = new BufferLogger(4);
		
		DelegatorLogger delegator = new DelegatorLogger();
		delegator.addLogger(log3);
		delegator.addLogger(log4);
		
		delegator.debug("one");
		delegator.debug("two");
		delegator.info("three");
		delegator.warn(1, "four");
		delegator.error("five");
		
		List<LogLine> logs = log4.getLogs();
		assertEquals(4, logs.size());
		assertTrue(logs.get(0).toString(), logs.get(0).toString().matches(
				" *[0-9]+ +[0-9]+\\.[0-9]+ +DEBUG \\(LoggingTest.java:[0-9]+\\) two"));
		assertTrue(logs.get(1).toString(), logs.get(1).toString().matches(
				" *[0-9]+ +[0-9]+\\.[0-9]+ +INFO  \\(LoggingTest.java:[0-9]+\\) three"));
		assertTrue(logs.get(2).toString(), logs.get(2).toString().matches(
				" *[0-9]+ +[0-9]+\\.[0-9]+ +WARN  \\(LoggingTest.java:[0-9]+ LoggingTest.java:[0-9]+\\) four"));
		assertTrue(logs.get(3).toString(), logs.get(3).toString().matches(
				" *[0-9]+ +[0-9]+\\.[0-9]+ +ERROR \\(LoggingTest.java:[0-9]+\\) five"));
		
		logs = log3.getLogs();
		assertEquals(3, logs.size());
		assertTrue(logs.get(0).toString(), logs.get(0).toString().matches(
				" *[0-9]+ +[0-9]+\\.[0-9]+ +INFO  \\(LoggingTest.java:[0-9]+\\) three"));
		assertTrue(logs.get(1).toString(), logs.get(1).toString().matches(
				" *[0-9]+ +[0-9]+\\.[0-9]+ +WARN  \\(LoggingTest.java:[0-9]+ LoggingTest.java:[0-9]+\\) four"));
		assertTrue(logs.get(2).toString(), logs.get(2).toString().matches(
				" *[0-9]+ +[0-9]+\\.[0-9]+ +ERROR \\(LoggingTest.java:[0-9]+\\) five"));
		
	}
	
	public static void main(String[] args) {
		StandardOutputLogger logger = new StandardOutputLogger();
		
		logger.debug("a debug message");
		logger.info("an info message");
		logger.warn("a warning message");
		logger.error("an error message");
		
		logger.debug(4, "Debugging");
	}
}
