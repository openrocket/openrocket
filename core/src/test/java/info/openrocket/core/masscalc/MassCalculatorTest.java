package info.openrocket.core.masscalc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.rocketcomponent.*;
import info.openrocket.core.rocketcomponent.position.AngleMethod;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.rocketcomponent.position.RadiusMethod;
import info.openrocket.core.util.MathUtil;
import org.junit.jupiter.api.Test;

import info.openrocket.core.motor.Motor;
import info.openrocket.core.simulation.MotorClusterState;
import info.openrocket.core.simulation.SimulationConditions;
import info.openrocket.core.simulation.SimulationStatus;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.TestRockets;
import info.openrocket.core.util.BaseTestCase;

public class MassCalculatorTest extends BaseTestCase {

	// tolerance for compared double test results
	private static final double EPSILON = 0.00000001; // note: this precision matches MathUtil.java

	@Test
	public void testEmptyRocket() {
		Rocket rocket = new Rocket();
		FlightConfiguration config = rocket.getEmptyConfiguration();

		final RigidBody actualStructure = MassCalculator.calculateStructure(config);
		final double actualRocketDryMass = actualStructure.cm.weight;
		final Coordinate actualRocketDryCM = actualStructure.cm;

		assertEquals(0, actualRocketDryMass, 0, " Empty Rocket Empty Mass is incorrect: ");

		Coordinate expCM = new Coordinate(0, 0, 0, 0);
		assertEquals(expCM.x, actualRocketDryCM.x, 0, "Empty Rocket CM.x is incorrect: ");
		assertEquals(expCM.y, actualRocketDryCM.y, 0, "Empty Rocket CM.y is incorrect: ");
		assertEquals(expCM.z, actualRocketDryCM.z, 0, "Empty Rocket CM.z is incorrect: ");
		assertEquals(expCM, actualRocketDryCM, "Empty Rocket CM is incorrect: ");

		double actualMOIrot = actualStructure.getRotationalInertia();
		double actualMOIlong = actualStructure.getLongitudinalInertia();
		assertEquals(0, actualMOIrot, 0, "Empty Rocket Rotational MOI calculated incorrectly: ");
		assertEquals(0, actualMOIlong, 0, "Empty Rocket Longitudinal MOI calculated incorrectly: ");
	}

