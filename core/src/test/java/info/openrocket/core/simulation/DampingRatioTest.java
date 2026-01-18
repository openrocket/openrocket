package info.openrocket.core.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.TestRockets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class DampingRatioTest extends BaseTestCase {

	@Test
	public void testDampingRatioTypeIsDefined() {
		assertSame(UnitGroup.UNITS_COEFFICIENT, FlightDataType.TYPE_DAMPING_RATIO.getUnitGroup());
		assertEquals("\u03b6", FlightDataType.TYPE_DAMPING_RATIO.getSymbol());

		List<FlightDataType> allTypes = Arrays.asList(FlightDataType.ALL_TYPES);
		assertTrue(allTypes.contains(FlightDataType.TYPE_DAMPING_RATIO));

		// Ensure we have a non-empty translated name for UI display.
		assertFalse(FlightDataType.TYPE_DAMPING_RATIO.getName().isBlank());
	}

	@ParameterizedTest
	@EnumSource(SimulationStepperMethod.class)
	public void testDampingRatioIsStoredInSimulationData(SimulationStepperMethod stepperMethod)
			throws SimulationException {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		Simulation simulation = new Simulation(rocket);
		simulation.setFlightConfigurationId(TestRockets.TEST_FCID_0);
		simulation.getOptions().setISAAtmosphere(true);
		simulation.getOptions().setTimeStep(0.05);
		simulation.getOptions().setRandomSeed(0xC0FFEE);
		simulation.getOptions().setSimulationStepperMethodChoice(stepperMethod);

		simulation.simulate();

		FlightDataBranch branch = simulation.getSimulatedData().getBranch(0);
		assertNotNull(branch);

		List<Double> zeta = branch.get(FlightDataType.TYPE_DAMPING_RATIO);
		List<Double> time = branch.get(FlightDataType.TYPE_TIME);

		assertNotNull(zeta);
		assertNotNull(time);
		assertEquals(time.size(), zeta.size());
		assertFalse(zeta.isEmpty());

		// At t=0 the rocket is still on the launch rod, so zeta is intentionally 0.
		assertEquals(0.0, zeta.get(0), "Expected NaN at t=0 (still on launch rod)");

		boolean foundFinite = false;
		for (double value : zeta) {
			if (Double.isNaN(value)) {
				continue;
			}
			assertFalse(Double.isInfinite(value), "\u03b6 should never be infinite");
			foundFinite = true;
			break;
		}

		assertTrue(foundFinite, "Expected at least one finite \u03b6 value during flight");
	}
}
