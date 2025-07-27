package info.openrocket.core.rocketcomponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import info.openrocket.core.util.BoundingBox;
import org.junit.jupiter.api.Test;

import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.TestRockets;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.ModID;

public class FlightConfigurationTest extends BaseTestCase {
	private final static double EPSILON = MathUtil.EPSILON * 1.0E3;

	/**
	 * Empty rocket (no components) specific configuration tests
	 */
	@Test
	public void testEmptyRocket() {
		Rocket r1 = TestRockets.makeEstesAlphaIII();
		FlightConfiguration config = r1.getSelectedConfiguration();

		FlightConfiguration configClone = config.clone();

		assertSame(config.getRocket(), configClone.getRocket());
	}

	@Test
	public void testFlightConfigurationRocketLength() {
		Rocket rocket = TestRockets.makeBeta();
		FlightConfiguration config = rocket.getEmptyConfiguration();
		rocket.setSelectedConfiguration(config.getId());

		config.setAllStages();

		// preconditions
		assertEquals(config.getActiveStageCount(), 2, "active stage count doesn't match");

		final double expectedLength = 0.335;
		final double calculatedLength = config.getLengthAerodynamic();
		assertEquals(expectedLength, calculatedLength, EPSILON, "source config length doesn't match: ");

		double expectedReferenceLength = 0.024;
		assertEquals(expectedReferenceLength, config.getReferenceLength(), EPSILON, "source config reference length doesn't match: ");

		double expectedReferenceArea = Math.pow(expectedReferenceLength / 2, 2) * Math.PI;
		double actualReferenceArea = config.getReferenceArea();
		assertEquals(expectedReferenceArea, actualReferenceArea, EPSILON, "source config reference area doesn't match: ");
	}

	@Test
	public void testCloneBasic() {
		Rocket rkt1 = TestRockets.makeBeta();
		FlightConfiguration config1 = rkt1.getSelectedConfiguration();

		// preconditions
		config1.setAllStages();
		int expectedStageCount = 2;
		int actualStageCount = config1.getActiveStageCount();
		assertEquals(actualStageCount, expectedStageCount, "active stage count doesn't match");
		int expectedMotorCount = 2;
		int actualMotorCount = config1.getActiveMotors().size();
		assertEquals(actualMotorCount, expectedMotorCount, "active motor count doesn't match");
		double expectedLength = 0.335;
		assertEquals(expectedLength, config1.getLengthAerodynamic(), EPSILON, "source config length doesn't match: ");
		double expectedReferenceLength = 0.024;
		assertEquals(expectedReferenceLength, config1.getReferenceLength(), EPSILON, "source config reference length doesn't match: ");
		double expectedReferenceArea = Math.pow(expectedReferenceLength / 2, 2) * Math.PI;
		double actualReferenceArea = config1.getReferenceArea();
		assertEquals(expectedReferenceArea, actualReferenceArea, EPSILON, "source config reference area doesn't match: ");

		// vvvv test target vvvv
		FlightConfiguration config2 = config1.clone();
		// ^^^^ test target ^^^^

		// postconditions
		expectedStageCount = 2;
		actualStageCount = config2.getActiveStageCount();
		assertEquals(actualStageCount, expectedStageCount, "active stage count doesn't match");
		expectedMotorCount = 2;
		actualMotorCount = config2.getActiveMotors().size();
		assertEquals(actualMotorCount, expectedMotorCount, "active motor count doesn't match");
		assertEquals(expectedLength, config2.getLengthAerodynamic(), EPSILON, "source config length doesn't match: ");
		assertEquals(expectedReferenceLength, config2.getReferenceLength(), EPSILON, "source config reference length doesn't match: ");
		assertEquals(expectedReferenceArea, config2.getReferenceArea(), EPSILON, "source config reference area doesn't match: ");

	}

	/**
	 * Test flight configuration ID methods
	 */
	@Test
	public void testCloneIndependence() {
		Rocket rkt1 = TestRockets.makeBeta();
		FlightConfiguration config1 = rkt1.getSelectedConfiguration();
		int expectedStageCount;
		int actualStageCount;
		int expectedMotorCount;
		int actualMotorCount;

		// test that cloned configurations operate independently:
		// change #1, test clone #2 -- verify that cloned configurations change
		// independent.
		config1.setAllStages();
		// vvvv test target vvvv
		FlightConfiguration config2 = config1.clone();
		// ^^^^ test target ^^^^
		config1.clearAllStages();

		// postcondition: config #1
		expectedStageCount = 0;
		actualStageCount = config1.getActiveStageCount();
		assertEquals(actualStageCount, expectedStageCount, "active stage count doesn't match");
		expectedMotorCount = 0;
		actualMotorCount = config1.getActiveMotors().size();
		assertEquals(actualMotorCount, expectedMotorCount, "active motor count doesn't match");

		// postcondition: config #2
		expectedStageCount = 2;
		actualStageCount = config2.getActiveStageCount();
		assertEquals(actualStageCount, expectedStageCount, "active stage count doesn't match");
		expectedMotorCount = 2;
		actualMotorCount = config2.getActiveMotors().size();
		assertEquals(actualMotorCount, expectedMotorCount, "active motor count doesn't match");
	}

