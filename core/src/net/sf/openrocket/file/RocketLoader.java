package net.sf.openrocket.file;

import java.io.InputStream;

import net.sf.openrocket.aerodynamics.WarningSet;

public interface RocketLoader {
	
	public void load(DocumentLoadingContext context, InputStream source) throws RocketLoadException;
	
	public WarningSet getWarnings();
	
}
