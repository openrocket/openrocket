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
 * Tests for tube-fin aerodynamic calculations.
 */
public class TubeFinSetCalcTest extends BaseTestCase {
	private static final double EPSILON = 0.000001;

	/**
	 * Verify that tube fins use their geometry-dependent transonic interpolation
	 * polynomial instead of its zero-initialized coefficient array.
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

		// AR = 2 * innerRadius / chord = 1, for which p(1) is about 0.32124.
		FlightConfiguration configuration = rocket.getSelectedConfiguration();
		FlightConditions conditions = new FlightConditions(configuration);
		conditions.setMach(1.0);
		AerodynamicForces forces = new AerodynamicForces();
		TubeFinSetCalc calculator = new TubeFinSetCalc(tubeFins);
		calculator.calculateNonaxialForces(conditions, Transformation.IDENTITY, forces, new WarningSet());

		double relativeCP = forces.getCP().getX() / tubeFins.getLength();
		assertEquals(0.3212417255, relativeCP, EPSILON,
				"Tube-fin CP should follow the transonic interpolation polynomial");
		assertTrue(relativeCP > 0.25, "The initialized polynomial must move this tube-fin CP aft");
	}
}
