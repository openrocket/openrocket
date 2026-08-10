package info.openrocket.swing.gui.figure3d.particles.spark;

import info.openrocket.swing.gui.figure3d.particles.Particle;
import info.openrocket.swing.gui.figure3d.particles.ParticleSettings;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SparkEmitterTest {
	private static final int SAMPLE_COUNT = 1_000;
	private static final float EPSILON = 1.0e-5f;

	@Test
	void emitsForwardBiasedFragmentsInsideBoundedCone() {
		Vector3f emitterPosition = new Vector3f(2.0f, -1.0f, 0.5f);
		Vector3f direction = new Vector3f(2.0f, -1.0f, 3.0f);
		Vector3f forward = new Vector3f(direction).normalize();
		ParticleSettings settings = createSettings(8.0f);
		SparkEmitter emitter = new SparkEmitter(emitterPosition, direction, settings);

		int concentratedFragments = 0;
		for (int i = 0; i < SAMPLE_COUNT; i++) {
			emitter.createParticle();
			Particle particle = emitter.getParticles().get(i);

			float axialSpeed = particle.velocity.dot(forward);
			float radialSpeedSquared = Math.max(0.0f,
					particle.velocity.lengthSquared() - axialSpeed * axialSpeed);
			float radialSpeed = (float) Math.sqrt(radialSpeedSquared);
			if (radialSpeed <= settings.spread * 0.27f + EPSILON) {
				concentratedFragments++;
			}

			assertTrue(axialSpeed > 0.0f, "spark must move downstream");
			assertTrue(radialSpeed <= settings.spread * 0.45f + EPSILON,
					"wide fragments must remain bounded");
			assertTrue(particle.position.distance(emitterPosition) <= particle.size * 0.75f + EPSILON,
					"spark origin must stay on the nozzle disk");
			assertEquals(0.0f, new Vector3f(particle.position).sub(emitterPosition).dot(forward), EPSILON,
					"nozzle scatter must be perpendicular to the exhaust direction");
			assertTrue(particle.size >= settings.minSize && particle.size <= settings.maxSize);
			assertTrue(particle.life >= settings.minLife && particle.life <= settings.maxLife);
			assertTrue(Float.isFinite(particle.velocity.x));
			assertTrue(Float.isFinite(particle.velocity.y));
			assertTrue(Float.isFinite(particle.velocity.z));
		}

		assertTrue(concentratedFragments >= SAMPLE_COUNT * 0.85f,
				"most sparks should stay in the concentrated exhaust cone");
	}

	@Test
	void fallsBackToForwardAxisForZeroDirection() {
		ParticleSettings settings = createSettings(0.0f);
		SparkEmitter emitter = new SparkEmitter(new Vector3f(), new Vector3f(), settings);

		for (int i = 0; i < 20; i++) {
			emitter.createParticle();
			Particle particle = emitter.getParticles().get(i);
			assertTrue(particle.velocity.x > 0.0f);
			assertEquals(0.0f, particle.velocity.y, EPSILON);
			assertEquals(0.0f, particle.velocity.z, EPSILON);
		}
	}

	private static ParticleSettings createSettings(float spread) {
		return new ParticleSettings(
				10.0f, 100.0f,
				0.4f, 1.2f,
				0.01f, 0.05f,
				spread, false,
				new Vector3f(),
				new Vector3f(1.0f, 0.28f, 0.02f),
				new Vector3f(1.0f, 0.92f, 0.58f),
				new RenderingConfiguration());
	}
}
