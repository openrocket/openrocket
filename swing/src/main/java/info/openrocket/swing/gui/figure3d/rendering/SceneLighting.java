package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.swing.gui.figure3d.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.particles.flame.FlameEmitter;
import info.openrocket.swing.gui.figure3d.scene.controllers.LightController;
import info.openrocket.swing.gui.figure3d.scene.graph.Light;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;

/**
 * Keeps dynamic scene lights synchronized with their render-time sources.
 */
final class SceneLighting {

	private SceneLighting() {
	}

	static void updateFlameLights(SceneView scene) {
		LightController lightController = scene.getLightController();
		for (ParticleEmitter emitter : scene.getParticleEmitters()) {
			if (!(emitter instanceof FlameEmitter flameEmitter)) {
				continue;
			}

			Light oldLight = flameEmitter.getFlameLight();
			flameEmitter.updateFlameLight();
			Light newLight = flameEmitter.getFlameLight();

			if (oldLight != newLight) {
				if (oldLight != null) {
					lightController.removeLight(oldLight);
				}
				if (newLight != null && !lightController.getLights().contains(newLight)) {
					lightController.addLight(newLight);
				}
			} else if (newLight != null) {
				if (lightController.getLights().contains(newLight)) {
					lightController.refreshVisualizer(newLight);
				} else {
					lightController.addLight(newLight);
				}
			}
		}
	}
}
