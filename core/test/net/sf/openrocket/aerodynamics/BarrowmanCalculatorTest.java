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
	
	
	@Test
	public void testCPDoubleStrapOn() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		FlightConfiguration config = rocket.getSelectedConfiguration();
		BarrowmanCalculator calc = new BarrowmanCalculator();
		FlightConditions conditions = new FlightConditions(config);
		WarningSet warnings = new WarningSet();
		
		double expCPx = 0.994642;
		double expCNa = 15.437111;
		Coordinate calcCP = calc.getCP(config, conditions, warnings);
		
		assertEquals(" Falcon 9 Heavy CP x value is incorrect:", expCPx, calcCP.x, EPSILON);
		assertEquals(" Falcon 9 Heavy CNa value is incorrect:", expCNa, calcCP.weight, EPSILON);
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
		
		ParallelStage booster = (ParallelStage)rocket.getChild(1).getChild(0).getChild(1); 
		NoseCone nose = (NoseCone)booster.getChild(0);
		BodyTube body = (BodyTube)booster.getChild(1);
		
		nose.setAftRadius(0.015);
		body.setOuterRadius( 0.012 );
		body.setName( body.getName()+"  << discontinuous");
		
		assertFalse(" Missed discontinuity in Falcon 9 Heavy:", calc.isContinuous( rocket));
	}
}
