package net.sf.openrocket.file;

import java.io.File;

import net.sf.openrocket.document.Attachment;
import net.sf.openrocket.document.attachments.FileSystemAttachment;

public class FileSystemAttachmentFactory implements AttachmentFactory {
	
	private final File baseDirectory;
	
	public FileSystemAttachmentFactory() {
		super();
		this.baseDirectory = null;
	}
	
	public FileSystemAttachmentFactory(File baseDirectory) {
		super();
		if (baseDirectory != null && baseDirectory.isDirectory() == false) {
			throw new IllegalArgumentException("Base file for FileSystemAttachmentFactory is not a directory");
		}
		this.baseDirectory = baseDirectory;
	}
	
	public Attachment getAttachment(File file) {
		return new FileSystemAttachment(file.getName(), file);
	}
	
	@Override
	public Attachment getAttachment(String name) {
		
		File file = new File(name);
		
		if (file.isAbsolute()) {
			return new FileSystemAttachment(name, file);
		}
		
		else {
			file = new File(baseDirectory, name);
			return new FileSystemAttachment(name, file);
		}
		
	}
	
}
