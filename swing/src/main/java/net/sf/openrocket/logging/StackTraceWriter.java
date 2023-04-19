package net.sf.openrocket.logging;

import java.io.IOException;
import java.io.Writer;

public class StackTraceWriter extends Writer {
	
	public static final String PREFIX = "   > ";
	
	private final StringBuilder buffer = new StringBuilder();
	private boolean addPrefix = true;

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		for (int i=0; i<len; i++) {
			if (addPrefix) {
				buffer.append(PREFIX);
				addPrefix = false;
			}
			char c = cbuf[off+i];
			buffer.append(c);
			if (c == '\n')
				addPrefix = true;
		}
	}

	
	@Override
	public String toString() {
		if (addPrefix && buffer.length() > 0) {
			return buffer.substring(0, buffer.length()-1);
		} else {
			return buffer.toString();
		}
	}

	
	@Override
	public void close() throws IOException {
		// no-op
	}

	@Override
	public void flush() throws IOException {
		// no-op
	}

}
