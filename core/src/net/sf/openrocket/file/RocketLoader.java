package net.sf.openrocket.file;

import java.io.InputStream;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.OpenRocketDocument;

public interface RocketLoader {
	
	public void load(OpenRocketDocument doc, InputStream source, MotorFinder motorFinder) throws RocketLoadException;
	
	public WarningSet getWarnings();
	
}
