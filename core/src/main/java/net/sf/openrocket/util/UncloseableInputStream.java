package net.sf.openrocket.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An InputStream filter that prevents closing the source stream.  The
 * {@link #close()} method is overridden to do nothing.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class UncloseableInputStream extends FilterInputStream {
	
	public UncloseableInputStream(InputStream in) {
		super(in);
	}
	
	@Override
	public void close() throws IOException {
		// No-op
	}
}
