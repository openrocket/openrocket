package info.openrocket.core.simulation.montecarlo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel.LevelWindModel;
import info.openrocket.core.models.wind.WindModelType;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.simulation.exception.SimulationCancelledException;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.listeners.system.InterruptListener;
import info.openrocket.core.util.BugException;

/**
 * Runs a nominal baseline and a reproducible set of dispersed trajectory simulations.
 * The supplied simulation and rocket document are never modified.
 * <p>
 * Every sample is drawn before any trajectory starts, so sampled inputs depend only on the
 * master seed. Each trajectory runs on an independent copy of the rocket to prevent
 * concurrent simulations from sharing mutable configuration and aerodynamic caches.
 * <p>
 * Only bodies that deploy a recovery device before reaching the ground are included. A
 * ballistic nominal flight is rejected with a {@link BallisticTrajectoryException}.
 */
public final class MonteCarloSimulationRunner {
	static final String BALLISTIC_DESCENT =
			"Ballistic descent: landing dispersion is only reported for bodies recovered under a deployed device";

	private static final MonteCarloProgressListener NO_PROGRESS = (completed, total) -> {
	};

	public MonteCarloResult run(Simulation simulation, MonteCarloSettings settings) {
		return run(simulation, settings, NO_PROGRESS);
	}

	/**
	 * Run a complete analysis.
	 *
	 * @param simulation simulation to disperse; neither it nor its rocket is modified
	 * @param settings analysis configuration
	 * @param progressListener receives monotonic progress updates on the calling thread
	 * @return the completed analysis
	 */
	public MonteCarloResult run(Simulation simulation, MonteCarloSettings settings,
			MonteCarloProgressListener progressListener) {
		Objects.requireNonNull(simulation, "simulation");
		Objects.requireNonNull(settings, "settings");
		Objects.requireNonNull(progressListener, "progressListener");

		long start = System.currentTimeMillis();
		int totalTrajectories = settings.getRunCount() + 1;

		// Reject an unsupported nominal flight before computing the ensemble.
		MonteCarloRunResult nominal = runTrajectory(simulation, MonteCarloSample.nominal(settings.getSeed()), true);
		progressListener.onProgress(1, totalTrajectories);

		// Sampling before submission makes it independent of worker scheduling.
		MonteCarloSampler sampler = new MonteCarloSampler(settings);
		List<MonteCarloSample> samples = new ArrayList<>(settings.getRunCount());
		for (int runNumber = 1; runNumber <= settings.getRunCount(); runNumber++) {
			samples.add(sampler.nextSample(runNumber));
		}

		List<MonteCarloRunResult> results = simulateAll(simulation, samples, settings.getThreadCount(),
				progressListener, totalTrajectories);
		return new MonteCarloResult(settings, nominal, results, System.currentTimeMillis() - start);
	}

	private List<MonteCarloRunResult> simulateAll(Simulation source, List<MonteCarloSample> samples,
			int threadCount, MonteCarloProgressListener progressListener, int totalTrajectories) {
		ExecutorService executor = Executors.newFixedThreadPool(Math.min(threadCount, samples.size()),
				workerThreadFactory());
		ExecutorCompletionService<MonteCarloRunResult> completionService =
				new ExecutorCompletionService<>(executor);
		try {
			for (MonteCarloSample sample : samples) {
				completionService.submit(() -> runTrajectory(source, sample, false));
			}

			// Collect in completion order for accurate progress, then restore run order.
			List<MonteCarloRunResult> results = new ArrayList<>(samples.size());
			for (int index = 0; index < samples.size(); index++) {
				results.add(null);
			}
			for (int completed = 0; completed < samples.size(); completed++) {
				MonteCarloRunResult result = awaitNextResult(completionService);
				results.set(result.sample().getRunNumber() - 1, result);
				progressListener.onProgress(completed + 2, totalTrajectories);
			}
			return results;
		} finally {
			executor.shutdownNow();
		}
	}

	private static MonteCarloRunResult awaitNextResult(
			ExecutorCompletionService<MonteCarloRunResult> completionService) {
		try {
			return awaitResult(completionService.take());
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new CancellationException("Landing-dispersion analysis was cancelled");
		}
	}

	private static MonteCarloRunResult awaitResult(Future<MonteCarloRunResult> future) {
		try {
			return future.get();
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new CancellationException("Landing-dispersion analysis was cancelled");
		} catch (CancellationException exception) {
			throw exception;
		} catch (ExecutionException exception) {
			Throwable cause = exception.getCause();
			if (cause instanceof CancellationException cancellation) {
				throw cancellation;
			}
			if (cause instanceof RuntimeException runtime) {
				throw runtime;
			}
			throw new BugException("Landing-dispersion trajectory failed unexpectedly", cause);
		}
	}

	private static ThreadFactory workerThreadFactory() {
		ThreadFactory defaultFactory = Executors.defaultThreadFactory();
		return runnable -> {
			Thread thread = defaultFactory.newThread(runnable);
			thread.setName("LandingDispersion-" + thread.getName());
			thread.setDaemon(true);
			return thread;
		};
	}

