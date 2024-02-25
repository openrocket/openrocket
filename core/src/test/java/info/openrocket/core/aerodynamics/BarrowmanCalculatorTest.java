package info.openrocket.core.aerodynamics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import info.openrocket.core.logging.WarningSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import info.openrocket.core.ServicesForTesting;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.ParallelStage;
import info.openrocket.core.rocketcomponent.PodSet;
import info.openrocket.core.rocketcomponent.RailButton;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.TestRockets;

public class BarrowmanCalculatorTest {
	protected final double EPSILON = 0.00001;

	private static Injector injector;

	@BeforeAll
	public static void setup() {
		Module applicationModule = new ServicesForTesting();
		Module pluginModule = new PluginModule();

		injector = Guice.createInjector(applicationModule, pluginModule);
		Application.setInjector(injector);

		// {
		// GuiModule guiModule = new GuiModule();
		// Module pluginModule = new PluginModule();
		// Injector injector = Guice.createInjector(guiModule, pluginModule);
		// Application.setInjector(injector);
		// }
	}

	/**
	 * Test a completely empty rocket.
	 */
	@Test
	public void testEmptyRocket() {
		// First test completely empty rocket
		Rocket rocket = new Rocket();
		FlightConfiguration config = rocket.getSelectedConfiguration();
		BarrowmanCalculator calc = new BarrowmanCalculator();
		FlightConditions conditions = new FlightConditions(config);
		WarningSet warnings = new WarningSet();

		Coordinate cp_calc = calc.getCP(config, conditions, warnings);

		assertEquals(0.0, cp_calc.weight, 0.0, " Empty rocket CNa value is incorrect:");
		assertEquals(0.0, cp_calc.x, 0.0, " Empty rocket cp x value is incorrect:");
		assertEquals(0.0, cp_calc.y, 0.0, " Empty rocket cp y value is incorrect:");
		assertEquals(0.0, cp_calc.z, 0.0, " Empty rocket cp z value is incorrect:");
	}

	@Test
	public void testCPSimpleDry() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		AxialStage stage = (AxialStage) rocket.getChild(0);
		FlightConfiguration config = rocket.getSelectedConfiguration();
		BarrowmanCalculator calc = new BarrowmanCalculator();
		FlightConditions conditions = new FlightConditions(config);
		WarningSet warnings = new WarningSet();

		// By Hand: i.e. Manually calculate the Barrowman numbers
		double exp_cna;
		double exp_cpx;
		{
			NoseCone nose = (NoseCone) stage.getChild(0);
			assertEquals(0.07, nose.getLength(), EPSILON, " Estes Alpha III nose cone has incorrect length:");
			assertEquals(0.012, nose.getAftRadius(), EPSILON, " Estes Alpha III nosecone has wrong (base) radius:");
			assertEquals(Transition.Shape.OGIVE, nose.getShapeType(), " Estes Alpha III nosecone has wrong type:");
			double cna_nose = 2;
			double cpx_nose = 0.03235;

			double cna_body = 0; // equal-to-zero, see [Barrowman66] p15.
			double cpx_body = 0;

			double cna_3fin = 24.146933;
			double cpx_3fin = 0.0193484;
			double fin_x = 0.22;
			cpx_3fin += fin_x;

			double cna_lugs = 0; // n/a
			double cpx_lugs = 0; // n/a

			// N.B. CP @ AoA = zero
			exp_cna = cna_nose + cna_body + cna_3fin + cna_lugs;
			exp_cpx = (cna_nose * cpx_nose + cna_body * cpx_body + cna_3fin * cpx_3fin + cna_lugs * cpx_lugs) / exp_cna;
		}

		Coordinate cp_calc = calc.getCP(config, conditions, warnings);

