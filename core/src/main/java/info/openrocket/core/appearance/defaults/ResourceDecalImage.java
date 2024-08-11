package info.openrocket.core.appearance.defaults;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import info.openrocket.core.appearance.DecalImage;
import info.openrocket.core.util.FileUtils;
import info.openrocket.core.util.StateChangeListener;

/**
 * 
 * Default implementation class of DecalImage
 *
 */
public class ResourceDecalImage implements DecalImage {

	/** File path to the image */
	private String resource;

	// Flag to check whether this DecalImage should be ignored for saving
	private boolean ignored = false;
	
	/**
	 *  main constructor, stores the file path given
	 * @param resource
	 */
	public ResourceDecalImage(final String resource) {
		this.resource = resource;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public String getName() {
		return resource;
	}
	
	@Override
	public InputStream getBytes() throws IOException {
		return this.getClass().getResourceAsStream(resource);
	}
	
	@Override
	public void exportImage(File file) throws IOException {
		InputStream is = getBytes();

		if (is == null) {
			return;
		}

		try (is; OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
			FileUtils.copy(is, os);
		}
	}
		
	@Override
	public void fireChangeEvent(Object source) {
	}
	
	@Override
	public void addChangeListener(StateChangeListener listener) {
		//Unimplemented, this can not change
	}
	
	@Override
	public void removeChangeListener(StateChangeListener listener) {
		//Unimplemented, this can not change
	}
	
	@Override
	public int compareTo(DecalImage o) {
		return getName().compareTo(o.getName());
	}

	@Override
	public void setDecalFile(File file) {
		if (file != null) {
			this.resource = file.getAbsolutePath();
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
	public File getDecalFile() {
		return new File(resource);
	}


}
