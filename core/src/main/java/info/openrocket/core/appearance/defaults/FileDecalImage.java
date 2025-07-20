package info.openrocket.core.appearance.defaults;

import info.openrocket.core.appearance.DecalImage;
import info.openrocket.core.util.DecalNotFoundException;
import info.openrocket.core.util.FileUtils;
import info.openrocket.core.util.StateChangeListener;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An implementation of DecalImage that loads the image from a java.io.File.
 * This allows for loading textures from arbitrary locations on the file system.
 */
public class FileDecalImage implements DecalImage {

	private File decalFile;
	private boolean ignored = false;

	/**
	 * Main constructor, stores the file to be used.
	 * @param file the image file. Cannot be null.
	 */
	public FileDecalImage(File file) {
		if (file == null) {
			throw new IllegalArgumentException("Decal file cannot be null.");
		}
		this.decalFile = file;
	}

	@Override
	public String getName() {
		return decalFile.getName();
	}

	@Override
	public InputStream getBytes() throws IOException, DecalNotFoundException {
		if (!decalFile.exists() || !decalFile.canRead()) {
			throw new DecalNotFoundException("Decal file not found or cannot be read: " + decalFile.getAbsolutePath(), null);
		}
		return new FileInputStream(this.decalFile);
	}

	@Override
	public void exportImage(File file) throws IOException, DecalNotFoundException {
		try (InputStream is = getBytes();
			 OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
			FileUtils.copy(is, os);
		}
	}

	@Override
	public int compareTo(DecalImage o) {
		if (o == null || o.getDecalFile() == null) {
			return 1;
		}
		return getDecalFile().compareTo(o.getDecalFile());
	}

	@Override
	public File getDecalFile() {
		return decalFile;
	}

	@Override
	public void setDecalFile(File file) {
		if (file != null) {
			this.decalFile = file;
		}
	}

	@Override
	public boolean isIgnored() {
		return this.ignored;
	}

	@Override
	public void setIgnored(boolean ignored) {
		this.ignored = ignored;
	}

	@Override
	public String toString() {
		return "FileDecal: [" + decalFile.getAbsolutePath() + "]";
	}

	// Unimplemented listener methods as this object's state is immutable from the perspective of listeners.
	@Override
	public void fireChangeEvent(Object source) {
	}

	@Override
	public void addChangeListener(StateChangeListener listener) {
	}

	@Override
	public void removeChangeListener(StateChangeListener listener) {
	}
}