package net.sf.openrocket.gui.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URL;

import javax.swing.SwingWorker;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.GeneralRocketLoader;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.MathUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A SwingWorker thread that opens a rocket design file.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class OpenFileWorker extends SwingWorker<OpenRocketDocument, Void> {
	private static final Logger log = LoggerFactory.getLogger(OpenFileWorker.class);
	
	private final File file;
	private final URL jarURL;
	private final GeneralRocketLoader loader;
	
	public OpenFileWorker(File file) {
		this.file = file;
		this.jarURL = null;
		loader = new GeneralRocketLoader(file);
	}
	
	
	public OpenFileWorker(URL fileURL) {
		this.jarURL = fileURL;
		this.file = null;
		loader = new GeneralRocketLoader(fileURL);
	}
	
	public GeneralRocketLoader getRocketLoader() {
		return loader;
	}
	
	@Override
	protected OpenRocketDocument doInBackground() throws Exception {
		InputStream is;
		
		// Get the correct input stream
		if (file != null) {
			is = new FileInputStream(file);
		} else {
			is = jarURL.openStream();
		}
		
		// Buffer stream unless already buffered
		if (!(is instanceof BufferedInputStream)) {
			is = new BufferedInputStream(is);
		}
		
		// Encapsulate in a ProgressInputStream
		is = new ProgressInputStream(is);
		
		try {
			OpenRocketDocument document = loader.load(is);
			
			// Set document state
			document.setFile(file);
			document.setSaved(true);
			
			return document;
		} finally {
			try {
				is.close();
			} catch (Exception e) {
				Application.getExceptionHandler().handleErrorCondition("Error closing file", e);
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
				log.info("Exception while estimating available bytes!", e);
				s = 0;
			}
			size = Math.max(s, 1);
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
