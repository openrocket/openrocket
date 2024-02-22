package info.openrocket.core.document;

import info.openrocket.core.util.BugException;

public class StorageOptions implements Cloneable {

	public enum FileType {
		OPENROCKET,
		ROCKSIM,
		RASAERO,
		WAVEFRONT_OBJ
	}

	private FileType fileType = FileType.OPENROCKET;

	private boolean saveSimulationData = false;

	private boolean explicitlySet = false;

	public FileType getFileType() {
		return fileType;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	public boolean getSaveSimulationData() {
		return saveSimulationData;
	}

	public void setSaveSimulationData(boolean s) {
		saveSimulationData = s;
	}

	public boolean isExplicitlySet() {
		return explicitlySet;
	}

	public void setExplicitlySet(boolean explicitlySet) {
		this.explicitlySet = explicitlySet;
	}

	@Override
	public StorageOptions clone() {
		try {
			return (StorageOptions) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new BugException("CloneNotSupportedException?!?", e);
		}
	}
}
