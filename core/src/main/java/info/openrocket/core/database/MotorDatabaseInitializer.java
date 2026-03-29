package info.openrocket.core.database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.util.FileUtils;

/**
 * Initializes the motor database by copying the bundled database and metadata
 * to the user's motor library directory if necessary.
 * 
 * The initialization logic is:
 * 1. If motors.db doesn't exist in the motor library directory:
 *    - Copy initial_motors.db from resources as motors.db
 *    - Copy metadata.json from resources
 * 2. If motors.db exists:
 *    - Check if metadata.json exists in motor library directory
 *    - If no metadata.json: overwrite db (assume it's outdated)
 *    - If metadata.json exists: compare database_version values
 *      - If library version < resources version: overwrite db and metadata
 *      - If equal or library > resources: do nothing
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class MotorDatabaseInitializer {
	
	private static final Logger log = LoggerFactory.getLogger(MotorDatabaseInitializer.class);
	
	private static final String RESOURCE_PATH = "/datafiles/thrustcurves/";
	private static final String INITIAL_DB_RESOURCE = RESOURCE_PATH + "initial_motors.db";
	private static final String METADATA_RESOURCE = RESOURCE_PATH + "metadata.json";
	
	private static final String MOTORS_DB_FILENAME = "motors.db";
	private static final String METADATA_FILENAME = "metadata.json";
	
	/**
	 * Initialize the motor database in the user's motor library directory.
	 * This should be called before the motor database is loaded.
	 */
	public static void initialize() {
		log.info("Initializing motor database");
		
		File motorLibraryDir = SystemInfo.getOpenRocketMotorLibraryDirectory();
		if (motorLibraryDir == null) {
			log.warn("Motor library directory is null, skipping database initialization");
			return;
		}
		
		File motorsDbFile = new File(motorLibraryDir, MOTORS_DB_FILENAME);
		File metadataFile = new File(motorLibraryDir, METADATA_FILENAME);
		
		if (!motorsDbFile.exists()) {
			// No motors.db exists - copy both files from resources
			log.info("No motors.db found in motor library directory, copying initial database");
			copyInitialDatabase(motorLibraryDir);
		} else {
			// motors.db exists - check metadata
			if (!metadataFile.exists()) {
				// No metadata.json - overwrite the database
				log.info("motors.db exists but no metadata.json, overwriting with initial database");
				copyInitialDatabase(motorLibraryDir);
			} else {
				// Both files exist - compare versions
				checkAndUpdateDatabase(motorLibraryDir, motorsDbFile, metadataFile);
			}
		}
		
		log.info("Motor database initialization complete");
	}
	
	/**
	 * Copy the initial database and metadata from resources to the motor library directory.
	 */
	private static void copyInitialDatabase(File motorLibraryDir) {
		copyResourceToFile(INITIAL_DB_RESOURCE, new File(motorLibraryDir, MOTORS_DB_FILENAME));
		copyResourceToFile(METADATA_RESOURCE, new File(motorLibraryDir, METADATA_FILENAME));
	}
	
	/**
	 * Check version numbers and update the database if the resources version is newer.
	 */
	private static void checkAndUpdateDatabase(File motorLibraryDir, File motorsDbFile, File metadataFile) {
		try {
			long resourceVersion = getResourceDatabaseVersion();
			long libraryVersion = MotorDatabaseMetadataIO.readDatabaseVersion(metadataFile);
			
			log.debug("Resource database version: {}, Library database version: {}", resourceVersion, libraryVersion);
			
			if (libraryVersion < resourceVersion) {
				log.info("Library database version ({}) is older than resources version ({}), updating",
						libraryVersion, resourceVersion);
				copyInitialDatabase(motorLibraryDir);
			} else {
				log.debug("Library database is up to date (version {})", libraryVersion);
			}
		} catch (Exception e) {
			log.warn("Error comparing database versions, skipping update: {}", e.getMessage());
		}
	}
	
	/**
	 * Get the database_version from the bundled metadata.json resource.
	 */
	private static long getResourceDatabaseVersion() throws IOException {
		return MotorDatabaseMetadataIO.readResource(MotorDatabaseInitializer.class, METADATA_RESOURCE).getDatabaseVersion();
	}
	
	/**
	 * Copy a resource file to a target file.
	 */
	private static void copyResourceToFile(String resourcePath, File targetFile) {
		try (InputStream is = MotorDatabaseInitializer.class.getResourceAsStream(resourcePath)) {
			if (is == null) {
				log.error("Could not find resource: {}", resourcePath);
				return;
			}
			
			try (OutputStream os = new FileOutputStream(targetFile)) {
				FileUtils.copy(is, os);
				log.debug("Copied {} to {}", resourcePath, targetFile.getAbsolutePath());
			}
		} catch (IOException e) {
			log.error("Error copying resource {} to {}: {}", resourcePath, targetFile.getAbsolutePath(), e.getMessage());
		}
	}
}
