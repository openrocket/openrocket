package info.openrocket.swing.gui.simulation;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.simulation.montecarlo.MonteCarloDistribution;
import info.openrocket.core.simulation.montecarlo.MonteCarloParameter;
import info.openrocket.core.simulation.montecarlo.MonteCarloResult;
import info.openrocket.core.simulation.montecarlo.MonteCarloSettings;
import info.openrocket.core.util.TestRockets;
import info.openrocket.swing.util.BaseTestCase;

public class LandingDispersionAnalysisCacheTest extends BaseTestCase {
	@AfterEach
	public void clearCache() {
		LandingDispersionAnalysisCache.clear();
	}

	@Test
	public void testResultRequiresMatchingAnalysisSettings() {
		Simulation simulation = new Simulation(TestRockets.makeEstesAlphaIII());
		MonteCarloSettings settings = settings(12345, 1, 0.1);
		MonteCarloResult result = result(settings);
		LandingDispersionAnalysisCache.put(simulation, result);

		assertSame(result, LandingDispersionAnalysisCache.get(simulation, settings));
		assertSame(result, LandingDispersionAnalysisCache.get(simulation,
				settings(12345, 4, 0.1)), "worker count is not a dispersion definition");
		assertNull(LandingDispersionAnalysisCache.get(simulation, settings(54321, 1, 0.1)));
		assertNull(LandingDispersionAnalysisCache.get(simulation, settings(12345, 1, 0.2)));
		assertSame(result, LandingDispersionAnalysisCache.get(simulation),
				"a settings mismatch hides but does not discard an otherwise valid result");
	}

	@Test
	public void testSimulationOptionChangeInvalidatesResult() {
		Simulation simulation = new Simulation(TestRockets.makeEstesAlphaIII());
		MonteCarloResult result = result(settings(12345, 1, 0.1));
		LandingDispersionAnalysisCache.put(simulation, result);

		// launchIntoWind is deliberately checked even though SimulationOptions.equals
		// currently omits it.
		simulation.getOptions().setLaunchIntoWind(!simulation.getOptions().getLaunchIntoWind());

		assertNull(LandingDispersionAnalysisCache.get(simulation));
	}

	@Test
	public void testRocketChangeInvalidatesResult() {
		Simulation simulation = new Simulation(TestRockets.makeEstesAlphaIII());
		MonteCarloResult result = result(settings(12345, 1, 0.1));
		LandingDispersionAnalysisCache.put(simulation, result);

		simulation.getRocket().setName("Modified rocket");

		assertNull(LandingDispersionAnalysisCache.get(simulation));
	}

	private static MonteCarloSettings settings(int seed, int threadCount, double axialDragSpread) {
		return MonteCarloSettings.builder()
				.runCount(2)
				.seed(seed)
				.threadCount(threadCount)
				.uncertainty(MonteCarloParameter.AXIAL_DRAG,
						MonteCarloDistribution.NORMAL, axialDragSpread)
				.build();
	}

	private static MonteCarloResult result(MonteCarloSettings settings) {
		return new MonteCarloResult(settings, null, List.of(), 0);
	}
}
