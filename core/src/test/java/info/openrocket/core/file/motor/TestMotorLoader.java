package info.openrocket.core.file.motor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;

import org.junit.jupiter.api.Test;

public class TestMotorLoader {

	private static final String DIGEST1 = "e523030bc96d5e63313b5723aaea267d";
	private static final String DIGEST2 = "6a41f0f10b7283793eb0e6b389753729";
	private static final String DIGEST3 = "e3164a735f9a50500f2725f0a33d246b";

	@Test
	public void testGeneralMotorLoader() throws IOException {
		MotorLoader loader = new GeneralMotorLoader();

		test(loader, "/file/motor/test1.eng", DIGEST1);
		test(loader, "/file/motor/test2.rse", DIGEST2);
		test(loader, "/file/motor/test.zip", DIGEST2, DIGEST1);
		test(loader, "/file/motor/test3.rse", DIGEST3);
	}

	@Test
	public void testRASPMotorLoader() throws IOException {
		test(new RASPMotorLoader(), "/file/motor/test1.eng", DIGEST1);
	}

	@Test
	public void testRockSimMotorLoader() throws IOException {
		test(new RockSimMotorLoader(), "/file/motor/test2.rse", DIGEST2);
	}

	@Test
	public void testRockSimMotorLoader3() throws IOException {
		test(new RockSimMotorLoader(), "/file/motor/test3.rse", DIGEST3);
	}

	@Test
	public void testZipMotorLoader() throws IOException {
		test(new ZipFileMotorLoader(), "/file/motor/test.zip", DIGEST2, DIGEST1);
	}

	@Test
	public void testRockSimMotorWriterRoundTrip() throws IOException {
		// Build a motor
		ThrustCurveMotor original = new ThrustCurveMotor.Builder()
				.setManufacturer(Manufacturer.getManufacturer("TestMfg"))
				.setDesignation("G80")
				.setDescription("Test motor")
				.setMotorType(Motor.Type.RELOAD)
				.setStandardDelays(new double[] { 4, 7, Motor.PLUGGED_DELAY })
				.setDiameter(0.029)
				.setLength(0.124)
				.setTimePoints(new double[] { 0, 0.5, 1.0, 1.5, 2.0 })
				.setThrustPoints(new double[] { 0, 80, 75, 40, 0 })
				.setCGPoints(new CoordinateIF[] {
						new Coordinate(0.062, 0, 0, 0.100),
						new Coordinate(0.060, 0, 0, 0.085),
						new Coordinate(0.058, 0, 0, 0.070),
						new Coordinate(0.055, 0, 0, 0.055),
						new Coordinate(0.050, 0, 0, 0.040)
				})
				.setDigest("test_digest_abc")
				.build();

		// Write to .rse
		RockSimMotorWriter writer = new RockSimMotorWriter();
		String rseContent = writer.write(original);

		// Parse the .rse content back
		RockSimMotorLoader loader = new RockSimMotorLoader();
		InputStream is = new ByteArrayInputStream(rseContent.getBytes(StandardCharsets.UTF_8));
		List<ThrustCurveMotor.Builder> motors = loader.load(is, "test.rse");
		assertEquals(1, motors.size(), "Expected exactly one motor from round-trip");

		ThrustCurveMotor loaded = motors.get(0).build();

		// Verify key properties survived the round-trip
		assertEquals(original.getDesignation(), loaded.getDesignation());
		assertEquals(original.getManufacturer().getSimpleName(), loaded.getManufacturer().getSimpleName());
		assertEquals(original.getMotorType(), loaded.getMotorType());
		assertEquals(original.getTimePoints().length, loaded.getTimePoints().length);
		assertEquals(original.getDiameter(), loaded.getDiameter(), 1e-6);
		assertEquals(original.getLength(), loaded.getLength(), 1e-6);
		assertEquals(original.getLaunchMass(), loaded.getLaunchMass(), 1e-6);
		assertTrue(Math.abs(original.getPropellantMass() - loaded.getPropellantMass()) < 1e-4,
				"Propellant mass mismatch");

		// Verify thrust curve data
		for (int i = 0; i < original.getTimePoints().length; i++) {
			assertEquals(original.getTimePoints()[i], loaded.getTimePoints()[i], 1e-6,
					"Time mismatch at point " + i);
			assertEquals(original.getThrustPoints()[i], loaded.getThrustPoints()[i], 1e-3,
					"Thrust mismatch at point " + i);
		}
	}

	@Test
	public void testRockSimMotorWriterRoundTripFromFile() throws IOException {
		// Load an existing .rse file
		InputStream is = this.getClass().getResourceAsStream("/file/motor/test2.rse");
		assertNotNull(is);
		RockSimMotorLoader loader = new RockSimMotorLoader();
		List<ThrustCurveMotor.Builder> originalMotors = loader.load(is, "test2.rse");
		is.close();
		assertEquals(1, originalMotors.size());
		ThrustCurveMotor original = originalMotors.get(0).build();

		// Write it back out
		RockSimMotorWriter writer = new RockSimMotorWriter();
		String rseContent = writer.write(original);

		// Parse it back
		InputStream is2 = new ByteArrayInputStream(rseContent.getBytes(StandardCharsets.UTF_8));
		List<ThrustCurveMotor.Builder> reloadedMotors = loader.load(is2, "roundtrip.rse");
		assertEquals(1, reloadedMotors.size());
		ThrustCurveMotor reloaded = reloadedMotors.get(0).build();

		// Verify key properties
		assertEquals(original.getDesignation(), reloaded.getDesignation());
		assertEquals(original.getTimePoints().length, reloaded.getTimePoints().length);
		assertEquals(original.getDiameter(), reloaded.getDiameter(), 1e-6);
		assertEquals(original.getLength(), reloaded.getLength(), 1e-6);
		assertEquals(original.getLaunchMass(), reloaded.getLaunchMass(), 1e-4);
	}

	private void test(MotorLoader loader, String file, String... digests) throws IOException {
		List<ThrustCurveMotor.Builder> motors;

		InputStream is = this.getClass().getResourceAsStream(file);
		assertNotNull(is, "File " + file + " not found");
		motors = loader.load(is, file);
		is.close();
		assertEquals(digests.length, motors.size());

		String[] d = new String[digests.length];
		for (int i = 0; i < motors.size(); i++) {
			d[i] = motors.get(i).build().getDigest();
		}

		Arrays.sort(digests);
		Arrays.sort(d);
		assertArrayEquals(d, digests, "d = " + Arrays.toString(d) + " digests = " + Arrays.toString(digests));
	}

}
