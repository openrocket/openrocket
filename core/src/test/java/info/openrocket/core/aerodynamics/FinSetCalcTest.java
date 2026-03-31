package info.openrocket.core.aerodynamics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
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

import java.util.Map;

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

		assertEquals(exp_cna_fins, forces.getCP().getWeight(), EPSILON, " FinSetCalc produces bad CNa: ");
		assertEquals(exp_cpx_fins, forces.getCP().getX(), EPSILON, " FinSetCalc produces bad C_p.x: ");
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

		assertEquals(exp_cna_fins, forces.getCP().getWeight(), EPSILON, " FinSetCalc produces bad CNa: ");
		assertEquals(exp_cpx_fins, forces.getCP().getX(), EPSILON, " FinSetCalc produces bad C_p.x: ");
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
		assertEquals(0.0, forces.getCP().getWeight(), EPSILON, "CNa should be zero for zero-area fin");
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
		assertEquals(0.0, forces.getCP().getWeight(), EPSILON, "CNa should be zero for canted zero-area fin");
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
		assertFalse(Double.isNaN(forces.getCP().getWeight()), "CNa should not be NaN for very small fin");
		assertFalse(Double.isNaN(forces.getCN()), "CN should not be NaN for very small fin");
		assertFalse(Double.isNaN(forces.getCm()), "Cm should not be NaN for very small fin");
		assertFalse(Double.isNaN(forces.getCroll()), "Croll should not be NaN for very small fin");
		assertFalse(Double.isNaN(forces.getCrollDamp()), "CrollDamp should not be NaN for very small fin");
		assertFalse(Double.isNaN(forces.getCrollForce()), "CrollForce should not be NaN for very small fin");
		assertFalse(Double.isNaN(forces.getCside()), "Cside should not be NaN for very small fin");
		assertFalse(Double.isNaN(forces.getCyaw()), "Cyaw should not be NaN for very small fin");

		// Verify CP location is valid
		assertFalse(Double.isNaN(forces.getCP().getX()), "CP x-coordinate should not be NaN for very small fin");
		assertFalse(Double.isNaN(forces.getCP().getY()), "CP y-coordinate should not be NaN for very small fin");
		assertFalse(Double.isNaN(forces.getCP().getZ()), "CP z-coordinate should not be NaN for very small fin");
	}

	/**
	 * Test that pressure and base drag are calculated separately for square cross-section fins.
	 * Square fins should have both pressure drag (stagnation) and base drag.
	 * The sum of the two should equal what the old combined method would have returned.
	 */
	@Test
	public void testSquareFinPressureAndBaseDragSeparation() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		TrapezoidFinSet fins = (TrapezoidFinSet) rocket.getChild(0).getChild(1).getChild(0);
		assertEquals(FinSet.CrossSection.SQUARE, fins.getCrossSection(), "Default cross-section should be SQUARE");

		FlightConfiguration config = rocket.getSelectedConfiguration();
		FlightConditions conditions = new FlightConditions(config);
		conditions.setMach(0.3);
		WarningSet warnings = new WarningSet();

		FinSetCalc calc = new FinSetCalc(fins);

		double stagnationCD = 1.0;
		double baseCD = 0.5;

		double pressureCD = calc.calculatePressureCD(conditions, stagnationCD, baseCD, warnings);
		double componentBaseCD = calc.calculateComponentBaseCD(conditions, baseCD, warnings);

		// Square fins: both pressure and base drag should be positive
		assertTrue(pressureCD > 0, "Square fin pressure CD should be positive");
		assertTrue(componentBaseCD > 0, "Square fin base CD should be positive");

		// Base drag should scale with baseCD (doubling baseCD should double component base drag)
		double componentBaseCD2 = calc.calculateComponentBaseCD(conditions, baseCD * 2, warnings);
		assertEquals(componentBaseCD * 2, componentBaseCD2, EPSILON,
				"Square fin base CD should scale linearly with baseCD");

		// Pressure CD should not change when baseCD changes (it depends on stagnationCD)
		double pressureCD2 = calc.calculatePressureCD(conditions, stagnationCD, baseCD * 2, warnings);
		assertEquals(pressureCD, pressureCD2, EPSILON,
				"Square fin pressure CD should not depend on baseCD");
	}

	/**
	 * Test that rounded cross-section fins get half the base drag.
	 */
	@Test
	public void testRoundedFinPressureAndBaseDragSeparation() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		TrapezoidFinSet fins = (TrapezoidFinSet) rocket.getChild(0).getChild(1).getChild(0);
		fins.setCrossSection(FinSet.CrossSection.ROUNDED);

		FlightConfiguration config = rocket.getSelectedConfiguration();
		FlightConditions conditions = new FlightConditions(config);
		conditions.setMach(0.3);
		WarningSet warnings = new WarningSet();

		FinSetCalc calc = new FinSetCalc(fins);

		double stagnationCD = 1.0;
		double baseCD = 0.5;

		double pressureCD = calc.calculatePressureCD(conditions, stagnationCD, baseCD, warnings);
		double componentBaseCD = calc.calculateComponentBaseCD(conditions, baseCD, warnings);

		assertTrue(pressureCD > 0, "Rounded fin pressure CD should be positive");
		assertTrue(componentBaseCD > 0, "Rounded fin base CD should be positive");

		// Rounded fins get half the base drag
		double refArea = conditions.getRefArea();
		double span = fins.getSpan();
		double thickness = fins.getThickness();
		double scaleFactor = span * thickness / refArea;
		double expectedBase = (baseCD / 2) * scaleFactor;

		assertEquals(expectedBase, componentBaseCD, EPSILON, "Rounded fin base CD should be half of baseCD * scaleFactor");
	}

	/**
	 * Test that airfoil cross-section fins have zero base drag.
	 */
	@Test
	public void testAirfoilFinZeroBaseDrag() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		TrapezoidFinSet fins = (TrapezoidFinSet) rocket.getChild(0).getChild(1).getChild(0);
		fins.setCrossSection(FinSet.CrossSection.AIRFOIL);

		FlightConfiguration config = rocket.getSelectedConfiguration();
		FlightConditions conditions = new FlightConditions(config);
		conditions.setMach(0.3);
		WarningSet warnings = new WarningSet();

		FinSetCalc calc = new FinSetCalc(fins);

		double componentBaseCD = calc.calculateComponentBaseCD(conditions, 0.5, warnings);
		assertEquals(0.0, componentBaseCD, EPSILON, "Airfoil fin should have zero base drag");
	}

	/**
	 * Test that zero-area fins return zero for both pressure and base drag.
	 */
	@Test
	public void testZeroAreaFinDragSeparation() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		TrapezoidFinSet fins = (TrapezoidFinSet) rocket.getChild(0).getChild(1).getChild(0);
		fins.setHeight(0.0);

		FlightConfiguration config = rocket.getSelectedConfiguration();
		FlightConditions conditions = new FlightConditions(config);
		WarningSet warnings = new WarningSet();

		FinSetCalc calc = new FinSetCalc(fins);

		assertEquals(0.0, calc.calculatePressureCD(conditions, 1.0, 0.5, warnings), EPSILON,
				"Zero-area fin pressure CD should be zero");
		assertEquals(0.0, calc.calculateComponentBaseCD(conditions, 0.5, warnings), EPSILON,
				"Zero-area fin base CD should be zero");
	}

	/**
	 * Integration test: verify that getForceAnalysis reports separate pressure and base drag for fins.
	 * The sum of pressureCD + baseCD for the fin should equal the total fin drag minus friction.
	 */
	@Test
	public void testForceAnalysisFinDragSeparation() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		FlightConfiguration config = rocket.getSelectedConfiguration();
		FlightConditions conditions = new FlightConditions(config);
		conditions.setMach(0.3);
		WarningSet warnings = new WarningSet();

		BarrowmanCalculator calculator = new BarrowmanCalculator();
		Map<RocketComponent, AerodynamicForces> forceMap = calculator.getForceAnalysis(config, conditions, warnings);

		// Find the fin set in the results
		AerodynamicForces finForces = null;
		for (Map.Entry<RocketComponent, AerodynamicForces> entry : forceMap.entrySet()) {
			if (entry.getKey() instanceof FinSet) {
				finForces = entry.getValue();
				break;
			}
		}

		assertNotNull(finForces, "Fin set should be present in force analysis");

		// Verify that both pressure and base CD are reported (not NaN)
		assertFalse(Double.isNaN(finForces.getPressureCD()), "Fin pressure CD should not be NaN");
		assertFalse(Double.isNaN(finForces.getBaseCD()), "Fin base CD should not be NaN");
		assertFalse(Double.isNaN(finForces.getFrictionCD()), "Fin friction CD should not be NaN");

		// For square fins, base drag should be positive
		assertTrue(finForces.getBaseCD() > 0, "Square fin base CD should be positive in force analysis");
		assertTrue(finForces.getPressureCD() > 0, "Square fin pressure CD should be positive in force analysis");

		// Total CD should equal sum of components
		double expectedCD = finForces.getPressureCD() + finForces.getBaseCD() + finForces.getFrictionCD();
		assertEquals(expectedCD, finForces.getCD(), EPSILON,
				"Total CD should equal pressureCD + baseCD + frictionCD");
	}

	/**
	 * Integration test: verify that airfoil fins report zero base drag in force analysis.
	 */
	@Test
	public void testForceAnalysisAirfoilFinZeroBaseDrag() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		TrapezoidFinSet fins = (TrapezoidFinSet) rocket.getChild(0).getChild(1).getChild(0);
		fins.setCrossSection(FinSet.CrossSection.AIRFOIL);

		FlightConfiguration config = rocket.getSelectedConfiguration();
		FlightConditions conditions = new FlightConditions(config);
		conditions.setMach(0.3);
		WarningSet warnings = new WarningSet();

		BarrowmanCalculator calculator = new BarrowmanCalculator();
		Map<RocketComponent, AerodynamicForces> forceMap = calculator.getForceAnalysis(config, conditions, warnings);

		AerodynamicForces finForces = null;
		for (Map.Entry<RocketComponent, AerodynamicForces> entry : forceMap.entrySet()) {
			if (entry.getKey() instanceof FinSet) {
				finForces = entry.getValue();
				break;
			}
		}

		assertNotNull(finForces, "Fin set should be present in force analysis");
		assertEquals(0.0, finForces.getBaseCD(), EPSILON, "Airfoil fin base CD should be zero in force analysis");
		assertTrue(finForces.getPressureCD() > 0, "Airfoil fin pressure CD should be positive");
	}
}