	@Test
	public void testStageOverride() {
		Rocket rocket = new Rocket();

		AxialStage stage = new AxialStage();
		rocket.addChild(stage);

		FlightConfiguration config = rocket.getEmptyConfiguration();
		config.setAllStages();
		rocket.enableEvents();

		BodyTube tube1 = new BodyTube();
		tube1.setLength(1.0);
		tube1.setMassOverridden(true);
		tube1.setOverrideMass(1.0);
		stage.addChild(tube1);

		BodyTube tube2 = new BodyTube();
		tube2.setLength(2.0);
		tube2.setMassOverridden(true);
		tube2.setOverrideMass(2.0);
		stage.addChild(tube2);
		// tube2.setAxialMethod(AxialMethod.ABSOLUTE);
		// tube2.setAxialOffset(1.0);

		RigidBody structure = MassCalculator.calculateStructure(config);
		assertEquals(3.0, structure.cm.weight, EPSILON, "No overrides -- mass incorrect");
		assertEquals(1.5, structure.cm.x, EPSILON, "No overrides -- CG incorrect");

		stage.setMassOverridden(true);
		stage.setOverrideMass(1.0);
		structure = MassCalculator.calculateStructure(config);
		assertEquals(4.0, structure.cm.weight, EPSILON, "Overrides: mass -- mass incorrect");
		assertEquals(1.5, structure.cm.x, EPSILON, "Overrides: mass -- CG incorrect");

		stage.setSubcomponentsOverriddenMass(true);
		structure = MassCalculator.calculateStructure(config);
		assertEquals(1.0, structure.cm.weight, EPSILON, "Overrides: mass, children mass --  mass incorrect");
		assertEquals(1.5, structure.cm.x, EPSILON, "Overrides: mass, children mass -- CG incorrect");

		stage.setCGOverridden(true);
		stage.setOverrideCGX(1.0);
		structure = MassCalculator.calculateStructure(config);
		assertEquals(1.0, structure.cm.weight, EPSILON, "Overrides: mass, children mass, CG -- mass incorrect");
		assertEquals(1.0, structure.cm.x, EPSILON, "Overrides: mass, children mass, CG -- CG incorrect");

		stage.setSubcomponentsOverriddenCG(true);
		structure = MassCalculator.calculateStructure(config);
		assertEquals(1.0, structure.cm.weight,
				EPSILON, "Overrides: mass, children mass, CG, children CG -- mass incorrect");
		assertEquals(1.0, structure.cm.x, EPSILON, "Overrides: mass, children mass, CG, children CG -- CG incorrect");

		stage.setSubcomponentsOverriddenMass(false);
		structure = MassCalculator.calculateStructure(config);
		assertEquals(4.0, structure.cm.weight, EPSILON, "Overrides: mass, CG, children CG -- mass incorrect");
		assertEquals(1.0, structure.cm.x, EPSILON, "Overrides: mass, CG, children CG -- CG incorrect");

		stage.setSubcomponentsOverriddenCG(false);
		structure = MassCalculator.calculateStructure(config);
		assertEquals(4.0, structure.cm.weight, EPSILON, "Overrides: mass, CG -- mass incorrect");
		assertEquals(1.375, structure.cm.x, EPSILON, "Overrides: mass, CG -- CG incorrect");
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

		double expRocketDryMass = 0.025268291846128787;
		assertEquals(expRocketDryMass, actualRocketDryMass, EPSILON, " Alpha III Empty Mass is incorrect: ");

		double expCMx = 0.19176843580003;
		double expCMy = -0.00031704007993248;		// Slight offset due to launch lug
		Coordinate expCM = new Coordinate(expCMx, expCMy, 0, expRocketDryMass);
		assertEquals(expCM.x, actualRocketDryCM.x, EPSILON, "Simple Rocket CM.x is incorrect: ");
		assertEquals(expCM.y, actualRocketDryCM.y, EPSILON, "Simple Rocket CM.y is incorrect: ");
		assertEquals(expCM.z, actualRocketDryCM.z, EPSILON, "Simple Rocket CM.z is incorrect: ");
		assertEquals(expCM.weight, actualRocketDryCM.weight, EPSILON, "Simple Rocket CM.weight is incorrect: ");
		assertEquals(expCM, actualRocketDryCM, "Simple Rocket CM is incorrect: ");

		double expMOIrot = 1.888136072268211E-5;
		double expMOIlong = 1.7808603404853048E-4;

		double actualMOIrot = actualStructure.getRotationalInertia();
		double actualMOIlong = actualStructure.getLongitudinalInertia();
		assertEquals(expMOIrot, actualMOIrot, EPSILON, "Alpha III Rotational MOI calculated incorrectly: ");
		assertEquals(expMOIlong, actualMOIlong, EPSILON, "Alpha III Longitudinal MOI calculated incorrectly: ");

		// if we use a mass override, setting to same mass, we should get same result
		AxialStage sustainer = (AxialStage) rocket.getChild(0);

		sustainer.setSubcomponentsOverriddenMass(true);
		sustainer.setMassOverridden(true);
		sustainer.setOverrideMass(actualRocketDryMass);

		final RigidBody overrideStructure = MassCalculator.calculateStructure(config);
		final Coordinate overrideRocketDryCM = overrideStructure.cm;

		assertEquals(actualRocketDryCM, overrideRocketDryCM, "Simple Rocket Override CM is incorrect: ");

		double overrideMOIrot = overrideStructure.getRotationalInertia();
		double overrideMOIlong = overrideStructure.getLongitudinalInertia();
		assertEquals(actualMOIrot, overrideMOIrot, EPSILON, "Alpha III Rotational MOI calculated incorrectly: ");
		assertEquals(actualMOIlong, overrideMOIlong, EPSILON, "Alpha III Longitudinal MOI calculated incorrectly: ");
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

		double expRocketLaunchMass = 0.04166829184612879;
		assertEquals(expRocketLaunchMass, actualRocketLaunchMass, EPSILON,
				" Alpha III Total Mass (with motor: " + desig + ") is incorrect: ");

		double expCMx = 0.20996446974544236;
		double expCMy = -0.00019225797151073; // Slight offset due to launch lug
		Coordinate expCM = new Coordinate(expCMx, expCMy, 0, expRocketLaunchMass);
		assertEquals(expCM.x, actualRocketLaunchCM.x, EPSILON, "Simple Rocket CM.x is incorrect: ");
		assertEquals(expCM.y, actualRocketLaunchCM.y, EPSILON, "Simple Rocket CM.y is incorrect: ");
		assertEquals(expCM.z, actualRocketLaunchCM.z, EPSILON, "Simple Rocket CM.z is incorrect: ");
		assertEquals(expCM.weight, actualRocketLaunchCM.weight, EPSILON, "Simple Rocket CM.weight is incorrect: ");
		assertEquals(expCM, actualRocketLaunchCM, "Simple Rocket CM is incorrect: ");
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

		final double expMotorLaunchMass = activeMotor.getLaunchMass(); // 0.0164 kg

		RigidBody actualMotorData = MassCalculator.calculateMotor(config);

		assertEquals(expMotorLaunchMass, actualMotorData.getMass(), EPSILON,
				" Motor Mass " + desig + " is incorrect: ");

		double expCMx = 0.238;
		Coordinate expCM = new Coordinate(expCMx, 0, 0, expMotorLaunchMass);
		assertEquals(expCM.x, actualMotorData.cm.x, EPSILON, "Simple Rocket CM.x is incorrect: ");
		assertEquals(expCM.y, actualMotorData.cm.y, EPSILON, "Simple Rocket CM.y is incorrect: ");
		assertEquals(expCM.z, actualMotorData.cm.z, EPSILON, "Simple Rocket CM.z is incorrect: ");
		assertEquals(expCM, actualMotorData.cm, "Simple Rocket CM is incorrect: ");
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
			assertEquals(expMass, actualMotorData.getMass(), EPSILON, " Motor Mass " + desig + " is incorrect: ");
		}
		{
			final double simTime = 2.03; // middle
			status.setSimulationTime(simTime);
			RigidBody actualMotorData = MassCalculator.calculateMotor(status);
			double expMass = activeMotor.getTotalMass(simTime - ignitionTime);
			assertEquals(expMass, actualMotorData.getMass(), EPSILON, " Motor Mass " + desig + " is incorrect: ");
		}
		{
			final double simTime = 3.03; // after burnout
			status.setSimulationTime(simTime);
			RigidBody actualMotorData = MassCalculator.calculateMotor(status);
			double expMass = activeMotor.getTotalMass(simTime - ignitionTime);
			assertEquals(expMass, actualMotorData.getMass(), EPSILON, " Motor Mass " + desig + " is incorrect: ");
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
		sustainerStage.setOverrideMass(0.001); // something small, but not zero

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
			assertEquals(2 * expSingleBodyMass, actualStructure.cm.weight, EPSILON);
			assertEquals(0.10, actualStructure.cm.x, EPSILON);
		}
		
		boosterStage.setSubcomponentsOverriddenMass(true);
		boosterStage.setMassOverridden(true);
		boosterStage.setOverrideMass(0.001); // something small, but not zero