	/**
	 * Simulate one trajectory.
	 *
	 * @param source simulation to disperse
	 * @param sample sampled deviations to apply
	 * @param refuseBallistic if {@code true}, a flight that reaches the ground with no
	 *        recovery device deployed aborts the whole analysis instead of being recorded
	 * @return the trajectory outcome
	 */
	private MonteCarloRunResult runTrajectory(Simulation source, MonteCarloSample sample,
			boolean refuseBallistic) {
		checkCancellation();

		Simulation simulation = source.duplicateForIndependentSimulation();
		SimulationOptions options = simulation.getOptions();
		applySampledOptions(options, sample);

		String failure = null;
		try {
			simulation.simulate(new MonteCarloVariationListener(sample), InterruptListener.INSTANCE);
		} catch (SimulationCancelledException exception) {
			throw new CancellationException(exception.getMessage());
		} catch (CancellationException exception) {
			throw exception;
		} catch (SimulationException | RuntimeException exception) {
			failure = exception.getLocalizedMessage();
			if (failure == null || failure.isBlank()) {
				failure = exception.getClass().getSimpleName();
			}
		}

		FlightData data = simulation.getSimulatedData();
		List<LandingPoint> landingPoints = extractLandingPoints(data);
		if (failure == null && simulation.hasErrors()) {
			failure = findAbortMessage(data);
		}
		if (failure == null && landingPoints.isEmpty() && hasBallisticGroundHit(data)) {
			if (refuseBallistic) {
				throw new BallisticTrajectoryException(BALLISTIC_DESCENT);
			}
			failure = BALLISTIC_DESCENT;
		}
		if (failure == null && landingPoints.isEmpty()) {
			failure = "Simulation produced no ground-hit event";
		}

		double maximumAltitude = data != null ? data.getMaxAltitude() : Double.NaN;
		double flightTime = data != null ? data.getFlightTime() : Double.NaN;
		return new MonteCarloRunResult(sample, landingPoints, maximumAltitude, flightTime, failure);
	}

	private static void applySampledOptions(SimulationOptions options, MonteCarloSample sample) {
		options.setRandomSeedFixed(true);
		options.setRandomSeed(sample.getSimulationSeed());
		options.setLaunchRodAngle(options.getLaunchRodAngle()
				+ sample.getVariation(MonteCarloParameter.LAUNCH_GUIDE_ANGLE));

		applyWindVariation(options, sample);

		// Resolve "launch into wind" against the varied wind, then add the physical
		// launcher alignment error around that intended direction.
		double intendedDirection = options.getLaunchRodDirection();
		options.setLaunchIntoWind(false);
		options.setLaunchRodDirection(intendedDirection
				+ sample.getVariation(MonteCarloParameter.LAUNCH_GUIDE_DIRECTION));
	}

	private static void applyWindVariation(SimulationOptions options, MonteCarloSample sample) {
		double speedVariation = sample.getVariation(MonteCarloParameter.WIND_SPEED);
		double directionVariation = sample.getVariation(MonteCarloParameter.WIND_DIRECTION);

		if (options.getWindModelType() == WindModelType.AVERAGE) {
			options.getAverageWindModel().setAverage(
					Math.max(0, options.getAverageWindModel().getAverage() + speedVariation));
			options.getAverageWindModel().setDirection(
					options.getAverageWindModel().getDirection() + directionVariation);
			return;
		}

		MultiLevelPinkNoiseWindModel model = options.getMultiLevelWindModel();
		for (LevelWindModel level : model.getLevels()) {
			level.setSpeed(Math.max(0, level.getSpeed() + speedVariation));
			level.setDirection(level.getDirection() + directionVariation);
		}
	}

	static List<LandingPoint> extractLandingPoints(FlightData data) {
		List<LandingPoint> points = new ArrayList<>();
		if (data == null) {
			return points;
		}

		for (int branchIndex = 0; branchIndex < data.getBranchCount(); branchIndex++) {
			FlightDataBranch branch = data.getBranch(branchIndex);
			if (branch.getFirstEvent(FlightEvent.Type.GROUND_HIT) == null) {
				continue;
			}
			// Ballistic bodies are outside the recovery-area analysis.
			if (!descendsUnderRecoveryDevice(branch)) {
				continue;
			}

			double east = branch.getLast(FlightDataType.TYPE_POSITION_X);
			double north = branch.getLast(FlightDataType.TYPE_POSITION_Y);
			if (Double.isFinite(east) && Double.isFinite(north)) {
				points.add(new LandingPoint(branchIndex, branch.getName(), east, north));
			}
		}
		return points;
	}

	private static boolean descendsUnderRecoveryDevice(FlightDataBranch branch) {
		FlightEvent deployment = branch.getFirstEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT);
		FlightEvent groundHit = branch.getFirstEvent(FlightEvent.Type.GROUND_HIT);
		return deployment != null && groundHit != null && deployment.getTime() < groundHit.getTime();
	}

	private static boolean hasBallisticGroundHit(FlightData data) {
		if (data == null) {
			return false;
		}
		for (FlightDataBranch branch : data.getBranches()) {
			if (branch.getFirstEvent(FlightEvent.Type.GROUND_HIT) != null
					&& !descendsUnderRecoveryDevice(branch)) {
				return true;
			}
		}
		return false;
	}

	private static String findAbortMessage(FlightData data) {
		if (data != null) {
			for (FlightDataBranch branch : data.getBranches()) {
				FlightEvent abort = branch.getFirstEvent(FlightEvent.Type.SIM_ABORT);
				if (abort != null && abort.getData() != null) {
					return abort.getData().toString();
				}
			}
		}
		return "Simulation aborted";
	}

	private static void checkCancellation() {
		if (Thread.currentThread().isInterrupted()) {
			throw new CancellationException("Landing-dispersion analysis was cancelled");
		}
	}
}
