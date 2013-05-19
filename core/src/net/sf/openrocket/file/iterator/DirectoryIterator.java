package net.sf.openrocket.file.iterator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.util.JarUtil;
import net.sf.openrocket.util.Pair;

/**
 * A DirectoryIterator that scans for files within a directory in the file system
 * matching a FileFilter.  The scan is optionally recursive.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class DirectoryIterator extends FileIterator {
	
	private static final Logger logger = LoggerFactory.getLogger(DirectoryIterator.class);
	
	private final FileFilter filter;
	private final File[] files;
	private final boolean recursive;
	private int position = 0;
	private DirectoryIterator subIterator = null;
	
	/**
	 * Sole constructor.
	 * 
	 * @param directory		the directory to read.
	 * @param filter		the filter for selecting files.
	 * @throws IOException	if the directory cannot be read.
	 */
	public DirectoryIterator(File directory, FileFilter filter, boolean recursive)
			throws IOException {
		
		this.filter = filter;
		this.recursive = recursive;
		
		this.files = directory.listFiles(new DirSelectionFileFilter(filter, recursive));
		if (this.files == null) {
			throw new IOException("not a directory or IOException occurred when listing files " +
					"from " + directory);
		}
	}
	
	



	@Override
	protected Pair<String, InputStream> findNext() {
		
		// Check if we're recursing
		if (subIterator != null) {
			if (subIterator.hasNext()) {
				return subIterator.next();
			} else {
				subIterator.close();
				subIterator = null;
			}
		}
		
		// Scan through file entries
		while (position < files.length) {
			File file = files[position];
			position++;
			
			try {
				if (recursive && file.isDirectory()) {
					subIterator = new DirectoryIterator(file, filter, recursive);
					if (subIterator.hasNext()) {
						return subIterator.next();
					} else {
						subIterator.close();
						subIterator = null;
						continue;
					}
				}
				
				InputStream is = new BufferedInputStream(new FileInputStream(file));
				return new Pair<String, InputStream>(file.getName(), is);
			} catch (IOException e) {
				logger.warn("Error opening file/directory " + file, e);
			}
		}
		return null;
	}
	
	

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
	public static FileIterator findDirectory(String directory, FileFilter filter) {
		FileIterator iterator = null;
		
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
				iterator = new DirectoryIterator(dir, filter, true);
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
			iterator = new DirectoryIterator(new File(directory), filter, true);
			if (iterator.hasNext()) {
				return iterator;
			}
			iterator.close();
		} catch (IOException e) {
			logger.error("Error opening directory " + directory);
		}
		
		return null;
	}
	
	

	/**
	 * A FileFilter wrapper that accepts or discards directories.
	 */
	private class DirSelectionFileFilter implements FileFilter {
		
		private final boolean acceptDirs;
		private final FileFilter parentFilter;
		
		
		public DirSelectionFileFilter(FileFilter filter, boolean acceptDirs) {
			this.acceptDirs = acceptDirs;
			this.parentFilter = filter;
		}
		
		
		@Override
		public boolean accept(File pathname) {
			if (pathname.getName().startsWith(".")) {
				return false;
			}
			if (pathname.isDirectory()) {
				return acceptDirs;
			}
			return parentFilter.accept(pathname);
		}
		
	}
	
}
