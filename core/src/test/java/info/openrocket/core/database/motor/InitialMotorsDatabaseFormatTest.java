package info.openrocket.core.database.motor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import info.openrocket.core.motor.ThrustCurveMotor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class InitialMotorsDatabaseFormatTest {

	@TempDir
	Path tempDir;

	@Test
	public void testInitialMotorsDbIsReadable() throws Exception {
		File dbFile = tempDir.resolve("initial_motors.db").toFile();
		try (InputStream in = InitialMotorsDatabaseFormatTest.class
				.getResourceAsStream("/datafiles/thrustcurves/initial_motors.db")) {
			assertNotNull(in, "initial_motors.db resource not found on classpath");
			Files.copy(in, dbFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

		ThrustCurveMotorSQLiteDatabase.validateDatabase(dbFile);
		List<ThrustCurveMotor> motors = ThrustCurveMotorSQLiteDatabase.readDatabase(dbFile);
		assertFalse(motors.isEmpty(), "initial_motors.db should contain motors");
	}
}

