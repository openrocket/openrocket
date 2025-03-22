package info.openrocket.core.models.wind;

import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.MathUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.ModID;
import info.openrocket.core.util.StateChangeListener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MultiLevelWindModelTest extends BaseTestCase {
	private final static double EPSILON = MathUtil.EPSILON;
	private static final double DELTA_T = PinkNoiseWindModel.DELTA_T;
	private static final int SAMPLE_SIZE = 1000;

	private MultiLevelPinkNoiseWindModel model;

	@BeforeEach
	void setUpModel() {
		model = new MultiLevelPinkNoiseWindModel();
	}

	@Test
	@DisplayName("Add and remove wind levels")
	void testAddAndRemoveWindLevels() {
		model.addWindLevel(100, 5, Math.PI / 4, 1d);
		model.addWindLevel(200, 10, Math.PI / 2, 1d);
		assertEquals(3, model.getLevels().size());

		model.removeWindLevel(100);
		assertEquals(2, model.getLevels().size());
		assertEquals(200, model.getLevels().get(1).altitude, EPSILON);

		model.removeWindLevel(0);
		assertEquals(1, model.getLevels().size());
		assertEquals(200, model.getLevels().get(0).altitude, EPSILON);

		model.removeWindLevel(0);
		assertEquals(1, model.getLevels().size());
		assertEquals(200, model.getLevels().get(0).altitude, EPSILON);

		model.removeWindLevel(200);
		assertTrue(model.getLevels().isEmpty());
	}

	@Test
	@DisplayName("Adding duplicate altitude throws IllegalArgumentException")
	void testAddDuplicateAltitude() {
		assertThrows(IllegalArgumentException.class, () -> model.addWindLevel(0, 0, 0, 1d));

		model.addWindLevel(100, 5, Math.PI / 4, 1d);
		assertThrows(IllegalArgumentException.class, () -> model.addWindLevel(100, 10, Math.PI / 2, 1d));
	}

	@Test
	@DisplayName("Get wind velocity")
	void testGetWindVelocity() {
		model.clearLevels();
		model.addWindLevel(0, 5, 0, 1d);
		model.addWindLevel(1000, 10, Math.PI / 2, 2d);

		verifyWind(0, 5, 0, 1);
		verifyWind(1000, 10, Math.PI / 2, 2);
	}

	@Test
	@DisplayName("Interpolation between levels")
	void testInterpolationBetweenLevels() {
		// Test speed interpolation
		model.getLevels().get(0).setSpeed(5);
		model.getLevels().get(0).setStandardDeviation(0.1);
		model.addWindLevel(1000, 10, 0, 0.3);

		verifyWind(200, 6, 0, 0.14);
		verifyWind(500, 7.5, 0, 0.2);
		verifyWind(900, 9.5, 0, 0.28);

		model.clearLevels();

		// Test direction interpolation when speed vectors are parallel
		model.addWindLevel(0, 5, 0, 0d);
		model.addWindLevel(1000, 5, Math.PI, 0d);

		verifyWind(200, 3, 0, EPSILON);
		verifyWind(501, 0, Math.PI, 0.01);
		verifyWind(900, 4, Math.PI, EPSILON);

		model.clearLevels();

		// Test direction interpolation when speed vectors are not parallel
		model.addWindLevel(0, 5, 0, 0d);
		model.addWindLevel(1000, 5, Math.PI / 2, 0d);

		verifyWind(200, 4.1231056256, 0.2449786631, EPSILON);
		verifyWind(500, 3.5355339059, Math.PI / 4, EPSILON);
		verifyWind(800, 4.1231056256, 1.3258176637, EPSILON);
	}

	@Test
	@DisplayName("Extrapolation outside levels")
	void testExtrapolationOutsideLevels() {
		model.addWindLevel(100, 5, 0, 1.4);
		model.addWindLevel(200, 10, Math.PI / 2, 2.2);

		//verifyWind(-100, 5, Math.PI, 1.4);
		verifyWind(0, 0, 0, 0);
		verifyWind(300, 10, Math.PI / 2, 2.2);
		verifyWind(1000, 10, Math.PI / 2, 2.2);
	}

	@Test
	@DisplayName("Resort levels")
	void testSortLevels() {
		model.addWindLevel(200, 10, Math.PI / 2, 1d);
		model.addWindLevel(100, 5, Math.PI / 4, 1d);
		model.addWindLevel(300, 15, 3 * Math.PI / 4, 1d);

		model.sortLevels();

		List<MultiLevelPinkNoiseWindModel.LevelWindModel> levels = model.getLevels();
		assertEquals(4, levels.size());
		assertEquals(0, levels.get(0).altitude, EPSILON);
		assertEquals(100, levels.get(1).altitude, EPSILON);
		assertEquals(200, levels.get(2).altitude, EPSILON);
		assertEquals(300, levels.get(3).altitude, EPSILON);
	}

	@Test
	@DisplayName("Clone model")
	void testClone() {
		model.addWindLevel(100, 5, Math.PI / 4, 1d);
		model.addWindLevel(200, 10, Math.PI / 2, 2d);

		MultiLevelPinkNoiseWindModel clonedModel = model.clone();
		assertNotSame(model, clonedModel);
		assertEquals(model, clonedModel);

		clonedModel.addWindLevel(300, 15, 3 * Math.PI / 4, 1d);
		assertNotEquals(model, clonedModel);
	}

	@Test
	@DisplayName("Load from another model")
	void testLoadFrom() {
		model.addWindLevel(100, 5, Math.PI / 4, 2d);
		model.addWindLevel(200, 10, Math.PI / 2, 1d);

		MultiLevelPinkNoiseWindModel newModel = new MultiLevelPinkNoiseWindModel();
		newModel.loadFrom(model);

		assertEquals(model, newModel);
	}

	@Test
	@DisplayName("Get ModID")
	void testGetModID() {
		assertEquals(ModID.ZERO, model.getModID());
	}

	@Test
	@DisplayName("Change listeners")
	void testChangeListeners() {
		final boolean[] listenerCalled = {false};
		StateChangeListener listener = event -> listenerCalled[0] = true;

		model.addChangeListener(listener);
		model.fireChangeEvent();
		assertTrue(listenerCalled[0]);

		listenerCalled[0] = false;
		model.removeChangeListener(listener);
		model.fireChangeEvent();
		assertFalse(listenerCalled[0]);
	}

	@Test
	@DisplayName("Import wind levels from CSV - Basic functionality")
	void testImportLevelsFromCSV() throws IOException {
		// Create a temporary CSV file
		File tempFile = File.createTempFile("windlevels", ".csv");
		tempFile.deleteOnExit();

		// Write test data
		Files.write(tempFile.toPath(), Arrays.asList(
				"alt,speed,dir,stddev",
				"0,5,0,1.5",
				"100,10,45,2.0",
				"1000,15,90,2.5"
		));

		model.importLevelsFromCSV(tempFile, ",");

		List<MultiLevelPinkNoiseWindModel.LevelWindModel> levels = model.getLevels();
		assertEquals(3, levels.size());

		// Check first level
		assertEquals(0, levels.get(0).getAltitude(), EPSILON);
		assertEquals(5, levels.get(0).getSpeed(), EPSILON);
		assertEquals(0, levels.get(0).getDirection(), EPSILON);
		assertEquals(1.5, levels.get(0).getStandardDeviation(), EPSILON);

		// Check middle level
		assertEquals(100, levels.get(1).getAltitude(), EPSILON);
		assertEquals(10, levels.get(1).getSpeed(), EPSILON);
		assertEquals(Math.PI/4, levels.get(1).getDirection(), EPSILON); // 45 degrees
		assertEquals(2.0, levels.get(1).getStandardDeviation(), EPSILON);

		// Check importing with the wrong separator
		assertThrows(IllegalArgumentException.class, () ->
				model.importLevelsFromCSV(tempFile, ";")
		);
	}

	@Test
	@DisplayName("Import wind levels - Different number formats")
	void testImportLevelsWithDifferentNumberFormats() throws IOException {
		File tempFile = File.createTempFile("windlevels", ".csv");
		tempFile.deleteOnExit();

		// Test different number formats
		Files.write(tempFile.toPath(), Arrays.asList(
				"alt,speed,dir,stddev",
				"0,5.5,0,1.5",      // Standard format
				"100,10,2,2.0",     // Integer
				"1000,15.0,90,2.5", // With trailing zero
				"3000,8.000,10,2.5" // Multiple trailing zeros
		));

		model.importLevelsFromCSV(tempFile, ",");

		List<MultiLevelPinkNoiseWindModel.LevelWindModel> levels = model.getLevels();
		assertEquals(4, levels.size());

		// Verify specific values
		assertEquals(5.5, levels.get(0).getSpeed(), EPSILON);
		assertEquals(15.0, levels.get(2).getSpeed(), EPSILON);
		assertEquals(8.0, levels.get(3).getSpeed(), EPSILON);
		assertEquals(Math.PI/2, levels.get(2).getDirection(), EPSILON); // 90 degrees
	}

	@Test
	@DisplayName("Import wind levels - Import negative speeds and directions outside the range [0, 2*PI)")
	void testImportLevelsWithNegativeValues() throws IOException {
		File tempFile = File.createTempFile("windlevels", ".csv");
		tempFile.deleteOnExit();

		Files.write(tempFile.toPath(), Arrays.asList(
				"alt,speed,dir,stddev",
				"0,-5,0,1.5",      // Negative speed
				"100,10,-45,2.0",  // Negative direction
				"1000,-15,-90,2.5"  // Negative speed and direction
		));

		model.importLevelsFromCSV(tempFile, ",");

		List<MultiLevelPinkNoiseWindModel.LevelWindModel> levels = model.getLevels();
		assertEquals(3, levels.size());

		// Verify negative values
		assertEquals(5, levels.get(0).getSpeed(), EPSILON);
		assertEquals(Math.PI, levels.get(0).getDirection(), EPSILON); // 180 degrees
		assertEquals(7 * Math.PI/4, levels.get(1).getDirection(), EPSILON); // 315 degrees
		assertEquals(15, levels.get(2).getSpeed(), EPSILON);
		assertEquals(Math.PI/2, levels.get(2).getDirection(), EPSILON); // 90 degrees
	}

	@Test
	@DisplayName("Import wind levels - Different number formats")
	void testImportLevelsWithMissingValue() throws IOException {
		File tempFile = File.createTempFile("windlevels", ".csv");
		tempFile.deleteOnExit();

		// Test different number formats
		Files.write(tempFile.toPath(), Arrays.asList(
				"alt,speed,dir,stddev",
				"0,5,0,1.5",
				"100,10,45",		// Missing value
				"1000,15,90,2.5"
		));

		assertThrows(IllegalArgumentException.class, () ->
			model.importLevelsFromCSV(tempFile, ",")
		);
	}

	@Test
	@DisplayName("Import wind levels - European number format")
	void testImportLevelsWithEuropeanNumberFormat() throws IOException {
		File tempFile = File.createTempFile("windlevels", ".csv");
		tempFile.deleteOnExit();

		Files.write(tempFile.toPath(), Arrays.asList(
				"alt;speed;dir;stddev",
				"0;5;0;1,5",        // European format with comma
				"100;10;45;2,0",    // European format
				"1000;15,2;90;2,5"  // European format with multiple commas
		));

		model.importLevelsFromCSV(tempFile, ";"); // Note: using semicolon as separator

		List<MultiLevelPinkNoiseWindModel.LevelWindModel> levels = model.getLevels();
		assertEquals(3, levels.size());

		assertEquals(1.5, levels.get(0).getStandardDeviation(), EPSILON);
		assertEquals(2.0, levels.get(1).getStandardDeviation(), EPSILON);
	}

	@Test
	@DisplayName("Import wind levels - Error cases")
	void testImportLevelsErrorCases() throws IOException {
		// Test missing required column
		File tempFile = File.createTempFile("windlevels", ".csv");
		tempFile.deleteOnExit();

		// Missing speed column
		Files.write(tempFile.toPath(), Arrays.asList(
				"alt,dir,stddev",
				"0,0,1.5"
		));

		assertThrows(IllegalArgumentException.class, () ->
				model.importLevelsFromCSV(tempFile, ",")
		);

		// Invalid number format
		Files.write(tempFile.toPath(), Arrays.asList(
				"alt,speed,dir,stddev",
				"0,invalid,0,1.5"
		));

		assertThrows(IllegalArgumentException.class, () ->
				model.importLevelsFromCSV(tempFile, ",")
		);

		// Empty value
		Files.write(tempFile.toPath(), Arrays.asList(
				"alt,speed,dir,stddev",
				"0,,0,1.5"
		));

		assertThrows(IllegalArgumentException.class, () ->
				model.importLevelsFromCSV(tempFile, ",")
		);
	}

	@Test
	@DisplayName("Import wind levels - Different column orders")
	void testImportLevelsWithDifferentColumnOrders() throws IOException {
		File tempFile = File.createTempFile("windlevels", ".csv");
		tempFile.deleteOnExit();

		Files.write(tempFile.toPath(), Arrays.asList(
				"dir,speed,alt,stddev",  // Different order
				"0,5,0,1.5",
				"45,10,100,2.0"
		));

		model.importLevelsFromCSV(tempFile, ",");

		List<MultiLevelPinkNoiseWindModel.LevelWindModel> levels = model.getLevels();
		assertEquals(2, levels.size());

		assertEquals(0, levels.get(0).getAltitude(), EPSILON);
		assertEquals(5, levels.get(0).getSpeed(), EPSILON);
		assertEquals(0, levels.get(0).getDirection(), EPSILON);
	}

	@Test
	@DisplayName("Import wind levels - Extra columns")
	void testImportLevelsWithExtraColumns() throws IOException {
		File tempFile = File.createTempFile("windlevels", ".csv");
		tempFile.deleteOnExit();

		Files.write(tempFile.toPath(), Arrays.asList(
				"alt,speed,dir,stddev,extra1,extra2",  // Extra columns
				"0,5,0,1.5,100,200",
				"100,10,45,2.0,300,400"
		));

		model.importLevelsFromCSV(tempFile, ",");

		List<MultiLevelPinkNoiseWindModel.LevelWindModel> levels = model.getLevels();
		assertEquals(2, levels.size());

		assertEquals(0, levels.get(0).getAltitude(), EPSILON);
		assertEquals(5, levels.get(0).getSpeed(), EPSILON);
	}

	private void verifyWind(double altitude, double expectedSpeed, double expectedDirection, double standardDeviation) {
		double[] speeds = new double[SAMPLE_SIZE];
		double[] directions = new double[SAMPLE_SIZE];

		for (int i = 0; i < SAMPLE_SIZE; i++) {
			Coordinate velocity = model.getWindVelocity(i * DELTA_T, altitude);
			speeds[i] = velocity.length();
			directions[i] = Math.atan2(velocity.x, velocity.y);
		}

		double avgSpeed = Arrays.stream(speeds, 0, SAMPLE_SIZE).average().orElse(0.0);
		double avgDirection = averageAngle(directions);

		// Check average speed and direction
		assertEquals(expectedSpeed, avgSpeed, standardDeviation, "Average wind speed at altitude " + altitude);
		assertEquals(expectedDirection, avgDirection, EPSILON, "Average wind direction at altitude " + altitude);

		// Check that some values are above and below the expected value
		//assertTrue(IntStream.range(0, SAMPLE_SIZE).anyMatch(i -> speeds[i] >= expectedSpeed));
		//assertTrue(IntStream.range(0, SAMPLE_SIZE).anyMatch(i -> speeds[i] <= expectedSpeed));
	}

	private double averageAngle(double[] angles) {
		double sumSin = 0, sumCos = 0;
		for (double angle : angles) {
			sumSin += Math.sin(angle);
			sumCos += Math.cos(angle);
		}
		return Math.atan2(sumSin, sumCos);
	}
}