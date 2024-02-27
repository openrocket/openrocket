package info.openrocket.swing.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import info.openrocket.core.logging.Markers;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

public class LogLevelBufferLoggerTest extends BaseTestCase {
	
	// NOTE cast
	private final static Logger logger = (Logger) LoggerFactory.getLogger(LogLevelBufferLoggerTest.class);
	
	@Test
	public void testLogger() {
		// assume SLF4J is bound to logback in the current environment
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		
		// Call context.reset() to clear any previous configuration, e.g. default 
		// configuration. For multi-step configuration, omit calling context.reset().
		context.reset();
		LogbackBufferLoggerAdaptor a = new LogbackBufferLoggerAdaptor(4);
		a.setName("buffer");
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
		
		LogLevelBufferLogger llbl = LoggingSystemSetup.getBufferLogger(logger);
		List<LogLine> list = llbl.getLogs();
		assertEquals(16, list.size());
		
		assertEquals(list.get(0).getMessage(), "user 1");
		assertEquals(list.get(1).getMessage(), "warn 1");
		assertEquals(list.get(2).getMessage(), "user 2");
		assertEquals(list.get(3).getMessage(), "===== 2 INFO lines removed =====");
		assertEquals(list.get(4).getMessage(), "info 3");
		assertEquals(list.get(5).getMessage(), "error 1");
		assertEquals(list.get(6).getMessage(), "===== 4 DEBUG lines removed =====");
		assertEquals(list.get(7).getMessage(), "debug 5");
		assertEquals(list.get(8).getMessage(), "warn 2");
		assertEquals(list.get(9).getMessage(), "debug 6");
		assertEquals(list.get(10).getMessage(), "user 3");
		assertEquals(list.get(11).getMessage(), "info 4");
		assertEquals(list.get(12).getMessage(), "debug 7");
		assertEquals(list.get(13).getMessage(), "info 5");
		assertEquals(list.get(14).getMessage(), "debug 8");
		assertEquals(list.get(15).getMessage(), "info 6");
		
	}
}
