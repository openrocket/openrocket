package net.sf.openrocket.file;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class FileInfo {

	public final URL fileURL;
	public final File directory;
	
	public FileInfo(File sourceFile) throws MalformedURLException {
		this.fileURL = sourceFile.toURI().toURL();
		this.directory = sourceFile.getParentFile();
	}
	
	public FileInfo(URL sourceURL) {
		this.fileURL = sourceURL;
		this.directory = null;
	}

	public URL getFileURL() {
		return fileURL;
	}

	public File getDirectory() {
		return directory;
	}
	
}
