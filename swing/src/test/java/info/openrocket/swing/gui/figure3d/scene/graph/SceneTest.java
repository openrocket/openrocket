package info.openrocket.swing.gui.figure3d.scene.graph;

import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.swing.gui.figure3d.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class SceneTest {

	@Test
	void collectionViewsDoNotBypassSceneMutationMethods() {
		Scene scene = new Scene(mock(Rocket.class), Camera.builder().build(), new RenderingConfiguration());
		SceneObject object = mock(SceneObject.class);
		ParticleEmitter emitter = mock(ParticleEmitter.class);
		scene.addObject(object);
		scene.addParticleEmitter(emitter);
		scene.setSelection(List.of(object));

		assertThrows(UnsupportedOperationException.class, () -> scene.getObjects().clear());
		assertThrows(UnsupportedOperationException.class, () -> scene.getParticleEmitters().clear());
		assertThrows(UnsupportedOperationException.class, () -> scene.getSelectedObjects().clear());
		assertThrows(UnsupportedOperationException.class, () -> scene.getLightController().getLights().clear());

		scene.removeObject(object);
		scene.clearParticleEmitters();
		assertEquals(0, scene.getObjects().size());
		assertEquals(0, scene.getParticleEmitters().size());
	}
}
