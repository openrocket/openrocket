package net.sf.openrocket.startup;

import java.awt.GraphicsEnvironment;
import java.util.Locale;

import javax.swing.JOptionPane;

import net.sf.openrocket.logging.LogHelper;

public class VersionHelper {
	
	private static final LogHelper log = Application.getLogger();
	
	private static final int REQUIRED_MAJOR_VERSION = 1;
	private static final int REQUIRED_MINOR_VERSION = 6;
	
	// OpenJDK 1.6.0_0-b16 is known to work, 1.6.0_0-b12 does not
	private static final String BAD_OPENJDK_VERSION = "^1.6.0_0-b([0-9]|1[1-5])$";
	
	
	/**
	 * Check that the JRE version is high enough.
	 */
	static void checkVersion() {
		
		String[] version = System.getProperty("java.specification.version", "").split("\\.");
		
		String jreName = System.getProperty("java.vm.name", "(unknown)");
		String jreVersion = System.getProperty("java.runtime.version", "(unknown)");
		String jreVendor = System.getProperty("java.vendor", "(unknown)");
		
		log.info("Running JRE " + jreName + " version " + jreVersion + " by " + jreVendor);
		
		int major, minor;
		
		try {
			major = Integer.parseInt(version[0]);
			minor = Integer.parseInt(version[1]);
			
			if (major < REQUIRED_MAJOR_VERSION ||
					(major == REQUIRED_MAJOR_VERSION && minor < REQUIRED_MINOR_VERSION)) {
				error(new String[] { "Java SE version 6 is required to run OpenRocket.",
						"You are currently running " + jreName + " version " +
								jreVersion + " by " + jreVendor });
			}
			
		} catch (RuntimeException e) {
			
			confirm(new String[] { "The Java version in use could not be detected.",
					"OpenRocket requires at least Java SE 6.",
					"Continue anyway?" });
			
		}
		
	}
	
	/**
	 * Check whether OpenJDK is being used, and if it is warn the user about
	 * problems and confirm whether to continue.
	 */
	static void checkOpenJDK() {
		
		if (System.getProperty("java.runtime.name", "").toLowerCase(Locale.ENGLISH).indexOf("icedtea") >= 0 ||
				System.getProperty("java.vm.name", "").toLowerCase(Locale.ENGLISH).indexOf("openjdk") >= 0) {
			
			String jreName = System.getProperty("java.vm.name", "(unknown)");
			String jreVersion = System.getProperty("java.runtime.version", "(unknown)");
			String jreVendor = System.getProperty("java.vendor", "(unknown)");
			
			if (jreVersion.matches(BAD_OPENJDK_VERSION)) {
				
				confirm(new String[] { "Old versions of OpenJDK are known to have problems " +
						"running OpenRocket.",
						" ",
						"You are currently running " + jreName + " version " +
								jreVersion + " by " + jreVendor,
						"Do you want to continue?" });
			}
		}
	}
	
	
	
	///////////  Helper methods  //////////
	
	/**
	 * Presents an error message to the user and exits the application.
	 * 
	 * @param message	an array of messages to present.
	 */
	private static void error(String[] message) {
		
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
	private static void confirm(String[] message) {
		
		if (!GraphicsEnvironment.isHeadless()) {
			
			if (JOptionPane.showConfirmDialog(null, message, "Error starting OpenRocket",
					JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
				System.exit(1);
			}
		}
	}
}
