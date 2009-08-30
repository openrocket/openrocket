package net.sf.openrocket.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;

import net.sf.openrocket.motor.Motor;

/**
 * A motor loader class that detects the file type based on the file name extension.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class GeneralMotorLoader extends MotorLoader {

	private static final MotorLoader RASP_LOADER = new RASPMotorLoader();
	private static final MotorLoader ROCKSIM_LOADER = new RockSimMotorLoader();
	
	
	@Override
	public List<Motor> load(InputStream stream, String filename) throws IOException {
		return selectLoader(filename).load(stream, filename);
	}

	@Override
	public List<Motor> load(Reader reader, String filename) throws IOException {
		return selectLoader(filename).load(reader, filename);
	}
	

	@Override
	protected Charset getDefaultCharset() {
		// Not used, may return null
		return null;
	}

	
	/**
	 * Return the appropriate motor loader based on the file name.
	 * 
	 * @param filename		the file name (may be <code>null</code>).
	 * @return				the appropriate motor loader to use for the file.
	 * @throws IOException	if the file type cannot be detected from the file name.
	 */
	public static MotorLoader selectLoader(String filename) throws IOException {
		if (filename == null) {
			throw new IOException("Unknown file type.");
		}
		
		String ext = "";
		int point = filename.lastIndexOf('.');
		
		if (point > 0)
			ext = filename.substring(point+1);
		
		if (ext.equalsIgnoreCase("eng")) {
			return RASP_LOADER;
		} else if (ext.equalsIgnoreCase("rse")) {
			return ROCKSIM_LOADER;
		}
		
		throw new IOException("Unknown file type.");
	}
	
}
