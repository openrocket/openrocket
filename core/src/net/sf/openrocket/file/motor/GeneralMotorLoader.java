package net.sf.openrocket.file.motor;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.sf.openrocket.file.UnknownFileTypeException;
import net.sf.openrocket.motor.Motor;

/**
 * A motor loader class that detects the file type based on the file name extension.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class GeneralMotorLoader implements MotorLoader {
	
	private final MotorLoader RASP_LOADER = new RASPMotorLoader();
	private final MotorLoader ROCKSIM_LOADER = new RockSimMotorLoader();
	private final MotorLoader ZIP_LOADER;
	
	
	public GeneralMotorLoader() {
		// Must use this loader in order to avoid recursive instantiation
		ZIP_LOADER = new ZipFileMotorLoader(this);
	}
	
	

	/**
	 * {@inheritDoc}
	 * 
	 * @throws UnknownFileTypeException		if the file format is not supported
	 */
	@Override
	public List<Motor> load(InputStream stream, String filename) throws IOException {
		return selectLoader(filename).load(stream, filename);
	}
	
	

	/**
	 * Return an array containing the supported file extensions.
	 * 
	 * @return	an array of the supported file extensions.
	 */
	public String[] getSupportedExtensions() {
		return new String[] { "rse", "eng", "zip" };
	}
	
	
	/**
	 * Return the appropriate motor loader based on the file name.
	 * 
	 * @param filename		the file name (may be <code>null</code>).
	 * @return				the appropriate motor loader to use for the file.
	 * @throws UnknownFileTypeException		if the file type cannot be detected from the file name.
	 */
	private MotorLoader selectLoader(String filename) throws IOException {
		if (filename == null) {
			throw new UnknownFileTypeException("Unknown file type, filename=null");
		}
		
		String ext = "";
		int point = filename.lastIndexOf('.');
		
		if (point > 0)
			ext = filename.substring(point + 1);
		
		if (ext.equalsIgnoreCase("eng")) {
			return RASP_LOADER;
		} else if (ext.equalsIgnoreCase("rse")) {
			return ROCKSIM_LOADER;
		} else if (ext.equalsIgnoreCase("zip")) {
			return ZIP_LOADER;
		}
		
		throw new UnknownFileTypeException("Unknown file type, filename=" + filename);
	}
	
}
