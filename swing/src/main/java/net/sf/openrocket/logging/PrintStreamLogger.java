package net.sf.openrocket.logging;

import java.io.PrintStream;
import java.util.EnumMap;

/**
 * A logger that output log lines to various print streams depending on the log level.
 * By default output is logged nowhere.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class PrintStreamLogger extends LogHelper {
	
	private final EnumMap<LogLevel, PrintStream> output = new EnumMap<LogLevel, PrintStream>(LogLevel.class);
	
	
	@Override
	public void log(LogLine line) {
		PrintStream stream = output.get(line.getLevel());
		if (stream != null) {
			stream.println(line.toString());
		}
	}
	
	public PrintStream getOutput(LogLevel level) {
		return output.get(level);
	}
	
	public void setOutput(LogLevel level, PrintStream stream) {
		if (level == null) {
			throw new IllegalArgumentException("level=" + level + " stream=" + stream);
		}
		output.put(level, stream);
	}
	
}
