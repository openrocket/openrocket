package net.sf.openrocket.file.iterator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.util.Pair;

/**
 * An abstract class for iterating over files fulfilling some condition.  The files are
 * returned as pairs of open InputStreams and file names.  Conditions can be for example
 * files in a directory matching a specific FileFilter.
 * <p>
 * Concrete implementations must implement the method {@link #findNext()} and possibly
 * {@link #close()}.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class FileIterator implements Iterator<Pair<File, InputStream>> {
	private static final Logger logger = LoggerFactory.getLogger(FileIterator.class);
	
	private Pair<File, InputStream> next = null;
	private int fileCount = 0;
	
	@Override
	public boolean hasNext() {
		if (next != null)
			return true;
		
		next = findNext();
		return (next != null);
	}
	
	
	@Override
	public Pair<File, InputStream> next() {
		if (next == null) {
			next = findNext();
		}
		if (next == null) {
			throw new NoSuchElementException("No more files");
		}
		
		Pair<File, InputStream> n = next;
		next = null;
		fileCount++;
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
	 * Return the number of files that have so far been returned by this iterator.
	 * 
	 * @return	the number of files that this iterator has returned so far.
	 */
	public int getFileCount() {
		return fileCount;
	}
	
	/**
	 * Return the next pair of file and InputStream.
	 * 
	 * @return	a pair with the file and input stream reading the file.
	 */
	protected abstract Pair<File, InputStream> findNext();
	
}
