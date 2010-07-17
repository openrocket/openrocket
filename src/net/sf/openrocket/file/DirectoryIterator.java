package net.sf.openrocket.file;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;

import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.JarUtil;
import net.sf.openrocket.util.Pair;

public abstract class DirectoryIterator implements Iterator<Pair<String, InputStream>> {

	private static final LogHelper logger = Application.getLogger();

	private Pair<String, InputStream> next = null;

	@Override
	public boolean hasNext() {
		if (next != null)
			return true;
		
		next = findNext();
		return (next != null);
	}
	

	@Override
	public Pair<String, InputStream> next() {
		if (next == null) {
			next = findNext();
		}
		if (next == null) {
			throw new NoSuchElementException("No more files");
		}
		
		Pair<String, InputStream> n = next;
		next = null;
		return n;
	}
	

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove() not supported");
	}
	

	
	/**
	 * Closes the resources related to this iterator.  This method should be
	 * overridden if the iterator needs to close any resources of its own, but
	 * must call this method as well.
	 */
	public void close() {
		if (next != null) {
			try {
				next.getV().close();
			} catch (IOException e) {
				logger.error("Error closing file " + next.getU());
			}
			next = null;
		}
	}
	
	/**
	 * Return the next pair of file name and InputStream.
	 * 
	 * @return	a pair with the file name and input stream reading the file.
	 */
	protected abstract Pair<String, InputStream> findNext();
	
	
	
	/**
	 * Return a DirectoryIterator for a directory that can be located either
	 * within the containing JAR file, in the classpath or in the current directory
	 * (searched in this order).  The first place that contains matching files
	 * will be iterated through.
	 * 
	 * @param directory		the directory to search for.
	 * @param filter		the filter for matching files in the directory.
	 * @return				a DirectoryIterator for iterating through the files in the
	 * 						directory, or <code>null</code> if no directory containing
	 * 						matching files can be found.
	 */
	public static DirectoryIterator findDirectory(String directory, FileFilter filter) {
		DirectoryIterator iterator = null;
		
		// Try to load from containing JAR file
		File jarFile = JarUtil.getCurrentJarFile();
		if (jarFile != null) {
			try {
				iterator = new ZipDirectoryIterator(jarFile, directory, filter);
				if (iterator.hasNext()) {
					return iterator;
				}
				iterator.close();
			} catch (IOException e) {
				logger.error("Error opening containing JAR file " + jarFile, e);
			}
		}
		

		// Try to find directory as a system resource
		URL url = ClassLoader.getSystemResource(directory);
		if (url != null) {
			try {
				File dir = JarUtil.urlToFile(url);
				iterator = new RegularDirectoryIterator(dir, filter);
				if (iterator.hasNext()) {
					return iterator;
				}
				iterator.close();
			} catch (Exception e1) {
				logger.error("Error opening directory from URL " + url);
			}
		}
		
		
		// Try to open directory as such
		try {
			iterator = new RegularDirectoryIterator(new File(directory), filter);
			if (iterator.hasNext()) {
				return iterator;
			}
			iterator.close();
		} catch (IOException e) {
			logger.error("Error opening directory " + directory);
		}
			
		return null;
	}
	
}
