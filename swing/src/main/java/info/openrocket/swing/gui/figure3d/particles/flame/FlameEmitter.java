package info.openrocket.swing.gui.figure3d.particles.flame;

import info.openrocket.core.util.MathUtil;
import info.openrocket.swing.gui.figure3d.particles.Particle;
import info.openrocket.swing.gui.figure3d.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.particles.PerlinNoise;
import info.openrocket.swing.gui.figure3d.scene.core.Light;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Particle emitter that creates realistic flame effects with dynamic lighting.
 * Uses Perlin noise for natural color variation and supports quality-based optimization.
 */
public class FlameEmitter extends ParticleEmitter {
	
	private final float noiseScale;
	private final float noiseSpeed;
	private final float flickerIntensity;
	private final float lightIntensity;
	private final float sizeMultiplier;
	private final float exposureScale;
	private final Vector3f scratchLightPosition = new Vector3f();
	private final Vector3f scratchWeightedPosition = new Vector3f();
	private final Vector3f scratchBaseColor = new Vector3f();
	private final Vector3f scratchFlameColor = new Vector3f();
	
	private Light flameLight;

	/**
	 * Creates a flame emitter with flame-specific settings.
	 * @param emitterPosition where flames are emitted from
	 * @param direction base direction of flame emission
	 * @param flameSettings flame-specific configuration including noise and lighting
	 */
	public FlameEmitter(Vector3f emitterPosition, Vector3f direction, FlameSettings flameSettings) {
		super(emitterPosition, direction, flameSettings);
		this.noiseScale = flameSettings.noiseScale;
		this.noiseSpeed = flameSettings.noiseSpeed;
		this.flickerIntensity = flameSettings.flickerIntensity;
		this.lightIntensity = flameSettings.lightIntensity;
		this.sizeMultiplier = flameSettings.sizeMultiplier;
		this.exposureScale = flameSettings.exposureScale;
	}
	
	public float getFlickerIntensity() {
		return flickerIntensity;
	}
	
	public float getLightIntensity() {
		return lightIntensity;
	}

	public float getSizeMultiplier() {
		return sizeMultiplier;
	}

	public float getExposureScale() {
		return exposureScale;
	}
	
	/**
	 * Calculate the average light position and intensity from all flame particles
	 */
	public Vector3f calculateLightPosition() {
		return calculateLightPosition(new Vector3f());
	}

	public Vector3f calculateLightPosition(Vector3f destination) {
		if (particles.isEmpty()) {
			return destination.set(emitterPosition);
		}
		
		destination.zero();
		float totalWeight = 0.0f;
		
		for (Particle particle : particles) {
			float ageRatio = 1.0f - (particle.getLife() / particle.getMaxLife());
			float weight = 1.0f - ageRatio; // Younger particles contribute more
			
			destination.add(scratchWeightedPosition.set(particle.getPosition()).mul(weight));
			totalWeight += weight;
		}
		
		if (totalWeight > 0) {
			destination.div(totalWeight);
		} else {
			destination.set(emitterPosition);
		}
		
		return destination;
	}
	
	/**
	 * Calculate effective light intensity based on particle count and age
	 */
	public float calculateEffectiveLightIntensity() {
		if (particles.isEmpty()) {
			return 0.0f;
		}
		
		// Quality-based lighting computation
		float intensity = 0.0f;
		int sampleStep = switch (settings.config.getQuality().getQuality()) {
			case LOW -> 4;    // Sample every 4th particle for performance
			case MEDIUM -> 2; // Sample every 2nd particle
			case HIGH -> 1;   // Sample all particles
		};
		
		for (int i = 0; i < particles.size(); i += sampleStep) {
			Particle particle = particles.get(i);
			float ageRatio = 1.0f - (particle.getLife() / particle.getMaxLife());
			float particleIntensity = (1.0f - ageRatio) * lightIntensity; // Fade with age
			intensity += particleIntensity * sampleStep; // Compensate for sampling
		}
		
		return Math.min(intensity * 0.01f, lightIntensity) * exposureScale; // Scale down, cap, and match visible flame brightness
	}
	
