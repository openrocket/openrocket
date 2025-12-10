package info.openrocket.swing.gui.figure3d.core.particles.flame;

import info.openrocket.swing.gui.figure3d.core.particles.Particle;
import info.openrocket.swing.gui.figure3d.core.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.core.particles.PerlinNoise;
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
    }
    
    public float getFlickerIntensity() {
        return flickerIntensity;
    }
    
    public float getLightIntensity() {
        return lightIntensity;
    }
    
    /**
     * Calculate the average light position and intensity from all flame particles
     */
    public Vector3f calculateLightPosition() {
        if (particles.isEmpty()) {
            return new Vector3f(emitterPosition);
        }
        
        Vector3f avgPosition = new Vector3f();
        float totalWeight = 0.0f;
        
        for (Particle particle : particles) {
            float ageRatio = 1.0f - (particle.getLife() / particle.getMaxLife());
            float weight = 1.0f - ageRatio; // Younger particles contribute more
            
            avgPosition.add(new Vector3f(particle.getPosition()).mul(weight));
            totalWeight += weight;
        }
        
        if (totalWeight > 0) {
            avgPosition.div(totalWeight);
        } else {
            avgPosition.set(emitterPosition);
        }
        
        return avgPosition;
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
        
        return Math.min(intensity * 0.01f, lightIntensity); // Scale down and cap
    }
    
    /**
     * Updates this flame's light properties. Creates the light if it doesn't exist.
     */
    public void updateFlameLight() {
        float intensity = calculateEffectiveLightIntensity();
        boolean shouldHaveLight = intensity > 0.1f;
        
        if (shouldHaveLight) {
            Vector3f position = calculateLightPosition();
            
            // Create bright orange flame color with intensity baked in
            float lightIntensity = Math.min(intensity * 8.0f, 3.0f); // Cap at 3.0 for bright but not overwhelming
            Vector3f flameColor = new Vector3f(
                1.0f * lightIntensity,  // Bright red component
                0.5f * lightIntensity,  // Medium green component  
                0.1f * lightIntensity   // Low blue component
            );
            
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
        velocity.x += (float) ((Math.random() - 0.5) * settings.spread);
        velocity.y += Math.abs((float) ((Math.random() - 0.5) * settings.spread * 0.5f)); // Mostly upward
        velocity.z += (float) ((Math.random() - 0.5) * settings.spread);

        // Use Perlin noise for flame color variation (creates realistic flame patterns)
        float time = System.currentTimeMillis() * 0.001f;
        
        // Add random offset for each particle
        float randomOffset = (float) Math.random() * 100.0f;
        
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
        colorFactor = Math.max(0.0f, Math.min(1.0f, colorFactor));
        
        // Interpolate between flame colors (red/orange to yellow/white)
        float r = settings.minColor.x + colorFactor * (settings.maxColor.x - settings.minColor.x);
        float g = settings.minColor.y + colorFactor * (settings.maxColor.y - settings.minColor.y);
        float b = settings.minColor.z + colorFactor * (settings.maxColor.z - settings.minColor.z);
        Vector3f color = new Vector3f(r, g, b);

        float size = (float) (Math.random() * (settings.maxSize - settings.minSize) + settings.minSize);
        float life = (float) (Math.random() * (settings.maxLife - settings.minLife) + settings.minLife);
        
        // Flames have more chaotic orientation and movement
        Quaternionf orientation = new Quaternionf().rotateXYZ(
            (float) (Math.random() * Math.PI * 2),
            (float) (Math.random() * Math.PI * 2), 
            (float) (Math.random() * Math.PI * 2)
        );
        
        // More dramatic angular velocity for flame flicker
        Vector3f angularVelocity = new Vector3f(
            (float) ((Math.random() - 0.5) * 2.0 * flickerIntensity),
            (float) ((Math.random() - 0.5) * 2.0 * flickerIntensity),
            (float) ((Math.random() - 0.5) * 2.0 * flickerIntensity)
        );

        particles.add(new Particle(position, velocity, color, size, life, orientation, angularVelocity));
    }
}