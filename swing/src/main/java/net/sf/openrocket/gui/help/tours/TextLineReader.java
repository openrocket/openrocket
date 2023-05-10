package net.sf.openrocket.gui.help.tours;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.NoSuchElementException;

import net.sf.openrocket.util.BugException;

/**
 * Read from a Reader object one line at a time, ignoring blank lines,
 * preceding and trailing whitespace and comment lines starting with '#'.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class TextLineReader implements Iterator<String> {
	
	private static final Charset UTF8 = Charset.forName("UTF-8");
	


	private final BufferedReader reader;
	
	private String next = null;
	
	/**
	 * Read from an input stream with UTF-8 character encoding.
	 */
	public TextLineReader(InputStream inputStream) {
		this(new InputStreamReader(inputStream, UTF8));
	}
	
	
	/**
	 * Read from a reader.
	 */
	public TextLineReader(Reader reader) {
		if (reader instanceof BufferedReader) {
			this.reader = (BufferedReader) reader;
		} else {
			this.reader = new BufferedReader(reader);
		}
	}
	
	
	/**
	 * Test whether the file has more lines available.
	 */
	@Override
	public boolean hasNext() {
		if (next != null) {
			return true;
		}
		
		try {
			next = readLine();
		} catch (IOException e) {
			throw new BugException(e);
		}
		
		return next != null;
	}
	
	
	/**
	 * Retrieve the next non-blank, non-comment line.
	 */
	@Override
	public String next() {
		if (hasNext()) {
			String ret = next;
			next = null;
			return ret;
		}
		
		throw new NoSuchElementException("End of file reached");
	}
	
	
	/**
	 * Peek what the next line would be.
	 */
	public String peek() {
		if (hasNext()) {
			return next;
		}
		
		throw new NoSuchElementException("End of file reached");
	}
	
	
	private String readLine() throws IOException {
		
		while (true) {
			// Read the next line
			String line = reader.readLine();
			if (line == null) {
				return null;
			}
			
			// Check whether to accept the line
			line = line.trim();
			if (line.length() > 0 && line.charAt(0) != '#') {
				return line;
			}
		}
		
	}
	
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException("Remove not supported");
	}
	
}
