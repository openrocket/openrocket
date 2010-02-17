package net.sf.openrocket.logging;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

public class LogLine implements Comparable<LogLine> {
	
	private static final AtomicInteger logCount = new AtomicInteger(1);
	private static final long startTime = System.currentTimeMillis();
	
	private final LogLevel level;
	private final int count;
	private final long timestamp;
	private final TraceException trace;
	private final String message;
	private final Throwable cause;
	
	private String formattedMessage = null;
	

	public LogLine(LogLevel level, TraceException trace, String message, Throwable cause) {
		this(level, logCount.getAndIncrement(), System.currentTimeMillis() - startTime,
				trace, message, cause);
	}
	
	public LogLine(LogLevel level, int count, TraceException trace, String message, 
			Throwable cause) {
		this(level, count, System.currentTimeMillis() - startTime, trace, message, cause);
	}
	
	public LogLine(LogLevel level, int count, long timestamp, 
			TraceException trace, String message, Throwable cause) {
		this.level = level;
		this.count = count;
		this.timestamp = timestamp;
		this.trace = trace;
		this.message = message;
		this.cause = cause;
	}

	
	
	/**
	 * @return the level
	 */
	public LogLevel getLevel() {
		return level;
	}


	/**
	 * @return the count
	 */
	public int getLogCount() {
		return count;
	}


	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}


	/**
	 * @return the trace
	 */
	public TraceException getTrace() {
		return trace;
	}


	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}


	/**
	 * @return the error
	 */
	public Throwable getCause() {
		return cause;
	}




	/**
	 * Return a formatted string of the log line.  The line contains the log
	 * line count, the time stamp, the log level, the trace position, the log
	 * message and, if provided, the stack trace of the error throwable.
	 */
	@Override
	public String toString() {
		if (formattedMessage == null) {
			formattedMessage = String.format("%4d %10.3f %-" + LogLevel.LENGTH + "s %s %s",
					count, timestamp/1000.0, level.toString(),
					trace.getMessage(), message);
			if (cause != null) {
				StackTraceWriter stw = new StackTraceWriter();
				PrintWriter pw = new PrintWriter(stw);
				cause.printStackTrace(pw);
				pw.flush();
				formattedMessage = formattedMessage + "\n" + stw.toString();
			}
		}
		return formattedMessage;
	}


	/**
	 * Compare against another log line based on the log line count number.
	 */
	@Override
	public int compareTo(LogLine o) {
		return this.count - o.count;
	}
	
}
