package info.openrocket.core.aerodynamics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import info.openrocket.core.logging.WarningSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import info.openrocket.core.ServicesForTesting;
import info.openrocket.core.aerodynamics.barrowman.FinSetCalc;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.TestRockets;
import info.openrocket.core.util.Transformation;

public class FinSetCalcTest {
	protected final double EPSILON = 0.0001;

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

	private AerodynamicForces sumFins(TrapezoidFinSet fins, Rocket rocket) {
		FlightConfiguration config = rocket.getSelectedConfiguration();
		FlightConditions conditions = new FlightConditions(config);
		WarningSet warnings = new WarningSet();
		AerodynamicForces assemblyForces = new AerodynamicForces().zero();
		AerodynamicForces componentForces = new AerodynamicForces();

		FinSetCalc calcObj = new FinSetCalc(fins);

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
		TrapezoidFinSet fins = (TrapezoidFinSet) rocket.getChild(0).getChild(1).getChild(0);

		// to make the fin properties explicit
		assertEquals(3, fins.getFinCount(), EPSILON, " Estes Alpha III fins have wrong count:");
		assertEquals(0.05, fins.getRootChord(), EPSILON, " Estes Alpha III fins have wrong root chord:");
		assertEquals(0.03, fins.getTipChord(), EPSILON, " Estes Alpha III fins have wrong tip chord:");
		assertEquals(0.02, fins.getSweep(), EPSILON, " Estes Alpha III fins have wrong sweep: ");
		assertEquals(0.05, fins.getHeight(), EPSILON, " Estes Alpha III fins have wrong height: ");

		// get the forces for the three fins
		AerodynamicForces forces = sumFins(fins, rocket);

		double exp_cna_fins = 24.146933;
		double exp_cpx_fins = 0.0193484;

		assertEquals(exp_cna_fins, forces.getCNa(), EPSILON, " FinSetCalc produces bad CNa: ");
		assertEquals(exp_cpx_fins, forces.getCP().x, EPSILON, " FinSetCalc produces bad C_p.x: ");
		assertEquals(0.0, forces.getCN(), EPSILON, " FinSetCalc produces bad CN: ");
		assertEquals(0.0, forces.getCm(), EPSILON, " FinSetCalc produces bad C_m: ");
	}

	@Test
	public void test4Fin() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		TrapezoidFinSet fins = (TrapezoidFinSet) rocket.getChild(0).getChild(1).getChild(0);
		fins.setFinCount(4);

		// to make the fin properties explicit
		assertEquals(4, fins.getFinCount(), EPSILON, " Estes Alpha III fins have wrong count:");
		assertEquals(0.05, fins.getRootChord(), EPSILON, " Estes Alpha III fins have wrong root chord:");
		assertEquals(0.03, fins.getTipChord(), EPSILON, " Estes Alpha III fins have wrong tip chord:");
		assertEquals(0.02, fins.getSweep(), EPSILON, " Estes Alpha III fins have wrong sweep: ");
		assertEquals(0.05, fins.getHeight(), EPSILON, " Estes Alpha III fins have wrong height: ");

		// get the forces for the four fins
		AerodynamicForces forces = sumFins(fins, rocket);

		double exp_cna_fins = 32.195911;
		double exp_cpx_fins = 0.0193484;

		assertEquals(exp_cna_fins, forces.getCNa(), EPSILON, " FinSetCalc produces bad CNa: ");
		assertEquals(exp_cpx_fins, forces.getCP().x, EPSILON, " FinSetCalc produces bad C_p.x: ");
		assertEquals(0.0, forces.getCN(), EPSILON, " FinSetCalc produces bad CN: ");
		assertEquals(0.0, forces.getCm(), EPSILON, " FinSetCalc produces bad C_m: ");
	}
}
