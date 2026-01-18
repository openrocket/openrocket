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

public class NaturalFrequencyTest extends BaseTestCase {

	@Test
	public void testNaturalFrequencyTypeIsDefined() {
		assertSame(UnitGroup.UNITS_ROLL, FlightDataType.TYPE_NATURAL_FREQUENCY.getUnitGroup());
		assertEquals("\u03c9n", FlightDataType.TYPE_NATURAL_FREQUENCY.getSymbol());

		List<FlightDataType> allTypes = Arrays.asList(FlightDataType.ALL_TYPES);
		assertTrue(allTypes.contains(FlightDataType.TYPE_NATURAL_FREQUENCY));

		// Ensure we have a non-empty translated name for UI display.
		assertFalse(FlightDataType.TYPE_NATURAL_FREQUENCY.getName().isBlank());
	}

	@ParameterizedTest
	@EnumSource(SimulationStepperMethod.class)
	public void testNaturalFrequencyIsStoredInSimulationData(SimulationStepperMethod stepperMethod)
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

		List<Double> omegaN = branch.get(FlightDataType.TYPE_NATURAL_FREQUENCY);
		List<Double> time = branch.get(FlightDataType.TYPE_TIME);

		assertNotNull(omegaN);
		assertNotNull(time);
		assertEquals(time.size(), omegaN.size());
		assertFalse(omegaN.isEmpty());

		boolean foundFinite = false;
		for (double value : omegaN) {
			if (Double.isNaN(value)) {
				continue;
			}
			assertFalse(Double.isInfinite(value), "\u03c9n should never be infinite");
			foundFinite = true;
			break;
		}

		assertTrue(foundFinite, "Expected at least one finite \u03c9n value during flight");
	}
}

