package net.sf.openrocket.startup;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;

import net.sf.openrocket.communication.UpdateInfo;
import net.sf.openrocket.communication.UpdateInfoRetriever;
import net.sf.openrocket.database.Databases;
import net.sf.openrocket.database.ThrustCurveMotorSet;
import net.sf.openrocket.database.ThrustCurveMotorSetDatabase;
import net.sf.openrocket.file.iterator.DirectoryIterator;
import net.sf.openrocket.file.iterator.FileIterator;
import net.sf.openrocket.file.motor.MotorLoaderHelper;
import net.sf.openrocket.gui.dialogs.UpdateInfoDialog;
import net.sf.openrocket.gui.main.BasicFrame;
import net.sf.openrocket.gui.main.ExceptionHandler;
import net.sf.openrocket.gui.main.SimpleFileFilter;
import net.sf.openrocket.gui.main.Splash;
import net.sf.openrocket.logging.DelegatorLogger;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.logging.LogLevel;
import net.sf.openrocket.logging.LogLevelBufferLogger;
import net.sf.openrocket.logging.PrintStreamLogger;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.util.GUIUtil;
import net.sf.openrocket.util.Prefs;


/**
 * A startup class that checks that a suitable JRE environment is being run.
 * If the environment is too old the execution is canceled, and if OpenJDK is being
 * used warns the user of problems and confirms whether to continue.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class Startup {
	
	private static LogHelper log;
	
	private static final String LOG_STDERR_PROPERTY = "openrocket.log.stderr";
	private static final String LOG_STDOUT_PROPERTY = "openrocket.log.stdout";
	
	private static final int LOG_BUFFER_LENGTH = 50;
	
	private static final String THRUSTCURVE_DIRECTORY = "datafiles/thrustcurves/";
	

	/** Block motor loading for this many milliseconds */
	private static AtomicInteger blockLoading = new AtomicInteger(Integer.MAX_VALUE);
	
	
	public static void main(final String[] args) throws Exception {
		
		// Check for "openrocket.debug" property before anything else
		checkDebugStatus();
		
		// Initialize logging first so we can use it
		initializeLogging();
		
		// Check that we have a head
		checkHead();
		
		// Check that we're running a good version of a JRE
		log.info("Checking JRE compatibility");
		VersionHelper.checkVersion();
		VersionHelper.checkOpenJDK();
		
		// Run the actual startup method in the EDT since it can use progress dialogs etc.
		log.info("Running main");
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				runMain(args);
			}
		});
		
		log.info("Startup complete");
		
		// Block motor loading for 1.5 seconds to allow window painting
		blockLoading.set(1500);
	}
	
	


	private static void checkDebugStatus() {
		if (System.getProperty("openrocket.debug") != null) {
			System.setProperty("openrocket.log.stdout", "VBOSE");
			System.setProperty("openrocket.log.tracelevel", "VBOSE");
			System.setProperty("openrocket.debug.menu", "true");
			System.setProperty("openrocket.debug.mutexlocation", "true");
			System.setProperty("openrocket.debug.motordigest", "true");
		}
	}
	
	


	private static void runMain(String[] args) {
		
		// Initialize the splash screen with version info
		log.info("Initializing the splash screen");
		Splash.init();
		
		// Setup the uncaught exception handler
		log.info("Registering exception handler");
		ExceptionHandler.registerExceptionHandler();
		
		// Start update info fetching
		final UpdateInfoRetriever updateInfo;
		if (Prefs.getCheckUpdates()) {
			log.info("Starting update check");
			updateInfo = new UpdateInfoRetriever();
			updateInfo.start();
		} else {
			log.info("Update check disabled");
			updateInfo = null;
		}
		
		// Set the best available look-and-feel
		log.info("Setting best LAF");
		GUIUtil.setBestLAF();
		
		// Set tooltip delay time.  Tooltips are used in MotorChooserDialog extensively.
		ToolTipManager.sharedInstance().setDismissDelay(30000);
		
		// Load defaults
		Prefs.loadDefaultUnits();
		
		// Load motors etc.
		log.info("Loading databases");
		loadMotor();
		Databases.fakeMethod();
		
		// Starting action (load files or open new document)
		log.info("Opening main application window");
		if (!handleCommandLine(args)) {
			BasicFrame.newAction();
		}
		
		// Check whether update info has been fetched or whether it needs more time
		log.info("Checking update status");
		checkUpdateStatus(updateInfo);
	}
	
	

	private static void loadMotor() {
		
		log.info("Starting motor loading from " + THRUSTCURVE_DIRECTORY + " in background thread.");
		ThrustCurveMotorSetDatabase db = new ThrustCurveMotorSetDatabase(true) {
			
			@Override
			protected void loadMotors() {
				
				// Block loading until timeout occurs or database is taken into use
				log.info("Blocking motor loading while starting up");
				while (!inUse && blockLoading.addAndGet(-100) > 0) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
				log.info("Blocking ended, inUse=" + inUse + " slowLoadingCount=" + blockLoading.get());
				
				// Start loading
				log.info("Loading motors from " + THRUSTCURVE_DIRECTORY);
				long t0 = System.currentTimeMillis();
				int fileCount;
				int thrustCurveCount;
				
				// Load the packaged thrust curves
				List<Motor> list;
				FileIterator iterator = DirectoryIterator.findDirectory(THRUSTCURVE_DIRECTORY,
								new SimpleFileFilter("", false, "eng", "rse"));
				if (iterator == null) {
					throw new IllegalStateException("Thrust curve directory " + THRUSTCURVE_DIRECTORY +
							"not found, distribution built wrong");
				}
				list = MotorLoaderHelper.load(iterator);
				for (Motor m : list) {
					this.addMotor((ThrustCurveMotor) m);
				}
				fileCount = iterator.getFileCount();
				
				thrustCurveCount = list.size();
				
				// Load the user-defined thrust curves
				for (File file : Prefs.getUserThrustCurveFiles()) {
					// TODO: LOW: This counts a directory as one file
					log.info("Loading motors from " + file);
					list = MotorLoaderHelper.load(file);
					for (Motor m : list) {
						this.addMotor((ThrustCurveMotor) m);
					}
					fileCount++;
					thrustCurveCount += list.size();
				}
				
				long t1 = System.currentTimeMillis();
				
				// Count statistics
				int distinctMotorCount = 0;
				int distinctThrustCurveCount = 0;
				distinctMotorCount = motorSets.size();
				for (ThrustCurveMotorSet set : motorSets) {
					distinctThrustCurveCount += set.getMotorCount();
				}
				log.info("Motor loading done, took " + (t1 - t0) + " ms to load "
						+ fileCount + " files/directories containing "
						+ thrustCurveCount + " thrust curves which contained "
						+ distinctMotorCount + " distinct motors with "
						+ distinctThrustCurveCount + " distinct thrust curves.");
			}
			
		};
		db.startLoading();
		Application.setMotorSetDatabase(db);
	}
	
	

	private static void checkUpdateStatus(final UpdateInfoRetriever updateInfo) {
		if (updateInfo == null)
			return;
		
		int delay = 1000;
		if (!updateInfo.isRunning())
			delay = 100;
		
		final Timer timer = new Timer(delay, null);
		
		ActionListener listener = new ActionListener() {
			private int count = 5;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!updateInfo.isRunning()) {
					timer.stop();
					
					String current = Prefs.getVersion();
					String last = Prefs.getString(Prefs.LAST_UPDATE, "");
					
					UpdateInfo info = updateInfo.getUpdateInfo();
					if (info != null && info.getLatestVersion() != null &&
							!current.equals(info.getLatestVersion()) &&
							!last.equals(info.getLatestVersion())) {
						
						UpdateInfoDialog infoDialog = new UpdateInfoDialog(info);
						infoDialog.setVisible(true);
						if (infoDialog.isReminderSelected()) {
							Prefs.putString(Prefs.LAST_UPDATE, "");
						} else {
							Prefs.putString(Prefs.LAST_UPDATE, info.getLatestVersion());
						}
					}
				}
				count--;
				if (count <= 0)
					timer.stop();
			}
		};
		timer.addActionListener(listener);
		timer.start();
	}
	
	
	/**
	 * Handles arguments passed from the command line.  This may be used either
	 * when starting the first instance of OpenRocket or later when OpenRocket is
	 * executed again while running.
	 * 
	 * @param args	the command-line arguments.
	 * @return		whether a new frame was opened or similar user desired action was
	 * 				performed as a result.
	 */
	public static boolean handleCommandLine(String[] args) {
		
		// Check command-line for files
		boolean opened = false;
		for (String file : args) {
			if (BasicFrame.open(new File(file), null)) {
				opened = true;
			}
		}
		return opened;
	}
	
	

	/**
	 * Check that the JRE is not running headless.
	 */
	private static void checkHead() {
		
		log.info("Checking for graphics head");
		
		if (GraphicsEnvironment.isHeadless()) {
			log.error("Application is headless.");
			System.err.println();
			System.err.println("OpenRocket cannot currently be run without the graphical " +
					"user interface.");
			System.err.println();
			System.exit(1);
		}
		
	}
	
	
	///////////  Logging  ///////////
	
	private static void initializeLogging() {
		DelegatorLogger delegator = new DelegatorLogger();
		
		// Log buffer
		LogLevelBufferLogger buffer = new LogLevelBufferLogger(LOG_BUFFER_LENGTH);
		delegator.addLogger(buffer);
		
		// Check whether to log to stdout/stderr
		PrintStreamLogger printer = new PrintStreamLogger();
		boolean logout = setLogOutput(printer, System.out, System.getProperty(LOG_STDOUT_PROPERTY), null);
		boolean logerr = setLogOutput(printer, System.err, System.getProperty(LOG_STDERR_PROPERTY), LogLevel.ERROR);
		if (logout || logerr) {
			delegator.addLogger(printer);
		}
		
		// Set the loggers
		Application.setLogger(delegator);
		Application.setLogBuffer(buffer);
		
		// Initialize the log for this class
		log = Application.getLogger();
		log.info("Logging subsystem initialized for OpenRocket " + Prefs.getVersion());
		String str = "Console logging output:";
		for (LogLevel l : LogLevel.values()) {
			PrintStream ps = printer.getOutput(l);
			str += " " + l.name() + ":";
			if (ps == System.err) {
				str += "stderr";
			} else if (ps == System.out) {
				str += "stdout";
			} else {
				str += "none";
			}
		}
		str += " (" + LOG_STDOUT_PROPERTY + "=" + System.getProperty(LOG_STDOUT_PROPERTY) +
				" " + LOG_STDERR_PROPERTY + "=" + System.getProperty(LOG_STDERR_PROPERTY) + ")";
		log.info(str);
	}
	
	private static boolean setLogOutput(PrintStreamLogger logger, PrintStream stream, String level, LogLevel defaultLevel) {
		LogLevel minLevel = LogLevel.fromString(level, defaultLevel);
		if (minLevel == null) {
			return false;
		}
		
		for (LogLevel l : LogLevel.values()) {
			if (l.atLeast(minLevel)) {
				logger.setOutput(l, stream);
			}
		}
		return true;
	}
	
	
	///////////  Helper methods  //////////
	
	/**
	 * Presents an error message to the user and exits the application.
	 * 
	 * @param message	an array of messages to present.
	 */
	static void error(String[] message) {
		
		System.err.println();
		System.err.println("Error starting OpenRocket:");
		System.err.println();
		for (int i = 0; i < message.length; i++) {
			System.err.println(message[i]);
		}
		System.err.println();
		

		if (!GraphicsEnvironment.isHeadless()) {
			
			JOptionPane.showMessageDialog(null, message, "Error starting OpenRocket",
					JOptionPane.ERROR_MESSAGE);
			
		}
		
		System.exit(1);
	}
	
	
	/**
	 * Presents the user with a message dialog and asks whether to continue.
	 * If the user does not select "Yes" the the application exits.
	 * 
	 * @param message	the message Strings to show.
	 */
	static void confirm(String[] message) {
		
		if (!GraphicsEnvironment.isHeadless()) {
			
			if (JOptionPane.showConfirmDialog(null, message, "Error starting OpenRocket",
					JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
				System.exit(1);
			}
		}
	}
	
}
