package info.openrocket.swing.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import info.openrocket.core.logging.Markers;

public class LogbackBufferLoggerAdaptor extends AppenderBase<ILoggingEvent> {
	
	private final DelegatorLogger logHelper;
	private final LogLevelBufferLogger logBuffer;
	
	private static final int LOG_BUFFER_LENGTH = 50;
	
	public LogbackBufferLoggerAdaptor(int bufferLength) {
		logHelper = new DelegatorLogger();
		logBuffer = new LogLevelBufferLogger(bufferLength);
		logHelper.addLogger(logBuffer);
	}
	
	public LogbackBufferLoggerAdaptor() {
		this(LOG_BUFFER_LENGTH);
	}
	
	DelegatorLogger getLogHelper() {
		return logHelper;
	}
	
	LogLevelBufferLogger getLogBuffer() {
		return logBuffer;
	}
	
	@Override
	protected void append(ILoggingEvent e) {
		LogLine ll = toLogLine(e);
		
		logHelper.log(ll);
	}
	
	private LogLevel toORLevel(Level l) {
		return switch (l.toInt()) {
			case Level.TRACE_INT -> LogLevel.VBOSE;
			case Level.DEBUG_INT -> LogLevel.DEBUG;
			case Level.INFO_INT -> LogLevel.INFO;
			case Level.WARN_INT -> LogLevel.WARN;
			case Level.ERROR_INT -> LogLevel.ERROR;
			default -> LogLevel.ERROR;
		};
	}
	
	private LogLine toLogLine(ILoggingEvent e) {
		LogLevel l = toORLevel(e.getLevel());
		if (Markers.USER_MARKER.equals(e.getMarker()))
			l = LogLevel.USER;
		if (Markers.STDERR_MARKER.equals(e.getMarker()))
			l = LogLevel.STDERR;
		Throwable t = null;
		if (e.getThrowableProxy() != null) {
			t = ((ThrowableProxy) e.getThrowableProxy()).getThrowable();
		}
		return new LogLine(l, new TraceException(), e.getFormattedMessage(), t);
	}
}
