package info.openrocket.swing.gui.figure3d.scene.controllers;

import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.util.BoundingBox;
import info.openrocket.core.util.Coordinate;
import info.openrocket.swing.gui.figure3d.scene.graph.Camera;
import info.openrocket.swing.gui.figure3d.scene.graph.Scene;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CameraControllerTest {

	@Test
	void unchangedModelBoundsDoNotRefitAfterRocketRotation() {
		TestContext context = createContext(new BoundingBox(
				new Coordinate(0.0, -1.0, -0.5), new Coordinate(10.0, 1.0, 0.5)));
		context.controller.focusOnRocket();
		context.scene.orbitRocket((float) Math.PI / 4.0f, 0.0f, 1.0f);
		context.camera.setDistance(context.camera.getDistance() * 1.1f);
		float preservedDistance = context.camera.getDistance();

		context.controller.refitOnRocketBoundsChange();

		assertEquals(preservedDistance, context.camera.getDistance(), preservedDistance * 0.0001f);
	}

	@Test
	void changedModelBoundsRefitTheCamera() {
		TestContext context = createContext(new BoundingBox(
				new Coordinate(0.0, -1.0, -0.5), new Coordinate(10.0, 1.0, 0.5)));
		context.controller.focusOnRocket();
		float originalDistance = context.camera.getDistance();
		context.bounds.set(new BoundingBox(
				new Coordinate(0.0, -1.0, -0.5), new Coordinate(15.0, 1.0, 0.5)));

		context.controller.refitOnRocketBoundsChange();

		assertTrue(context.camera.getDistance() > originalDistance);
	}

	@Test
	void fittedDistanceDoesNotDependOnRocketYaw() {
		TestContext context = createContext(new BoundingBox(
				new Coordinate(0.0, -1.0, -0.5), new Coordinate(10.0, 1.0, 0.5)));
		context.controller.focusOnRocket();
		float sideDistance = context.camera.getDistance();
		context.scene.orbitRocket((float) Math.PI / 4.0f, 0.0f, 1.0f);

		context.controller.focusOnRocket();

		assertEquals(sideDistance, context.camera.getDistance(), sideDistance * 0.0001f);
	}

	private static TestContext createContext(BoundingBox initialBounds) {
		AtomicReference<BoundingBox> bounds = new AtomicReference<>(initialBounds);
		Rocket rocket = mock(Rocket.class);
		when(rocket.getBoundingBox()).thenAnswer(ignored -> bounds.get());
		Camera camera = Camera.builder().withAspectRatio(1.6f).withFixedCenterOfInterest(false).build();
		RenderingConfiguration configuration = new RenderingConfiguration();
		Scene scene = new Scene(rocket, camera, configuration);
		CameraController controller = new CameraController(rocket, camera, scene, configuration);
		controller.initialize(rocket, 1.6f);
		return new TestContext(bounds, camera, scene, controller);
	}

	private record TestContext(AtomicReference<BoundingBox> bounds, Camera camera, Scene scene,
			CameraController controller) {
	}
}
