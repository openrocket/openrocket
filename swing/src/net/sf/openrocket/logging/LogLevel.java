package net.sf.openrocket.logging;

import java.util.Locale;

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
	 * Level for indicating error conditions or atypical events that can occur during
	 * normal operation (errors while loading files, weird computation results etc).
	 */
	WARN,
	
	/** 
	 * Level for logging user actions (adding and modifying components, running
	 * simulations etc).  A user action should be logged as soon as possible on this
	 * level.  The level is separate so that additional INFO messages won't purge
	 * user actions from a bounded log buffer.
	 */
	USER,
	
	/**
	 * Level for indicating general level actions the software is performing and
	 * other notable events during execution (dialogs shown, simulations run etc).
	 */
	INFO,
	
	/**
	 * Level for indicating mid-results, outcomes of methods and other debugging 
	 * information.  The data logged should be of value when analyzing error
	 * conditions and what has caused them.  Places that are called repeatedly
	 * during e.g. flight simulation should use the VBOSE level instead.
	 */
	DEBUG,
	
	/**
	 * Level at which redirected StdErr messages are logged
	 */
	STDERR,
	
	/**
	 * Level of verbose debug logging to be used in areas which are called repeatedly,
	 * such as computational methods used in simulations.  This level is separated to
	 * allow filtering out the verbose logs generated during simulations, DnD etc.
	 * from the normal debug logs.
	 */
	VBOSE;
	
	/** The log level with highest priority */
	public static final LogLevel HIGHEST;
	/** The log level with lowest priority */
	public static final LogLevel LOWEST;
	/** The maximum length of a level textual description */
	public static final int LENGTH;
	
	static {
		int length = 0;
		for (LogLevel l : LogLevel.values()) {
			length = Math.max(length, l.toString().length());
		}
		LENGTH = length;
		
		LogLevel[] values = LogLevel.values();
		HIGHEST = values[0];
		LOWEST = values[values.length - 1];
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
	
	
	/**
	 * Return a log level corresponding to a string.  The string is case-insensitive.  If the
	 * string is case-insensitively equal to "all", then the lowest logging level is returned.
	 * 
	 * @param value			the string name of a log level, or "all"
	 * @param defaultLevel	the value to return if the string doesn't correspond to any log level or is null
	 * @return				the corresponding log level, of defaultLevel.
	 */
	public static LogLevel fromString(String value, LogLevel defaultLevel) {
		
		// Normalize the string
		if (value == null) {
			return defaultLevel;
		}
		value = value.toUpperCase(Locale.ENGLISH).trim();
		
		// Find the correct level
		LogLevel level = defaultLevel;
		if (value.equals("ALL")) {
			LogLevel[] values = LogLevel.values();
			level = values[values.length - 1];
		} else {
			try {
				level = LogLevel.valueOf(value);
			} catch (Exception e) {
				// Ignore
			}
		}
		return level;
	}
	
}