	/**
	 * Single stage rocket specific configuration tests
	 */
	@Test
	public void testSingleStageRocket() {
		Rocket r1 = TestRockets.makeEstesAlphaIII();
		FlightConfiguration config = r1.getSelectedConfiguration();

		// test explicitly setting only first stage active
		config.clearAllStages();
		config.setOnlyStage(0);

		// test that getStageCount() returns correct value
		int expectedStageCount = 1;
		int stageCount = config.getStageCount();
		assertEquals(stageCount, expectedStageCount, "stage count doesn't match");

		expectedStageCount = 1;
		stageCount = config.getActiveStageCount();
		assertEquals(stageCount, expectedStageCount, "active stage count doesn't match");

		// test explicitly setting all stages up to first stage active
		config.setOnlyStage(0);

		// test explicitly setting all stages active
		config.setAllStages();
	}

	/**
	 * Single stage rocket specific configuration tests
	 */
	@Test
	public void testDefaultConfigurationIsEmpty() {
		Rocket r1 = TestRockets.makeEstesAlphaIII();

		// don't change the configuration:
		FlightConfiguration defaultConfig = r1.getSelectedConfiguration();

		assertEquals(r1.getEmptyConfiguration().getActiveMotors().size(), 0, "Empty configuration has motors! it should be empty!");
		assertEquals(defaultConfig.getActiveMotors().size(), 0, "Default configuration is not the empty configuration. It should be!");
	}

	@Test
	public void testCreateConfigurationNullId() {
		/* Setup */
		Rocket rkt = TestRockets.makeEstesAlphaIII();

		// PRE-CONDITION:
		// test that all configurations correctly loaded:
		int expectedConfigCount = 5;
		int actualConfigCount = rkt.getConfigurationCount();
		assertEquals(actualConfigCount, expectedConfigCount, "number of loaded configuration counts doesn't actually match.");

		// create with
		rkt.createFlightConfiguration(null);
		expectedConfigCount = 6;
		actualConfigCount = rkt.getConfigurationCount();
		assertEquals(actualConfigCount, expectedConfigCount, "createFlightConfiguration with null: doesn't actually work.");
	}

	@Test
	public void testMotorConfigurations() {
		/* Setup */
		Rocket rkt = TestRockets.makeEstesAlphaIII();

		InnerTube smmt = (InnerTube) rkt.getChild(0).getChild(1).getChild(2);

		int expectedMotorCount = 5;
		int actualMotorCount = smmt.getMotorConfigurationSet().size();
		assertEquals(actualMotorCount, expectedMotorCount, "number of motor configurations doesn't match.");

	}

	@Test
	public void testFlightConfigurationGetters() {
		Rocket rkt = TestRockets.makeEstesAlphaIII();

		// test that all configurations correctly loaded:
		int expectedConfigCount = 5;
		int actualConfigCount = rkt.getConfigurationCount();
		assertEquals(actualConfigCount, expectedConfigCount, "number of loaded configuration counts doesn't actually match.");

		actualConfigCount = rkt.getIds().size();
		assertEquals(actualConfigCount, expectedConfigCount, "number of configuration array ids doesn't actually match.");

		// upon success, these silently complete.
		// upon failure, they'll throw exceptions:
		rkt.getFlightConfigurationByIndex(4);
		rkt.getFlightConfigurationByIndex(5, true);
	}

	@Test
	public void testGetFlightConfigurationOutOfBounds() {
		Rocket rkt = TestRockets.makeEstesAlphaIII();

		// test that all configurations correctly loaded:
		int expectedConfigCount = 5;
		int actualConfigCount = rkt.getConfigurationCount();
		assertEquals(actualConfigCount, expectedConfigCount, "number of loaded configuration counts doesn't actually match.");

		// this SHOULD throw an exception --
		// it's out of bounds on, and no configuration exists at index 5.
		assertThrows(IndexOutOfBoundsException.class, () -> {
			rkt.getFlightConfigurationByIndex(5);
		});
	}

