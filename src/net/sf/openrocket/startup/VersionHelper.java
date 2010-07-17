package net.sf.openrocket.startup;

import net.sf.openrocket.logging.LogHelper;

public class VersionHelper {
	
	private static final LogHelper logger = Application.getLogger();

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
		
		logger.info("Running JRE " + jreName + " version " + jreVersion + " by " + jreVendor);
		
		int major, minor;
	
		try {
			major = Integer.parseInt(version[0]);
			minor = Integer.parseInt(version[1]);
			
			if (major < REQUIRED_MAJOR_VERSION || 
					(major == REQUIRED_MAJOR_VERSION && minor < REQUIRED_MINOR_VERSION)) {
				Startup.error(new String[] {"Java SE version 6 is required to run OpenRocket.",
						"You are currently running " + jreName + " version " +
						jreVersion + " by " + jreVendor});
			}
			
		} catch (RuntimeException e) {
			
			Startup.confirm(new String[] {"The Java version in use could not be detected.",
					"OpenRocket requires at least Java SE 6.",
					"Continue anyway?"});
			
		}
		
	}

	/**
	 * Check whether OpenJDK is being used, and if it is warn the user about
	 * problems and confirm whether to continue.
	 */
	static void checkOpenJDK() {
		
		if (System.getProperty("java.runtime.name", "").toLowerCase().indexOf("icedtea")>=0 ||
				System.getProperty("java.vm.name", "").toLowerCase().indexOf("openjdk")>=0) {
	
			String jreName = System.getProperty("java.vm.name", "(unknown)");
			String jreVersion = System.getProperty("java.runtime.version", "(unknown)");
			String jreVendor = System.getProperty("java.vendor", "(unknown)");
	
			if (jreVersion.matches(BAD_OPENJDK_VERSION)) {
			
				Startup.confirm(new String[] {"Old versions of OpenJDK are known to have problems " +
						"running OpenRocket.",
						" ",
						"You are currently running " + jreName + " version " +
						jreVersion + " by " + jreVendor,
						"Do you want to continue?"});
			}
		}
	}

}
