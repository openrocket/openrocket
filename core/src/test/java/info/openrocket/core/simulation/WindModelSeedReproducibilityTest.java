package info.openrocket.core.simulation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel;
import info.openrocket.core.models.wind.PinkNoiseWindModel;
import info.openrocket.core.models.wind.WindModel;
import info.openrocket.core.models.wind.WindModelType;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.TestRockets;
import org.junit.jupiter.api.Test;

/**
 * A simulation run twice with the same random seed must produce the same flight.
 * <p>
 * The seed reaches the integrator -- {@code RK4SimulationStepper} builds its
 * {@code Random} from {@code SimulationConditions.getRandomSeed()} -- but it did
 * not reach the wind models, so any flight with wind turbulence was irreproducible.
 * {@code PinkNoiseWindModel} was seeded once when the enclosing
 * {@code SimulationOptions} was constructed and its seed field was final, so
 * {@code setRandomSeed} could not change it; every level of
 * {@code MultiLevelPinkNoiseWindModel} was built with the no-argument constructor
 * and so was never connected to the seed at all.
 */
public class WindModelSeedReproducibilityTest extends BaseTestCase {

	private static final int SEED = 12345;
	private static final int RUNS = 5;

	/** Turbulence has to be non-zero, or there is no randomness to reproduce. */
	private static final double TURBULENCE = 0.2;

	private static final double WIND_SPEED = 5.0;

	/**
	 * Long enough that the rocket leaves the guide fast enough to fly. On a 1 m guide
	 * this airframe departs in this wind for a good fraction of seeds, and the flight
	 * ends a metre off the pad -- which is reproducible too, but would leave the test
	 * asserting the repeatability of an abort rather than of a flight.
	 */
	private static final double LAUNCH_GUIDE_LENGTH = 3.0;

	/** Still-air apogee is around 133 m; a flight well below this one did not fly. */
	private static final double MIN_MEANINGFUL_APOGEE = 100.0;

	/**
	 * Tolerance on "the same flight", in metres.
	 * <p>
	 * Not zero, because the simulator is not bit-reproducible even with no randomness
	 * in play: on unmodified {@code unstable}, repeated runs of this same flight with
	 * the turbulence intensity set to zero still differ by around 5e-9 m. That floor
	 * is unrelated to the seed and is not what this test is about. An unseeded wind
	 * model, by contrast, moves the apogee by of the order of a metre, so this bound
	 * sits some four orders of magnitude below the effect being tested and three
	 * above the noise it has to ignore.
	 */
	private static final double SAME_FLIGHT_TOLERANCE = 1.0e-4;

	@Test
	public void testAverageWindModelIsReproducibleFromSeed() throws SimulationException {
		assertReproducible(WindModelType.AVERAGE);
	}

	@Test
	public void testMultiLevelWindModelIsReproducibleFromSeed() throws SimulationException {
		assertReproducible(WindModelType.MULTI_LEVEL);
	}

	private void assertReproducible(WindModelType type) throws SimulationException {
		final double reference = apogee(type);

		// Guard the fixture, not the fix: an aborted flight is reproducible as readily
		// as a real one, so without this the test could keep passing while measuring
		// nothing but how repeatably the rocket falls over.
		assertTrue(reference > MIN_MEANINGFUL_APOGEE,
				"fixture must produce a real flight, but apogee was " + reference + " m");

		for (int i = 1; i < RUNS; i++) {
			assertEquals(reference, apogee(type), SAME_FLIGHT_TOLERANCE,
					"run " + i + " with seed " + SEED + " must reproduce the first run");
		}
	}

	/**
	 * The seed must reach the wind itself, checked on the models directly.
	 * <p>
	 * Deliberately not expressed as "different seeds give different flights": the
	 * integrator draws on the same seed, so two flights differ under two seeds even
	 * when the wind is identical, and a model that ignored the value it was given
	 * would pass such a test. Sampling the models removes the integrator from the
	 * measurement, leaving only the property being asserted.
	 */
	@Test
	public void testSeedControlsTheWindItself() {
		for (WindModel model : new WindModel[] { averageModel(), multiLevelModel() }) {
			final String name = model.getClass().getSimpleName();

			model.setSeed(SEED);
			final double[] first = sample(model);

			model.setSeed(SEED + 1);
			final double[] other = sample(model);

			model.setSeed(SEED);
			final double[] repeat = sample(model);

			assertArrayEquals(first, repeat, 0.0,
					name + ": the same seed must reproduce the same wind exactly");
			assertFalse(Arrays.equals(first, other),
					name + ": a different seed must produce different wind, but the samples "
							+ "were identical -- the seed is being ignored");
		}
	}

	/** Wind speed sampled over the first seconds of a flight. */
	private double[] sample(WindModel model) {
		final double[] out = new double[40];
		for (int i = 0; i < out.length; i++) {
			out[i] = model.getWindVelocity(i * 0.1, 50.0).length();
		}
		return out;
	}

	private PinkNoiseWindModel averageModel() {
		final PinkNoiseWindModel model = new PinkNoiseWindModel();
		model.setAverage(WIND_SPEED);
		model.setStandardDeviation(WIND_SPEED * TURBULENCE);
		return model;
	}

	private MultiLevelPinkNoiseWindModel multiLevelModel() {
		final MultiLevelPinkNoiseWindModel model = new MultiLevelPinkNoiseWindModel();
		model.clearLevels();
		model.addWindLevel(0, WIND_SPEED, 0, WIND_SPEED * TURBULENCE);
		model.addWindLevel(200, WIND_SPEED * 1.5, 0, WIND_SPEED * TURBULENCE);
		return model;
	}

	private double apogee(WindModelType type) throws SimulationException {
		return apogee(type, SEED);
	}

	private double apogee(WindModelType type, int seed) throws SimulationException {
		final Rocket rocket = TestRockets.makeEstesAlphaIII();
		final Simulation sim = new Simulation(rocket);
		sim.setFlightConfigurationId(TestRockets.TEST_FCID_0);

		final SimulationOptions options = sim.getOptions();
		options.setISAAtmosphere(true);
		options.setTimeStep(0.05);
		options.setLaunchRodLength(LAUNCH_GUIDE_LENGTH);
		options.setWindModelType(type);

		if (type == WindModelType.AVERAGE) {
			options.setWindSpeedAverage(WIND_SPEED);
			options.setWindTurbulenceIntensity(TURBULENCE);
		} else {
			final MultiLevelPinkNoiseWindModel model = options.getMultiLevelWindModel();
			model.clearLevels();
			model.addWindLevel(0, WIND_SPEED, 0, WIND_SPEED * TURBULENCE);
			model.addWindLevel(200, WIND_SPEED * 1.5, 0, WIND_SPEED * TURBULENCE);
		}

		options.setRandomSeed(seed);
		sim.simulate();

		return sim.getSimulatedData().getBranch(0).getMaximum(FlightDataType.TYPE_ALTITUDE);
	}
}
