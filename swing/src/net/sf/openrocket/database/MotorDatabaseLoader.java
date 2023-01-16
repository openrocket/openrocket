package net.sf.openrocket.database;

import java.awt.Dialog;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.text.MessageFormat;
import java.util.List;

import net.sf.openrocket.l10n.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.database.motor.ThrustCurveMotorSetDatabase;
import net.sf.openrocket.file.iterator.DirectoryIterator;
import net.sf.openrocket.file.iterator.FileIterator;
import net.sf.openrocket.file.motor.GeneralMotorLoader;
import net.sf.openrocket.gui.util.SimpleFileFilter;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Pair;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

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
	
	/**
	 * sole constructor, default startup delay = 0
	 */
	public MotorDatabaseLoader() {
		super(STARTUP_DELAY);
	}
	
	
	@Override
	protected void loadDatabase() {
		loadSerializedMotorDatabase();
		loadUserDefinedMotors();
	}


	/**
	 * Loads the user defined motors
	 * the directories are defined in the preferences
	 */
	private void loadUserDefinedMotors() {
		GeneralMotorLoader loader = new GeneralMotorLoader();
		SimpleFileFilter fileFilter = new SimpleFileFilter("", loader.getSupportedExtensions());
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


	/**
	 * Loads the default, with established serialized manufacturing and data
	 * uses directory "datafiles/thrustcurves" for data  
	 */
	private void loadSerializedMotorDatabase() {
		log.info("Starting reading serialized motor database");
		FileIterator iterator = DirectoryIterator.findDirectory(THRUSTCURVE_DIRECTORY, new SimpleFileFilter("", false, "ser"));
		while (iterator.hasNext()) {
			Pair<File, InputStream> f = iterator.next();
			loadSerialized(f);
		}
		log.info("Ending reading serialized motor database, motorCount=" + motorCount);
	}
	
	
	/**
	 * loads a serailized motor data from an stream
	 * 
	 * @param f	the pair of a File (for logging) and the input stream
	 */
	@SuppressWarnings("unchecked")
	private void loadSerialized(Pair<File, InputStream> f) {
		try {
			log.debug("Reading motors from file " + f.getU().getPath());
			ObjectInputStream ois = new ObjectInputStream(f.getV());
			List<ThrustCurveMotor> motors = (List<ThrustCurveMotor>) ois.readObject();
			addMotors(motors);
		} catch (Exception ex) {
			throw new BugException(ex);
		}
	}
	
	/**
	 * loads a single motor file into the database using a simple file handler object
	 * 
	 * @param loader	the motor loading handler object
	 * @param file		the File to the file itself
	 */
	private void loadFile(GeneralMotorLoader loader, File file) {
		try {
			log.debug("Loading motors from file " + file);
			loadFile(
					loader,
					new Pair<>(
							file,
							new BufferedInputStream(new FileInputStream(file))));
		} catch (Exception e) {
			log.warn("Exception while reading " + file + ": " + e, e);
		}
	}
	
	/**
	 * loads a single motor file into the database using inputStream instead of file object
	 * 
	 * @param loader	an object to handle the loading
	 * @param f			the pair of File name and its input stream
	 */
	private void loadFile(GeneralMotorLoader loader, Pair<File, InputStream> f) {
		try {
			List<ThrustCurveMotor.Builder> motors = loader.load(f.getV(), f.getU().getName());
			try {
				addMotorsFromBuilders(motors);
			}
			catch (IllegalArgumentException e) {
				Translator trans = Application.getTranslator();
				String fullPath = f.getU().getPath();
				String message = "<html><body><p style='width: 400px;'><i>" + e.getMessage() +
						"</i>.<br><br>" + MessageFormat.format( trans.get("MotorDbLoaderDlg.message1"), fullPath) +
						"<br>" + trans.get("MotorDbLoaderDlg.message2") + "</p></body></html>";
				JOptionPane pane = new JOptionPane(message, JOptionPane.WARNING_MESSAGE);
				JDialog dialog = pane.createDialog(null, trans.get("MotorDbLoaderDlg.title"));
				dialog.setModalityType(Dialog.ModalityType.MODELESS);
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
			}
			f.getV().close();
		} catch (Exception e) {
			log.warn("Exception while loading file " + f.getU() + ": " + e, e);
			try {
				f.getV().close();
			} catch (IOException e1) {
			}
		}
	}
	
	/**
	 * loads an entire directory of motor files
	 * 
	 * @param loader 		a motor loading handler object
	 * @param fileFilter	the supported extensions of files
	 * @param file			the directory file object
	 */
	private void loadDirectory(GeneralMotorLoader loader, SimpleFileFilter fileFilter, File file) {
		FileIterator iterator;
		try {
			iterator = new DirectoryIterator(file, fileFilter, true);
		} catch (Exception e) {
			log.warn("Unable to read directory " + file + ": " + e, e);
			return;
		}
		while (iterator.hasNext()) {
			loadFile(loader, iterator.next());
		}
	}


	
	
	/**
	 * adds a motor list into the database
	 * @param motors	the list of motors to be added
	 */
	private synchronized void addMotors(List<ThrustCurveMotor> motors) {
		for (ThrustCurveMotor m : motors) {
			motorCount++;
			database.addMotor(m);
		}
	}
	
	/**
	 * builds the motors while building them
	 * 
	 * @param motorBuilders List of motor builders to be used for adding motor into the database
	 */
	private synchronized void addMotorsFromBuilders(List<ThrustCurveMotor.Builder> motorBuilders) {
		for (ThrustCurveMotor.Builder m : motorBuilders) {
			motorCount++;
			database.addMotor(m.build());
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
