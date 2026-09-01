package info.openrocket.core.simulation.montecarlo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class MonteCarloResultTest {
	@Test
	public void testMetricValuesFollowStableBranchIdentityAndExcludeFailures() {
		String branchId = "sustainer";
		MonteCarloSettings settings = MonteCarloSettings.builder().runCount(3).seed(17).build();
		MonteCarloRunResult nominal = metricRun(0, branchId, 0, 100, null, null);
		MonteCarloRunResult successful = metricRun(1, branchId, 1, 110, null, null);
		MonteCarloRunResult branchFailure = metricRun(2, branchId, 0, 120, "Branch aborted", null);
		MonteCarloRunResult trajectoryFailure = metricRun(3, branchId, 0, 130, null, "Trajectory failed");
		MonteCarloResult result = new MonteCarloResult(settings, nominal,
				List.of(successful, branchFailure, trajectoryFailure), 1);

		assertEquals(List.of(110.0),
				result.getMetricValues(branchId, MonteCarloMetric.APOGEE_ALTITUDE));
		assertEquals(2, result.getMetricMissingCount(branchId, MonteCarloMetric.APOGEE_ALTITUDE));
		assertEquals(1, result.getFlightBranches().size());
		assertEquals(branchId, result.getFlightBranches().get(0).branchId());
		assertEquals(0, result.getFlightBranches().get(0).branchIndex(),
				"the nominal branch supplies the stable display order");
	}

	private static MonteCarloRunResult metricRun(int runNumber, String branchId, int branchIndex,
			double apogee, String branchFailure, String trajectoryFailure) {
		MonteCarloSample sample = runNumber == 0 ? MonteCarloSample.nominal(17)
				: new MonteCarloSample(runNumber, 17 + runNumber, Collections.emptyMap());
		MonteCarloBranchResult branch = new MonteCarloBranchResult(branchId, branchIndex,
				"Sustainer", Map.of(MonteCarloMetric.APOGEE_ALTITUDE, apogee), branchFailure);
		return new MonteCarloRunResult(sample, List.of(), List.of(), List.of(branch),
				apogee, 10, trajectoryFailure);
	}

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

		assertEquals(1, result.getLandingPoints(nominalPoint.bodyId()).size());
		assertEquals(3, result.getLandingPoints(nominalPoint.bodyId()).get(0).east());
		assertEquals(1, result.getFailureCount(nominalPoint.bodyId()));
	}

	@Test
	public void testStableBodyIdentitySurvivesBranchReordering() {
		String bodyId = "component-body";
		MonteCarloSettings settings = MonteCarloSettings.builder().runCount(2).seed(17).build();
		MonteCarloRunResult nominal = new MonteCarloRunResult(MonteCarloSample.nominal(17),
				List.of(new LandingPoint(bodyId, 0, "Rocket", 1, 2)), 100, 10, null);
		MonteCarloRunResult reordered = new MonteCarloRunResult(
				new MonteCarloSample(1, 18, Collections.emptyMap()),
				List.of(new LandingPoint("other-component", 0, "Booster", 50, 60),
						new LandingPoint(bodyId, 1, "Rocket", 3, 4)),
				100, 10, null);
		MonteCarloResult result = new MonteCarloResult(settings, nominal, List.of(reordered), 1);

		assertEquals(1, result.getLandingPoints(bodyId).size());
		assertEquals(3, result.getLandingPoints(bodyId).get(0).east());
		assertEquals(1, reordered.getLandingPoint(bodyId).branchIndex());
	}

	@Test
	public void testBodyFailureDoesNotDiscardSuccessfulSiblingLanding() {
		String sustainerId = "sustainer";
		String boosterId = "booster";
		MonteCarloSettings settings = MonteCarloSettings.builder().runCount(2).seed(17).build();
		MonteCarloRunResult nominal = new MonteCarloRunResult(MonteCarloSample.nominal(17),
				List.of(new LandingPoint(sustainerId, 0, "Sustainer", 1, 2),
						new LandingPoint(boosterId, 1, "Booster", 3, 4)),
				100, 10, null);
		MonteCarloRunResult partiallyFailed = new MonteCarloRunResult(
				new MonteCarloSample(1, 18, Collections.emptyMap()),
				List.of(new LandingPoint(sustainerId, 0, "Sustainer", 5, 6),
						new LandingPoint(boosterId, 1, "Booster", 7, 8)),
				List.of(new LandingBodyFailure(boosterId, 1, "Booster", "Simulation aborted")),
				100, 10, null);
		MonteCarloResult result = new MonteCarloResult(settings, nominal, List.of(partiallyFailed), 1);

		assertEquals(1, result.getLandingPoints(sustainerId).size());
		assertEquals(0, result.getFailureCount(sustainerId));
		assertEquals(0, result.getLandingPoints(boosterId).size());
		assertEquals(1, result.getFailureCount(boosterId));
		assertEquals("Simulation aborted", partiallyFailed.getFailureMessage(boosterId));
		assertNull(partiallyFailed.getFailureMessage(sustainerId));
	}
}
