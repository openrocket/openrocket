package net.sf.openrocket.logging;

import java.util.EnumMap;
import java.util.List;

/**
 * A logger implementation that buffers specific levels of log lines.
 * The levels that are logged are set using the method
 * {@link #setStoreLevel(LogLevel, boolean)}.  The stored LogLines can
 * be obtained using {@link #getLogs()}.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class BufferLogger extends LogHelper {

	private final CyclicBuffer<LogLine> buffer;
	private final EnumMap<LogLevel, Boolean> storeLevels = 
		new EnumMap<LogLevel, Boolean>(LogLevel.class);
	
	
	/**
	 * Create a buffered logger with that logs the specified number of log 
	 * lines.  By default all log levels are buffered.
	 * 
	 * @param length	the length of the buffer.
	 */
	public BufferLogger(int length) {
		for (LogLevel l: LogLevel.values()) {
			storeLevels.put(l, true);
		}
		buffer = new CyclicBuffer<LogLine>(length);
	}
	
	
	@Override
	public void log(LogLine line) {
		if (storeLevels.get(line.getLevel())) {
			buffer.add(line);
		}
	}

	/**
	 * Set whether the specified log level is buffered.
	 * 
	 * @param level		the log level.
	 * @param store		whether to store the level.
	 */
	public void setStoreLevel(LogLevel level, boolean store) {
		storeLevels.put(level, store);
	}
	
	/**
	 * Get whether the specified log level is buffered.
	 * 
	 * @param level		the log level.
	 * @return			whether the log level is stored.
	 */
	public boolean getStoreLevel(LogLevel level) {
		return storeLevels.get(level);
	}

	
	/**
	 * Return all the buffered log lines.
	 * 
	 * @return	a list of all buffered log lines.
	 */
	public List<LogLine> getLogs() {
		return buffer.asList();
	}
	
	/**
	 * Return the number of log lines that has been overwritten.
	 * 
	 * @return	the number of log lines missed.
	 */
	public int getOverwriteCount() {
		return buffer.getOverwriteCount();
	}
}
