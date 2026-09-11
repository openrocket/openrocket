package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.core.aerodynamics.AerodynamicCalculator;
import info.openrocket.core.aerodynamics.BarrowmanCalculator;
import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.TestRockets;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The 3D centre-of-pressure caret used to be computed inside {@link CaretsPass} from default
 * flight conditions. The Component Analysis window can override Mach, angle of attack and roll
 * rate, and those overrides live in the design view — so the caret ignored them and disagreed
 * with both the 2D figure and the numbers in the overlay.
 *
 * <p>This test does not touch the pass (it needs a GL context). It pins the premise the fix
 * rests on: that the centre of pressure genuinely moves with Mach, so a view that recomputes
 * it from default conditions cannot be showing the right thing.</p>
 */
class CaretsPassTest extends BaseTestCase {

	@Test
	void centreOfPressureMovesWithMach() {
		Rocket rocket = buildRocket();
		FlightConfiguration config = rocket.getSelectedConfiguration();
		AerodynamicCalculator calculator = new BarrowmanCalculator();

		CoordinateIF subsonic = cpAtMach(calculator, config, 0.3);
		CoordinateIF transonic = cpAtMach(calculator, config, 0.9);

		assertTrue(subsonic.getWeight() > 0, "the test rocket should have a usable CP");
		assertTrue(transonic.getWeight() > 0, "the test rocket should have a usable CP");
		assertNotEquals(subsonic.getX(), transonic.getX(), 1.0e-6,
				"CP must depend on Mach, otherwise the caret could safely ignore it");
	}

	private static CoordinateIF cpAtMach(AerodynamicCalculator calculator, FlightConfiguration config, double mach) {
		FlightConditions conditions = new FlightConditions(config);
		conditions.setMach(mach);
		conditions.setAOA(0);
		conditions.setRollRate(0);
		return calculator.getCP(config, conditions, new WarningSet());
	}

	private static Rocket buildRocket() {
		Rocket rocket = TestRockets.makeBigBlue();
		rocket.getSelectedConfiguration().setAllStages();
		return rocket;
	}
}
