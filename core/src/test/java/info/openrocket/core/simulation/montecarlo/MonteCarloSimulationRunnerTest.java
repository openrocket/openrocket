package info.openrocket.core.simulation.montecarlo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.rocketcomponent.RecoveryDevice;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.TestRockets;

public class MonteCarloSimulationRunnerTest extends BaseTestCase {
	private static Simulation source(double windStandardDeviation) {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		Simulation source = new Simulation(rocket);
		source.setFlightConfigurationId(TestRockets.TEST_FCID_0);
		source.getOptions().setLaunchRodLength(3.0);
		source.getOptions().getAverageWindModel().setAverage(3.0);
		source.getOptions().getAverageWindModel().setStandardDeviation(windStandardDeviation);
		return source;
	}

	/** A ballistic nominal trajectory is outside the recovery-area analysis. */
	@Test
	public void testBallisticFlightIsRefused() {
		Simulation source = source(0.0);
		for (RocketComponent component : source.getRocket().getAllChildren()) {
			if (component instanceof RecoveryDevice) {
				component.getParent().removeChild(component);
			}
		}

		MonteCarloSettings settings = MonteCarloSettings.builder()
				.runCount(2)
				.seed(13579)
				.uncertainty(MonteCarloParameter.AXIAL_DRAG, MonteCarloDistribution.NORMAL, 0.1)
				.build();

		assertThrows(BallisticTrajectoryException.class,
				() -> new MonteCarloSimulationRunner().run(source, settings));
	}

	@Test
	public void testLandingPointsRequireDeploymentBeforeGroundHit() {
		FlightDataBranch recovered = landingBranch("Recovered", 5.0, 10.0, 12.0, 34.0);
		FlightDataBranch ballistic = landingBranch("Ballistic", null, 10.0, 56.0, 78.0);
		FlightDataBranch lateDeployment = landingBranch("Late deployment", 11.0, 10.0, 90.0, 12.0);

		List<LandingPoint> points = MonteCarloSimulationRunner.extractLandingPoints(
				new FlightData(recovered, ballistic, lateDeployment));

		assertEquals(1, points.size());
		assertEquals("Recovered", points.get(0).branchName());
		assertEquals(12.0, points.get(0).east());
		assertEquals(34.0, points.get(0).north());
	}

	/**
	 * Recovery drag is applied while the landing stepper is running, which is a different
	 * code path from the flight steppers. Assert on the trajectory rather than on the
	 * listener, so that a variation that never reaches the descent phase fails here.
	 */
	@Test
	public void testRecoveryDragDispersesTheLandingPoint() {
		MonteCarloSettings settings = MonteCarloSettings.builder()
				.runCount(4)
				.seed(987654321)
				.uncertainty(MonteCarloParameter.RECOVERY_DRAG, MonteCarloDistribution.UNIFORM, 0.5)
				.build();

		MonteCarloResult result = new MonteCarloSimulationRunner().run(source(0.0), settings);
		LandingBody body = result.getLandingBodies().get(0);
		List<LandingPoint> points = result.getLandingPoints(body.branchIndex());
		assertEquals(4, points.size());

		double minimum = points.stream().mapToDouble(LandingPoint::rangeFromPad).min().orElseThrow();
		double maximum = points.stream().mapToDouble(LandingPoint::rangeFromPad).max().orElseThrow();
		assertTrue(maximum - minimum > 1.0,
				"a +/-50% recovery drag spread must move the landing point, saw " + (maximum - minimum) + " m");
	}

	/**
	 * Sampling happens before any trajectory runs, so the sampled inputs must not depend
	 * on the thread count. Trajectories use a looser bound because turbulent descent is
	 * sensitive to identity-dependent floating-point summation order.
	 */
	@Test
	public void testResultsAreIndependentOfThreadCount() {
		MonteCarloSettings.Builder builder = MonteCarloSettings.builder()
				.runCount(4)
				.seed(24680)
				.uncertainty(MonteCarloParameter.AXIAL_DRAG, MonteCarloDistribution.NORMAL, 0.1);

		MonteCarloResult serial = new MonteCarloSimulationRunner().run(source(0.2), builder.threadCount(1).build());
		MonteCarloResult parallel = new MonteCarloSimulationRunner().run(source(0.2), builder.threadCount(4).build());

		LandingBody body = serial.getLandingBodies().get(0);
		for (int run = 0; run < 4; run++) {
			MonteCarloRunResult serialRun = serial.getRunResults().get(run);
			MonteCarloRunResult parallelRun = parallel.getRunResults().get(run);

			assertEquals(serialRun.sample().getSimulationSeed(), parallelRun.sample().getSimulationSeed());
			assertEquals(serialRun.sample().getVariations(), parallelRun.sample().getVariations());

			LandingPoint serialPoint = serialRun.getLandingPoint(body.branchIndex());
			LandingPoint parallelPoint = parallelRun.getLandingPoint(body.branchIndex());
			assertNotNull(serialPoint);
			assertNotNull(parallelPoint);
			assertEquals(serialPoint.east(), parallelPoint.east(), 0.5);
			assertEquals(serialPoint.north(), parallelPoint.north(), 0.5);
		}
	}

