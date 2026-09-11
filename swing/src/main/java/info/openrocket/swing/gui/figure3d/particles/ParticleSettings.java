package info.openrocket.swing.gui.figure3d.particles;

import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Vector3f;

/**
 * Configuration class for particle system behavior and appearance.
 * Defines physics parameters, visual properties, and quality-adjusted settings.
 */
public class ParticleSettings {

	public final float velocity;
	public final float creationRate; // particles per second
	public final float minLife, maxLife;
	public final float minSize, maxSize;
	public final float spread;
	public final boolean burst;
	public final Vector3f gravity;
	public final Vector3f minColor, maxColor;

	public RenderingConfiguration config;

	/**
	 * Creates particle settings with all configuration parameters.
	 * @param velocity initial particle velocity magnitude
	 * @param creationRate particles created per second
	 * @param minLife minimum particle lifetime in seconds
	 * @param maxLife maximum particle lifetime in seconds
	 * @param minSize minimum particle size
	 * @param maxSize maximum particle size
	 * @param spread angular spread of particle emission in radians
	 * @param burst true for one-time burst, false for continuous emission
	 * @param gravity gravity vector affecting particle movement
	 * @param minColor minimum RGB color values
	 * @param maxColor maximum RGB color values
	 * @param config rendering configuration for quality adjustments
	 */
	public ParticleSettings(float velocity, float creationRate, float minLife, float maxLife, float minSize, float maxSize,
							float spread, boolean burst, Vector3f gravity, Vector3f minColor, Vector3f maxColor,
							RenderingConfiguration config) {
		this.velocity = velocity;
		this.creationRate = creationRate;
		this.minLife = minLife;
		this.maxLife = maxLife;
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.spread = spread;
		this.burst = burst;
		this.gravity = gravity;
		this.minColor = minColor;
		this.maxColor = maxColor;
		this.config = config;
	}

	/**
	 * Returns creation rate adjusted for rendering quality settings.
	 * Lower quality reduces particle count for better performance.
	 * @return creation rate scaled by quality multiplier (0.3x-1.0x)
	 */
	public float getQualityAdjustedCreationRate() {
		float qualityMultiplier = switch (config.getQuality().getQuality()) {
			case LOW -> 0.3f;
			case MEDIUM -> 0.7f;
			case HIGH -> 1.0f;
		};
		return creationRate * qualityMultiplier;
	}
}
