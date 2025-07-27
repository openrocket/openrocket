package info.openrocket.swing.startup;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.plugin.JarMigrationHelper;
import info.openrocket.core.plugin.PluginHelper;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.startup.Application;
import net.miginfocom.layout.LayoutUtil;
import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.arch.SystemInfo.Platform;
import info.openrocket.core.communication.UpdateInfo;
import info.openrocket.core.communication.UpdateInfoRetriever;
import info.openrocket.core.communication.UpdateInfoRetriever.ReleaseStatus;
import info.openrocket.core.communication.WelcomeInfoRetriever;
import info.openrocket.core.database.Databases;
import info.openrocket.swing.gui.dialogs.UpdateInfoDialog;
import info.openrocket.swing.gui.dialogs.WelcomeDialog;
import info.openrocket.swing.gui.main.BasicFrame;
import info.openrocket.swing.gui.main.Splash;
import info.openrocket.swing.gui.main.SwingExceptionHandler;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.logging.LoggingSystemSetup;
import info.openrocket.swing.logging.PrintStreamToSLF4J;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.util.BuildProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import static info.openrocket.core.file.rasaero.export.BodyTubeDTOAdapter.trans;

/**
 * Start the OpenRocket swing application.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SwingStartup {
	private final static Logger log = LoggerFactory.getLogger(SwingStartup.class);
	
	/**
	 * OpenRocket startup main method.
	 */
	public static void main(final String[] args) throws Exception {

		// Check for "openrocket.debug" property before anything else
		checkDebugStatus();

		if (System.getProperty("openrocket.debug.layout") != null) {
			LayoutUtil.setGlobalDebugMillis(100);
		}
		
		// Initialize logging first so we can use it
		initializeLogging();
		log.info("Starting up OpenRocket version {}", BuildProperties.getVersion());

		// Check JRE version
		boolean ignoreJRE = System.getProperty("openrocket.ignore-jre") != null;
		if (!ignoreJRE && !checkJREVersion()) {
			return;
		}
		
		// Check that we're not running headless
		log.info("Checking for graphics head");
		checkHead();
		
		// If running on a MAC set up OSX UI Elements.
		if (SystemInfo.getPlatform() == Platform.MAC_OS) {
			OSXSetup.setupOSX();
		}
		
		final SwingStartup runner = new SwingStartup();
		
		// Run the actual startup method in the EDT since it can use progress dialogs etc.
		log.info("Moving startup to EDT");
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				runner.runInEDT(args);
			}
		});
		
		log.info("Startup complete");
		
	}

	/**
	 * Checks whether the Java Runtime Engine version is supported.
	 *
	 * @return true if the JRE is supported, false if not
	 */
	private static boolean checkJREVersion() {
		String JREVersion = System.getProperty("java.version");
		if (JREVersion != null) {
			try {
				// We're only interested in the big decimal part of the JRE version
				int version = Integer.parseInt(JREVersion.split("\\.")[0]);
				if (IntStream.of(Application.SUPPORTED_JRE_VERSIONS).noneMatch(c -> c == version)) {
					String title = "Unsupported Java version";
					String message1 = "Unsupported Java version: %s";
					String message2 = "Supported version(s): %s";
					String message3 = "Please change the Java Runtime Environment version or install OpenRocket using a packaged installer.";

					StringBuilder message = new StringBuilder();
					message.append(String.format(message1, JREVersion));
					message.append("\n");
					String[] supported = Arrays.stream(Application.SUPPORTED_JRE_VERSIONS)
							.mapToObj(String::valueOf)
							.toArray(String[]::new);
					message.append(String.format(message2, String.join(", ", supported)));
					message.append("\n\n");
					message.append(message3);

					JOptionPane.showMessageDialog(null, message.toString(),
							title, JOptionPane.ERROR_MESSAGE);
					return false;
				}
			} catch (NumberFormatException e) {
				log.warn("Malformed JRE version - " + JREVersion);
			}
		}
		return true;
	}

	/**
	 * Set proper system properties if openrocket.debug is defined.
	 */
	private static void checkDebugStatus() {
		if (System.getProperty("openrocket.debug") != null) {
			setPropertyIfNotSet("openrocket.debug.menu", "true");
			setPropertyIfNotSet("openrocket.debug.mutexlocation", "true");
			setPropertyIfNotSet("openrocket.debug.motordigest", "true");
			setPropertyIfNotSet("jogl.debug", "all");
		}
	}
	
	private static void setPropertyIfNotSet(String key, String value) {
		if (System.getProperty(key) == null) {
			System.setProperty(key, value);
		}
	}
	
	/**
	 * Initializes the logging system.
	 */
	public static void initializeLogging() {
		LoggingSystemSetup.setupLoggingAppender();
		
		if (System.getProperty("openrocket.debug") != null) {
			LoggingSystemSetup.addConsoleAppender();
		}
		//Replace System.err with a PrintStream that logs lines to DEBUG, or VBOSE if they are indented.
		//If debug info is not being output to the console then the data is both logged and written to
		//stderr.
		final PrintStream stdErr = System.err;
		System.setErr(PrintStreamToSLF4J.getPrintStream("STDERR", stdErr));
	}
	
	/**
	 * Run in the EDT when starting up OpenRocket.
	 *
	 * @param args	command line arguments
	 */
	private void runInEDT(String[] args) {
		
		// Initialize the splash screen with version info
		log.info("Initializing the splash screen");
		Splash.init();
		
		// Setup the uncaught exception handler
		log.info("Registering exception handler");
		SwingExceptionHandler exceptionHandler = new SwingExceptionHandler();
		Application.setExceptionHandler(exceptionHandler);
		exceptionHandler.registerExceptionHandler();
		
		// Load motors etc.
		log.info("Loading databases");
		
		GuiModule guiModule = new GuiModule();
		Module pluginModule = new PluginModule();
		Injector injector = Guice.createInjector(guiModule, pluginModule);
		Application.setInjector(injector);
		
		guiModule.startLoader();
		
		// Start update info fetching
		final UpdateInfoRetriever updateRetriever = startUpdateChecker();
		
		// Set the look-and-feel
		log.info("Setting LAF");
		String cmdLAF = System.getProperty("openrocket.laf");
		if (cmdLAF != null) {
			log.info("Setting cmd line LAF '{}'", cmdLAF);
			ApplicationPreferences prefs = Application.getPreferences();
			prefs.setUITheme(UITheme.Themes.valueOf(cmdLAF));
		}
		GUIUtil.applyLAF();
		
		// Set tooltip delay time.  Tooltips are used in MotorChooserDialog extensively.
		ToolTipManager.sharedInstance().setDismissDelay(30000);
		
		// Load defaults
		((SwingPreferences) Application.getPreferences()).loadDefaultUnits();
		((SwingPreferences) Application.getPreferences()).loadDefaultComponentMaterials();

		Databases.fakeMethod();

		// Set up the OSX file open handler here so that it can handle files that are opened when OR is not yet running.
		if (SystemInfo.getPlatform() == Platform.MAC_OS) {
			OSXSetup.setupOSXOpenFileHandler();
		}
		
		// Starting action (load files or open new document)
		log.info("Opening main application window");
		if (!handleCommandLine(args)) {
			BasicFrame startupFrame = BasicFrame.reopen();
			BasicFrame.setStartupFrame(startupFrame);
			showWelcomeDialog();
		}
		
		// Check whether update info has been fetched or whether it needs more time
		log.info("Checking update status");
		checkUpdateStatus(updateRetriever);

		// Check if plugins were migrated, if so, display a message
		final List<File> files = PluginHelper.getPluginJars();
		final Translator trans = Application.getTranslator();
		files.stream()
				.filter(f -> f.getName().contains(JarMigrationHelper.MIGRATION_SUFFIX + JarMigrationHelper.NEW_MIGRATION_SUFFIX))
				.forEach(f -> displayPluginMigratedMessage(f, trans));
	}

	private static void displayPluginMigratedMessage(File f, Translator trans) {
		File newFile = new File(f.getAbsolutePath().replace(JarMigrationHelper.NEW_MIGRATION_SUFFIX, ""));
		f.renameTo(newFile);
		String message = String.format(trans.get("SwingStartup.pluginMigrated"),
				newFile.getName().replace(JarMigrationHelper.MIGRATION_SUFFIX, ""), newFile);
		JOptionPane.showMessageDialog(null, message, "Plugin migrated", JOptionPane.INFORMATION_MESSAGE);
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

	public static UpdateInfoRetriever startUpdateChecker() {
		final UpdateInfoRetriever updateRetriever;
		if (Application.getPreferences().getCheckUpdates()) {
			log.info("Starting update check");
			updateRetriever = new UpdateInfoRetriever();
			updateRetriever.startFetchUpdateInfo();
		} else {
			log.info("Update check disabled");
			updateRetriever = null;
		}
		return updateRetriever;
	}
	
	public static void checkUpdateStatus(final UpdateInfoRetriever updateRetriever) {
		if (updateRetriever == null)
			return;
		
		int delay = 1000;
		if (!updateRetriever.isRunning())
			delay = 100;
		
		final Timer timer = new Timer(delay, null);
		
		ActionListener listener = new ActionListener() {
			private int count = 5;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!updateRetriever.isRunning()) {
					timer.stop();

					final SwingPreferences preferences = (SwingPreferences) Application.getPreferences();
					UpdateInfo info = updateRetriever.getUpdateInfo();

					// Only display something when an update is found
					boolean checkAllUpdates = System.getProperty("openrocket.debug.checkAllVersionUpdates") != null;
					if (info != null && info.getException() == null && info.getReleaseStatus() == ReleaseStatus.OLDER &&
							(checkAllUpdates || !preferences.getIgnoreUpdateVersions().contains(info.getLatestRelease().getReleaseName()))) {
						UpdateInfoDialog infoDialog = new UpdateInfoDialog(info);
						infoDialog.setVisible(true);
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
	 * Shows a welcome dialog displaying the release notes for this build version.
	 */
	public static void showWelcomeDialog() {
		// Don't show if this build version is ignored
		if (Application.getPreferences().getIgnoreWelcome(BuildProperties.getVersion())) {
			log.debug("Welcome dialog ignored");
			return;
		}

		// Fetch this version's release notes
		String releaseNotes;
		try {
			releaseNotes = WelcomeInfoRetriever.retrieveWelcomeInfo();
		} catch (IOException e) {
			log.error("Error retrieving welcome info", e);
			return;
		}
		if (releaseNotes == null) {
			log.debug("No release notes found");
			return;
		}

		// Show the dialog
		WelcomeDialog dialog = new WelcomeDialog(releaseNotes);
		dialog.setVisible(true);
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
	private boolean handleCommandLine(String[] args) {
		
		// Check command-line for files
		boolean opened = false;
		for (String file : args) {
			if (BasicFrame.open(new File(file), null) != null) {
				opened = true;
			}
		}
		return opened;
	}
	
}
