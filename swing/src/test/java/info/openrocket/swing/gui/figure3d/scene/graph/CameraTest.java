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
}
