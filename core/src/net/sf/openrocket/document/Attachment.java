package net.sf.openrocket.document;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public interface Attachment {
	
	public abstract String getName();
	
	public abstract InputStream getBytes() throws FileNotFoundException, IOException;
	
}