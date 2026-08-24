package info.openrocket.core.aerodynamics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import info.openrocket.core.logging.Warning;
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
import info.openrocket.core.util.PolyInterpolator;
import info.openrocket.core.util.TestRockets;
import info.openrocket.core.util.Transformation;

import java.util.Map;

public class FinSetCalcTest {
	protected final double EPSILON = 0.0001;

	private static Injector injector;

	/** Exposes the single-fin CNa calculation for interpolation tests. */
	private static class TestableFinSetCalc extends FinSetCalc {
		TestableFinSetCalc(FinSet fins) {
			super(fins);
		}

		double calculateFinCNa(FlightConditions conditions) {
			return calculateFinCNa1(conditions);
		}
	}

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
		return sumFins(fins, rocket, Double.NaN);
	}

	private AerodynamicForces sumFins(TrapezoidFinSet fins, Rocket rocket, double mach) {
		FlightConfiguration config = rocket.getSelectedConfiguration();
		FlightConditions conditions = new FlightConditions(config);
		if (!Double.isNaN(mach)) {
			conditions.setMach(mach);
		}
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

	/**
	 * Verify that the transonic interpolation uses the derivative of the
	 * subsonic model at Mach 0.9, independent of the Mach being queried.
	 */
	@Test
	public void testTransonicCNaUsesSubsonicEndpointDerivative() {
		final double subsonicMach = 0.9;
		final double supersonicMach = 1.5;
		final double queryMach = 1.2;
		final double derivativeStep = 0.000001;

		Rocket rocket = TestRockets.makeEstesAlphaIII();
		TrapezoidFinSet fins = (TrapezoidFinSet) rocket.getChild(0).getChild(1).getChild(0);
		FlightConditions conditions = new FlightConditions(rocket.getSelectedConfiguration());
		conditions.setAOA(0);
		TestableFinSetCalc calculator = new TestableFinSetCalc(fins);

		conditions.setMach(subsonicMach);
		double subsonicValue = calculator.calculateFinCNa(conditions);
		conditions.setMach(subsonicMach - derivativeStep);
		double belowSubsonicValue = calculator.calculateFinCNa(conditions);
		double subsonicDerivative = (subsonicValue - belowSubsonicValue) / derivativeStep;

		conditions.setMach(supersonicMach);
		double supersonicValue = calculator.calculateFinCNa(conditions);
		double supersonicBetaCubed = Math.pow(supersonicMach * supersonicMach - 1, 1.5);
		double supersonicDerivative = -fins.getPlanformArea() / conditions.getRefArea()
				* 2 * supersonicMach / supersonicBetaCubed;

		PolyInterpolator interpolator = new PolyInterpolator(
				new double[] { subsonicMach, supersonicMach },
				new double[] { subsonicMach, supersonicMach },
				new double[] { subsonicMach });
		double expected = interpolator.interpolate(queryMach, subsonicValue, supersonicValue,
				subsonicDerivative, supersonicDerivative, 0);

		conditions.setMach(queryMach);
		double actual = calculator.calculateFinCNa(conditions);
		assertEquals(expected, actual, EPSILON,
				"Transonic CNa should use the derivative at the Mach 0.9 endpoint");
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

		double exp_cna_fins = 28.82053382;
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

		double exp_cna_fins = 38.42737843;
		double exp_cpx_fins = 0.0193484;

		assertEquals(exp_cna_fins, forces.getCP().getWeight(), EPSILON, " FinSetCalc produces bad CNa: ");
		assertEquals(exp_cpx_fins, forces.getCP().getX(), EPSILON, " FinSetCalc produces bad C_p.x: ");
		assertEquals(0.0, forces.getCN(), EPSILON, " FinSetCalc produces bad CN: ");
		assertEquals(0.0, forces.getCm(), EPSILON, " FinSetCalc produces bad C_m: ");
	}

	@Test
	public void testLowAspectRatioFinSupersonicCP() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		TrapezoidFinSet fins = (TrapezoidFinSet) rocket.getChild(0).getChild(1).getChild(0);

		// Aspect ratio 4*h/(cr+ct) = 0.2, which puts the pole of the empirical
		// supersonic CP formula (ar*beta == 0.5) at about mach 2.69
		fins.setHeight(0.005);
		fins.setRootChord(0.06);
		fins.setTipChord(0.04);
		fins.setSweep(0.0);

		double previous = Double.NaN;
		for (double mach = 0.5; mach <= 5.5; mach += 0.01) {
			AerodynamicForces forces = sumFins(fins, rocket, mach);
			double cpx = forces.getCP().getX();

			assertTrue(Double.isFinite(cpx), "CP x should stay finite at mach " + mach);

			// A 0.01 step in mach may not move the CP by anything like a percent of the
			// root chord.  Clamping only the result of the empirical formula would leave
			// a quarter-MAC step across its pole, which this catches.
			if (!Double.isNaN(previous)) {
				assertTrue(Math.abs(cpx - previous) < 0.01 * fins.getRootChord(),
						"CP x jumped from " + previous + " to " + cpx + " at mach " + mach);
				assertTrue(cpx >= previous - 1.0e-12,
						"CP should not move forward at mach " + mach);
			}
			previous = cpx;
		}

		double quarterChordCP = sumFins(fins, rocket, 0.5).getCP().getX();
		assertEquals(quarterChordCP, sumFins(fins, rocket, 4.0).getCP().getX(), EPSILON,
				"The low-aspect-ratio fallback should avoid the invalid source branch");
		assertTrue(sumFins(fins, rocket, 5.2).getCP().getX() > quarterChordCP,
				"The CP should join the source curve after ar*beta exceeds one");
	}

	@Test
	public void testOrdinaryFinSupersonicCPUsesSourceEquation() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		TrapezoidFinSet fins = (TrapezoidFinSet) rocket.getChild(0).getChild(1).getChild(0);

		// Use an unswept rectangular fin with AR = 2*h/chord = 2.5. Its CP x is
		// therefore the relative source-equation position multiplied by the chord.
		fins.setHeight(0.0625);
		fins.setRootChord(0.05);
		fins.setTipChord(0.05);
		fins.setSweep(0.0);

		double previous = Double.NEGATIVE_INFINITY;
		for (double mach = 2.0; mach <= 4.0; mach += 0.1) {
			double cpx = sumFins(fins, rocket, mach).getCP().getX();
			double arBeta = 2.5 * Math.sqrt(mach * mach - 1);
			double expectedRelativeCP = (arBeta - 0.67) / (2 * arBeta - 1);

			assertEquals(expectedRelativeCP * fins.getRootChord(), cpx, 1.0e-10,
					"Ordinary fins should retain the source equation at mach " + mach);
			assertTrue(cpx > previous, "CP should keep moving aft at mach " + mach);
			previous = cpx;
		}
	}

	/**
	 * Verify that the low-aspect-ratio transonic continuation moves smoothly
	 * between its endpoint positions instead of requiring an output clamp.
	 */
	@Test
	public void testIntermediateAspectRatioTransonicCPIsShapePreserving() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		TrapezoidFinSet fins = (TrapezoidFinSet) rocket.getChild(0).getChild(1).getChild(0);

		// AR = 4*h/(cr+ct) = 0.6. The original fifth-order interpolation develops
		// a forward excursion for this geometry.
		fins.setHeight(0.015);
		fins.setRootChord(0.05);
		fins.setTipChord(0.05);
		fins.setSweep(0.0);

		double subsonicCP = sumFins(fins, rocket, 0.5).getCP().getX();
		double transonicCP = sumFins(fins, rocket, 1.5).getCP().getX();
		double supersonicCP = sumFins(fins, rocket, 2.0).getCP().getX();

		assertTrue(transonicCP > subsonicCP,
				"The transonic CP should leave the quarter chord without a flat output clamp");
		assertTrue(transonicCP < supersonicCP,
				"The transonic CP should remain between its endpoint positions");

		double previous = subsonicCP;
		for (double mach = 0.51; mach < 2.0; mach += 0.01) {
			double cpx = sumFins(fins, rocket, mach).getCP().getX();
			assertTrue(cpx >= previous - 1.0e-12,
					"The transonic CP moved forward at mach " + mach);
			previous = cpx;
		}

		// The shape-preserving curve matches both the value and first derivative of
		// the supersonic curve at Mach 2.
		double step = 1.0e-4;
		double leftSlope = (supersonicCP - sumFins(fins, rocket, 2.0 - step).getCP().getX()) / step;
		double rightSlope = (sumFins(fins, rocket, 2.0 + step).getCP().getX() - supersonicCP) / step;
		assertEquals(leftSlope, rightSlope, 1.0e-5,
				"The CP slope should be continuous at Mach 2");
	}

	/** Verify continuity through the fallback, bridge, and interpolation boundaries. */
	@Test
	public void testTransonicCPIsContinuousAcrossAspectRatios() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		TrapezoidFinSet fins = (TrapezoidFinSet) rocket.getChild(0).getChild(1).getChild(0);
		fins.setRootChord(0.05);
		fins.setTipChord(0.05);
		fins.setSweep(0.0);

		double previous = Double.NaN;
		for (double aspectRatio = 0.1; aspectRatio <= 1.0; aspectRatio += 0.002) {
			// For an unswept rectangular fin, AR = 2*height/chord.
			fins.setHeight(aspectRatio * fins.getRootChord() / 2);
			double cpx = sumFins(fins, rocket, 1.5).getCP().getX();

			assertTrue(Double.isFinite(cpx), "CP should be finite at AR " + aspectRatio);
			if (!Double.isNaN(previous)) {
				assertTrue(cpx >= previous - 1.0e-12,
						"CP should vary monotonically with AR " + aspectRatio);
				assertTrue(cpx - previous < 0.01 * fins.getRootChord(),
						"CP should not jump at AR " + aspectRatio);
			}
			previous = cpx;
		}
	}

	@Test
	public void testZeroAreaFin() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		TrapezoidFinSet fins = (TrapezoidFinSet) rocket.getChild(0).getChild(1).getChild(0);

		// Set fin dimensions to zero
		fins.setHeight(0.0);

		assertEquals(0.0, fins.getPlanformArea(), EPSILON, "Zero-area fin should have zero planform area");

		// The user should be told about it
		WarningSet warnings = new WarningSet();
		new FinSetCalc(fins).calculateNonaxialForces(
				new FlightConditions(rocket.getSelectedConfiguration()),
				Transformation.IDENTITY, new AerodynamicForces(), warnings);
		assertTrue(warnings.stream().anyMatch(w -> w.getMessageDescription()
						.equals(Warning.ZERO_AREA_FIN.getMessageDescription())),
				"Zero-area fin should raise a warning");

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
