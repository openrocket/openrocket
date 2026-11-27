package info.openrocket.core.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import info.openrocket.core.database.motor.ThrustCurveMotorSQLiteDatabase;
import info.openrocket.core.database.motor.ThrustCurveMotorSet;
import info.openrocket.core.database.motor.ThrustCurveMotorSetDatabase;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;

public class MotorDatabaseLoaderTest extends BaseTestCase {

	private static final String SAMPLE_RASP_ENG = "; User RASP motor\n"
			+ "M1550R 75 560 P 2.500 4.500 AeroTech\n"
			+ "0.1 1200.0\n"
			+ "1.0 1550.0\n"
			+ "2.0 0.0\n";

	private static final String SAMPLE_RSE = "<engine-database>\n"
			+ "  <engine-list>\n"
			+ "    <engine mfg=\"AeroTech\" code=\"K550W\" Type=\"reloadable\" dia=\"54\" len=\"326\" "
			+ "initWt=\"1450\" propWt=\"720\" delays=\"6,10,14\" auto-calc-mass=\"1\" auto-calc-cg=\"1\">\n"
			+ "      <data>\n"
			+ "        <eng-data t=\"0.0\" f=\"0.0\" m=\"1450\" cg=\"163\"/>\n"
			+ "        <eng-data t=\"0.5\" f=\"550.0\" m=\"1100\" cg=\"163\"/>\n"
			+ "        <eng-data t=\"2.0\" f=\"0.0\" m=\"730\" cg=\"163\"/>\n"
			+ "      </data>\n"
			+ "    </engine>\n"
			+ "  </engine-list>\n"
			+ "</engine-database>\n";

	@TempDir
	Path tempDir;

	@Test
	public void testLoadUserDefinedEngRseZipAndSqliteFilesMarksMotorsAsUserDefined() throws Exception {
		Path subDir = Files.createDirectory(tempDir.resolve("nested"));
		File engFile = subDir.resolve("AeroTech_M1550R.eng").toFile();
		Files.writeString(engFile.toPath(), SAMPLE_RASP_ENG, StandardCharsets.ISO_8859_1);

		File rseFile = tempDir.resolve("custom_k550.rse").toFile();
		Files.writeString(rseFile.toPath(), SAMPLE_RSE, StandardCharsets.UTF_8);

		File zipFile = tempDir.resolve("user_pack.zip").toFile();
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
			zos.putNextEntry(new ZipEntry("packed.eng"));
			String zipEng = "; Packed user motor\n"
					+ "J350W 38 300 P 0.350 0.700 AeroTech\n"
					+ "0.1 350.0\n"
					+ "1.5 0.0\n";
			zos.write(zipEng.getBytes(StandardCharsets.ISO_8859_1));
			zos.closeEntry();
		}

		File dbFile = tempDir.resolve("user_motors.db").toFile();
		ThrustCurveMotor dbMotor = new ThrustCurveMotor.Builder()
				.setManufacturer(Manufacturer.getManufacturer("TestCo"))
				.setCode("H100")
				.setCommonName("H100")
				.setDesignation("H100")
				.setDataSource("thrustcurve.org")
				.setMotorType(Motor.Type.SINGLE)
				.setStandardDelays(new double[] { 5 })
				.setDiameter(0.029)
				.setLength(0.15)
				.setTimePoints(new double[] { 0.0, 0.5, 1.0 })
				.setThrustPoints(new double[] { 0.0, 100.0, 0.0 })
				.setCGPoints(new CoordinateIF[] {
						new Coordinate(0.075, 0, 0, 0.25),
						new Coordinate(0.075, 0, 0, 0.20),
						new Coordinate(0.075, 0, 0, 0.15)
				})
				.setInitialMass(0.25)
				.build();
		ThrustCurveMotorSQLiteDatabase.writeDatabase(dbFile, List.of(dbMotor));

		// Reading the SQLite database directly (internal DB path) must not mark motors as user-defined.
		List<ThrustCurveMotor> internalRead = ThrustCurveMotorSQLiteDatabase.readDatabase(dbFile);
		assertEquals(1, internalRead.size());
		assertFalse(internalRead.get(0).isUserDefined());
		assertEquals("", internalRead.get(0).getSourceFile());

		// Loading from the user directory must mark every motor as user-defined with its source file path.
		MotorDatabaseLoader loader = new MotorDatabaseLoader();
		loader.loadUserDefinedMotors(List.of(tempDir.toFile()));
		loader.markAsLoaded();

		ThrustCurveMotorSetDatabase database = loader.getDatabase();
		ThrustCurveMotor loadedEng = findMotorByDesignation(database, "M1550R");
		assertTrue(loadedEng.isUserDefined());
		assertEquals(engFile.getAbsolutePath(), loadedEng.getSourceFile());

		ThrustCurveMotor loadedRse = findMotorByDesignation(database, "K550W");
		assertTrue(loadedRse.isUserDefined());
		assertEquals(rseFile.getAbsolutePath(), loadedRse.getSourceFile());

		ThrustCurveMotor loadedZip = findMotorByDesignation(database, "J350W");
		assertTrue(loadedZip.isUserDefined());
		assertEquals(zipFile.getAbsolutePath(), loadedZip.getSourceFile());

		ThrustCurveMotor loadedDb = findMotorByDesignation(database, "H100");
		assertTrue(loadedDb.isUserDefined());
		assertEquals(dbFile.getAbsolutePath(), loadedDb.getSourceFile());
		assertEquals("thrustcurve.org", loadedDb.getDataSource());
	}

	private static ThrustCurveMotor findMotorByDesignation(ThrustCurveMotorSetDatabase database, String designation) {
		for (ThrustCurveMotorSet set : database.getMotorSets()) {
			for (ThrustCurveMotor motor : set.getMotors()) {
				if (designation.equals(motor.getDesignation())) {
					return motor;
				}
			}
		}
		throw new AssertionError("Motor not found: " + designation);
	}
}
