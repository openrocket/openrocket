package info.openrocket.core.file;

import info.openrocket.core.document.Attachment;

public interface AttachmentFactory {

	public Attachment getAttachment(String name);

}
