package net.sf.openrocket.logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

/**
 * A logger that buffers a specific number of log lines from every log
 * level.  This prevents a multitude of lower-level lines from purging
 * away important higher-level messages.  The method {@link #getLogs()}
 * combines these logs into their original (natural) order.  A log line 
 * is also inserted stating the number of log lines of the particular
 * level that have been purged from the buffer.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class LogLevelBufferLogger extends LogHelper {
	
	private final EnumMap<LogLevel, BufferLogger> loggers =
			new EnumMap<LogLevel, BufferLogger>(LogLevel.class);
	
	
	/**
	 * Sole constructor.
	 * @param count		the number of log lines from each level to buffer.
	 */
	public LogLevelBufferLogger(int count) {
		for (LogLevel level : LogLevel.values()) {
			loggers.put(level, new BufferLogger(count));
		}
	}
	
	@Override
	public void log(LogLine line) {
		// Delegate to the buffered logger of this level
		loggers.get(line.getLevel()).log(line);
	}
	
	
	/**
	 * Retrieve all buffered log lines in order.  A log line is also added to indicate the number of
	 * log lines that have been purged for that log level.
	 * 
	 * @return	a list of log lines in order.
	 */
	public List<LogLine> getLogs() {
		List<LogLine> result = new ArrayList<LogLine>();
		
		for (LogLevel level : LogLevel.values()) {
			BufferLogger logger = loggers.get(level);
			List<LogLine> logs = logger.getLogs();
			int misses = logger.getOverwriteCount();
			
			if (misses > 0) {
				if (logs.isEmpty()) {
					result.add(new LogLine(level, 0, 0, null,
							"===== " + misses + " " + level + " lines removed but log is empty! =====",
							null));
				} else {
					result.add(new LogLine(level, logs.get(0).getLogCount(), 0, null,
							"===== " + misses + " " + level + " lines removed =====", null));
				}
			}
			result.addAll(logs);
		}
		
		Collections.sort(result);
		return result;
	}
	

}
