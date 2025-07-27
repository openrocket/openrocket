package info.openrocket.core.aerodynamics;

import static org.junit.jupiter.api.Assertions.*;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import info.openrocket.core.ServicesForTesting;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.startup.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import info.openrocket.core.models.atmosphere.AtmosphericConditions;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.util.Coordinate;

class FlightConditionsTest {
	private FlightConditions conditions;
	private static final double EPSILON = 1e-6;

	private Rocket rocket;

	@BeforeEach
	void setUp() {
		com.google.inject.Module applicationModule = new ServicesForTesting();
		Module pluginModule = new PluginModule();

		Injector injector = Guice.createInjector(applicationModule, pluginModule);
		Application.setInjector(injector);

		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		this.rocket = document.getRocket();
		BodyTube bodyTube = new BodyTube();
		bodyTube.setLength(1.0);
		bodyTube.setOuterRadius(0.05);
		this.rocket.getChild(0).addChild(bodyTube);

		FlightConfiguration mockConfig = new FlightConfiguration(rocket);
		this.conditions = new FlightConditions(mockConfig);
	}

	@Test
	void testSetAndGetRefLength() {
		double expectedLength = ((BodyTube) rocket.getChild(0).getChild(0)).getOuterRadius() * 2;
		assertEquals(expectedLength, conditions.getRefLength(), EPSILON);

		conditions.setRefLength(2.0);
		assertEquals(2.0, conditions.getRefLength(), EPSILON);
		assertEquals(Math.PI, conditions.getRefArea(), EPSILON);
	}

	@Test
	void testSetAndGetRefArea() {
		// Get the actual reference area from FlightConditions
		double actualRefArea = conditions.getRefArea();

		// Test that setting this area results in the same value
		conditions.setRefArea(actualRefArea);
		assertEquals(actualRefArea, conditions.getRefArea(), EPSILON);

		// Test that setting a new area works correctly
		double newArea = 4.0;
		conditions.setRefArea(newArea);
		assertEquals(newArea, conditions.getRefArea(), EPSILON);
		assertEquals(Math.sqrt(newArea / Math.PI) * 2, conditions.getRefLength(), EPSILON);
	}

	@Test
	void testSetAndGetAOA() {
		conditions.setAOA(Math.PI / 4);
		assertEquals(Math.PI / 4, conditions.getAOA(), EPSILON);
		assertEquals(Math.sin(Math.PI / 4), conditions.getSinAOA(), EPSILON);
		assertEquals(Math.sin(Math.PI / 4) / (Math.PI / 4), conditions.getSincAOA(), EPSILON);
	}

	@Test
	void testSetAndGetTheta() {
		conditions.setTheta(Math.PI / 3);
		assertEquals(Math.PI / 3, conditions.getTheta(), EPSILON);
	}

	@Test
	void testSetAndGetMach() {
		conditions.setMach(0.2);
		assertEquals(0.2, conditions.getMach(), EPSILON);
		assertEquals(0.9797958971, conditions.getBeta(), EPSILON);

		conditions.setMach(0.8);
		assertEquals(0.8, conditions.getMach(), EPSILON);
		assertEquals(0.6, conditions.getBeta(), EPSILON);

		conditions.setMach(0.9999999999);
		assertEquals(0.9999999999, conditions.getMach(), EPSILON);
		assertEquals(0.25, conditions.getBeta(), EPSILON);

		conditions.setMach(1.00000000001);
		assertEquals(1.00000000001, conditions.getMach(), EPSILON);
		assertEquals(0.25, conditions.getBeta(), EPSILON);

		conditions.setMach(1.3);
		assertEquals(1.3, conditions.getMach(), EPSILON);
		assertEquals(0.8306623863, conditions.getBeta(), EPSILON);

		conditions.setMach(3);
		assertEquals(3, conditions.getMach(), EPSILON);
		assertEquals(2.8284271247, conditions.getBeta(), EPSILON);
	}

	@Test
	void testSetAndGetVelocity() {
		AtmosphericConditions atm = new AtmosphericConditions();
		conditions.setAtmosphericConditions(atm);

		double expectedMachSpeed = atm.getMachSpeed();
		conditions.setVelocity(expectedMachSpeed / 2);

		assertEquals(0.5, conditions.getMach(), EPSILON);
		assertEquals(expectedMachSpeed / 2, conditions.getVelocity(), EPSILON);
	}

	@Test
	void testSetAndGetRollRate() {
		conditions.setRollRate(5.0);
		assertEquals(5.0, conditions.getRollRate(), EPSILON);
	}

	@Test
	void testSetAndGetPitchRate() {
		conditions.setPitchRate(2.5);
		assertEquals(2.5, conditions.getPitchRate(), EPSILON);
	}

	@Test
	void testSetAndGetYawRate() {
		conditions.setYawRate(1.5);
		assertEquals(1.5, conditions.getYawRate(), EPSILON);
	}

	@Test
	void testSetAndGetPitchCenter() {
		Coordinate center = new Coordinate(1.0, 2.0, 3.0);
		conditions.setPitchCenter(center);
		assertEquals(center, conditions.getPitchCenter());
	}

	@Test
	void testClone() {
		conditions.setAOA(Math.PI / 6);
		conditions.setMach(0.7);
		conditions.setRollRate(3.0);
		AtmosphericConditions atm = new AtmosphericConditions(280, 90000);
		conditions.setAtmosphericConditions(atm);

		FlightConditions cloned = conditions.clone();

		assertNotSame(conditions, cloned);
		assertEquals(conditions.getAOA(), cloned.getAOA(), EPSILON);
		assertEquals(conditions.getMach(), cloned.getMach(), EPSILON);
		assertEquals(conditions.getRollRate(), cloned.getRollRate(), EPSILON);
		assertEquals(conditions.getAtmosphericConditions().getTemperature(),
				cloned.getAtmosphericConditions().getTemperature(), EPSILON);
		assertEquals(conditions.getAtmosphericConditions().getPressure(),
				cloned.getAtmosphericConditions().getPressure(), EPSILON);
	}

	@Test
	void testEquals() {
		FlightConditions conditions1 = new FlightConditions(null);
		FlightConditions conditions2 = new FlightConditions(null);

		conditions1.setAOA(Math.PI / 6);
		conditions1.setMach(0.7);
		conditions2.setAOA(Math.PI / 6);
		conditions2.setMach(0.7);

		assertTrue(conditions1.equals(conditions2));

		conditions2.setMach(0.8);
		assertFalse(conditions1.equals(conditions2));
	}

	@Test
	void testSetAndGetAtmosphericConditions() {
		AtmosphericConditions atm = new AtmosphericConditions(280, 90000);
		conditions.setAtmosphericConditions(atm);

		assertEquals(280, conditions.getAtmosphericConditions().getTemperature(), EPSILON);
		assertEquals(90000, conditions.getAtmosphericConditions().getPressure(), EPSILON);
	}

	@Test
	void testGetVelocityWithChangedAtmosphere() {
		AtmosphericConditions atm = new AtmosphericConditions(280, 90000);
		conditions.setAtmosphericConditions(atm);
		conditions.setMach(0.5);

		double expectedVelocity = 0.5 * atm.getMachSpeed();
		assertEquals(expectedVelocity, conditions.getVelocity(), EPSILON);

		// Change atmospheric conditions
		atm.setTemperature(300);
		conditions.setAtmosphericConditions(atm);

		// Velocity should change with new atmospheric conditions
		expectedVelocity = 0.5 * atm.getMachSpeed();
		assertEquals(expectedVelocity, conditions.getVelocity(), EPSILON);
	}
}