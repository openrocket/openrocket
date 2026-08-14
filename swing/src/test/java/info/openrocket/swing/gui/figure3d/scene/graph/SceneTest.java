package info.openrocket.swing.gui.figure3d.scene.graph;

import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.swing.gui.figure3d.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
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
		assertSame(scene.getObjects(), scene.getObjects());
		assertSame(scene.getParticleEmitters(), scene.getParticleEmitters());
		assertSame(scene.getSelectedObjects(), scene.getSelectedObjects());

		scene.removeObject(object);
		scene.clearParticleEmitters();
		assertEquals(0, scene.getObjects().size());
		assertEquals(0, scene.getParticleEmitters().size());
	}

	@Test
	void bulkAddTreatsAnEmptyListAsANoOpAndRejectsNulls() {
		Scene scene = new Scene(mock(Rocket.class), Camera.builder().build(), new RenderingConfiguration());

		assertDoesNotThrow(() -> scene.addObjects(List.of()));
		assertThrows(NullPointerException.class, () -> scene.addObjects(null));
		assertThrows(NullPointerException.class, () -> scene.addObjects(Collections.singletonList(null)));
		assertThrows(NullPointerException.class, () -> scene.addObject(null));
		assertThrows(NullPointerException.class, () -> scene.addParticleEmitter(null));
	}
}
