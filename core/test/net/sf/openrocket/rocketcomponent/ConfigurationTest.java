package net.sf.openrocket.rocketcomponent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.BitSet;
import java.util.EventObject;
import java.util.Iterator;

import net.sf.openrocket.rocketcomponent.RocketComponent.Position;
import net.sf.openrocket.util.StateChangeListener;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Test;

public class ConfigurationTest extends BaseTestCase {
	
	/**
	 * Test change events and modIDs
	 */
	@Test
	public void testChangeEvent() {
		
		/* Setup */
		Rocket r1 = makeEmptyRocket();
		Configuration config = r1.getDefaultConfiguration();
		
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
	 * Test configuration rocket component and motor iterators
	 */
	@Test
	public void testConfigIterators() {
		
		/* Setup */
		Rocket r1 = makeSingleStageTestRocket();
		Configuration config = r1.getDefaultConfiguration();
		
		/* Test */
		
		// Test rocket component iterator
		// TODO: validate iterator iterates correctly
		for (Iterator<RocketComponent> i = config.iterator(); i.hasNext();) {
			i.next();
		}
		
		// Rocket component iterator remove method is unsupported, should throw exception
		try {
			Iterator<RocketComponent> configIterator = config.iterator();
			configIterator.remove();
		} catch (UnsupportedOperationException e) {
			assertTrue(e.getMessage().equals("remove unsupported"));
		}
		
		// Test motor iterator
		/* TODO: no motors in model Iterator<MotorMount> motorIterator() 
		 * TODO: validate iterator iterates correctly
		for (Iterator<MotorMount> i = config.motorIterator(); i.hasNext();) {
			i.next();
		}
		*/
		
		// Motor iterator remove method is unsupported, should throw exception
		try {
			Iterator<MotorMount> motorIterator = config.motorIterator();
			motorIterator.remove();
		} catch (UnsupportedOperationException e) {
			assertTrue(e.getMessage().equals("remove unsupported"));
		}
		
		/* Cleanup */
		config.release();
		
	}
	
	
	/**
	 * Empty rocket (no components) specific configuration tests
	 */
	@Test
	public void testEmptyRocket() {
		Rocket r1 = makeEmptyRocket();
		Configuration config = r1.getDefaultConfiguration();
		
		Configuration configClone = config.clone(); // TODO validate clone worked
		assertFalse(config.getRocket() == null);
		assertFalse(config.hasMotors());
		
		config.release();
	}
	
	
	/**
	 * Test flight configuration ID methods
	 */
	@Test
	public void testFlightConfigID() {
		
		/* Setup */
		Rocket r1 = makeSingleStageTestRocket();
		Configuration config = r1.getDefaultConfiguration();
		
		/* Test */
		
		// Test flight configuration ID setting
		String origFlightConfigID = config.getFlightConfigurationID(); // save for later
		String testFlightConfigID = origFlightConfigID + "_ConfigurationTest";
		
		// if id is already set (ie, not null), setting to null should work
		assertFalse(config.getFlightConfigurationID() == null);
		config.setFlightConfigurationID(null);
		assertTrue(config.getFlightConfigurationID() == null);
		
		// now that id is set to null, setting to null should not set again (do for coverage)
		config.setFlightConfigurationID(null);
		assertTrue(config.getFlightConfigurationID() == null);
		
		// reset the id from null to a test value
		config.setFlightConfigurationID(testFlightConfigID);
		assertTrue(config.getFlightConfigurationID().equals(testFlightConfigID));
		
		// setting it to the same non-null value should just return (do for coverage)
		config.setFlightConfigurationID(testFlightConfigID);
		assertTrue(config.getFlightConfigurationID().equals(testFlightConfigID));
		
		// set back to original value
		config.setFlightConfigurationID(origFlightConfigID);
		assertTrue(config.getFlightConfigurationID().equals(origFlightConfigID));
		
		/* Cleanup */
		config.release();
	}
	
	
	/**
	 * Test flight configuration ID methods
	 */
	@Test
	public void testGeneralMethods() {
		
		/* Setup */
		Rocket r1 = makeSingleStageTestRocket();
		Configuration config = r1.getDefaultConfiguration();
		
		/* Test */
		
		// general method tests
		Configuration configClone = config.clone(); // TODO validate clone worked
		
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
		Configuration config = r1.getDefaultConfiguration();
		
		BitSet activeStageFlags = new BitSet();
		activeStageFlags.set(0, false); // first stage
		
		/* Test */
		
		// test cloning of single stage rocket
		Configuration configClone = config.clone(); // TODO validate clone worked
		
		// test explicitly setting only first stage active
		config.setOnlyStage(0);
		activeStageFlags.clear();
		activeStageFlags.set(0, true);
		validateStages(config, 1, activeStageFlags);
		
		// test explicitly setting all stages up to first stage active
		config.setToStage(0);
		activeStageFlags.clear();
		activeStageFlags.set(0, true);
		validateStages(config, 1, activeStageFlags);
		
		// test explicitly setting all stages active
		config.setAllStages();
		activeStageFlags.clear();
		activeStageFlags.set(0, true);
		validateStages(config, 1, activeStageFlags);
		
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
		Configuration config = r1.getDefaultConfiguration();
		
		BitSet activeStageFlags = new BitSet();
		activeStageFlags.set(0, false); // booster (first) stage
		activeStageFlags.set(1, false); // sustainer (second) stage
		
		/* Test */
		
		// test cloning of two stage rocket
		Configuration configClone = config.clone(); // TODO validate clone worked
		
		// test explicitly setting only first stage active
		config.setOnlyStage(0);
		activeStageFlags.clear();
		activeStageFlags.set(0, true);
		validateStages(config, 2, activeStageFlags);
		
		// test explicitly setting all stages up to first stage active
		config.setToStage(0);
		activeStageFlags.clear();
		activeStageFlags.set(0, true);
		validateStages(config, 2, activeStageFlags);
		
		// test explicitly setting all stages up to second stage active
		config.setToStage(1);
		activeStageFlags.clear();
		activeStageFlags.set(0, 2, true);
		validateStages(config, 2, activeStageFlags);
		
		// test explicitly setting all two stages active
		config.setAllStages();
		activeStageFlags.clear();
		activeStageFlags.set(0, 2, true);
		validateStages(config, 2, activeStageFlags);
		
		// Cleanup
		config.release();
		
		
	}
	
	///////////////////// Helper Methods ////////////////////////////
	
	public void validateStages(Configuration config, int expectedStageCount, BitSet activeStageFlags) {
		
		// test that getStageCount() returns correct value
		int stageCount = config.getStageCount();
		assertTrue(stageCount == expectedStageCount);
		
		// test that getActiveStageCount() and getActiveStages() returns correct values
		int expectedActiveStageCount = 0;
		for (int i = 0; i < expectedStageCount; i++) {
			if (activeStageFlags.get(i)) {
				expectedActiveStageCount++;
			}
		}
		assertTrue(config.getActiveStageCount() == expectedActiveStageCount);
		int[] stages = config.getActiveStages();
		assertTrue(stages.length == expectedActiveStageCount);
		
		// test if isHead() detects first stage being active or inactive
		if (activeStageFlags.get(0)) {
			assertTrue(config.isHead());
		} else {
			assertFalse(config.isHead());
		}
		
		// test if isStageActive() detects stage x being active or inactive
		for (int i = 0; i < expectedStageCount; i++) {
			if (activeStageFlags.get(i)) {
				assertTrue(config.isStageActive(i));
			} else {
				assertFalse(config.isStageActive(i));
			}
		}
		
		// test boundary conditions 
		
		// stage -1 should not exist, and isStageActive() should throw exception
		boolean IndexOutOfBoundsExceptionFlag = false;
		try {
			assertFalse(config.isStageActive(-1));
		} catch (IndexOutOfBoundsException e) {
			IndexOutOfBoundsExceptionFlag = true;
		}
		assertTrue(IndexOutOfBoundsExceptionFlag);
		
		// n+1 stage should not exist, isStageActive() should return false
		// TODO: isStageActive(stageCount + 1) really should throw IndexOutOfBoundsException
		assertFalse(config.isStageActive(stageCount + 1));
		
	}
	
	
	//////////////////// Test Rocket Creation Methods /////////////////////////
	
	public static Rocket makeEmptyRocket() {
		Rocket rocket = new Rocket();
		return rocket;
	}
	
	public static Rocket makeSingleStageTestRocket() {
		
		// TODO: get units correct, these units are prob wrong, are lengths are CM, mass are grams
		
		Rocket rocket;
		Stage stage;
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
		stage = new Stage();
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
		inner.setMotorMount(true);
		inner.setPositionValue(0.5);
		inner.setRelativePosition(Position.BOTTOM);
		inner.setOuterRadius(1.9 / 2);
		inner.setInnerRadius(1.8 / 2);
		inner.setLength(7.5);
		tube1.addChild(inner);
		
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
		
		// Flight configuration
		String id = rocket.newFlightConfigurationID();
		
		//		Motor m = Application.getMotorSetDatabase().findMotors(null, null, "L540", Double.NaN, Double.NaN).get(0);
		//		tube3.setMotor(id, m);
		//		tube3.setMotorOverhang(0.02);
		rocket.getDefaultConfiguration().setFlightConfigurationID(id);
		
		//		tube3.setIgnitionEvent(MotorMount.IgnitionEvent.NEVER);
		
		rocket.getDefaultConfiguration().setAllStages();
		
		return rocket;
	}
	
	
	public static Rocket makeTwoStageTestRocket() {
		
		// TODO: get units correct, these units are prob wrong, are lengths are CM, mass are grams
		
		final double R = 2.5 / 2; // cm
		final double BT_T = 0.1;
		
		Rocket rocket = makeSingleStageTestRocket();
		
		Stage stage = new Stage();
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
		
		rocket.addChild(stage);
		
		return rocket;
		
	}
	
}
