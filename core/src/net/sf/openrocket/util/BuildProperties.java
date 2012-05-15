package net.sf.openrocket.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.MissingResourceException;
import java.util.Properties;

public class BuildProperties {

	private static final Properties PROPERTIES;
	private static final String BUILD_VERSION;
	private static final String BUILD_SOURCE;
	private static final boolean DEFAULT_CHECK_UPDATES;
	
	/**
	 * Return the OpenRocket version number.
	 */
	public static String getVersion() {
		return BUILD_VERSION;
	}
	
	/**
	 * Return the OpenRocket build source (e.g. "default" or "Debian")
	 */
	public static String getBuildSource() {
		return BUILD_SOURCE;
	}
	
	public static boolean getDefaultCheckUpdates() {
		return DEFAULT_CHECK_UPDATES;
	}
	
	static {
		try {
			InputStream is = BuildProperties.class.getClassLoader().getResourceAsStream("build.properties");
			if (is == null) {
				throw new MissingResourceException(
						"build.properties not found, distribution built wrong" +
								"   classpath:" + System.getProperty("java.class.path"),
						"build.properties", "build.version");
			}
			
			PROPERTIES = new Properties();
			PROPERTIES.load(is);
			is.close();
			
			String version = PROPERTIES.getProperty("build.version");
			if (version == null) {
				throw new MissingResourceException(
						"build.version not found in property file",
						"build.properties", "build.version");
			}
			BUILD_VERSION = version.trim();
			
			BUILD_SOURCE = PROPERTIES.getProperty("build.source");
			if (BUILD_SOURCE == null) {
				throw new MissingResourceException(
						"build.source not found in property file",
						"build.properties", "build.source");
			}
			
			String value = PROPERTIES.getProperty("build.checkupdates");
			if (value != null)
				DEFAULT_CHECK_UPDATES = Boolean.parseBoolean(value);
			else
				DEFAULT_CHECK_UPDATES = true;
			
		} catch (IOException e) {
			throw new MissingResourceException(
					"Error reading build.properties",
					"build.properties", "build.version");
		}
	}

}