	/**
	 * Multi stage rocket specific configuration tests
	 */
	@Test
	public void testMultiStageRocket() {

		/* Setup */
		Rocket rkt = TestRockets.makeBeta();
		FlightConfiguration config = rkt.getSelectedConfiguration();

		int expectedStageCount;
		int stageCount;

		expectedStageCount = 2;
		stageCount = config.getStageCount();
		assertEquals(stageCount, expectedStageCount, "stage count doesn't match");

		config.clearAllStages();
		assertFalse(config.isStageActive(0), " clear all stages: check #0: ");
		assertFalse(config.isStageActive(1), " clear all stages: check #1: ");

		// test explicitly setting only first stage active
		config.setOnlyStage(0);

		expectedStageCount = 1;
		stageCount = config.getActiveStageCount();
		assertEquals(stageCount, expectedStageCount, "active stage count doesn't match");

		assertTrue(config.isStageActive(0), " setting single stage active: ");

		// test explicitly setting all stages up to second stage active
		config.setOnlyStage(1);
		assertFalse(config.isStageActive(0), "Setting single stage active: ");
		assertTrue(config.isStageActive(1), "Setting single stage active: ");

		config.clearStage(0);
		assertFalse(config.isStageActive(0), " deactivate stage #0: ");
		assertTrue(config.isStageActive(1), "     active stage #1: ");

		// test explicitly setting all two stages active
		config.setAllStages();
		assertTrue(config.isStageActive(0), " activate all stages: check stage #0: ");
		assertTrue(config.isStageActive(1), " activate all stages: check stage #1: ");

		// test toggling single stage
		config.setAllStages();
		config.toggleStage(0);
		assertFalse(config.isStageActive(0), " toggle stage #0: ");

		config.toggleStage(0);
		assertTrue(config.isStageActive(0), " toggle stage #0: ");

		config.toggleStage(0);
		assertFalse(config.isStageActive(0), " toggle stage #0: ");

		AxialStage sustainer = rkt.getTopmostStage(config);
		AxialStage booster = rkt.getBottomCoreStage(config);
		assertEquals(sustainer.getStageNumber(), 1, " sustainer stage is stage #1: ");
		assertEquals(booster.getStageNumber(), 1, " booster stage is stage #1: ");

		config.setAllStages();
		config._setStageActive(1, false);
		sustainer = rkt.getTopmostStage(config);
		booster = rkt.getBottomCoreStage(config);
		assertEquals(sustainer.getStageNumber(), 0, " sustainer stage is stage #1: ");
		assertEquals(booster.getStageNumber(), 0, " booster stage is stage #1: ");

		config.setAllStages();
		sustainer = rkt.getTopmostStage(config);
		booster = rkt.getBottomCoreStage(config);
		assertEquals(sustainer.getStageNumber(), 0, " sustainer stage is stage #0: ");
		assertEquals(booster.getStageNumber(), 1, " booster stage is stage #1: ");

		config.clearAllStages();
		config.activateStagesThrough(sustainer);
		assertTrue(config.isStageActive(sustainer.getStageNumber()), " sustainer stage is active: ");
		assertFalse(config.isStageActive(booster.getStageNumber()), " booster stage is inactive: ");

		config.clearAllStages();
		config.activateStagesThrough(booster);
		assertTrue(config.isStageActive(sustainer.getStageNumber()), " sustainer stage is active: ");
		assertTrue(config.isStageActive(booster.getStageNumber()), " booster stage is active: ");

	}

	/**
	 * Multi stage rocket specific configuration tests
	 */
	@Test
	public void testMotorClusters() {

		/* Setup */
		Rocket rkt = TestRockets.makeBeta();
		FlightConfiguration config = rkt.getSelectedConfiguration();

		config.clearAllStages();
		int expectedMotorCount = 0;
		int actualMotorCount = config.getActiveMotors().size();
		assertEquals(actualMotorCount, expectedMotorCount, "active motor count doesn't match");

		config.setOnlyStage(0);
		expectedMotorCount = 1;
		actualMotorCount = config.getActiveMotors().size();
		assertEquals(actualMotorCount, expectedMotorCount, "active motor count doesn't match: ");

		config.setOnlyStage(1);
		expectedMotorCount = 1;
		actualMotorCount = config.getActiveMotors().size();
		assertEquals(actualMotorCount, expectedMotorCount, "active motor count doesn't match: ");

		config.setAllStages();
		expectedMotorCount = 2;
		actualMotorCount = config.getActiveMotors().size();
		assertEquals(actualMotorCount, expectedMotorCount, "active motor count doesn't match: ");
	}

	@Test
	public void testIterateComponents() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		FlightConfiguration selected = rocket.getSelectedConfiguration();

