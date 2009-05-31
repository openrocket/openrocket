package net.sf.openrocket.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import net.sf.openrocket.file.Loader;
import net.sf.openrocket.util.ChangeSource;



/**
 * A database set.  This class functions as a <code>Set</code> that contains items
 * of a specific type.  Additionally, the items can be accessed via an index number.
 * The elements are always kept in their natural order.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

// TODO: HIGH: Database saving
public class Database<T extends Comparable<T>> extends AbstractSet<T> implements ChangeSource {

	private final List<T> list = new ArrayList<T>();
	private final EventListenerList listenerList = new EventListenerList();
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
			index = -(index+1);
		}
		list.add(index,element);
		fireChangeEvent();
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
	

	@Override
	public void addChangeListener(ChangeListener listener) {
		listenerList .add(ChangeListener.class, listener);
	}


	@Override
	public void removeChangeListener(ChangeListener listener) {
		listenerList .remove(ChangeListener.class, listener);
	}

	
	protected void fireChangeEvent() {
		Object[] listeners = listenerList.getListenerList();
		ChangeEvent e = null;
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==ChangeListener.class) {
				// Lazily create the event:
				if (e == null)
					e = new ChangeEvent(this);
				((ChangeListener)listeners[i+1]).stateChanged(e);
			}
		}
	}
	

	
	////////  Directory loading
	
	
	
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
			public boolean accept(File dir, String name) {
				return name.matches(pattern);
			}
		});
		if (files == null) {
			throw new IOException("not a directory: "+dir);
		}
		for (File file: files) {
			try {
				this.addAll(loader.load(new FileInputStream(file), file.getName()));
			} catch (IOException e) {
				System.err.println("Error loading file "+file+": " + e.getMessage());
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

		// Find the jar file this class is contained in and open it
		URL jarUrl = null;
		CodeSource codeSource = Database.class.getProtectionDomain().getCodeSource();
		if (codeSource != null)
			jarUrl = codeSource.getLocation();
		
		if (jarUrl == null) {
			throw new IOException("Could not find containing JAR file.");
		}
		File file = urlToFile(jarUrl);
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
						System.err.println("Error loading file " + file + ": "
								+ e.getMessage());
					}
				}
			}

		} finally {
			jarFile.close();
		}
	}
	
	
	static File urlToFile(URL url) {
		URI uri;
		try {
			uri = url.toURI();
		} catch (URISyntaxException e) {
			try {
				uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), 
						url.getPath(), url.getQuery(), url.getRef());
			} catch (URISyntaxException e1) {
				throw new IllegalArgumentException("Broken URL: " + url);
			}
		}
		return new File(uri);
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
		
		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public T next() {
			return iterator.next();
		}

		@Override
		public void remove() {
			iterator.remove();
			fireChangeEvent();
		}
	}
}
