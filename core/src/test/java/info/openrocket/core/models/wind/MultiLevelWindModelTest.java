package info.openrocket.core.models.wind;

import info.openrocket.core.util.MathUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.ModID;
import info.openrocket.core.util.StateChangeListener;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MultiLevelWindModelTest {
	private final static double EPSILON = MathUtil.EPSILON;

	private MultiLevelWindModel model;

	@BeforeEach
	void setUp() {
		model = new MultiLevelWindModel();
	}

	@Test
	@DisplayName("Add and remove wind levels")
	void testAddAndRemoveWindLevels() {
		model.addWindLevel(100, 5, Math.PI / 4);
		model.addWindLevel(200, 10, Math.PI / 2);
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
		model.addWindLevel(100, 5, Math.PI / 4);
		assertThrows(IllegalArgumentException.class, () -> model.addWindLevel(100, 10, Math.PI / 2));
	}

	@Test
	@DisplayName("Get wind velocity")
	void testGetWindVelocity() {
		model.addWindLevel(0, 5, 0);
		model.addWindLevel(1000, 10, Math.PI / 2);

		verifyWind(500, 7.5, Math.PI / 4);
	}

	@Test
	@DisplayName("Interpolation between levels")
	void testInterpolationBetweenLevels() {
		model.addWindLevel(0, 5, 0);
		model.addWindLevel(1000, 10, Math.PI);

		verifyWind(200, 6, Math.PI / 5);
		verifyWind(500, 7.5, Math.PI / 2);
		verifyWind(900, 9.5, 9 * Math.PI / 10);
	}

	@Test
	@DisplayName("Extrapolation outside levels")
	void testExtrapolationOutsideLevels() {
		model.addWindLevel(100, 5, 0);
		model.addWindLevel(200, 10, Math.PI / 2);

		verifyWind(0, 5, 0);
		verifyWind(300, 10, Math.PI / 2);
		verifyWind(1000, 10, Math.PI / 2);
	}

	@Test
	@DisplayName("Resort levels")
	void testResortLevels() {
		model.addWindLevel(200, 10, Math.PI / 2);
		model.addWindLevel(100, 5, Math.PI / 4);
		model.addWindLevel(300, 15, 3 * Math.PI / 4);

		model.resortLevels();

		List<MultiLevelWindModel.WindLevel> levels = model.getLevels();
		assertEquals(3, levels.size());
		assertEquals(100, levels.get(0).altitude, EPSILON);
		assertEquals(200, levels.get(1).altitude, EPSILON);
		assertEquals(300, levels.get(2).altitude, EPSILON);
	}

	@Test
	@DisplayName("Clone model")
	void testClone() {
		model.addWindLevel(100, 5, Math.PI / 4);
		model.addWindLevel(200, 10, Math.PI / 2);

		MultiLevelWindModel clonedModel = model.clone();
		assertNotSame(model, clonedModel);
		assertEquals(model, clonedModel);

		clonedModel.addWindLevel(300, 15, 3 * Math.PI / 4);
		assertNotEquals(model, clonedModel);
	}

	@Test
	@DisplayName("Load from another model")
	void testLoadFrom() {
		model.addWindLevel(100, 5, Math.PI / 4);
		model.addWindLevel(200, 10, Math.PI / 2);

		MultiLevelWindModel newModel = new MultiLevelWindModel();
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

	private void verifyWind(double altitude, double expectedSpeed, double expectedDirection) {
		Coordinate velocity = model.getWindVelocity(0, altitude);
		assertEquals(expectedSpeed, velocity.length(), EPSILON, "Wind speed at altitude " + altitude);
		assertEquals(expectedSpeed * Math.sin(expectedDirection), velocity.x, EPSILON, "Wind velocity X component at altitude " + altitude);
		assertEquals(expectedSpeed * Math.cos(expectedDirection), velocity.y, EPSILON, "Wind velocity Y component at altitude " + altitude);
		assertEquals(0, velocity.z, EPSILON, "Wind velocity Z component at altitude " + altitude);
		assertEquals(expectedDirection, Math.atan2(velocity.x, velocity.y), EPSILON, "Wind direction at altitude " + altitude);
	}
}