package net.sf.openrocket.gui.figure3d;

import java.awt.SplashScreen;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.media.opengl.GLProfile;

import net.sf.openrocket.arch.SystemInfo;
import net.sf.openrocket.arch.SystemInfo.Platform;
import net.sf.openrocket.gui.main.Splash;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;

public class OpenGLUtils {
	private static final LogHelper log = Application.getLogger();

	/**
	 * Keep the state of 3D consistent for the entire launch, so if user enables
	 * 3d and opens a new window it stays disabled.
	 */
	private static Boolean enabledThisLaunch = null;

	/**
	 * set true in enterDangerZone, false in exitDangerZone allows the exit
	 * function to return immediately if called extra times
	 */
	private static boolean inTheDangerZone = false;

	/**
	 * Call this method as early as possible.
	 */
	public static void earlyInitialize() {
		// If crash detection fails this will allow someone to disable
		// the 3d preference from the command line.
		if (System.getProperty("openrocket.3d.disable") != null) {
			Application.getPreferences().set3dEnabled(false);
		}

		if (!is3dEnabled()) {
			log.debug("OpenGL is disabled");
		} else {
			log.debug("Initializing OpenGL");
			enterDangerZone();
			if (SystemInfo.getPlatform() == Platform.UNIX) {
				log.debug("Dismissing splash screen (Linux/Java/JOGL bug)");
				// Fixes a linux / X bug: Splash must be closed before GL Init
				SplashScreen splash = Splash.getSplashScreen();
				if (splash != null && splash.isVisible())
					splash.close();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// Intentionally Ignored
				}
			}
			log.debug("Calling GLProfile.initSingleton()");
			GLProfile.initSingleton();
			exitDangerZone();
		}
	}

	/**
	 * Returns true if 3d functions are enabled
	 * 
	 * @return
	 */
	static boolean is3dEnabled() {
		if (enabledThisLaunch == null)
			enabledThisLaunch = new Boolean(Application.getPreferences().is3dEnabled());
		return enabledThisLaunch.booleanValue();
	}

	/**
	 * Signal that we are about to do something that can cause a GL crash. If
	 * exitDangerZone is not called after this the 3D user preference will be
	 * disabled at the next startup.
	 */
	static void enterDangerZone() {
		log.verbose("Entering GL DangerZone");
		inTheDangerZone = true;
		Application.getPreferences().set3dEnabled(false);
		try {
			Preferences.userRoot().flush();
		} catch (BackingStoreException e) {
			log.warn("Unable to flush prefs in enterDangerZone()");
		}
	}

	/**
	 * Signal that some GL operation has succeeded. the UserPreference will be
	 * left alone.
	 * 
	 * Safe to call when not in the danger-zone. Safe to call quite often
	 */
	static void exitDangerZone() {
		if (!inTheDangerZone)
			return;
		inTheDangerZone = false;
		log.verbose("Exiting GL DangerZone");
		Application.getPreferences().set3dEnabled(true);
		try {
			Preferences.userRoot().flush();
		} catch (BackingStoreException e) {
			log.warn("Unable to flush prefs in exitDangerZone()");
		}
	}
	
	/**
	 * Seriously never call this it will segfault the JVM.
	@Deprecated
	static void segfault(){
		try {
			log.error("Segfaulting!");
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			Unsafe unsafe =  (Unsafe)f.get(null);
			unsafe.putAddress(0, 0);
		} catch (Exception e) {
			log.error("Unable to segfault", e);
		}

	}
	**/
}