		{ // [1] test Rocket CM, after:
			final RigidBody actualStructure = MassCalculator.calculateStructure(config);
			
			final double actualRocketDryMass = actualStructure.cm.weight;
			assertEquals(expSingleBodyMass + 0.001, actualRocketDryMass, EPSILON);

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
			assertEquals(expSingleBodyMass + newMass, actualRocketDryMass, EPSILON);
			assertEquals(newMass, boosterBody.getMass(), EPSILON);
			assertEquals(newMass, boosterBody.getSectionMass(), EPSILON);
			
			final Coordinate actualRocketDryCM = actualStructure.cm;
			assertEquals(0.06976699, actualRocketDryCM.x, EPSILON);
		}

		boosterBody.setSubcomponentsOverriddenMass(true); // change. Also, this body lacks subcomponents.
		boosterBody.setMassOverridden(true); // repeat
		boosterBody.setOverrideMass(newMass); // repeat

		{ // [1] test Rocket CM, after:
			final RigidBody actualStructure = MassCalculator.calculateStructure(config);
			
			final double actualRocketDryMass = actualStructure.cm.weight;
			assertEquals(expSingleBodyMass + newMass, actualRocketDryMass, EPSILON);
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
			expMass = 0.02255114133733203;
			cc = rkt.getChild(0).getChild(0);
			compMass = cc.getComponentMass();
			assertEquals(expMass, compMass, EPSILON, "P/L NoseCone mass calculated incorrectly: ");

			expMass = 0.02904490372;
			cc = rkt.getChild(0).getChild(1);
			compMass = cc.getComponentMass();
			assertEquals(expMass, compMass, EPSILON, "P/L Body mass calculated incorrectly: ");

			expMass = 0.007289284477103441;
			cc = rkt.getChild(0).getChild(2);
			compMass = cc.getComponentMass();
			assertEquals(expMass, compMass, EPSILON, "P/L Transition mass calculated incorrectly: ");

			expMass = 0.029224351500753608;
			cc = rkt.getChild(0).getChild(3);
			compMass = cc.getComponentMass();
			assertEquals(expMass, compMass, EPSILON, "P/L Upper Stage Body mass calculated incorrectly: ");
			{
				expMass = 0.0079759509252;
				cc = rkt.getChild(0).getChild(3).getChild(0);
				compMass = cc.getComponentMass();
				assertEquals(expMass, compMass, EPSILON, cc.getName() + " mass calculated incorrectly: ");

				expMass = 0.00072;
				cc = rkt.getChild(0).getChild(3).getChild(1);
				compMass = cc.getComponentMass();
				assertEquals(expMass, compMass, EPSILON, cc.getName() + " mass calculated incorrectly: ");
			}

			expMass = 0.01948290100050243;
			cc = rkt.getChild(0).getChild(4);
			compMass = cc.getComponentMass();
			assertEquals(expMass, compMass, EPSILON, cc.getName() + " mass calculated incorrectly: ");
		}

		// ====== Core Stage ======
		// ====== ====== ======
		final AxialStage coreStage = (AxialStage) rkt.getChild(1);
		{
			expMass = 0.1298860066700161;
			final BodyComponent coreBody = (BodyComponent) coreStage.getChild(0);
			compMass = coreBody.getComponentMass();
			assertEquals(expMass, compMass, EPSILON, coreBody.getName() + " mass calculated incorrectly: ");
		}

		// ====== Booster Set Stage ======
		// ====== ====== ======
		ParallelStage boosters = (ParallelStage) coreStage.getChild(0).getChild(0);
		{
			expMass = 0.02109368568877191;
			// think of the casts as an assert that ( child instanceof NoseCone) == true
			NoseCone nose = (NoseCone) boosters.getChild(0);
			compMass = nose.getComponentMass();
			assertEquals(expMass, compMass, EPSILON, nose.getName() + " mass calculated incorrectly: ");

			expMass = 0.129886006;
			BodyTube body = (BodyTube) boosters.getChild(1);
			compMass = body.getComponentMass();
			assertEquals(expMass, compMass, EPSILON, body.getName() + " mass calculated incorrectly: ");

			expMass = 0.01890610458;
			InnerTube mmt = (InnerTube) boosters.getChild(1).getChild(0);
			compMass = mmt.getComponentMass();
			assertEquals(expMass, compMass, EPSILON, mmt.getName() + " mass calculated incorrectly: ");

			expMass = 0.13329359999999998;
			final FinSet boosterFins = (FinSet) boosters.getChild(1).getChild(1);
			compMass = boosterFins.getComponentMass();
			assertEquals(expMass, compMass, EPSILON, boosterFins.getName() + " mass calculated incorrectly: ");
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
			expCMx = 0.08079767055284799;
			NoseCone nc = (NoseCone) rkt.getChild(0).getChild(0);
			actCMx = nc.getComponentCG().x;
			assertEquals(expCMx, actCMx, EPSILON, "P/L NoseCone CMx calculated incorrectly: ");

			expCMx = 0.066;
			BodyTube plbody = (BodyTube) rkt.getChild(0).getChild(1);
			actCMx = plbody.getComponentCG().x;
			assertEquals(expCMx, actCMx, EPSILON, "P/L Body CMx calculated incorrectly: ");

			expCMx = 0.006640909510057012;
			Transition tr = (Transition) rkt.getChild(0).getChild(2);
			actCMx = tr.getComponentCG().x;
			assertEquals(expCMx, actCMx, EPSILON, "P/L Transition CMx calculated incorrectly: ");

			expCMx = 0.09;
			BodyTube upperBody = (BodyTube) rkt.getChild(0).getChild(3);
			actCMx = upperBody.getComponentCG().x;
			assertEquals(expCMx, actCMx, EPSILON, "P/L Upper Stage Body CMx calculated incorrectly: ");
			{
				expCMx = 0.0125;
				Parachute chute = (Parachute) rkt.getChild(0).getChild(3).getChild(0);
				actCMx = chute.getComponentCG().x;
				assertEquals(expCMx, actCMx, EPSILON, "Parachute CMx calculated incorrectly: ");

				expCMx = 0.0125;
				ShockCord cord = (ShockCord) rkt.getChild(0).getChild(3).getChild(1);
				actCMx = cord.getComponentCG().x;
				assertEquals(expCMx, actCMx, EPSILON, "Shock Cord CMx calculated incorrectly: ");
			}

			expCMx = 0.06;
			BodyTube interstage = (BodyTube) rkt.getChild(0).getChild(4);
			actCMx = interstage.getComponentCG().x;
			assertEquals(expCMx, actCMx, EPSILON, "Interstage CMx calculated incorrectly: ");
		}

