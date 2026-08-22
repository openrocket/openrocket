package info.openrocket.core.simulation.montecarlo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel.LevelWindModel;
import info.openrocket.core.models.wind.WindModelType;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.RecoveryDevice;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.simulation.SimulationConditions;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.simulation.extension.AbstractSimulationExtension;
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
	public void testUnsafeExtensionsAreRejectedBeforeTheyExecute() {
		Simulation source = source(0.0);
		CountingExtension extension = new CountingExtension(false, false);
		source.getSimulationExtensions().add(extension);
		MonteCarloSettings settings = MonteCarloSettings.builder().runCount(2).seed(13579).build();

		UnsafeSimulationExtensionException exception = assertThrows(
				UnsafeSimulationExtensionException.class,
				() -> new MonteCarloSimulationRunner().run(source, settings));

		assertEquals(List.of("CountingExtension"), exception.getExtensionNames());
		assertEquals(0, extension.getInitializationCount());
	}

	@Test
	public void testUnexpectedRuntimeFailurePropagatesFromWorker() {
		Simulation source = source(0.0);
		CountingExtension extension = new CountingExtension(true, true);
		source.getSimulationExtensions().add(extension);
		MonteCarloSettings settings = MonteCarloSettings.builder().runCount(2).seed(13579).build();

		IllegalStateException exception = assertThrows(IllegalStateException.class,
				() -> new MonteCarloSimulationRunner().run(source, settings));

		assertEquals("Extension implementation bug", exception.getMessage());
		assertTrue(extension.getInitializationCount() > 1);
	}

	@Test
	public void testWindSpeedVariationPreservesTurbulenceStandardDeviation() {
		MonteCarloSample sample = new MonteCarloSample(1, 13579,
				Map.of(MonteCarloParameter.WIND_SPEED, 2.0));

		SimulationOptions averageOptions = new SimulationOptions();
		averageOptions.setWindModelType(WindModelType.AVERAGE);
		averageOptions.getAverageWindModel().setAverage(5.0);
		averageOptions.getAverageWindModel().setStandardDeviation(1.25);
		MonteCarloSimulationRunner.applyWindVariation(averageOptions, sample);

		assertEquals(7.0, averageOptions.getAverageWindModel().getAverage());
		assertEquals(1.25, averageOptions.getAverageWindModel().getStandardDeviation());

		SimulationOptions multiLevelOptions = new SimulationOptions();
		multiLevelOptions.setWindModelType(WindModelType.MULTI_LEVEL);
		MultiLevelPinkNoiseWindModel multiLevel = multiLevelOptions.getMultiLevelWindModel();
		multiLevel.clearLevels();
		multiLevel.addWindLevel(0, 3.0, 0.25, 0.75);
		multiLevel.addWindLevel(100, 6.0, 0.5, 1.5);
		MonteCarloSimulationRunner.applyWindVariation(multiLevelOptions, sample);

		List<LevelWindModel> levels = multiLevel.getLevels();
		assertEquals(5.0, levels.get(0).getSpeed());
		assertEquals(0.75, levels.get(0).getStandardDeviation());
		assertEquals(8.0, levels.get(1).getSpeed());
		assertEquals(1.5, levels.get(1).getStandardDeviation());
	}

	@Test
	public void testLandingPointsRequireDeploymentBeforeGroundHit() {
		AxialStage recoveredStage = new AxialStage();
		recoveredStage.setName("Recovered");
		FlightDataBranch recovered = landingBranch(recoveredStage, 5.0, 10.0, 12.0, 34.0);
		FlightDataBranch ballistic = landingBranch("Ballistic", null, 10.0, 56.0, 78.0);
		FlightDataBranch lateDeployment = landingBranch("Late deployment", 11.0, 10.0, 90.0, 12.0);

		List<LandingPoint> points = MonteCarloSimulationRunner.extractLandingPoints(
				new FlightData(recovered, ballistic, lateDeployment));

		assertEquals(1, points.size());
		assertEquals("Recovered", points.get(0).branchName());
		assertEquals(12.0, points.get(0).east());
		assertEquals(34.0, points.get(0).north());
		assertEquals(recoveredStage.getID().toString(), points.get(0).bodyId());
		assertEquals(recoveredStage.getID(), recovered.clone().getSourceComponentId());
	}

	@Test
	public void testScalarMetricsAreExtractedPerFlightBranch() {
		AxialStage stage = new AxialStage();
		stage.setName("Sustainer");
		FlightDataBranch branch = new FlightDataBranch(stage.getName(), stage,
				FlightDataType.TYPE_TIME, FlightDataType.TYPE_ALTITUDE,
				FlightDataType.TYPE_VELOCITY_TOTAL, FlightDataType.TYPE_MACH_NUMBER,
				FlightDataType.TYPE_ACCELERATION_TOTAL);
		branch.addPoint();
		branch.setValue(FlightDataType.TYPE_TIME, 0);
		branch.setValue(FlightDataType.TYPE_ALTITUDE, 0);
		branch.setValue(FlightDataType.TYPE_VELOCITY_TOTAL, 20);
		branch.setValue(FlightDataType.TYPE_MACH_NUMBER, 0.1);
		branch.setValue(FlightDataType.TYPE_ACCELERATION_TOTAL, 30);
		branch.addPoint();
		branch.setValue(FlightDataType.TYPE_TIME, 12);
		branch.setValue(FlightDataType.TYPE_ALTITUDE, 500);
		branch.setValue(FlightDataType.TYPE_VELOCITY_TOTAL, 8);
		branch.setValue(FlightDataType.TYPE_MACH_NUMBER, 0.7);
		branch.setValue(FlightDataType.TYPE_ACCELERATION_TOTAL, 45);
		branch.addEvent(new FlightEvent(FlightEvent.Type.APOGEE, 9));
		branch.addEvent(new FlightEvent(FlightEvent.Type.GROUND_HIT, 12));

		MonteCarloBranchResult result = MonteCarloSimulationRunner.extractBranchResults(
				new FlightData(branch)).get(0);

		assertEquals(stage.getID().toString(), result.branchId());
		assertEquals(500, result.getMetric(MonteCarloMetric.APOGEE_ALTITUDE));
		assertEquals(20, result.getMetric(MonteCarloMetric.MAXIMUM_VELOCITY));
		assertEquals(0.7, result.getMetric(MonteCarloMetric.MAXIMUM_MACH));
		assertEquals(45, result.getMetric(MonteCarloMetric.MAXIMUM_ACCELERATION));
		assertEquals(9, result.getMetric(MonteCarloMetric.TIME_TO_APOGEE));
		assertEquals(12, result.getMetric(MonteCarloMetric.FLIGHT_TIME));
		assertEquals(8, result.getMetric(MonteCarloMetric.LANDING_VELOCITY));
	}

	@Test
	public void testSeparatedBoostersAreSelectableLandingBodies() {
		Rocket rocket = TestRockets.makeMultiStageEventTestRocket();
		rocket.getSelectedConfiguration().setAllStages();

		Simulation simulation = new Simulation(rocket);
		simulation.setFlightConfigurationId(
				rocket.getSelectedConfiguration().getFlightConfigurationID());
		simulation.getOptions().setISAAtmosphere(true);
		simulation.getOptions().setTimeStep(0.05);
		simulation.getOptions().getAverageWindModel().setAverage(0.1);

		MonteCarloSettings settings = MonteCarloSettings.builder()
				.runCount(2)
				.threadCount(1)
				.seed(0)
				.build();

		List<String> bodyNames = new MonteCarloSimulationRunner().run(simulation, settings)
				.getLandingBodies().stream()
				.map(LandingBody::branchName)
				.toList();

		assertTrue(bodyNames.contains("Center Booster"),
				"a separated booster must remain selectable when it lands ballistically: " + bodyNames);
		assertTrue(bodyNames.contains("Side boosters"),
				"a separated booster with recovery must remain selectable: " + bodyNames);
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
		List<LandingPoint> points = result.getLandingPoints(body.bodyId());
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

		Simulation source = source(0.2);
		MonteCarloResult serial = new MonteCarloSimulationRunner().run(source, builder.threadCount(1).build());
		MonteCarloResult parallel = new MonteCarloSimulationRunner().run(source, builder.threadCount(4).build());

		LandingBody body = serial.getLandingBodies().get(0);
		for (int run = 0; run < 4; run++) {
			MonteCarloRunResult serialRun = serial.getRunResults().get(run);
			MonteCarloRunResult parallelRun = parallel.getRunResults().get(run);

			assertEquals(serialRun.sample().getSimulationSeed(), parallelRun.sample().getSimulationSeed());
			assertEquals(serialRun.sample().getVariations(), parallelRun.sample().getVariations());

			LandingPoint serialPoint = serialRun.getLandingPoint(body.bodyId());
			LandingPoint parallelPoint = parallelRun.getLandingPoint(body.bodyId());
			assertNotNull(serialPoint);
			assertNotNull(parallelPoint, () -> "Missing body " + body.bodyId()
					+ " from " + parallelRun.landingPoints());
			assertEquals(serialPoint.east(), parallelPoint.east(), 0.5);
			assertEquals(serialPoint.north(), parallelPoint.north(), 0.5);
		}
	}

	/** With the stochastic wind switched off, an analysis repeats exactly. */
	@Test
	public void testAnalysisRepeatsExactlyWithoutStochasticWind() {
		Simulation source = source(0.0);
		String expectedBodyId = source.getRocket().getStage(0).getID().toString();
		MonteCarloSettings settings = MonteCarloSettings.builder()
				.runCount(3)
				.seed(1122334)
				.uncertainty(MonteCarloParameter.AXIAL_DRAG, MonteCarloDistribution.NORMAL, 0.1)
				.build();

		List<Integer> progress = new ArrayList<>();
		MonteCarloResult first = new MonteCarloSimulationRunner().run(source, settings,
				(completed, total) -> {
					assertEquals(4, total);
					progress.add(completed);
				});
		MonteCarloResult repeat = new MonteCarloSimulationRunner().run(source, settings);
		assertEquals(List.of(1, 2, 3, 4), progress);
		assertEquals(expectedBodyId, first.getLandingBodies().get(0).bodyId());
		assertEquals(expectedBodyId, repeat.getLandingBodies().get(0).bodyId());

		LandingBody body = first.getLandingBodies().get(0);
		for (int run = 0; run < 3; run++) {
			LandingPoint firstPoint = first.getRunResults().get(run).getLandingPoint(body.bodyId());
			LandingPoint repeatPoint = repeat.getRunResults().get(run).getLandingPoint(body.bodyId());
			assertNotNull(repeatPoint, "Missing body " + body.bodyId()
					+ " from " + repeat.getRunResults().get(run).landingPoints());
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
			LandingPoint firstPoint = first.getRunResults().get(run).getLandingPoint(body.bodyId());
			LandingPoint repeatPoint = repeat.getRunResults().get(run).getLandingPoint(body.bodyId());
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
		populateLandingBranch(branch, deploymentTime, groundHitTime, east, north);
		return branch;
	}

	private static FlightDataBranch landingBranch(AxialStage stage, Double deploymentTime,
			double groundHitTime, double east, double north) {
		FlightDataBranch branch = new FlightDataBranch(stage.getName(), stage, FlightDataType.TYPE_TIME,
				FlightDataType.TYPE_POSITION_X, FlightDataType.TYPE_POSITION_Y);
		populateLandingBranch(branch, deploymentTime, groundHitTime, east, north);
		return branch;
	}

	private static void populateLandingBranch(FlightDataBranch branch, Double deploymentTime,
			double groundHitTime, double east, double north) {
		branch.addPoint();
		branch.setValue(FlightDataType.TYPE_TIME, groundHitTime);
		branch.setValue(FlightDataType.TYPE_POSITION_X, east);
		branch.setValue(FlightDataType.TYPE_POSITION_Y, north);
		if (deploymentTime != null) {
			branch.addEvent(new FlightEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT,
					deploymentTime));
		}
		branch.addEvent(new FlightEvent(FlightEvent.Type.GROUND_HIT, groundHitTime));
	}

	private static final class CountingExtension extends AbstractSimulationExtension {
		private final AtomicInteger initializationCount = new AtomicInteger();
		private final boolean monteCarloSafe;
		private final boolean failAfterNominal;

		private CountingExtension(boolean monteCarloSafe, boolean failAfterNominal) {
			this.monteCarloSafe = monteCarloSafe;
			this.failAfterNominal = failAfterNominal;
		}

		@Override
		public boolean isMonteCarloSafe() {
			return monteCarloSafe;
		}

		@Override
		public void initialize(SimulationConditions conditions) {
			if (initializationCount.incrementAndGet() > 1 && failAfterNominal) {
				throw new IllegalStateException("Extension implementation bug");
			}
		}

		private int getInitializationCount() {
			return initializationCount.get();
		}
	}
}
