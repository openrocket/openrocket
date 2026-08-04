package info.openrocket.swing.gui.figureelements;

import info.openrocket.core.aerodynamics.BarrowmanCalculator;
import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.MathUtil;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RocketInfoContextHelperTest extends BaseTestCase {

	@Test
	void bodyTubeOnlyUsesSameFiniteCpAndStabilityInBothViews() {
		int previousStabilityUnit = UnitGroup.UNITS_STABILITY.getDefaultUnitIndex();
		try {
			UnitGroup.UNITS_STABILITY.setDefaultUnit("cal");

			Rocket rocket = new Rocket();
			AxialStage stage = new AxialStage();
			rocket.addChild(stage);
			stage.addChild(new BodyTube(1.0, 0.125));

			FlightConfiguration configuration = new FlightConfiguration(rocket);
			configuration.setAllStages();
			FlightConditions conditions = new FlightConditions(configuration);
			RocketInfo twoDimensionalInfo = new RocketInfo(configuration);

			RocketInfoContextHelper.RocketPhysics physics = RocketInfoContextHelper.computePhysics(
					configuration, conditions, new WarningSet(), new BarrowmanCalculator(), true,
					twoDimensionalInfo);

			assertTrue(physics.cp().getWeight() <= MathUtil.EPSILON,
					"a body tube alone should not have a usable CP");

			RocketInfo threeDimensionalInfo = new RocketInfo(configuration);
			threeDimensionalInfo.set3DView(true);
			RocketInfoContextHelper.applyCgAndCp(threeDimensionalInfo, physics.cg(), physics.cp());

			Unit metres = UnitGroup.UNITS_LENGTH.getUnit("m");
			assertEquals("0 m", twoDimensionalInfo.getCp(metres));
			assertEquals(twoDimensionalInfo.getCp(metres), threeDimensionalInfo.getCp(metres));
			String stability = twoDimensionalInfo.getStabilityCombined();
			assertTrue(stability.startsWith("-2 cal"), "unexpected stability: " + stability);
			assertEquals(twoDimensionalInfo.getStabilityCombined(),
					threeDimensionalInfo.getStabilityCombined());
		} finally {
			UnitGroup.UNITS_STABILITY.setDefaultUnit(previousStabilityUnit);
		}
	}
}
