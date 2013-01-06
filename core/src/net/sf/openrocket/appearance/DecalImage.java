package net.sf.openrocket.appearance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public interface DecalImage {

	public String getName();
	public InputStream getBytes() throws FileNotFoundException, IOException;
	public void exportImage( File file, boolean watchForChanges ) throws IOException;
	
}
