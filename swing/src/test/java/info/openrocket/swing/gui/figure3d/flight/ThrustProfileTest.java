package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThrustProfileTest {
	@Test
	void measuresThrustAgainstTheBurnAverage() {
		// An ignition spike to 30 N, a 10 N sustain and a tail-off to zero over a 2 s burn.
		FlightDataBranch branch = new FlightDataBranch("burn", FlightDataType.TYPE_TIME, FlightDataType.TYPE_THRUST_FORCE);
		double[][] curve = { { 0.0, 0.0 }, { 0.1, 30.0 }, { 0.3, 10.0 }, { 1.7, 10.0 }, { 2.0, 0.0 } };
		for (double[] point : curve) {
			branch.addPoint();
			branch.setValue(FlightDataType.TYPE_TIME, point[0]);
			branch.setValue(FlightDataType.TYPE_THRUST_FORCE, point[1]);
		}
		ThrustProfile profile = ThrustProfile.fromBranch(branch, List.of(new FlightReplayData.BurnInterval(0.0, 2.0)));

		assertTrue(profile.relativeThrustAt(0.1) > 2.0, "The ignition spike is well above average");
		assertEquals(1.0, profile.relativeThrustAt(1.0), 0.15, "The sustain is about average");
		assertTrue(profile.relativeThrustAt(1.95) < 0.3, "The tail-off dies down");
		assertEquals(1.0, profile.relativeThrustAt(5.0), "Outside a burn nothing is scaled");
	}

	@Test
	void withoutThrustDataEveryBurnLooksAverage() {
		FlightDataBranch branch = new FlightDataBranch("no thrust", FlightDataType.TYPE_TIME);
		branch.addPoint();
		branch.setValue(FlightDataType.TYPE_TIME, 0.0);

		assertEquals(1.0, ThrustProfile.fromBranch(branch, List.of(new FlightReplayData.BurnInterval(0.0, 1.0)))
				.relativeThrustAt(0.5));
		assertEquals(1.0, ThrustProfile.fromBranch(null, List.of()).relativeThrustAt(0.5));
	}
}
