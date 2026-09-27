package info.openrocket.swing.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import info.openrocket.core.util.ExpressionParser;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class TraceExceptionTest extends BaseTestCase {

	@Test
	public void locationOfCoreLogLineIsItsCallSite() throws Exception {
		Logger logger = (Logger) LoggerFactory.getLogger(ExpressionParser.class);
		LogbackBufferLoggerAdaptor adaptor = new LogbackBufferLoggerAdaptor(4);
		adaptor.setName("trace-location");
		adaptor.start();
		logger.addAppender(adaptor);
		Level previousLevel = logger.getLevel();
		logger.setLevel(Level.DEBUG);
		try {
			// Logs "Evaluated expression ..." at the debug level
			new ExpressionParser().parse("1+1");
		} finally {
			logger.detachAppender(adaptor);
			logger.setLevel(previousLevel);
		}

		List<LogLine> logs = adaptor.getLogBuffer().getLogs();
		assertEquals(1, logs.size());
		String location = logs.get(0).getLocation();
		assertTrue(location.startsWith("(ExpressionParser.java:"), location);
	}
}
