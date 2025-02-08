package info.openrocket.core.document;

import static org.junit.jupiter.api.Assertions.*;

import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.TestRockets;
import info.openrocket.core.logging.SimulationAbort;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collections;
import java.util.List;

public class SimulationTest extends BaseTestCase {
	private static final double EPSILON = 0.0001;

	private Rocket rocket;
	private Simulation simulation;

	@BeforeEach
	public void setUpSim() {
		rocket = TestRockets.makeEstesAlphaIII();
		simulation = new Simulation(rocket);
		simulation.setFlightConfigurationId(TestRockets.TEST_FCID_0);
		simulation.getOptions().setISAAtmosphere(true);
		simulation.getOptions().setTimeStep(0.05);
	}

	@Test
	public void testBasicSimulationCreation() {
		assertNotNull(simulation);
		assertEquals(rocket, simulation.getRocket());
		assertEquals(Simulation.Status.NOT_SIMULATED, simulation.getStatus());
		assertEquals("", simulation.getName());
	}

	@Test
	public void testSimulationName() {
		String testName = "Test Flight #1";
		simulation.setName(testName);
		assertEquals(testName, simulation.getName());

		simulation.setName(null);
		assertEquals("", simulation.getName());
	}

	@Test
	public void testSimulationCopy() {
		simulation.setName("Original Sim");
		simulation.getOptions().setLaunchRodLength(2.0);
		simulation.getOptions().setLaunchRodAngle(45.0);

		Simulation copy = simulation.copy();

		assertNotNull(copy);
		assertEquals(simulation.getName(), copy.getName());
		assertEquals(simulation.getOptions().getLaunchRodLength(),
				copy.getOptions().getLaunchRodLength(),
				EPSILON);
		assertEquals(simulation.getOptions().getLaunchRodAngle(),
				copy.getOptions().getLaunchRodAngle(),
				EPSILON);
		assertEquals(Simulation.Status.NOT_SIMULATED, copy.getStatus());

		// Verify copy is independent
		copy.setName("Modified Copy");
		assertNotEquals(simulation.getName(), copy.getName());
	}

	@Test
	public void testSimulationWithNoMotors() throws SimulationException {
		// Create configuration without motors
		FlightConfiguration config = rocket.getFlightConfiguration(TestRockets.TEST_FCID_0);
		config.clearAllMotors();

		simulation.simulate();

		// Verify simulation aborted due to no motors
		FlightData data = simulation.getSimulatedData();
		FlightEvent abort = data.getBranch(0).getLastEvent(FlightEvent.Type.SIM_ABORT);
		assertNotNull(abort, "Simulation without motors should abort");
		assertEquals(SimulationAbort.Cause.NO_MOTORS_DEFINED,
				((SimulationAbort)abort.getData()).getCause());
	}

	@Test
	public void testBasicSimulationExecution() throws SimulationException {
		simulation.simulate();

		FlightData data = simulation.getSimulatedData();
		assertNotNull(data, "Simulation data should not be null");
		assertTrue(data.getMaxAltitude() > 0, "Max altitude should be positive");
		assertTrue(data.getMaxVelocity() > 0, "Max velocity should be positive");
		assertTrue(data.getFlightTime() > 0, "Flight time should be positive");
		assertEquals(Simulation.Status.UPTODATE, simulation.getStatus());
	}

	@Test
	public void testConfigurationManagement() {
		FlightConfigurationId newId = new FlightConfigurationId();
		simulation.setFlightConfigurationId(newId);
		assertEquals(newId, simulation.getFlightConfigurationId());

		FlightConfiguration config = simulation.getActiveConfiguration();
		assertNotNull(config);
		assertEquals(newId, config.getFlightConfigurationID());
	}

	@Test
	public void testOptionsModification() {
		SimulationOptions options = simulation.getOptions();

		// Modify some options
		double rodLength = 2.0;
		double rodAngle = 4 * Math.PI / 13;

		options.setLaunchRodLength(rodLength);
		options.setLaunchRodAngle(rodAngle);

		// Verify changes are reflected
		assertEquals(rodLength, options.getLaunchRodLength(), EPSILON);
		assertEquals(rodAngle, options.getLaunchRodAngle(), EPSILON);

		// Status should be outdated after options change
		assertEquals(Simulation.Status.NOT_SIMULATED, simulation.getStatus());
	}

	@Test
	public void testAltitudeAboveSeaLevel() throws SimulationException {
		double launchAltitude = 123;
		simulation.getOptions().setLaunchAltitude(launchAltitude);

		simulation.simulate();

		FlightData flightData =  simulation.getSimulatedData();
		FlightDataBranch branch = flightData.getBranch(0);

		List<Double> altitudeData = branch.get(FlightDataType.TYPE_ALTITUDE);
		List<Double> altitudeASLData = branch.get(FlightDataType.TYPE_ALTITUDE_ABOVE_SEA);

		assertNotNull(altitudeData);
		assertNotNull(altitudeASLData);
		assertEquals(altitudeData.size(), altitudeASLData.size());
		assertFalse(altitudeData.isEmpty());

		// Verify that altitude above sea level = altitude + launch altitude for each data point
		for (int i = 0; i < altitudeData.size(); i++) {
			double altitude = altitudeData.get(i);
			double altitudeASL = altitudeASLData.get(i);
			assertEquals(altitude + launchAltitude, altitudeASL, 0.001,
					"Altitude above sea level should equal altitude + launch altitude at index " + i);
		}

		// Additionally verify max altitudes
		double maxAltitude = Collections.max(altitudeData);
		double maxAltitudeASL = Collections.max(altitudeASLData);
		assertEquals(maxAltitude + launchAltitude, maxAltitudeASL, 0.001,
				"Maximum altitude above sea level should equal maximum altitude + launch altitude");
	}
}
