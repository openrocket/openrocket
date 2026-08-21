package info.openrocket.core.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.Quaternion;
import info.openrocket.core.util.TestRockets;
import info.openrocket.core.util.WorldCoordinate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Numerical release-gate tests for the Runge-Kutta simulation steppers.
 * <p>
 * These tests exercise the actual {@link SimulationStepper#step} implementations,
 * while replacing rocket forces with smooth, deterministic acceleration models.
 * Full-flight simulations contain motor, deployment, and event discontinuities, so
 * they cannot reliably demonstrate the formal order of an integration method.
 */
public class RungeKuttaSimulationStepperTest extends BaseTestCase {

	private static final double ANALYTIC_TOLERANCE = 1.0e-10;
	private static final double OSCILLATOR_DURATION = 4.0;
	private static final double[] CONVERGENCE_TIME_STEPS = { 0.4, 0.2, 0.1 };

	/**
	 * A ballistic trajectory under constant acceleration has a closed-form solution.
	 * This also guards the weighted-stage accumulation that previously failed in RK6.
	 */
	@ParameterizedTest(name = "{0}")
	@EnumSource(SimulationStepperMethod.class)
	public void testConstantAccelerationMatchesBallisticSolution(SimulationStepperMethod method)
			throws SimulationException {
		Coordinate initialPosition = new Coordinate(1.0, -2.0, 100.0);
		Coordinate initialVelocity = new Coordinate(5.0, -3.0, 30.0);
		Coordinate acceleration = new Coordinate(2.0, 1.0, -9.81);
		double duration = 2.0;

		SimulationStatus result = integrate(method, status -> acceleration,
				initialPosition, initialVelocity, 0.2, duration);

		Coordinate expectedPosition = new Coordinate(
				initialPosition.getX() + initialVelocity.getX() * duration
						+ acceleration.getX() * duration * duration / 2.0,
				initialPosition.getY() + initialVelocity.getY() * duration
						+ acceleration.getY() * duration * duration / 2.0,
				initialPosition.getZ() + initialVelocity.getZ() * duration
						+ acceleration.getZ() * duration * duration / 2.0);
		Coordinate expectedVelocity = new Coordinate(
				initialVelocity.getX() + acceleration.getX() * duration,
				initialVelocity.getY() + acceleration.getY() * duration,
				initialVelocity.getZ() + acceleration.getZ() * duration);

		assertCoordinateEquals(expectedPosition, result.getRocketPosition(), ANALYTIC_TOLERANCE,
				method + " ballistic position");
		assertCoordinateEquals(expectedVelocity, result.getRocketVelocity(), ANALYTIC_TOLERANCE,
				method + " ballistic velocity");
	}

	/**
	 * Verify the observed order on the smooth harmonic oscillator x'' = -x.
	 * Halving the timestep should reduce error by approximately 2^p for order p.
	 */
	@ParameterizedTest(name = "{0} converges at order {1}")
	@MethodSource("stepperOrders")
	public void testSmoothProblemConvergesAtExpectedOrder(SimulationStepperMethod method,
			double minimumOrder) throws SimulationException {
		double[] errors = new double[CONVERGENCE_TIME_STEPS.length];
		for (int i = 0; i < CONVERGENCE_TIME_STEPS.length; i++) {
			SimulationStatus result = integrate(method,
					status -> new Coordinate(0, 0, -status.getRocketPosition().getZ()),
					new Coordinate(0, 0, 1), Coordinate.ZERO,
					CONVERGENCE_TIME_STEPS[i], OSCILLATOR_DURATION);
			errors[i] = oscillatorError(result, OSCILLATOR_DURATION);
		}

		for (int i = 0; i < errors.length - 1; i++) {
			double observedOrder = Math.log(errors[i] / errors[i + 1]) / Math.log(2.0);
			assertTrue(observedOrder >= minimumOrder,
					method + " observed order " + observedOrder + " between timesteps "
							+ CONVERGENCE_TIME_STEPS[i] + " and " + CONVERGENCE_TIME_STEPS[i + 1]
							+ ", errors=" + errors[i] + " and " + errors[i + 1]);
		}
	}

	/**
	 * RK4 and RK6 are independent approximations of the same equations and must
	 * approach the same state as their timestep decreases.
	 */
	@Test
	public void testRK4AndRK6ConvergeToSameSolution() throws SimulationException {
		AccelerationModel oscillator =
				status -> new Coordinate(0, 0, -status.getRocketPosition().getZ());
		Coordinate initialPosition = new Coordinate(0, 0, 1);

		SimulationStatus rk4 = integrate(SimulationStepperMethod.RK4, oscillator,
				initialPosition, Coordinate.ZERO, 0.05, OSCILLATOR_DURATION);
		SimulationStatus rk6 = integrate(SimulationStepperMethod.RK6, oscillator,
				initialPosition, Coordinate.ZERO, 0.05, OSCILLATOR_DURATION);

		assertCoordinateEquals(rk4.getRocketPosition(), rk6.getRocketPosition(), 1.0e-6,
				"RK4/RK6 oscillator position");
		assertCoordinateEquals(rk4.getRocketVelocity(), rk6.getRocketVelocity(), 1.0e-6,
				"RK4/RK6 oscillator velocity");
	}

	private static Stream<Arguments> stepperOrders() {
		return Stream.of(
				Arguments.of(SimulationStepperMethod.RK4, 3.7),
				Arguments.of(SimulationStepperMethod.RK6, 5.5));
	}

	/**
	 * Integrate a deterministic second-order system using the production stepper.
	 */
	private SimulationStatus integrate(SimulationStepperMethod method, AccelerationModel accelerationModel,
			CoordinateIF initialPosition, CoordinateIF initialVelocity, double timeStep, double duration)
			throws SimulationException {
		int stepCount = (int) Math.round(duration / timeStep);
		assertEquals(duration, stepCount * timeStep, 1.0e-12,
				"Test duration must be an integer number of timesteps");

		Rocket rocket = TestRockets.makeEstesAlphaIII();
		Simulation simulation = new Simulation(rocket);
		simulation.setFlightConfigurationId(TestRockets.TEST_FCID_0);
		simulation.getOptions().setISAAtmosphere(true);
		simulation.getOptions().setTimeStep(timeStep);
		simulation.getOptions().getAverageWindModel().setAverage(0.0);
		simulation.getOptions().getAverageWindModel().setStandardDeviation(0.0);

		SimulationConditions conditions = simulation.getOptions().toSimulationConditions();
		conditions.setSimulation(simulation);
		FlightConfiguration configuration = simulation.getActiveConfiguration();
		SimulationStatus status = new SimulationStatus(configuration, conditions);
		status.setFlightDataBranch(new FlightDataBranch("Numerical integration", FlightDataType.TYPE_TIME));
		status.setRocketPosition(initialPosition);
		status.setRocketVelocity(initialVelocity);
		WorldCoordinate worldPosition = conditions.getGeodeticComputation()
				.addCoordinate(conditions.getLaunchSite(), initialPosition);
		status.setRocketWorldPosition(worldPosition);
		status.setLiftoff(true);
		status.setLaunchRodCleared(true);

		SimulationStepper stepper = createStepper(method, accelerationModel);
		status = stepper.initialize(status);
		for (int i = 0; i < stepCount; i++) {
			stepper.step(status, timeStep);
		}
		return status;
	}

	private SimulationStepper createStepper(SimulationStepperMethod method, AccelerationModel accelerationModel) {
		return switch (method) {
			case RK4 -> new DeterministicRK4Stepper(accelerationModel);
			case RK6 -> new DeterministicRK6Stepper(accelerationModel);
		};
	}

	private double oscillatorError(SimulationStatus status, double time) {
		double positionError = status.getRocketPosition().getZ() - Math.cos(time);
		double velocityError = status.getRocketVelocity().getZ() + Math.sin(time);
		return Math.hypot(positionError, velocityError);
	}

	private void assertCoordinateEquals(CoordinateIF expected, CoordinateIF actual,
			double tolerance, String description) {
		assertEquals(expected.getX(), actual.getX(), tolerance, description + " x");
		assertEquals(expected.getY(), actual.getY(), tolerance, description + " y");
		assertEquals(expected.getZ(), actual.getZ(), tolerance, description + " z");
	}

	/** A smooth acceleration function evaluated at each Runge-Kutta stage. */
	@FunctionalInterface
	private interface AccelerationModel {
		CoordinateIF calculate(SimulationStatus status);
	}

	/** RK4 test adapter that preserves the production integration machinery. */
	private static class DeterministicRK4Stepper extends RK4SimulationStepper {
		private final AccelerationModel accelerationModel;

		DeterministicRK4Stepper(AccelerationModel accelerationModel) {
			this.accelerationModel = accelerationModel;
		}

		@Override
		void calculateAcceleration(SimulationStatus status, DataStore store) {
			setDeterministicAcceleration(status, store, accelerationModel);
		}
	}

	/** RK6 test adapter that preserves the production integration machinery. */
	private static class DeterministicRK6Stepper extends RK6SimulationStepper {
		private final AccelerationModel accelerationModel;

		DeterministicRK6Stepper(AccelerationModel accelerationModel) {
			this.accelerationModel = accelerationModel;
		}

		@Override
		void calculateAcceleration(SimulationStatus status, DataStore store) {
			setDeterministicAcceleration(status, store, accelerationModel);
		}
	}

	/** Populate the fields consumed by timestep selection and flight-data storage. */
	private static void setDeterministicAcceleration(SimulationStatus status,
			AbstractSimulationStepper.DataStore store,
			AccelerationModel accelerationModel) {
		CoordinateIF acceleration = accelerationModel.calculate(status);
		Quaternion orientation = status.getRocketOrientationQuaternion();
		store.accelerationData = new AccelerationData(null, null, acceleration, Coordinate.ZERO, orientation);
		store.gravity = 0.0;
		store.thrustForce = 0.0;
		store.dragForce = 0.0;
		store.coriolisAcceleration = Coordinate.ZERO;
	}
}
