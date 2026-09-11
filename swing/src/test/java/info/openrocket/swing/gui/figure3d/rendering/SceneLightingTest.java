package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.swing.gui.figure3d.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.particles.flame.FlameEmitter;
import info.openrocket.swing.gui.figure3d.scene.controllers.LightController;
import info.openrocket.swing.gui.figure3d.scene.graph.Light;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SceneLightingTest {

	@Test
	void updatesExistingFlameLightWithoutReAddingIt() {
		Light light = mock(Light.class);
		TestContext context = contextWith(light, light, new ArrayList<>(List.of(light)));

		SceneLighting.updateFlameLights(context.scene);

		verify(context.flameEmitter).updateFlameLight();
		verify(context.lightController, never()).removeLight(light);
		verify(context.lightController, never()).addLight(light);
		verify(context.lightController).refreshVisualizer(light);
	}

	@Test
	void addsLightWhenFlameBecomesVisible() {
		Light light = mock(Light.class);
		TestContext context = contextWith(null, light, new ArrayList<>());

		SceneLighting.updateFlameLights(context.scene);

		verify(context.lightController).addLight(light);
		verify(context.lightController, never()).removeLight(light);
	}

	@Test
	void removesLightWhenFlameGoesOut() {
		Light light = mock(Light.class);
		TestContext context = contextWith(light, null, new ArrayList<>(List.of(light)));

		SceneLighting.updateFlameLights(context.scene);

		verify(context.lightController).removeLight(light);
		verify(context.lightController, never()).addLight(light);
	}

	@Test
	void retriesRegistrationWhenExistingLightIsNotManaged() {
		Light light = mock(Light.class);
		TestContext context = contextWith(light, light, new ArrayList<>());

		SceneLighting.updateFlameLights(context.scene);

		verify(context.lightController).addLight(light);
		verify(context.lightController, never()).refreshVisualizer(light);
	}

	@Test
	void ignoresOtherParticleEmitters() {
		SceneView scene = mock(SceneView.class);
		LightController lightController = mock(LightController.class);
		ParticleEmitter emitter = mock(ParticleEmitter.class);
		when(scene.getLightController()).thenReturn(lightController);
		when(scene.getParticleEmitters()).thenReturn(List.of(emitter));

		SceneLighting.updateFlameLights(scene);

		verify(lightController, never()).getLights();
	}

	private static TestContext contextWith(Light oldLight, Light newLight, List<Light> managedLights) {
		SceneView scene = mock(SceneView.class);
		LightController lightController = mock(LightController.class);
		FlameEmitter flameEmitter = mock(FlameEmitter.class);
		when(scene.getLightController()).thenReturn(lightController);
		when(scene.getParticleEmitters()).thenReturn(List.of(flameEmitter));
		when(lightController.getLights()).thenReturn(managedLights);
		when(flameEmitter.getFlameLight()).thenReturn(oldLight, newLight);
		return new TestContext(scene, lightController, flameEmitter);
	}

	private record TestContext(SceneView scene, LightController lightController, FlameEmitter flameEmitter) {
	}
}
