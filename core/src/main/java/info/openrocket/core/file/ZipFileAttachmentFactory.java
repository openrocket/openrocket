package info.openrocket.core.file;

import java.net.URL;

import info.openrocket.core.document.Attachment;
import info.openrocket.core.document.attachments.ZipFileAttachment;

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
