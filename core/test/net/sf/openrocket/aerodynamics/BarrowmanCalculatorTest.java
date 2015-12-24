package net.sf.openrocket.aerodynamics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import net.sf.openrocket.ServicesForTesting;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.plugin.PluginModule;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.InnerTube;
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
		FlightConfigurationId fcid = config.getFlightConfigurationID();
		AerodynamicCalculator calc = new BarrowmanCalculator();
		FlightConditions conditions = new FlightConditions(config);
		WarningSet warnings = new WarningSet();
		
		MotorConfiguration inst = TestRockets.getTestD12Motor();
		InnerTube motorTube = (InnerTube)rkt.getChild(0).getChild(1).getChild(1);
		motorTube.setMotorInstance(fcid, inst);
		motorTube.setMotorMount(true);
		motorTube.setMotorOverhang(0.005);
		
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
	
}
