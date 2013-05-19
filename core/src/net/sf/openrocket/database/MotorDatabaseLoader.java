package net.sf.openrocket.database;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;

import net.sf.openrocket.database.motor.ThrustCurveMotorSetDatabase;
import net.sf.openrocket.file.iterator.DirectoryIterator;
import net.sf.openrocket.file.iterator.FileIterator;
import net.sf.openrocket.file.motor.GeneralMotorLoader;
import net.sf.openrocket.gui.util.SimpleFileFilter;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An asynchronous database loader that loads the internal thrust curves
 * and external user-supplied thrust curves to a ThrustCurveMotorSetDatabase.
 * The resulting database is obtained using getDatabase().
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class MotorDatabaseLoader extends AsynchronousDatabaseLoader {
	
	private final static Logger log = LoggerFactory.getLogger(MotorDatabaseLoader.class);
	
	private static final String THRUSTCURVE_DIRECTORY = "datafiles/thrustcurves/";
	private static final long STARTUP_DELAY = 0;
	
	private final ThrustCurveMotorSetDatabase database = new ThrustCurveMotorSetDatabase();
	private int motorCount = 0;
	
	
	public MotorDatabaseLoader() {
		super(STARTUP_DELAY);
	}
	
	
	@Override
	protected void loadDatabase() {
		
		GeneralMotorLoader loader = new GeneralMotorLoader();
		SimpleFileFilter fileFilter = new SimpleFileFilter("", loader.getSupportedExtensions());
		
		log.info("Starting reading serialized motor database");
		FileIterator iterator = DirectoryIterator.findDirectory(THRUSTCURVE_DIRECTORY, new SimpleFileFilter("", false, "ser"));
		while (iterator.hasNext()) {
			Pair<String, InputStream> f = iterator.next();
			loadSerialized(f);
		}
		log.info("Ending reading serialized motor database, motorCount=" + motorCount);
		
		
		log.info("Starting reading user-defined motors");
		for (File file : ((SwingPreferences) Application.getPreferences()).getUserThrustCurveFiles()) {
			if (file.isFile()) {
				loadFile(loader, file);
			} else if (file.isDirectory()) {
				loadDirectory(loader, fileFilter, file);
			} else {
				log.warn("User-defined motor file " + file + " is neither file nor directory");
			}
		}
		log.info("Ending reading user-defined motors, motorCount=" + motorCount);
		
	}
	
	
	
	@SuppressWarnings("unchecked")
	private void loadSerialized(Pair<String, InputStream> f) {
		try {
			log.debug("Reading motors from file " + f.getU());
			ObjectInputStream ois = new ObjectInputStream(f.getV());
			List<Motor> motors = (List<Motor>) ois.readObject();
			addMotors(motors);
		} catch (Exception ex) {
			throw new BugException(ex);
		}
	}
	
	
	private void loadFile(GeneralMotorLoader loader, File file) {
		BufferedInputStream bis = null;
		try {
			log.debug("Loading motors from file " + file);
			bis = new BufferedInputStream(new FileInputStream(file));
			List<Motor> motors = loader.load(bis, file.getName());
			addMotors(motors);
			bis.close();
		} catch (IOException e) {
			log.warn("IOException while reading " + file + ": " + e, e);
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e1) {
					
				}
			}
		}
	}
	
	private void loadDirectory(GeneralMotorLoader loader, SimpleFileFilter fileFilter, File file) {
		FileIterator iterator;
		try {
			iterator = new DirectoryIterator(file, fileFilter, true);
		} catch (IOException e) {
			log.warn("Unable to read directory " + file + ": " + e, e);
			return;
		}
		while (iterator.hasNext()) {
			Pair<String, InputStream> f = iterator.next();
			try {
				List<Motor> motors = loader.load(f.getV(), f.getU());
				addMotors(motors);
				f.getV().close();
			} catch (IOException e) {
				log.warn("IOException while loading file " + f.getU() + ": " + e, e);
				try {
					f.getV().close();
				} catch (IOException e1) {
				}
			}
		}
	}
	
	private synchronized void addMotors(List<Motor> motors) {
		for (Motor m : motors) {
			motorCount++;
			database.addMotor((ThrustCurveMotor) m);
		}
	}
	
	/**
	 * Returns the loaded database.  If the database has not fully loaded,
	 * this blocks until it is.
	 * 
	 * @return	the motor database
	 */
	public ThrustCurveMotorSetDatabase getDatabase() {
		blockUntilLoaded();
		return database;
	}
}
