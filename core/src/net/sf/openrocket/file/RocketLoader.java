package net.sf.openrocket.file;

import java.io.File;
import java.io.InputStream;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.OpenRocketDocument;

public interface RocketLoader {
	
	public OpenRocketDocument load(File source, MotorFinder motorFinder) throws RocketLoadException;
	
	public OpenRocketDocument load(InputStream source, MotorFinder motorFinder) throws RocketLoadException;
	
	public WarningSet getWarnings();
	
}
