package net.sf.openrocket.aerodynamics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import net.sf.openrocket.ServicesForTesting;
import net.sf.openrocket.plugin.PluginModule;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.ParallelStage;
import net.sf.openrocket.rocketcomponent.PodSet;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.TestRockets;

public class BarrowmanCalculatorTest {
	protected final double EPSILON = 0.00001;
	
	private static Injector injector;
	
	@BeforeClass
	public static void setup() {
		Module applicationModule = new ServicesForTesting();
		Module pluginModule = new PluginModule();
		
		injector = Guice.createInjector( applicationModule, pluginModule);
		Application.setInjector(injector);
		
//		{
//			GuiModule guiModule = new GuiModule();
//			Module pluginModule = new PluginModule();
//			Injector injector = Guice.createInjector(guiModule, pluginModule);
//			Application.setInjector(injector);
//		}
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

		assertEquals(" Empty rocket CNa value is incorrect:", 0.0, cp_calc.weight , 0.0);
		assertEquals(" Empty rocket cp x value is incorrect:", 0.0, cp_calc.x , 0.0);
		assertEquals(" Empty rocket cp y value is incorrect:", 0.0, cp_calc.y , 0.0);
		assertEquals(" Empty rocket cp z value is incorrect:", 0.0, cp_calc.z , 0.0);
	}
	
	@Test
	public void testCPSimpleDry() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		AxialStage stage = (AxialStage)rocket.getChild(0);
		FlightConfiguration config = rocket.getSelectedConfiguration();
		BarrowmanCalculator calc = new BarrowmanCalculator();
		FlightConditions conditions = new FlightConditions(config);
		WarningSet warnings = new WarningSet();
		
		// By Hand:  i.e. Manually calculate the Barrowman numbers
		double exp_cna;
		double exp_cpx;
		{
			NoseCone nose = (NoseCone)stage.getChild(0);
			assertEquals(" Estes Alpha III nose cone has incorrect length:", 0.07, nose.getLength(), EPSILON);
			assertEquals(" Estes Alpha III nosecone has wrong (base) radius:", 0.012, nose.getAftRadius(), EPSILON);
			assertEquals(" Estes Alpha III nosecone has wrong type:", Transition.Shape.OGIVE, nose.getType());
			double cna_nose = 2;
			double cpx_nose = 0.03235;
			
			double cna_body=0; // equal-to-zero, see [Barrowman66] p15.
			double cpx_body=0;
			
			double cna_3fin = 24.146933;
			double cpx_3fin = 0.0193484;
			double fin_x = 0.22;
			cpx_3fin += fin_x;
			
			double cna_lugs=0; // n/a
			double cpx_lugs=0; // n/a
			
			// N.B. CP @ AoA = zero
			exp_cna = cna_nose + cna_body + cna_3fin + cna_lugs;
			exp_cpx = ( cna_nose*cpx_nose + cna_body*cpx_body + cna_3fin*cpx_3fin + cna_lugs*cpx_lugs)/exp_cna;
		}
		
		Coordinate cp_calc = calc.getCP(config, conditions, warnings);
		
