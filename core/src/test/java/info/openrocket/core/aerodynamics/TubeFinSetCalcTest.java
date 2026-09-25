package info.openrocket.core.aerodynamics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import info.openrocket.core.aerodynamics.barrowman.TubeFinSetCalc;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.TubeFinSet;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.TestRockets;
import info.openrocket.core.util.Transformation;

import org.junit.jupiter.api.Test;

/**
 * Tests for the tube-fin aerodynamic calculations.
 */
public class TubeFinSetCalcTest extends BaseTestCase {
	private static final double EPSILON = 0.000001;

	/** Calculate the tube-fin CP as a fraction of its chord. */
	private double relativeCP(TubeFinSet tubeFins, Rocket rocket, double mach) {
		FlightConfiguration config = rocket.getSelectedConfiguration();
		FlightConditions conditions = new FlightConditions(config);
		conditions.setMach(mach);
		AerodynamicForces forces = new AerodynamicForces();
		TubeFinSetCalc calc = new TubeFinSetCalc(tubeFins);
		calc.calculateNonaxialForces(conditions, Transformation.IDENTITY, forces, new WarningSet());
		return forces.getCP().getX() / tubeFins.getLength();
	}

	/**
	 * Verify that tube fins use their transonic interpolation polynomial instead
	 * of leaving its coefficients at their zero-initialized values.
	 */
	@Test
	public void testTransonicCPUsesInterpolationPolynomial() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		BodyTube body = (BodyTube) rocket.getChild(0).getChild(1);
		TubeFinSet tubeFins = new TubeFinSet();
		tubeFins.setOuterRadius(0.027);
		tubeFins.setThickness(0.002);
		tubeFins.setLength(0.05);
		body.addChild(tubeFins);

		// AR = 2*innerRadius/chord = 1, for which p(1) is about 0.32124.
		double relativeCP = relativeCP(tubeFins, rocket, 1.0);
		assertEquals(0.3212384847, relativeCP, EPSILON,
				"Tube-fin CP should follow the transonic interpolation polynomial");
		assertTrue(relativeCP > 0.25, "The initialized polynomial must move this tube-fin CP aft");
	}

	/** Verify that tube fins share the finite low-aspect-ratio CP fallback. */
	@Test
	public void testLowAspectRatioTubeFinCPIsFiniteAndMonotone() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		BodyTube body = (BodyTube) rocket.getChild(0).getChild(1);
		TubeFinSet tubeFins = new TubeFinSet();
		tubeFins.setOuterRadius(0.007);
		tubeFins.setThickness(0.002);
		tubeFins.setLength(0.05);
		body.addChild(tubeFins);

		// AR = 2*innerRadius/chord = 0.2, whose unbounded source equation has
		// a pole near Mach 2.69.
		double previous = Double.NEGATIVE_INFINITY;
		for (double mach = 0.5; mach <= 5.5; mach += 0.01) {
			double relativeCP = relativeCP(tubeFins, rocket, mach);
			assertTrue(Double.isFinite(relativeCP), "Tube-fin CP should be finite at mach " + mach);
			assertTrue(relativeCP >= previous - 1.0e-12,
					"Tube-fin CP should not move forward at mach " + mach);
			previous = relativeCP;
		}

		assertEquals(0.25, relativeCP(tubeFins, rocket, 2.0), EPSILON,
				"The invalid low-aspect-ratio source branch should not be evaluated");
		assertTrue(relativeCP(tubeFins, rocket, 5.2) > 0.25,
				"The tube-fin CP should join the source curve above its validity boundary");
	}
}
