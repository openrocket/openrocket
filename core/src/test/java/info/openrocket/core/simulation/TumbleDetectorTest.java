package info.openrocket.core.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.logging.SimulationAbort;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.TestRockets;
import org.junit.jupiter.api.Test;

/**
 * Tests for kinematic tumble detection.
 * <p>
 * The unit tests drive {@link TumbleDetector} directly, so that the properties
 * being asserted -- what the detector does at low speed, on descent, and under a
 * transient excursion -- are exercised deterministically rather than inferred
 * from a whole flight. The integration tests then confirm the behaviour that
 * issue #3183 is about, on a real airframe in a real crosswind.
 */
public class TumbleDetectorTest extends BaseTestCase {

	private static final double SEA_LEVEL_DENSITY = 1.225;

	/** Representative pitch natural frequency for a small model rocket, rad/s. */
	private static final double OMEGA_N = 10.0;

	/** One pitch period at OMEGA_N, s. */
	private static final double PITCH_PERIOD = 2 * Math.PI / OMEGA_N;

	/**
	 * Drive a detector for the given duration at a fixed angle of attack.
	 *
	 * @param detector  the detector to drive
	 * @param aoaDeg    angle of attack to hold, degrees
	 * @param airSpeed  air-relative speed, m/s
	 * @param duration  how long to hold this state, s
	 * @param startTime simulation time at which to start
	 * @return the simulation time after the last step
	 */
	private double drive(TumbleDetector detector, double aoaDeg, double airSpeed,
			double duration, double startTime) {
		final double dt = 0.01;
		double time = startTime;
		for (int i = 0; i < Math.round(duration / dt); i++) {
			time += dt;
			detector.update(time, true, Math.toRadians(aoaDeg), airSpeed, SEA_LEVEL_DENSITY, OMEGA_N);
		}
		return time;
	}

	@Test
	public void testAlignedFlightIsNotTumbling() {
		final TumbleDetector detector = new TumbleDetector();
		drive(detector, 5, 100.0, 5.0, 0.0);

		assertFalse(detector.isTumbling(),
				"a rocket flying along its own axis must never be reported as tumbling");
	}

	@Test
	public void testSustainedHighAOAIsTumbling() {
		final TumbleDetector detector = new TumbleDetector();
		drive(detector, 120, 20.0, 5.0, 0.0);

		assertTrue(detector.isTumbling(),
				"a sustained high angle of attack must be reported as tumbling");
	}

	/**
	 * The regression that issue #3183 is really about: a brief excursion must not
	 * latch the detector. A statically stable rocket recovers from a gust-induced
	 * excursion within a fraction of its pitch period.
	 */
	@Test
	public void testBriefExcursionIsNotTumbling() {
		final TumbleDetector detector = new TumbleDetector();

		double time = drive(detector, 2, 60.0, 1.0, 0.0);
		// The excursion lasts a tenth of a pitch period.
		time = drive(detector, 150, 60.0, PITCH_PERIOD / 10, time);
		drive(detector, 2, 60.0, 1.0, time);

		assertFalse(detector.isTumbling(),
				"a transient excursion shorter than a pitch period must not trigger tumbling");
	}

	/**
	 * The measurement that motivates filtering rather than thresholding: a single
	 * step above the stall angle, which a turbulence sample can produce on its own,
	 * must not be enough.
	 */
	@Test
	public void testSingleStepSpikeIsNotTumbling() {
		final TumbleDetector detector = new TumbleDetector();

		double time = drive(detector, 3, 20.0, 1.0, 0.0);
		time = drive(detector, 179, 20.0, 0.01, time);

		assertFalse(detector.isTumbling(),
				"one integration step at a high angle of attack must not trigger tumbling");
	}

	/**
	 * The case raised in review: tumbling must still be detected on the way down,
	 * for rockets that recover by tumbling rather than under a parachute. The
	 * detector must not be gated on flight phase.
	 */
	@Test
	public void testDescentTumblingIsDetected() {
		final TumbleDetector detector = new TumbleDetector();
		// Descending at 15 m/s broadside to the airflow.
		drive(detector, 95, 15.0, 5.0, 0.0);

		assertTrue(detector.isTumbling(),
				"descent tumbling must be detected; the criterion must not be gated on flight phase");
	}

	/**
	 * Near apogee the velocity direction is ill-conditioned. The detector must hold
	 * its state rather than accumulate noise, and must not reset what it learned
	 * before apogee.
	 */
	@Test
	public void testStateIsHeldWhileAirflowIsNegligible() {
		final TumbleDetector detector = new TumbleDetector();
		drive(detector, 120, 20.0, 5.0, 0.0);
		assertTrue(detector.isTumbling(), "precondition: detector has latched onto tumbling");

		final double before = detector.getFilteredAOA();
		// Below the dynamic pressure gate: ~1.3 m/s at sea level.
		drive(detector, 0, 0.5, 2.0, 5.0);

		assertEquals(before, detector.getFilteredAOA(), 1e-12,
				"filter state must be held, not updated, while the airflow direction is meaningless");
		assertTrue(detector.isTumbling(),
				"passing through apogee must not discard evidence accumulated before it");
	}

