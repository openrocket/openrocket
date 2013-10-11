package net.sf.openrocket.logging;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Container object for a log line.  A log line consists of the following elements:
 * <ul>
 * 	<li>a LogLevel
 * 	<li>a TraceException
 * 	<li>a message
 * 	<li>a cause Throwable
 * 	<li>an incremental log line counter (provided by LogLine)
 * 	<li>a millisecond timestamp (provided by LogLine)
 * </ul>
 * Any one of the provided input values may be null.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class LogLine implements Comparable<LogLine> {
	
	private static final AtomicInteger logCount = new AtomicInteger(1);
	private static final long startTime = System.currentTimeMillis();
	
	private final LogLevel level;
	private final int count;
	private final long timestamp;
	private final TraceException trace;
	private final String message;
	private final Throwable cause;
	
	private volatile String formattedMessage = null;
	
	
	/**
	 * Construct a LogLine at the current moment.  The next log line count number is selected
	 * and the current run time set to the timestamp.
	 * 
	 * @param level		the logging level
	 * @param trace		the trace exception for the log line, <code>null</code> permitted
	 * @param message	the log message
	 * @param cause		the causing throwable, <code>null</code> permitted
	 */
	public LogLine(LogLevel level, TraceException trace, String message, Throwable cause) {
		this(level, logCount.getAndIncrement(), System.currentTimeMillis() - startTime, trace, message, cause);
	}
	
	/**
	 * Construct a LogLine with all parameters.  This should only be used in special conditions,
	 * for example to insert a log line at a specific point within normal logs.
	 * 
	 * @param level		the logging level
	 * @param count		the log line count number
	 * @param timestamp	the log line timestamp
	 * @param trace		the trace exception for the log line, <code>null</code> permitted
	 * @param message	the log message
	 * @param cause		the causing throwable, <code>null</code> permitted
	 */
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
	
	
	public String getLocation() {
		if (trace != null) {
			return trace.getLocation();
		} else {
			return "(-)";
		}
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
			String str;
			str = String.format("%4d %10.3f %-" + LogLevel.LENGTH + "s %s %s",
					count, timestamp / 1000.0, (level != null) ? level.toString() : "NULL",
					getLocation(),
					message);
			if (cause != null) {
				StackTraceWriter stw = new StackTraceWriter();
				PrintWriter pw = new PrintWriter(stw);
				cause.printStackTrace(pw);
				pw.flush();
				str = str + "\n" + stw.toString();
			}
			formattedMessage = str;
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
