package info.openrocket.swing.gui.figure3d.core.particles.spark;

import info.openrocket.swing.gui.figure3d.core.particles.ParticleSettings;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Vector3f;

public class SparkSettings extends ParticleSettings {

    public SparkSettings(float velocity, float creationRate, float minLife, float maxLife, float minSize, float maxSize,
                         float spread, boolean burst, Vector3f gravity, Vector3f minColor, Vector3f maxColor,
                         RenderingConfiguration config) {
        super(velocity, creationRate, minLife, maxLife, minSize, maxSize, spread, burst, gravity, minColor, maxColor, config);
    }

    /**
     * Settings for subtle sparks, like those from a grinder.
     */
    public static SparkSettings subtle(RenderingConfiguration config) {
        return new SparkSettings(2.0f, 10,      // Velocity and creation rate
                0.2f, 0.5f,     // Life span
                0.01f, 0.02f,   // Size range
                2.0f,           // Spread factor
                false,          // Burst
                new Vector3f(0, 0, 0),      // Gravity
                new Vector3f(1.0f, 0.8f, 0.2f), new Vector3f(1.0f, 1.0f, 0.8f),     // Color range
                config);
    }

    /**
     * Settings for a normal amount of sparks.
     */
    public static SparkSettings normal(RenderingConfiguration config) {
        return new SparkSettings(5.0f, 50,  // Velocity and creation rate
                0.5f, 1.0f,     // Life span
                0.02f, 0.04f,   // Size range
                5.0f,           // Spread factor
                false,          // Burst
                new Vector3f(0, 0, 0),      // Gravity
                new Vector3f(1.0f, 0.8f, 0.2f), new Vector3f(1.0f, 1.0f, 0.8f),     // Color range
                config);
    }

    /**
     * Settings for an intense shower of sparks.
     */
    public static SparkSettings intense(RenderingConfiguration config) {
        return intense(config, 1.0f, 1.0f, 0.0f);
    }

    public static SparkSettings intense(RenderingConfiguration config, float exhaustScale, float sparkConcentration, float sparkWeight) {
        float scale = Math.max(0.0f, exhaustScale);
        float concentration = Math.max(0.0f, sparkConcentration);
        float weight = Math.max(0.0f, sparkWeight);
        return new SparkSettings(10.0f * scale, 200 * concentration,    // Velocity and creation rate
                0.8f, 1.5f,     // Life span
                0.03f * scale, 0.06f * scale,   // Size range
                10.0f,          // Spread factor
                false,          // Burst
                new Vector3f(0, -9.8f * weight, 0),      // Gravity
                new Vector3f(1.0f, 0.8f, 0.2f), new Vector3f(1.0f, 1.0f, 0.8f),     // Color range
                config);
    }

    /**
     * Settings for a firework-like explosion of sparks.
     */
    public static SparkSettings firework(RenderingConfiguration config) {
        return new SparkSettings(20.0f, 500,    // Velocity and creation rate
                1.0f, 2.0f,     // Life span
                0.05f, 0.1f,    // Size range
                20.0f,          // Spread factor
                true,           // Burst
                new Vector3f(0, -9.8f, 0),      // Gravity
                new Vector3f(1.0f, 0.5f, 0.5f), new Vector3f(1.0f, 1.0f, 1.0f),     // Color range
                config);
    }
}
