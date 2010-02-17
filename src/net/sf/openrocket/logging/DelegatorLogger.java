package net.sf.openrocket.logging;

import java.util.ArrayList;
import java.util.List;

/**
 * A logger implementation that delegates logging to other logger implementations.
 * Multiple loggers can be added to the delegator, all of which will receive
 * all of the log lines.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class DelegatorLogger extends LogHelper {

	private List<LogHelper> loggers = new ArrayList<LogHelper>();
	
	@Override
	public void log(LogLine line) {
		LogHelper[] array = loggers.toArray(new LogHelper[0]);
		for (LogHelper l: array) {
			l.log(line);
		}
	}
	
	
	public void addLogger(LogHelper logger) {
		this.loggers.add(logger);
	}
	
	public void removeLogger(LogHelper logger) {
		this.loggers.remove(logger);
	}

}
