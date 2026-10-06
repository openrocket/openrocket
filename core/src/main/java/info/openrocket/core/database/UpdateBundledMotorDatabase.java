package info.openrocket.core.database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Build tool that replaces the bundled motor database (initial_motors.db and metadata.json) with the published one when
 * the published one is newer. The download is verified and validated by {@link MotorDatabaseRemoteUpdater}, exactly as
 * when the application updates its motor database.
 *
 * Usage: UpdateBundledMotorDatabase &lt;bundled motor database directory&gt;
 */
public class UpdateBundledMotorDatabase {
	private static final String INITIAL_DB_FILENAME = "initial_motors.db";
	private static final String MOTORS_DB_FILENAME = "motors.db";
	private static final String METADATA_FILENAME = "metadata.json";

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("Usage: UpdateBundledMotorDatabase <bundled motor database directory>");
			System.exit(1);
		}
		File bundledDir = new File(args[0]);
		if (!new File(bundledDir, INITIAL_DB_FILENAME).isFile()) {
			throw new IOException(INITIAL_DB_FILENAME + " not found in " + bundledDir.getAbsolutePath());
		}

		MotorDatabaseRemoteUpdater updater = new MotorDatabaseRemoteUpdater();
		MotorDatabaseMetadata remoteMetadata = updater.fetchRemoteMetadata();
		if (!updater.isRemoteNewer(bundledDir, remoteMetadata)) {
			System.out.println("The bundled motor database is up to date (published database_version="
					+ remoteMetadata.getDatabaseVersion() + ").");
			return;
		}

		Path downloadDir = Files.createTempDirectory("openrocket-motor-database");
		updater.installRemoteDatabase(downloadDir.toFile(), remoteMetadata);
		Files.copy(downloadDir.resolve(MOTORS_DB_FILENAME), bundledDir.toPath().resolve(INITIAL_DB_FILENAME),
				StandardCopyOption.REPLACE_EXISTING);
		Files.copy(downloadDir.resolve(METADATA_FILENAME), bundledDir.toPath().resolve(METADATA_FILENAME),
				StandardCopyOption.REPLACE_EXISTING);
		System.out.println("Updated the bundled motor database to database_version="
				+ remoteMetadata.getDatabaseVersion() + ".");
	}
}
