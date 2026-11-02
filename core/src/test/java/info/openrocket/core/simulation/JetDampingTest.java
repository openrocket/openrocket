package info.openrocket.core.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.TreeMap;

import info.openrocket.core.aerodynamics.AerodynamicForces;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.listeners.AbstractSimulationListener;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.MutableCoordinate;
import info.openrocket.core.util.TestRockets;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Verifies the jet damping moment in the rigid-body dynamics. With the aerodynamic forces removed, the only
 * moment acting on the rocket is jet damping, so a lateral rotation rate obeys I * dw/dt = -C2R * w, with
 * C2R = mdot * (x_nozzle - x_cg)^2. The rate must therefore decay as w0 * exp(-integral(C2R / I dt)) during
 * the burn and stay constant once the motor has burned out.
 */
public class JetDampingTest extends BaseTestCase {

	private static final double INITIAL_PITCH_RATE = 0.5;	// rad/s

	@ParameterizedTest
	@EnumSource(SimulationStepperMethod.class)
	public void testJetDampingDecaysPitchRate(SimulationStepperMethod stepperMethod) throws SimulationException {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		Simulation simulation = new Simulation(rocket);
		simulation.setFlightConfigurationId(TestRockets.TEST_FCID_0);
		simulation.getOptions().setISAAtmosphere(true);
		simulation.getOptions().setTimeStep(0.001);
		simulation.getOptions().setSimulationStepperMethodChoice(stepperMethod);

		PitchRateRecorder recorder = new PitchRateRecorder();
		simulation.simulate(recorder);

		// Nozzle exit position from the motor mount geometry
		MotorMount mount = null;
		for (RocketComponent component : rocket) {
			if (component instanceof MotorMount m && m.isMotorMount()) {
				mount = m;
			}
		}
		CoordinateIF nozzleExit = new Coordinate(mount.getLength() + mount.getMotorOverhang());
		double nozzleX = ((RocketComponent) mount).toAbsolute(nozzleExit)[0].getX();

		FlightDataBranch branch = simulation.getSimulatedData().getBranch(0);
		List<Double> time = branch.getView(FlightDataType.TYPE_TIME);
		List<Double> motorMass = branch.getView(FlightDataType.TYPE_MOTOR_MASS);
		List<Double> cg = branch.getView(FlightDataType.TYPE_CG_LOCATION);
		List<Double> inertia = branch.getView(FlightDataType.TYPE_LONGITUDINAL_INERTIA);

		// Integrate C2R / I over each sample interval: the expelled mass (m_k - m_k+1) times the mean arm^2 / I.
		double expectedLogRatio = 0;
		int burnSamples = 0;
		int coastSamples = 0;
		for (int k = 0; k + 1 < time.size(); k++) {
			double t1 = time.get(k + 1);
			if (time.get(k) < recorder.startTime[0] - 1.0e-9) {
				continue;
			}
			Double rate = recorder.rates.get(t1);
			if (rate == null) {
				break;	// past apogee
			}

			double expelledMass = motorMass.get(k) - motorMass.get(k + 1);
			double armOverInertia0 = MathUtil.pow2(nozzleX - cg.get(k)) / inertia.get(k);
			double armOverInertia1 = MathUtil.pow2(nozzleX - cg.get(k + 1)) / inertia.get(k + 1);
			expectedLogRatio -= expelledMass * 0.5 * (armOverInertia0 + armOverInertia1);

			double actualLogRatio = Math.log(rate / INITIAL_PITCH_RATE);
			assertEquals(expectedLogRatio, actualLogRatio, 1.0e-3 * Math.abs(expectedLogRatio) + 1.0e-6,
					"Pitch rate at t=" + t1);
			if (expelledMass > 0) {
				burnSamples++;
			} else {
				coastSamples++;
			}
		}

		// The A8 burns well past launch rod clearance, so the rate must have decayed noticeably during the burn
		// and then held constant during the coast.
		assertTrue(burnSamples > 10, "Expected samples during the burn");
		assertTrue(coastSamples > 10, "Expected samples during the coast");
		assertTrue(expectedLogRatio < -0.01, "Expected a measurable jet damping effect, got " + expectedLogRatio);
	}

	/**
	 * Removes the aerodynamic forces, applies a lateral rotation rate once the launch rod is cleared, and records
	 * the lateral body rotation rate after every step until apogee.
	 */
	private static class PitchRateRecorder extends AbstractSimulationListener {
		final TreeMap<Double, Double> rates = new TreeMap<>();
		// Arrays and maps are shared with the clone that the simulation runs
		final double[] startTime = { Double.NaN };

		@Override
		public AerodynamicForces postAerodynamicCalculation(SimulationStatus status, AerodynamicForces forces) {
			return new AerodynamicForces().zero();
		}

		@Override
		public void postStep(SimulationStatus status) {
			if (!status.isLaunchRodCleared() || status.isApogeeReached()) {
				return;
			}
			if (Double.isNaN(startTime[0])) {
				startTime[0] = status.getSimulationTime();
				status.setRocketRotationVelocity(
						status.getRocketOrientationQuaternion().rotate(new Coordinate(INITIAL_PITCH_RATE, 0, 0)));
				return;
			}

			MutableCoordinate rotation = new MutableCoordinate();
			rotation.set(status.getRocketRotationVelocity());
			status.getRocketOrientationQuaternion().invRotateInPlace(rotation);
			rates.put(status.getSimulationTime(), MathUtil.hypot(rotation.getX(), rotation.getY()));
		}
	}
}
