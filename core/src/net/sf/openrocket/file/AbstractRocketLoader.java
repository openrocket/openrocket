package net.sf.openrocket.file;

import java.io.IOException;
import java.io.InputStream;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.OpenRocketDocument;


public abstract class AbstractRocketLoader implements RocketLoader {
	protected final WarningSet warnings = new WarningSet();
	
	
	/**
	 * Loads a rocket from the specified InputStream.
	 */
	@Override
	public final void load(OpenRocketDocument doc, InputStream source, MotorFinder motorFinder) throws RocketLoadException {
		warnings.clear();
		
		try {
			loadFromStream(doc, source, motorFinder);
		} catch (RocketLoadException e) {
			throw e;
		} catch (IOException e) {
			throw new RocketLoadException("I/O error: " + e.getMessage(), e);
		}
	}
	
	
	
	/**
	 * This method is called by the default implementations of #load(File) 
	 * and load(InputStream) to load the rocket.
	 * 
	 * @throws RocketLoadException	if an error occurs during loading.
	 */
	protected abstract void loadFromStream(OpenRocketDocument doc, InputStream source, MotorFinder motorFinder) throws IOException,
			RocketLoadException;
	
	
	
	@Override
	public final WarningSet getWarnings() {
		return warnings;
	}
}
