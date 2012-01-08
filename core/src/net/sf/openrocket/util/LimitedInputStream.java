package net.sf.openrocket.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A filtering InputStream that limits the number of bytes that can be
 * read from a stream.   This can be used to enforce security, so that overlong
 * input is ignored.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class LimitedInputStream extends FilterInputStream {

	private int remaining;
	
	public LimitedInputStream(InputStream is, int limit) {
		super(is);
		this.remaining = limit;
	}

	
	@Override
	public int available() throws IOException {
		int available = super.available();
		return Math.min(available, remaining);
	}


	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (remaining <= 0)
			return -1;
		
		int result = super.read(b, off, Math.min(len, remaining));
		if (result >= 0)
			remaining -= result;
		return result;
	}


	@Override
	public long skip(long n) throws IOException {
		if (n > remaining)
			n = remaining;
		long result = super.skip(n);
		remaining -= result;
		return result;
	}


	@Override
	public int read() throws IOException {
		if (remaining <= 0)
			return -1;
		
		int result = super.read();
		if (result >= 0)
			remaining--;
		return result;
	}

	
	
	//  Disable mark support

	@Override
	public void mark(int readlimit) {

	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public synchronized void reset() throws IOException {
		throw new IOException("mark/reset not supported");
	}
	
}
