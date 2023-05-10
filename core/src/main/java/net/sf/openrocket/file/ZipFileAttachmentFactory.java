package net.sf.openrocket.file;

import java.net.URL;

import net.sf.openrocket.document.Attachment;
import net.sf.openrocket.document.attachments.ZipFileAttachment;

public class ZipFileAttachmentFactory implements AttachmentFactory {
	
	private final URL zipFile;
	
	public ZipFileAttachmentFactory(URL zipFile) {
		super();
		this.zipFile = zipFile;
	}
	
	@Override
	public Attachment getAttachment(String name) {
		return new ZipFileAttachment(name, zipFile);
	}
}
