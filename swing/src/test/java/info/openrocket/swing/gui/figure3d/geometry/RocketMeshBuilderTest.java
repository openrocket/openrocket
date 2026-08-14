package info.openrocket.swing.gui.figure3d.geometry;

import info.openrocket.swing.gui.figure3d.scene.graph.Scene;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RocketMeshBuilderTest {

	@Test
	void rebuildingOriginAxesRemovesOnlyTaggedAxesAndCleansTheirResources() {
		SceneObject axis = mock(SceneObject.class);
		when(axis.isOriginAxis()).thenReturn(true);
		SceneObject otherOverlay = mock(SceneObject.class);
		Scene scene = mock(Scene.class);
		when(scene.getObjects()).thenReturn(List.of(axis, otherOverlay));
		RenderingConfiguration configuration = new RenderingConfiguration();
		configuration.getVisualEffects().setOriginAxesVisible(false);

		RocketMeshBuilder.rebuildOriginAxes(scene, configuration, true, true);

		verify(scene).removeObject(axis);
		verify(axis).cleanup();
		verify(scene, never()).removeObject(otherOverlay);
		verify(otherOverlay, never()).cleanup();
	}
}