		// ====== Core Stage ======
		// ====== ====== ======
		final AxialStage coreStage = (AxialStage) rkt.getChild(1);
		{
			expCMx = 0.4;
			BodyTube coreBody = (BodyTube) coreStage.getChild(0);
			actCMx = coreBody.getComponentCG().x;
			assertEquals(expCMx, actCMx, EPSILON, "Core Body CMx calculated incorrectly: ");
		}

		// ====== Booster Set Stage ======
		// ====== ====== ======
		ParallelStage boosters = (ParallelStage) coreStage.getChild(0).getChild(0);
		{
			expCMx = 0.05383295859557998;
			// think of the casts as an assert that ( child instanceof NoseCone) == true
			NoseCone nose = (NoseCone) boosters.getChild(0);
			actCMx = nose.getComponentCG().x;
			assertEquals(expCMx, actCMx, EPSILON, "Booster Nose CMx calculated incorrectly: ");

			expCMx = 0.4;
			BodyTube body = (BodyTube) boosters.getChild(1);
			actCMx = body.getComponentCG().x;
			assertEquals(expCMx, actCMx, EPSILON, "BoosterBody CMx calculated incorrectly: ");

			expCMx = 0.075;
			InnerTube mmt = (InnerTube) boosters.getChild(1).getChild(0);
			actCMx = mmt.getComponentCG().x;
			assertEquals(expCMx, actCMx, EPSILON, " Motor Mount Tube CMx calculated incorrectly: ");

			expCMx = 0.19393939;
			FinSet boosterFins = (FinSet) boosters.getChild(1).getChild(1);
			actCMx = boosterFins.getComponentCG().x;
			assertEquals(expCMx, actCMx, EPSILON, "Core Fins CMx calculated incorrectly: ");
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
			assertEquals(3.937551444398643E-5, payloadNose.getRotationalInertia(), EPSILON,
					payloadNose.getName() + " Rotational MOI calculated incorrectly: ");
			assertEquals(4.983150394809428E-5, payloadNose.getLongitudinalInertia(), EPSILON,
					payloadNose.getName() + " Longitudinal MOI calculated incorrectly: ");

			// Component: Payload BodyTube
			final BodyTube payloadBody = (BodyTube) payloadStage.getChild(1);
			assertEquals(7.70416e-5, payloadBody.getRotationalInertia(), EPSILON,
					payloadBody.getName() + " Rotational MOI calculated incorrectly: ");
			assertEquals(8.06940e-5, payloadBody.getLongitudinalInertia(), EPSILON,
					payloadBody.getName() + " Longitudinal MOI calculated incorrectly: ");

			// Component: Payload Trailing Transition
			final Transition payloadTail = (Transition) payloadStage.getChild(2);
			assertEquals(1.43691e-5, payloadTail.getRotationalInertia(), EPSILON,
					payloadTail.getName() + " Rotational MOI calculated incorrectly: ");
			assertEquals(7.30265e-6, payloadTail.getLongitudinalInertia(), EPSILON,
					payloadTail.getName() + " Longitudinal MOI calculated incorrectly: ");

			// Component: Interstage
			cc = rocket.getChild(0).getChild(3);
			expInertia = 4.22073e-5;
			compInertia = cc.getRotationalInertia();
			assertEquals(expInertia, compInertia, EPSILON, cc.getName() + " Rotational MOI calculated incorrectly: ");
			expInertia = 0.0001;
			compInertia = cc.getLongitudinalInertia();
			assertEquals(expInertia, compInertia, EPSILON, cc.getName() + " Longitudinal MOI calculated incorrectly: ");

			{
				cc = rocket.getChild(0).getChild(3).getChild(0);
				expInertia = 6.23121e-7;
				compInertia = cc.getRotationalInertia();
				assertEquals(expInertia, compInertia, EPSILON,
						cc.getName() + " Rotational MOI calculated incorrectly: ");
				expInertia = 7.26975e-7;
				compInertia = cc.getLongitudinalInertia();
				assertEquals(expInertia, compInertia, EPSILON,
						cc.getName() + " Longitudinal MOI calculated incorrectly: ");

				cc = rocket.getChild(0).getChild(3).getChild(1);
				expInertia = 5.625e-8;
				compInertia = cc.getRotationalInertia();
				assertEquals(expInertia, compInertia, EPSILON,
						cc.getName() + " Rotational MOI calculated incorrectly: ");
				expInertia = 6.5625e-8;
				compInertia = cc.getLongitudinalInertia();
				assertEquals(expInertia, compInertia, EPSILON,
						cc.getName() + " Longitudinal MOI calculated incorrectly: ");
			}

			cc = rocket.getChild(0).getChild(4);
			expInertia = 2.81382e-5;
			compInertia = cc.getRotationalInertia();
			assertEquals(expInertia, compInertia, EPSILON, cc.getName() + " Rotational MOI calculated incorrectly: ");
			expInertia = 3.74486e-5;
			compInertia = cc.getLongitudinalInertia();
			assertEquals(expInertia, compInertia, EPSILON, cc.getName() + " Longitudinal MOI calculated incorrectly: ");
		}

