package net.sf.openrocket.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PrintStreamToSLF4J {
	
	public static PrintStream getPrintStream(String category, PrintStream orignal) {
		
		final Logger logger = LoggerFactory.getLogger(category);
		
		return new PrintStream(new OutputStream() {
			StringBuilder currentLine = new StringBuilder();
			
			@Override
			public synchronized void write(int b) throws IOException {
				if (b == '\r' || b == '\n') {
					//Line is complete, log it
					if (currentLine.toString().trim().length() > 0) {
						String s = currentLine.toString();
						if (Character.isWhitespace(s.charAt(0))) {
							logger.trace(Markers.STDERR_MARKER, currentLine.toString());
						} else {
							logger.debug(Markers.STDERR_MARKER, currentLine.toString());
						}
					}
					currentLine = new StringBuilder();
				} else {
					//append to the line being built
					currentLine.append((char) b);
				}
			}
		});
		
	}
}
