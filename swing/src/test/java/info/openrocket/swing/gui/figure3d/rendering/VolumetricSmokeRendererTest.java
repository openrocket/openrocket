package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.swing.gui.figure3d.particles.Particle;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VolumetricSmokeRendererTest {
	@Test
	void scriptedParticleOpacityReachesTheRenderedAlpha() {
		Particle particle = new Particle(new Vector3f(), new Vector3f(), new Vector3f(1.0f), 1.0f, 1.0f);
		particle.setLifetime(0.2f, 1.0f);
		particle.setOpacity(0.5f);

		assertEquals(0.35f, VolumetricSmokeRenderer.effectiveParticleAlpha(particle, 0.8f, 1.0f), 1e-6f);

		particle.setOpacity(0.0f);
		assertEquals(0.0f, VolumetricSmokeRenderer.effectiveParticleAlpha(particle, 0.8f, 1.0f), 1e-6f);
	}
}
