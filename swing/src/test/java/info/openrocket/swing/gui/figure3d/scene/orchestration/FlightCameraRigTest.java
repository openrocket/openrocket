package info.openrocket.swing.gui.figure3d.scene.orchestration;

import info.openrocket.swing.gui.figure3d.scene.graph.Camera;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlightCameraRigTest {
	@Test
	void followCameraPreservesPanWhenAdvancingAndScrubbingBackward() {
		Camera camera = Camera.builder().withFixedCenterOfInterest(false).build();
		Vector3f launch = new Vector3f(0.0f, 1.0f, 0.0f);
		Vector3f apogee = new Vector3f(15.0f, 100.0f, -20.0f);
		Vector3f pan = new Vector3f(3.0f, 0.0f, 0.0f);
		camera.setCenterOfInterest(new Vector3f(launch).add(pan));
		FlightCameraRig.trackFlightPivot(camera, launch, apogee);
		assertEquals(new Vector3f(apogee).add(pan), camera.getCenterOfInterest());
		FlightCameraRig.trackFlightPivot(camera, apogee, launch);
		assertEquals(new Vector3f(launch).add(pan), camera.getCenterOfInterest());
	}
	@Test
	void padCameraStaysAtThePadEyeWhileAimingAtTheRocket() {
		Camera camera = Camera.builder().withFixedCenterOfInterest(false).build();
		Vector3f eye = new Vector3f(20.0f, 4.0f, 20.0f);
		for (float altitude : new float[] { 1.0f, 50.0f, 2_000.0f }) {
			Vector3f rocket = new Vector3f(3.0f, altitude, -2.0f);
			float distance = FlightCameraRig.lookFrom(camera, eye, rocket);
			camera.update();

			assertEquals(eye.distance(rocket), distance, distance * 1e-5f);
			assertEquals(0.0f, camera.getPosition().distance(eye), distance * 1e-4f,
					"The pad camera must not move at altitude " + altitude);
			assertEquals(rocket, camera.getEffectiveLookAt());
		}
	}

	@Test
	void telephotoLensKeepsTheRocketAtItsLaunchSizeOnScreen() {
		float baseFov = (float) Math.toRadians(45.0);
		float launchDistance = 100.0f;
		float launchTan = FlightCameraRig.telephotoTanHalfFieldOfView(baseFov, launchDistance, launchDistance);
		assertEquals(Math.tan(baseFov / 2.0), launchTan, 1e-6);
		// On-screen size is proportional to 1 / (distance * tan(fov / 2)).
		for (float distance : new float[] { 400.0f, 4_000.0f, 40_000.0f }) {
			float tan = FlightCameraRig.telephotoTanHalfFieldOfView(baseFov, launchDistance, distance);
			assertEquals(launchDistance * launchTan, distance * tan, 1e-2f);
		}
		// Closer than at launch (e.g. landing near the pad) keeps the normal lens.
		assertEquals(launchTan, FlightCameraRig.telephotoTanHalfFieldOfView(baseFov, launchDistance, 30.0f));
	}

	@Test
	void padWheelZoomScalesTheLensInsteadOfMovingTheEye() {
		Camera camera = Camera.builder().withFixedCenterOfInterest(false).build();
		Vector3f eye = new Vector3f(20.0f, 4.0f, 20.0f);
		float applied = FlightCameraRig.lookFrom(camera, eye, new Vector3f(0.0f, 50.0f, 0.0f));

		camera.dolly(1.0f);
		float zoomIn = FlightCameraRig.updatedPadZoomScale(1.0f, camera.getDistance(), applied);
		assertTrue(zoomIn < 1.0f, "Scrolling in must narrow the lens");
		assertEquals(1.0f, FlightCameraRig.updatedPadZoomScale(1.0f, applied, applied));
		assertEquals(1.0f, FlightCameraRig.updatedPadZoomScale(1.0f, camera.getDistance(), Float.NaN),
				"A reset must not read the fitted distance as a zoom");
	}

	@Test
	void padCameraDropsAPanOffsetLeftFromAnotherView() {
		Camera camera = Camera.builder().withFixedCenterOfInterest(false).build();
		camera.pan(40.0f, 30.0f, 800, 600);
		Vector3f rocket = new Vector3f(0.0f, 50.0f, 0.0f);

		FlightCameraRig.lookFrom(camera, new Vector3f(20.0f, 4.0f, 20.0f), rocket);

		assertEquals(rocket, camera.getEffectiveLookAt(), "The pad view must aim exactly at the rocket");
	}
}
