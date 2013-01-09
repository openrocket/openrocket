package net.sf.openrocket.document;

public interface AttachmentFactory<T extends Attachment> {
	
	public T getAttachment(String name);
	
}
