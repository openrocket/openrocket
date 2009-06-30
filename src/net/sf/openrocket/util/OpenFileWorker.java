package net.sf.openrocket.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

import javax.swing.SwingWorker;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.GeneralRocketLoader;
import net.sf.openrocket.file.RocketLoader;


/**
 * A SwingWorker thread that opens a rocket design file.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class OpenFileWorker extends SwingWorker<OpenRocketDocument, Void> {

	private static final RocketLoader ROCKET_LOADER = new GeneralRocketLoader();

	private final File file;
	
	public OpenFileWorker(File file) {
		this.file = file;
	}
	
	
	@Override
	protected OpenRocketDocument doInBackground() throws Exception {
		ProgressInputStream is = new ProgressInputStream(
				new BufferedInputStream(new FileInputStream(file)));
		try {
			return ROCKET_LOADER.load(is);
		} finally {
			try {
				is.close();
			} catch (Exception e) {
				System.err.println("Error closing file: ");
				e.printStackTrace();
			}
		}
	}

	
	

	private class ProgressInputStream extends FilterInputStream {

		private final int size;
		private int readBytes = 0;
		private int progress = -1;
		
		protected ProgressInputStream(InputStream in) {
			super(in);
			int s;
			try {
				s = in.available();
			} catch (IOException e) {
				System.err.println("ERROR estimating available bytes!");
				s = 0;
			}
			size = s;
		}

		
		
		@Override
		public int read() throws IOException {
			int c = in.read();
			if (c >= 0) {
				readBytes++;
				setProgress();
			}
			if (isCancelled()) {
				throw new InterruptedIOException("OpenFileWorker was cancelled");
			}
			return c;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			int n = in.read(b, off, len);
			if (n > 0) {
				readBytes += n;
				setProgress();
			}
			if (isCancelled()) {
				throw new InterruptedIOException("OpenFileWorker was cancelled");
			}
			return n;
		}

		@Override
		public int read(byte[] b) throws IOException {
			int n = in.read(b);
			if (n > 0) {
				readBytes += n;
				setProgress();
			}
			if (isCancelled()) {
				throw new InterruptedIOException("OpenFileWorker was cancelled");
			}
			return n;
		}

		@Override
		public long skip(long n) throws IOException {
			long nr = in.skip(n);
			if (nr > 0) {
				readBytes += nr;
				setProgress();
			}
			if (isCancelled()) {
				throw new InterruptedIOException("OpenFileWorker was cancelled");
			}
			return nr;
		}
		
		@Override
		public synchronized void reset() throws IOException {
			in.reset();
			readBytes = size - in.available();
			setProgress();
			if (isCancelled()) {
				throw new InterruptedIOException("OpenFileWorker was cancelled");
			}
		}

		
		
		private void setProgress() {
			int p = MathUtil.clamp(readBytes * 100 / size, 0, 100);
			if (progress != p) {
				progress = p;
				OpenFileWorker.this.setProgress(progress);
			}
		}
	}
}
