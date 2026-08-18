package info.openrocket.core.simulation.montecarlo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

public class MonteCarloResultTest {
	@Test
	public void testFailedTrajectoriesAreExcludedFromLandingStatistics() {
		MonteCarloSettings settings = MonteCarloSettings.builder().runCount(2).seed(17).build();
		LandingPoint nominalPoint = new LandingPoint(0, "Rocket", 1, 2);
		MonteCarloRunResult nominal = new MonteCarloRunResult(MonteCarloSample.nominal(17),
				List.of(nominalPoint), 100, 10, null);
		MonteCarloRunResult successful = new MonteCarloRunResult(
				new MonteCarloSample(1, 18, Collections.emptyMap()),
				List.of(new LandingPoint(0, "Rocket", 3, 4)), 100, 10, null);
		MonteCarloRunResult failed = new MonteCarloRunResult(
				new MonteCarloSample(2, 19, Collections.emptyMap()),
				List.of(new LandingPoint(0, "Rocket", 50, 60)), 100, 10, "Simulation aborted");
		MonteCarloResult result = new MonteCarloResult(settings, nominal, List.of(successful, failed), 1);

		assertEquals(1, result.getLandingPoints(0).size());
		assertEquals(3, result.getLandingPoints(0).get(0).east());
		assertEquals(1, result.getFailureCount(0));
	}
}
