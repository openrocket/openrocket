package net.sf.openrocket.file;

import java.io.InputStream;

import net.sf.openrocket.logging.WarningSet;

public interface RocketLoader {
	
	public void load(DocumentLoadingContext context, InputStream source, String fileName) throws RocketLoadException;
	
	public WarningSet getWarnings();
	
}
