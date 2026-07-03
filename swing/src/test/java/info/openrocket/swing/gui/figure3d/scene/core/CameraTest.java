package info.openrocket.swing.gui.figure3d.scene.core;

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
		Camera camera = Camera.builder()
				.withZoomLimits(CameraConstants.DEFAULT_MIN_ZOOM, CameraConstants.DEFAULT_MAX_ZOOM)
				.build();

		camera.fitBounds(new Vector3f(0.01f, 0.01f, 0.01f));

		assertTrue(camera.getDistance() < CameraConstants.DEFAULT_MIN_ZOOM,
				"Small fitted bounds should not be clamped by the stale default minimum zoom");
	}
}
