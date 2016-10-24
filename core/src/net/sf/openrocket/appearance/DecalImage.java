package net.sf.openrocket.appearance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import net.sf.openrocket.util.ChangeSource;

/**
 * Interface to handle image files for declas
 *
 */
public interface DecalImage extends ChangeSource, Comparable<DecalImage> {
	
	/**
	 * returns the name of the file path of the image
	 * @return	name of file path
	 */
	public String getName();
	
	/**
	 * gets the Stream of bytes representing the image itself
	 * 
	 * @return the Stream of bytes representing the image 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public InputStream getBytes() throws FileNotFoundException, IOException;
	
	/**
	 * exports an image into the File
	 * @param file	The File handler object
	 * @throws IOException
	 */
	public void exportImage(File file) throws IOException;
	
	/**
	 * wake up call to listeners
	 * @param source	The source of the wake up call
	 */
	public void fireChangeEvent(Object source);
}