		assertEquals(" Estes Alpha III CNa value is incorrect:", exp_cna, cp_calc.weight, EPSILON);
		assertEquals(" Estes Alpha III cp x value is incorrect:", exp_cpx, cp_calc.x, EPSILON);
		assertEquals(" Estes Alpha III cp y value is incorrect:", 0.0, cp_calc.y, EPSILON);
	}
	
	@Test
	public void testCPSimpleWithMotor() {
		Rocket rkt = TestRockets.makeEstesAlphaIII();
		FlightConfiguration config = rkt.getSelectedConfiguration();
		AerodynamicCalculator calc = new BarrowmanCalculator();
		FlightConditions conditions = new FlightConditions(config);
		WarningSet warnings = new WarningSet();

		// calculated from OpenRocket 15.03:
		//double expCPx = 0.225;
		// verified from the equations: 
		double expCPx = 0.2235154;
		double exp_cna = 26.146933;
		Coordinate calcCP = calc.getCP(config, conditions, warnings);
		 
		assertEquals(" Estes Alpha III cp x value is incorrect:", expCPx, calcCP.x, EPSILON);
		assertEquals(" Estes Alpha III CNa value is incorrect:", exp_cna, calcCP.weight, EPSILON);
	}

	// Component CP calculations resulting in expected test values are in comments in TestRockets.makeFalcon9Heavy()
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
			assertEquals(" Falcon 9 Heavy CNa value is incorrect:", 16.51651439, cp_3fin.weight, EPSILON);
			assertEquals(" Falcon 9 Heavy CP x value is incorrect:", 1.00667319, cp_3fin.x, EPSILON);
			assertEquals(" Falcon 9 Heavy CP y value is incorrect:", 0.0, cp_3fin.y, EPSILON);
			assertEquals(" Falcon 9 Heavy CP z value is incorrect:", 0.0, cp_3fin.z, EPSILON);
		}{
			boosterFins.setFinCount(2);
			boosterFins.setAngleOffset(Math.PI/4);
			final Coordinate cp_2fin = calc.getCP(config, conditions, warnings);
			assertEquals(" Falcon 9 Heavy CNa value is incorrect:", 12.1073483560, cp_2fin.weight, EPSILON);
			assertEquals(" Falcon 9 Heavy CP x value is incorrect:", 0.9440139181, cp_2fin.x, EPSILON);
			assertEquals(" Falcon 9 Heavy CP y value is incorrect:", 0.0, cp_2fin.y, EPSILON);
			assertEquals(" Falcon 9 Heavy CP z value is incorrect:", 0.0, cp_2fin.z, EPSILON);
		}{
			boosterFins.setFinCount(1);
			final Coordinate cp_1fin = calc.getCP(config, conditions, warnings);
			assertEquals(" Falcon 9 Heavy CNa value is incorrect:",  7.6981823141, cp_1fin.weight, EPSILON);
			assertEquals(" Falcon 9 Heavy CP x value is incorrect:", 0.8095779106, cp_1fin.x, EPSILON);
			assertEquals(" Falcon 9 Heavy CP y value is incorrect:", 0f, cp_1fin.y, EPSILON);
			assertEquals(" Falcon 9 Heavy CP z value is incorrect:", 0f, cp_1fin.z, EPSILON);
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
			((FinSet)rocket.getChild(0).getChild(1).getChild(0)).setFinCount(4);
			final Coordinate wholeRocketCP = calc.getCP(config, conditions, warnings);
			assertEquals("Split-Fin Rocket CNa value is incorrect:", 34.19591165, wholeRocketCP.weight, EPSILON);
			assertEquals("Split-Fin Rocket CP x value is incorrect:", 0.22724216, wholeRocketCP.x, EPSILON);
		}{
			((FinSet)rocket.getChild(0).getChild(1).getChild(0)).setFinCount(3);
			final Coordinate wholeRocketCP = calc.getCP(config, conditions, warnings);
			assertEquals("Split-Fin Rocket CNa value is incorrect:", 26.14693374, wholeRocketCP.weight, EPSILON);
			assertEquals("Split-Fin Rocket CP x value is incorrect:", 0.22351541, wholeRocketCP.x, EPSILON);
		}{
			((FinSet)rocket.getChild(0).getChild(1).getChild(0)).setFinCount(2);
			final Coordinate wholeRocketCP = calc.getCP(config, conditions, warnings);
			assertEquals("Split-Fin Rocket CNa value is incorrect:", 2.0, wholeRocketCP.weight, EPSILON);
			assertEquals("Split-Fin Rocket CP x value is incorrect:", 0.032356, wholeRocketCP.x, EPSILON);
		}{
			((FinSet)rocket.getChild(0).getChild(1).getChild(0)).setFinCount(1);
			final Coordinate wholeRocketCP = calc.getCP(config, conditions, warnings);
			assertEquals("Split-Fin Rocket CNa value is incorrect:", 2.0, wholeRocketCP.weight, EPSILON);
			assertEquals("Split-Fin Rocket CP x value is incorrect:", 0.032356, wholeRocketCP.x, EPSILON);
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
			assertEquals("Split-Fin Rocket CNa value is incorrect:", 26.14693374, wholeRocketCP.weight, EPSILON);
			assertEquals("Split-Fin Rocket CP x value is incorrect:", 0.22351541, wholeRocketCP.x, EPSILON);
		}{
			final BodyTube body = (BodyTube)rocket.getChild(0).getChild(1);
			final TrapezoidFinSet fins = (TrapezoidFinSet)body.getChild(0);
			fins.setAngleOffset(0);
			TestRockets.splitRocketFins(body, fins, 3);

			final Coordinate wholeRocketCP = calc.getCP(config, conditions, warnings);
			assertEquals("Split-Fin Rocket CNa value is incorrect:", 26.14693374, wholeRocketCP.weight, EPSILON);
			assertEquals("Split-Fin Rocket CP x value is incorrect:", 0.22351541, wholeRocketCP.x, EPSILON);
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
			((FinSet)rocket.getChild(0).getChild(1).getChild(0)).setFinCount(4);
			final Coordinate wholeRocketCP = calc.getCP(config, conditions, warnings);
			assertEquals("Split-Fin Rocket CNa value is incorrect:", 34.19591165, wholeRocketCP.weight, EPSILON);
			assertEquals("Split-Fin Rocket CP x value is incorrect:", 0.22724, wholeRocketCP.x, EPSILON);
		}{
			final BodyTube body = (BodyTube)rocket.getChild(0).getChild(1);
			final TrapezoidFinSet fins = (TrapezoidFinSet)body.getChild(0);
			TestRockets.splitRocketFins(body, fins, 4);

			final Coordinate wholeRocketCP = calc.getCP(config, conditions, warnings);
			assertEquals("Split-Fin Rocket CNa value is incorrect:", 34.19591165, wholeRocketCP.weight, EPSILON);
			assertEquals("Split-Fin Rocket CP x value is incorrect:", 0.22724, wholeRocketCP.x, EPSILON);
		}
	}
	// test rocket with endplates on fins.  Comments tracing
	// calculation of CP are in TestRockets.makeEndPlateRocket().
	@Test
	public void testEndPlateCP() {
		final Rocket rocket = TestRockets.makeEndPlateRocket();
		final FlightConfiguration config = new FlightConfiguration(rocket, null);
		//		rocket.setFlightConfiguration(config.getId(), config);
		//		rocket.setSelectedConfiguration(config.getId());
		final AerodynamicCalculator calc = new BarrowmanCalculator();
		final FlightConditions conditions = new FlightConditions(config);
		final WarningSet warnings = new WarningSet();

		final Coordinate cp = calc.getCP(config, conditions, warnings);
		assertEquals(" Endplate rocket cp x value is incorrect:", 0.25461, cp.x, EPSILON);
		assertEquals(" Endplate rocket cp y value is incorrect:", 0.0, cp.y, EPSILON);
		assertEquals(" Endplate rocket cp z value is incorrect:", 0.0, cp.z, EPSILON);
		assertEquals(" Endplate rocket CNa value is incorrect:", 40.96857, cp.weight, EPSILON);
	}
	
	@Test
	public void testGetWorstCP() {
//		Rocket rocket = TestRockets.makeFalcon9Heavy();
//		FlightConfiguration config = rocket.getSelectedConfiguration();
//		BarrowmanCalculator calc = new BarrowmanCalculator();
//		FlightConditions conditions = new FlightConditions(config);
//		WarningSet warnings = new WarningSet();
		
		// NYI
//		Coordinate calcBestCP = calc.getCP(config, conditions, warnings);
//		Coordinate calcWorstCP = calc.getWorstCP(config, conditions, warnings);
		
		//fail("Not yet implemented");
//		Coordinate expBestCP = new Coordinate( -1, 0,0,0);
//		assertEquals(" Falcon Heavy best CP x value is incorrect:", expBestCP.x, calcBestCP.x, EPSILON);
//		Coordinate expWorstCP = new Coordinate( -1, 0,0,0);
//		assertEquals(" Falcon Heavy Worst CP x value is incorrect:", expWorstCP.x, calcWorstCP.x, EPSILON);
	}
	
	@Test
	public void testContinuousRocket() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		AerodynamicCalculator calc = new BarrowmanCalculator();
		FlightConfiguration configuration = rocket.getSelectedConfiguration();
		WarningSet warnings = new WarningSet();
		
		calc.checkGeometry(configuration, rocket, warnings);
		assertTrue("Estes Alpha III should be continuous: ", warnings.isEmpty());		
	}
	
	@Test
	public void testContinuousRocketWithStrapOns() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		AerodynamicCalculator calc = new BarrowmanCalculator();
		FlightConfiguration configuration = rocket.getSelectedConfiguration();
		WarningSet warnings = new WarningSet();
		
		calc.checkGeometry(configuration, rocket, warnings);
		assertTrue("F9H should be continuous: ", warnings.isEmpty());		
	}
	
	@Test
	public void testRadialDiscontinuousRocket() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		AerodynamicCalculator calc = new BarrowmanCalculator();
		FlightConfiguration configuration = rocket.getSelectedConfiguration();
		WarningSet warnings = new WarningSet();
		
		NoseCone nose = (NoseCone)rocket.getChild(0).getChild(0);
		BodyTube body = (BodyTube)rocket.getChild(0).getChild(1);
		
		nose.setAftRadius(0.015);
		body.setOuterRadius( 0.012 );
		body.setName( body.getName()+"  << discontinuous");

		calc.checkGeometry(configuration, rocket, warnings);
		assertFalse(" Estes Alpha III has an undetected discontinuity:", warnings.isEmpty());		
	}
	
	@Test
	public void testRadialDiscontinuityWithStrapOns() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		AerodynamicCalculator calc = new BarrowmanCalculator();
		FlightConfiguration configuration = rocket.getSelectedConfiguration();
		WarningSet warnings = new WarningSet();
		
		final AxialStage coreStage = (AxialStage)rocket.getChild(1);
		final ParallelStage booster = (ParallelStage)coreStage.getChild(0).getChild(0);
				
		NoseCone nose = (NoseCone)booster.getChild(0);
		BodyTube body = (BodyTube)booster.getChild(1);
		
		nose.setAftRadius(0.015);
		body.setOuterRadius( 0.012 );
		body.setName( body.getName()+"  << discontinuous");
		
		calc.checkGeometry(configuration, rocket, warnings);
		assertFalse(" Missed discontinuity in Falcon 9 Heavy:" , warnings.isEmpty());		
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

		final AerodynamicForces forcesNoPods = calcPods.getAerodynamicForces(configNoPods, conditionsNoPods, warningsNoPods);
		final AerodynamicForces forcesPods = calcPods.getAerodynamicForces(configPods, conditionsPods, warningsPods);
		assertEquals(" Estes Alpha III With Pods rocket CD value is incorrect:", forcesPods.getCD(), forcesNoPods.getCD(), EPSILON);

		// The "with pods" version has no way of seeing the fins are
		// on the actual body tube rather than the phantom tubes,
		// so CD won't take fin-body interference into consideration.
		// So we'll adjust our CD in these tests.  The magic numbers
		// in x and w come from temporarily disabling the
		// interference calculation in FinSetCalc and comparing
		// results with and without it
		// cpNoPods (0.34125,0.00000,0.00000,w=16.20502) -- interference disabled
		// cpNoPods (0.34797,0.00000,0.00000,w=19.34773) -- interference enabled
		
		final Coordinate cpNoPods = calcNoPods.getCP(configNoPods, conditionsNoPods, warningsNoPods);
		final Coordinate cpPods = calcPods.getCP(configPods, conditionsPods, warningsPods);
		assertEquals(" Alpha III With Pods rocket cp x value is incorrect:", cpNoPods.x - 0.002788761352, cpPods.x, EPSILON);
		assertEquals(" Alpha III With Pods rocket cp y value is incorrect:", cpNoPods.y, cpPods.y, EPSILON);
		assertEquals(" Alpha III With Pods rocket cp z value is incorrect:", cpNoPods.z, cpPods.z, EPSILON);
		assertEquals(" Alpha III With Pods rocket CNa value is incorrect:", cpPods.weight, cpNoPods.weight - 3.91572, EPSILON);
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
		AxialStage stage1 = new AxialStage();		// To be placed in front of the design
		rocketFront.addChild(stage1, 0);
		FlightConfiguration configFront = rocketFront.getSelectedConfiguration();
		BarrowmanCalculator calcFront = new BarrowmanCalculator();
		FlightConditions conditionsFront = new FlightConditions(configFront);
		warnings = new WarningSet();

		Coordinate cp_calcFront = calcFront.getCP(configFront, conditionsFront, warnings);

		assertEquals(" Estes Alpha III with front empty stage CNa value is incorrect:", cp_calcRef.weight, cp_calcFront.weight , EPSILON);
		assertEquals(" Estes Alpha III with front empty stage cp x value is incorrect:", cp_calcRef.x, cp_calcFront.x , EPSILON);
		assertEquals(" Estes Alpha III with front empty stage cp y value is incorrect:", cp_calcRef.y, cp_calcFront.y , EPSILON);
		assertEquals(" Estes Alpha III with front empty stage cp z value is incorrect:", cp_calcRef.z, cp_calcFront.z , EPSILON);

		// Now test with adding an empty stage in the rear of the design
		Rocket rocketRear = TestRockets.makeEstesAlphaIII();
		AxialStage stage2 = new AxialStage();		// To be placed in the rear of the design
		rocketRear.addChild(stage2);
		FlightConfiguration configRear = rocketRear.getSelectedConfiguration();
		BarrowmanCalculator calcRear = new BarrowmanCalculator();
		FlightConditions conditionsRear = new FlightConditions(configRear);
		warnings = new WarningSet();

		Coordinate cp_calcRear = calcRear.getCP(configRear, conditionsRear, warnings);

		assertEquals(" Estes Alpha III with rear empty stage CNa value is incorrect:", cp_calcRef.weight, cp_calcRear.weight , EPSILON);
		assertEquals(" Estes Alpha III with rear empty stage cp x value is incorrect:", cp_calcRef.x, cp_calcRear.x , EPSILON);
		assertEquals(" Estes Alpha III with rear empty stage cp y value is incorrect:", cp_calcRef.y, cp_calcRear.y , EPSILON);
		assertEquals(" Estes Alpha III with rear empty stage cp z value is incorrect:", cp_calcRef.z, cp_calcRear.z , EPSILON);

		// Test with multiple empty stages
		Rocket rocketMulti = rocketFront;
		AxialStage stage3 = new AxialStage();		// To be placed in the rear of the design
		rocketMulti.addChild(stage3);
		FlightConfiguration configMulti = rocketMulti.getSelectedConfiguration();
		BarrowmanCalculator calcMulti = new BarrowmanCalculator();
		FlightConditions conditionsMulti = new FlightConditions(configMulti);
		warnings = new WarningSet();

		Coordinate cp_calcMulti = calcMulti.getCP(configMulti, conditionsMulti, warnings);

		assertEquals(" Estes Alpha III with multiple empty stages CNa value is incorrect:", cp_calcRef.weight, cp_calcMulti.weight , EPSILON);
		assertEquals(" Estes Alpha III with multiple empty stages cp x value is incorrect:", cp_calcRef.x, cp_calcMulti.x , EPSILON);
		assertEquals(" Estes Alpha III with multiple empty stages cp y value is incorrect:", cp_calcRef.y, cp_calcMulti.y , EPSILON);
		assertEquals(" Estes Alpha III with multiple empty stages cp z value is incorrect:", cp_calcRef.z, cp_calcMulti.z , EPSILON);
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
		assertTrue("reference rocket should have no warnings", warnings.isEmpty());
		final double refCD = refForces.getCD();

		// test rocket
		final Rocket testRocket = TestRockets.makeEstesAlphaIIIwithInlinePod();
		final PodSet pod = (PodSet) testRocket.getChild(0).getChild(1).getChild(0);
		final FlightConfiguration testConfig = testRocket.getSelectedConfiguration();
		final FlightConditions testConditions = new FlightConditions(testConfig);
		
		TestRockets.dumpRocket(testRocket, "/home/joseph/rockets/openrocket/git/openrocket/work/testrocket.ork");
		final BarrowmanCalculator testCalc = new BarrowmanCalculator();
		double testCP = testCalc.getCP(testConfig, testConditions, warnings).x;
		final AerodynamicForces testForces = testCalc.getAerodynamicForces(testConfig, testConditions, warnings);
		assertTrue("test rocket should have no warnings", warnings.isEmpty());

		assertEquals("ref and test rocket CP should match", refCP, testCP, EPSILON);

		final double testCD = testForces.getCD();
		assertEquals("ref and test rocket CD should match", refCD, testCD, EPSILON);

		// move the pod back.
		pod.setAxialOffset(pod.getAxialOffset() + 0.1);
		testCP = testCalc.getCP(testConfig, testConditions, warnings).x;
		assertFalse("should be warning from gap in airframe", warnings.isEmpty());

		// move the pod forward.
		warnings.clear();
		pod.setAxialOffset(pod.getAxialOffset() - 0.2);
		testCP = testCalc.getCP(testConfig, testConditions, warnings).x;		
		assertFalse("should be warning from airframe overlap", warnings.isEmpty());
	}
		
}
