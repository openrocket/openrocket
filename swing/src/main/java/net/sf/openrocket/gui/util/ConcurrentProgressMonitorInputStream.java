package net.sf.openrocket.gui.util;

import java.awt.Component;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;



/**
 * A functional equivalent of <code>ProgressMonitorInputStream</code> which 
 * uses {@link ConcurrentProgressMonitor} and leaves the progress dialog open
 * to be manually closed later on.
 */

public class ConcurrentProgressMonitorInputStream extends FilterInputStream {
	private ConcurrentProgressMonitor monitor;
	private int nread = 0;
	private int size = 0;


	/**
	 * Constructs an object to monitor the progress of an input stream.
	 *
	 * @param message Descriptive text to be placed in the dialog box
	 *                if one is popped up.
	 * @param parentComponent The component triggering the operation
	 *                        being monitored.
	 * @param in The input stream to be monitored.
	 */
	public ConcurrentProgressMonitorInputStream(Component parentComponent,
			Object message, InputStream in) {
		super(in);
		try {
			size = in.available();
		} catch (IOException ioe) {
			size = 0;
		}
		monitor = new ConcurrentProgressMonitor(parentComponent, message, null, 0,
				size + 1);
	}


	/**
	 * Get the ProgressMonitor object being used by this stream. Normally
	 * this isn't needed unless you want to do something like change the
	 * descriptive text partway through reading the file.
	 * @return the ProgressMonitor object used by this object 
	 */
	public ConcurrentProgressMonitor getProgressMonitor() {
		return monitor;
	}


	/**
	 * Overrides <code>FilterInputStream.read</code> 
	 * to update the progress monitor after the read.
	 */
	@Override
	public int read() throws IOException {
		int c = in.read();
		if (c >= 0)
			monitor.setProgress(++nread);
		if (monitor.isCanceled()) {
			InterruptedIOException exc = new InterruptedIOException("progress");
			exc.bytesTransferred = nread;
			throw exc;
		}
		return c;
	}


	/**
	 * Overrides <code>FilterInputStream.read</code> 
	 * to update the progress monitor after the read.
	 */
	@Override
	public int read(byte b[]) throws IOException {
		int nr = in.read(b);
		if (nr > 0)
			monitor.setProgress(nread += nr);
		if (monitor.isCanceled()) {
			InterruptedIOException exc = new InterruptedIOException("progress");
			exc.bytesTransferred = nread;
			throw exc;
		}
		return nr;
	}


	/**
	 * Overrides <code>FilterInputStream.read</code> 
	 * to update the progress monitor after the read.
	 */
	@Override
	public int read(byte b[], int off, int len) throws IOException {
		int nr = in.read(b, off, len);
		if (nr > 0)
			monitor.setProgress(nread += nr);
		if (monitor.isCanceled()) {
			InterruptedIOException exc = new InterruptedIOException("progress");
			exc.bytesTransferred = nread;
			throw exc;
		}
		return nr;
	}


	/**
	 * Overrides <code>FilterInputStream.skip</code> 
	 * to update the progress monitor after the skip.
	 */
	@Override
	public long skip(long n) throws IOException {
		long nr = in.skip(n);
		if (nr > 0)
			monitor.setProgress(nread += nr);
		return nr;
	}


	/**
	 * Overrides <code>FilterInputStream.close</code> 
	 * to close the progress monitor as well as the stream.
	 */
	@Override
	public void close() throws IOException {
		in.close();
        monitor.close();
	}


	/**
	 * Overrides <code>FilterInputStream.reset</code> 
	 * to reset the progress monitor as well as the stream.
	 */
	@Override
	public synchronized void reset() throws IOException {
		in.reset();
		nread = size - in.available();
		monitor.setProgress(nread);
	}
}