		// ====== Core Stage ======
		// ====== ====== ======
		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		{
			final BodyTube coreBody = (BodyTube) coreStage.getChild(0);
			expInertia = 0.000187588;
			compInertia = coreBody.getRotationalInertia();
			assertEquals(expInertia, compInertia, EPSILON,
					coreBody.getName() + " Rotational MOI calculated incorrectly: ");
			expInertia = 0.00702105;
			compInertia = coreBody.getLongitudinalInertia();
			assertEquals(expInertia, compInertia, EPSILON,
					coreBody.getName() + " Longitudinal MOI calculated incorrectly: ");

		}

		// ====== Booster Set Stage ======
		// ====== ====== ======
		ParallelStage boosters = (ParallelStage) coreStage.getChild(0).getChild(0);
		{
			final NoseCone boosterNose = (NoseCone) boosters.getChild(0);
			expInertia = 1.9052671920796627E-5;
			compInertia = boosterNose.getRotationalInertia();
			assertEquals(expInertia, compInertia, EPSILON,
					boosterNose.getName() + " Rotational MOI calculated incorrectly: ");
			expInertia = 2.2559876786981176E-5;
			compInertia = boosterNose.getLongitudinalInertia();
			assertEquals(expInertia, compInertia, EPSILON,
					boosterNose.getName() + " Longitudinal MOI calculated incorrectly: ");

			final BodyTube boosterBody = (BodyTube) boosters.getChild(1);
			expInertia = 1.875878651e-4;
			compInertia = boosterBody.getRotationalInertia();
			assertEquals(expInertia, compInertia, EPSILON,
					boosterBody.getName() + " Rotational MOI calculated incorrectly: ");
			expInertia = 0.00702104762;
			compInertia = boosterBody.getLongitudinalInertia();
			assertEquals(expInertia, compInertia, EPSILON,
					boosterBody.getName() + " Longitudinal MOI calculated incorrectly: ");

			cc = boosters.getChild(1).getChild(0);
			expInertia = 4.11444e-6;
			compInertia = cc.getRotationalInertia();
			assertEquals(expInertia, compInertia, EPSILON, cc.getName() + " Rotational MOI calculated incorrectly: ");
			expInertia = 3.75062e-5;
			compInertia = cc.getLongitudinalInertia();
			assertEquals(expInertia, compInertia, EPSILON, cc.getName() + " Longitudinal MOI calculated incorrectly: ");

			final FinSet boosterFins = (FinSet) boosters.getChild(1).getChild(1);
			expInertia = 0.000928545614574877;
			compInertia = boosterFins.getRotationalInertia();
			assertEquals(expInertia, compInertia, EPSILON, boosterFins.getName() + " Rotational MOI calculated incorrectly: ");
			expInertia = 0.001246261927287438;
			compInertia = boosterFins.getLongitudinalInertia();
			assertEquals(expInertia, compInertia, EPSILON, boosterFins.getName() + " Longitudinal MOI calculated incorrectly: ");

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

		double expMass = 0.11628853296935873;
		double expCMx = 0.2780673116227175;
		assertEquals(expMass, actualCM.weight, EPSILON, "Upper Stage Mass is incorrect: ");

		assertEquals(expCMx, actualCM.x, EPSILON, "Upper Stage CM.x is incorrect: ");
		assertEquals(0.0f, actualCM.y, EPSILON, "Upper Stage CM.y is incorrect: ");
		assertEquals(0.0f, actualCM.z, EPSILON, "Upper Stage CM.z is incorrect: ");
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
		assertEquals(expMass, actualCM.weight, EPSILON, "Upper Stage Mass is incorrect: ");

		assertEquals(expCMx, actualCM.x, EPSILON, "Upper Stage CM.x is incorrect: ");
		assertEquals(0.0f, actualCM.y, EPSILON, "Upper Stage CM.y is incorrect: ");
		assertEquals(0.0f, actualCM.z, EPSILON, "Upper Stage CM.z is incorrect: ");
	}

	@Test
	public void testFalcon9HCoreMotorLaunchCM() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket
				.getFlightConfiguration(new FlightConfigurationId(TestRockets.FALCON_9H_FCID_1));
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

