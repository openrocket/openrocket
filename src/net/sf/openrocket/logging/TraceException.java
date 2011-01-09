package net.sf.openrocket.logging;



/**
 * An exception that is used to store a stack trace.  On modern computers
 * instantiation of an exception takes on the order of one microsecond, while
 * examining the trace typically takes several times longer.  Therefore the
 * exception should be stored and the stack trace examined only when necessary.
 * <p>
 * The {@link #getMessage()} method returns a description of the position
 * where this exception has been instantiated.  The position is provided
 * as many levels upwards from the instantiation position as provided to the
 * constructor.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class TraceException extends Exception {
	
	private static final String STANDARD_PACKAGE_PREFIX = "net.sf.openrocket.";
	
	private final int minLevel;
	private final int maxLevel;
	private volatile String message = null;
	
	
	/**
	 * Instantiate exception that provides the line of instantiation as a message.
	 */
	public TraceException() {
		this(0, 0);
	}
	
	/**
	 * Instantiate exception that provides the provided number of levels upward
	 * from the instantiation location as a message.  The level provided 
	 * is how many levels upward should be examined to find the stack trace 
	 * position for the exception message.
	 * 
	 * @param level		how many levels upward to examine the stack trace to find
	 * 					the correct message.
	 */
	public TraceException(int level) {
		this(level, level);
	}
	
	
	/**
	 * Instantiate exception that provides a range of levels upward from the
	 * instantiation location as a message.  This is useful to identify the
	 * next level of callers upward.
	 * 
	 * @param minLevel	the first level which to include.
	 * @param maxLevel	the last level which to include.
	 */
	public TraceException(int minLevel, int maxLevel) {
		if (minLevel > maxLevel || minLevel < 0) {
			throw new IllegalArgumentException("minLevel=" + minLevel + " maxLevel=" + maxLevel);
		}
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
	}
	
	
	/**
	 * Construct an exception with the specified message.
	 * 
	 * @param message	the message for the exception.
	 */
	public TraceException(String message) {
		this(0, 0);
		this.message = message;
	}
	
	
	/**
	 * Construct an exception with the specified message and cause.
	 * 
	 * @param message	the message for the exception.
	 * @param cause		the cause for this exception.
	 */
	public TraceException(String message, Throwable cause) {
		this(0, 0);
		this.message = message;
		this.initCause(cause);
	}
	
	
	/**
	 * Get the description of the code position as provided in the constructor.
	 */
	@Override
	public String getMessage() {
		if (message == null) {
			StackTraceElement[] elements = this.getStackTrace();
			
			StringBuilder sb = new StringBuilder();
			if (minLevel < elements.length) {
				
				sb.append("(");
				sb.append(toString(elements[minLevel]));
				for (int i = minLevel + 1; i <= maxLevel; i++) {
					if (i < elements.length) {
						sb.append(' ').append(toString(elements[i]));
					}
				}
				sb.append(')');
				
			} else if (elements.length == 0) {
				
				sb.append("(no stack trace)");
				
			} else {
				
				sb.append('(');
				sb.append(toString(elements[0]));
				for (int i = 1; i < elements.length; i++) {
					sb.append(' ').append(toString(elements[i]));
				}
				sb.append(" level=").append(minLevel).append(')');
				
			}
			message = sb.toString();
		}
		return message;
	}
	
	
	private static String toString(StackTraceElement element) {
		if (element.getClassName().startsWith(STANDARD_PACKAGE_PREFIX)) {
			return element.getFileName() + ":" + element.getLineNumber();
		} else {
			return element.toString();
		}
	}
	
}
