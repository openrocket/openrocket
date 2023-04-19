package net.sf.openrocket.aerodynamics;

import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import net.sf.openrocket.ServicesForTesting;
import net.sf.openrocket.aerodynamics.barrowman.FinSetCalc;
import net.sf.openrocket.plugin.PluginModule;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.TestRockets;
import net.sf.openrocket.util.Transformation;

public class FinSetCalcTest {
	protected final double EPSILON = 0.0001;
	
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

	private AerodynamicForces sumFins(TrapezoidFinSet fins, Rocket rocket)
	{
		FlightConfiguration config = rocket.getSelectedConfiguration();
		FlightConditions conditions = new FlightConditions(config);
		WarningSet warnings = new WarningSet();
		AerodynamicForces assemblyForces = new AerodynamicForces().zero();
		AerodynamicForces componentForces = new AerodynamicForces();

		FinSetCalc calcObj = new FinSetCalc( fins );
		
		// Need to sum forces for fins
		for (Integer i = 0; i < fins.getFinCount(); i++) {
			calcObj.calculateNonaxialForces(conditions,
											Transformation.rotate_x(Math.PI * i / fins.getFinCount()),
											componentForces, warnings);
			assemblyForces.merge(componentForces);
		}

		return assemblyForces;
	}
		
	@Test
	public void test3Fin() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		TrapezoidFinSet fins = (TrapezoidFinSet)rocket.getChild(0).getChild(1).getChild(0);
		
		// to make the fin properties explicit 
		assertEquals(" Estes Alpha III fins have wrong count:", 3, fins.getFinCount(), EPSILON);
		assertEquals(" Estes Alpha III fins have wrong root chord:", 0.05, fins.getRootChord(), EPSILON);
		assertEquals(" Estes Alpha III fins have wrong tip chord:", 0.03, fins.getTipChord(), EPSILON); 
		assertEquals(" Estes Alpha III fins have wrong sweep: ", 0.02, fins.getSweep(), EPSILON);
		assertEquals(" Estes Alpha III fins have wrong height: ", 0.05, fins.getHeight(), EPSILON);

		// get the forces for the three fins
		AerodynamicForces forces = sumFins(fins, rocket);

		double exp_cna_fins = 24.146933;
		double exp_cpx_fins = 0.0193484;
				
		assertEquals(" FinSetCalc produces bad CNa: ", exp_cna_fins, forces.getCNa(), EPSILON);
		assertEquals(" FinSetCalc produces bad C_p.x: ", exp_cpx_fins, forces.getCP().x, EPSILON);
		assertEquals(" FinSetCalc produces bad CN: ", 0.0, forces.getCN(), EPSILON);
		assertEquals(" FinSetCalc produces bad C_m: ", 0.0, forces.getCm(), EPSILON);
	}
	

	@Test
	public void test4Fin() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		TrapezoidFinSet fins = (TrapezoidFinSet)rocket.getChild(0).getChild(1).getChild(0);
		fins.setFinCount(4);
		
		// to make the fin properties explicit 
		assertEquals(" Estes Alpha III fins have wrong count:", 4, fins.getFinCount(), EPSILON);
		assertEquals(" Estes Alpha III fins have wrong root chord:", 0.05, fins.getRootChord(), EPSILON);
		assertEquals(" Estes Alpha III fins have wrong tip chord:", 0.03, fins.getTipChord(), EPSILON); 
		assertEquals(" Estes Alpha III fins have wrong sweep: ", 0.02, fins.getSweep(), EPSILON);
		assertEquals(" Estes Alpha III fins have wrong height: ", 0.05, fins.getHeight(), EPSILON);

		// get the forces for the four fins
		AerodynamicForces forces = sumFins(fins, rocket);
		
		double exp_cna_fins = 32.195911;
		double exp_cpx_fins = 0.0193484;
				
		assertEquals(" FinSetCalc produces bad CNa: ", exp_cna_fins, forces.getCNa(), EPSILON);
		assertEquals(" FinSetCalc produces bad C_p.x: ", exp_cpx_fins, forces.getCP().x, EPSILON);
		assertEquals(" FinSetCalc produces bad CN: ", 0.0, forces.getCN(), EPSILON);
		assertEquals(" FinSetCalc produces bad C_m: ", 0.0, forces.getCm(), EPSILON);
	}
}
