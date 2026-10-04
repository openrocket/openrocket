package info.openrocket.core.document;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import info.openrocket.core.aerodynamics.lookup.CsvMachAoALookup;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.TestRockets;

public class SimulationLookupInvalidationTest extends BaseTestCase {

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	public void testAddingLookupInvalidatesResults(boolean drag) {
		Simulation simulation = loadedSimulation(new SimulationOptions());
		setLookup(simulation.getOptions(), drag, 1);
		assertEquals(Simulation.Status.OUTDATED, simulation.getStatus());
	}

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	public void testReplacingLookupInvalidatesResults(boolean drag) {
		SimulationOptions options = new SimulationOptions();
		setLookup(options, drag, 1);
		Simulation simulation = loadedSimulation(options);

		setLookup(options, drag, 2);

		assertEquals(Simulation.Status.OUTDATED, simulation.getStatus());
	}

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	public void testClearingLookupInvalidatesResults(boolean drag) {
		SimulationOptions options = new SimulationOptions();
		setLookup(options, drag, 1);
		Simulation simulation = loadedSimulation(options);

		if (drag) {
			options.clearDragLookup();
		} else {
			options.clearStabilityLookup();
		}

		assertEquals(Simulation.Status.OUTDATED, simulation.getStatus());
	}

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	public void testUnchangedLookupKeepsResultsCurrent(boolean drag) {
		SimulationOptions options = new SimulationOptions();
		setLookup(options, drag, 1);
		Simulation simulation = loadedSimulation(options);

		assertEquals(options, options.clone());
		assertEquals(Simulation.Status.LOADED, simulation.getStatus());
	}

	private static Simulation loadedSimulation(SimulationOptions options) {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		rocket.setSelectedConfiguration(TestRockets.TEST_FCID_0);
		Simulation simulation = new Simulation(null, rocket, Simulation.Status.LOADED, "Test", options,
				List.of(), null);
		assertEquals(Simulation.Status.LOADED, simulation.getStatus());
		return simulation;
	}

	private static void setLookup(SimulationOptions options, boolean drag, double value) {
		if (drag) {
			options.setDragLookup(null, CsvMachAoALookup.parse(List.of("Mach,Cd", "0," + value),
					List.of("cd"), ','));
		} else {
			options.setStabilityLookup(null, CsvMachAoALookup.parse(List.of("Mach,Cn,Cm,Cp", "0,1,1," + value),
					List.of("cn", "cm", "cp"), ','));
		}
	}
}
