package net.sf.openrocket.masscalc;

import static org.junit.Assert.assertEquals;

import java.util.List;

import net.sf.openrocket.document.OpenRocketDocumentFactory;
import net.sf.openrocket.rocketcomponent.*;
import net.sf.openrocket.rocketcomponent.position.AngleMethod;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.rocketcomponent.position.RadiusMethod;
import net.sf.openrocket.util.MathUtil;
import org.junit.Test;

import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.simulation.MotorClusterState;
import net.sf.openrocket.simulation.SimulationConditions;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.TestRockets;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

public class MassCalculatorTest extends BaseTestCase {

	// tolerance for compared double test results
	private static final double EPSILON = 0.00000001;   // note:  this precision matches MathUtil.java

	@Test
	public void testEmptyRocket() {
		Rocket rocket = new Rocket();
		FlightConfiguration config = rocket.getEmptyConfiguration();

		final RigidBody actualStructure = MassCalculator.calculateStructure(config);
		final double actualRocketDryMass = actualStructure.cm.weight;
		final Coordinate actualRocketDryCM = actualStructure.cm;

		assertEquals(" Empty Rocket Empty Mass is incorrect: ", 0, actualRocketDryMass, 0);

		Coordinate expCM = new Coordinate(0, 0, 0, 0);
		assertEquals("Empty Rocket CM.x is incorrect: ", expCM.x, actualRocketDryCM.x, 0);
		assertEquals("Empty Rocket CM.y is incorrect: ", expCM.y, actualRocketDryCM.y, 0);
		assertEquals("Empty Rocket CM.z is incorrect: ", expCM.z, actualRocketDryCM.z, 0);
		assertEquals("Empty Rocket CM is incorrect: ", expCM, actualRocketDryCM);

		double actualMOIrot = actualStructure.getRotationalInertia();
		double actualMOIlong = actualStructure.getLongitudinalInertia();
		assertEquals("Empty Rocket Rotational MOI calculated incorrectly: ", 0, actualMOIrot, 0);
		assertEquals("Empty Rocket Longitudinal MOI calculated incorrectly: ", 0, actualMOIlong, 0);
	}

	@Test
	public void testAlphaIIIStructure() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		rocket.setName("AlphaIII." + Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket.getEmptyConfiguration();
		
		config.setAllStages();

		final RigidBody actualStructure = MassCalculator.calculateStructure(config);
		final double actualRocketDryMass = actualStructure.cm.weight;
		final Coordinate actualRocketDryCM = actualStructure.cm;

		double expRocketDryMass = 0.025268247714878626;
		assertEquals(" Alpha III Empty Mass is incorrect: ", expRocketDryMass, actualRocketDryMass, EPSILON);

		double expCMx = 0.1917685523;
		Coordinate expCM = new Coordinate(expCMx, 0, 0, expRocketDryMass);
		assertEquals("Simple Rocket CM.x is incorrect: ", expCM.x, actualRocketDryCM.x, EPSILON);
		assertEquals("Simple Rocket CM.y is incorrect: ", expCM.y, actualRocketDryCM.y, EPSILON);
		assertEquals("Simple Rocket CM.z is incorrect: ", expCM.z, actualRocketDryCM.z, EPSILON);
		assertEquals("Simple Rocket CM is incorrect: ", expCM, actualRocketDryCM);


		double expMOIrot = 1.8763734635622462E-5;
		double expMOIlong = 1.7808603404853048E-4;

		double actualMOIrot = actualStructure.getRotationalInertia();
		double actualMOIlong = actualStructure.getLongitudinalInertia();
		assertEquals("Alpha III Rotational MOI calculated incorrectly: ", expMOIrot, actualMOIrot, EPSILON);
		assertEquals("Alpha III Longitudinal MOI calculated incorrectly: ", expMOIlong, actualMOIlong, EPSILON);

		// if we use a mass override, setting to same mass, we should get same result
		AxialStage sustainer = (AxialStage) rocket.getChild(0);

		sustainer.setSubcomponentsOverriddenMass(true);
		sustainer.setMassOverridden(true);
		sustainer.setOverrideMass(actualRocketDryMass);

		final RigidBody overrideStructure = MassCalculator.calculateStructure(config);
		final Coordinate overrideRocketDryCM = overrideStructure.cm;

		assertEquals("Simple Rocket Override CM is incorrect: ", actualRocketDryCM, overrideRocketDryCM);

		double overrideMOIrot = overrideStructure.getRotationalInertia();
		double overrideMOIlong = overrideStructure.getLongitudinalInertia();
		assertEquals("Alpha III Rotational MOI calculated incorrectly: ", actualMOIrot, overrideMOIrot, EPSILON);
		assertEquals("Alpha III Longitudinal MOI calculated incorrectly: ", actualMOIlong, overrideMOIlong, EPSILON);
	}

	@Test
	public void testAlphaIIILaunchMass() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		rocket.setName("AlphaIII." + Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket.getFlightConfigurationByIndex(0, false);

		InnerTube mmt = (InnerTube) rocket.getChild(0).getChild(1).getChild(2);
		Motor activeMotor = mmt.getMotorConfig(config.getFlightConfigurationID()).getMotor();
		String desig = activeMotor.getDesignation();

		RigidBody actualLaunchRigidBody = MassCalculator.calculateLaunch(config);
		double actualRocketLaunchMass = actualLaunchRigidBody.getMass();
		final Coordinate actualRocketLaunchCM = actualLaunchRigidBody.cm;

		double expRocketLaunchMass = 0.041668247714878634;
		assertEquals(" Alpha III Total Mass (with motor: " + desig + ") is incorrect: ", expRocketLaunchMass, actualRocketLaunchMass, EPSILON);

		double expCMx = 0.20996455968266833;
		Coordinate expCM = new Coordinate(expCMx, 0, 0, expRocketLaunchMass);
		assertEquals("Simple Rocket CM.x is incorrect: ", expCM.x, actualRocketLaunchCM.x, EPSILON);
		assertEquals("Simple Rocket CM.y is incorrect: ", expCM.y, actualRocketLaunchCM.y, EPSILON);
		assertEquals("Simple Rocket CM.z is incorrect: ", expCM.z, actualRocketLaunchCM.z, EPSILON);
		assertEquals("Simple Rocket CM is incorrect: ", expCM, actualRocketLaunchCM);
	}

	@Test
	public void testAlphaIIIMotorMass() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		rocket.setName("AlphaIII." + Thread.currentThread().getStackTrace()[1].getMethodName());

		InnerTube mmt = (InnerTube) rocket.getChild(0).getChild(1).getChild(2);
		FlightConfiguration config = rocket.getFlightConfigurationByIndex(0, false);
		FlightConfigurationId fcid = config.getFlightConfigurationID();
		Motor activeMotor = mmt.getMotorConfig(fcid).getMotor();
		String desig = activeMotor.getDesignation();

		final double expMotorLaunchMass = activeMotor.getLaunchMass();    // 0.0164 kg

		RigidBody actualMotorData = MassCalculator.calculateMotor(config);

		assertEquals(" Motor Mass " + desig + " is incorrect: ", expMotorLaunchMass, actualMotorData.getMass(), EPSILON);

