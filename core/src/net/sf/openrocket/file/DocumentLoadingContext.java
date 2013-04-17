package net.sf.openrocket.file;

import net.sf.openrocket.document.OpenRocketDocument;

public class DocumentLoadingContext {
	
	private int fileVersion;
	private MotorFinder motorFinder;
	private AttachmentFactory attachmentFactory = new FileSystemAttachmentFactory();
	private OpenRocketDocument document;
	
	public int getFileVersion() {
		return fileVersion;
	}
	
	public void setFileVersion(int fileVersion) {
		this.fileVersion = fileVersion;
	}
	
	public MotorFinder getMotorFinder() {
		return motorFinder;
	}
	
	public void setMotorFinder(MotorFinder motorFinder) {
		this.motorFinder = motorFinder;
	}
	
	public OpenRocketDocument getOpenRocketDocument() {
		return document;
	}
	
	public void setOpenRocketDocument(OpenRocketDocument document) {
		this.document = document;
	}
	
	public AttachmentFactory getAttachmentFactory() {
		return attachmentFactory;
	}
	
	public void setAttachmentFactory(AttachmentFactory attachmentFactory) {
		this.attachmentFactory = attachmentFactory;
	}
	
}