		assertEquals(expMotorMass, actMotorMass, EPSILON,
				core.getName() + " => " + motorDesignation + " propellant mass is incorrect: ");
		assertEquals(expCM.x, actCM.x, EPSILON,
				core.getName() + " => " + motorDesignation + " propellant CoM x is incorrect: ");
		assertEquals(expCM.y, actCM.y, EPSILON,
				core.getName() + " => " + motorDesignation + " propellant CoM y is incorrect: ");
		assertEquals(expCM.z, actCM.z, EPSILON,
				core.getName() + " => " + motorDesignation + " propellant CoM z is incorrect: ");
	}

	@Test
	public void testFalcon9HCoreMotorLaunchMOIs() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket
				.getFlightConfiguration(new FlightConfigurationId(TestRockets.FALCON_9H_FCID_1));
		config.setOnlyStage(1);

		RigidBody corePropInertia = MassCalculator.calculateMotor(config);

		// validated against a specific motor/radius/length
		final double expIxx = 0.003380625;

		final double expIyy = 0.156701835;

		final double actCorePropIxx = corePropInertia.getIxx();
		final double actCorePropIyy = corePropInertia.getIyy();

		assertEquals(expIxx, actCorePropIxx, EPSILON, "Core Stage motor axial MOI is incorrect: ");
		assertEquals(expIyy, actCorePropIyy, EPSILON, "Core Stage motor longitudinal MOI is incorrect: ");
	}

	@Test
	public void testFalcon9HBoosterStructureCM() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket.getEmptyConfiguration();

		config.setOnlyStage(TestRockets.FALCON_9H_BOOSTER_STAGE_NUMBER);

		final RigidBody actualData = MassCalculator.calculateStructure(config);
		final Coordinate actualCM = actualData.getCM();

		double expMass = 0.6063587938961827;
		double expCMx = 1.0750544407309763;
		assertEquals(expMass, actualCM.weight, EPSILON, "Heavy Booster Mass is incorrect: ");

		assertEquals(expCMx, actualCM.x, EPSILON, "Heavy Booster CM.x is incorrect: ");
		assertEquals(0.0f, actualCM.y, EPSILON, "Heavy Booster CM.y is incorrect: ");
		assertEquals(0.0f, actualCM.z, EPSILON, "Heavy Booster CM.z is incorrect: ");
	}

	@Test
	public void testFalcon9HBoosterLaunchCM() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket
				.getFlightConfiguration(new FlightConfigurationId(TestRockets.FALCON_9H_FCID_1));
		config.setOnlyStage(TestRockets.FALCON_9H_BOOSTER_STAGE_NUMBER);

		RigidBody actualBoosterLaunchData = MassCalculator.calculateLaunch(config);

		double actualMass = actualBoosterLaunchData.getMass();
		double expectedMass = 1.5903587938961827;
		assertEquals(expectedMass, actualMass, EPSILON, " Booster Launch Mass is incorrect: ");

		final Coordinate actualCM = actualBoosterLaunchData.getCM();
		double expectedCMx = 1.223107189094683;
		Coordinate expCM = new Coordinate(expectedCMx, 0, 0, expectedMass);
		assertEquals(expCM.x, actualCM.x, EPSILON, " Booster Launch CM.x is incorrect: ");
		assertEquals(expCM.y, actualCM.y, EPSILON, " Booster Launch CM.y is incorrect: ");
		assertEquals(expCM.z, actualCM.z, EPSILON, " Booster Launch CM.z is incorrect: ");
		assertEquals(expCM, actualCM, " Booster Launch CM is incorrect: ");
	}

	@Test
	public void testFalcon9HBoosterSpentCM() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket
				.getFlightConfiguration(new FlightConfigurationId(TestRockets.FALCON_9H_FCID_1));
		config.setOnlyStage(TestRockets.FALCON_9H_BOOSTER_STAGE_NUMBER);

		// Validate Booster Launch Mass
		RigidBody spentData = MassCalculator.calculateBurnout(config);
		Coordinate spentCM = spentData.getCM();

		double expSpentMass = 1.1183587938961828;
		double expSpentCMx = 1.1846026528203366;
		Coordinate expLaunchCM = new Coordinate(expSpentCMx, 0, 0, expSpentMass);
		assertEquals(expLaunchCM.weight, spentCM.weight, EPSILON, " Booster Launch Mass is incorrect: ");
		assertEquals(expLaunchCM.x, spentCM.x, EPSILON, " Booster Launch CM.x is incorrect: ");
		assertEquals(expLaunchCM.y, spentCM.y, EPSILON, " Booster Launch CM.y is incorrect: ");
		assertEquals(expLaunchCM.z, spentCM.z, EPSILON, " Booster Launch CM.z is incorrect: ");
		assertEquals(expLaunchCM, spentCM, " Booster Launch CM is incorrect: ");
	}

	@Test
	public void testFalcon9HBoosterMotorCM() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket
				.getFlightConfiguration(new FlightConfigurationId(TestRockets.FALCON_9H_FCID_1));
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

		assertEquals(expBoosterPropMass, actualPropellant.getMass(), EPSILON,
				boosters.getName() + " => " + boosterMotor.getDesignation() + " propellant mass is incorrect: ");
		assertEquals(expCM.x, actCM.x, EPSILON,
				boosters.getName() + " => " + boosterMotor.getDesignation() + " propellant CoM x is incorrect: ");
		assertEquals(expCM.y, actCM.y, EPSILON,
				boosters.getName() + " => " + boosterMotor.getDesignation() + " propellant CoM y is incorrect: ");
		assertEquals(expCM.z, actCM.z, EPSILON,
				boosters.getName() + " => " + boosterMotor.getDesignation() + " propellant CoM z is incorrect: ");
	}

	@Test
	public void testFalcon9HeavyBoosterMotorLaunchMOIs() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket
				.getFlightConfiguration(new FlightConfigurationId(TestRockets.FALCON_9H_FCID_1));
		config.setOnlyStage(TestRockets.FALCON_9H_BOOSTER_STAGE_NUMBER);

		RigidBody actualInertia = MassCalculator.calculateMotor(config);

		// System.err.println( rocket.toDebugTree());

		final double expIxx = 0.006380379;
		assertEquals(expIxx, actualInertia.getIxx(), EPSILON, "Booster stage propellant axial MOI is incorrect: ");
		final double expIyy = 0.001312553;
		assertEquals(expIyy, actualInertia.getIyy(),
				EPSILON, "Booster stage propellant longitudinal MOI is incorrect: ");
	}

	@Test
	public void testFalcon9HeavyBoosterSpentMOIs() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket
				.getFlightConfiguration(new FlightConfigurationId(TestRockets.FALCON_9H_FCID_1));
		config.setOnlyStage(TestRockets.FALCON_9H_BOOSTER_STAGE_NUMBER);

		RigidBody spent = MassCalculator.calculateBurnout(config);

		double expMOIRotational = 0.009193574474290651;
		double boosterMOIRotational = spent.getRotationalInertia();
		assertEquals(expMOIRotational, boosterMOIRotational, EPSILON, " Booster x-axis MOI is incorrect: ");

		double expMOI_tr = 0.05741546005688325;
		double boosterMOI_tr = spent.getLongitudinalInertia();
		assertEquals(expMOI_tr, boosterMOI_tr, EPSILON, " Booster transverse MOI is incorrect: ");
	}

	@Test
	public void testFalcon9HeavyBoosterLaunchMOIs() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		rocket.setName("Falcon9Heavy." + Thread.currentThread().getStackTrace()[1].getMethodName());

		FlightConfiguration config = rocket
				.getFlightConfiguration(new FlightConfigurationId(TestRockets.FALCON_9H_FCID_1));
		config.setOnlyStage(TestRockets.FALCON_9H_BOOSTER_STAGE_NUMBER);

		RigidBody launchData = MassCalculator.calculateLaunch(config);

		final double expIxx = 0.012254081474290652;
		final double actIxx = launchData.getRotationalInertia();
		final double expIyy = 0.06363179384136365;
		final double actIyy = launchData.getLongitudinalInertia();

		assertEquals(expIxx, actIxx, EPSILON, " Booster x-axis MOI is incorrect: ");
		assertEquals(expIyy, actIyy, EPSILON, " Booster transverse MOI is incorrect: ");
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
		assertEquals(expTotalMass, calcTotalMass, EPSILON, " Booster Launch Mass is incorrect: ");

		double expCMx = 5.92;
		Coordinate expCM = new Coordinate(expCMx, 0, 0, expTotalMass);
		assertEquals(expCM.x, boosterSetCM.x, EPSILON, " Booster Launch CM.x is incorrect: ");
		assertEquals(expCM.y, boosterSetCM.y, EPSILON, " Booster Launch CM.y is incorrect: ");
		assertEquals(expCM.z, boosterSetCM.z, EPSILON, " Booster Launch CM.z is incorrect: ");
		assertEquals(expCM, boosterSetCM, " Booster Launch CM is incorrect: ");

		// Validate MOI
		double expMOI_axial = 0.005873702474290652;
		double boosterMOI_xx = burnout.getRotationalInertia();
		assertEquals(expMOI_axial, boosterMOI_xx, EPSILON, " Booster x-axis MOI is incorrect: ");

		double expMOI_tr = 17.78089035006232;
		double boosterMOI_tr = burnout.getLongitudinalInertia();
		assertEquals(expMOI_tr, boosterMOI_tr, EPSILON, " Booster transverse MOI is incorrect: ");
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
		assertEquals(expTotalMass, boosterData.getMass(), EPSILON, " Booster Launch Mass is incorrect: ");

		double expCMx = 0.2827146624421746;
		Coordinate expCM = new Coordinate(expCMx, 0, 0, expTotalMass);
		assertEquals(expCM.x, boosterCM.x, EPSILON, " Booster Launch CM.x is incorrect: ");
		assertEquals(expCM.y, boosterCM.y, EPSILON, " Booster Launch CM.y is incorrect: ");
		assertEquals(expCM.z, boosterCM.z, EPSILON, " Booster Launch CM.z is incorrect: ");
		assertEquals(expCM, boosterCM, " Booster Launch CM is incorrect: ");

		// Validate MOI
		double expMOI_axial = 0.02493025354590946;
		double boosterMOI_xx = boosterData.getRotationalInertia();
		assertEquals(expMOI_axial, boosterMOI_xx, EPSILON, " Booster x-axis MOI is incorrect: ");

		double expMOI_tr = 0.3488283595364345;
		double boosterMOI_tr = boosterData.getLongitudinalInertia();
		assertEquals(expMOI_tr, boosterMOI_tr, EPSILON, " Booster transverse MOI is incorrect: ");
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
		final double expMass = 0.6063587938961827;
		double calcTotalMass = structure.getMass();
		assertEquals(expMass, calcTotalMass, EPSILON, " Booster Launch Mass is incorrect: ");

		final double expCMx = 0.5567081598531745;
		Coordinate expCM = new Coordinate(expCMx, 0, 0, expMass);
		assertEquals(expCM.x, structure.getCM().x, EPSILON, " Booster Launch CM.x is incorrect: ");
		assertEquals(expCM.y, structure.getCM().y, EPSILON, " Booster Launch CM.y is incorrect: ");
		assertEquals(expCM.z, structure.getCM().z, EPSILON, " Booster Launch CM.z is incorrect: ");
		assertEquals(expCM, structure.getCM(), " Booster Launch CM is incorrect: ");

		// Validate MOI
		final double expMOI_axial = 0.005873702474290652;
		double boosterMOI_xx = structure.getRotationalInertia();
		assertEquals(expMOI_axial, boosterMOI_xx, EPSILON, " Booster x-axis MOI is incorrect: ");

		final double expMOI_tr = 0.04063515705066886;
		double boosterMOI_tr = structure.getLongitudinalInertia();
		assertEquals(expMOI_tr, boosterMOI_tr, EPSILON, " Booster transverse MOI is incorrect: ");
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
		pods.setAxialMethod(AxialMethod.BOTTOM);
		pods.setAxialOffset(0.0);
		pods.setAngleMethod(AngleMethod.RELATIVE);
		pods.setAngleOffset(0.0);
		pods.setRadiusMethod(RadiusMethod.FREE);
		pods.setRadiusOffset(0.04);

		final BodyTube podBody = new BodyTube(0.0, 0.02);
		podBody.setThickness(0.001);
		podBody.setName("Primary Body");
		pods.addChild(podBody);

		final TrapezoidFinSet fins = new TrapezoidFinSet(1, 0.05, 0.05, 0.0, 0.001);
		fins.setName("podFins");
		fins.setThickness(0.01);
		fins.setMassOverridden(true);
		fins.setOverrideMass(0.02835);
		fins.setSubcomponentsOverriddenMass(false);
		fins.setAxialOffset(-0.01);
		fins.setAxialMethod(AxialMethod.BOTTOM);
		podBody.addChild(fins);

		rocket.enableEvents();
		config.setAllStages();

		// =======================================================================

		// // DEBUG
		// System.err.println(rocket.toDebugTree());

		RigidBody structure = MassCalculator.calculateStructure(config);
		final double expMass = 0.0900260149;
		double calcTotalMass = structure.getMass();
		assertEquals(expMass, calcTotalMass, EPSILON, " Booster Launch Mass is incorrect: ");

		final double expCMx = 0.3039199615;
		final double expCMy = 0.0;
		final double expCMz = 0.0;
		Coordinate expCM = new Coordinate(expCMx, expCMy, expCMz, expMass);
		assertEquals(expCM.x, structure.getCM().x, EPSILON, " Booster Launch CM.x is incorrect: ");
		assertEquals(expCM.y, structure.getCM().y, EPSILON, " Booster Launch CM.y is incorrect: ");
		assertEquals(expCM.z, structure.getCM().z, EPSILON, " Booster Launch CM.z is incorrect: ");
		assertEquals(expCM, structure.getCM(), " Booster Launch CM is incorrect: ");

	}

	@Test
	public void testEmptyStages() {
		Rocket rocketRef = TestRockets.makeEstesAlphaIII(); // Reference rocket
		FlightConfiguration configRef = rocketRef.getEmptyConfiguration();
		configRef.setAllStages();

		final RigidBody structureRef = MassCalculator.calculateStructure(configRef);
		final double rocketDryMassRef = structureRef.cm.weight;
		final Coordinate rocketDryCMRef = structureRef.cm;

		Rocket rocket = TestRockets.makeEstesAlphaIII();
		AxialStage stage1 = new AxialStage(); // To be added to the front of the rocket
		AxialStage stage2 = new AxialStage(); // To be added to the rear of the rocket
		rocket.addChild(stage1, 0);
		rocket.addChild(stage2);
		FlightConfiguration config = rocket.getEmptyConfiguration();
		config.setAllStages();

		final RigidBody structure = MassCalculator.calculateStructure(config);
		final double rocketDryMass = structure.cm.weight;
		final Coordinate rocketDryCM = structure.cm;

		assertEquals(rocketDryMassRef, rocketDryMass, EPSILON, " Empty Stages Rocket Empty Mass is incorrect: ");

		assertEquals(rocketDryCMRef.x, rocketDryCM.x, EPSILON, "Empty Stages Rocket CM.x is incorrect: ");
		assertEquals(rocketDryCMRef.y, rocketDryCM.y, EPSILON, "Empty Stages Rocket CM.y is incorrect: ");
		assertEquals(rocketDryCMRef.z, rocketDryCM.z, EPSILON, "Empty Stages Rocket CM.z is incorrect: ");
		assertEquals(rocketDryCMRef, rocketDryCM, "Empty Stages Rocket CM is incorrect: ");

		double MOIrotRef = structureRef.getRotationalInertia();
		double MOIlongRef = structureRef.getLongitudinalInertia();

		double MOIrot = structure.getRotationalInertia();
		double MOIlong = structure.getLongitudinalInertia();
		assertEquals(MOIrotRef, MOIrot, EPSILON, "Empty Stages Rocket Rotational MOI calculated incorrectly: ");
		assertEquals(MOIlongRef, MOIlong, EPSILON, "Empty Stages Rocket Longitudinal MOI calculated incorrectly: ");

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

		assertEquals(overrideRocketDryCMRef, overrideRocketDryCM, "Empty Stages Rocket Override CM is incorrect: ");

		double overrideMOIrotRef = overrideStructureRef.getRotationalInertia();
		double overrideMOIlongRef = overrideStructureRef.getLongitudinalInertia();
		double overrideMOIrot = overrideStructure.getRotationalInertia();
		double overrideMOIlong = overrideStructure.getLongitudinalInertia();
		assertEquals(overrideMOIrotRef, overrideMOIrot,
				EPSILON, "Empty Stages Rocket Rotational MOI calculated incorrectly: ");
		assertEquals(overrideMOIlongRef,
				overrideMOIlong, EPSILON, "Empty Stages Rocket Longitudinal MOI calculated incorrectly: ");
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

	@Test
	public void testTubeFinMass() {
		Rocket rocket = OpenRocketDocumentFactory.createNewRocket().getRocket();
		AxialStage stage = rocket.getStage(0);
		BodyTube bodyTube = new BodyTube();
		stage.addChild(bodyTube);
		TubeFinSet tubeFinSet = new TubeFinSet();
		tubeFinSet.setOuterRadius(0.04);
		tubeFinSet.setThickness(0.002);
		tubeFinSet.setLength(0.1);
		tubeFinSet.setInstanceCount(3);
		bodyTube.addChild(tubeFinSet);

		assertEquals(0.0001470265, tubeFinSet.getComponentVolume(), EPSILON);
		assertEquals(0.0999780446, tubeFinSet.getComponentMass(), EPSILON);
		assertEquals(0.0999780446, tubeFinSet.getMass(), EPSILON);

		tubeFinSet.setInstanceCount(4);

		assertEquals(0.000196035, tubeFinSet.getComponentVolume(), EPSILON);
		assertEquals(0.133304059, tubeFinSet.getComponentMass(), EPSILON);
		assertEquals(0.133304059, tubeFinSet.getMass(), EPSILON);

		tubeFinSet.setMassOverridden(true);
		tubeFinSet.setOverrideMass(0.02);

		assertEquals(0.133304059, tubeFinSet.getComponentMass(), EPSILON);
		assertEquals(0.02, tubeFinSet.getMass(), EPSILON);
	}

}
