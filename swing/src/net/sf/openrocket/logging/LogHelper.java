package net.sf.openrocket.logging;



/**
 * Base class for all loggers used in OpenRocket.
 * <p>
 * This class contains methods for logging at various log levels, and methods
 * which take the logging level as a parameter.  All methods may take three types
 * of parameters:
 * <ul>
 * 	<li><code>levels</code>		number of additional levels of the stack trace to print
 * 								on the log line.  This is useful to determine from where
 * 								the current method has been called.  Zero if not provided.
 *  <li><code>message</code>	the String message (may be null).
 *  <li><code>cause</code>		the exception that caused this log (may be null).
 * </ul>
 * <p>
 * The logging methods are guaranteed never to throw an exception, and can thus be safely
 * used in finally blocks.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class LogHelper {
	
	/**
	 * Log a LogLine object.  This method needs to be able to cope with multiple threads
	 * accessing it concurrently (for example by being synchronized).
	 * 
	 * @param line	the LogLine to log.
	 */
	public abstract void log(LogLine line);
	
}