	/** With the stochastic wind switched off, an analysis repeats exactly. */
	@Test
	public void testAnalysisRepeatsExactlyWithoutStochasticWind() {
		MonteCarloSettings settings = MonteCarloSettings.builder()
				.runCount(3)
				.seed(1122334)
				.uncertainty(MonteCarloParameter.AXIAL_DRAG, MonteCarloDistribution.NORMAL, 0.1)
				.build();

		List<Integer> progress = new ArrayList<>();
		MonteCarloResult first = new MonteCarloSimulationRunner().run(source(0.0), settings,
				(completed, total) -> {
					assertEquals(4, total);
					progress.add(completed);
				});
		MonteCarloResult repeat = new MonteCarloSimulationRunner().run(source(0.0), settings);
		assertEquals(List.of(1, 2, 3, 4), progress);

		LandingBody body = first.getLandingBodies().get(0);
		for (int run = 0; run < 3; run++) {
			LandingPoint firstPoint = first.getRunResults().get(run).getLandingPoint(body.branchIndex());
			LandingPoint repeatPoint = repeat.getRunResults().get(run).getLandingPoint(body.branchIndex());
			assertEquals(firstPoint.east(), repeatPoint.east(), 1.0e-9);
			assertEquals(firstPoint.north(), repeatPoint.north(), 1.0e-9);
		}
	}

	@Test
	public void testRunnerIsReproducibleAndDoesNotMutateSourceSimulation() {
		Simulation source = source(0.2);

		MonteCarloSettings settings = MonteCarloSettings.builder()
				.runCount(2)
				.seed(123456789)
				.uncertainty(MonteCarloParameter.WIND_DIRECTION, MonteCarloDistribution.UNIFORM,
						Math.toRadians(5))
				.build();
		MonteCarloSimulationRunner runner = new MonteCarloSimulationRunner();
		MonteCarloResult first = runner.run(source, settings);
		MonteCarloResult repeat = runner.run(source, settings);

		assertNull(source.getSimulatedData(), "analysis must not replace the selected simulation's data");
		assertEquals(Simulation.Status.NOT_SIMULATED, source.getStatus());
		assertEquals(2, first.getRunResults().size());
		assertFalse(first.getLandingBodies().isEmpty());

		LandingBody body = first.getLandingBodies().get(0);
		for (int run = 0; run < settings.getRunCount(); run++) {
			LandingPoint firstPoint = first.getRunResults().get(run).getLandingPoint(body.branchIndex());
			LandingPoint repeatPoint = repeat.getRunResults().get(run).getLandingPoint(body.branchIndex());
			assertNotNull(firstPoint);
			assertNotNull(repeatPoint);
			assertEquals(firstPoint.east(), repeatPoint.east(), 5.0e-4);
			assertEquals(firstPoint.north(), repeatPoint.north(), 5.0e-4);
		}
	}

	private static FlightDataBranch landingBranch(String name, Double deploymentTime,
			double groundHitTime, double east, double north) {
		FlightDataBranch branch = new FlightDataBranch(name, FlightDataType.TYPE_TIME,
				FlightDataType.TYPE_POSITION_X, FlightDataType.TYPE_POSITION_Y);
		branch.addPoint();
		branch.setValue(FlightDataType.TYPE_TIME, groundHitTime);
		branch.setValue(FlightDataType.TYPE_POSITION_X, east);
		branch.setValue(FlightDataType.TYPE_POSITION_Y, north);
		if (deploymentTime != null) {
			branch.addEvent(new FlightEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT,
					deploymentTime));
		}
		branch.addEvent(new FlightEvent(FlightEvent.Type.GROUND_HIT, groundHitTime));
		return branch;
	}
}
