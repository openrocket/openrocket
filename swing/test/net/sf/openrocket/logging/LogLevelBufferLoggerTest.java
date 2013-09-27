package net.sf.openrocket.logging;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

public class LogLevelBufferLoggerTest {
	
	// NOTE cast
	private final static Logger logger = (Logger) LoggerFactory.getLogger(LogLevelBufferLoggerTest.class);
	
	@Test
	@Ignore
	// TODO: test testLogger() is failing, prob due to changes in logging recently
	public void testLogger() {
		
		// assume SLF4J is bound to logback in the current environment
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		
		// Call context.reset() to clear any previous configuration, e.g. default 
		// configuration. For multi-step configuration, omit calling context.reset().
		context.reset();
		LogbackBufferLoggerAdaptor a = new LogbackBufferLoggerAdaptor(4);
		a.start();
		logger.addAppender(a);
		
		logger.debug("debug 1");
		logger.debug("debug 2");
		logger.info(Markers.USER_MARKER, "user 1");
		logger.info("info 1");
		logger.info("info 2");
		logger.warn("warn 1");
		logger.debug("debug 3");
		logger.debug("debug 4");
		logger.info(Markers.USER_MARKER, "user 2");
		logger.info("info 3");
		logger.error("error 1");
		logger.debug("debug 5");
		logger.warn("warn 2");
		logger.debug("debug 6");
		logger.info(Markers.USER_MARKER, "user 3");
		logger.info("info 4");
		logger.debug("debug 7");
		logger.info("info 5");
		logger.debug("debug 8");
		logger.info("info 6");
		
		LogLevelBufferLogger llbl = LoggingSystemSetup.getBufferLogger();
		List<LogLine> list = llbl.getLogs();
		assertEquals(16, list.size());
		
		assertEquals("user 1", list.get(0).getMessage());
		assertEquals("warn 1", list.get(1).getMessage());
		assertEquals("user 2", list.get(2).getMessage());
		assertEquals("===== 2 INFO lines removed =====", list.get(3).getMessage());
		assertEquals("info 3", list.get(4).getMessage());
		assertEquals("error 1", list.get(5).getMessage());
		assertEquals("===== 4 DEBUG lines removed =====", list.get(6).getMessage());
		assertEquals("debug 5", list.get(7).getMessage());
		assertEquals("warn 2", list.get(8).getMessage());
		assertEquals("debug 6", list.get(9).getMessage());
		assertEquals("user 3", list.get(10).getMessage());
		assertEquals("info 4", list.get(11).getMessage());
		assertEquals("debug 7", list.get(12).getMessage());
		assertEquals("info 5", list.get(13).getMessage());
		assertEquals("debug 8", list.get(14).getMessage());
		assertEquals("info 6", list.get(15).getMessage());
		
	}
}
