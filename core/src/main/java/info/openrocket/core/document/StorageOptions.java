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
	private byte[] previewImage;		// File preview image data

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

	/**
	 * Get the file preview image data.
	 * @return byte array containing image data, or null if no preview image is set.
	 */
	public byte[] getPreviewImage() {
		return previewImage;
	}

	/**
	 * Set the file preview image data.
	 * @param previewImage byte array containing image data.
	 */
	public void setPreviewImage(byte[] previewImage) {
		this.previewImage = previewImage;
	}

	/**
	 * Clear the file preview image data.
	 */
	public void clearPreviewImage() {
		this.previewImage = null;
	}

	@Override
	public StorageOptions clone() {
		try {
			StorageOptions clone = (StorageOptions) super.clone();
			if (this.previewImage != null) {
				clone.previewImage = this.previewImage.clone();
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new BugException("CloneNotSupportedException?!?", e);
		}
	}
}
