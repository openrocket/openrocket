package info.openrocket.core.arch;

import java.io.File;
import java.util.Locale;

import info.openrocket.core.util.BugException;

public class SystemInfo {

	/**
	 * Enumeration of supported operating systems.
	 * 
	 * @see <a href="http://lopica.sourceforge.net/os.html">JNLP os and arch Value
	 *      Collection</a>
	 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
	 */
	public enum Platform {
		WINDOWS,
		MAC_OS,
		UNIX
	}

	/**
	 * Return the current operating system.
	 * 
	 * @return the operating system of the current system.
	 */
	public static Platform getPlatform() {
		String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

		if (os.contains("win")) {
			return Platform.WINDOWS;
		} else if (os.contains("mac")) {
			return Platform.MAC_OS;
		} else {
			/*
			 * Assume UNIX otherwise, e.g. "Linux", "Solaris", "AIX" etc.
			 */
			return Platform.UNIX;
		}
	}

	/**
	 * Returns true if the OpenRocket is running a confined manner that may
	 * require alternative behaviors and experiences.
	 * 
	 * Note: future versions may return further confinement information and
	 * this interface is subject to change.
	 * 
	 * @return true if the system is running in a confined manner, false
	 *         otherwise
	 */
	public static boolean isConfined() {
		return switch (getPlatform()) {
			case UNIX -> (System.getenv("SNAP_VERSION") != null);
			default -> false;
		};
	}

	/**
	 * Return the application data directory of this user. The location depends
	 * on the current platform.
	 * <p>
	 * The directory will not be created by this method.
	 * 
	 * @return the application directory for OpenRocket
	 */
	public static File getUserApplicationDirectory() {
		final String homeDir = System.getProperty("user.home");
		final File dir;

		switch (getPlatform()) {
			case WINDOWS:
				String appdata = System.getenv("APPDATA");
				if (appdata != null) {
					dir = new File(appdata, "OpenRocket/");
				} else {
					dir = new File(homeDir, "OpenRocket/");
				}
				break;

			case MAC_OS:
				dir = new File(homeDir, "Library/Application Support/OpenRocket/");
				break;

			case UNIX:
				dir = new File(homeDir, ".openrocket/");
				break;

			default:
				throw new BugException("Not implemented for platform " + getPlatform());
		}

		return dir;
	}

}
