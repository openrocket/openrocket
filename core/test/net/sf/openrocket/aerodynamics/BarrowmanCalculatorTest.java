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
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.ParallelStage;
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
			assertEquals(" Falcon 9 Heavy CNa value is incorrect:", 21.5111598, cp_3fin.weight, EPSILON);
			assertEquals(" Falcon 9 Heavy CP x value is incorrect:", 1.04662388, cp_3fin.x, EPSILON);
			assertEquals(" Falcon 9 Heavy CP y value is incorrect:", 0.0, cp_3fin.y, EPSILON);
			assertEquals(" Falcon 9 Heavy CP z value is incorrect:", 0.0, cp_3fin.z, EPSILON);
		}{
			boosterFins.setFinCount(2);
			final Coordinate cp_2fin = calc.getCP(config, conditions, warnings);
			assertEquals(" Falcon 9 Heavy CNa value is incorrect:", 27.585207667168696, cp_2fin.weight, EPSILON);
			assertEquals(" Falcon 9 Heavy CP x value is incorrect:", 1.0757127676940474, cp_2fin.x, EPSILON);
			assertEquals(" Falcon 9 Heavy CP y value is incorrect:", 0.0, cp_2fin.y, EPSILON);
			assertEquals(" Falcon 9 Heavy CP z value is incorrect:", 0.0, cp_2fin.z, EPSILON);
		}{
			boosterFins.setFinCount(1);
			final Coordinate cp_1fin = calc.getCP(config, conditions, warnings);
			assertEquals(" Falcon 9 Heavy CNa value is incorrect:", 15.43711196967902, cp_1fin.weight, EPSILON);
			assertEquals(" Falcon 9 Heavy CP x value is incorrect:", 0.99464, cp_1fin.x, EPSILON);
			assertEquals(" Falcon 9 Heavy CP y value is incorrect:", 0f, cp_1fin.y, EPSILON);
			assertEquals(" Falcon 9 Heavy CP z value is incorrect:", 0f, cp_1fin.z, EPSILON);
		}{
			// absent -- 3.28901627g @[0.31469937,0.05133333,0.00000000]
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
		
		assertTrue("Estes Alpha III should be continous: ", calc.isContinuous( rocket));
	}
	
	@Test
	public void testContinuousRocketWithStrapOns() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		AerodynamicCalculator calc = new BarrowmanCalculator();
		
		assertTrue("F9H should be continuous: ", calc.isContinuous( rocket));
	}
	
	@Test
	public void testRadialDiscontinuousRocket() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		AerodynamicCalculator calc = new BarrowmanCalculator();
		
		NoseCone nose = (NoseCone)rocket.getChild(0).getChild(0);
		BodyTube body = (BodyTube)rocket.getChild(0).getChild(1);
		
		nose.setAftRadius(0.015);
		body.setOuterRadius( 0.012 );
		body.setName( body.getName()+"  << discontinuous");
		
		assertFalse(" Estes Alpha III has an undetected discontinuity:", calc.isContinuous( rocket));
	}
	
	@Test
	public void testRadialDiscontinuityWithStrapOns() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		AerodynamicCalculator calc = new BarrowmanCalculator();
		
		final AxialStage coreStage = (AxialStage)rocket.getChild(1);
		final ParallelStage booster = (ParallelStage)coreStage.getChild(0).getChild(0);
				
		NoseCone nose = (NoseCone)booster.getChild(0);
		BodyTube body = (BodyTube)booster.getChild(1);
		
		nose.setAftRadius(0.015);
		body.setOuterRadius( 0.012 );
		body.setName( body.getName()+"  << discontinuous");
		
		assertFalse(" Missed discontinuity in Falcon 9 Heavy:", calc.isContinuous( rocket));
	}
}
