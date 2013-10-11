package net.sf.openrocket.startup;

import java.awt.Image;
import java.awt.Toolkit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.arch.SystemInfo;
import net.sf.openrocket.arch.SystemInfo.Platform;
import net.sf.openrocket.gui.dialogs.AboutDialog;
import net.sf.openrocket.gui.dialogs.preferences.PreferencesDialog;
import net.sf.openrocket.gui.main.BasicFrame;
import com.apple.eawt.AboutHandler;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.QuitEvent;

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
	 * The handler for the Quit item in the OSX app menu
	 */
	private static final QuitHandler qh = new QuitHandler() {
		@Override
		public void handleQuitRequestWith(final QuitEvent e, final QuitResponse r) {
			BasicFrame.quitAction();
			// if we get here the user canceled
			r.cancelQuit();
		}
	};

	/**
	 * The handler for the About item in the OSX app menu
	 */
	private static final AboutHandler ah = new AboutHandler() {
		@Override
		public void handleAbout(final AboutEvent a) {
			new AboutDialog(null).setVisible(true);
		}
	};

	/**
	 * The handler for the Preferences item in the OSX app menu
	 */
	private static final PreferencesHandler ph = new PreferencesHandler() {
		@Override
		public void handlePreferences(final PreferencesEvent p) {
			PreferencesDialog.showPreferences(null);
		}
	};

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
			final com.apple.eawt.Application osxApp = com.apple.eawt.Application.getApplication();

			if (osxApp == null) {
				// Application is null: Something is wrong, give up on OSX
				// setup.
				throw new NullPointerException("com.apple.eawt.Application.getApplication() returned NULL. "
						+ "Aborting OSX UI Setup.");
			}
			
			// Set handlers
			osxApp.setQuitHandler(qh);
			osxApp.setAboutHandler(ah);
			osxApp.setPreferencesHandler(ph);

			// Set the dock icon to the largest icon
			final Image dockIcon = Toolkit.getDefaultToolkit().getImage(
					SwingStartup.class.getResource(ICON_RSRC));
			osxApp.setDockIconImage(dockIcon);

		} catch (final Throwable t) {
			// None of the preceding is critical to the app,
			// so at worst case log an error and continue
			log.warn("Error setting up OSX UI:", t);
		}
	}

}
