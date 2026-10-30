package info.openrocket.core.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.TestRockets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class RollAngleTest extends BaseTestCase {

	@Test
	public void testRollAngleTypeIsDefined() {
		assertSame(UnitGroup.UNITS_ANGLE, FlightDataType.TYPE_ORIENTATION_ROLL.getUnitGroup());
		assertTrue(Arrays.asList(FlightDataType.ALL_TYPES).contains(FlightDataType.TYPE_ORIENTATION_ROLL));
		assertFalse(FlightDataType.TYPE_ORIENTATION_ROLL.getName().isBlank());
	}

	/**
	 * Canted fins spin the rocket up during the boost. While it climbs almost vertically, the
	 * recorded roll angle must advance by the recorded roll rate, in the same direction.
	 */
	@ParameterizedTest
	@EnumSource(SimulationStepperMethod.class)
	public void testRollAngleAdvancesWithTheRollRate(SimulationStepperMethod stepperMethod)
			throws SimulationException {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		for (RocketComponent component : rocket) {
			if (component instanceof FinSet fins) {
				fins.setCantAngle(Math.toRadians(3.0));
			}
		}
		Simulation simulation = new Simulation(rocket);
		simulation.setFlightConfigurationId(TestRockets.TEST_FCID_0);
		simulation.getOptions().setISAAtmosphere(true);
		simulation.getOptions().setTimeStep(0.01);
		simulation.getOptions().setWindSpeedAverage(0.0);
		simulation.getOptions().setWindTurbulenceIntensity(0.0);
		simulation.getOptions().setRandomSeed(0xC0FFEE);
		simulation.getOptions().setSimulationStepperMethodChoice(stepperMethod);

		simulation.simulate();

		FlightDataBranch branch = simulation.getSimulatedData().getBranch(0);
		List<Double> time = branch.get(FlightDataType.TYPE_TIME);
		List<Double> roll = branch.get(FlightDataType.TYPE_ORIENTATION_ROLL);
		List<Double> rollRate = branch.get(FlightDataType.TYPE_ROLL_RATE);
		List<Double> theta = branch.get(FlightDataType.TYPE_ORIENTATION_THETA);
		assertEquals(time.size(), roll.size());

		double angleChange = 0.0;
		double integratedRate = 0.0;
		for (int i = 1; i < time.size(); i++) {
			if (time.get(i) > 1.5 || theta.get(i) < Math.toRadians(80.0)
					|| Double.isNaN(roll.get(i - 1)) || Double.isNaN(rollRate.get(i - 1))) {
				continue;
			}
			double step = roll.get(i) - roll.get(i - 1);
			// The angle is recorded within +/- pi; take each step the short way round.
			angleChange += step - 2.0 * Math.PI * Math.rint(step / (2.0 * Math.PI));
			integratedRate += 0.5 * (rollRate.get(i) + rollRate.get(i - 1)) * (time.get(i) - time.get(i - 1));
		}

		assertTrue(Math.abs(integratedRate) > 2.0 * Math.PI,
				"The canted fins should spin the rocket at least a full turn, but it turned " + integratedRate);
		assertEquals(integratedRate, angleChange, 0.02 * Math.abs(integratedRate));
	}
}
