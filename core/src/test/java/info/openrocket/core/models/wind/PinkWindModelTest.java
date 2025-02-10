package info.openrocket.core.models.wind;

import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.ModID;
import info.openrocket.core.util.StateChangeListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotSame;


class PinkNoiseWindModelTest {
	private static final double EPSILON = MathUtil.EPSILON;
	private static final double DELTA_T = PinkNoiseWindModel.DELTA_T;
	private static final int SAMPLE_SIZE = 1000;

	private PinkNoiseWindModel model;

	@BeforeEach
	void setUp() {
		model = new PinkNoiseWindModel(42); // Use fixed seed for reproducibility
	}

	@Test
	@DisplayName("Test initial state")
	void testInitialState() {
		assertEquals(0, model.getAverage(), EPSILON);
		assertEquals(0, model.getStandardDeviation(), EPSILON);
		assertEquals(Math.PI / 2, model.getDirection(), EPSILON);
		assertEquals(0, model.getTurbulenceIntensity(), EPSILON);
	}

	@Test
	@DisplayName("Test setting average wind speed including negative values")
	void testSetAverage() {
		// Test positive speed
		model.setAverage(10.0);
		assertEquals(10.0, model.getAverage(), EPSILON);
		double originalDirection = model.getDirection();

		// Test negative speed (should flip direction and make speed positive)
		model.setAverage(-10.0);
		assertEquals(10.0, model.getAverage(), EPSILON);
		assertEquals(MathUtil.reduce2Pi(Math.PI + originalDirection), model.getDirection(), EPSILON);

		// Test multiple negative speed changes
		model.setAverage(-15.0);
		assertEquals(15.0, model.getAverage(), EPSILON);
		assertEquals(originalDirection, model.getDirection(), EPSILON); // Direction should stay the same for second negative

		// Test setting again to positive speed
		model.setAverage(20.0);
		assertEquals(20.0, model.getAverage(), EPSILON);
		assertEquals(originalDirection, model.getDirection(), EPSILON);
	}

	@Test
	@DisplayName("Test setting wind direction with normalization")
	void testSetDirection() {
		// Test basic direction setting
		model.setDirection(Math.PI);
		assertEquals(Math.PI, model.getDirection(), EPSILON);

		// Test direction > 2*PI
		model.setDirection(3 * Math.PI);
		assertEquals(Math.PI, model.getDirection(), EPSILON);

		// Test direction > 4*PI
		model.setDirection(9 * Math.PI);
		assertEquals(Math.PI, model.getDirection(), EPSILON);

		// Test negative direction
		model.setDirection(-Math.PI / 2);
		assertEquals(3 * Math.PI / 2, model.getDirection(), EPSILON);

		// Test negative direction > 2*PI
		model.setDirection(-3 * Math.PI);
		assertEquals(Math.PI, model.getDirection(), EPSILON);

		// Test negative direction > 4*PI
		model.setDirection(-5 * Math.PI);
		assertEquals(Math.PI, model.getDirection(), EPSILON);

		// Verify that very large values are handled correctly
		model.setDirection(1000 * Math.PI);
		assertTrue(model.getDirection() >= 0 && model.getDirection() <= 2 * Math.PI,
				"Direction should be normalized to [0, 2*PI)");
	}

	@Test
	@DisplayName("Test standard deviation and turbulence intensity")
	void testStandardDeviationAndTurbulence() {
		model.setAverage(10.0);
		model.setStandardDeviation(2.0);

		assertEquals(2.0, model.getStandardDeviation(), EPSILON);
		assertEquals(0.2, model.getTurbulenceIntensity(), EPSILON);

		// Test setting turbulence intensity
		model.setTurbulenceIntensity(0.3);
		assertEquals(3.0, model.getStandardDeviation(), EPSILON);

		// Test edge case with zero average
		model.setAverage(0.0);
		model.setStandardDeviation(0.0);
		assertEquals(0.0, model.getTurbulenceIntensity(), EPSILON);

		model.setStandardDeviation(1.0);
		assertEquals(1, model.getTurbulenceIntensity(), EPSILON);
	}

	@Test
	@DisplayName("Test wind velocity generation")
	void testGetWindVelocity() {
		model.setAverage(10.0);
		model.setDirection(0.0); // Wind blowing toward positive X
		model.setStandardDeviation(2.0);

		// Sample wind velocities over time
		double[] speeds = new double[SAMPLE_SIZE];
		for (int i = 0; i < SAMPLE_SIZE; i++) {
			Coordinate velocity = model.getWindVelocity(i * DELTA_T, 0);
			speeds[i] = velocity.length();
		}

		// Check statistical properties
		double avgSpeed = Arrays.stream(speeds).average().orElse(0.0);
		assertEquals(10.0, avgSpeed, 0.5); // Allow some deviation due to randomness

		// Verify that reset works properly
		Coordinate v1 = model.getWindVelocity(1.0, 0);
		Coordinate v2 = model.getWindVelocity(0.5, 0);
		assertNotEquals(v1, v2);
	}

	@Test
	@DisplayName("Test model cloning")
	void testClone() {
		model.setAverage(10.0);
		model.setDirection(Math.PI / 4);
		model.setStandardDeviation(2.0);

		PinkNoiseWindModel clone = model.clone();

		assertEquals(model.getAverage(), clone.getAverage(), EPSILON);
		assertEquals(model.getDirection(), clone.getDirection(), EPSILON);
		assertEquals(model.getStandardDeviation(), clone.getStandardDeviation(), EPSILON);
		assertNotSame(model, clone);
	}

	@Test
	@DisplayName("Test model equality")
	void testEquals() {
		model.setAverage(10.0);
		model.setDirection(Math.PI / 4);
		model.setStandardDeviation(2.0);

		PinkNoiseWindModel other = new PinkNoiseWindModel(42);
		other.setAverage(10.0);
		other.setDirection(Math.PI / 4);
		other.setStandardDeviation(2.0);

		assertEquals(model, other);
		assertEquals(model.hashCode(), other.hashCode());

		other.setAverage(11.0);
		assertNotEquals(model, other);
	}

	@Test
	@DisplayName("Test change listeners")
	void testChangeListeners() {
		final boolean[] listenerCalled = {false};
		StateChangeListener listener = event -> listenerCalled[0] = true;

		model.addChangeListener(listener);
		model.setAverage(10.0);
		assertTrue(listenerCalled[0]);

		listenerCalled[0] = false;
		model.removeChangeListener(listener);
		model.setAverage(15.0);
		assertFalse(listenerCalled[0]);
	}

	@Test
	@DisplayName("Test ModID")
	void testGetModID() {
		assertEquals(ModID.ZERO, model.getModID());
	}

	@Test
	@DisplayName("Test invalid time parameter")
	void testInvalidTime() {
		assertThrows(IllegalArgumentException.class, () -> model.getWindVelocity(-1.0, 0));
	}
}