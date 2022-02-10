package net.sf.openrocket.appearance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import net.sf.openrocket.util.ChangeSource;
import net.sf.openrocket.util.DecalNotFoundException;

/**
 * Interface to handle image files for decals
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
	public InputStream getBytes() throws FileNotFoundException, IOException, DecalNotFoundException;
	
	/**
	 * exports an image into the File
	 * @param file	The File handler object
	 * @throws IOException
	 */
	public void exportImage(File file) throws IOException, DecalNotFoundException;
	
	/**
	 * wake up call to listeners
	 * @param source	The source of the wake up call
	 */
	public void fireChangeEvent(Object source);

	/**
	 * Get the decal file on which the DecalImage is based
	 * @return decal source file
	 */
	public File getDecalFile();

	/**
	 * Set the decal file on which the DecalImage is based
	 * @param file decal source file
	 */
	public void setDecalFile(File file);

	/**
	 * Checks whether this DecalImage should be ignored when saving the OpenRocket document.
	 * @return true if DecalImage should be ignored, false if should be saved
	 */
	public boolean isIgnored();

	/**
	 * Sets the flag to know whether this DecalImage should be ignored when saving the OpenRocket document.
	 * @param ignored true if DecalImage should be ignored, false if should be saved
	 */
	public void setIgnored(boolean ignored);
}