	@Test
	public void testCopyPreservesFilterState() {
		final TumbleDetector detector = new TumbleDetector();
		drive(detector, 120, 20.0, 5.0, 0.0);

		final TumbleDetector copy = new TumbleDetector(detector);

		assertEquals(detector.getFilteredAOA(), copy.getFilteredAOA(), 1e-12,
				"a branch created at stage separation must inherit its parent's filter state");
		assertEquals(detector.isTumbling(), copy.isTumbling());
	}

	// ---------------------------------------------------------------- integration

	/**
	 * Issue #3183. A steady 6 m/s crosswind -- no gust, no turbulence, nothing
	 * random anywhere in the scenario -- aborted every run under the previous
	 * criterion. The wind is supplied by a listener rather than by the bundled pink
	 * noise model, because that model is seeded when the options object is built and
	 * {@code setRandomSeed} does not re-seed it, so turbulent runs are not
	 * reproducible.
	 */
	@Test
	public void testSteadyCrosswindDoesNotAbort() throws SimulationException {
		final Simulation simulation = crosswindSimulation(TestRockets.makeEstesAlphaIII(), 6.0);
		simulation.simulate(DeterministicWind.steady(6.0));

		final FlightDataBranch branch = simulation.getSimulatedData().getBranch(0);
		assertNotNull(branch);

		for (FlightEvent event : branch.getEvents()) {
			if (event.getType() == FlightEvent.Type.SIM_ABORT) {
				final SimulationAbort abort = (SimulationAbort) event.getData();
				assertFalse(abort.getCause() == SimulationAbort.Cause.TUMBLE_UNDER_THRUST,
						"a stable rocket in a steady crosswind must not abort as tumbling under thrust");
			}
		}
	}

	/**
	 * The recovered flights must be healthy ones, not aborts traded for nonsense
	 * trajectories: the 6 m/s flight must reach an apogee close to the still-air one.
	 */
	@Test
	public void testRecoveredFlightReachesExpectedApogee() throws SimulationException {
		final Simulation calm = crosswindSimulation(TestRockets.makeEstesAlphaIII(), 0.0);
		calm.simulate(DeterministicWind.steady(0.0));
		final double calmApogee = calm.getSimulatedData().getBranch(0)
				.getMaximum(FlightDataType.TYPE_ALTITUDE);

		final Simulation windy = crosswindSimulation(TestRockets.makeEstesAlphaIII(), 6.0);
		windy.simulate(DeterministicWind.steady(6.0));
		final double windyApogee = windy.getSimulatedData().getBranch(0)
				.getMaximum(FlightDataType.TYPE_ALTITUDE);

		assertTrue(Math.abs(windyApogee - calmApogee) < 0.10 * calmApogee,
				"recovered flight apogee " + windyApogee + " m should be close to the "
						+ calmApogee + " m reached in still air");
	}

	/**
	 * The complementary guarantee: removing the fins makes the rocket genuinely
	 * unstable, and that must still be detected.
	 */
	@Test
	public void testUnstableRocketIsStillDetected() throws SimulationException {
		final Rocket rocket = TestRockets.makeEstesAlphaIII();
		removeFins(rocket);

		final Simulation simulation = crosswindSimulation(rocket, 2.0);
		simulation.simulate(DeterministicWind.steady(2.0));

		final FlightDataBranch branch = simulation.getSimulatedData().getBranch(0);
		assertNotNull(branch);

		boolean detected = false;
		for (FlightEvent event : branch.getEvents()) {
			if (event.getType() == FlightEvent.Type.TUMBLE) {
				detected = true;
			}
			if (event.getType() == FlightEvent.Type.SIM_ABORT
					&& ((SimulationAbort) event.getData()).getCause() == SimulationAbort.Cause.TUMBLE_UNDER_THRUST) {
				detected = true;
			}
		}

		assertTrue(detected, "a finless rocket must still be detected as tumbling");
	}

	private Simulation crosswindSimulation(Rocket rocket, double windSpeed) {
		final Simulation simulation = new Simulation(rocket);
		simulation.setFlightConfigurationId(TestRockets.TEST_FCID_0);
		simulation.getOptions().setISAAtmosphere(true);
		simulation.getOptions().setTimeStep(0.05);
		simulation.getOptions().setLaunchRodLength(1.0);
		simulation.getOptions().setWindSpeedAverage(windSpeed);
		simulation.getOptions().setWindTurbulenceIntensity(0.0);
		return simulation;
	}

	private void removeFins(RocketComponent component) {
		final List<RocketComponent> children = List.copyOf(component.getChildren());
		for (RocketComponent child : children) {
			if (child instanceof FinSet) {
				component.removeChild(child);
			} else if (child instanceof BodyTube || child instanceof AxialStage) {
				removeFins(child);
			}
		}
	}
}
