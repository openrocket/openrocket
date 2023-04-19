package net.sf.openrocket.logging;

import java.util.List;

import net.sf.openrocket.util.ArrayList;

/**
 * A logger implementation that delegates logging to other logger implementations.
 * Multiple loggers can be added to the delegator, all of which will receive
 * all of the log lines.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class DelegatorLogger extends LogHelper {
	
	/**
	 * List of loggers.  This list must not be modified, instead it should be
	 * replaced every time the list is changed.
	 */
	private volatile ArrayList<LogHelper> loggers = new ArrayList<LogHelper>();
	
	@Override
	public void log(LogLine line) {
		// Must create local reference for thread safety
		List<LogHelper> list = loggers;
		for (LogHelper l : list) {
			l.log(line);
		}
	}
	
	
	/**
	 * Add a logger from the delegation list.
	 * @param logger	the logger to add.
	 */
	public synchronized void addLogger(LogHelper logger) {
		ArrayList<LogHelper> newList = loggers.clone();
		newList.add(logger);
		this.loggers = newList;
	}
	
	/**
	 * Remove a logger from the delegation list.
	 * @param logger	the logger to be removed.
	 */
	public synchronized void removeLogger(LogHelper logger) {
		ArrayList<LogHelper> newList = loggers.clone();
		newList.remove(logger);
		this.loggers = newList;
	}
	
}
