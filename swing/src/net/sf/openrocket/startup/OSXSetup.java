package net.sf.openrocket.startup;

import java.awt.*;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.OpenFilesHandler;
import java.awt.desktop.PreferencesHandler;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.AppReopenedListener;

import net.sf.openrocket.communication.UpdateInfoRetriever;
import net.sf.openrocket.gui.util.DummyFrameMenuOSX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.arch.SystemInfo;
import net.sf.openrocket.arch.SystemInfo.Platform;
import net.sf.openrocket.gui.dialogs.AboutDialog;
import net.sf.openrocket.gui.dialogs.preferences.PreferencesDialog;
import net.sf.openrocket.gui.main.BasicFrame;

import javax.swing.*;

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
			// Put the menu bar at the top of the screen
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			// Set the name in the menu
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_NAME);

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

			// Set the foreground of active tabs to black; there was a bug where you had a white background and white foreground
			UIManager.put("TabbedPane.foreground", Color.black);

			// Set the select foreground for buttons to not be black on a blue background
			UIManager.put("ToggleButton.selectForeground", Color.WHITE);

		} catch (final Throwable t) {
			// None of the preceding is critical to the app,
			// so at worst case log an error and continue
			log.warn("Error setting up OSX UI:", t);
		}
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
