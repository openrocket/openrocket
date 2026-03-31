package info.openrocket.swing.gui.figure3d.core.particles.smoke;

import info.openrocket.swing.gui.figure3d.core.particles.Particle;
import info.openrocket.swing.gui.figure3d.core.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.core.particles.PerlinNoise;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Particle emitter that creates realistic smoke effects with natural dispersion.
 * Smoke particles rise and spread with quality-adjusted noise patterns.
 */
public class SmokeEmitter extends ParticleEmitter {
    
    private final float noiseScale;
    private final float noiseSpeed;
    private final float lightSensitivity;
    private final float opacityMultiplier;

    /**
     * Creates a smoke emitter with smoke-specific settings.
     * @param emitterPosition where smoke is emitted from
     * @param direction base direction of smoke emission
     * @param smokeSettings smoke-specific configuration including noise and light sensitivity
     */
    public SmokeEmitter(Vector3f emitterPosition, Vector3f direction, SmokeSettings smokeSettings) {
        super(emitterPosition, direction, smokeSettings);
        this.noiseScale = smokeSettings.noiseScale;
        this.noiseSpeed = smokeSettings.noiseSpeed;
        this.lightSensitivity = smokeSettings.lightSensitivity;
        this.opacityMultiplier = smokeSettings.opacityMultiplier;
    }
    
    public float getLightSensitivity() {
        return lightSensitivity;
    }

    public float getOpacityMultiplier() {
        return opacityMultiplier;
    }

    /**
     * Creates a smoke particle with natural dispersion and color variation.
     * Smoke spreads more in vertical directions and uses Perlin noise for realistic patterns.
     */
    @Override
    protected void createParticle() {
        Vector3f position = new Vector3f(emitterPosition);
        Vector3f velocity = new Vector3f(direction).mul(settings.velocity);

        // For exhaust smoke, add spread perpendicular to main direction (+x)
        // Keep strong forward velocity, add spread only in y and z directions
        velocity.x += (float) ((Math.random() - 0.5) * settings.spread * 0.2f); // Minimal x spread
        velocity.y += (float) ((Math.random() - 0.5) * settings.spread); // Full y spread
        velocity.z += (float) ((Math.random() - 0.5) * settings.spread); // Full z spread

        // Smoke naturally rises and disperses without strong forces

        // Use Perlin noise for more coherent color variations
        float time = System.currentTimeMillis() * 0.001f; // Convert to seconds
        
        // Add random offset to position to ensure variation even when particles spawn at same spot
        float randomOffset = (float) Math.random() * 100.0f;
        
        // Quality-adjusted noise computation - reduce noise samples for lower quality
        float colorFactor = 0.5f;
        switch (settings.config.getQuality().getQuality()) {
            case MEDIUM, HIGH -> {
                // Full multiple octaves of noise for high quality
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
        
        // Normalize and enhance contrast
        colorFactor = Math.max(0.0f, Math.min(1.0f, colorFactor)); // Clamp to [0,1]
        
        // Interpolate between min and max color using noise factor
        float r = settings.minColor.x + colorFactor * (settings.maxColor.x - settings.minColor.x);
        float g = settings.minColor.y + colorFactor * (settings.maxColor.y - settings.minColor.y);
        float b = settings.minColor.z + colorFactor * (settings.maxColor.z - settings.minColor.z);
        Vector3f color = new Vector3f(r, g, b);

        float size = (float) (Math.random() * (settings.maxSize - settings.minSize) + settings.minSize);
        float life = (float) (Math.random() * (settings.maxLife - settings.minLife) + settings.minLife);
        
        // Random 3D orientation
        Quaternionf orientation = new Quaternionf().rotateXYZ(
            (float) (Math.random() * Math.PI * 2), // Random X rotation
            (float) (Math.random() * Math.PI * 2), // Random Y rotation  
            (float) (Math.random() * Math.PI * 2)  // Random Z rotation
        );
        
        // Random 3D angular velocity (rotation around random axes)
        Vector3f angularVelocity = new Vector3f(
            (float) ((Math.random() - 0.5) * 0.3), // X axis rotation speed
            (float) ((Math.random() - 0.5) * 0.3), // Y axis rotation speed
            (float) ((Math.random() - 0.5) * 0.3)  // Z axis rotation speed
        );

        particles.add(new Particle(position, velocity, color, size, life, orientation, angularVelocity));
    }
}
