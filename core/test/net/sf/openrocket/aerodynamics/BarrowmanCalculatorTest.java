package net.sf.openrocket.aerodynamics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import net.sf.openrocket.ServicesForTesting;
import net.sf.openrocket.plugin.PluginModule;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.ParallelStage;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.TestRockets;

public class BarrowmanCalculatorTest {
	protected final double EPSILON = MathUtil.EPSILON;
	
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
		FlightConfiguration config = rocket.getSelectedConfiguration();
		AerodynamicCalculator calc = new BarrowmanCalculator();
		FlightConditions conditions = new FlightConditions(config);
		WarningSet warnings = new WarningSet();
		
		// calculated from OpenRocket 15.03
		double expCPx = 0.225; // cm
		Coordinate calcCP = calc.getCP(config, conditions, warnings);
		
		assertEquals(" Estes Alpha III cp x value is incorrect:", expCPx, calcCP.x, EPSILON);
		Coordinate expCP = new Coordinate(expCPx, 0,0,0);
		assertEquals(" Estes Alpha III CP is incorrect:", expCP, calcCP);
	}
	
	@Test
	public void testCPSimpleWithMotor() {
		Rocket rkt = TestRockets.makeEstesAlphaIII();
		FlightConfiguration config = rkt.getSelectedConfiguration();
		AerodynamicCalculator calc = new BarrowmanCalculator();
		FlightConditions conditions = new FlightConditions(config);
		WarningSet warnings = new WarningSet();
		
		
		// calculated from OpenRocket 15.03
		double expCPx = 0.225; // cm
		/// this is what the 
		Coordinate calcCP = calc.getCP(config, conditions, warnings);
		
		assertEquals(" Estes Alpha III cp x value is incorrect:", expCPx, calcCP.x, EPSILON);
		Coordinate expCP = new Coordinate(expCPx, 0,0,0);
		assertEquals(" Estes Alpha III CP is incorrect:", expCP, calcCP);
		fail("Not yet implemented");
	}
	
	
	@Test
	public void testCPDoubleStrapOn() {
		Rocket rocket = TestRockets.makeFalcon9Heavy();
		FlightConfiguration config = rocket.getSelectedConfiguration();
		BarrowmanCalculator calc = new BarrowmanCalculator();
		FlightConditions conditions = new FlightConditions(config);
		WarningSet warnings = new WarningSet();
		
		calc.debug = true;
		// calculated from OpenRocket 15.03
		double expCPx = 0.225; // cm
		Coordinate calcCP = calc.getCP(config, conditions, warnings);
		
		fail("NYI");
		assertEquals(" Falcon Heavy CP x value is incorrect:", expCPx, calcCP.x, EPSILON);
		Coordinate expCP = new Coordinate(expCPx, 0,0,0);
		assertEquals(" Falcon Heavy CP is incorrect:", expCP, calcCP);
	}
	
	@Test
	public void testGetWorstCP() {
		fail("Not yet implemented");
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
		
		ParallelStage booster = (ParallelStage)rocket.getChild(1).getChild(1); 
		NoseCone nose = (NoseCone)booster.getChild(0);
		BodyTube body = (BodyTube)booster.getChild(1);
		
		nose.setAftRadius(0.015);
		body.setOuterRadius( 0.012 );
		body.setName( body.getName()+"  << discontinuous");
		
		assertFalse(" Missed discontinuity in Falcon 9 Heavy:", calc.isContinuous( rocket));
	}
}
