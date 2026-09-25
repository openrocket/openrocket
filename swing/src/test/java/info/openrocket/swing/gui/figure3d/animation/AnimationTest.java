package info.openrocket.swing.gui.figure3d.animation;

import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AnimationTest {
	private static final float EPSILON = 1.0e-4f;

	@Test
	void convertsSimulationAxesAndWorldScale() {
		Vector3f position = SimToWorld.toEngine(2.0f, 3.0f, 4.0f);

		assertEquals(2.0f * RenderingConstants.WORLD_SCALE, position.x, EPSILON);
		assertEquals(4.0f * RenderingConstants.WORLD_SCALE, position.y, EPSILON);
		assertEquals(-3.0f * RenderingConstants.WORLD_SCALE, position.z, EPSILON);
	}

	@Test
	void playbackClockAppliesRateAndClampsAtBothEnds() {
		PlaybackClock clock = new PlaybackClock(2.0, 5.0);
		clock.setRate(2.0);
		clock.update(2.0);
		assertEquals(5.0, clock.getTime());

		clock.setRate(-3.0);
		clock.update(2.0);
		assertEquals(2.0, clock.getTime());

		clock.setTime(4.0);
		assertEquals(4.0, clock.getTime());
	}

	@Test
	void flightPoseInterpolatesPositionAndExplicitOrientation() {
		FlightDataBranch branch = new FlightDataBranch("flight",
				FlightDataType.TYPE_TIME,
				FlightDataType.TYPE_POSITION_X,
				FlightDataType.TYPE_POSITION_Y,
				FlightDataType.TYPE_ALTITUDE,
				FlightDataType.TYPE_ORIENTATION_THETA,
				FlightDataType.TYPE_ORIENTATION_PHI);
		addPoint(branch, 0.0, 0.0, 0.0, 10.0, 0.0, 0.0);
		addPoint(branch, 1.0, 2.0, 4.0, 20.0, Math.PI / 2.0, 0.0);

		FlightPoseProvider provider = FlightPoseProvider.fromFlightDataBranch(branch);
		Vector3f midpoint = provider.getPosition(0.5);
		assertEquals(20.0f, midpoint.x, EPSILON);
		assertEquals(300.0f, midpoint.y, EPSILON);
		assertEquals(-40.0f, midpoint.z, EPSILON);

		Vector3f direction = provider.getOrientation(1.0).transform(new Vector3f(0.0f, 1.0f, 0.0f));
		assertEquals(1.0f, direction.x, EPSILON);
		assertEquals(0.0f, direction.y, EPSILON);
		assertEquals(0.0f, direction.z, EPSILON);
	}

	@Test
	void flightPoseSupportsPolarLateralPositionFallback() {
		FlightDataBranch branch = new FlightDataBranch("flight",
				FlightDataType.TYPE_TIME,
				FlightDataType.TYPE_POSITION_XY,
				FlightDataType.TYPE_POSITION_DIRECTION,
				FlightDataType.TYPE_ALTITUDE);
		branch.addPoint();
		branch.setValue(FlightDataType.TYPE_TIME, 0.0);
		branch.setValue(FlightDataType.TYPE_POSITION_XY, 2.0);
		branch.setValue(FlightDataType.TYPE_POSITION_DIRECTION, Math.PI / 2.0);
		branch.setValue(FlightDataType.TYPE_ALTITUDE, 3.0);

		Vector3f position = FlightPoseProvider.fromFlightDataBranch(branch).getPosition(0.0);
		assertEquals(0.0f, position.x, EPSILON);
		assertEquals(60.0f, position.y, EPSILON);
		assertEquals(-40.0f, position.z, EPSILON);
	}

	@Test
	void flightPoseRejectsMissingLateralPosition() {
		FlightDataBranch branch = new FlightDataBranch("flight",
				FlightDataType.TYPE_TIME, FlightDataType.TYPE_ALTITUDE);
		branch.addPoint();
		branch.setValue(FlightDataType.TYPE_TIME, 0.0);
		branch.setValue(FlightDataType.TYPE_ALTITUDE, 3.0);

		assertThrows(IllegalArgumentException.class,
				() -> FlightPoseProvider.fromFlightDataBranch(branch));
	}

	private static void addPoint(FlightDataBranch branch, double time, double east, double north,
			double altitude, double theta, double phi) {
		branch.addPoint();
		branch.setValue(FlightDataType.TYPE_TIME, time);
		branch.setValue(FlightDataType.TYPE_POSITION_X, east);
		branch.setValue(FlightDataType.TYPE_POSITION_Y, north);
		branch.setValue(FlightDataType.TYPE_ALTITUDE, altitude);
		branch.setValue(FlightDataType.TYPE_ORIENTATION_THETA, theta);
		branch.setValue(FlightDataType.TYPE_ORIENTATION_PHI, phi);
	}
}
