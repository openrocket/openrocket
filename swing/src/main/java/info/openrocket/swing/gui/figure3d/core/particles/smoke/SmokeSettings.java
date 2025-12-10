package info.openrocket.swing.gui.figure3d.core.particles.smoke;

import info.openrocket.swing.gui.figure3d.core.particles.ParticleSettings;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Vector3f;

public class SmokeSettings extends ParticleSettings {
    
    public final float noiseScale;
    public final float noiseSpeed;
    public final float lightSensitivity;
	private static final Vector3f GRAVITY = new Vector3f(0, 0.4f, 0); // Upwards force

    public SmokeSettings(float velocity, float creationRate, float minLife, float maxLife, float minSize, float maxSize,
                         float spread, boolean burst, Vector3f gravity, Vector3f minColor, Vector3f maxColor,
                         float noiseScale, float noiseSpeed, float lightSensitivity,
                         RenderingConfiguration config) {
        super(velocity, creationRate, minLife, maxLife, minSize, maxSize, spread, burst, gravity, minColor, maxColor, config);
        this.noiseScale = noiseScale;
        this.noiseSpeed = noiseSpeed;
        this.lightSensitivity = lightSensitivity;
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
                1.5f, 0.8f, 0.5f,   // Noise scale, speed, and light sensitivity
                config);
    }

    /**
     * Settings for medium smoke, like from a campfire.
     */
    public static SmokeSettings medium(RenderingConfiguration config) {
        return new SmokeSettings(10f, 300,  // Velocity and creation rate
                4.0f, 7.0f,     // Life span
                0.1f, 0.25f,    // Size range
                1.2f,           // Spread factor
                false,          // Burst
				GRAVITY,  // Gravity
                new Vector3f(0.6f, 0.6f, 0.6f), new Vector3f(0.9f, 0.9f, 0.9f),     // Color range
                2.0f, 1.0f, 0.6f,   // Noise scale, speed, and light sensitivity
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
                3.0f, 1.2f, 0.8f,   // Noise scale, speed, and light sensitivity
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
                2.5f, 1.5f, 0.6f,   // Noise scale, speed, and light sensitivity
                config);
    }
}