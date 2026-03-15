package info.openrocket.core.database;

import java.awt.Dialog;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import info.openrocket.core.l10n.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.database.motor.ThrustCurveMotorSQLiteDatabase;
import info.openrocket.core.database.motor.ThrustCurveMotorSetDatabase;
import info.openrocket.core.file.iterator.DirectoryIterator;
import info.openrocket.core.file.iterator.FileIterator;
import info.openrocket.core.file.motor.GeneralMotorLoader;
import info.openrocket.core.gui.util.SimpleFileFilter;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.Pair;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

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
		loadInternalMotorDatabase();
		loadUserDefinedMotors();
	}


	/**
	 * Loads the user defined motors
	 * the directories are defined in the preferences
	 */
	private void loadUserDefinedMotors() {
		GeneralMotorLoader loader = new GeneralMotorLoader();
		SimpleFileFilter fileFilter = new SimpleFileFilter("", buildUserMotorExtensions(loader));
		log.info("Starting reading user-defined motors");
		for (File file : (Application.getPreferences()).getUserThrustCurveFiles()) {
			if (file.isFile()) {
				if (!fileFilter.accept(file)) {
					log.warn("User-defined motor file " + file + " does not have a supported extension");
					continue;
				}
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
	 * Loads the default database from SQLite, with a legacy fallback to serialized data.
	 * Uses directory "datafiles/thrustcurves" for data.
	 */
	private void loadInternalMotorDatabase() {
		log.info("Starting reading internal motor database");
		FileIterator iterator = DirectoryIterator.findDirectory(THRUSTCURVE_DIRECTORY,
				new SimpleFileFilter("", false, "db"));
		if (iterator != null) {
			while (iterator.hasNext()) {
				Pair<File, InputStream> f = iterator.next();
				loadSqlite(f);
			}
			log.info("Ending reading SQLite motor database, motorCount=" + motorCount);
			return;
		}

		log.warn("No SQLite motor database found in " + THRUSTCURVE_DIRECTORY + ", falling back to serialized motors");
		iterator = DirectoryIterator.findDirectory(THRUSTCURVE_DIRECTORY, new SimpleFileFilter("", false, "ser"));
		if (iterator == null) {
			log.error("Unable to read serialized motor database from " + THRUSTCURVE_DIRECTORY);
			return;
		}
		while (iterator.hasNext()) {
			Pair<File, InputStream> f = iterator.next();
			loadSerialized(f);
		}
		log.info("Ending reading serialized motor database, motorCount=" + motorCount);
	}
	
	
	/**
	 * loads a serailized motor data from a stream
	 * 
	 * @param f	the pair of a File (for logging) and the input stream
	 */
	@SuppressWarnings("unchecked")
	private void loadSerialized(Pair<File, InputStream> f) {
		log.debug("Reading motors from file " + f.getU().getPath());
		try (ObjectInputStream ois = new ObjectInputStream(f.getV())) {
			List<ThrustCurveMotor> motors = (List<ThrustCurveMotor>) ois.readObject();
			addMotors(motors);
		} catch (Exception ex) {
			throw new BugException(ex);
		}
	}

	private void loadSqlite(Pair<File, InputStream> f) {
		SqliteFile sqliteFile = null;
		try {
			sqliteFile = materializeSqliteFile(f);
			log.debug("Reading motors from SQLite database " + sqliteFile.file.getPath());
			List<ThrustCurveMotor> motors = ThrustCurveMotorSQLiteDatabase.readDatabase(sqliteFile.file);
			addMotors(motors);
		} catch (Exception ex) {
			throw new BugException(ex);
		} finally {
			if (sqliteFile != null && sqliteFile.temporary && !sqliteFile.file.delete()) {
				log.debug("Unable to delete temporary SQLite database " + sqliteFile.file.getAbsolutePath());
			}
		}
	}

	private SqliteFile materializeSqliteFile(Pair<File, InputStream> f) throws IOException {
		File file = f.getU();
		if (file != null && file.isFile()) {
			try {
				f.getV().close();
			} catch (IOException e) {
				log.debug("Unable to close SQLite input stream for " + file.getPath(), e);
			}
			return new SqliteFile(file, false);
		}

		File tempFile = File.createTempFile("openrocket-thrustcurves", ".db");
		tempFile.deleteOnExit();
		try (InputStream stream = f.getV()) {
			Files.copy(stream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		return new SqliteFile(tempFile, true);
	}

	private static String[] buildUserMotorExtensions(GeneralMotorLoader loader) {
		String[] supported = loader.getSupportedExtensions();
		String[] extensions = Arrays.copyOf(supported, supported.length + 1);
		extensions[supported.length] = "db";
		return extensions;
	}

	private static boolean isSqliteFile(File file) {
		if (file == null) {
			return false;
		}
		String name = file.getName().toLowerCase(Locale.ENGLISH);
		return name.endsWith(".db");
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
			if (isSqliteFile(f.getU())) {
				loadSqlite(f);
				return;
			}

			try {
				List<ThrustCurveMotor.Builder> motors = loader.load(f.getV(), f.getU().getName());
				addMotorsFromBuilders(motors);
			} catch (IllegalArgumentException | IOException e) {
				Translator trans = Application.getTranslator();
				String fullPath = f.getU().getPath();
				String message = "<html><body><p style='width: 400px;'><i>" + e.getMessage() +
						"</i>.<br><br>" + MessageFormat.format(trans.get("MotorDbLoaderDlg.message1"), fullPath) +
						"<br>" + trans.get("MotorDbLoaderDlg.message2") + "</p></body></html>";
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						JOptionPane pane = new JOptionPane(message, JOptionPane.WARNING_MESSAGE);
						JDialog dialog = pane.createDialog(null, trans.get("MotorDbLoaderDlg.title"));
						dialog.setModalityType(Dialog.ModalityType.MODELESS);
						dialog.setAlwaysOnTop(true);
						dialog.setVisible(true);
					}
				});
			}
			f.getV().close();
		} catch (Exception e) {
			log.warn("Exception while loading file " + f.getU() + ": " + e, e);
			try {
				if (f.getV() != null) {
					f.getV().close();
				}
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

	private static class SqliteFile {
		private final File file;
		private final boolean temporary;

		private SqliteFile(File file, boolean temporary) {
			this.file = file;
			this.temporary = temporary;
		}
	}
}
