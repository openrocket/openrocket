package info.openrocket.core.aerodynamics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import info.openrocket.core.logging.WarningSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import info.openrocket.core.ServicesForTesting;
import info.openrocket.core.aerodynamics.barrowman.SymmetricComponentCalc;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.TestRockets;
import info.openrocket.core.util.Transformation;

public class SymmetricComponentCalcTest {
	protected final double EPSILON = MathUtil.EPSILON * 1000;

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

	@Test
	public void testConicalNoseParams() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		NoseCone nose = (NoseCone) rocket.getChild(0).getChild(0);
		nose.setShapeType(Transition.Shape.CONICAL);

		// to illustrate the NoseCone properties to the reader:
		assertEquals(0.07, nose.getLength(), EPSILON, " Estes Alpha III nose cone has incorrect length:");
		assertEquals(0.012, nose.getAftRadius(), EPSILON, " Estes Alpha III nosecone has wrong (base) radius:");
		assertEquals(Transition.Shape.CONICAL, nose.getShapeType(), " Estes Alpha III nosecone has wrong type:");

		FlightConfiguration config = rocket.getSelectedConfiguration();
		FlightConditions conditions = new FlightConditions(config);
		Transformation transform = Transformation.IDENTITY;
		WarningSet warnings = new WarningSet();
		AerodynamicForces forces = new AerodynamicForces();
		SymmetricComponentCalc calcObj = new SymmetricComponentCalc(nose);

		conditions.setAOA(0.0);
		// vvv TEST MEH! vvv
		calcObj.calculateNonaxialForces(conditions, transform, forces, warnings);
		// ^^^

		double cna_nose = 2;
		double cpx_nose = 2.0 / 3.0 * nose.getLength();

		assertEquals(cna_nose, forces.getCNa(), EPSILON, " SymmetricComponentCalc produces bad CNa: ");
		assertEquals(cpx_nose, forces.getCP().x, EPSILON, " SymmetricComponentCalc produces bad C_p.x: ");
		assertEquals(0.0, forces.getCN(), EPSILON, " SymmetricComponentCalc produces bad CN: ");
		assertEquals(0.0, forces.getCm(), EPSILON, " SymmetricComponentCalc produces bad C_m: ");
	}

	@Test
	public void testOgiveNoseParams() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		NoseCone nose = (NoseCone) rocket.getChild(0).getChild(0);

		// to illustrate the NoseCone properties to the reader:
		assertEquals(0.07, nose.getLength(), EPSILON, " Estes Alpha III nose cone has incorrect length:");
		assertEquals(0.012, nose.getAftRadius(), EPSILON, " Estes Alpha III nosecone has wrong (base) radius:");
		assertEquals(Transition.Shape.OGIVE, nose.getShapeType(), " Estes Alpha III nosecone has wrong type:");

		FlightConfiguration config = rocket.getSelectedConfiguration();
		FlightConditions conditions = new FlightConditions(config);
		Transformation transform = Transformation.IDENTITY;
		WarningSet warnings = new WarningSet();
		AerodynamicForces forces = new AerodynamicForces();
		SymmetricComponentCalc calcObj = new SymmetricComponentCalc(nose);

		conditions.setAOA(0.0);
		// vvv TEST vvv
		calcObj.calculateNonaxialForces(conditions, transform, forces, warnings);
		// ^^^ ^^^

		double l_nose = nose.getLength();
		double cna_nose = 2;
		double cpx_nose = 0.46216 * l_nose;
		assertEquals(cna_nose, forces.getCNa(), EPSILON, " SymmetricComponentCalc produces bad CNa:  ");
		assertEquals(cpx_nose, forces.getCP().x, EPSILON, " SymmetricComponentCalc produces bad C_p.x:");
		assertEquals(0.0, forces.getCN(), EPSILON, " SymmetricComponentCalc produces bad CN:   ");
		assertEquals(0.0, forces.getCm(), EPSILON, " SymmetricComponentCalc produces bad C_m:  ");
	}

	@Test
	public void testEllipseNoseconeDrag() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		NoseCone nose = (NoseCone) rocket.getChild(0).getChild(0);
		// use an ellipsoidal nose cone with fineness ratio 5
		nose.setShapeType(Transition.Shape.ELLIPSOID);
		nose.setLength(nose.getAftRadius() * 5.0);
		SymmetricComponentCalc calcObj = new SymmetricComponentCalc(nose);

		FlightConfiguration config = rocket.getSelectedConfiguration();
		FlightConditions conditions = new FlightConditions(config);
		conditions.setAOA(0.0);

		WarningSet warnings = new WarningSet();

		double frontalArea = Math.PI * nose.getAftRadius() * nose.getAftRadius();
		// vvv TEST vvv
		// these values from a reimplementation of the pressure cd calculation in python
		// values at M = 0, 0.05, ... , 1.15
		double cd[] = {
				8.000392024269301e-07, 2.422001414621988e-06, 2.0098855921838474e-05,
				8.295843903984836e-05, 0.000230425812213129, 0.0005104254351619708,
				0.0009783566607446353, 0.0016963974152150677, 0.0027329880483111142,
				0.004162427611715722, 0.006064546184601524, 0.008524431611715306,
				0.011632196831399358, 0.01548277847481293, 0.020175760185344116,
				0.02581521589534269, 0.032509569500239276, 0.04037146820686186,
				0.04951766743137877, 0.06006892556095394, 0.07214990722137526,
				0.08588909394291767, 0.10141870131021756, 0.11887460183385967 };

		for (int i = 0; i < cd.length; i++) {
			double m = i / 20.0;
			String buf = "SymmetricComponentCalc produces bad Cd at index " + i + "(m=" + m + ")";
			conditions.setMach(m);
			double testcd = calcObj.calculatePressureCD(conditions, 0.0, 0.0, warnings) *
					conditions.getRefArea() / frontalArea;
			assertEquals(cd[(int) Math.round(m * 20)], testcd, EPSILON, buf);
		}
	}

}
