package net.sf.openrocket.file.motor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.file.UnknownFileTypeException;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.util.UncloseableInputStream;

/**
 * A motor loader that loads motors from a ZIP file.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ZipFileMotorLoader implements MotorLoader {
	private static final Logger log = LoggerFactory.getLogger(ZipFileMotorLoader.class);
	
	private final MotorLoader loader;
	
	
	/**
	 * Construct a ZipFileMotorLoader that loads files using a 
	 * {@link GeneralMotorLoader}.
	 */
	public ZipFileMotorLoader() {
		this(new GeneralMotorLoader());
	}
	
	/**
	 * Constructs a ZipFileMotorLoader that loads files using the provided motor loader.
	 * 
	 * @param loader	the motor loader to use when loading.
	 */
	public ZipFileMotorLoader(MotorLoader loader) {
		this.loader = loader;
	}
	
	
	@Override
	public List<Motor> load(InputStream stream, String filename) throws IOException {
		List<Motor> motors = new ArrayList<Motor>();
		
		ZipInputStream is = new ZipInputStream(stream);
		
		// SAX seems to close the input stream, prevent it
		InputStream uncloseable = new UncloseableInputStream(is);
		
		while (true) {
			ZipEntry entry = is.getNextEntry();
			if (entry == null)
				break;
			
			if (entry.isDirectory())
				continue;
			
			// Get the file name of the entry
			String name = entry.getName();
			int index = name.lastIndexOf('/');
			if (index < 0) {
				index = name.lastIndexOf('\\');
			}
			if (index >= 0) {
				name = name.substring(index + 1);
			}
			
			try {
				List<Motor> m = loader.load(uncloseable, entry.getName());
				motors.addAll(m);
				log.info("Loaded " + m.size() + " motors from ZIP entry " + entry.getName());
			} catch (UnknownFileTypeException e) {
				log.info("Could not read ZIP entry " + entry.getName() + ": " + e.getMessage());
			}
			
		}
		
		return motors;
	}
	
}
