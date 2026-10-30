package info.openrocket.swing.gui.figure3d.animation;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.SimulationStatus;
import info.openrocket.core.simulation.listeners.AbstractSimulationListener;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.Quaternion;
import info.openrocket.core.util.TestRockets;
import info.openrocket.swing.util.BaseTestCase;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Checks the replayed attitude against the orientation the simulator actually flew. */
class FlightPoseProviderSimulationTest extends BaseTestCase {
	// The unposed rocket lies along the engine's X axis with its nose toward -X.
	private static final Vector3f NOSE = new Vector3f(-1, 0, 0);

	@Test
	void rebuildsTheSimulatedAttitudeFromTheRecordedAngles() throws Exception {
		// Canted fins spin the rocket and a tilted rod and wind make it fly off vertical, so
		// both the pointing and the roll are exercised.
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		for (RocketComponent component : rocket) {
			if (component instanceof FinSet fins) {
				fins.setCantAngle(Math.toRadians(3.0));
			}
		}
		Simulation simulation = new Simulation(rocket);
		simulation.setFlightConfigurationId(TestRockets.TEST_FCID_0);
		simulation.getOptions().setISAAtmosphere(true);
		simulation.getOptions().setTimeStep(0.01);
		simulation.getOptions().setLaunchRodAngle(Math.toRadians(10.0));
		simulation.getOptions().setLaunchRodDirection(Math.toRadians(120.0));
		simulation.getOptions().setWindSpeedAverage(4.0);
		simulation.getOptions().setRandomSeed(0xC0FFEE);

		Map<Double, Quaternion> flown = new HashMap<>();
		simulation.simulate(new AbstractSimulationListener() {
			@Override
			public void postStep(SimulationStatus status) {
				flown.put(status.getSimulationTime(), status.getRocketOrientationQuaternion().clone());
			}
		});

		FlightDataBranch branch = simulation.getSimulatedData().getBranch(0);
		FlightPoseProvider provider = FlightPoseProvider.fromFlightDataBranch(branch);
		List<Double> times = branch.get(FlightDataType.TYPE_TIME);
		List<Double> theta = branch.get(FlightDataType.TYPE_ORIENTATION_THETA);
		int compared = 0;
		for (int i = 0; i < times.size(); i++) {
			Quaternion attitude = flown.get(times.get(i));
			// Pointing straight down leaves the roll undefined.
			if (attitude == null || theta.get(i) < Math.toRadians(-80.0)) {
				continue;
			}
			Quaternionf replayed = provider.getOrientation(times.get(i));
			// The simulator's nose is its body Z axis; its body X axis stands in for a fin.
			assertSameDirection(engine(attitude.rotate(new Coordinate(0, 0, 1))),
					replayed.transform(new Vector3f(NOSE)), "nose", times.get(i));
			assertSameDirection(engine(attitude.rotate(new Coordinate(1, 0, 0))),
					replayed.transform(new Vector3f(0, 1, 0)), "fin", times.get(i));
			compared++;
		}
		assertTrue(compared > 100, "Only " + compared + " samples could be compared");
	}

	/** A simulator direction (east, north, up) in engine axes. */
	private static Vector3f engine(CoordinateIF direction) {
		return new Vector3f((float) direction.getX(), (float) direction.getZ(), (float) -direction.getY());
	}

	private static void assertSameDirection(Vector3f expected, Vector3f actual, String what, double time) {
		float angle = expected.angle(actual);
		assertTrue(angle < 1.0e-3f,
				"The replayed " + what + " is " + Math.toDegrees(angle) + "° off at t=" + time
						+ ": expected " + expected + " but was " + actual);
	}
}
