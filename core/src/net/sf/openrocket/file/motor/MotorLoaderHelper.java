package net.sf.openrocket.file.motor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.file.iterator.DirectoryIterator;
import net.sf.openrocket.file.iterator.FileIterator;
import net.sf.openrocket.gui.util.SimpleFileFilter;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.util.Pair;

public final class MotorLoaderHelper {

	private static final Logger log = LoggerFactory.getLogger(MotorLoaderHelper.class);

	private MotorLoaderHelper() {
		// Prevent construction
	}

	/**
	 * Load a file or directory of thrust curves.  Directories are loaded
	 * recursively.  Any errors during loading are logged, but otherwise ignored.
	 * 
	 * @param target	the file or directory to load.
	 * @return			a list of all motors in the file/directory.
	 */
	public static List<Motor> load(File target) {
		GeneralMotorLoader loader = new GeneralMotorLoader();

		if (target.isDirectory()) {

			try {
				return load(new DirectoryIterator(target, new SimpleFileFilter("", loader.getSupportedExtensions()), true));
			} catch (IOException e) {
				log.warn("Could not read directory " + target, e);
				return Collections.emptyList();
			}

		} else {

			InputStream is = null;
			try {
				is = new FileInputStream(target);
				return loader.load(new BufferedInputStream(is), target.getName());
			} catch (IOException e) {
				log.warn("Could not load file " + target, e);
				return Collections.emptyList();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						log.error("Could not close file " + target, e);
					}
				}
			}

		}
	}

	public static List<Motor> load( InputStream is, String fileName ) {
		GeneralMotorLoader loader = new GeneralMotorLoader();
		try {
			List<Motor> motors = loader.load(is, fileName);
			if (motors.size() == 0) {
				log.warn("No motors found in file " + fileName);
			}
			return motors;
		} catch (IOException e) {
			log.warn("IOException when loading motor file " + fileName, e);
		}
		return Collections.<Motor>emptyList();
	}

	/**
	 * Load motors from files iterated over by a FileIterator.  Any errors during
	 * loading are logged, but otherwise ignored.
	 * <p>
	 * The iterator is closed at the end of the operation.
	 * 
	 * @param iterator	the FileIterator that iterates of the files to load.
	 * @return			a list of all motors loaded.
	 */
	public static List<Motor> load(FileIterator iterator) {
		List<Motor> list = new ArrayList<Motor>();

		while (iterator.hasNext()) {
			final Pair<String, InputStream> input = iterator.next();
			log.debug("Loading motors from file " + input.getU());
			try {
				List<Motor> motors = load(input.getV(), input.getU());
				for (Motor m : motors) {
					list.add((ThrustCurveMotor) m);
				}
			} finally {
				try {
					input.getV().close();
				} catch (IOException e) {
					log.error("IOException when closing InputStream", e);
				}
			}
		}
		iterator.close();

		return list;
	}

}
