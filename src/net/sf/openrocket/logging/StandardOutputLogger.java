package net.sf.openrocket.logging;

import java.io.PrintStream;

public class StandardOutputLogger extends LogHelper {

	private static final PrintStream[] output;
	static {
		LogLevel[] levels = LogLevel.values();
		
		output = new PrintStream[levels.length];
		for (int i=0; i<levels.length; i++) {
			if (levels[i].atLeast(LogLevel.WARN))
				output[i] = System.err;
			else
				output[i] = System.out;
		}
	}
	
	
	@Override
	public void log(LogLine line) {
		PrintStream stream = output[line.getLevel().ordinal()];
		if (stream != null) {
			stream.println(line.toString());
		}
	}

	
	public void setOutput(LogLevel level, PrintStream stream) {
		if (level == null || stream == null) {
			throw new IllegalArgumentException("level="+level+" stream="+stream);
		}
		output[level.ordinal()] = stream;
	}
	
}
