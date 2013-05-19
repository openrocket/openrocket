package net.sf.openrocket.file.iterator;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.util.Pair;

/**
 * A DirectoryIterator that reads files from the specified directory of a
 * ZIP (or JAR) file.
 * 
 * TODO: MEDIUM: This is always a recursive search.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ZipDirectoryIterator extends FileIterator {
	
	private static final Logger logger = LoggerFactory.getLogger(ZipDirectoryIterator.class);

	private final File zipFileName;
	private final String directory;
	private final FileFilter filter;
	
	private ZipFile zipFile;
	private Enumeration<? extends ZipEntry> entries;
	
	
	/**
	 * Sole constructor.
	 * 
	 * @param zipFileName	the ZIP file to read.
	 * @param directory		the directory within the ZIP file to read, relative to the
	 * 						base (an empty string corresponds to the root directory)
	 * @param filter		the filter for accepted files.
	 * @throws IOException	if the ZIP file could not be read.
	 */
	public ZipDirectoryIterator(File zipFileName, String directory, FileFilter filter) 
			throws IOException {

		// Process directory and extension
		if (!directory.endsWith("/")) {
			directory += "/";
		}
		
		this.zipFileName = zipFileName;
		this.directory = directory;
		this.filter = filter;

		
		// Loop through ZIP entries searching for files to load
		this.zipFile = new ZipFile(zipFileName);
		entries = zipFile.entries();

	}
	

	@Override
	public void close() {
		super.close();
		if (zipFile != null) {
			try {
				zipFile.close();
			} catch (IOException e) {
				logger.error("Closing ZIP file failed", e);
			}
			zipFile = null;
			entries = null;
		}
	}
	
	
	@Override
	protected Pair<String, InputStream> findNext() {
		if (entries == null) {
			return null;
		}

		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			String name = entry.getName();
			File file = new File(name);
			if (name.startsWith(directory) && filter.accept(file)) {
				try {
					InputStream is = zipFile.getInputStream(entry);
					return new Pair<String, InputStream>(name, is);
				} catch (IOException e) {
					logger.error("IOException when reading ZIP file " + zipFileName, e);
				}
			}
		}
		
		return null;
	}


}
