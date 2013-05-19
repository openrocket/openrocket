package net.sf.openrocket.appearance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import net.sf.openrocket.util.ChangeSource;

public interface DecalImage extends ChangeSource, Comparable<DecalImage> {
	
	public String getName();
	
	public InputStream getBytes() throws FileNotFoundException, IOException;
	
	public void exportImage(File file) throws IOException;
	
	public void fireChangeEvent(Object source);
}
