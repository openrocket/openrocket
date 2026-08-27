package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.swing.gui.figure3d.scene.graph.Camera;
import info.openrocket.swing.gui.figure3d.scene.graph.Scene;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;

class CameraPointOfInterestPassTest {

	@Test
	void rocketRotationModeUsesScenePivot() {
		Camera camera = Camera.builder()
				.withAspectRatio(1.6f)
				.withFixedCenterOfInterest(false)
				.build();
		Scene scene = new Scene(mock(Rocket.class), camera, new RenderingConfiguration());
		camera.setCenterOfInterest(new Vector3f(5.0f, 0.0f, 0.0f));
		scene.updateRocketPivotFromCamera();
		camera.pan(0.0f, 100.0f, 1000, 500);
		camera.update();

		Vector3f markerPosition = CameraPointOfInterestPass.getMarkerPosition(scene, true);

		assertEquals(scene.getRocketRotationPivot(new Vector3f()), markerPosition);
		assertNotEquals(camera.getEffectiveLookAt(), markerPosition,
				"The camera look-at target always projects to screen center and is not the rotation pivot");
	}

	@Test
	void cameraOrbitModeUsesEffectiveLookAtTarget() {
		Camera camera = Camera.builder()
				.withAspectRatio(1.6f)
				.withFixedCenterOfInterest(false)
				.build();
		Scene scene = new Scene(mock(Rocket.class), camera, new RenderingConfiguration());
		camera.setCenterOfInterest(new Vector3f(5.0f, 0.0f, 0.0f));
		camera.pan(0.0f, 100.0f, 1000, 500);

		assertEquals(camera.getEffectiveLookAt(),
				CameraPointOfInterestPass.getMarkerPosition(scene, false));
	}

	@Test
	void markerReflectsExplicitScenePivotOverride() {
		Camera camera = Camera.builder().withFixedCenterOfInterest(false).build();
		Scene scene = new Scene(mock(Rocket.class), camera, new RenderingConfiguration());
		Vector3f overriddenPivot = new Vector3f(2.0f, 3.0f, 4.0f);
		scene.setRocketRotationPivotOverride(overriddenPivot.x, overriddenPivot.y, overriddenPivot.z);

		assertEquals(overriddenPivot, CameraPointOfInterestPass.getMarkerPosition(scene, true));
	}
}