		assertEquals(exp_cna, cp_calc.weight, EPSILON, " Estes Alpha III CNa value is incorrect:");
		assertEquals(exp_cpx, cp_calc.x, EPSILON, " Estes Alpha III cp x value is incorrect:");
		assertEquals(0.0, cp_calc.y, EPSILON, " Estes Alpha III cp y value is incorrect:");
	}

	@Test
	public void testCPSimpleWithMotor() {
		Rocket rkt = TestRockets.makeEstesAlphaIII();
		FlightConfiguration config = rkt.getSelectedConfiguration();
		AerodynamicCalculator calc = new BarrowmanCalculator();
		FlightConditions conditions = new FlightConditions(config);
		WarningSet warnings = new WarningSet();

		// calculated from OpenRocket 15.03:
		// double expCPx = 0.225;
		// verified from the equations:
		double expCPx = 0.2235154;
		double exp_cna = 26.146933;
		Coordinate calcCP = calc.getCP(config, conditions, warnings);

		assertEquals(expCPx, calcCP.x, EPSILON, " Estes Alpha III cp x value is incorrect:");
		assertEquals(exp_cna, calcCP.weight, EPSILON, " Estes Alpha III CNa value is incorrect:");
	}

	// Component CP calculations resulting in expected test values are in comments
	// in TestRockets.makeFalcon9Heavy()
	@Test
	public void testCPParallelBoosters() {
		final Rocket rocket = TestRockets.makeFalcon9Heavy();
		final ParallelStage boosterStage = (ParallelStage) rocket.getChild(1).getChild(0).getChild(0);
		final TrapezoidFinSet boosterFins = (TrapezoidFinSet) boosterStage.getChild(1).getChild(1);
		final FlightConfiguration config = rocket.getSelectedConfiguration();
		final BarrowmanCalculator calc = new BarrowmanCalculator();
		final FlightConditions conditions = new FlightConditions(config);
		final WarningSet warnings = new WarningSet();

		{
			boosterFins.setFinCount(3);
			final Coordinate cp_3fin = calc.getCP(config, conditions, warnings);
			assertEquals(16.51651439, cp_3fin.weight, EPSILON, " Falcon 9 Heavy CNa value is incorrect:");
			assertEquals(1.00667319, cp_3fin.x, EPSILON, " Falcon 9 Heavy CP x value is incorrect:");
			assertEquals(0.0, cp_3fin.y, EPSILON, " Falcon 9 Heavy CP y value is incorrect:");
			assertEquals(0.0, cp_3fin.z, EPSILON, " Falcon 9 Heavy CP z value is incorrect:");
		}
		{
			boosterFins.setFinCount(2);
			boosterFins.setAngleOffset(Math.PI / 4);
			final Coordinate cp_2fin = calc.getCP(config, conditions, warnings);
			assertEquals(12.1073483560, cp_2fin.weight, EPSILON, " Falcon 9 Heavy CNa value is incorrect:");
			assertEquals(0.9440139181, cp_2fin.x, EPSILON, " Falcon 9 Heavy CP x value is incorrect:");
			assertEquals(0.0, cp_2fin.y, EPSILON, " Falcon 9 Heavy CP y value is incorrect:");
			assertEquals(0.0, cp_2fin.z, EPSILON, " Falcon 9 Heavy CP z value is incorrect:");
		}
		{
			boosterFins.setFinCount(1);
			final Coordinate cp_1fin = calc.getCP(config, conditions, warnings);
			assertEquals(7.6981823141, cp_1fin.weight, EPSILON, " Falcon 9 Heavy CNa value is incorrect:");
			assertEquals(0.8095779106, cp_1fin.x, EPSILON, " Falcon 9 Heavy CP x value is incorrect:");
			assertEquals(0f, cp_1fin.y, EPSILON, " Falcon 9 Heavy CP y value is incorrect:");
			assertEquals(0f, cp_1fin.z, EPSILON, " Falcon 9 Heavy CP z value is incorrect:");
		}
	}

	@Test
	public void testFinCountEffect() {
		final BarrowmanCalculator calc = new BarrowmanCalculator();
		final WarningSet warnings = new WarningSet();

		final Rocket rocket = TestRockets.makeEstesAlphaIII();
		final FlightConfiguration config = rocket.getSelectedConfiguration();
		final FlightConditions conditions = new FlightConditions(config);
		{
			((FinSet) rocket.getChild(0).getChild(1).getChild(0)).setFinCount(4);
			final Coordinate wholeRocketCP = calc.getCP(config, conditions, warnings);
			assertEquals(34.19591165, wholeRocketCP.weight, EPSILON, "Split-Fin Rocket CNa value is incorrect:");
			assertEquals(0.22724216, wholeRocketCP.x, EPSILON, "Split-Fin Rocket CP x value is incorrect:");
		}
		{
			((FinSet) rocket.getChild(0).getChild(1).getChild(0)).setFinCount(3);
			final Coordinate wholeRocketCP = calc.getCP(config, conditions, warnings);
			assertEquals(26.14693374, wholeRocketCP.weight, EPSILON, "Split-Fin Rocket CNa value is incorrect:");
			assertEquals(0.22351541, wholeRocketCP.x, EPSILON, "Split-Fin Rocket CP x value is incorrect:");
		}
		{
			((FinSet) rocket.getChild(0).getChild(1).getChild(0)).setFinCount(2);
			final Coordinate wholeRocketCP = calc.getCP(config, conditions, warnings);
			assertEquals(2.0, wholeRocketCP.weight, EPSILON, "Split-Fin Rocket CNa value is incorrect:");
			assertEquals(0.032356, wholeRocketCP.x, EPSILON, "Split-Fin Rocket CP x value is incorrect:");
		}
		{
			((FinSet) rocket.getChild(0).getChild(1).getChild(0)).setFinCount(1);
			final Coordinate wholeRocketCP = calc.getCP(config, conditions, warnings);
			assertEquals(2.0, wholeRocketCP.weight, EPSILON, "Split-Fin Rocket CNa value is incorrect:");
			assertEquals(0.032356, wholeRocketCP.x, EPSILON, "Split-Fin Rocket CP x value is incorrect:");
		}
	}

	@Test
	public void testCpSplitTripleFin() {
		final BarrowmanCalculator calc = new BarrowmanCalculator();
		final WarningSet warnings = new WarningSet();

		final Rocket rocket = TestRockets.makeEstesAlphaIII();
		final FlightConfiguration config = rocket.getSelectedConfiguration();
		final FlightConditions conditions = new FlightConditions(config);

		{
			final Coordinate wholeRocketCP = calc.getCP(config, conditions, warnings);
			assertEquals(26.14693374, wholeRocketCP.weight, EPSILON, "Split-Fin Rocket CNa value is incorrect:");
			assertEquals(0.22351541, wholeRocketCP.x, EPSILON, "Split-Fin Rocket CP x value is incorrect:");
		}
		{
			final BodyTube body = (BodyTube) rocket.getChild(0).getChild(1);
			final TrapezoidFinSet fins = (TrapezoidFinSet) body.getChild(0);
			fins.setAngleOffset(0);
			TestRockets.splitRocketFins(body, fins, 3);

			final Coordinate wholeRocketCP = calc.getCP(config, conditions, warnings);
			assertEquals(26.14693374, wholeRocketCP.weight, EPSILON, "Split-Fin Rocket CNa value is incorrect:");
			assertEquals(0.22351541, wholeRocketCP.x, EPSILON, "Split-Fin Rocket CP x value is incorrect:");
		}
	}

	@Test
	public void testCpSplitQuadrupleFin() {
		final BarrowmanCalculator calc = new BarrowmanCalculator();
		final WarningSet warnings = new WarningSet();

		final Rocket rocket = TestRockets.makeEstesAlphaIII();
		final FlightConfiguration config = rocket.getSelectedConfiguration();
		final FlightConditions conditions = new FlightConditions(config);

		{
			((FinSet) rocket.getChild(0).getChild(1).getChild(0)).setFinCount(4);
			final Coordinate wholeRocketCP = calc.getCP(config, conditions, warnings);
			assertEquals(34.19591165, wholeRocketCP.weight, EPSILON, "Split-Fin Rocket CNa value is incorrect:");
			assertEquals(0.22724, wholeRocketCP.x, EPSILON, "Split-Fin Rocket CP x value is incorrect:");
		}
		{
			final BodyTube body = (BodyTube) rocket.getChild(0).getChild(1);
			final TrapezoidFinSet fins = (TrapezoidFinSet) body.getChild(0);
			TestRockets.splitRocketFins(body, fins, 4);

			final Coordinate wholeRocketCP = calc.getCP(config, conditions, warnings);
			assertEquals(34.19591165, wholeRocketCP.weight, EPSILON, "Split-Fin Rocket CNa value is incorrect:");
			assertEquals(0.22724, wholeRocketCP.x, EPSILON, "Split-Fin Rocket CP x value is incorrect:");
		}
	}

	// test rocket with endplates on fins. Comments tracing
	// calculation of CP are in TestRockets.makeEndPlateRocket().
	@Test
	public void testEndPlateCP() {
		final Rocket rocket = TestRockets.makeEndPlateRocket();
		final FlightConfiguration config = new FlightConfiguration(rocket, null);
		// rocket.setFlightConfiguration(config.getId(), config);
		// rocket.setSelectedConfiguration(config.getId());
		final AerodynamicCalculator calc = new BarrowmanCalculator();
		final FlightConditions conditions = new FlightConditions(config);
		final WarningSet warnings = new WarningSet();

		final Coordinate cp = calc.getCP(config, conditions, warnings);
		assertEquals(0.25461, cp.x, EPSILON, " Endplate rocket cp x value is incorrect:");
		assertEquals(0.0, cp.y, EPSILON, " Endplate rocket cp y value is incorrect:");
		assertEquals(0.0, cp.z, EPSILON, " Endplate rocket cp z value is incorrect:");
		assertEquals(40.96857, cp.weight, EPSILON, " Endplate rocket CNa value is incorrect:");
	}

	@Test
	public void testGetWorstCP() {
		// Rocket rocket = TestRockets.makeFalcon9Heavy();
		// FlightConfiguration config = rocket.getSelectedConfiguration();
		// BarrowmanCalculator calc = new BarrowmanCalculator();
		// FlightConditions conditions = new FlightConditions(config);
		// WarningSet warnings = new WarningSet();

		// NYI
		// Coordinate calcBestCP = calc.getCP(config, conditions, warnings);
		// Coordinate calcWorstCP = calc.getWorstCP(config, conditions, warnings);

		// fail("Not yet implemented");
		// Coordinate expBestCP = new Coordinate( -1, 0,0,0);
		// assertEquals(expBestCP.x,
		// calcBestCP.x, EPSILON, " Falcon Heavy best CP x value is incorrect:");
		// Coordinate expWorstCP = new Coordinate( -1, 0,0,0);
		// assertEquals(expWorstCP.x,
		// calcWorstCP.x, EPSILON, " Falcon Heavy Worst CP x value is incorrect:");
	}

	@Test
	public void testContinuousRocket() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		AerodynamicCalculator calc = new BarrowmanCalculator();
		FlightConfiguration configuration = rocket.getSelectedConfiguration();
		WarningSet warnings = new WarningSet();

		calc.checkGeometry(configuration, rocket, warnings);
		assertTrue(warnings.isEmpty(), "Estes Alpha III should be continuous: ");
	}

	@Test
	public void testContinuousRocketWithStrapOns() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		AerodynamicCalculator calc = new BarrowmanCalculator();
		FlightConfiguration configuration = rocket.getSelectedConfiguration();
		WarningSet warnings = new WarningSet();

		calc.checkGeometry(configuration, rocket, warnings);
		assertTrue(warnings.isEmpty(), "F9H should be continuous: ");
	}

	@Test
	public void testRadialDiscontinuousRocket() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		AerodynamicCalculator calc = new BarrowmanCalculator();
		FlightConfiguration configuration = rocket.getSelectedConfiguration();
		WarningSet warnings = new WarningSet();

		NoseCone nose = (NoseCone) rocket.getChild(0).getChild(0);
		BodyTube body = (BodyTube) rocket.getChild(0).getChild(1);

		nose.setAftRadius(0.015);
		body.setOuterRadius(0.012);
		body.setName(body.getName() + "  << discontinuous");

		calc.checkGeometry(configuration, rocket, warnings);
		assertFalse(warnings.isEmpty(), " Estes Alpha III has an undetected discontinuity:");
	}

	@Test
	public void testRadialDiscontinuityWithStrapOns() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		AerodynamicCalculator calc = new BarrowmanCalculator();
		FlightConfiguration configuration = rocket.getSelectedConfiguration();
		WarningSet warnings = new WarningSet();

		final AxialStage coreStage = (AxialStage) rocket.getChild(1);
		final ParallelStage booster = (ParallelStage) coreStage.getChild(0).getChild(0);

		NoseCone nose = (NoseCone) booster.getChild(0);
		BodyTube body = (BodyTube) booster.getChild(1);

		nose.setAftRadius(0.015);
		body.setOuterRadius(0.012);
		body.setName(body.getName() + "  << discontinuous");

		calc.checkGeometry(configuration, rocket, warnings);
		assertFalse(warnings.isEmpty(), " Missed discontinuity in Falcon 9 Heavy:");
	}

	@Test
	public void testPhantomTubes() {
		Rocket rocketNoPods = TestRockets.makeEstesAlphaIII();
		FlightConfiguration configNoPods = rocketNoPods.getSelectedConfiguration();
		FlightConditions conditionsNoPods = new FlightConditions(configNoPods);
		WarningSet warningsNoPods = new WarningSet();

		Rocket rocketWithPods = TestRockets.makeEstesAlphaIIIWithPods();
		FlightConfiguration configPods = rocketWithPods.getSelectedConfiguration();
		FlightConditions conditionsPods = new FlightConditions(configPods);
		WarningSet warningsPods = new WarningSet();
		AerodynamicCalculator calcPods = new BarrowmanCalculator();
		AerodynamicCalculator calcNoPods = new BarrowmanCalculator();

		final AerodynamicForces forcesNoPods = calcPods.getAerodynamicForces(configNoPods, conditionsNoPods,
				warningsNoPods);
		final AerodynamicForces forcesPods = calcPods.getAerodynamicForces(configPods, conditionsPods, warningsPods);
		assertEquals(forcesPods.getCD(),
				forcesNoPods.getCD(), EPSILON, " Estes Alpha III With Pods rocket CD value is incorrect:");

		// The "with pods" version has no way of seeing the fins are
		// on the actual body tube rather than the phantom tubes,
		// so CD won't take fin-body interference into consideration.
		// So we'll adjust our CD in these tests. The magic numbers
		// in x and w come from temporarily disabling the
		// interference calculation in FinSetCalc and comparing
		// results with and without it
		// cpNoPods (0.34125,0.00000,0.00000,w=16.20502) -- interference disabled
		// cpNoPods (0.34797,0.00000,0.00000,w=19.34773) -- interference enabled

		final Coordinate cpNoPods = calcNoPods.getCP(configNoPods, conditionsNoPods, warningsNoPods);
		final Coordinate cpPods = calcPods.getCP(configPods, conditionsPods, warningsPods);
		assertEquals(cpNoPods.x - 0.002788761352, cpPods.x,
				EPSILON, " Alpha III With Pods rocket cp x value is incorrect:");
		assertEquals(cpNoPods.y, cpPods.y, EPSILON, " Alpha III With Pods rocket cp y value is incorrect:");
		assertEquals(cpNoPods.z, cpPods.z, EPSILON, " Alpha III With Pods rocket cp z value is incorrect:");
		assertEquals(cpPods.weight, cpNoPods.weight - 3.91572,
				EPSILON, " Alpha III With Pods rocket CNa value is incorrect:");
	}

	/**
	 * Tests whether adding extra empty stages has an effect.
	 */
	@Test
	public void testEmptyStages() {
		// Reference rocket
		Rocket rocketRef = TestRockets.makeEstesAlphaIII();
		FlightConfiguration configRef = rocketRef.getSelectedConfiguration();
		BarrowmanCalculator calcRef = new BarrowmanCalculator();
		FlightConditions conditionsRef = new FlightConditions(configRef);
		WarningSet warnings = new WarningSet();

		Coordinate cp_calcRef = calcRef.getCP(configRef, conditionsRef, warnings);

		// First test with adding an empty stage in the front of the design
		Rocket rocketFront = TestRockets.makeEstesAlphaIII();
		AxialStage stage1 = new AxialStage(); // To be placed in front of the design
		rocketFront.addChild(stage1, 0);
		FlightConfiguration configFront = rocketFront.getSelectedConfiguration();
		BarrowmanCalculator calcFront = new BarrowmanCalculator();
		FlightConditions conditionsFront = new FlightConditions(configFront);
		warnings = new WarningSet();

		Coordinate cp_calcFront = calcFront.getCP(configFront, conditionsFront, warnings);

		assertEquals(cp_calcRef.weight,
				cp_calcFront.weight, EPSILON, " Estes Alpha III with front empty stage CNa value is incorrect:");
		assertEquals(cp_calcRef.x, cp_calcFront.x,
				EPSILON, " Estes Alpha III with front empty stage cp x value is incorrect:");
		assertEquals(cp_calcRef.y, cp_calcFront.y,
				EPSILON, " Estes Alpha III with front empty stage cp y value is incorrect:");
		assertEquals(cp_calcRef.z, cp_calcFront.z,
				EPSILON, " Estes Alpha III with front empty stage cp z value is incorrect:");

		// Now test with adding an empty stage in the rear of the design
		Rocket rocketRear = TestRockets.makeEstesAlphaIII();
		AxialStage stage2 = new AxialStage(); // To be placed in the rear of the design
		rocketRear.addChild(stage2);
		FlightConfiguration configRear = rocketRear.getSelectedConfiguration();
		BarrowmanCalculator calcRear = new BarrowmanCalculator();
		FlightConditions conditionsRear = new FlightConditions(configRear);
		warnings = new WarningSet();

		Coordinate cp_calcRear = calcRear.getCP(configRear, conditionsRear, warnings);

		assertEquals(cp_calcRef.weight,
				cp_calcRear.weight, EPSILON, " Estes Alpha III with rear empty stage CNa value is incorrect:");
		assertEquals(cp_calcRef.x, cp_calcRear.x,
				EPSILON, " Estes Alpha III with rear empty stage cp x value is incorrect:");
		assertEquals(cp_calcRef.y, cp_calcRear.y,
				EPSILON, " Estes Alpha III with rear empty stage cp y value is incorrect:");
		assertEquals(cp_calcRef.z, cp_calcRear.z,
				EPSILON, " Estes Alpha III with rear empty stage cp z value is incorrect:");

		// Test with multiple empty stages
		Rocket rocketMulti = rocketFront;
		AxialStage stage3 = new AxialStage(); // To be placed in the rear of the design
		rocketMulti.addChild(stage3);
		FlightConfiguration configMulti = rocketMulti.getSelectedConfiguration();
		BarrowmanCalculator calcMulti = new BarrowmanCalculator();
		FlightConditions conditionsMulti = new FlightConditions(configMulti);
		warnings = new WarningSet();

		Coordinate cp_calcMulti = calcMulti.getCP(configMulti, conditionsMulti, warnings);

		assertEquals(cp_calcRef.weight,
				cp_calcMulti.weight, EPSILON, " Estes Alpha III with multiple empty stages CNa value is incorrect:");
		assertEquals(cp_calcRef.x,
				cp_calcMulti.x, EPSILON, " Estes Alpha III with multiple empty stages cp x value is incorrect:");
		assertEquals(cp_calcRef.y,
				cp_calcMulti.y, EPSILON, " Estes Alpha III with multiple empty stages cp y value is incorrect:");
		assertEquals(cp_calcRef.z,
				cp_calcMulti.z, EPSILON, " Estes Alpha III with multiple empty stages cp z value is incorrect:");
	}

	/**
	 * Tests in-line pod aerodynamics and warnings
	 *
	 */
	@Test
	public void testInlinePods() {
		WarningSet warnings = new WarningSet();

		// reference rocket and results
		final Rocket refRocket = TestRockets.makeEstesAlphaIII();
		final FlightConfiguration refConfig = refRocket.getSelectedConfiguration();
		final FlightConditions refConditions = new FlightConditions(refConfig);

		final BarrowmanCalculator refCalc = new BarrowmanCalculator();
		double refCP = refCalc.getCP(refConfig, refConditions, warnings).x;
		final AerodynamicForces refForces = refCalc.getAerodynamicForces(refConfig, refConditions, warnings);
		assertTrue(warnings.isEmpty(), "reference rocket should have no warnings");
		final double refCD = refForces.getCD();

		// test rocket
		final Rocket testRocket = TestRockets.makeEstesAlphaIIIwithInlinePod();
		final PodSet pod = (PodSet) testRocket.getChild(0).getChild(1).getChild(0);
		final FlightConfiguration testConfig = testRocket.getSelectedConfiguration();
		final FlightConditions testConditions = new FlightConditions(testConfig);

		final BarrowmanCalculator testCalc = new BarrowmanCalculator();
		double testCP = testCalc.getCP(testConfig, testConditions, warnings).x;
		final AerodynamicForces testForces = testCalc.getAerodynamicForces(testConfig, testConditions, warnings);
		assertTrue(warnings.isEmpty(), "test rocket should have no warnings");

		assertEquals(refCP, testCP, EPSILON, "ref and test rocket CP should match");

		final double testCD = testForces.getCD();
		assertEquals(refCD, testCD, EPSILON, "ref and test rocket CD should match");

		// move the pod back.
		pod.setAxialOffset(pod.getAxialOffset() + 0.1);
		testCP = testCalc.getCP(testConfig, testConditions, warnings).x;
		assertEquals(1, warnings.size(), "should be warning from gap in airframe");

		// move the pod forward.
		warnings.clear();
		pod.setAxialOffset(pod.getAxialOffset() - 0.3);
		testCP = testCalc.getCP(testConfig, testConditions, warnings).x;
		assertEquals(1, warnings.size(), "should be warning from airframe overlap");

		// move the pod back.
		warnings.clear();
		pod.setAxialOffset(pod.getAxialOffset() + 0.1);
		testCP = testCalc.getCP(testConfig, testConditions, warnings).x;
		assertEquals(1, warnings.size(), "should be warning from podset airframe overlap");
	}

	@Test
	public void testBaseDragWithOverride() {
		final WarningSet warnings = new WarningSet();
		final BarrowmanCalculator calc = new BarrowmanCalculator();

		// get base drag of minimal rocket consisting of just a tube.
		final Rocket tubeRocket = new Rocket();
		final AxialStage tubeStage = new AxialStage();
		tubeRocket.addChild(tubeStage);

		final BodyTube tubeBodyTube = new BodyTube();
		tubeStage.addChild(tubeBodyTube);

		final FlightConfiguration tubeConfig = new FlightConfiguration(tubeRocket);
		final FlightConditions tubeConditions = new FlightConditions(tubeConfig);
		final AerodynamicForces tubeForces = calc.getAerodynamicForces(tubeConfig, tubeConditions, warnings);
		final double tubeBaseCD = tubeForces.getBaseCD();

		// get base CD of minimal rocket consisting of just a cone
		final Rocket coneRocket = new Rocket();
		final AxialStage coneStage = new AxialStage();
		coneRocket.addChild(coneStage);

		NoseCone coneCone = new NoseCone();
		coneCone.setAftRadius(tubeBodyTube.getOuterRadius());
		coneStage.addChild(coneCone);

		final FlightConfiguration coneConfig = new FlightConfiguration(coneRocket);
		final FlightConditions coneConditions = new FlightConditions(coneConfig);
		final AerodynamicForces coneForces = calc.getAerodynamicForces(coneConfig, coneConditions, warnings);
		final double coneBaseCD = coneForces.getBaseCD();

		// now our test rocket, with a tube and a cone
		final Rocket testRocket = new Rocket();
		final AxialStage testStage = new AxialStage();
		testRocket.addChild(testStage);

		final BodyTube testTube = new BodyTube();
		testTube.setOuterRadius(tubeBodyTube.getOuterRadius());
		testStage.addChild(testTube);

		final NoseCone testCone = new NoseCone();
		testCone.setAftRadius(coneCone.getAftRadius());
		testStage.addChild(testCone);

		FlightConfiguration testConfig = new FlightConfiguration(testRocket);
		FlightConditions testConditions = new FlightConditions(testConfig);

		// no overrides
		AerodynamicForces testForces = calc.getAerodynamicForces(testConfig, testConditions, warnings);
		assertEquals(tubeBaseCD + coneBaseCD,
				testForces.getBaseCD(), EPSILON, "base CD should be base CD of tube plus base CD of cone");

		// override tube CD
		testTube.setCDOverridden(true);
		testTube.setOverrideCD(0);
		testForces = calc.getAerodynamicForces(testConfig, testConditions, warnings);
		assertEquals(coneBaseCD, testForces.getBaseCD(), EPSILON, "base CD should be base CD of cone");

		// override cone CD
		testCone.setCDOverridden(true);
		testCone.setOverrideCD(0);
		testForces = calc.getAerodynamicForces(testConfig, testConditions, warnings);
		assertEquals(0.0, testForces.getBaseCD(), EPSILON, "base CD should be 0");

		// and turn off tube override
		testTube.setCDOverridden(false);
		testForces = calc.getAerodynamicForces(testConfig, testConditions, warnings);
		assertEquals(tubeBaseCD, testForces.getBaseCD(), EPSILON, "base CD should be base CD of tube");
	}

	/**
	 * Tests railbutton drag. Really is testing instancing more than actual drag
	 * calculations, and making
	 * sure we don't divide by 0 when not moving
	 */
	@Test
	public void testRailButtonDrag() {
		// minimal rocket with nothing on it but two railbuttons
		final Rocket rocket = new Rocket();

		final AxialStage stage = new AxialStage();
		rocket.addChild(stage);

		// phantom tubes have no drag to confuse things
		final BodyTube phantom = new BodyTube();
		phantom.setOuterRadius(0);
		stage.addChild(phantom);

		// set up test environment
		WarningSet warnings = new WarningSet();
		final FlightConfiguration config = rocket.getSelectedConfiguration();
		final FlightConditions conditions = new FlightConditions(config);
		final BarrowmanCalculator calc = new BarrowmanCalculator();

		// part 1: instancing

		// Put two individual railbuttons and get their CD
		final RailButton button1 = new RailButton();
		button1.setInstanceCount(1);
		button1.setAxialOffset(1.0);
		phantom.addChild(button1);

		final RailButton button2 = new RailButton();
		button2.setInstanceCount(1);
		button2.setAxialOffset(2.0);
		phantom.addChild(button2);

		final AerodynamicForces individualForces = calc.getAerodynamicForces(config, conditions, warnings);
		final double individualCD = individualForces.getCD();

		// get rid of individual buttons and put in a railbutton set with two instances
		// at same locations as original
		// railbuttons
		phantom.removeChild(button1);
		phantom.removeChild(button2);

		final RailButton buttons = new RailButton();
		buttons.setInstanceCount(2);
		buttons.setAxialOffset(1.0);
		buttons.setInstanceSeparation(1.0);

		final AerodynamicForces pairForces = calc.getAerodynamicForces(config, conditions, warnings);
		final double pairCD = pairForces.getCD();

		assertEquals(individualCD, pairCD, EPSILON, "two individual railbuttons should have same CD as a pair");

		// part 2: test at Mach 0
		conditions.setMach(MathUtil.EPSILON);
		final AerodynamicForces epsForces = calc.getAerodynamicForces(config, conditions, warnings);
		final double epsCD = epsForces.getCD();

		conditions.setMach(0);
		final AerodynamicForces zeroForces = calc.getAerodynamicForces(config, conditions, warnings);
		final double zeroCD = zeroForces.getCD();
		assertEquals(epsCD, zeroCD, EPSILON, "drag at mach 0 should equal drag at mach MathUtil.EPSILON");
	}
}
