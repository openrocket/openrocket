package info.openrocket.swing.gui.figure3d.core.particles.flame;

import info.openrocket.swing.gui.figure3d.core.particles.ParticleSettings;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Vector3f;

public class FlameSettings extends ParticleSettings {
    
    public final float noiseScale;
    public final float noiseSpeed;
    public final float flickerIntensity;
    public final float lightIntensity;
    public final float sizeMultiplier;
    private static final Vector3f DEFAULT_NORMAL_MIN_COLOR = new Vector3f(1.0f, 0.3f, 0.0f);
    private static final Vector3f DEFAULT_NORMAL_MAX_COLOR = new Vector3f(1.0f, 1.0f, 0.8f);

    public FlameSettings(float velocity, float creationRate, float minLife, float maxLife, float minSize, float maxSize,
                         float spread, boolean burst, Vector3f gravity, Vector3f minColor, Vector3f maxColor,
                         float noiseScale, float noiseSpeed, float flickerIntensity, float lightIntensity, float sizeMultiplier,
                         RenderingConfiguration config) {
        super(velocity, creationRate, minLife, maxLife, minSize, maxSize, spread, burst, gravity, minColor, maxColor, config);
        this.noiseScale = noiseScale;
        this.noiseSpeed = noiseSpeed;
        this.flickerIntensity = flickerIntensity;
        this.lightIntensity = lightIntensity;
        this.sizeMultiplier = sizeMultiplier;
    }

    /**
     * Settings for a typical rocket flame.
     */
    public static FlameSettings normal(RenderingConfiguration config) {
        return normal(config, null);
    }

    /**
     * Settings for a typical rocket flame tinted with a user-selected base color.
     */
    public static FlameSettings normal(RenderingConfiguration config, Vector3f flameColor) {
        return normal(config, flameColor, 1.0f, 1.0f);
    }

    public static FlameSettings normal(RenderingConfiguration config, Vector3f flameColor, float exhaustScale, float flameAspectRatio) {
        Vector3f minColor = new Vector3f(DEFAULT_NORMAL_MIN_COLOR);
        Vector3f maxColor = new Vector3f(DEFAULT_NORMAL_MAX_COLOR);
        if (flameColor != null) {
            Vector3f clamped = clampColor(flameColor);
            // Photo Studio expects the selected flame color to dominate the whole plume.
            // Keep the hue close to the user's choice and vary mostly by brightness.
            minColor = clampColor(new Vector3f(clamped).mul(0.35f));
            maxColor = clampColor(new Vector3f(clamped));
        }
        float scale = Math.max(0.0f, exhaustScale);
        float sizeMultiplier = Math.max(0.0f, flameAspectRatio);

        return new FlameSettings(18f * scale, 300, 0.3f, 1f, 0.1f * scale, 0.25f * scale, 0.6f, false,
                new Vector3f(0, 0f, 0), // No gravity
                minColor,
                maxColor,
                3.0f, 4.0f, 0.8f, 1.5f, sizeMultiplier,
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
                8.0f, 10.0f, 2.0f, 10.0f, 1.0f,
                config);
    }

    private static Vector3f clampColor(Vector3f color) {
        return new Vector3f(
                Math.max(0.0f, Math.min(1.0f, color.x)),
                Math.max(0.0f, Math.min(1.0f, color.y)),
                Math.max(0.0f, Math.min(1.0f, color.z))
        );
    }
}
