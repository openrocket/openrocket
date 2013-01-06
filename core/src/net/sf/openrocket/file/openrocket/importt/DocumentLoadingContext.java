package net.sf.openrocket.file.openrocket.importt;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.MotorFinder;

public class DocumentLoadingContext {
	
	private int fileVersion;
	private MotorFinder motorFinder;
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
	
}
