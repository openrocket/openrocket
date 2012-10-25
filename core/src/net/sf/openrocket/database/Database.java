package net.sf.openrocket.database;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.file.Loader;
import net.sf.openrocket.file.iterator.DirectoryIterator;
import net.sf.openrocket.file.iterator.FileIterator;
import net.sf.openrocket.util.JarUtil;
import net.sf.openrocket.util.Pair;



/**
 * A database set.  This class functions as a <code>Set</code> that contains items
 * of a specific type.  Additionally, the items can be accessed via an index number.
 * The elements are always kept in their natural order.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class Database<T extends Comparable<T>> extends AbstractSet<T> {
	private static final Logger log = LoggerFactory.getLogger(Database.class);
	
	protected final List<T> list = new ArrayList<T>();
	private final ArrayList<DatabaseListener<T>> listeners =
			new ArrayList<DatabaseListener<T>>();
	private final Loader<T> loader;
	
	
	public Database() {
		loader = null;
	}
	
	public Database(Loader<T> loader) {
		this.loader = loader;
	}
	
	

	@Override
	public Iterator<T> iterator() {
		return new DBIterator();
	}
	
	@Override
	public int size() {
		return list.size();
	}
	
	@Override
	public boolean add(T element) {
		int index;
		
		index = Collections.binarySearch(list, element);
		if (index >= 0) {
			// List might contain the element
			if (list.contains(element)) {
				return false;
			}
		} else {
			index = -(index + 1);
		}
		list.add(index, element);
		fireAddEvent(element);
		return true;
	}
	
	
	/**
	 * Get the element with the specified index.
	 * @param index	the index to retrieve.
	 * @return		the element at the index.
	 */
	public T get(int index) {
		return list.get(index);
	}
	
	/**
	 * Return the index of the given <code>Motor</code>, or -1 if not in the database.
	 * 
	 * @param m   the motor
	 * @return	  the index of the motor
	 */
	public int indexOf(T m) {
		return list.indexOf(m);
	}
	
	
	public void addDatabaseListener(DatabaseListener<T> listener) {
		listeners.add(listener);
	}
	
	public void removeChangeListener(DatabaseListener<T> listener) {
		listeners.remove(listener);
	}
	
	

	@SuppressWarnings("unchecked")
	protected void fireAddEvent(T element) {
		Object[] array = listeners.toArray();
		for (Object l : array) {
			((DatabaseListener<T>) l).elementAdded(element, this);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void fireRemoveEvent(T element) {
		Object[] array = listeners.toArray();
		for (Object l : array) {
			((DatabaseListener<T>) l).elementRemoved(element, this);
		}
	}
	
	

	////////  Directory loading
	
	public void load( String dir, final String pattern ) throws IOException {
		
		FileFilter filter = new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.getName().matches(pattern);
			}
			
		};
		
		FileIterator files = DirectoryIterator.findDirectory(dir, filter);
		while( files != null && files.hasNext() ) {
			Pair<String, InputStream> file = files.next();
			try {
				this.addAll(loader.load(file.getV(), file.getU()));
			} catch (IOException e) {
				log.warn("Error loading file " + file + ": " + e.getMessage(), e);
			}
		}
		if ( files != null ) {
			files.close();
		}
	}

	/**
	 * Load all files in a directory to the motor database.  Only files with file
	 * names matching the given pattern (as matched by <code>String.matches(String)</code>)
	 * are processed.
	 * 
	 * @param dir			the directory to read.
	 * @param pattern		the pattern to match the file names to.
	 * @throws IOException	if an IO error occurs when reading the JAR archive
	 * 						(errors reading individual files are printed to stderr).
	 */
	public void loadDirectory(File dir, final String pattern) throws IOException {
		if (loader == null) {
			throw new IllegalStateException("no file loader set");
		}
		
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File directory, String name) {
				return name.matches(pattern);
			}
		});
		if (files == null) {
			throw new IOException("not a directory: " + dir);
		}
		for (File file : files) {
			try {
				this.addAll(loader.load(new FileInputStream(file), file.getName()));
			} catch (IOException e) {
				log.warn("Error loading file " + file + ": " + e.getMessage(), e);
			}
		}
	}
	
	
	/**
	 * Read all files in a directory contained in the JAR file that this class belongs to.
	 * Only files whose names match the given pattern (as matched by
	 * <code>String.matches(String)</code>) will be read.
	 * 
	 * @param dir			the directory within the JAR archive to read.
	 * @param pattern		the pattern to match the file names to.
	 * @throws IOException	if an IO error occurs when reading the JAR archive
	 * 						(errors reading individual files are printed to stderr).
	 */
	public void loadJarDirectory(String dir, String pattern) throws IOException {
		
		// Process directory and extension
		if (!dir.endsWith("/")) {
			dir += "/";
		}
		
		// Find and open the jar file this class is contained in
		File file = JarUtil.getCurrentJarFile();
		JarFile jarFile = new JarFile(file);
		
		try {
			
			// Loop through JAR entries searching for files to load
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				if (name.startsWith(dir) && name.matches(pattern)) {
					try {
						InputStream stream = jarFile.getInputStream(entry);
						this.addAll(loader.load(stream, name));
					} catch (IOException e) {
						log.warn("Error loading file " + file + ": " + e.getMessage(), e);
					}
				}
			}
			
		} finally {
			jarFile.close();
		}
	}
	
	

	public void load(File file) throws IOException {
		if (loader == null) {
			throw new IllegalStateException("no file loader set");
		}
		this.addAll(loader.load(new FileInputStream(file), file.getName()));
	}
	
	

	/**
	 * Iterator class implementation that fires changes if remove() is called.
	 */
	private class DBIterator implements Iterator<T> {
		private Iterator<T> iterator = list.iterator();
		private T current = null;
		
		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}
		
		@Override
		public T next() {
			current = iterator.next();
			return current;
		}
		
		@Override
		public void remove() {
			iterator.remove();
			fireRemoveEvent(current);
		}
	}
}
