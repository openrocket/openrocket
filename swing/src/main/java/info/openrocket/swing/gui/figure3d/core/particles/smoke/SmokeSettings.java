package info.openrocket.swing.gui.figure3d.core.particles.smoke;

import info.openrocket.core.util.MathUtil;
import info.openrocket.swing.gui.figure3d.core.particles.ParticleSettings;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Vector3f;

public class SmokeSettings extends ParticleSettings {
	
	public final float noiseScale;
	public final float noiseSpeed;
	public final float lightSensitivity;
	public final float opacityMultiplier;
	private static final Vector3f GRAVITY = new Vector3f(0, 0.4f, 0); // Upwards force
	private static final Vector3f DEFAULT_MEDIUM_MIN_COLOR = new Vector3f(0.6f, 0.6f, 0.6f);
	private static final Vector3f DEFAULT_MEDIUM_MAX_COLOR = new Vector3f(0.9f, 0.9f, 0.9f);

	public SmokeSettings(float velocity, float creationRate, float minLife, float maxLife, float minSize, float maxSize,
						 float spread, boolean burst, Vector3f gravity, Vector3f minColor, Vector3f maxColor,
						 float noiseScale, float noiseSpeed, float lightSensitivity, float opacityMultiplier,
						 RenderingConfiguration config) {
		super(velocity, creationRate, minLife, maxLife, minSize, maxSize, spread, burst, gravity, minColor, maxColor, config);
		this.noiseScale = noiseScale;
		this.noiseSpeed = noiseSpeed;
		this.lightSensitivity = lightSensitivity;
		this.opacityMultiplier = opacityMultiplier;
	}

	/**
	 * Settings for light smoke, like from a cigarette or small fire.
	 */
	public static SmokeSettings light(RenderingConfiguration config) {
		return new SmokeSettings(6f, 200,   // Velocity and creation rate
				2.0f, 4.0f,     // Life span
				0.1f, 0.15f,    // Size range
				1f,             // Spread factor
				false,          // Burst
				GRAVITY,      // Gravity
				new Vector3f(0.7f, 0.7f, 0.7f), new Vector3f(1f, 1f, 1f),   // Color range
				1.5f, 0.8f, 0.5f, 1.0f,   // Noise scale, speed, light sensitivity, opacity
				config);
	}

	/**
	 * Settings for medium smoke, like from a campfire.
	 */
	public static SmokeSettings medium(RenderingConfiguration config) {
		return medium(config, null);
	}

	/**
	 * Settings for medium smoke, tinted with the provided base color.
	 */
	public static SmokeSettings medium(RenderingConfiguration config, Vector3f smokeColor) {
		return medium(config, smokeColor, 1.0f, 1.0f);
	}

	public static SmokeSettings medium(RenderingConfiguration config, Vector3f smokeColor, float opacityMultiplier, float exhaustScale) {
		Vector3f minColor = new Vector3f(DEFAULT_MEDIUM_MIN_COLOR);
		Vector3f maxColor = new Vector3f(DEFAULT_MEDIUM_MAX_COLOR);
		if (smokeColor != null) {
			Vector3f clamped = clampColor(smokeColor);
			minColor = clampColor(new Vector3f(clamped).mul(0.70f));
			maxColor = clampColor(new Vector3f(clamped).mul(0.95f).add(0.05f, 0.05f, 0.05f));
		}
		float scale = Math.max(0.0f, exhaustScale);
		float lengthScale = Math.max(0.0f, config.getVisualEffects().getParticleLengthScale())
				* Math.max(0.0f, config.getVisualEffects().getSmokeLengthScale());

		return new SmokeSettings(10f * scale, 300,  // Velocity and creation rate
				4.0f * lengthScale, 7.0f * lengthScale,     // Life span
				0.1f * scale, 0.25f * scale,    // Size range
				1.2f,           // Spread factor
				false,          // Burst
				GRAVITY,  // Gravity
				minColor, maxColor,     // Color range
				2.0f, 1.0f, 0.6f, opacityMultiplier,   // Noise scale, speed, light sensitivity, opacity
				config);
	}

	/**
	 * Settings for heavy smoke, like from a large fire or explosion.
	 */
	public static SmokeSettings heavy(RenderingConfiguration config) {
		return new SmokeSettings(13f, 400,  // Velocity and creation rate
				1.0f, 3.0f,     // Life span
				0.1f, 0.4f,     // Size range
				1f,             // Spread factor
				false,          // Burst
				GRAVITY,      // Gravity
				new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0.9f, 0.9f, 0.9f),     // Color range
				3.0f, 1.2f, 0.8f, 1.0f,   // Noise scale, speed, light sensitivity, opacity
				config);
	}

	/**
	 * Settings for a puff of smoke, like from an explosion or burst.
	 */
	public static SmokeSettings puff(RenderingConfiguration config) {
		return new SmokeSettings(2.0f, 100, // Velocity and creation rate
				2.0f, 4.0f,     // Life span
				0.2f, 0.5f,     // Size range
				3.0f,           // Spread factor
				true,           // Burst
				GRAVITY,      // Gravity
				new Vector3f(0.3f, 0.3f, 0.3f), new Vector3f(0.8f, 0.8f, 0.8f),     // Color range
				2.5f, 1.5f, 0.6f, 1.0f,   // Noise scale, speed, light sensitivity, opacity
				config);
	}

	private static Vector3f clampColor(Vector3f color) {
		return new Vector3f(
				MathUtil.clamp(color.x, 0.0f, 1.0f),
				MathUtil.clamp(color.y, 0.0f, 1.0f),
				MathUtil.clamp(color.z, 0.0f, 1.0f)
		);
	}
}