	/**
	 * Updates this flame's light properties. Creates the light if it doesn't exist.
	 */
	public void updateFlameLight() {
		float intensity = calculateEffectiveLightIntensity();
		boolean shouldHaveLight = intensity > 0.1f;
		
		if (shouldHaveLight) {
			Vector3f position = calculateLightPosition(scratchLightPosition);
			
			// Color the dynamic light from the configured flame particle color range.
			float lightIntensity = Math.min(intensity * 8.0f, 3.0f); // Cap at 3.0 for bright but not overwhelming
			scratchBaseColor.set(settings.maxColor).mul(0.65f)
					.add(scratchFlameColor.set(settings.minColor).mul(0.35f));
			Vector3f flameColor = scratchFlameColor.set(scratchBaseColor).mul(lightIntensity);
			
			if (flameLight == null) {
				// Create new light
				flameLight = Light.point()
					.withPosition(position)
					.withColor(flameColor)
					.build();
			} else {
				// Update existing light
				flameLight.setPosition(position.x, position.y, position.z);
				flameLight.setColor(flameColor.x, flameColor.y, flameColor.z);
			}
		} else {
			// Clear the light reference when it shouldn't exist
			flameLight = null;
		}
	}
	
	/**
	 * Gets this flame's light reference (may be null)
	 */
	public Light getFlameLight() {
		return flameLight;
	}

	/**
	 * Creates a flame particle with realistic flame characteristics.
	 * Uses Perlin noise for natural color variation and upward-biased motion.
	 */
	@Override
	protected void createParticle() {
		Vector3f position = new Vector3f(emitterPosition);
		Vector3f velocity = new Vector3f(direction).mul(settings.velocity);

		// Flames spread more in horizontal directions and rise upward
		velocity.x += nextRandomSignedFloat() * settings.spread;
		velocity.y += Math.abs(nextRandomSignedFloat() * settings.spread * 0.5f); // Mostly upward
		velocity.z += nextRandomSignedFloat() * settings.spread;

		// Use Perlin noise for flame color variation (creates realistic flame patterns)
		float time = getNoiseTime();
		
		// Add random offset for each particle
		float randomOffset = nextRandomFloat() * 100.0f;
		
		// Quality-adjusted noise computation for flame
		float colorFactor = 0.5f;
		switch (settings.config.getQuality().getQuality()) {
			case MEDIUM, HIGH -> {
				// Full complexity for high quality flames
				float colorNoise1 = PerlinNoise.noise(
					(position.x + randomOffset) * noiseScale, 
					(position.y + randomOffset) * noiseScale + time * noiseSpeed, 
					(position.z + randomOffset) * noiseScale
				);
				float colorNoise2 = PerlinNoise.noise(
					(position.x + randomOffset * 0.7f) * noiseScale * 0.5f, 
					(position.y + randomOffset * 0.7f) * noiseScale * 0.5f + time * noiseSpeed * 0.8f, 
					(position.z + randomOffset * 0.7f) * noiseScale * 0.5f
				) * 0.3f;
				
				float combinedNoise = colorNoise1 + colorNoise2;
				colorFactor = (combinedNoise + 1.5f) / 3.0f;
			}
		}
		
		// Normalize and enhance contrast for flame colors
		colorFactor = MathUtil.clamp(colorFactor, 0.0f, 1.0f);
		
		// Interpolate between flame colors (red/orange to yellow/white)
		float r = settings.minColor.x + colorFactor * (settings.maxColor.x - settings.minColor.x);
		float g = settings.minColor.y + colorFactor * (settings.maxColor.y - settings.minColor.y);
		float b = settings.minColor.z + colorFactor * (settings.maxColor.z - settings.minColor.z);
		Vector3f color = new Vector3f(r, g, b);

		float size = nextRandomFloat() * (settings.maxSize - settings.minSize) + settings.minSize;
		float life = nextRandomFloat() * (settings.maxLife - settings.minLife) + settings.minLife;
		
		// Flames have more chaotic orientation and movement
		Quaternionf orientation = new Quaternionf().rotateXYZ(
			nextRandomFloat() * (float) (Math.PI * 2),
			nextRandomFloat() * (float) (Math.PI * 2), 
			nextRandomFloat() * (float) (Math.PI * 2)
		);
		
		// More dramatic angular velocity for flame flicker
		Vector3f angularVelocity = new Vector3f(
			nextRandomSignedFloat() * 2.0f * flickerIntensity,
			nextRandomSignedFloat() * 2.0f * flickerIntensity,
			nextRandomSignedFloat() * 2.0f * flickerIntensity
		);

		particles.add(new Particle(position, velocity, color, size, life, orientation, angularVelocity));
	}
}
