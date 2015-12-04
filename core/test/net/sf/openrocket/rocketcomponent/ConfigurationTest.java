package net.sf.openrocket.rocketcomponent;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.EventObject;

import org.junit.Test;

import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorInstance;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.rocketcomponent.RocketComponent.Position;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.StateChangeListener;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class ConfigurationTest extends BaseTestCase {
	
	/**
	 * Test change events and modIDs
	 */
	@Test
	public void testChangeEvent() {
		
		/* Setup */
		Rocket r1 = makeEmptyRocket();
		FlightConfiguration config = r1.getDefaultConfiguration();
		
		StateChangeListener listener1 = new StateChangeListener() {
			@Override
			public void stateChanged(EventObject e) {
			}
		};
		
		StateChangeListener listener2 = new StateChangeListener() {
			@Override
			public void stateChanged(EventObject e) {
			}
		};
		
		config.addChangeListener(listener1);
		config.addChangeListener(listener2);
		
		/* Test */
		
		// ModID should not change if nothing changed
		int origModID = config.getModID();
		int noChangeModID = config.getModID();
		assertTrue(origModID == noChangeModID);
		
		
		// After a change event, modID should change
		config.fireChangeEvent();
		int changeModID = config.getModID();
		assertTrue(origModID < changeModID);
		
		/* Cleanup */
		config.removeChangeListener(listener1);
		config.removeChangeListener(listener2);
		config.release();
		
	}
	
	
	/**
	 * Empty rocket (no components) specific configuration tests
	 */
	@Test
	public void testEmptyRocket() {
		Rocket r1 = makeEmptyRocket();
		FlightConfiguration config = r1.getDefaultConfiguration();
		
		FlightConfiguration configClone = config.clone();
		
		assertTrue(config.getRocket() == configClone.getRocket());
		
		config.release();
	}
	
	
	/**
	 * Test flight configuration ID methods
	 */
	@Test
	public void testGeneralMethods() {
		
		/* Setup */
		Rocket r1 = makeSingleStageTestRocket();
		FlightConfiguration config = r1.getDefaultConfiguration();
		
		/* Test */
		
		// general method tests
		FlightConfiguration configClone = config.clone(); // TODO validate clone worked
		
		assertFalse(config.getRocket() == null);
		
		// TODO rocket has no motors!  assertTrue(config.hasMotors());
		
		// rocket info tests
		double length = config.getLength();
		double refLength = config.getReferenceLength();
		double refArea = config.getReferenceArea();
		
		// TODO validate that the values are correct
		//log.debug("ConfigurationTest, length: " + String.valueOf(length));
		//log.debug("ConfigurationTest, refLength: " + String.valueOf(refLength));
		//log.debug("ConfigurationTest, refArea: " + String.valueOf(refArea));
		
		/* Cleanup */
		config.release();
	}
	
	/**
	 * Single stage rocket specific configuration tests
	 */
	@Test
	public void testSingleStageRocket() {
		
		/* Setup */
		Rocket r1 = makeSingleStageTestRocket();
		FlightConfiguration config = r1.getDefaultConfiguration();
		
		/* Test */
		
		// test cloning of single stage rocket
		FlightConfiguration configClone = config.clone(); // TODO validate clone worked
		configClone.release();
		
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
		
		// Cleanup
		config.release();
		
	}
	
	/**
	 * Multi stage rocket specific configuration tests
	 */
	@Test
	public void testMultiStageRocket() {
		
		/* Setup */
		Rocket r1 = makeTwoStageTestRocket();
		FlightConfiguration config = r1.getDefaultConfiguration();
		
		// test cloning of two stage rocket
		FlightConfiguration configClone = config.clone(); // TODO validate clone worked
		configClone.release();
		
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
		assertThat(config.toStageListDetail() + "Setting single stage active: ", config.isStageActive(0), equalTo(false));
		assertThat(config.toStageListDetail() + "Setting single stage active: ", config.isStageActive(1), equalTo(true));
		
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
		
		// Cleanup
		config.release();
		
	}
	
	/**
	 * Multi stage rocket specific configuration tests
	 */
	@Test
	public void testMotorClusters() {
		
		/* Setup */
		Rocket rkt = makeTwoStageMotorRocket();
		FlightConfiguration config = rkt.getDefaultConfiguration();
		
		
		config.clearAllStages();
		int expectedMotorCount = 0;
		int actualMotorCount = config.getActiveMotors().size();
		assertThat("motor count doesn't match", actualMotorCount, equalTo(expectedMotorCount));
		
		config.setOnlyStage(0);
		expectedMotorCount = 1;
		actualMotorCount = config.getActiveMotors().size();
		assertThat("motor count doesn't match: ", actualMotorCount, equalTo(expectedMotorCount));

		config.setOnlyStage(1);
		expectedMotorCount = 2;
		actualMotorCount = config.getActiveMotors().size();
		assertThat("motor count doesn't match: ", actualMotorCount, equalTo(expectedMotorCount));

		config.setAllStages();
		expectedMotorCount = 3;
//		{
//			System.err.println("Booster Stage only: config set detail: "+rkt.getConfigSet().toDebug());
//			System.err.println("Booster Stage only: config stage detail: "+config.toStageListDetail());
//			System.err.println("Booster Stage only: config motor detail: "+config.toMotorDetail());
//			config.enableDebugging();
//			config.updateMotors();
//			config.getActiveMotors();
//		}
		actualMotorCount = config.getActiveMotors().size();
		assertThat("motor count doesn't match: ", actualMotorCount, equalTo(expectedMotorCount));

				
	}
	
	///////////////////// Helper Methods ////////////////////////////
//	
//	public void validateStages(Configuration config, int expectedStageCount, BitSet activeStageFlags) {
//		
//		// test that getStageCount() returns correct value
//		int stageCount = config.getStageCount();
//		assertTrue(stageCount == expectedStageCount);
//		
//		// test that getActiveStageCount() and getActiveStages() returns correct values
//		int expectedActiveStageCount = 0;
//		for (int i = 0; i < expectedStageCount; i++) {
//			if (activeStageFlags.get(i)) {
//				expectedActiveStageCount++;
//			}
//		}
//		assertTrue(config.getActiveStageCount() == expectedActiveStageCount);
//		
//		assertTrue("this test is not yet written.", false);
//		//		int[] stages = config.getActiveStages();
//		//		
//		//		assertTrue(stages.length == expectedActiveStageCount);
//		//		
//		//		// test if isHead() detects first stage being active or inactive
//		//		if (activeStageFlags.get(0)) {
//		//			assertTrue(config.isHead());
//		//		} else {
//		//			assertFalse(config.isHead());
//		//		}
//		//		
//		//		// test if isStageActive() detects stage x being active or inactive
//		//		for (int i = 0; i < expectedStageCount; i++) {
//		//			if (activeStageFlags.get(i)) {
//		//				assertTrue(config.isStageActive(i));
//		//			} else {
//		//				assertFalse(config.isStageActive(i));
//		//			}
//		//		}
//		//		
//		//		// test boundary conditions 
//		//		
//		//		// stage -1 should not exist, and isStageActive() should throw exception
//		//		boolean IndexOutOfBoundsExceptionFlag = false;
//		//		try {
//		//			assertFalse(config.isStageActive(-1));
//		//		} catch (IndexOutOfBoundsException e) {
//		//			IndexOutOfBoundsExceptionFlag = true;
//		//		}
//		//		assertTrue(IndexOutOfBoundsExceptionFlag);
//		//		
//		//		// n+1 stage should not exist, isStageActive() should return false
//		//		// TODO: isStageActive(stageCount + 1) really should throw IndexOutOfBoundsException
//		//		assertFalse(config.isStageActive(stageCount + 1));
//		
//	}
	
	//////////////////// Test Rocket Creation Methods /////////////////////////
	
	public static Rocket makeEmptyRocket() {
		Rocket rocket = new Rocket();
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
		assertThat(" configuration list contains : ", rocket.getConfigSet().size(), equalTo(expectedConfigurationCount));
		
		FlightConfiguration newConfig = new FlightConfiguration(rocket,null);
		rocket.setFlightConfiguration( newConfig.getId(), newConfig);
		rocket.setDefaultConfiguration( newConfig.getId());
		assertThat(" configuration updates stage Count correctly: ", newConfig.getActiveStageCount(), equalTo(expectedStageCount));
		expectedConfigurationCount = 1;
		assertThat(" configuration list contains : ", rocket.getConfigSet().size(), equalTo(expectedConfigurationCount));
		
		//FlightConfigurationID fcid = config.getFlightConfigurationID();
//		Motor m = Application.getMotorSetDatabase().findMotors(null, null, "L540", Double.NaN, Double.NaN).get(0);
//		MotorInstance inst = m.getNewInstance();
//		inner.setMotorInstance( fcid, inst);
//		inner.setMotorOverhang(0.02);
//		
//		//inner.setMotorMount(true);
//		assertThat(" configuration updates stage Count correctly: ", inner.hasMotor(), equalTo(true));
//		
//		final int expectedMotorCount = 1;
//		assertThat(" configuration updates correctly: ", inner.getMotorCount(), equalTo(expectedMotorCount));
//		
//		// Flight configuration
//		//FlightConfigurationID id = rocket.newFlightConfigurationID();
//		
//		
//		//	tube3.setIgnitionEvent(MotorMount.IgnitionEvent.NEVER);
		
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
		
		return rocket;	
	}
	
	public static Rocket makeTwoStageMotorRocket() {
		Rocket rocket = makeTwoStageTestRocket();
		FlightConfigurationID fcid = rocket.getDefaultConfiguration().getId();
		
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
			sustainerMount.setMotorInstance(fcid, sustainerMotor.getNewInstance());
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
			boosterMount.setMotorInstance(fcid, boosterMotor.getNewInstance());
			boosterMount.setClusterConfiguration( ClusterConfiguration.CONFIGURATIONS[1]); // double-mount
		}
		return rocket;
	}
	
}
