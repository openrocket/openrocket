package info.openrocket.core.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.GeodeticComputationStrategy;
import info.openrocket.core.util.TestRockets;

public class SimulationLaunchSettingsTest extends BaseTestCase {

	@Test
	public void testAtmosphereModeInvalidatesResults() throws Exception {
		assertInvalidatesResults(options -> options.setISAAtmosphere(false));
	}

	@Test
	public void testLaunchIntoWindInvalidatesResults() throws Exception {
		assertInvalidatesResults(options -> options.setLaunchIntoWind(false));
	}

	@Test
	public void testGeodeticStrategyInvalidatesResults() throws Exception {
		assertInvalidatesResults(options -> options.setGeodeticComputation(GeodeticComputationStrategy.FLAT));
	}

	private static void assertInvalidatesResults(Consumer<SimulationOptions> change) throws Exception {
		Simulation simulation = new Simulation(TestRockets.makeEstesAlphaIII());
		simulation.setFlightConfigurationId(TestRockets.TEST_FCID_0);
		SimulationOptions options = simulation.getOptions();
		options.setTimeStep(0.05);
		options.setMaxSimulationTime(60);
		options.setLaunchRodLength(1);
		options.setISAAtmosphere(true);
		options.setLaunchPressure(80000);
		options.setLaunchTemperature(310);
		options.setLaunchIntoWind(true);
		options.setLaunchRodAngle(0.1);
		options.setLaunchRodDirection(0.2);
		options.getAverageWindModel().setDirection(0.7);
		options.setGeodeticComputation(GeodeticComputationStrategy.SPHERICAL);
		options.setRandomSeed(123);
		options.setRandomSeedFixed(true);
		simulation.simulate();
		assertEquals(Simulation.Status.UPTODATE, simulation.getStatus());
		assertEquals(options, options.clone());

		change.accept(options);

		assertNotEquals(options, simulation.getSimulatedConditions());
		assertEquals(Simulation.Status.OUTDATED, simulation.getStatus());
	}
}
