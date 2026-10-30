package info.openrocket.swing.gui.figure3d.scene.graph;

import info.openrocket.swing.gui.figure3d.animation.PoseProvider;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.materials.Appearance3D;
import info.openrocket.swing.gui.figure3d.rendering.Renderable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SceneObjectTest {
	@Test
	void foregroundRedrawIsOptIn() {
		SceneObject object = SceneObject.withRenderable(null, mock(Mesh.class), mock(Renderable.class),
				new Vector3f(), mock(Appearance3D.class));

		assertFalse(object.isRenderInForeground());
		assertFalse(object.isForegroundDecoration());
		object.setRenderInForeground(true);
		object.setForegroundDecoration(true);
		assertTrue(object.isRenderInForeground());
		assertTrue(object.isForegroundDecoration());
	}

	@Test
	void uniformVisualScalePreservesTheObjectOrigin() {
		SceneObject object = SceneObject.withRenderable(null, mock(Mesh.class), mock(Renderable.class),
				new Vector3f(2.0f, 3.0f, 4.0f), mock(Appearance3D.class));

		object.setUniformScale(0.25f);

		Matrix4f transform = object.getModelMatrix();
		assertEquals(new Vector3f(2.0f, 3.0f, 4.0f), transform.transformPosition(new Vector3f()));
		assertEquals(new Vector3f(2.25f, 3.0f, 4.0f),
				transform.transformPosition(new Vector3f(1.0f, 0.0f, 0.0f)));
	}

	@Test
	void basePositionMovesAPosedObjectsOffsetFromItsTrajectory() {
		SceneObject object = SceneObject.withRenderable(null, mock(Mesh.class), mock(Renderable.class),
				new Vector3f(1.0f, 0.0f, 0.0f), mock(Appearance3D.class));
		PoseProvider provider = new PoseProvider() {
			@Override public Vector3f getPosition(double t) { return new Vector3f(0.0f, (float) t, 0.0f); }
			@Override public Quaternionf getOrientation(double t) { return new Quaternionf(); }
			@Override public double getStartTime() { return 0.0; }
			@Override public double getEndTime() { return 10.0; }
		};
		object.setPoseProvider(provider);
		object.applyPoseAtTime(5.0);
		assertEquals(new Vector3f(1.0f, 5.0f, 0.0f), object.getModelMatrix().transformPosition(new Vector3f()));

		object.setBasePosition(new Vector3f(-3.0f, 0.0f, 2.0f));
		object.applyPoseAtTime(6.0);

		assertEquals(new Vector3f(-3.0f, 6.0f, 2.0f), object.getModelMatrix().transformPosition(new Vector3f()));
	}

	@Test
	void cleanupReleasesOwnedResourcesOnlyOnce() {
		Renderable renderable = mock(Renderable.class);
		Appearance3D appearance = mock(Appearance3D.class);
		SceneObject object = SceneObject.withRenderable(null, mock(Mesh.class), renderable,
				new Vector3f(), appearance);

		object.cleanup();
		object.cleanup();

		verify(renderable).cleanup();
		verify(appearance).cleanup();
	}
}
