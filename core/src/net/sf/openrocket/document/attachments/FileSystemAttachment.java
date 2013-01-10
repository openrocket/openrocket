package net.sf.openrocket.document.attachments;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import net.sf.openrocket.document.Attachment;

public class FileSystemAttachment extends BaseAttachment implements Attachment {
	
	private final File location;
	
	public FileSystemAttachment(String name, File location) {
		super(name);
		this.location = location;
	}
	
	@Override
	public InputStream getBytes() throws FileNotFoundException, IOException {
		return new FileInputStream(location);
	}
	
}
