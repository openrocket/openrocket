package net.sf.openrocket.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.OpenRocketDocument;


public abstract class AbstractRocketLoader implements RocketLoader {
	protected final WarningSet warnings = new WarningSet();
	
	
	/**
	 * Loads a rocket from the specified File object.
	 */
	@Override
	public final OpenRocketDocument load(File source, MotorFinder motorFinder) throws RocketLoadException {
		warnings.clear();
		InputStream stream = null;
		
		try {
			
			stream = new BufferedInputStream(new FileInputStream(source));
			return load(stream, motorFinder);
			
		} catch (FileNotFoundException e) {
			throw new RocketLoadException("File not found: " + source);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Loads a rocket from the specified InputStream.
	 */
	@Override
	public final OpenRocketDocument load(InputStream source, MotorFinder motorFinder) throws RocketLoadException {
		warnings.clear();
		
		try {
			return loadFromStream(source, motorFinder);
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
	protected abstract OpenRocketDocument loadFromStream(InputStream source, MotorFinder motorFinder) throws IOException,
			RocketLoadException;
	
	
	
	@Override
	public final WarningSet getWarnings() {
		return warnings;
	}
}
