package info.openrocket.swing.gui.figure3d.core.particles.flame;

import info.openrocket.swing.gui.figure3d.core.particles.ParticleSettings;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Vector3f;

public class FlameSettings extends ParticleSettings {
    
    public final float noiseScale;
    public final float noiseSpeed;
    public final float flickerIntensity;
    public final float lightIntensity;

    public FlameSettings(float velocity, float creationRate, float minLife, float maxLife, float minSize, float maxSize,
                         float spread, boolean burst, Vector3f gravity, Vector3f minColor, Vector3f maxColor,
                         float noiseScale, float noiseSpeed, float flickerIntensity, float lightIntensity,
                         RenderingConfiguration config) {
        super(velocity, creationRate, minLife, maxLife, minSize, maxSize, spread, burst, gravity, minColor, maxColor, config);
        this.noiseScale = noiseScale;
        this.noiseSpeed = noiseSpeed;
        this.flickerIntensity = flickerIntensity;
        this.lightIntensity = lightIntensity;
    }

    /**
     * Settings for a typical rocket flame.
     */
    public static FlameSettings normal(RenderingConfiguration config) {
        return new FlameSettings(18f, 300, 0.3f, 1f, 0.1f, 0.25f, 0.6f, false,
                new Vector3f(0, 0f, 0), // No gravity
                new Vector3f(1.0f, 0.3f, 0.0f), // Deep orange/red
                new Vector3f(1.0f, 1.0f, 0.8f), // Bright yellow-white
                3.0f, 4.0f, 0.8f, 1.5f,
                config);
    }

    /**
     * Settings for an explosion flame burst.
     */
    public static FlameSettings explosion(RenderingConfiguration config) {
        return new FlameSettings(8.0f, 200, 0.2f, 0.6f, 0.3f, 0.8f, 5.0f, true,
                new Vector3f(0, 0f, 0), // No gravity
                new Vector3f(1.0f, 0.4f, 0.0f), // Bright orange
                new Vector3f(1.0f, 1.0f, 0.9f), // Almost white hot
                8.0f, 10.0f, 2.0f, 10.0f,
                config);
    }
}