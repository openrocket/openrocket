package info.openrocket.swing.gui.figure3d.scene.controllers;

import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.util.BoundingBox;
import info.openrocket.core.util.Coordinate;
import info.openrocket.swing.gui.figure3d.scene.graph.Camera;
import info.openrocket.swing.gui.figure3d.scene.graph.Scene;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

	@Test
	void resizeRefitsTheWholeFlightBoundsInsteadOfReturningToTheRocket() {
		TestContext context = createContext(new BoundingBox(
				new Coordinate(0.0, -1.0, -0.5), new Coordinate(10.0, 1.0, 0.5)));
		Vector3f trajectoryCenter = new Vector3f(120.0f, 450.0f, -30.0f);
		Vector3f trajectoryDimensions = new Vector3f(300.0f, 20.0f, 10.0f);
		context.controller.focusOnBounds(trajectoryCenter, trajectoryDimensions);
		float wideDistance = context.camera.getDistance();

		context.controller.resize(0.5f);

		assertEquals(trajectoryCenter, context.camera.getCenterOfInterest());
		assertTrue(context.camera.getDistance() > wideDistance);
	}

	@Test
	void resizePreservesTheExpandedWholeFlightZoomRange() {
		TestContext context = createContext(new BoundingBox(
				new Coordinate(0.0, -1.0, -0.5), new Coordinate(10.0, 1.0, 0.5)));
		context.controller.focusOnBounds(new Vector3f(120.0f, 450.0f, -30.0f),
				new Vector3f(1_000.0f, 3_000.0f, 500.0f), 0.001f, 20.0f);

		context.controller.resize(0.5f);
		float resizedFitDistance = context.camera.getDistance();
		context.camera.setDistance(0.0f);

		assertEquals(resizedFitDistance * 0.001f, context.camera.getDistance(), 1e-4f);
	}

	@Test
	void disabledPanIgnoresPersistentAndModifierDrivenPanRequests() {
		TestContext context = createContext(new BoundingBox(
				new Coordinate(0.0, -1.0, -0.5), new Coordinate(10.0, 1.0, 0.5)));
		Vector3f originalTarget = context.camera.getEffectiveLookAt();
		context.controller.setPanEnabled(false);

		context.controller.handlePan(120.0f, -80.0f, 1_000, 700);

		assertEquals(originalTarget, context.camera.getEffectiveLookAt());
	}

	@Test
	void explicitCameraDistanceCanDisableFitTracking() {
		TestContext context = createContext(new BoundingBox(
				new Coordinate(0.0, -1.0, -0.5), new Coordinate(10.0, 1.0, 0.5)));
		context.controller.focusOnRocket();
		context.controller.setZoomFitting(false);

		assertFalse(context.controller.isZoomFitting());
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
