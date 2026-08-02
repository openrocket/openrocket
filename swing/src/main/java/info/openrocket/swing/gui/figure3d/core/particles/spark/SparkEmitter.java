package info.openrocket.swing.gui.figure3d.core.particles.spark;

import info.openrocket.swing.gui.figure3d.core.particles.Particle;
import info.openrocket.swing.gui.figure3d.core.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.core.particles.ParticleSettings;
import org.joml.Vector3f;

/**
 * Particle emitter that creates spark effects with quality-adjusted color variation.
 * Sparks have uniform spread in all directions with basic physics.
 */
public class SparkEmitter extends ParticleEmitter {

	/**
	 * Creates a spark emitter.
	 * @param emitterPosition where sparks are emitted from
	 * @param direction base direction of spark emission
	 * @param settings particle configuration for spark behavior
	 */
	public SparkEmitter(Vector3f emitterPosition, Vector3f direction, ParticleSettings settings) {
		super(emitterPosition, direction, settings);
	}

	/**
	 * Creates a spark particle with uniform directional spread.
	 * Color variation depends on quality settings for performance optimization.
	 */
	@Override
	protected void createParticle() {
		Vector3f position = new Vector3f(emitterPosition);
		Vector3f velocity = new Vector3f(direction).mul(settings.velocity);

		// Add some randomness to velocity based on spread
		velocity.x += nextRandomSignedFloat() * settings.spread;
		velocity.y += nextRandomSignedFloat() * settings.spread;
		velocity.z += nextRandomSignedFloat() * settings.spread;

		float colorFactor = 0.5f;
		switch (settings.config.getQuality().getQuality()) {
			case MEDIUM, HIGH -> colorFactor = nextRandomFloat();
		}

		// Linearly interpolate between min and max color
		float r = settings.minColor.x + colorFactor * (settings.maxColor.x - settings.minColor.x);
		float g = settings.minColor.y + colorFactor * (settings.maxColor.y - settings.minColor.y);
		float b = settings.minColor.z + colorFactor * (settings.maxColor.z - settings.minColor.z);
		Vector3f color = new Vector3f(r, g, b);

		float size = nextRandomFloat() * (settings.maxSize - settings.minSize) + settings.minSize;
		float life = nextRandomFloat() * (settings.maxLife - settings.minLife) + settings.minLife;

		particles.add(new Particle(position, velocity, color, size, life));
	}
}
