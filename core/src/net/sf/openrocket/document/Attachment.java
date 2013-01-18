package net.sf.openrocket.document;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import net.sf.openrocket.util.ChangeSource;

public interface Attachment extends Comparable<Attachment>, ChangeSource {
	
	public abstract String getName();
	
	public abstract InputStream getBytes() throws FileNotFoundException, IOException;
	
}