		double expCMx = 0.238;
		Coordinate expCM = new Coordinate(expCMx, 0, 0, expMotorLaunchMass);
		assertEquals("Simple Rocket CM.x is incorrect: ", expCM.x, actualMotorData.cm.x, EPSILON);
		assertEquals("Simple Rocket CM.y is incorrect: ", expCM.y, actualMotorData.cm.y, EPSILON);
		assertEquals("Simple Rocket CM.z is incorrect: ", expCM.z, actualMotorData.cm.z, EPSILON);
		assertEquals("Simple Rocket CM is incorrect: ", expCM, actualMotorData.cm);
	}


	@Test
	public void testAlphaIIIMotorSimulationMass() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		rocket.setName("AlphaIII." + Thread.currentThread().getStackTrace()[1].getMethodName());

		InnerTube mmt = (InnerTube) rocket.getChild(0).getChild(1).getChild(2);
		FlightConfiguration config = rocket.getFlightConfigurationByIndex(0, false);
		FlightConfigurationId fcid = config.getFlightConfigurationID();
		Motor activeMotor = mmt.getMotorConfig(fcid).getMotor();
		String desig = activeMotor.getDesignation();

		// this is probably not enough for a full-up simulation, but it IS enough for a motor-mass calculation.
		SimulationStatus status = new SimulationStatus(config, new SimulationConditions());
		
		// Ignite motor at 1.0 seconds
		MotorClusterState currentMotorState = ((List<MotorClusterState>) status.getMotors()).get(0);
		final double ignitionTime = 1.0;
		currentMotorState.ignite(ignitionTime);
		
		{
			final double simTime = 1.03; // almost launch
			status.setSimulationTime(simTime);
			RigidBody actualMotorData = MassCalculator.calculateMotor(status);
			double expMass = activeMotor.getTotalMass(simTime - ignitionTime);
			assertEquals(" Motor Mass " + desig + " is incorrect: ", expMass, actualMotorData.getMass(), EPSILON);
		}
		{
			final double simTime = 2.03; // middle
			status.setSimulationTime(simTime);
			RigidBody actualMotorData = MassCalculator.calculateMotor(status);
			double expMass = activeMotor.getTotalMass(simTime - ignitionTime);
			assertEquals(" Motor Mass " + desig + " is incorrect: ", expMass, actualMotorData.getMass(), EPSILON);
		}
		{
			final double simTime = 3.03; // after burnout
			status.setSimulationTime(simTime);
			RigidBody actualMotorData = MassCalculator.calculateMotor(status);
			double expMass = activeMotor.getTotalMass(simTime - ignitionTime);
			assertEquals(" Motor Mass " + desig + " is incorrect: ", expMass, actualMotorData.getMass(), EPSILON);
		}
	}
	
	@Test
	public void testStageCMxOverride() {
		final Rocket rocket = TestRockets.makeSimple2Stage();
		final AxialStage sustainerStage = (AxialStage) rocket.getChild(0);
		final AxialStage boosterStage = (AxialStage) rocket.getChild(1);
		final FlightConfiguration config = rocket.getSelectedConfiguration();
		
		{ // [0] verify / document structure
			final BodyTube sustainerBody = (BodyTube) sustainerStage.getChild(0);
			assertEquals(0.0, sustainerBody.getPosition().x, EPSILON);
			assertEquals(0.1, sustainerBody.getLength(), EPSILON);
			
			final BodyTube boosterBody = (BodyTube) boosterStage.getChild(0);
			assertEquals(0.10, boosterBody.getComponentLocations()[0].x, EPSILON);
			assertEquals(0.10, boosterBody.getLength(), EPSILON);
		}
		
		{ // [1] test Rocket CM, before:
			final RigidBody actualStructure = MassCalculator.calculateStructure(config);
			
			final double actualRocketDryMass = actualStructure.cm.weight;
			final double expRocketDryMass = 0.0081178754;
			assertEquals(expRocketDryMass, actualRocketDryMass, EPSILON);
			
			final Coordinate actualRocketDryCM = actualStructure.cm;
			final double expCMx = 0.10;
			assertEquals(expCMx, actualRocketDryCM.x, EPSILON);
		}
		
		boosterStage.setSubcomponentsOverriddenCG(true);
		boosterStage.setCGOverridden(true);
		boosterStage.setOverrideCGX(0.0);
		
		{ // [1] test Rocket CM, before:
			final RigidBody actualStructure = MassCalculator.calculateStructure(config);
			
			final double actualRocketDryMass = actualStructure.cm.weight;
			final double expRocketDryMass = 0.0081178754;
			assertEquals(expRocketDryMass, actualRocketDryMass, EPSILON);
			
			final Coordinate actualRocketDryCM = actualStructure.cm;
			final double expCMx = 0.075;
			assertEquals(expCMx, actualRocketDryCM.x, EPSILON);
		}
	}
	
	@Test
	public void testSingleStageMassOverride() {
		final Rocket rocket = TestRockets.makeSimple2Stage();
		final AxialStage sustainerStage = (AxialStage) rocket.getChild(0);
		final BodyTube sustainerBody = (BodyTube) sustainerStage.getChild(0);
		final FlightConfiguration config = rocket.getSelectedConfiguration();
		config.setOnlyStage(0);
		
		final double expSingleBodyMass = 0.0040589377;
		{ // [0] verify / document structure
			assertEquals(0.0, sustainerBody.getPosition().x, EPSILON);
			assertEquals(0.1, sustainerBody.getLength(), EPSILON);
			assertEquals(expSingleBodyMass, sustainerBody.getMass(), EPSILON);
		}
		
		{ // [1] test Rocket CM, before:
			final RigidBody actualStructure = MassCalculator.calculateStructure(config);
			final double actualRocketDryMass = actualStructure.cm.weight;
			assertEquals(expSingleBodyMass, actualRocketDryMass, EPSILON);
			
			final Coordinate actualRocketDryCM = actualStructure.cm;
			assertEquals(0.05, actualRocketDryCM.x, EPSILON);
		}
		
		sustainerStage.setSubcomponentsOverriddenMass(true);
		sustainerStage.setMassOverridden(true);
		sustainerStage.setOverrideMass(0.001);   // something small, but not zero
		
		{ // [1] test Rocket CM, before:
			final RigidBody actualStructure = MassCalculator.calculateStructure(config);
			
			final double actualRocketDryMass = actualStructure.cm.weight;
			final double expRocketDryMass = 0.001;
			assertEquals(expRocketDryMass, actualRocketDryMass, EPSILON);
			
			final Coordinate actualRocketDryCM = actualStructure.cm;
			final double expCMx = 0.05;
			assertEquals(expCMx, actualRocketDryCM.x, EPSILON);
		}
	}

	@Test
	public void testDoubleStageMassOverride() {
		final Rocket rocket = TestRockets.makeSimple2Stage();
		final AxialStage sustainerStage = (AxialStage) rocket.getChild(0);
		final BodyTube sustainerBody = (BodyTube) sustainerStage.getChild(0);
		final AxialStage boosterStage = (AxialStage) rocket.getChild(1);
		final BodyTube boosterBody = (BodyTube) boosterStage.getChild(0);
		final FlightConfiguration config = rocket.getSelectedConfiguration();
		
		final double expSingleBodyMass = 0.0040589377;
		{ // [0] verify / document structure
			assertEquals(0.0, sustainerBody.getPosition().x, EPSILON);
			assertEquals(0.1, sustainerBody.getLength(), EPSILON);
			assertEquals(expSingleBodyMass, sustainerBody.getMass(), EPSILON);
			assertEquals(0.0, boosterBody.getPosition().x, EPSILON);
			assertEquals(0.1, boosterBody.getLength(), EPSILON);
			assertEquals(expSingleBodyMass, boosterBody.getMass(), EPSILON);
		}
		
		{ // [1] test Rocket CM, before:
			final RigidBody actualStructure = MassCalculator.calculateStructure(config);
			assertEquals(2*expSingleBodyMass, actualStructure.cm.weight, EPSILON);
			assertEquals(0.10, actualStructure.cm.x, EPSILON);
		}
		
		boosterStage.setSubcomponentsOverriddenMass(true);
		boosterStage.setMassOverridden(true);
		boosterStage.setOverrideMass(0.001);   // something small, but not zero
		
		{ // [1] test Rocket CM, after:
			final RigidBody actualStructure = MassCalculator.calculateStructure(config);
			
			final double actualRocketDryMass = actualStructure.cm.weight;
			assertEquals(expSingleBodyMass+0.001, actualRocketDryMass, EPSILON);
			
			final Coordinate actualRocketDryCM = actualStructure.cm;
			assertEquals(0.06976699, actualRocketDryCM.x, EPSILON);
		}
	}
	
	@Test
	public void testComponentCMxOverride() {
		final Rocket rocket = TestRockets.makeSimple2Stage();
		final AxialStage sustainerStage = (AxialStage) rocket.getChild(0);
		final BodyTube sustainerBody = (BodyTube) sustainerStage.getChild(0);
		final AxialStage boosterStage = (AxialStage) rocket.getChild(1);
		final BodyTube boosterBody = (BodyTube) boosterStage.getChild(0);
		final FlightConfiguration config = rocket.getSelectedConfiguration();
		
		{ // [0] verify / document structure
			assertEquals(0.0, sustainerBody.getPosition().x, EPSILON);
			assertEquals(0.1, sustainerBody.getLength(), EPSILON);
			
			assertEquals(0.10, boosterBody.getComponentLocations()[0].x, EPSILON);
			assertEquals(0.10, boosterBody.getLength(), EPSILON);
		}
		
		{ // [1] test Rocket CM, before:
			final RigidBody actualStructure = MassCalculator.calculateStructure(config);
			
			final double actualRocketDryMass = actualStructure.cm.weight;
			final double expRocketDryMass = 0.0081178754;
			assertEquals(expRocketDryMass, actualRocketDryMass, EPSILON);
			
			final Coordinate actualRocketDryCM = actualStructure.cm;
			final double expCMx = 0.10;
			assertEquals(expCMx, actualRocketDryCM.x, EPSILON);
		}
		
		boosterBody.setSubcomponentsOverriddenCG(false);
		boosterBody.setCGOverridden(true);
		boosterBody.setOverrideCGX(0.0);
		
		{ // [1] test Rocket CM, before:
			final RigidBody actualStructure = MassCalculator.calculateStructure(config);
			
			final double actualRocketDryMass = actualStructure.cm.weight;
			final double expRocketDryMass = 0.0081178754;
			assertEquals(expRocketDryMass, actualRocketDryMass, EPSILON);
			
			final Coordinate actualRocketDryCM = actualStructure.cm;
			final double expCMx = 0.075;
			assertEquals(expCMx, actualRocketDryCM.x, EPSILON);
		}
	}
	
	@Test
	public void testComponentMassOverride() {
		final Rocket rocket = TestRockets.makeSimple2Stage();
		final AxialStage sustainerStage = (AxialStage) rocket.getChild(0);
		final BodyTube sustainerBody = (BodyTube) sustainerStage.getChild(0);
		final AxialStage boosterStage = (AxialStage) rocket.getChild(1);
		final BodyTube boosterBody = (BodyTube) boosterStage.getChild(0);
		final FlightConfiguration config = rocket.getSelectedConfiguration();
		
		final double expSingleBodyMass = 0.0040589377;
		{ // [0] verify / document structure
			assertEquals(0.0, sustainerBody.getPosition().x, EPSILON);
			assertEquals(0.1, sustainerBody.getLength(), EPSILON);
			assertEquals(expSingleBodyMass, sustainerBody.getMass(), EPSILON);
			assertEquals(expSingleBodyMass, sustainerBody.getSectionMass(), EPSILON);
			
			assertEquals(0.10, boosterBody.getComponentLocations()[0].x, EPSILON);
			assertEquals(0.10, boosterBody.getLength(), EPSILON);
			assertEquals(expSingleBodyMass, boosterBody.getMass(), EPSILON);
			assertEquals(expSingleBodyMass, boosterBody.getSectionMass(), EPSILON);
		}
		
		{ // [1] test Rocket CM, before:
			final RigidBody actualStructure = MassCalculator.calculateStructure(config);
			
			final double actualRocketDryMass = actualStructure.cm.weight;
			final double expRocketDryMass = 0.0081178754;
			assertEquals(expRocketDryMass, actualRocketDryMass, EPSILON);
			
			final Coordinate actualRocketDryCM = actualStructure.cm;
			final double expCMx = 0.10;
			assertEquals(expCMx, actualRocketDryCM.x, EPSILON);
		}
		
		boosterBody.setSubcomponentsOverriddenMass(false);
		boosterBody.setMassOverridden(true);
		double newMass = 0.001;
		boosterBody.setOverrideMass(newMass);
		
		{ // [1] test Rocket CM, after:
			final RigidBody actualStructure = MassCalculator.calculateStructure(config);
			
			final double actualRocketDryMass = actualStructure.cm.weight;
			assertEquals(expSingleBodyMass+newMass, actualRocketDryMass, EPSILON);
			assertEquals(newMass, boosterBody.getMass(), EPSILON);
			assertEquals(newMass, boosterBody.getSectionMass(), EPSILON);
			
			final Coordinate actualRocketDryCM = actualStructure.cm;
			assertEquals(0.06976699, actualRocketDryCM.x, EPSILON);
		}
		
		boosterBody.setSubcomponentsOverriddenMass(true); // change.  Also, this body lacks subcomponents.
		boosterBody.setMassOverridden(true); // repeat
		boosterBody.setOverrideMass(newMass);  // repeat
		
		{ // [1] test Rocket CM, after:
			final RigidBody actualStructure = MassCalculator.calculateStructure(config);
			
			final double actualRocketDryMass = actualStructure.cm.weight;
			assertEquals(expSingleBodyMass+newMass, actualRocketDryMass, EPSILON);
			assertEquals(newMass, boosterBody.getMass(), EPSILON);
			assertEquals(newMass, boosterBody.getSectionMass(), EPSILON);
			
			final Coordinate actualRocketDryCM = actualStructure.cm;
			assertEquals(0.06976699, actualRocketDryCM.x, EPSILON);
		}
	}
	
	@Test
	public void testFalcon9HComponentMasses() {
		Rocket rkt = TestRockets.makeFalcon9Heavy();
		rkt.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());

		double expMass;
		RocketComponent cc;
		double compMass;

		// ====== Payload Stage ====== 
		// ====== ====== ====== ======
		{
			expMass = 0.022549558353;
			cc = rkt.getChild(0).getChild(0);
			compMass = cc.getComponentMass();
			assertEquals("P/L NoseCone mass calculated incorrectly: ", expMass, compMass, EPSILON);

			expMass = 0.02904490372;
			cc = rkt.getChild(0).getChild(1);
			compMass = cc.getComponentMass();
			assertEquals("P/L Body mass calculated incorrectly: ", expMass, compMass, EPSILON);

			expMass = 0.007289284477103441;
			cc = rkt.getChild(0).getChild(2);
			compMass = cc.getComponentMass();
			assertEquals("P/L Transition mass calculated incorrectly: ", expMass, compMass, EPSILON);

			expMass = 0.029224351500753608;
			cc = rkt.getChild(0).getChild(3);
			compMass = cc.getComponentMass();
			assertEquals("P/L Upper Stage Body mass calculated incorrectly: ", expMass, compMass, EPSILON);
			{
				expMass = 0.0079759509252;
				cc = rkt.getChild(0).getChild(3).getChild(0);
				compMass = cc.getComponentMass();
				assertEquals(cc.getName() + " mass calculated incorrectly: ", expMass, compMass, EPSILON);

				expMass = 0.00072;
				cc = rkt.getChild(0).getChild(3).getChild(1);
				compMass = cc.getComponentMass();
				assertEquals(cc.getName() + " mass calculated incorrectly: ", expMass, compMass, EPSILON);
			}

			expMass = 0.01948290100050243;
			cc = rkt.getChild(0).getChild(4);
			compMass = cc.getComponentMass();
			assertEquals(cc.getName() + " mass calculated incorrectly: ", expMass, compMass, EPSILON);
		}

		// ====== Core Stage ====== 
		// ====== ====== ======
		final AxialStage coreStage = (AxialStage) rkt.getChild(1);
		{
			expMass = 0.1298860066700161;
			final BodyComponent coreBody = (BodyComponent) coreStage.getChild(0);
			compMass = coreBody.getComponentMass();
			assertEquals(coreBody.getName() + " mass calculated incorrectly: ", expMass, compMass, EPSILON);
		}

		// ====== Booster Set Stage ====== 
		// ====== ====== ======
		ParallelStage boosters = (ParallelStage) coreStage.getChild(0).getChild(0);
		{
			expMass = 0.0222459863653;
			// think of the casts as an assert that ( child instanceof NoseCone) == true  
			NoseCone nose = (NoseCone) boosters.getChild(0);
			compMass = nose.getComponentMass();
			assertEquals(nose.getName() + " mass calculated incorrectly: ", expMass, compMass, EPSILON);

			expMass = 0.129886006;
			BodyTube body = (BodyTube) boosters.getChild(1);
			compMass = body.getComponentMass();
			assertEquals(body.getName() + " mass calculated incorrectly: ", expMass, compMass, EPSILON);

			expMass = 0.01890610458;
			InnerTube mmt = (InnerTube) boosters.getChild(1).getChild(0);
			compMass = mmt.getComponentMass();
			assertEquals(mmt.getName() + " mass calculated incorrectly: ", expMass, compMass, EPSILON);

			expMass = 0.13329359999999998;
			final FinSet boosterFins = (FinSet) boosters.getChild(1).getChild(1);
			compMass = boosterFins.getComponentMass();
			assertEquals(boosterFins.getName() + " mass calculated incorrectly: ", expMass, compMass, EPSILON);
		}
	}

	@Test
	public void testFalcon9HComponentCM() {
		Rocket rkt = TestRockets.makeFalcon9Heavy();
		rkt.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());

		double expCMx;
		double actCMx;
		// ====== Payload Stage ======
		// ====== ====== ====== ======
		{
			expCMx = 0.080801726467;
			NoseCone nc = (NoseCone) rkt.getChild(0).getChild(0);
			actCMx = nc.getComponentCG().x;
			assertEquals("P/L NoseCone CMx calculated incorrectly: ", expCMx, actCMx, EPSILON);

			expCMx = 0.066;
			BodyTube plbody = (BodyTube) rkt.getChild(0).getChild(1);
			actCMx = plbody.getComponentCG().x;
			assertEquals("P/L Body CMx calculated incorrectly: ", expCMx, actCMx, EPSILON);

			expCMx = 0.006640945419;
			Transition tr = (Transition) rkt.getChild(0).getChild(2);
			actCMx = tr.getComponentCG().x;
			assertEquals("P/L Transition CMx calculated incorrectly: ", expCMx, actCMx, EPSILON);

			expCMx = 0.09;
			BodyTube upperBody = (BodyTube) rkt.getChild(0).getChild(3);
			actCMx = upperBody.getComponentCG().x;
			assertEquals("P/L Upper Stage Body CMx calculated incorrectly: ", expCMx, actCMx, EPSILON);
			{
				expCMx = 0.0125;
				Parachute chute = (Parachute) rkt.getChild(0).getChild(3).getChild(0);
				actCMx = chute.getComponentCG().x;
				assertEquals("Parachute CMx calculated incorrectly: ", expCMx, actCMx, EPSILON);

				expCMx = 0.0125;
				ShockCord cord = (ShockCord) rkt.getChild(0).getChild(3).getChild(1);
				actCMx = cord.getComponentCG().x;
				assertEquals("Shock Cord CMx calculated incorrectly: ", expCMx, actCMx, EPSILON);
			}

			expCMx = 0.06;
			BodyTube interstage = (BodyTube) rkt.getChild(0).getChild(4);
			actCMx = interstage.getComponentCG().x;
			assertEquals("Interstage CMx calculated incorrectly: ", expCMx, actCMx, EPSILON);
		}

		// ====== Core Stage ======
		// ====== ====== ======
		final AxialStage coreStage = (AxialStage) rkt.getChild(1);
		{
			expCMx = 0.4;
			BodyTube coreBody = (BodyTube) coreStage.getChild(0);
			actCMx = coreBody.getComponentCG().x;
			assertEquals("Core Body CMx calculated incorrectly: ", expCMx, actCMx, EPSILON);
		}

		// ====== Booster Set Stage ======
		// ====== ====== ======
		ParallelStage boosters = (ParallelStage) coreStage.getChild(0).getChild(0);
		{
			expCMx = 0.055710581052;
			// think of the casts as an assert that ( child instanceof NoseCone) == true
			NoseCone nose = (NoseCone) boosters.getChild(0);
			actCMx = nose.getComponentCG().x;
			assertEquals("Booster Nose CMx calculated incorrectly: ", expCMx, actCMx, EPSILON);

			expCMx = 0.4;
			BodyTube body = (BodyTube) boosters.getChild(1);
			actCMx = body.getComponentCG().x;
			assertEquals("BoosterBody CMx calculated incorrectly: ", expCMx, actCMx, EPSILON);

			expCMx = 0.075;
			InnerTube mmt = (InnerTube) boosters.getChild(1).getChild(0);
			actCMx = mmt.getComponentCG().x;
			assertEquals(" Motor Mount Tube CMx calculated incorrectly: ", expCMx, actCMx, EPSILON);

			expCMx = 0.19393939;
			FinSet boosterFins = (FinSet) boosters.getChild(1).getChild(1);
			actCMx = boosterFins.getComponentCG().x;
			assertEquals("Core Fins CMx calculated incorrectly: ", expCMx, actCMx, EPSILON);
		}
	}

	@Test
	public void testFalcon9HComponentMOI() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration emptyConfig = rocket.getEmptyConfiguration();
		rocket.setSelectedConfiguration(emptyConfig.getFlightConfigurationID());

		double expInertia;
		RocketComponent cc;
		double compInertia;

		// ====== Payload Stage ====== 
		// ====== ====== ====== ======
		{
			final AxialStage payloadStage = (AxialStage) rocket.getChild(0);

			// Component: Nose Cone			
			final NoseCone payloadNose = (NoseCone) payloadStage.getChild(0);
			assertEquals(payloadNose.getName() + " Rotational MOI calculated incorrectly: ", 3.508155e-5, payloadNose.getRotationalInertia(), EPSILON);
			assertEquals(payloadNose.getName() + " Longitudinal MOI calculated incorrectly: ", 2.0400578477e-6, payloadNose.getLongitudinalInertia(), EPSILON);

			// Component: Payload BodyTube
			final BodyTube payloadBody = (BodyTube) payloadStage.getChild(1);
			assertEquals(payloadBody.getName() + " Rotational MOI calculated incorrectly: ", 7.70416e-5, payloadBody.getRotationalInertia(), EPSILON);
			assertEquals(payloadBody.getName() + " Longitudinal MOI calculated incorrectly: ", 8.06940e-5, payloadBody.getLongitudinalInertia(), EPSILON);

			// Component: Payload Trailing Transition
			final Transition payloadTail = (Transition) payloadStage.getChild(2);
			assertEquals(payloadTail.getName() + " Rotational MOI calculated incorrectly: ", 1.43691e-5, payloadTail.getRotationalInertia(), EPSILON);
			assertEquals(payloadTail.getName() + " Longitudinal MOI calculated incorrectly: ", 7.30265e-6, payloadTail.getLongitudinalInertia(), EPSILON);

			// Component: Interstage
			cc = rocket.getChild(0).getChild(3);
			expInertia = 4.22073e-5;
			compInertia = cc.getRotationalInertia();
			assertEquals(cc.getName() + " Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			expInertia = 0.0001;
			compInertia = cc.getLongitudinalInertia();
			assertEquals(cc.getName() + " Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);

			{
				cc = rocket.getChild(0).getChild(3).getChild(0);
				expInertia = 6.23121e-7;
				compInertia = cc.getRotationalInertia();
				assertEquals(cc.getName() + " Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
				expInertia = 7.26975e-7;
				compInertia = cc.getLongitudinalInertia();
				assertEquals(cc.getName() + " Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);

				cc = rocket.getChild(0).getChild(3).getChild(1);
				expInertia = 5.625e-8;
				compInertia = cc.getRotationalInertia();
				assertEquals(cc.getName() + " Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
				expInertia = 6.5625e-8;
				compInertia = cc.getLongitudinalInertia();
				assertEquals(cc.getName() + " Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			}

			cc = rocket.getChild(0).getChild(4);
			expInertia = 2.81382e-5;
			compInertia = cc.getRotationalInertia();
			assertEquals(cc.getName() + " Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			expInertia = 3.74486e-5;
			compInertia = cc.getLongitudinalInertia();
			assertEquals(cc.getName() + " Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
		}

		// ====== Core Stage ====== 
		// ====== ====== ======
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		{
			final BodyTube coreBody = (BodyTube) coreStage.getChild(0);
			expInertia = 0.000187588;
			compInertia = coreBody.getRotationalInertia();
			assertEquals(coreBody.getName() + " Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			expInertia = 0.00702105;
			compInertia = coreBody.getLongitudinalInertia();
			assertEquals(coreBody.getName() + " Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);

		}

		// ====== Booster Set Stage ====== 
		// ====== ====== ======
		ParallelStage boosters = (ParallelStage) coreStage.getChild(0).getChild(0);
		{
			final NoseCone boosterNose = (NoseCone) boosters.getChild(0);
			expInertia = 1.82665797857e-5;
			compInertia = boosterNose.getRotationalInertia();
			assertEquals(boosterNose.getName() + " Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			expInertia = 1.96501191666e-7;
			compInertia = boosterNose.getLongitudinalInertia();
			assertEquals(boosterNose.getName() + " Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);

			final BodyTube boosterBody = (BodyTube) boosters.getChild(1);
			expInertia = 1.875878651e-4;
			compInertia = boosterBody.getRotationalInertia();
			assertEquals(boosterBody.getName() + " Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			expInertia = 0.00702104762;
			compInertia = boosterBody.getLongitudinalInertia();
			assertEquals(boosterBody.getName() + " Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);

			cc = boosters.getChild(1).getChild(0);
			expInertia = 4.11444e-6;
			compInertia = cc.getRotationalInertia();
			assertEquals(cc.getName() + " Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			expInertia = 3.75062e-5;
			compInertia = cc.getLongitudinalInertia();
			assertEquals(cc.getName() + " Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);

			final FinSet boosterFins = (FinSet) boosters.getChild(1).getChild(1);
			expInertia = 0.000928545614574877;
			compInertia = boosterFins.getRotationalInertia();
			assertEquals(boosterFins.getName() + " Rotational MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);
			expInertia = 0.001246261927287438;
			compInertia = boosterFins.getLongitudinalInertia();
			assertEquals(boosterFins.getName() + " Longitudinal MOI calculated incorrectly: ", expInertia, compInertia, EPSILON);

		}
	}

	@Test
	public void testFalcon9HPayloadStructureCM() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket.getEmptyConfiguration();

		// validate payload stage
		AxialStage payloadStage = (AxialStage) rocket.getChild(0);
		config.setOnlyStage(payloadStage.getStageNumber());

		final RigidBody actualStructureData = MassCalculator.calculateStructure(config);
		final Coordinate actualCM = actualStructureData.cm;

		double expMass = 0.1162869499;
		double expCMx = 0.2780707857;
		assertEquals("Upper Stage Mass is incorrect: ", expMass, actualCM.weight, EPSILON);

		assertEquals("Upper Stage CM.x is incorrect: ", expCMx, actualCM.x, EPSILON);
		assertEquals("Upper Stage CM.y is incorrect: ", 0.0f, actualCM.y, EPSILON);
		assertEquals("Upper Stage CM.z is incorrect: ", 0.0f, actualCM.z, EPSILON);
	}

	@Test
	public void testFalcon9HCoreStructureCM() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket.getEmptyConfiguration();
		AxialStage coreStage = (AxialStage) rocket.getChild(1);
		config.setOnlyStage(coreStage.getStageNumber());

		final RigidBody actualData = MassCalculator.calculateStructure(config);
		final Coordinate actualCM = actualData.cm;

		double expMass = 0.12988600;
		double expCMx = 0.964;
		assertEquals("Upper Stage Mass is incorrect: ", expMass, actualCM.weight, EPSILON);

		assertEquals("Upper Stage CM.x is incorrect: ", expCMx, actualCM.x, EPSILON);
		assertEquals("Upper Stage CM.y is incorrect: ", 0.0f, actualCM.y, EPSILON);
		assertEquals("Upper Stage CM.z is incorrect: ", 0.0f, actualCM.z, EPSILON);
	}

	@Test
	public void testFalcon9HCoreMotorLaunchCM() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket.getFlightConfiguration(new FlightConfigurationId(TestRockets.FALCON_9H_FCID_1));
		AxialStage core = (AxialStage) rocket.getChild(1);
		final int coreNum = core.getStageNumber();
		config.setOnlyStage(coreNum);

		final MotorMount mnt = (MotorMount) core.getChild(0);
		final Motor motor = mnt.getMotorConfig(config.getFlightConfigurationID()).getMotor();
		final String motorDesignation = motor.getDesignation();

		RigidBody actMotorData = MassCalculator.calculateMotor(config);

		final double actMotorMass = actMotorData.getMass();
		final Coordinate actCM = actMotorData.cm;

		final double expMotorMass = motor.getLaunchMass();
		final Coordinate expCM = new Coordinate(1.053, 0, 0, expMotorMass);

		assertEquals(core.getName() + " => " + motorDesignation + " propellant mass is incorrect: ", expMotorMass, actMotorMass, EPSILON);
		assertEquals(core.getName() + " => " + motorDesignation + " propellant CoM x is incorrect: ", expCM.x, actCM.x, EPSILON);
		assertEquals(core.getName() + " => " + motorDesignation + " propellant CoM y is incorrect: ", expCM.y, actCM.y, EPSILON);
		assertEquals(core.getName() + " => " + motorDesignation + " propellant CoM z is incorrect: ", expCM.z, actCM.z, EPSILON);
	}

	@Test
	public void testFalcon9HCoreMotorLaunchMOIs() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket.getFlightConfiguration(new FlightConfigurationId(TestRockets.FALCON_9H_FCID_1));
		config.setOnlyStage(1);

		RigidBody corePropInertia = MassCalculator.calculateMotor(config);

		// validated against a specific motor/radius/length
		final double expIxx = 0.003380625;

		final double expIyy = 0.156701835;

		final double actCorePropIxx = corePropInertia.getIxx();
		final double actCorePropIyy = corePropInertia.getIyy();

		assertEquals("Core Stage motor axial MOI is incorrect: ", expIxx, actCorePropIxx, EPSILON);
		assertEquals("Core Stage motor longitudinal MOI is incorrect: ", expIyy, actCorePropIyy, EPSILON);
	}

	@Test
	public void testFalcon9HBoosterStructureCM() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket.getEmptyConfiguration();

		config.setOnlyStage(TestRockets.FALCON_9H_BOOSTER_STAGE_NUMBER);

		final RigidBody actualData = MassCalculator.calculateStructure(config);
		final Coordinate actualCM = actualData.getCM();

		double expMass = 0.608663395;
		double expCMx = 1.073157592;
		assertEquals("Heavy Booster Mass is incorrect: ", expMass, actualCM.weight, EPSILON);

		assertEquals("Heavy Booster CM.x is incorrect: ", expCMx, actualCM.x, EPSILON);
		assertEquals("Heavy Booster CM.y is incorrect: ", 0.0f, actualCM.y, EPSILON);
		assertEquals("Heavy Booster CM.z is incorrect: ", 0.0f, actualCM.z, EPSILON);
	}

	@Test
	public void testFalcon9HBoosterLaunchCM() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket.getFlightConfiguration(new FlightConfigurationId(TestRockets.FALCON_9H_FCID_1));
		config.setOnlyStage(TestRockets.FALCON_9H_BOOSTER_STAGE_NUMBER);

		RigidBody actualBoosterLaunchData = MassCalculator.calculateLaunch(config);

		double actualMass = actualBoosterLaunchData.getMass();
		double expectedMass = 1.592663395;
		assertEquals(" Booster Launch Mass is incorrect: ", expectedMass, actualMass, EPSILON);

		final Coordinate actualCM = actualBoosterLaunchData.getCM();
		double expectedCMx = 1.22216804;
		Coordinate expCM = new Coordinate(expectedCMx, 0, 0, expectedMass);
		assertEquals(" Booster Launch CM.x is incorrect: ", expCM.x, actualCM.x, EPSILON);
		assertEquals(" Booster Launch CM.y is incorrect: ", expCM.y, actualCM.y, EPSILON);
		assertEquals(" Booster Launch CM.z is incorrect: ", expCM.z, actualCM.z, EPSILON);
		assertEquals(" Booster Launch CM is incorrect: ", expCM, actualCM);
	}

	@Test
	public void testFalcon9HBoosterSpentCM() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket.getFlightConfiguration(new FlightConfigurationId(TestRockets.FALCON_9H_FCID_1));
		config.setOnlyStage(TestRockets.FALCON_9H_BOOSTER_STAGE_NUMBER);

		// Validate Booster Launch Mass
		RigidBody spentData = MassCalculator.calculateBurnout(config);
		Coordinate spentCM = spentData.getCM();

		double expSpentMass = 1.12066339;
		double expSpentCMx = 1.18334714;
		Coordinate expLaunchCM = new Coordinate(expSpentCMx, 0, 0, expSpentMass);
		assertEquals(" Booster Launch Mass is incorrect: ", expLaunchCM.weight, spentCM.weight, EPSILON);
		assertEquals(" Booster Launch CM.x is incorrect: ", expLaunchCM.x, spentCM.x, EPSILON);
		assertEquals(" Booster Launch CM.y is incorrect: ", expLaunchCM.y, spentCM.y, EPSILON);
		assertEquals(" Booster Launch CM.z is incorrect: ", expLaunchCM.z, spentCM.z, EPSILON);
		assertEquals(" Booster Launch CM is incorrect: ", expLaunchCM, spentCM);
	}

	@Test
	public void testFalcon9HBoosterMotorCM() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket.getFlightConfiguration(new FlightConfigurationId(TestRockets.FALCON_9H_FCID_1));
		config.setOnlyStage(TestRockets.FALCON_9H_BOOSTER_STAGE_NUMBER);

		RigidBody actualPropellant = MassCalculator.calculateMotor(config);
		final Coordinate actCM = actualPropellant.getCM();

		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosters = (ParallelStage) coreStage.getChild(0).getChild(0);
		final MotorMount mnt = (MotorMount) boosters.getChild(1).getChild(0);
		final Motor boosterMotor = mnt.getMotorConfig(config.getFlightConfigurationID()).getMotor();

		final double expBoosterPropMassEach = boosterMotor.getLaunchMass();
		final double boosterSetMotorCount = 8.; /// use a double merely to prevent type-casting issues
		final double expBoosterPropMass = expBoosterPropMassEach * boosterSetMotorCount;

		final Coordinate expCM = new Coordinate(1.31434, 0, 0, expBoosterPropMass);

		assertEquals(boosters.getName() + " => " + boosterMotor.getDesignation() + " propellant mass is incorrect: ", expBoosterPropMass, actualPropellant.getMass(), EPSILON);
		assertEquals(boosters.getName() + " => " + boosterMotor.getDesignation() + " propellant CoM x is incorrect: ", expCM.x, actCM.x, EPSILON);
		assertEquals(boosters.getName() + " => " + boosterMotor.getDesignation() + " propellant CoM y is incorrect: ", expCM.y, actCM.y, EPSILON);
		assertEquals(boosters.getName() + " => " + boosterMotor.getDesignation() + " propellant CoM z is incorrect: ", expCM.z, actCM.z, EPSILON);
	}

	@Test
	public void testFalcon9HeavyBoosterMotorLaunchMOIs() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket.getFlightConfiguration(new FlightConfigurationId(TestRockets.FALCON_9H_FCID_1));
		config.setOnlyStage(TestRockets.FALCON_9H_BOOSTER_STAGE_NUMBER);

		RigidBody actualInertia = MassCalculator.calculateMotor(config);

//		System.err.println( rocket.toDebugTree());

		final double expIxx = 0.006380379;
		assertEquals("Booster stage propellant axial MOI is incorrect: ", expIxx, actualInertia.getIxx(), EPSILON);
		final double expIyy = 0.001312553;
		assertEquals("Booster stage propellant longitudinal MOI is incorrect: ", expIyy, actualInertia.getIyy(), EPSILON);
	}

	@Test
	public void testFalcon9HeavyBoosterSpentMOIs() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket.getFlightConfiguration(new FlightConfigurationId(TestRockets.FALCON_9H_FCID_1));
		config.setOnlyStage(TestRockets.FALCON_9H_BOOSTER_STAGE_NUMBER);

		RigidBody spent = MassCalculator.calculateBurnout(config);

		double expMOIRotational = 0.009205665421431532;
		double boosterMOIRotational = spent.getRotationalInertia();
		assertEquals(" Booster x-axis MOI is incorrect: ", expMOIRotational, boosterMOIRotational, EPSILON);

		double expMOI_tr = 0.0582250994240395;
		double boosterMOI_tr = spent.getLongitudinalInertia();
		assertEquals(" Booster transverse MOI is incorrect: ", expMOI_tr, boosterMOI_tr, EPSILON);
	}

	@Test
	public void testFalcon9HeavyBoosterLaunchMOIs() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket.getFlightConfiguration(new FlightConfigurationId(TestRockets.FALCON_9H_FCID_1));
		config.setOnlyStage(TestRockets.FALCON_9H_BOOSTER_STAGE_NUMBER);


		RigidBody launchData = MassCalculator.calculateLaunch(config);

		final double expIxx = 0.01226617242143153;
		final double actIxx = launchData.getRotationalInertia();
		final double expIyy = 0.06455356411879717;
		final double actIyy = launchData.getLongitudinalInertia();

		assertEquals(" Booster x-axis MOI is incorrect: ", expIxx, actIxx, EPSILON);
		assertEquals(" Booster transverse MOI is incorrect: ", expIyy, actIyy, EPSILON);
	}


	@Test
	public void testFalcon9HeavyBoosterStageMassOverride() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket.getEmptyConfiguration();
		rocket.setSelectedConfiguration(config.getId());
		config.setOnlyStage(TestRockets.FALCON_9H_BOOSTER_STAGE_NUMBER);

		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosters = (ParallelStage) coreStage.getChild(0).getChild(0);
		final double overrideMass = 0.5;
		boosters.setSubcomponentsOverriddenMass(true);
		boosters.setMassOverridden(true);
		boosters.setOverrideMass(overrideMass);
		boosters.setSubcomponentsOverriddenCG(true);
		boosters.setCGOverridden(true);
		boosters.setOverrideCGX(6.0);

		RigidBody burnout = MassCalculator.calculateStructure(config);
		Coordinate boosterSetCM = burnout.getCM();
		double calcTotalMass = burnout.getMass();

		double expTotalMass = overrideMass;
		assertEquals(" Booster Launch Mass is incorrect: ", expTotalMass, calcTotalMass, EPSILON);

		double expCMx = 6.484;
		Coordinate expCM = new Coordinate(expCMx, 0, 0, expTotalMass);
		assertEquals(" Booster Launch CM.x is incorrect: ", expCM.x, boosterSetCM.x, EPSILON);
		assertEquals(" Booster Launch CM.y is incorrect: ", expCM.y, boosterSetCM.y, EPSILON);
		assertEquals(" Booster Launch CM.z is incorrect: ", expCM.z, boosterSetCM.z, EPSILON);
		assertEquals(" Booster Launch CM is incorrect: ", expCM, boosterSetCM);

		// Validate MOI
		double expMOI_axial = 0.005885793421431532;
		double boosterMOI_xx = burnout.getRotationalInertia();
		assertEquals(" Booster x-axis MOI is incorrect: ", expMOI_axial, boosterMOI_xx, EPSILON);

		double expMOI_tr =  17.86133586701;
		double boosterMOI_tr = burnout.getLongitudinalInertia();
		assertEquals(" Booster transverse MOI is incorrect: ", expMOI_tr, boosterMOI_tr, EPSILON);
	}

	@Test
	public void testFalcon9HeavyComponentMassOverride() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket.getEmptyConfiguration();
		rocket.setSelectedConfiguration(config.getId());

		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosters = (ParallelStage) coreStage.getChild(0).getChild(0);
		config.setOnlyStage(boosters.getStageNumber());

		NoseCone nose = (NoseCone) boosters.getChild(0);
		nose.setMassOverridden(true);
		nose.setOverrideMass(0.71);
		// cm= 0.71000000g @[0.53971058,0.07700000,0.00000000]

		BodyTube body = (BodyTube) boosters.getChild(1);
		body.setMassOverridden(true);
		body.setOverrideMass(0.622);
		// cm= 0.62200000g @[0.96400000,0.07700000,0.00000000]

		InnerTube mmt = (InnerTube) boosters.getChild(1).getChild(0);
		mmt.setMassOverridden(true);
		mmt.setOverrideMass(0.213);
		// cm= 0.21300000g @[1.28900000,0.07700000,0.00000000]

		// Fin mass is not overridden
		// cm= 0.15995232g @[1.23793939,0.07700000,0.00000000]

		RigidBody boosterData = MassCalculator.calculateStructure(config);
		Coordinate boosterCM = boosterData.getCM();
		// cm= 3.409905g@[0.853614,-0.000000,0.000000]

		double expTotalMass = 3.3565872;
		assertEquals(" Booster Launch Mass is incorrect: ", expTotalMass, boosterData.getMass(), EPSILON);

		double expCMx = 0.847508988;
		Coordinate expCM = new Coordinate(expCMx, 0, 0, expTotalMass);
		assertEquals(" Booster Launch CM.x is incorrect: ", expCM.x, boosterCM.x, EPSILON);
		assertEquals(" Booster Launch CM.y is incorrect: ", expCM.y, boosterCM.y, EPSILON);
		assertEquals(" Booster Launch CM.z is incorrect: ", expCM.z, boosterCM.z, EPSILON);
		assertEquals(" Booster Launch CM is incorrect: ", expCM, boosterCM);

		// Validate MOI
		double expMOI_axial = 0.024813612416088204;
		double boosterMOI_xx = boosterData.getRotationalInertia();
		assertEquals(" Booster x-axis MOI is incorrect: ", expMOI_axial, boosterMOI_xx, EPSILON);

		double expMOI_tr =  0.34567788938578525;
		double boosterMOI_tr = boosterData.getLongitudinalInertia();
		assertEquals(" Booster transverse MOI is incorrect: ", expMOI_tr, boosterMOI_tr, EPSILON);
	}

	@Test
	public void testFalcon9HeavyComponentCMxOverride() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());
		
		FlightConfiguration config = rocket.getEmptyConfiguration();
		rocket.setSelectedConfiguration(config.getId());

		config.setOnlyStage(TestRockets.FALCON_9H_BOOSTER_STAGE_NUMBER);
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage boosters = (ParallelStage) coreStage.getChild(0).getChild(0);

		NoseCone nose = (NoseCone) boosters.getChild(0);
		nose.setCGOverridden(true);
		nose.setOverrideCGX(0.22);

		BodyTube body = (BodyTube) boosters.getChild(1);
		body.setCGOverridden(true);
		body.setOverrideCGX(0.433);

		InnerTube mmt = (InnerTube) boosters.getChild(1).getChild(0);
		mmt.setCGOverridden(true);
		mmt.setOverrideCGX(0.395);

		RigidBody structure = MassCalculator.calculateStructure(config);

		final double expMass = 0.6086633952494;
		double calcTotalMass = structure.getMass();
		assertEquals(" Booster Launch Mass is incorrect: ", expMass, calcTotalMass, EPSILON);

		final double expCMx = 1.1191303646438673;
		Coordinate expCM = new Coordinate(expCMx, 0, 0, expMass);
		assertEquals(" Booster Launch CM.x is incorrect: ", expCM.x, structure.getCM().x, EPSILON);
		assertEquals(" Booster Launch CM.y is incorrect: ", expCM.y, structure.getCM().y, EPSILON);
		assertEquals(" Booster Launch CM.z is incorrect: ", expCM.z, structure.getCM().z, EPSILON);
		assertEquals(" Booster Launch CM is incorrect: ", expCM, structure.getCM());

		// Validate MOI
		final double expMOI_axial = 0.005885793421;
		double boosterMOI_xx = structure.getRotationalInertia();
		assertEquals(" Booster x-axis MOI is incorrect: ", expMOI_axial, boosterMOI_xx, EPSILON);

		final double expMOI_tr = 0.040989095911;
		double boosterMOI_tr = structure.getLongitudinalInertia();
		assertEquals(" Booster transverse MOI is incorrect: ", expMOI_tr, boosterMOI_tr, EPSILON);
	}

	
	@Test
	public void testSimplePhantomPodRocket() {
		Rocket rocket = new Rocket();
		rocket.setName("Test Phantom Pods");

		rocket.createFlightConfiguration(TestRockets.TEST_FCID_0);
		rocket.setSelectedConfiguration(TestRockets.TEST_FCID_0);
		final FlightConfiguration config = rocket.getSelectedConfiguration();

		final AxialStage stage = new AxialStage();
		stage.setName("Primary Stage");
		rocket.addChild(stage);
		
		final BodyTube primaryBody = new BodyTube(0.4, 0.02);
		primaryBody.setThickness(0.001);
		primaryBody.setName("Primary Body");
		stage.addChild(primaryBody);

		final PodSet pods = new PodSet();
		pods.setName("Pods");
		pods.setInstanceCount(2);
		primaryBody.addChild(pods);
		pods.setAxialMethod(AxialMethod.BOTTOM);    pods.setAxialOffset(0.0);
		pods.setAngleMethod(AngleMethod.RELATIVE);  pods.setAngleOffset(0.0);
		pods.setRadiusMethod(RadiusMethod.FREE);    pods.setRadiusOffset(0.04);

		final BodyTube podBody = new BodyTube(0.0, 0.02);
		podBody.setThickness(0.001);
		podBody.setName("Primary Body");
		pods.addChild(podBody);

		final TrapezoidFinSet fins = new TrapezoidFinSet(1, 0.05, 0.05, 0.0, 0.001);
		fins.setName("podFins");
		fins.setThickness(0.01);
		fins.setMassOverridden(true);	fins.setOverrideMass(0.02835);
		fins.setSubcomponentsOverriddenMass(false);
		fins.setAxialOffset(-0.01);		fins.setAxialMethod(AxialMethod.BOTTOM);
		podBody.addChild(fins);

		rocket.enableEvents();
		config.setAllStages();

		// =======================================================================

		// // DEBUG
		// System.err.println(rocket.toDebugTree());

		RigidBody structure = MassCalculator.calculateStructure(config);
		final double expMass = 0.0900260149;
		double calcTotalMass = structure.getMass();
		assertEquals(" Booster Launch Mass is incorrect: ", expMass, calcTotalMass, EPSILON);

		final double expCMx = 0.3039199615;
		final double expCMy = 0.0;
		final double expCMz = 0.0;
		Coordinate expCM = new Coordinate(expCMx, expCMy, expCMz, expMass);
		assertEquals(" Booster Launch CM.x is incorrect: ", expCM.x, structure.getCM().x, EPSILON);
		assertEquals(" Booster Launch CM.y is incorrect: ", expCM.y, structure.getCM().y, EPSILON);
		assertEquals(" Booster Launch CM.z is incorrect: ", expCM.z, structure.getCM().z, EPSILON);
		assertEquals(" Booster Launch CM is incorrect: ", expCM, structure.getCM());
 
	}

	@Test
	public void testEmptyStages() {
		Rocket rocketRef = TestRockets.makeEstesAlphaIII();		// Reference rocket
		FlightConfiguration configRef = rocketRef.getEmptyConfiguration();
		configRef.setAllStages();

		final RigidBody structureRef = MassCalculator.calculateStructure(configRef);
		final double rocketDryMassRef = structureRef.cm.weight;
		final Coordinate rocketDryCMRef = structureRef.cm;

		Rocket rocket = TestRockets.makeEstesAlphaIII();
		AxialStage stage1 = new AxialStage();		// To be added to the front of the rocket
		AxialStage stage2 = new AxialStage();		// To be added to the rear of the rocket
		rocket.addChild(stage1, 0);
		rocket.addChild(stage2);
		FlightConfiguration config = rocket.getEmptyConfiguration();
		config.setAllStages();

		final RigidBody structure = MassCalculator.calculateStructure(config);
		final double rocketDryMass = structure.cm.weight;
		final Coordinate rocketDryCM = structure.cm;

		assertEquals(" Empty Stages Rocket Empty Mass is incorrect: ", rocketDryMassRef, rocketDryMass, EPSILON);

		assertEquals("Empty Stages Rocket CM.x is incorrect: ", rocketDryCMRef.x, rocketDryCM.x, EPSILON);
		assertEquals("Empty Stages Rocket CM.y is incorrect: ", rocketDryCMRef.y, rocketDryCM.y, EPSILON);
		assertEquals("Empty Stages Rocket CM.z is incorrect: ", rocketDryCMRef.z, rocketDryCM.z, EPSILON);
		assertEquals("Empty Stages Rocket CM is incorrect: ", rocketDryCMRef, rocketDryCM);

		double MOIrotRef = structureRef.getRotationalInertia();
		double MOIlongRef = structureRef.getLongitudinalInertia();

		double MOIrot = structure.getRotationalInertia();
		double MOIlong = structure.getLongitudinalInertia();
		assertEquals("Empty Stages Rocket Rotational MOI calculated incorrectly: ", MOIrotRef, MOIrot, EPSILON);
		assertEquals("Empty Stages Rocket Longitudinal MOI calculated incorrectly: ", MOIlongRef, MOIlong, EPSILON);

		// if we use a mass override, setting to same mass, we should get same result
		AxialStage sustainerRef = (AxialStage) rocketRef.getChild(0);
		sustainerRef.setSubcomponentsOverriddenMass(true);
		sustainerRef.setMassOverridden(true);
		sustainerRef.setOverrideMass(rocketDryMassRef);

		AxialStage sustainer = (AxialStage) rocket.getChild(0);
		sustainer.setSubcomponentsOverriddenMass(true);
		sustainer.setMassOverridden(true);
		sustainer.setOverrideMass(rocketDryMass);

		final RigidBody overrideStructureRef = MassCalculator.calculateStructure(configRef);
		final Coordinate overrideRocketDryCMRef = overrideStructureRef.cm;

		final RigidBody overrideStructure = MassCalculator.calculateStructure(config);
		final Coordinate overrideRocketDryCM = overrideStructure.cm;

		assertEquals("Empty Stages Rocket Override CM is incorrect: ", overrideRocketDryCMRef, overrideRocketDryCM);

		double overrideMOIrotRef = overrideStructureRef.getRotationalInertia();
		double overrideMOIlongRef = overrideStructureRef.getLongitudinalInertia();
		double overrideMOIrot = overrideStructure.getRotationalInertia();
		double overrideMOIlong = overrideStructure.getLongitudinalInertia();
		assertEquals("Empty Stages Rocket Rotational MOI calculated incorrectly: ", overrideMOIrotRef, overrideMOIrot, EPSILON);
		assertEquals("Empty Stages Rocket Longitudinal MOI calculated incorrectly: ", overrideMOIlongRef, overrideMOIlong, EPSILON);
	}

	@Test
	public void testStructureMass() {
		Rocket rocket = OpenRocketDocumentFactory.createNewRocket().getRocket();
		AxialStage stage = rocket.getStage(0);
		stage.addChild(new NoseCone());
		BodyTube bodyTube = new BodyTube();
		stage.addChild(bodyTube);
		MassComponent massComponent = new MassComponent();
		massComponent.setComponentMass(0.01);
		bodyTube.addChild(massComponent);

		assertEquals(0.041016634, bodyTube.getMass(), EPSILON);
		assertEquals(0.051016634, bodyTube.getSectionMass(), EPSILON);

		bodyTube.setMassOverridden(true);
		bodyTube.setOverrideMass(0.02);

		assertEquals(0.02, bodyTube.getMass(), EPSILON);
		assertEquals(0.03, bodyTube.getSectionMass(), EPSILON);

		bodyTube.setSubcomponentsOverriddenMass(true);

		assertEquals(0.02, bodyTube.getMass(), EPSILON);
		assertEquals(0.02, bodyTube.getSectionMass(), EPSILON);
	}
	
}
