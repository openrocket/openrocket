package info.openrocket.swing.gui.figure3d.math;

import info.openrocket.swing.gui.figure3d.geometry.IntList;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.Vertex;
import info.openrocket.swing.gui.figure3d.materials.Appearance3D;
import info.openrocket.swing.gui.figure3d.rendering.Renderable;
import info.openrocket.swing.gui.figure3d.scene.graph.Camera;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class DefaultRaycasterTest {
	private DefaultRaycaster raycaster;

	@BeforeEach
	void createCenterRay() {
		Camera camera = Camera.builder().withAspectRatio(1.0f).build();
		camera.setSideView();
		camera.update();
		raycaster = new DefaultRaycaster();
		raycaster.update(50.0f, 50.0f, 100, 100, camera);
	}

	@Test
	void centerRayPointsFromCameraTowardOrigin() {
		assertTrue(Math.abs(raycaster.getRayDirection().x) < 1.0e-6f);
		assertTrue(Math.abs(raycaster.getRayDirection().y) < 1.0e-6f);
		assertTrue(raycaster.getRayDirection().z < -0.999f);
		assertTrue(raycaster.getRayOrigin().z > 0.0f);
	}

	@Test
	void returnsClosestIntersectedObjectAfterModelTransform() {
		SceneObject farther = createTriangle();
		farther.getModelMatrix().translate(0.0f, 0.0f, -2.0f);
		SceneObject closer = createTriangle();

		assertSame(closer, raycaster.getIntersectedObject(List.of(farther, closer)));
	}

	@Test
	void skipsNonSelectableObjects() {
		SceneObject closer = createTriangle();
		closer.setSelectable(false);
		SceneObject farther = createTriangle();
		farther.getModelMatrix().translate(0.0f, 0.0f, -2.0f);

		assertSame(farther, raycaster.getIntersectedObject(List.of(closer, farther)));
	}

	private static SceneObject createTriangle() {
		List<Vertex> vertices = List.of(
				vertex(-1.0f, -1.0f),
				vertex(1.0f, -1.0f),
				vertex(0.0f, 1.0f));
		IntList indices = new IntList();
		indices.addTriangle(0, 1, 2);
		return SceneObject.withRenderable(null, new Mesh(vertices, indices), mock(Renderable.class),
				new Vector3f(), mock(Appearance3D.class));
	}

	private static Vertex vertex(float x, float y) {
		return new Vertex(new Vector3f(x, y, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f),
				new Vector2f(), 0);
	}
}
