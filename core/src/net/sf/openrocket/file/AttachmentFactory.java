package net.sf.openrocket.file;

import net.sf.openrocket.document.Attachment;

public interface AttachmentFactory {
	
	public Attachment getAttachment(String name);
	
}
