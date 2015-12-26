package net.sf.openrocket.rocketcomponent;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.rocketcomponent.RocketComponent.Position;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class FlightConfigurationTest extends BaseTestCase {
	
	private final static double EPSILON = MathUtil.EPSILON*1E3; 
	
	/**
	 * Empty rocket (no components) specific configuration tests
	 */
	@Test
	public void testEmptyRocket() {
		Rocket r1 = makeEmptyRocket();
		FlightConfiguration config = r1.getSelectedConfiguration();
		
		FlightConfiguration configClone = config.clone();
		
		assertTrue(config.getRocket() == configClone.getRocket());
	}
	
	/**
	 * Test flight configuration ID methods
	 */
	@Test
	public void testCloneBasic() {
		Rocket rkt1 = makeTwoStageMotorRocket();
		FlightConfiguration config1 = rkt1.getSelectedConfiguration();
		
		// preconditions
		config1.setAllStages();
		int expectedStageCount = 2;
		int actualStageCount = config1.getActiveStageCount();
		assertThat("active stage count doesn't match", actualStageCount, equalTo(expectedStageCount));
		int expectedMotorCount = 2;
		int actualMotorCount = config1.getActiveMotors().size();
		assertThat("active motor count doesn't match", actualMotorCount, equalTo(expectedMotorCount));
		double expectedLength = 176.8698848;
		double actualLength = config1.getLength();
		assertEquals("source config length doesn't match: ", expectedLength, actualLength, EPSILON);
		double expectedReferenceLength = 2.5;
		double actualReferenceLength = config1.getReferenceLength();
		assertEquals("source config reference length doesn't match: ", expectedReferenceLength, actualReferenceLength, EPSILON);
		double expectedReferenceArea = 4.9087385212;
		double actualReferenceArea = config1.getReferenceArea();
		assertEquals("source config reference area doesn't match: ", expectedReferenceArea, actualReferenceArea, EPSILON);

		// vvvv test target vvvv 
		FlightConfiguration config2= config1.clone();
		// ^^^^ test target ^^^^
		
		// postconditions
		expectedStageCount = 2;
		actualStageCount = config2.getActiveStageCount();
		assertThat("active stage count doesn't match", actualStageCount, equalTo(expectedStageCount));
		expectedMotorCount = 2;
		actualMotorCount = config2.getActiveMotors().size();
		assertThat("active motor count doesn't match", actualMotorCount, equalTo(expectedMotorCount));
		actualLength = config2.getLength();
		assertEquals("source config length doesn't match: ", expectedLength, actualLength, EPSILON);
		actualReferenceLength = config2.getReferenceLength();
		assertEquals("source config reference length doesn't match: ", expectedReferenceLength, actualReferenceLength, EPSILON);
		actualReferenceArea = config2.getReferenceArea();
		assertEquals("source config reference area doesn't match: ", expectedReferenceArea, actualReferenceArea, EPSILON);

	}
	
	/**
	 * Test flight configuration ID methods
	 */
	@Test
	public void testCloneIndependence() {
		Rocket rkt1 = makeTwoStageMotorRocket();
		FlightConfiguration config1 = rkt1.getSelectedConfiguration();
		int expectedStageCount;
		int actualStageCount;
		int expectedMotorCount;
		int actualMotorCount;
		
		// test that cloned configurations operate independently:
		// change #1, test clone #2 -- verify that cloned configurations change independent.
		config1.setAllStages();
		// vvvv test target vvvv 
		FlightConfiguration config2 = config1.clone();
		// ^^^^ test target ^^^^
		config1.clearAllStages();

		// postcondition: config #1
		expectedStageCount = 0;
		actualStageCount = config1.getActiveStageCount();
		assertThat("active stage count doesn't match", actualStageCount, equalTo(expectedStageCount));
		expectedMotorCount = 0;
		actualMotorCount = config1.getActiveMotors().size();
		assertThat("active motor count doesn't match", actualMotorCount, equalTo(expectedMotorCount));
		
		// postcondition: config #2
		expectedStageCount = 2;
		actualStageCount = config2.getActiveStageCount();
		assertThat("active stage count doesn't match", actualStageCount, equalTo(expectedStageCount));
		expectedMotorCount = 2;
		actualMotorCount = config2.getActiveMotors().size();
		assertThat("active motor count doesn't match", actualMotorCount, equalTo(expectedMotorCount));
	}
	
	/**
	 * Single stage rocket specific configuration tests
	 */
	@Test
	public void testSingleStageRocket() {
		
		/* Setup */
		Rocket r1 = makeSingleStageTestRocket();
		FlightConfiguration config = r1.getSelectedConfiguration();
		
		// test explicitly setting only first stage active
		config.clearAllStages();
		config.setOnlyStage(0);
		
		//config.dumpConfig();
		//System.err.println("treedump: \n" + treedump);
		
		// test that getStageCount() returns correct value
		int expectedStageCount = 1;
		int stageCount = config.getStageCount();
		assertTrue("stage count doesn't match", stageCount == expectedStageCount);
		
		expectedStageCount = 1;
		stageCount = config.getActiveStageCount();
		assertThat("active stage count doesn't match", stageCount, equalTo(expectedStageCount));
		
		// test explicitly setting all stages up to first stage active
		config.setOnlyStage(0);
		
		// test explicitly setting all stages active
		config.setAllStages();
		
	}
	
	/**
	 * Multi stage rocket specific configuration tests
	 */
	@Test
	public void testMultiStageRocket() {
		
		/* Setup */
		Rocket r1 = makeTwoStageTestRocket();
		FlightConfiguration config = r1.getSelectedConfiguration();
		
		int expectedStageCount;
		int stageCount;
		
		expectedStageCount = 2;
		stageCount = config.getStageCount();
		assertThat("stage count doesn't match", stageCount, equalTo(expectedStageCount));
		
		config.clearAllStages();
		assertThat(" clear all stages: check #0: ", config.isStageActive(0), equalTo(false));
		assertThat(" clear all stages: check #1: ", config.isStageActive(1), equalTo(false));
		
		// test explicitly setting only first stage active
		config.setOnlyStage(0);
		
		expectedStageCount = 1;
		stageCount = config.getActiveStageCount();
		assertThat("active stage count doesn't match", stageCount, equalTo(expectedStageCount));
		
		assertThat(" setting single stage active: ", config.isStageActive(0), equalTo(true));
		
		// test explicitly setting all stages up to second stage active
		config.setOnlyStage(1);
		assertThat("Setting single stage active: ", config.isStageActive(0), equalTo(false));
		assertThat("Setting single stage active: ", config.isStageActive(1), equalTo(true));
		
		config.clearStage(0);
		assertThat(" deactivate stage #0: ", config.isStageActive(0), equalTo(false));
		assertThat("     active stage #1: ", config.isStageActive(1), equalTo(true));
		
		// test explicitly setting all two stages active
		config.setAllStages();
		assertThat(" activate all stages: check stage #0: ", config.isStageActive(0), equalTo(true));
		assertThat(" activate all stages: check stage #1: ", config.isStageActive(1), equalTo(true));
		
		// test toggling single stage
		config.setAllStages();
		config.toggleStage(0);
		assertThat(" toggle stage #0: ", config.isStageActive(0), equalTo(false));
		
		config.toggleStage(0);
		assertThat(" toggle stage #0: ", config.isStageActive(0), equalTo(true));
		
		config.toggleStage(0);
		assertThat(" toggle stage #0: ", config.isStageActive(0), equalTo(false));
		
	}
	
	/**
	 * Multi stage rocket specific configuration tests
	 */
	@Test
	public void testMotorClusters() {
		
		/* Setup */
		Rocket rkt = makeTwoStageMotorRocket();
		FlightConfiguration config = rkt.getSelectedConfiguration();
		
		
		config.clearAllStages();
		int expectedMotorCount = 0;
		int actualMotorCount = config.getActiveMotors().size();
		assertThat("active motor count doesn't match", actualMotorCount, equalTo(expectedMotorCount));
		
		config.setOnlyStage(0);
		expectedMotorCount = 1;
		actualMotorCount = config.getActiveMotors().size();
		assertThat("active motor count doesn't match: ", actualMotorCount, equalTo(expectedMotorCount));

		config.setOnlyStage(1);
		expectedMotorCount = 1;
		actualMotorCount = config.getActiveMotors().size();
		assertThat("active motor count doesn't match: ", actualMotorCount, equalTo(expectedMotorCount));

		config.setAllStages();
		expectedMotorCount = 2;
		actualMotorCount = config.getActiveMotors().size();
		assertThat("active motor count doesn't match: ", actualMotorCount, equalTo(expectedMotorCount));
	}
	
	//////////////////// Test Rocket Creation Methods /////////////////////////
	
	public static Rocket makeEmptyRocket() {
		Rocket rocket = new Rocket();
		rocket.enableEvents();
		return rocket;
	}
	
	public static Rocket makeSingleStageTestRocket() {
		
		// TODO: get units correct, these units are prob wrong, are lengths are CM, mass are grams
		
		Rocket rocket;
		AxialStage stage;
		NoseCone nosecone;
		BodyTube tube1;
		TrapezoidFinSet finset;
		
		// body tube constants
		final double R = 2.5 / 2; // cm
		final double BT_T = 0.1;
		
		// nose cone constants
		final double NC_T = 0.2;
		final double R2 = 2.3 / 2;
		
		rocket = new Rocket();
		stage = new AxialStage();
		stage.setName("Stage1");
		
		nosecone = new NoseCone(Transition.Shape.OGIVE, 10.0, R);
		nosecone.setThickness(NC_T);
		nosecone.setAftShoulderLength(2.0);
		nosecone.setAftShoulderRadius(R2);
		nosecone.setAftShoulderThickness(NC_T);
		nosecone.setAftShoulderCapped(true);
		nosecone.setFilled(false);
		stage.addChild(nosecone);
		
		tube1 = new BodyTube(30, R, BT_T);
		stage.addChild(tube1);
		
		LaunchLug lug = new LaunchLug();
		lug.setLength(3.5);
		tube1.addChild(lug);
		
		/*
		TubeCoupler coupler = new TubeCoupler();
		coupler.setOuterRadiusAutomatic(true);
		coupler.setThickness(0.005);
		coupler.setLength(0.28);
		coupler.setMassOverridden(true);
		coupler.setOverrideMass(0.360);
		coupler.setRelativePosition(Position.BOTTOM);
		coupler.setPositionValue(-0.14);
		tube1.addChild(coupler);
		*/
		
		// Parachute
		MassComponent mass = new MassComponent(4.5, R2, 8.0);
		mass.setRelativePosition(Position.TOP);
		mass.setPositionValue(3.0);
		tube1.addChild(mass);
		
		// Cord
		mass = new MassComponent(40.0, R2, 72);
		mass.setRelativePosition(Position.TOP);
		mass.setPositionValue(2.0);
		tube1.addChild(mass);
		
		// Motor mount
		InnerTube inner = new InnerTube();
		inner.setName("Sustainer MMT");
		inner.setPositionValue(0.5);
		inner.setRelativePosition(Position.BOTTOM);
		inner.setOuterRadius(1.9 / 2);
		inner.setInnerRadius(1.8 / 2);
		inner.setLength(7.5);
		tube1.addChild(inner);
		
		// Motor 
		
		// Centering rings for motor mount
		CenteringRing center = new CenteringRing();
		center.setInnerRadiusAutomatic(true);
		center.setOuterRadiusAutomatic(true);
		center.setLength(0.005);
		center.setMassOverridden(true);
		center.setOverrideMass(0.038);
		center.setRelativePosition(Position.BOTTOM);
		center.setPositionValue(0.25);
		tube1.addChild(center);
		
		center = new CenteringRing();
		center.setInnerRadiusAutomatic(true);
		center.setOuterRadiusAutomatic(true);
		center.setLength(0.005);
		center.setMassOverridden(true);
		center.setOverrideMass(0.038);
		center.setRelativePosition(Position.BOTTOM);
		center.setPositionValue(-6.0);
		tube1.addChild(center);
		
		
		center = new CenteringRing();
		center.setInnerRadiusAutomatic(true);
		center.setOuterRadiusAutomatic(true);
		center.setLength(0.005);
		center.setMassOverridden(true);
		center.setOverrideMass(0.038);
		center.setRelativePosition(Position.TOP);
		center.setPositionValue(0.83);
		tube1.addChild(center);
		
		// Fins
		finset = new TrapezoidFinSet();
		finset.setFinCount(3);
		finset.setRootChord(5.0);
		finset.setTipChord(5.0);
		finset.setHeight(3.0);
		finset.setThickness(0.005);
		finset.setSweepAngle(40.0);
		finset.setRelativePosition(Position.BOTTOM);
		finset.setPositionValue(-0.5);
		finset.setBaseRotation(Math.PI / 2);
		tube1.addChild(finset);
		
		// Stage construction
		rocket.addChild(stage);
		rocket.setPerfectFinish(false);
		rocket.enableEvents();

		final int expectedStageCount = 1;
		assertThat(" rocket has incorrect stage count: ", rocket.getStageCount(), equalTo(expectedStageCount));
		
		int expectedConfigurationCount = 0;
		assertThat(" configuration list contains : ", rocket.getFlightConfigurationCount(), equalTo(expectedConfigurationCount));
		
		FlightConfiguration newConfig = new FlightConfiguration(rocket,null);
		rocket.setFlightConfiguration( newConfig.getId(), newConfig);
		rocket.setDefaultConfiguration( newConfig.getId());
		assertThat(" configuration updates stage Count correctly: ", newConfig.getActiveStageCount(), equalTo(expectedStageCount));
		expectedConfigurationCount = 1;
		assertThat(" configuration list contains : ", rocket.getFlightConfigurationCount(), equalTo(expectedConfigurationCount));
		
		rocket.update();
		rocket.enableEvents();
		return rocket;
	}
	
	
	public static Rocket makeTwoStageTestRocket() {
		
		// TODO: get units correct, these units are prob wrong, are lengths are CM, mass are grams
		
		final double R = 2.5 / 2; // cm
		final double BT_T = 0.1;
		
		Rocket rocket = makeSingleStageTestRocket();
		
		AxialStage stage = new AxialStage();
		stage.setName("Booster");
		
		BodyTube boosterTube = new BodyTube(9.0, R, BT_T);
		stage.addChild(boosterTube);
		
		TubeCoupler coupler = new TubeCoupler();
		coupler.setOuterRadiusAutomatic(true);
		coupler.setThickness(BT_T);
		coupler.setLength(3.0);
		coupler.setRelativePosition(Position.TOP);
		coupler.setPositionValue(-1.5);
		boosterTube.addChild(coupler);
		
		TrapezoidFinSet finset = new TrapezoidFinSet();
		finset.setFinCount(3);
		finset.setRootChord(5.0);
		finset.setTipChord(5.0);
		finset.setHeight(3.0);
		finset.setThickness(0.005);
		finset.setSweepAngle(40.0);
		finset.setRelativePosition(Position.BOTTOM);
		finset.setPositionValue(-0.25);
		finset.setBaseRotation(Math.PI / 2);
		boosterTube.addChild(finset);
		
		// Motor mount
		InnerTube inner = new InnerTube();
		inner.setName("Booster MMT");
		inner.setPositionValue(0.5);
		inner.setRelativePosition(Position.BOTTOM);
		inner.setOuterRadius(1.9 / 2);
		inner.setInnerRadius(1.8 / 2);
		inner.setLength(7.5);
		boosterTube.addChild(inner);
		
		rocket.addChild(stage);
		
		// already set in "makeSingleStageTestRocket()" above...
//		rocket.enableEvents();
//		FlightConfiguration newConfig = new FlightConfiguration(rocket,null);
//		rocket.setFlightConfiguration( newConfig.getId(), newConfig);
		
		rocket.update();
		rocket.enableEvents();
		return rocket;	
	}

	public static Rocket makeTwoStageMotorRocket() {
		Rocket rocket = makeTwoStageTestRocket();
		FlightConfigurationId fcid = rocket.getSelectedConfiguration().getId();
		
		{
			// public ThrustCurveMotor(Manufacturer manufacturer, String designation, String description,
			//			Motor.Type type, double[] delays, double diameter, double length,
			//			double[] time, double[] thrust,
			//          Coordinate[] cg, String digest);
			ThrustCurveMotor sustainerMotor = new ThrustCurveMotor(
					Manufacturer.getManufacturer("AeroTech"),"D10", "Desc", 
					Motor.Type.SINGLE, new double[] {3,5,7},0.018, 0.07,
					new double[] { 0, 1, 2 }, new double[] { 0, 25, 0 },
					new Coordinate[] {
						new Coordinate(.035, 0, 0, 0.026),new Coordinate(.035, 0, 0, .021),new Coordinate(.035, 0, 0, 0.016)}, 
					"digest D10 test");
						
			InnerTube sustainerMount = (InnerTube) rocket.getChild(0).getChild(1).getChild(3);
			sustainerMount.setMotorMount(true);
			sustainerMount.setMotorInstance(fcid, new MotorConfiguration(sustainerMotor));
		}
		
		{
			// public ThrustCurveMotor(Manufacturer manufacturer, String designation, String description,
			//			Motor.Type type, double[] delays, double diameter, double length,
			//			double[] time, double[] thrust,
			//          Coordinate[] cg, String digest);
			ThrustCurveMotor boosterMotor = new ThrustCurveMotor(
					Manufacturer.getManufacturer("AeroTech"),"D21", "Desc", 
					Motor.Type.SINGLE, new double[] {}, 0.018, 0.07,
					new double[] { 0, 1, 2 }, new double[] { 0, 32, 0 },
					new Coordinate[] {
						new Coordinate(.035, 0, 0, 0.025),new Coordinate(.035, 0, 0, .020),new Coordinate(.035, 0, 0, 0.0154)}, 
					"digest D21 test");
			InnerTube boosterMount = (InnerTube) rocket.getChild(1).getChild(0).getChild(2);
			boosterMount.setMotorMount(true);
			boosterMount.setMotorInstance(fcid, new MotorConfiguration(boosterMotor));
			boosterMount.setClusterConfiguration( ClusterConfiguration.CONFIGURATIONS[1]); // double-mount
		}
		rocket.update();
		rocket.enableEvents();
		return rocket;
	}

}
