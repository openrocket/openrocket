package net.sf.openrocket.gui.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.SwingWorker;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.RocketSaver;
import net.sf.openrocket.startup.Application;

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
		
		int estimate = (int)saver.estimateFileSize(document, 
				document.getDefaultStorageOptions());
		
		// Create the ProgressOutputStream that provides progress estimates
		ProgressOutputStream os = new ProgressOutputStream(
				new BufferedOutputStream(new FileOutputStream(file)), 
				estimate, this) {
			
			@Override
			protected void setProgress(int progress) {
				SaveFileWorker.this.setProgress(progress);
			}
			
		};
		
		String rawFilename = file.getName();
		
		try {
			saver.save(rawFilename, os, document);
		} finally {
			try {
				os.close();
			} catch (Exception e) {
				Application.getExceptionHandler().handleErrorCondition("Error closing file", e);
			}
		}
		return null;
	}
	
}
