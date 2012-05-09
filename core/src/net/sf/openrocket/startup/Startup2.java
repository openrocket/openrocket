package net.sf.openrocket.startup;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;

import net.sf.openrocket.communication.UpdateInfo;
import net.sf.openrocket.communication.UpdateInfoRetriever;
import net.sf.openrocket.database.ComponentPresetDatabase;
import net.sf.openrocket.database.Databases;
import net.sf.openrocket.gui.dialogs.UpdateInfoDialog;
import net.sf.openrocket.gui.main.BasicFrame;
import net.sf.openrocket.gui.main.Splash;
import net.sf.openrocket.gui.main.SwingExceptionHandler;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.util.BuildProperties;

/**
 * The second class in the OpenRocket startup sequence.  This class can assume the
 * Application class to be properly set up, and can use any classes safely.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class Startup2 {
	private static final LogHelper log = Application.getLogger();
	

	private static final String THRUSTCURVE_DIRECTORY = "datafiles/thrustcurves/";
	
	/**
	 * Run when starting up OpenRocket after Application has been set up.
	 * 
	 * @param args	command line arguments
	 */
	static void runMain(final String[] args) throws Exception {
		
		log.info("Starting up OpenRocket version " + BuildProperties.getVersion());
		
		// Check that we're not running headless
		log.info("Checking for graphics head");
		checkHead();
		
		// Check that we're running a good version of a JRE
		log.info("Checking JRE compatibility");
		VersionHelper.checkVersion();
		VersionHelper.checkOpenJDK();
		
		// Run the actual startup method in the EDT since it can use progress dialogs etc.
		log.info("Moving startup to EDT");
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				runInEDT(args);
			}
		});
		
		log.info("Startup complete");
	}
	
	
	/**
	 * Run in the EDT when starting up OpenRocket.
	 * 
	 * @param args	command line arguments
	 */
	private static void runInEDT(String[] args) {
		
		// Initialize the splash screen with version info
		log.info("Initializing the splash screen");
		Splash.init();
		
		// Must be done after localization is initialized
		ComponentPresetDatabase componentPresetDao = new ComponentPresetDatabase();
		ConcurrentComponentPresetDatabaseLoader presetLoader = new ConcurrentComponentPresetDatabaseLoader( componentPresetDao );
		presetLoader.load();
		
		Application.setComponentPresetDao( componentPresetDao );
		
		// Setup the uncaught exception handler
		log.info("Registering exception handler");
		SwingExceptionHandler exceptionHandler = new SwingExceptionHandler();
		Application.setExceptionHandler(exceptionHandler);
		exceptionHandler.registerExceptionHandler();
		
		// Start update info fetching
		final UpdateInfoRetriever updateInfo;
		if ( Application.getPreferences().getCheckUpdates()) {
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
		((SwingPreferences) Application.getPreferences()).loadDefaultUnits();
		
		// Load motors etc.
		log.info("Loading databases");
		
		ConcurrentLoadingThrustCurveMotorSetDatabase motorLoader = new ConcurrentLoadingThrustCurveMotorSetDatabase(THRUSTCURVE_DIRECTORY);
		motorLoader.startLoading();
		Application.setMotorSetDatabase(motorLoader);

		Databases.fakeMethod();
		
		try {
			presetLoader.await();
		} catch ( InterruptedException iex) {
			
		}

		// Starting action (load files or open new document)
		log.info("Opening main application window");
		if (!handleCommandLine(args)) {
			BasicFrame.newAction();
		}
		
		// Check whether update info has been fetched or whether it needs more time
		log.info("Checking update status");
		checkUpdateStatus(updateInfo);
		
	}
	
	
	/**
	 * Check that the JRE is not running headless.
	 */
	private static void checkHead() {
		
		if (GraphicsEnvironment.isHeadless()) {
			log.error("Application is headless.");
			System.err.println();
			System.err.println("OpenRocket cannot currently be run without the graphical " +
					"user interface.");
			System.err.println();
			System.exit(1);
		}
		
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
					
					String current = BuildProperties.getVersion();
					String last = Application.getPreferences().getString(Preferences.LAST_UPDATE, "");
					
					UpdateInfo info = updateInfo.getUpdateInfo();
					if (info != null && info.getLatestVersion() != null &&
							!current.equals(info.getLatestVersion()) &&
							!last.equals(info.getLatestVersion())) {
						
						UpdateInfoDialog infoDialog = new UpdateInfoDialog(info);
						infoDialog.setVisible(true);
						if (infoDialog.isReminderSelected()) {
							Application.getPreferences().putString(Preferences.LAST_UPDATE, "");
						} else {
							Application.getPreferences().putString(Preferences.LAST_UPDATE, info.getLatestVersion());
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
	private static boolean handleCommandLine(String[] args) {
		
		// Check command-line for files
		boolean opened = false;
		for (String file : args) {
			if (BasicFrame.open(new File(file), null)) {
				opened = true;
			}
		}
		return opened;
	}
	
}
