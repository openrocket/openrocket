package net.sf.openrocket.file.motor;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.sf.openrocket.file.Loader;
import net.sf.openrocket.motor.Motor;


public interface MotorLoader extends Loader<Motor> {
	
	/**
	 * Load motors from the specified <code>InputStream</code>.
	 * 
	 * @param stream		the source of the motor definitions.
	 * @param filename		the file name of the file, may be <code>null</code> if not 
	 * 						applicable.
	 * @return				a list of motors contained in the file.
	 * @throws IOException	if an I/O exception occurs of the file format is invalid.
	 */
	@Override
	public List<Motor> load(InputStream stream, String filename) throws IOException;
	
}
