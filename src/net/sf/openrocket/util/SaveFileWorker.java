package net.sf.openrocket.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;

import javax.swing.SwingWorker;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.RocketSaver;

public class SaveFileWorker extends SwingWorker<Void, Void> {

	private final OpenRocketDocument document;
	private final File file;
	private final RocketSaver saver;
	
	public SaveFileWorker(OpenRocketDocument document, File file, RocketSaver saver) {
		this.document = document;
		this.file = file;
		this.saver = saver;
	}
	
	
	@Override
	protected Void doInBackground() throws Exception {
		ProgressOutputStream os = new ProgressOutputStream(
				new BufferedOutputStream(new FileOutputStream(file)), 
				(int)saver.estimateFileSize(document, document.getDefaultStorageOptions()));
		
		try {
			saver.save(os, document);
		} finally {
			try {
				os.close();
			} catch (Exception e) {
				System.err.println("Error closing file: ");
				e.printStackTrace();
			}
		}
		return null;
	}
	
	
	private class ProgressOutputStream extends FilterOutputStream {

		private final int totalBytes;
		private int writtenBytes = 0;
		private int progress = -1;
		
		public ProgressOutputStream(OutputStream out, int estimate) {
			super(out);
			this.totalBytes = estimate;
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			out.write(b, off, len);
			writtenBytes += len;
			setProgress();
			if (isCancelled()) {
				throw new InterruptedIOException("SaveFileWorker was cancelled");
			}
		}

		@Override
		public void write(byte[] b) throws IOException {
			out.write(b);
			writtenBytes += b.length;
			setProgress();
			if (isCancelled()) {
				throw new InterruptedIOException("SaveFileWorker was cancelled");
			}
		}

		@Override
		public void write(int b) throws IOException {
			out.write(b);
			writtenBytes++;
			setProgress();
			if (isCancelled()) {
				throw new InterruptedIOException("SaveFileWorker was cancelled");
			}
		}
		
		
		private void setProgress() {
			int p = MathUtil.clamp(writtenBytes * 100 / totalBytes, 0, 100);
			if (progress != p) {
				progress = p;
				SaveFileWorker.this.setProgress(progress);
			}
		}
	}
}
