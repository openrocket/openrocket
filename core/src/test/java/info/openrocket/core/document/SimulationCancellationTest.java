package info.openrocket.core.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.simulation.SimulationStatus;
import info.openrocket.core.simulation.exception.SimulationCancelledException;
import info.openrocket.core.simulation.listeners.AbstractSimulationListener;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.TestRockets;

public class SimulationCancellationTest extends BaseTestCase {

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	public void testCancelledFlightRemainsOutdatedUntilSuccessfulRerun(boolean previouslySimulated) throws Exception {
		Simulation simulation = new Simulation(TestRockets.makeEstesAlphaIII());
		simulation.setFlightConfigurationId(TestRockets.TEST_FCID_0);
		simulation.getOptions().setISAAtmosphere(true);
		simulation.getOptions().setTimeStep(0.05);
		simulation.getOptions().setMaxSimulationTime(60);
		simulation.getOptions().setLaunchRodLength(1);
		if (previouslySimulated) {
			simulation.simulate();
			assertEquals(Simulation.Status.UPTODATE, simulation.getStatus());
		}

		assertThrows(SimulationCancelledException.class, () -> simulation.simulate(new AbstractSimulationListener() {
			@Override
			public void postStep(SimulationStatus status) throws SimulationCancelledException {
				if (status.getSimulationTime() >= 0.3) {
					throw new SimulationCancelledException("Cancelled during boost");
				}
			}
		}));

		assertNotNull(simulation.getSimulatedData());
		assertTrue(simulation.getSimulatedData().getFlightTime() < 1);
		assertNull(simulation.getSimulatedData().getBranch(0).getFirstEvent(FlightEvent.Type.GROUND_HIT));
		assertEquals(Simulation.Status.OUTDATED, simulation.getStatus());
		assertEquals(Simulation.Status.OUTDATED, simulation.clone().getStatus());

		simulation.simulate();

		assertEquals(Simulation.Status.UPTODATE, simulation.getStatus());
		assertNotNull(simulation.getSimulatedData().getBranch(0).getFirstEvent(FlightEvent.Type.GROUND_HIT));
	}
}
