package info.openrocket.core.file;

import java.util.HashMap;
import java.util.Map;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.rocketcomponent.RocketComponent;

public class DocumentLoadingContext {

	private int fileVersion;
	private MotorFinder motorFinder;
	private AttachmentFactory attachmentFactory = new FileSystemAttachmentFactory();
	private OpenRocketDocument document;
	/**
	 * Components by the identifier they have in the file being loaded, for file formats that refer to components by
	 * such an identifier (for example RockSim's part serial numbers).
	 */
	private final Map<String, RocketComponent> componentsByFileId = new HashMap<>();

	public void putComponentByFileId(String id, RocketComponent component) {
		componentsByFileId.put(id, component);
	}

	public RocketComponent getComponentByFileId(String id) {
		return componentsByFileId.get(id);
	}

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
