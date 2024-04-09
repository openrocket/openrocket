package info.openrocket.core.aerodynamics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import info.openrocket.core.logging.WarningSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import info.openrocket.core.ServicesForTesting;
import info.openrocket.core.aerodynamics.barrowman.RailButtonCalc;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.RailButton;
import info.openrocket.core.rocketcomponent.LaunchLug;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.TestRockets;

public class RailButtonCalcTest {
	protected final double EPSILON = 0.0001;

	private static Injector injector;

	@BeforeAll
	public static void setup() {
		Module applicationModule = new ServicesForTesting();
		Module pluginModule = new PluginModule();

		injector = Guice.createInjector(applicationModule, pluginModule);
		Application.setInjector(injector);
	}

	@Test
	public void testRailButtons() {

		Rocket rocket = TestRockets.makeEstesAlphaIII();
		FlightConfiguration config = rocket.getSelectedConfiguration();

		// Get the body tube...
		BodyTube tube = (BodyTube) rocket.getChild(0).getChild(1);

		// Replace the launch lug with a (single) railbutton
		LaunchLug lug = (LaunchLug) tube.getChild(1);
		rocket.removeChild(lug);

		RailButton button = new RailButton();
		tube.addChild(button);

		// Button parameters from Binder Design standard 1010
		button.setOuterDiameter(0.011);
		button.setInnerDiameter(0.006);

		button.setBaseHeight(0.002);
		button.setFlangeHeight(0.002);
		button.setTotalHeight(0.008);

		button.setAxialMethod(AxialMethod.ABSOLUTE);
		button.setAxialOffset(1.0);

		// Set up flight conditions
		FlightConditions conditions = new FlightConditions(config);
		conditions.setMach(1.0);

		BarrowmanCalculator barrowmanObj = new BarrowmanCalculator();
		RailButtonCalc calcObj = new RailButtonCalc(button);

		// Calculate effective CD for rail button
		// Boundary layer height
		double rex = calcObj.calculateReynoldsNumber(1.0, conditions); // Reynolds number of button location
		double del = 0.37 * 1.0 / Math.pow(rex, 0.2); // Boundary layer height

		// Interpolate velocity at midpoint of railbutton
		double mach = MathUtil.map(0.008 / 2.0, 0, del, 0, 1.0);

		// Interpolate to get CD
		double cd = MathUtil.map(mach, 0.2, 0.3, 1.22, 1.25);

		// Reference area of rail button
		final double outerArea = button.getTotalHeight() * button.getOuterDiameter();
		final double notchArea = (button.getOuterDiameter() - button.getInnerDiameter()) * button.getInnerHeight();
		final double refArea = outerArea - notchArea;

		// Get "effective" CD
		double calccd = cd * MathUtil.pow2(mach) * barrowmanObj.calculateStagnationCD(conditions.getMach()) * refArea
				/ conditions.getRefArea();

		// Now compare with value from RailButtonCalc
		WarningSet warnings = new WarningSet();
		AerodynamicForces assemblyForces = new AerodynamicForces().zero();
		AerodynamicForces componentForces = new AerodynamicForces();

		double testcd = calcObj.calculatePressureCD(conditions,
				barrowmanObj.calculateStagnationCD(conditions.getMach()), 0, warnings);

		assertEquals(calccd, testcd, EPSILON, "Calculated rail button CD incorrect");
	}
}
