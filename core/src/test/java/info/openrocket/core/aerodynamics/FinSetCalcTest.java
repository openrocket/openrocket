package info.openrocket.core.aerodynamics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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

		assertEquals(exp_cna_fins, forces.getCP().weight, EPSILON, " FinSetCalc produces bad CNa: ");
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

		assertEquals(exp_cna_fins, forces.getCP().weight, EPSILON, " FinSetCalc produces bad CNa: ");
		assertEquals(exp_cpx_fins, forces.getCP().x, EPSILON, " FinSetCalc produces bad C_p.x: ");
		assertEquals(0.0, forces.getCN(), EPSILON, " FinSetCalc produces bad CN: ");
		assertEquals(0.0, forces.getCm(), EPSILON, " FinSetCalc produces bad C_m: ");
	}

	@Test
	public void testZeroAreaFin() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		TrapezoidFinSet fins = (TrapezoidFinSet) rocket.getChild(0).getChild(1).getChild(0);

		// Set fin dimensions to zero
		fins.setHeight(0.0);

		assertEquals(0.0, fins.getPlanformArea(), EPSILON, "Zero-area fin should have zero planform area");

		// Calculate forces
		AerodynamicForces forces = sumFins(fins, rocket);

		// Verify all force components are zero and not NaN
		assertEquals(0.0, forces.getCP().weight, EPSILON, "CNa should be zero for zero-area fin");
		assertEquals(0.0, forces.getCN(), EPSILON, "CN should be zero for zero-area fin");
		assertEquals(0.0, forces.getCm(), EPSILON, "Cm should be zero for zero-area fin");
		assertEquals(0.0, forces.getCroll(), EPSILON, "Croll should be zero for zero-area fin");
		assertEquals(0.0, forces.getCrollDamp(), EPSILON, "CrollDamp should be zero for zero-area fin");
		assertEquals(0.0, forces.getCrollForce(), EPSILON, "CrollForce should be zero for zero-area fin");
		assertEquals(0.0, forces.getCside(), EPSILON, "Cside should be zero for zero-area fin");
		assertEquals(0.0, forces.getCyaw(), EPSILON, "Cyaw should be zero for zero-area fin");

		// Check the same for a canted fin
		fins.setCantAngle(0.1);

		// Calculate forces
		forces = sumFins(fins, rocket);

		// Verify all force components are zero and not NaN
		assertEquals(0.0, forces.getCP().weight, EPSILON, "CNa should be zero for canted zero-area fin");
		assertEquals(0.0, forces.getCN(), EPSILON, "CN should be zero for canted zero-area fin");
		assertEquals(0.0, forces.getCm(), EPSILON, "Cm should be zero for canted zero-area fin");
		assertEquals(0.0, forces.getCroll(), EPSILON, "Croll should be zero for canted zero-area fin");
		assertEquals(0.0, forces.getCrollDamp(), EPSILON, "CrollDamp should be zero for canted zero-area fin");
		assertEquals(0.0, forces.getCrollForce(), EPSILON, "CrollForce should be zero for canted zero-area fin");
		assertEquals(0.0, forces.getCside(), EPSILON, "Cside should be zero for canted zero-area fin");
		assertEquals(0.0, forces.getCyaw(), EPSILON, "Cyaw should be zero for canted zero-area fin");
	}

	@Test
	public void testVerySmallArea() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		TrapezoidFinSet fins = (TrapezoidFinSet) rocket.getChild(0).getChild(1).getChild(0);

		// Set fin dimensions to very small values (less than 0.0025m)
		double tinyDimension = 0.0001; // 0.1 mm
		fins.setHeight(tinyDimension);

		// Calculate forces
		AerodynamicForces forces = sumFins(fins, rocket);

		// Verify results are not NaN
		assertFalse(Double.isNaN(forces.getCP().weight), "CNa should not be NaN for very small fin");
		assertFalse(Double.isNaN(forces.getCN()), "CN should not be NaN for very small fin");
		assertFalse(Double.isNaN(forces.getCm()), "Cm should not be NaN for very small fin");
		assertFalse(Double.isNaN(forces.getCroll()), "Croll should not be NaN for very small fin");
		assertFalse(Double.isNaN(forces.getCrollDamp()), "CrollDamp should not be NaN for very small fin");
		assertFalse(Double.isNaN(forces.getCrollForce()), "CrollForce should not be NaN for very small fin");
		assertFalse(Double.isNaN(forces.getCside()), "Cside should not be NaN for very small fin");
		assertFalse(Double.isNaN(forces.getCyaw()), "Cyaw should not be NaN for very small fin");

		// Verify CP location is valid
		assertFalse(Double.isNaN(forces.getCP().x), "CP x-coordinate should not be NaN for very small fin");
		assertFalse(Double.isNaN(forces.getCP().y), "CP y-coordinate should not be NaN for very small fin");
		assertFalse(Double.isNaN(forces.getCP().z), "CP z-coordinate should not be NaN for very small fin");
	}
}