		selected.clearAllStages();
		selected.toggleStage(1);

		// vvvv Test Target vvvv
		InstanceMap instances = selected.getActiveInstances();
		// ^^^^ Test Target ^^^^

		// Payload Stage
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		{ // Core Stage
			final List<InstanceContext> coreStageContextList = instances.getInstanceContexts(coreStage);
			final InstanceContext coreStageContext = coreStageContextList.get(0);
			assertSame(coreStageContext.component.getClass(), AxialStage.class);
			assertEquals(coreStageContext.component.getID(), rocket.getChild(1).getID());
			assertEquals(coreStageContext.component.getInstanceCount(), 1);

			final Coordinate coreLocation = coreStageContext.getLocation();
			assertEquals(coreLocation.x, 0.564, EPSILON);
			assertEquals(coreLocation.y, 0.0, EPSILON);
			assertEquals(coreLocation.z, 0.0, EPSILON);

			// ... skip uninteresting component
		}

		// Booster Stage
		{ // instance #1
			final ParallelStage boosterStage = (ParallelStage) coreStage.getChild(0).getChild(0);
			final List<InstanceContext> boosterStageContextList = instances.getInstanceContexts(boosterStage);
			final InstanceContext boosterStage0Context = boosterStageContextList.get(0);
			assertSame(boosterStage0Context.component.getClass(), ParallelStage.class);
			assertEquals(boosterStage0Context.component.getID(), boosterStage.getID());
			assertEquals(boosterStage0Context.instanceNumber, 0);
			{
				final Coordinate loc = boosterStage0Context.getLocation();
				assertEquals(loc.x, 0.484, EPSILON);
				assertEquals(loc.y, 0.077, EPSILON);
				assertEquals(loc.z, 0.0, EPSILON);
			}

			final InstanceContext boosterStage1Context = boosterStageContextList.get(1);
			assertSame(boosterStage1Context.component.getClass(), ParallelStage.class);
			assertEquals(boosterStage1Context.component.getID(), boosterStage.getID());
			assertEquals(boosterStage1Context.instanceNumber, 1);
			{
				final Coordinate loc = boosterStage1Context.getLocation();
				assertEquals(loc.x, 0.484, EPSILON);
				assertEquals(loc.y, -0.077, EPSILON);
				assertEquals(loc.z, 0.0, EPSILON);
			}

			{ // Booster Body:
				final BodyTube boosterBody = (BodyTube) boosterStage.getChild(1);
				final List<InstanceContext> boosterBodyContextList = instances.getInstanceContexts(boosterBody);

				// this is the instance number rocket-wide
				final InstanceContext boosterBodyContext = boosterBodyContextList.get(1);

				// this is the instance number per-parent
				assertEquals(boosterBodyContext.instanceNumber, 0);

				assertSame(boosterBodyContext.component.getClass(), BodyTube.class);

				final Coordinate bodyTubeLocation = boosterBodyContext.getLocation();
				assertEquals(bodyTubeLocation.x, 0.564, EPSILON);
				assertEquals(bodyTubeLocation.y, -0.077, EPSILON);
				assertEquals(bodyTubeLocation.z, 0.0, EPSILON);

				{ // Booster::Motor Tubes ( x2 x4)
					final InnerTube boosterMMT = (InnerTube) boosterBody.getChild(0);
					final List<InstanceContext> mmtContextList = instances.getInstanceContexts(boosterMMT);
					assertEquals(8, mmtContextList.size());

					final InstanceContext motorTubeContext0 = mmtContextList.get(4);
					assertSame(motorTubeContext0.component.getClass(), InnerTube.class);
					assertEquals(motorTubeContext0.instanceNumber, 0);
					final Coordinate motorTube0Location = motorTubeContext0.getLocation();
					assertEquals(motorTube0Location.x, 1.214, EPSILON);
					assertEquals(motorTube0Location.y, -0.062, EPSILON);
					assertEquals(motorTube0Location.z, -0.015, EPSILON);

					final InstanceContext motorTubeContext1 = mmtContextList.get(5);
					assertSame(motorTubeContext1.component.getClass(), InnerTube.class);
					assertEquals(motorTubeContext1.instanceNumber, 1);
					final Coordinate motorTube1Location = motorTubeContext1.getLocation();
					assertEquals(motorTube1Location.x, 1.214, EPSILON);
					assertEquals(motorTube1Location.y, -0.092, EPSILON);
					assertEquals(motorTube1Location.z, -0.015, EPSILON);

					final InstanceContext motorTubeContext2 = mmtContextList.get(6);
					assertSame(motorTubeContext2.component.getClass(), InnerTube.class);
					assertEquals(motorTubeContext2.instanceNumber, 2);
					final Coordinate motorTube2Location = motorTubeContext2.getLocation();
					assertEquals(motorTube2Location.x, 1.214, EPSILON);
					assertEquals(motorTube2Location.y, -0.092, EPSILON);
					assertEquals(motorTube2Location.z, 0.015, EPSILON);

					final InstanceContext motorTubeContext3 = mmtContextList.get(7);
					assertSame(motorTubeContext3.component.getClass(), InnerTube.class);
					assertEquals(motorTubeContext3.instanceNumber, 3);
					final Coordinate motorTube3Location = motorTubeContext3.getLocation();
					assertEquals(motorTube3Location.x, 1.214, EPSILON);
					assertEquals(motorTube3Location.y, -0.062, EPSILON);
					assertEquals(motorTube3Location.z, 0.015, EPSILON);

				}
				{ // Booster::Fins::Instances ( x2 x3)
					final FinSet fins = (FinSet) boosterBody.getChild(1);
					final List<InstanceContext> finContextList = instances.getInstanceContexts(fins);
					assertEquals(6, finContextList.size());

					final InstanceContext boosterFinContext0 = finContextList.get(3);
					assertSame(boosterFinContext0.component.getClass(), TrapezoidFinSet.class);
					assertEquals(boosterFinContext0.instanceNumber, 0);
					final Coordinate boosterFin0Location = boosterFinContext0.getLocation();
					assertEquals(1.044, boosterFin0Location.x, EPSILON);
					assertEquals(-0.1155, boosterFin0Location.y, EPSILON);
					assertEquals(0.0, boosterFin0Location.z, EPSILON);

					final InstanceContext boosterFinContext1 = finContextList.get(4);
					assertSame(boosterFinContext1.component.getClass(), TrapezoidFinSet.class);
					assertEquals(boosterFinContext1.instanceNumber, 1);
					final Coordinate boosterFin1Location = boosterFinContext1.getLocation();
					assertEquals(1.044, boosterFin1Location.x, EPSILON);
					assertEquals(-0.05775, boosterFin1Location.y, EPSILON);
					assertEquals(-0.033341978, boosterFin1Location.z, EPSILON);

					final InstanceContext boosterFinContext2 = finContextList.get(5);
					assertSame(boosterFinContext2.component.getClass(), TrapezoidFinSet.class);
					assertEquals(boosterFinContext2.instanceNumber, 2);
					final Coordinate boosterFin2Location = boosterFinContext2.getLocation();
					assertEquals(1.044, boosterFin2Location.x, EPSILON);
					assertEquals(-0.05775, boosterFin2Location.y, EPSILON);
					assertEquals(0.03334, boosterFin2Location.z, EPSILON);
				}

			}

		}
	}

	@Test
	public void testIterateCoreComponents_AllStagesActive() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		FlightConfiguration selected = rocket.getSelectedConfiguration();

		selected.setAllStages();

		// vvvv Test Target vvvv
		ArrayList<RocketComponent> components = selected.getCoreComponents();
		// ^^^^ Test Target ^^^^

		assertEquals(components.size(), 10);

		final AxialStage payloadStage = (AxialStage) components.get(0);
		assertEquals(payloadStage.getName(), "Payload Fairing Stage");

		final AxialStage coreStage = (AxialStage) components.get(1);
		assertEquals(coreStage.getName(), "Core Stage");

		assertInstanceOf(NoseCone.class, components.get(2));

		assertInstanceOf(BodyTube.class, components.get(3));
		assertEquals(components.get(3).getName(), "PL Fairing Body");

		assertInstanceOf(Transition.class, components.get(4));

		assertInstanceOf(BodyTube.class, components.get(5));
		assertEquals(components.get(5).getName(), "Upper Stage Body");

		assertInstanceOf(BodyTube.class, components.get(6));
		assertEquals(components.get(6).getName(), "Interstage");

		assertInstanceOf(BodyTube.class, components.get(7));
		assertEquals(components.get(7).getName(), "Core Stage Body");

		assertInstanceOf(Parachute.class, components.get(8));
		assertInstanceOf(ShockCord.class, components.get(9));
	}

	@Test
	public void testIterateCoreComponents_ActiveOnly() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		FlightConfiguration selected = rocket.getSelectedConfiguration();

		selected.clearAllStages();
		selected.toggleStage(2); // booster only.

		// vvvv Test Target vvvv
		ArrayList<RocketComponent> components = selected.getCoreComponents();
		// ^^^^ Test Target ^^^^

		assertEquals(components.size(), 0);

		// =================================
		selected.clearAllStages();
		selected.toggleStage(1); // booster only.

		// vvvv Test Target vvvv
		components = selected.getCoreComponents();
		// ^^^^ Test Target ^^^^

		assertEquals(components.size(), 2);

		final AxialStage coreStage = (AxialStage) components.get(0);
		assertEquals(coreStage.getName(), "Core Stage");

		assertInstanceOf(BodyTube.class, components.get(1));
		assertEquals(components.get(1).getName(), "Core Stage Body");

	}

	@Test
	public void testName() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		FlightConfiguration selected = rocket.getSelectedConfiguration();

		ParallelStage boosterStage = (ParallelStage) rocket.getChild(1).getChild(0).getChild(0);

		// Parse over different instances
		for (int i = 1; i < 3; i++) {
			boosterStage.setInstanceCount(i);

			// Test only motors
			selected.setName("[{motors}]");

			selected.setAllStages();
			assertEquals("[[Rocket.motorCount.noStageMotors]; M1350-0; " + 4*i + "\u00D7 G77-0]", selected.getName());

			// Test only manufacturers
			selected.setName("[{manufacturers}]");

			selected.setAllStages();
			assertEquals("[[Rocket.motorCount.noStageMotors]; AeroTech; " + 4*i + "\u00D7 AeroTech]", selected.getName());

			// Test only cases
			selected.setName("[{cases}]");

			selected.setAllStages();
			assertEquals("[[Rocket.motorCount.noStageMotors]; SU 75/512; " + 4*i + "\u00D7 SU 29/180]", selected.getName());

			// Test only motors or only manufacturers
			selected.setName("[{motors}] - [{manufacturers}]");

			selected.setAllStages();
			assertEquals("[[Rocket.motorCount.noStageMotors]; M1350-0; " + 4*i + "\u00D7 G77-0] - [[Rocket.motorCount.noStageMotors]; AeroTech; " + 4*i + "\u00D7 AeroTech]",
					selected.getName());

			selected.setOnlyStage(0);
			assertEquals("[[Rocket.motorCount.Nomotor]] - [[Rocket.motorCount.Nomotor]]", selected.getName());

			selected.setOnlyStage(1);
			assertEquals("[; M1350-0; ] - [; AeroTech; ]", selected.getName());

			selected.setAllStages();
			selected._setStageActive(0, false);
			assertEquals("[; M1350-0; " + 4*i + "\u00D7 G77-0] - [; AeroTech; " + 4*i + "\u00D7 AeroTech]", selected.getName());

			// Test combination of motors and manufacturers
			selected.setName("[{motors  manufacturers}] -- [{manufacturers}] - [{motors}]");

			selected.setAllStages();
			assertEquals("[[Rocket.motorCount.noStageMotors]; M1350-0  AeroTech; " + 4*i + "\u00D7 G77-0  AeroTech] -- [[Rocket.motorCount.noStageMotors]; AeroTech; " + 4*i + "\u00D7 AeroTech] - [[Rocket.motorCount.noStageMotors]; M1350-0; " + 4*i + "\u00D7 G77-0]",
					selected.getName());

			selected.setOnlyStage(0);
			assertEquals("[[Rocket.motorCount.Nomotor]] -- [[Rocket.motorCount.Nomotor]] - [[Rocket.motorCount.Nomotor]]", selected.getName());

			selected.setOnlyStage(1);
			assertEquals("[; M1350-0  AeroTech; ] -- [; AeroTech; ] - [; M1350-0; ]", selected.getName());

			selected.setAllStages();
			selected._setStageActive(0, false);
			assertEquals("[; M1350-0  AeroTech; " + 4*i + "\u00D7 G77-0  AeroTech] -- [; AeroTech; " + 4*i + "\u00D7 AeroTech] - [; M1350-0; " + 4*i + "\u00D7 G77-0]",
					selected.getName());

			// Test combination of manufacturers and motors
			selected.setName("[{manufacturers | motors}]");

			selected.setAllStages();
			assertEquals("[[Rocket.motorCount.noStageMotors]; AeroTech | M1350-0; " + 4*i + "\u00D7 AeroTech | G77-0]",
					selected.getName());

			selected.setOnlyStage(0);
			assertEquals("[[Rocket.motorCount.Nomotor]]", selected.getName());

			selected.setOnlyStage(1);
			assertEquals("[; AeroTech | M1350-0; ]", selected.getName());

			selected.setAllStages();
			selected._setStageActive(0, false);
			assertEquals("[; AeroTech | M1350-0; " + 4*i + "\u00D7 AeroTech | G77-0]", selected.getName());

			// Test combination of motors, manufacturers and cases
			selected.setName("[{motors manufacturers | cases}]");

			selected.setAllStages();
			assertEquals("[[Rocket.motorCount.noStageMotors]; M1350-0 AeroTech | SU 75/512; " + 4*i + "\u00D7 G77-0 AeroTech | SU 29/180]", selected.getName());

			// Test combination of motors, manufacturers and cases
			selected.setName("[{motors manufacturers | cases}]");

			selected.setAllStages();
			assertEquals("[[Rocket.motorCount.noStageMotors]; M1350-0 AeroTech | SU 75/512; " + 4*i + "\u00D7 G77-0 AeroTech | SU 29/180]", selected.getName());
		}

		// Test empty tags
		selected.setName("{}");

		selected.setAllStages();
		assertEquals("{}", selected.getName());

		selected.setOnlyStage(0);
		assertEquals("{}", selected.getName());

		selected.setOnlyStage(1);
		assertEquals("{}", selected.getName());		

		selected.setAllStages();
		selected._setStageActive(0, false);
		assertEquals("{}", selected.getName());		

		// Test invalid tags (1)
		selected.setName("{motorms}");

		selected.setAllStages();
		assertEquals("{motorms}", selected.getName());

		selected.setOnlyStage(0);
		assertEquals("{motorms}", selected.getName());

		selected.setOnlyStage(1);
		assertEquals("{motorms}", selected.getName());

		selected.setName("{motor}");

		selected.setAllStages();
		selected._setStageActive(0, false);
		assertEquals("{motor}", selected.getName());

		// Test invalid tags (2)
		selected.setName("{mot'ors manuf'acturers '}");

		selected.setAllStages();
		assertEquals("{mot'ors manuf'acturers '}", selected.getName());

		selected.setOnlyStage(0);
		assertEquals("{mot'ors manuf'acturers '}", selected.getName());

		selected.setOnlyStage(1);
		assertEquals("{mot'ors manuf'acturers '}", selected.getName());

		selected.setAllStages();
		selected._setStageActive(0, false);
		assertEquals("{mot'ors manuf'acturers '}", selected.getName());
	}

	@Test
	public void testCopy() throws NoSuchFieldException, IllegalAccessException {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		FlightConfiguration original = rocket.getSelectedConfiguration();
		original.setName("[{motors}] - [{manufacturers}]");
		original.setOnlyStage(0);

		// vvvv Test Target vvvv
		FlightConfiguration copy = original.copy(null);
		// ^^^^ Test Target ^^^^

		assertNotEquals(original, copy);
		assertNotSame(original, copy);
		assertEquals(original.getName(), copy.getName());
		assertNotEquals(original.getFlightConfigurationID(), copy.getFlightConfigurationID());

		// Test preloadStageActiveness copy
		Field preloadStageActivenessField = FlightConfiguration.class.getDeclaredField("preloadStageActiveness");
		preloadStageActivenessField.setAccessible(true);
		Map<Integer, Boolean> preloadStageActivenessOriginal = (Map<Integer, Boolean>) preloadStageActivenessField
				.get(original);
		Map<Integer, Boolean> preloadStageActivenessCopy = (Map<Integer, Boolean>) preloadStageActivenessField
				.get(copy);
		assertEquals(preloadStageActivenessOriginal, preloadStageActivenessCopy);
		if (preloadStageActivenessOriginal == null) {
			assertNull(preloadStageActivenessCopy);
		} else {
			assertNotSame(preloadStageActivenessOriginal, preloadStageActivenessCopy);
		}

		// Test cachedBoundsAerodynamic copy
		Field cachedBoundsAerodynamicField = FlightConfiguration.class.getDeclaredField("cachedBoundsAerodynamic");
		cachedBoundsAerodynamicField.setAccessible(true);
		BoundingBox cachedBoundsAerodynamicOriginal = (BoundingBox) cachedBoundsAerodynamicField.get(original);
		BoundingBox cachedBoundsAerodynamicCopy = (BoundingBox) cachedBoundsAerodynamicField.get(copy);
		assertEquals(cachedBoundsAerodynamicOriginal, cachedBoundsAerodynamicCopy);
		assertNotSame(cachedBoundsAerodynamicOriginal, cachedBoundsAerodynamicCopy);

		// Test cachedBounds copy
		Field cachedBoundsField = FlightConfiguration.class.getDeclaredField("cachedBounds");
		cachedBoundsField.setAccessible(true);
		BoundingBox cachedBoundsOriginal = (BoundingBox) cachedBoundsField.get(original);
		BoundingBox cachedBoundsCopy = (BoundingBox) cachedBoundsField.get(copy);
		assertEquals(cachedBoundsOriginal, cachedBoundsCopy);
		assertNotSame(cachedBoundsOriginal, cachedBoundsCopy);

		// Test modID copy
		assertEquals(original.getModID(), copy.getModID());

		// Test boundModID
		Field boundsModIDField = FlightConfiguration.class.getDeclaredField("boundsModID");
		boundsModIDField.setAccessible(true);
		ModID boundsModIDCopy = (ModID) boundsModIDField.get(copy);
		assertEquals(ModID.INVALID, boundsModIDCopy);

		// Test refLengthModID
		Field refLengthModIDField = FlightConfiguration.class.getDeclaredField("refLengthModID");
		refLengthModIDField.setAccessible(true);
		ModID refLengthModIDCopy = (ModID) refLengthModIDField.get(copy);
		assertEquals(ModID.INVALID, refLengthModIDCopy);

		// Test stageActiveness copy
		for (int i = 0; i < original.getStageCount(); i++) {
			assertEquals(original.isStageActive(i), copy.isStageActive(i));
		}
	}

	@Test
	public void testClone() throws NoSuchFieldException, IllegalAccessException {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		FlightConfiguration original = rocket.getSelectedConfiguration();
		original.setOnlyStage(0);

		// vvvv Test Target vvvv
		FlightConfiguration clone = original.clone();
		// ^^^^ Test Target ^^^^

		assertEquals(original, clone);
		assertNotSame(original, clone);
		assertEquals(original.getName(), clone.getName());
		assertEquals(original.getFlightConfigurationID(), clone.getFlightConfigurationID());

		// Test preloadStageActiveness clone
		Field preloadStageActivenessField = FlightConfiguration.class.getDeclaredField("preloadStageActiveness");
		preloadStageActivenessField.setAccessible(true);
		Map<Integer, Boolean> preloadStageActivenessOriginal = (Map<Integer, Boolean>) preloadStageActivenessField
				.get(original);
		Map<Integer, Boolean> preloadStageActivenessClone = (Map<Integer, Boolean>) preloadStageActivenessField
				.get(clone);
		assertEquals(preloadStageActivenessOriginal, preloadStageActivenessClone);
		if (preloadStageActivenessOriginal == null) {
			assertNull(preloadStageActivenessClone);
		} else {
			assertNotSame(preloadStageActivenessOriginal, preloadStageActivenessClone);
		}

		// Test cachedBoundsAerodynamic clone
		Field cachedBoundsAerodynamicField = FlightConfiguration.class.getDeclaredField("cachedBoundsAerodynamic");
		cachedBoundsAerodynamicField.setAccessible(true);
		BoundingBox cachedBoundsAerodynamicOriginal = (BoundingBox) cachedBoundsAerodynamicField.get(original);
		BoundingBox cachedBoundsAerodynamicClone = (BoundingBox) cachedBoundsAerodynamicField.get(clone);
		assertEquals(cachedBoundsAerodynamicOriginal, cachedBoundsAerodynamicClone);
		assertNotSame(cachedBoundsAerodynamicOriginal, cachedBoundsAerodynamicClone);

		// Test cachedBounds clone
		Field cachedBoundsField = FlightConfiguration.class.getDeclaredField("cachedBounds");
		cachedBoundsField.setAccessible(true);
		BoundingBox cachedBoundsOriginal = (BoundingBox) cachedBoundsField.get(original);
		BoundingBox cachedBoundsClone = (BoundingBox) cachedBoundsField.get(clone);
		assertEquals(cachedBoundsOriginal, cachedBoundsClone);
		assertNotSame(cachedBoundsOriginal, cachedBoundsClone);

		// Test modID clone
		assertEquals(original.getModID(), clone.getModID());

		// Test boundModID
		Field boundsModIDField = FlightConfiguration.class.getDeclaredField("boundsModID");
		boundsModIDField.setAccessible(true);
		ModID boundsModIDClone = (ModID) boundsModIDField.get(clone);
		assertEquals(ModID.INVALID, boundsModIDClone);

		// Test refLengthModID
		Field refLengthModIDField = FlightConfiguration.class.getDeclaredField("refLengthModID");
		refLengthModIDField.setAccessible(true);
		ModID refLengthModIDClone = (ModID) refLengthModIDField.get(clone);
		assertEquals(ModID.INVALID, refLengthModIDClone);

		// Test stageActiveness copy
		for (int i = 0; i < original.getStageCount(); i++) {
			assertEquals(original.isStageActive(i), clone.isStageActive(i));
		}
	}
}
