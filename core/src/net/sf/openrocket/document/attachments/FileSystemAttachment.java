package net.sf.openrocket.document.attachments;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import net.sf.openrocket.document.Attachment;

/**
 * 
 * 	defines a file system attachment
 *	stores the attachment location
 */
public class FileSystemAttachment extends Attachment {
	/** the file location*/
	private final File location;
	
	/**
	 * main constructor,
	 * 
	 * @param name		name of attachment
	 * @param location	File location of attachment
	 */
	public FileSystemAttachment(String name, File location) {
		super(name);
		this.location = location;
	}
	
	/**
	 * 
	 * @return	the File object with the attachment location
	 */
	public File getLocation() {
		return location;
	}
	
	/**
	 *	{@inheritDoc}
	 *	creates the stream based on the location passed while building
	 */
	@Override
	public InputStream getBytes() throws FileNotFoundException, IOException {
		return new FileInputStream(location);
	}
	
}
