package net.sf.openrocket.logging;

/**
 * The logging level.  The natural order of the LogLevel orders the levels
 * from highest priority to lowest priority.  Comparisons of the relative levels
 * should be performed using the methods {@link #atLeast(LogLevel)},
 * {@link #moreThan(LogLevel)} and {@link #compareTo(LogLevel)}.
 * <p>
 * A description of the level can be obtained using {@link #toString()}.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public enum LogLevel {
	/**
	 * Level for indicating a bug or error condition noticed in the software or JRE.
	 * No ERROR level events _should_ occur while running the program. 
	 */
	ERROR,
	/** 
	 * Level for indicating error conditions or untypical events that can occur during
	 * normal operation (errors while loading files, weird computation results etc).
	 */
	WARN,
	/** 
	 * Level for logging user actions (adding and modifying components, running
	 * simulations etc).
	 */
	USER,
	/**
	 * Level for indicating general level actions the software is performing and
	 * other notable events during execution (dialogs shown, simulations run etc).
	 */
	INFO,
	/**
	 * Level for indicating mid-results and other debugging information.  No debug
	 * logging should be performed in performance-intensive places to avoid slowing
	 * the system.  On the other hand for example cached results should be logged.
	 */
	DEBUG;

	
	/** The maximum length of a level textual description */
	public static final int LENGTH;
	static {
		int length = 0;
		for (LogLevel l: LogLevel.values()) {
			length = Math.max(length, l.toString().length());
		}
		LENGTH = length;
	}
	
	/**
	 * Return true if this log level is of a priority at least that of
	 * <code>level</code>.
	 */
	public boolean atLeast(LogLevel level) {
		return this.compareTo(level) <= 0;
	}

	/**
	 * Return true if this log level is of a priority greater than that of
	 * <code>level</code>.
	 */
	public boolean moreThan(LogLevel level) {
		return this.compareTo(level) < 0;
	}
	
}
