package net.sf.openrocket.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Pair;

/**
 * A DirectoryIterator that scans for files within a directory in the file system.
 * 
 * TODO: MEDIUM: Does not support recursive search.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class RegularDirectoryIterator extends DirectoryIterator {

	private static final LogHelper logger = Application.getLogger();

	private final File[] files;
	private int position = 0;
	
	/**
	 * Sole constructor.
	 * 
	 * @param directory		the directory to read.
	 * @param filter		the filter for selecting files.
	 * @throws IOException	if the directory cannot be read.
	 */
	public RegularDirectoryIterator(File directory, FileFilter filter) 
			throws IOException {
		this.files = directory.listFiles(filter);
		if (this.files == null) {
			throw new IOException("not a directory or IOException occurred when listing files " +
					"from " + directory);
		}
	}
	
	
	
	@Override
	protected Pair<String, InputStream> findNext() {
		for (; position < files.length; position++) {
			try {
				InputStream is = new BufferedInputStream(new FileInputStream(files[position]));
				position++;
				return new Pair<String, InputStream>(files[position-1].getName(), is);
			} catch (FileNotFoundException e) {
				logger.warn("Error opening file " + files[position], e);
			}
		}
		return null;
	}

}
