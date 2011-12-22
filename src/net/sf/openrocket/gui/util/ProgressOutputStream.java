package net.sf.openrocket.gui.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;

import javax.swing.SwingWorker;

import net.sf.openrocket.util.MathUtil;


public abstract class ProgressOutputStream extends FilterOutputStream {

	private final int totalBytes;
	private final SwingWorker<?,?> worker;
	private int writtenBytes = 0;
	private int progress = -1;
	
	public ProgressOutputStream(OutputStream out, int estimate, SwingWorker<?,?> worker) {
		super(out);
		this.totalBytes = estimate;
		this.worker = worker;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
		writtenBytes += len;
		setProgress();
		if (worker.isCancelled()) {
			throw new InterruptedIOException("SaveFileWorker was cancelled");
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		out.write(b);
		writtenBytes += b.length;
		setProgress();
		if (worker.isCancelled()) {
			throw new InterruptedIOException("SaveFileWorker was cancelled");
		}
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
		writtenBytes++;
		setProgress();
		if (worker.isCancelled()) {
			throw new InterruptedIOException("SaveFileWorker was cancelled");
		}
	}
	
	
	private void setProgress() {
		int p = MathUtil.clamp(writtenBytes * 100 / totalBytes, 0, 100);
		if (progress != p) {
			progress = p;
			setProgress(progress);
		}
	}
	
	/**
	 * Set the current progress.  The value of <code>progress</code> is guaranteed
	 * to be between 0 and 100, inclusive.
	 * 
	 * @param progress	the current progress in the range 0-100.
	 */
	protected abstract void setProgress(int progress);

}