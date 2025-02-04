package info.openrocket.core.models.wind;

import info.openrocket.core.util.MathUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.ModID;
import info.openrocket.core.util.StateChangeListener;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class MultiLevelWindModelTest {
	private final static double EPSILON = MathUtil.EPSILON;
	private static final double DELTA_T = PinkNoiseWindModel.DELTA_T;
	private static final int SAMPLE_SIZE = 1000;

	private MultiLevelPinkNoiseWindModel model;

	@BeforeEach
	void setUp() {
		model = new MultiLevelPinkNoiseWindModel();
	}

	@Test
	@DisplayName("Add and remove wind levels")
	void testAddAndRemoveWindLevels() {
		model.addWindLevel(100, 5, Math.PI / 4, 1d);
		model.addWindLevel(200, 10, Math.PI / 2, 1d);
		assertEquals(2, model.getLevels().size());

		model.removeWindLevel(100);
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
		model.addWindLevel(100, 5, Math.PI / 4, 1d);
		assertThrows(IllegalArgumentException.class, () -> model.addWindLevel(100, 10, Math.PI / 2, 1d));
	}

	@Test
	@DisplayName("Get wind velocity")
	void testGetWindVelocity() {
		model.addWindLevel(0, 5, 0, 1d);
		model.addWindLevel(1000, 10, Math.PI / 2, 2d);

		verifyWind(0, 5, 0, 1);
		verifyWind(1000, 10, Math.PI / 2, 2);
	}

	@Test
	@DisplayName("Interpolation between levels")
	void testInterpolationBetweenLevels() {
		// Test speed interpolation
		model.addWindLevel(0, 5, 0, 0.1);
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

		verifyWind(0, 5, 0, 1.4);
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
		assertEquals(3, levels.size());
		assertEquals(100, levels.get(0).altitude, EPSILON);
		assertEquals(200, levels.get(1).altitude, EPSILON);
		assertEquals(300, levels.get(2).altitude, EPSILON);
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