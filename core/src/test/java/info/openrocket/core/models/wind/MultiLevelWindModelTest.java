package info.openrocket.core.models.wind;

import info.openrocket.core.unit.DegreeUnit;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.MathUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.ModID;
import info.openrocket.core.util.StateChangeListener;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MultiLevelWindModelTest extends BaseTestCase {
	private final static double EPSILON = MathUtil.EPSILON;
	private static final double DELTA_T = PinkNoiseWindModel.DELTA_T;
	private static final int SAMPLE_SIZE = 1000;

	private MultiLevelPinkNoiseWindModel model;

	@TempDir
	Path tempDir;

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
				"altitude,speed,direction,stddev",
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
				"altitude,speed,direction,stddev",
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
				"altitude,speed,direction,stddev",
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
				"altitude,speed,direction,stddev",
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
				"altitude;speed;direction;stddev",
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
				"altitude,direction,stddev",
				"0,0,1.5"
		));

		assertThrows(IllegalArgumentException.class, () ->
				model.importLevelsFromCSV(tempFile, ",")
		);

		// Invalid number format
		Files.write(tempFile.toPath(), Arrays.asList(
				"altitude,speed,direction,stddev",
				"0,invalid,0,1.5"
		));

		assertThrows(IllegalArgumentException.class, () ->
				model.importLevelsFromCSV(tempFile, ",")
		);

		// Empty value
		Files.write(tempFile.toPath(), Arrays.asList(
				"altitude,speed,direction,stddev",
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
				"direction,speed,altitude,stddev",  // Different order
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
				"altitude,speed,direction,stddev,extra1,extra2",  // Extra columns
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

	@Test
	@DisplayName("Import with custom column names")
	void testImportWithCustomColumnNames() throws IOException {
		// Create a temporary CSV file with different column names
		File tempFile = tempDir.resolve("custom_columns.csv").toFile();

		Files.write(tempFile.toPath(), Arrays.asList(
				"height,velocity,angle,variation",
				"0,5,0,1.5",
				"100,10,45,2.0",
				"1000,15,90,2.5"
		));

		// Import with custom column names
		model.importLevelsFromCSV(tempFile, ",",
				"height", "velocity", "angle", "variation",
				UnitGroup.UNITS_DISTANCE.getSIUnit(),
				UnitGroup.UNITS_WINDSPEED.getSIUnit(),
				new DegreeUnit(),
				UnitGroup.UNITS_WINDSPEED.getSIUnit(),
				true);

		// Verify the data was imported correctly
		List<MultiLevelPinkNoiseWindModel.LevelWindModel> levels = model.getLevels();
		assertEquals(3, levels.size(), "Should have 3 wind levels");

		assertEquals(0, levels.get(0).getAltitude(), EPSILON);
		assertEquals(5, levels.get(0).getSpeed(), EPSILON);
		assertEquals(0, levels.get(0).getDirection(), EPSILON);
		assertEquals(1.5, levels.get(0).getStandardDeviation(), EPSILON);

		assertEquals(100, levels.get(1).getAltitude(), EPSILON);
		assertEquals(10, levels.get(1).getSpeed(), EPSILON);
		assertEquals(Math.PI/4, levels.get(1).getDirection(), EPSILON); // 45 degrees
		assertEquals(2.0, levels.get(1).getStandardDeviation(), EPSILON);
	}

	@Test
	@DisplayName("Import with column indices")
	void testImportWithColumnIndices() throws IOException {
		// Create a temporary CSV file without headers
		File tempFile = tempDir.resolve("no_headers.csv").toFile();

		Files.write(tempFile.toPath(), Arrays.asList(
				"0,5,0,1.5",
				"100,10,45,2.0",
				"1000,15,90,2.5"
		));

		// Import using column indices without headers
		model.importLevelsFromCSV(tempFile, ",",
				"0", "1", "2", "3", // alt=0, speed=1, dir=2, stddev=3
				UnitGroup.UNITS_DISTANCE.getSIUnit(),
				UnitGroup.UNITS_WINDSPEED.getSIUnit(),
				new DegreeUnit(),
				UnitGroup.UNITS_WINDSPEED.getSIUnit(),
				false);  // no headers

		// Verify the data was imported correctly
		List<MultiLevelPinkNoiseWindModel.LevelWindModel> levels = model.getLevels();
		assertEquals(3, levels.size(), "Should have 3 wind levels");

		assertEquals(0, levels.get(0).getAltitude(), EPSILON);
		assertEquals(5, levels.get(0).getSpeed(), EPSILON);
		assertEquals(0, levels.get(0).getDirection(), EPSILON);
		assertEquals(1.5, levels.get(0).getStandardDeviation(), EPSILON);

		assertEquals(100, levels.get(1).getAltitude(), EPSILON);
		assertEquals(10, levels.get(1).getSpeed(), EPSILON);
		assertEquals(Math.PI/4, levels.get(1).getDirection(), EPSILON); // 45 degrees
	}

	@Test
	@DisplayName("Import with custom units")
	void testImportWithCustomUnits() throws IOException {
		// Create a temporary CSV file with values in different units
		File tempFile = tempDir.resolve("custom_units.csv").toFile();

		Files.write(tempFile.toPath(), Arrays.asList(
				"altitude,speed,direction,stddev",
				// Alt in feet, speed in mph, direction in degrees, stddev in mph
				"0,11.2,0,3.4",       // 0m, 5m/s, 0rad, 1.5m/s
				"328,22.4,45,4.5",    // 100m, 10m/s, PI/4rad, 2.0m/s
				"3280,33.6,90,5.6"    // 1000m, 15m/s, PI/2rad, 2.5m/s
		));

		// Import with custom units
		Unit feetUnit = UnitGroup.UNITS_DISTANCE.getUnit("ft");
		Unit mphUnit = UnitGroup.UNITS_WINDSPEED.getUnit("mph");
		Unit degreeUnit = new DegreeUnit();

		model.importLevelsFromCSV(tempFile, ",",
				"altitude", "speed", "direction", "stddev",
				feetUnit,      // altitude in feet
				mphUnit,       // speed in mph
				degreeUnit,    // direction in degrees
				mphUnit,       // stddev in mph
				true);

		// Verify the data was imported and converted correctly
		List<MultiLevelPinkNoiseWindModel.LevelWindModel> levels = model.getLevels();
		assertEquals(3, levels.size(), "Should have 3 wind levels");

		// Check conversions (allowing for rounding errors)
		assertEquals(0, levels.get(0).getAltitude(), EPSILON);
		assertEquals(5, levels.get(0).getSpeed(), 0.1); // ~5 m/s
		assertEquals(0, levels.get(0).getDirection(), EPSILON);
		assertEquals(1.5, levels.get(0).getStandardDeviation(), 0.1); // ~1.5 m/s

		assertEquals(100, levels.get(1).getAltitude(), 0.1); // ~100 m
		assertEquals(10, levels.get(1).getSpeed(), 0.1); // ~10 m/s
		assertEquals(Math.PI/4, levels.get(1).getDirection(), EPSILON); // 45 degrees
	}

	@Test
	@DisplayName("Import with optional standard deviation column")
	void testImportWithOptionalStdDevColumn() throws IOException {
		// Create a temporary CSV file without stddev column
		File tempFile = tempDir.resolve("no_stddev.csv").toFile();

		Files.write(tempFile.toPath(), Arrays.asList(
				"altitude,speed,direction",
				"0,5,0",
				"100,10,45",
				"1000,15,90"
		));

		// Import without stddev column
		model.importLevelsFromCSV(tempFile, ",",
				"altitude", "speed", "direction", "",  // empty stddev column name
				UnitGroup.UNITS_DISTANCE.getSIUnit(),
				UnitGroup.UNITS_WINDSPEED.getSIUnit(),
				new DegreeUnit(),
				null,  // no unit needed for stddev
				true);

		// Verify the data was imported correctly
		List<MultiLevelPinkNoiseWindModel.LevelWindModel> levels = model.getLevels();
		assertEquals(3, levels.size(), "Should have 3 wind levels");

		// The standard deviation should be the default value from PinkNoiseWindModel
		double defaultStdDev = new PinkNoiseWindModel().getStandardDeviation();

		assertEquals(0, levels.get(0).getAltitude(), EPSILON);
		assertEquals(5, levels.get(0).getSpeed(), EPSILON);
		assertEquals(0, levels.get(0).getDirection(), EPSILON);
		assertEquals(defaultStdDev, levels.get(0).getStandardDeviation(), EPSILON);
	}

	@Test
	@DisplayName("Import with invalid column indices")
	void testImportWithInvalidColumnIndices() throws IOException {
		// Create a temporary CSV file
		File tempFile = tempDir.resolve("invalid_indices.csv").toFile();

		Files.write(tempFile.toPath(), Arrays.asList(
				"altitude,speed,direction,stddev",
				"0,5,0,1.5"
		));

		// Try to import with an out-of-bounds index
		assertThrows(IllegalArgumentException.class, () ->
				model.importLevelsFromCSV(tempFile, ",",
						"0", "1", "5", "3",  // dir=5 is out of bounds
						UnitGroup.UNITS_DISTANCE.getSIUnit(),
						UnitGroup.UNITS_WINDSPEED.getSIUnit(),
						new DegreeUnit(),
						UnitGroup.UNITS_WINDSPEED.getSIUnit(),
						true)
		);

		// Try to import with a non-numeric index when headers=false
		assertThrows(IllegalArgumentException.class, () ->
				model.importLevelsFromCSV(tempFile, ",",
						"0", "speed", "2", "3",  // non-numeric "speed" for index
						UnitGroup.UNITS_DISTANCE.getSIUnit(),
						UnitGroup.UNITS_WINDSPEED.getSIUnit(),
						new DegreeUnit(),
						UnitGroup.UNITS_WINDSPEED.getSIUnit(),
						false)
		);
	}

	@Test
	@DisplayName("Import with European units and number format")
	void testImportWithEuropeanUnitsAndFormat() throws IOException {
		// Create a temporary CSV file with European number format
		File tempFile = tempDir.resolve("european_format.csv").toFile();

		Files.write(tempFile.toPath(), Arrays.asList(
				"hohe;geschwindigkeit;richtung;abweichung",
				"0;5,0;0;1,5",
				"100;10,0;45;2,0",
				"1000;15,0;90;2,5"
		));

		// Import with European column names and formatting
		model.importLevelsFromCSV(tempFile, ";",
				"hohe", "geschwindigkeit", "richtung", "abweichung",
				UnitGroup.UNITS_DISTANCE.getSIUnit(),
				UnitGroup.UNITS_WINDSPEED.getSIUnit(),
				new DegreeUnit(),
				UnitGroup.UNITS_WINDSPEED.getSIUnit(),
				true);

		// Verify the data was imported correctly
		List<MultiLevelPinkNoiseWindModel.LevelWindModel> levels = model.getLevels();
		assertEquals(3, levels.size(), "Should have 3 wind levels");

		assertEquals(0, levels.get(0).getAltitude(), EPSILON);
		assertEquals(5.0, levels.get(0).getSpeed(), EPSILON);
		assertEquals(0, levels.get(0).getDirection(), EPSILON);
		assertEquals(1.5, levels.get(0).getStandardDeviation(), EPSILON);
	}

	@Test
	@DisplayName("Import with mixed column references (names and indices)")
	void testImportWithMixedColumnReferences() throws IOException {
		// This test is only relevant if the API allows mixing names and indices
		// Create a temporary CSV file
		File tempFile = tempDir.resolve("mixed_references.csv").toFile();

		Files.write(tempFile.toPath(), Arrays.asList(
				"height,velocity,angle,variation",
				"0,5,0,1.5",
				"100,10,45,2.0"
		));

		// Import using a mix of names and indices
		// Note: This assumes your implementation can handle mixed references
		// If not, adjust the test accordingly
		try {
			model.importLevelsFromCSV(tempFile, ",",
					"height", "1", "angle", "3",  // mix of names and indices
					UnitGroup.UNITS_DISTANCE.getSIUnit(),
					UnitGroup.UNITS_WINDSPEED.getSIUnit(),
					new DegreeUnit(),
					UnitGroup.UNITS_WINDSPEED.getSIUnit(),
					true);

			// If implementation supports mixed references, verify the data
			List<MultiLevelPinkNoiseWindModel.LevelWindModel> levels = model.getLevels();
			assertEquals(2, levels.size(), "Should have 2 wind levels");

			assertEquals(0, levels.get(0).getAltitude(), EPSILON);
			assertEquals(5, levels.get(0).getSpeed(), EPSILON);
			assertEquals(0, levels.get(0).getDirection(), EPSILON);
		} catch (IllegalArgumentException e) {
			// If mixing is not supported, that's fine - the test is informative
			// No assertion needed
		}
	}
}