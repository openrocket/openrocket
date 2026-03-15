package info.openrocket.core.database.motor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class ThrustCurveMotorSQLiteDatabaseTest {

	@TempDir
	Path tempDir;

	@Test
	public void testRoundTripPersistsMotors() throws Exception {
		ThrustCurveMotor motorA = new ThrustCurveMotor.Builder()
				.setManufacturer(Manufacturer.getManufacturer("TestCo"))
				.setCode("T100")
				.setCommonName("T100")
				.setDesignation("T100")
				.setDescription("Test motor A")
				.setMotorType(Motor.Type.SINGLE)
				.setStandardDelays(new double[] { 3, 5 })
				.setDiameter(0.03)
				.setLength(0.12)
				.setTimePoints(new double[] { 0.0, 0.5, 1.0 })
				.setThrustPoints(new double[] { 0.0, 10.0, 0.0 })
				.setCGPoints(new CoordinateIF[] {
						new Coordinate(0.05, 0, 0, 0.10),
						new Coordinate(0.05, 0, 0, 0.09),
						new Coordinate(0.05, 0, 0, 0.08)
				})
				.setCaseInfo("Case A")
				.setPropellantInfo("Prop A")
				.setInitialMass(0.10)
				.setDigest("digest-1")
				.build();

		ThrustCurveMotor motorB = new ThrustCurveMotor.Builder()
				.setManufacturer(Manufacturer.getManufacturer("AnotherCo"))
				.setCode("R200")
				.setCommonName("R200")
				.setDesignation("R200")
				.setDescription("Test motor B")
				.setMotorType(Motor.Type.RELOAD)
				.setStandardDelays(new double[] {})
				.setDiameter(0.05)
				.setLength(0.20)
				.setTimePoints(new double[] { 0.0, 0.3, 0.9 })
				.setThrustPoints(new double[] { 0.0, 25.0, 0.0 })
				.setCGPoints(new CoordinateIF[] {
						new Coordinate(0.07, 0, 0, 0.20),
						new Coordinate(0.07, 0, 0, 0.18),
						new Coordinate(0.07, 0, 0, 0.16)
				})
				.setCaseInfo("Case B")
				.setPropellantInfo("Prop B")
				.setInitialMass(0.20)
				.setDigest("digest-2")
				.setAvailability(false)
				.build();

		File dbFile = tempDir.resolve("motors.db").toFile();
		ThrustCurveMotorSQLiteDatabase.writeDatabase(dbFile, List.of(motorA, motorB));

		List<ThrustCurveMotor> loaded = ThrustCurveMotorSQLiteDatabase.readDatabase(dbFile);
		assertEquals(2, loaded.size());

		ThrustCurveMotor loadedA = findByDesignation(loaded, "T100");
		assertEquals("TestCo", loadedA.getManufacturer().getDisplayName());
		assertEquals("T100", loadedA.getCode());
		assertEquals("T100", loadedA.getCommonName());
		assertEquals("T100", loadedA.getDesignation());
		assertEquals("Test motor A", loadedA.getDescription());
		assertEquals(Motor.Type.SINGLE, loadedA.getMotorType());
		assertEquals(0.03, loadedA.getDiameter(), 1.0e-9);
		assertEquals(0.12, loadedA.getLength(), 1.0e-9);
		assertEquals("Case A", loadedA.getCaseInfo());
		assertEquals("Prop A", loadedA.getPropellantInfo());
		assertEquals(0.10, loadedA.getInitialMass(), 1.0e-9);
		assertEquals("digest-1", loadedA.getDigest());
		assertTrue(loadedA.isAvailable());
		assertArrayEquals(new double[] { 3, 5 }, loadedA.getStandardDelays(), 0.0);
		assertArrayEquals(new double[] { 0.0, 0.5, 1.0 }, loadedA.getTimePoints(), 0.0);
		assertArrayEquals(new double[] { 0.0, 10.0, 0.0 }, loadedA.getThrustPoints(), 0.0);
		assertCoordinatesMatch(loadedA.getCGPoints(), new double[] { 0.05, 0.05, 0.05 },
				new double[] { 0.10, 0.09, 0.08 });

		ThrustCurveMotor loadedB = findByDesignation(loaded, "R200");
		assertEquals("AnotherCo", loadedB.getManufacturer().getDisplayName());
		assertEquals(Motor.Type.RELOAD, loadedB.getMotorType());
		assertFalse(loadedB.isAvailable());
		assertArrayEquals(new double[] {}, loadedB.getStandardDelays(), 0.0);
		assertArrayEquals(new double[] { 0.0, 0.3, 0.9 }, loadedB.getTimePoints(), 0.0);
		assertArrayEquals(new double[] { 0.0, 25.0, 0.0 }, loadedB.getThrustPoints(), 0.0);
		assertCoordinatesMatch(loadedB.getCGPoints(), new double[] { 0.07, 0.07, 0.07 },
				new double[] { 0.20, 0.18, 0.16 });
	}

	@Test
	public void testValidateDatabaseMissingColumns() throws Exception {
		Class.forName("org.sqlite.JDBC");
		File dbFile = tempDir.resolve("invalid.db").toFile();
		try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
				Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE meta (key TEXT PRIMARY KEY, value TEXT NOT NULL)");
			stmt.execute("INSERT INTO meta (key, value) VALUES ('schema_version', '1')");
			stmt.execute("CREATE TABLE motors (id INTEGER PRIMARY KEY)");
		}

		SQLException exception = assertThrows(SQLException.class,
				() -> ThrustCurveMotorSQLiteDatabase.validateDatabase(dbFile));
		assertTrue(exception.getMessage().contains("missing required columns"));
	}

	private ThrustCurveMotor findByDesignation(List<ThrustCurveMotor> motors, String designation) {
		for (ThrustCurveMotor motor : motors) {
			if (designation.equals(motor.getDesignation())) {
				return motor;
			}
		}
		fail("Motor not found: " + designation);
		return null;
	}

	private void assertCoordinatesMatch(CoordinateIF[] coordinates, double[] xValues, double[] masses) {
		assertEquals(xValues.length, coordinates.length);
		assertEquals(masses.length, coordinates.length);
		for (int i = 0; i < coordinates.length; i++) {
			assertEquals(xValues[i], coordinates[i].getX(), 1.0e-9);
			assertEquals(0.0, coordinates[i].getY(), 1.0e-9);
			assertEquals(0.0, coordinates[i].getZ(), 1.0e-9);
			assertEquals(masses[i], coordinates[i].getWeight(), 1.0e-9);
		}
	}
}
