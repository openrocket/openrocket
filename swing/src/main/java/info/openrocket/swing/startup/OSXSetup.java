package info.openrocket.swing.startup;

import java.awt.Desktop;
import java.awt.Image;
import java.awt.Taskbar;
import java.awt.Toolkit;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.OpenFilesHandler;
import java.awt.desktop.PreferencesHandler;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.AppReopenedListener;

import info.openrocket.core.communication.UpdateInfoRetriever;
import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.arch.SystemInfo.Platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.swing.gui.dialogs.AboutDialog;
import info.openrocket.swing.gui.dialogs.preferences.PreferencesDialog;
import info.openrocket.swing.gui.main.BasicFrame;


/**
 * Static code for initialization of OSX UI Elements: Menu, Icon, Name and
 * Application menu handlers.
 * 
 * @author Bill Kuker <bkuker@billkuker.com>
 * 
 */
final class OSXSetup {
	private static final Logger log = LoggerFactory.getLogger(OSXSetup.class);

	// The name in the app menu
	private static final String APP_NAME = "OpenRocket";
	
	// The image resource to use for the Dock Icon
	private static final String ICON_RSRC = "/pix/icon/icon-256.png";

	/**
	 * The handler for file associations
	 */
	public static final OpenFilesHandler OPEN_FILE_HANDLER = (e) -> {
		log.info("Opening file from association: " + e.getFiles().get(0));
		BasicFrame.open(e.getFiles().get(0), BasicFrame.lastFrameInstance);
	};
	
	/**
	 * The handler for the Quit item in the OSX app menu
	 */
	private static final QuitHandler QUIT_HANDLER = (e, r) -> {
		BasicFrame.quitAction();
		// if we get here the user canceled
		r.cancelQuit();
	};

	private static final AppReopenedListener APP_REOPENED_HANDLER = (e) -> {
		if (BasicFrame.isFramesEmpty()) {
			log.info("App re-opened");
			BasicFrame.reopen();

			// Also check for software updates
			final UpdateInfoRetriever updateRetriever = SwingStartup.startUpdateChecker();
			SwingStartup.checkUpdateStatus(updateRetriever);
		}
	};

	/**
	 * The handler for the About item in the OSX app menu
	 */
	private static final AboutHandler ABOUT_HANDLER = a -> new AboutDialog(BasicFrame.lastFrameInstance).setVisible(true);

	/**
	 * The handler for the Preferences item in the OSX app menu
	 */
	private static final PreferencesHandler PREFERENCES_HANDLER = p -> PreferencesDialog.showPreferences(BasicFrame.lastFrameInstance);

	/**
	 * Sets up the Application's Icon, Name, Menu and some menu item handlers
	 * for Apple OSX. This method needs to be called before other AWT or Swing
	 * things happen, or parts will fail to work.
	 * 
	 * This function should fail gracefully if the OS is wrong.
	 */
	static void setupOSX() {
		if (SystemInfo.getPlatform() != Platform.MAC_OS) {
			log.warn("Attempting to set up OSX UI on non-MAC_OS");
		}
		log.debug("Setting up OSX UI Elements");
		try {
			setupOSXProperties();

			// This line must come AFTER the above properties are set, otherwise
			// the name will not appear
			final Desktop osxDesktop = Desktop.getDesktop();

			if (osxDesktop == null) {
				// Application is null: Something is wrong, give up on OS setup
				throw new NullPointerException("com.apple.eawt.Application.getApplication() returned NULL. "
						+ "Aborting OSX UI Setup.");
			}
			
			// Set handlers
			osxDesktop.setAboutHandler(ABOUT_HANDLER);
			osxDesktop.setPreferencesHandler(PREFERENCES_HANDLER);
			osxDesktop.setQuitHandler(QUIT_HANDLER);
			osxDesktop.addAppEventListener(APP_REOPENED_HANDLER);

			// Set the dock icon to the largest icon
			final Image dockIcon = Toolkit.getDefaultToolkit().getImage(
					SwingStartup.class.getResource(ICON_RSRC));
			final Taskbar osxTaskbar = Taskbar.getTaskbar();
			osxTaskbar.setIconImage(dockIcon);

		} catch (final Throwable t) {
			// None of the preceding is critical to the app,
			// so at worst case log an error and continue
			log.warn("Error setting up OSX UI:", t);
		}
	}

	public static void setupOSXProperties() {
		if (SystemInfo.getPlatform() != Platform.MAC_OS) {
			log.warn("Attempting to set up OSX properties on non-MAC_OS");
			return;
		}

		// Put the menu bar at the top of the screen
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		// Fix window title bar color
		System.setProperty("apple.awt.application.appearance", "system");
		// Set the name in the menu
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_NAME);
	}

	/**
	 * Sets up the open file handler, which handles file association on macOS.
	 */
	public static void setupOSXOpenFileHandler() {
		if (SystemInfo.getPlatform() != Platform.MAC_OS) {
			log.warn("Attempting to set up OSX file handler on non-MAC_OS");
		}
		final Desktop osxDesktop = Desktop.getDesktop();
		if (osxDesktop == null) {
			// Application is null: Something is wrong, give up on OS setup
			throw new NullPointerException("com.apple.eawt.Application.getApplication() returned NULL. "
					+ "Aborting OSX UI Setup.");
		}
		osxDesktop.setOpenFileHandler(OPEN_FILE_HANDLER);
	}

}
