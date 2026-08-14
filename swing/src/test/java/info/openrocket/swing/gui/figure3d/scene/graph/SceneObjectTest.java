package info.openrocket.swing.gui.figure3d.scene.graph;

import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.materials.Appearance3D;
import info.openrocket.swing.gui.figure3d.rendering.Renderable;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SceneObjectTest {

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
