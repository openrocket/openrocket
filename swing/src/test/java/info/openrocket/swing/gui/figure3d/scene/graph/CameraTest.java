package info.openrocket.swing.gui.figure3d.scene.graph;

import info.openrocket.swing.gui.figure3d.constants.CameraConstants;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CameraTest {

	@Test
	void getPositionReturnsDefensiveCopy() {
		Camera camera = Camera.builder().build();

		Vector3f position = camera.getPosition();
		float originalX = position.x;
		position.x += 10.0f;

		assertEquals(originalX, camera.getPosition().x);
	}

	@Test
	void fitBoundsUpdatesMinimumZoomForSmallBounds() {
		Camera camera = Camera.builder().build();
		camera.setZoomLimits(CameraConstants.DEFAULT_MIN_ZOOM, CameraConstants.DEFAULT_MAX_ZOOM);

		camera.fitBounds(new Vector3f(0.01f, 0.01f, 0.01f));

		assertTrue(camera.getDistance() < CameraConstants.DEFAULT_MIN_ZOOM,
				"Small fitted bounds should not be clamped by the stale default minimum zoom");
	}

	@Test
	void fitBoundsAllowsZoomingOutToFivePercentScale() {
		Camera camera = Camera.builder().build();
		camera.fitBounds(new Vector3f(10.0f, 2.0f, 2.0f));
		float fittedDistance = camera.getDistance();

		camera.setDistance(Float.MAX_VALUE);

		assertEquals(fittedDistance * 20.0f, camera.getDistance(), fittedDistance * 0.001f);
		assertEquals(0.05f, fittedDistance / camera.getDistance(), 0.0001f,
				"The 3D camera should support the zoom selector's smaller scales");
	}

	@Test
	void fittedDistanceDoesNotDependOnYaw() {
		Camera camera = Camera.builder().build();
		Vector3f dimensions = new Vector3f(10.0f, 2.0f, 1.0f);
		camera.setAngleY(0.35f);

		camera.setAngleX(0.0f);
		camera.update();
		camera.fitBounds(dimensions);
		float sideDistance = camera.getDistance();

		camera.setAngleX((float) Math.PI / 2.0f);
		camera.update();
		camera.fitBounds(dimensions);
		float endDistance = camera.getDistance();

		camera.setAngleX((float) Math.PI / 4.0f);
		camera.update();
		camera.fitBounds(dimensions);

		assertEquals(sideDistance, endDistance, sideDistance * 0.0001f);
		assertEquals(sideDistance, camera.getDistance(), sideDistance * 0.0001f);
	}

	@Test
	void wheelZoomUsesTheSameRatioAtEveryDistance() {
		Camera camera = Camera.builder().build();
		camera.setZoomLimits(0.1f, 10_000.0f);

		camera.setDistance(1_000.0f);
		camera.dolly(1.0f);
		float distantRatio = camera.getDistance() / 1_000.0f;

		camera.setDistance(10.0f);
		camera.dolly(1.0f);
		float closeRatio = camera.getDistance() / 10.0f;

		assertEquals(distantRatio, closeRatio, 1e-6f);
		assertTrue(distantRatio < 0.95f, "One wheel notch should make a visible zoom change");
	}

	@Test
	void orbitCanCrossVerticalPolesByDefault() {
		Camera camera = Camera.builder().build();
		camera.setSideView();

		camera.orbit(0.0f, (float) Math.toRadians(100.0), 1.0f);
		camera.update();

		assertEquals(Math.toRadians(100.0), camera.getAngleY(), 0.0001);
		assertTrue(camera.getPosition().z < 0.0f,
				"Crossing 90 degrees should move the camera beyond the vertical pole");
		assertTrue(camera.getViewMatrix().isFinite(),
				"Crossing a vertical pole should keep the view transform valid");
	}

	@Test
	void orbitHonorsExplicitPitchClamping() {
		Camera camera = Camera.builder().build();
		camera.setSideView();
		camera.setPitchClampingEnabled(true);

		camera.orbit(0.0f, (float) Math.toRadians(100.0), 1.0f);

		assertEquals(CameraConstants.MAX_PITCH_ANGLE, camera.getAngleY(), 0.0001f);
	}

	@Test
	void poseBlendStartsAndEndsExactlyAtItsEndpoints() {
		Camera.Pose from = new Camera.Pose(new Vector3f(0, 0, 0), new Vector3f(), 10.0f, 0.1f, 0.2f, 0.8f, 1.0f, 100.0f);
		Camera.Pose to = new Camera.Pose(new Vector3f(50, 20, -5), new Vector3f(0, 3, 0), 1_000.0f, 0.5f, -0.3f,
				0.1f, 10.0f, 10_000.0f);

		assertEquals(from.centerOfInterest(), Camera.Pose.blend(from, to, 0.0f).centerOfInterest());
		assertEquals(from.distance(), Camera.Pose.blend(from, to, 0.0f).distance(), 1e-3f);
		Camera.Pose end = Camera.Pose.blend(from, to, 1.0f);
		assertEquals(to.centerOfInterest(), end.centerOfInterest());
		assertEquals(to.viewOffset(), end.viewOffset());
		assertEquals(to.distance(), end.distance(), 1e-2f);
		assertEquals(to.angleX(), end.angleX(), 1e-6f);
		assertEquals(to.fieldOfView(), end.fieldOfView(), 1e-6f);
	}

	@Test
	void poseBlendZoomsGeometricallyAndTurnsTheShortWayRound() {
		Camera.Pose from = new Camera.Pose(new Vector3f(), new Vector3f(), 10.0f, (float) Math.toRadians(170.0), 0.0f,
				0.8f, 1.0f, 100.0f);
		Camera.Pose to = new Camera.Pose(new Vector3f(), new Vector3f(), 1_000.0f, (float) Math.toRadians(-170.0),
				0.0f, 0.8f, 1.0f, 100_000.0f);

		Camera.Pose middle = Camera.Pose.blend(from, to, 0.5f);

		assertEquals(100.0f, middle.distance(), 1e-2f, "Halfway between 10 and 1000 in zoom is 100");
		assertEquals(-1.0, Math.cos(middle.angleX()), 1e-4, "170 to -170 degrees passes through 180, not 0");
	}

	@Test
	void poseBlendNeverTakesTheEyeBelowBothEndpoints() {
		// From an eye at head height looking steeply up at a high rocket, to a distant side view.
		Vector3f rocket = new Vector3f(0.0f, 3_000.0f, 0.0f);
		Vector3f padEye = new Vector3f(140.0f, 34.0f, 140.0f);
		Vector3f toPadEye = new Vector3f(padEye).sub(rocket);
		Camera.Pose pad = new Camera.Pose(rocket, new Vector3f(), toPadEye.length(),
				(float) Math.atan2(toPadEye.x, toPadEye.z), (float) Math.asin(toPadEye.y / toPadEye.length()),
				0.05f, 1.0f, 1.0e6f);
		Camera.Pose overview = new Camera.Pose(new Vector3f(0.0f, 1_500.0f, 0.0f), new Vector3f(), 40_000.0f,
				0.3f, 0.05f, 0.8f, 1.0f, 1.0e6f);

		for (float amount = 0.05f; amount < 1.0f; amount += 0.05f) {
			Camera camera = Camera.builder().withFixedCenterOfInterest(false).build();
			camera.restorePose(Camera.Pose.blend(pad, overview, amount));
			camera.update();
			assertTrue(camera.getPosition().y >= 34.0f - 1e-2f,
					"The eye went below the ground-level endpoint at " + amount + ": " + camera.getPosition());
		}
	}

	@Test
	void restoredPoseIsNotClampedToTheCurrentZoomRange() {
		Camera camera = Camera.builder().withFixedCenterOfInterest(false).build();
		camera.setZoomLimits(1.0f, 20.0f);
		Camera.Pose far = new Camera.Pose(new Vector3f(1, 2, 3), new Vector3f(0, 4, 0), 5_000.0f, 0.3f, 0.4f,
				0.5f, 100.0f, 10_000.0f);

		camera.restorePose(far);

		assertEquals(far, camera.capturePose());
	}

	@Test
	void distantSubjectsCanKeepANearPlaneProportionalToTheirDistance() {
		Camera camera = Camera.builder().build();
		camera.setZoomLimits(0.01f, 1.0e6f);
		camera.setDistance(20_000.0f);
		float cappedNear = camera.getProjectionMatrix().perspectiveNear();

		camera.setNearPlaneScalesWithDistance(true);
		float scaledNear = camera.getProjectionMatrix().perspectiveNear();

		assertEquals(CameraConstants.DEFAULT_Z_NEAR, cappedNear, 1e-3f, "The design view keeps its capped near plane");
		assertEquals(20_000.0f * CameraConstants.DYNAMIC_Z_NEAR_DISTANCE_FACTOR, scaledNear, 1.0f);
		camera.setDistance(10.0f);
		assertEquals(10.0f * CameraConstants.DYNAMIC_Z_NEAR_DISTANCE_FACTOR,
				camera.getProjectionMatrix().perspectiveNear(), 1e-3f, "Up close it still follows the distance");
	}
}
