package info.openrocket.swing.gui.figure3d.scene.properties;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Configuration for visual effects within the OpenRocket 3D visualization system.
 * This class manages settings that control dynamic visual elements and post-processing
 * effects that enhance the realism and informativeness of the rocket visualization.
 * 
 * <p>The visual effects system provides control over:</p>
 * <ul>
 *   <li><b>Particle effects:</b> Realistic motor exhaust including flames, smoke, and sparks</li>
 *   <li><b>Motion blur:</b> Cinematic effects for dynamic camera movement</li>
 *   <li><b>Display elements:</b> Helper visualizations like coordinate axes</li>
 *   <li><b>Per-motor control:</b> Individual particle settings for multi-stage rockets</li>
 * </ul>
 * 
 * <p>These effects integrate with the rendering pipeline to provide realistic
 * visualization of rocket operation while maintaining performance through configurable
 * quality settings and selective enabling of effect types.</p>
 */
public class VisualEffectsSettings {

    // Motion blur settings
    private boolean motionBlurEnabled = false;
    private float motionBlurFactor = 5.0f;
    
    // Display elements
    private boolean originAxesVisible = false;
    private boolean caretScaleWithView = false;
    
    // Global particle settings
    private boolean particleEffectsEnabled = true;
    private boolean staticParticles = true;
    private float particleTime = 10.0f;
    
    // Individual particle type settings
    private boolean sparkParticlesEnabled = true;
    private boolean smokeParticlesEnabled = true;
    private boolean flameParticlesEnabled = true;
    
    // Per-motor particle control (motor component ID -> enabled state)
    private final Map<String, Boolean> perMotorParticleEnabled = new HashMap<>();

    // Motion Blur
    
    /**
     * Checks if motion blur post-processing effect is enabled.
     * Motion blur creates trailing effects during camera movement for cinematic visualization.
     * 
     * @return true if motion blur is active
     */
    public boolean isMotionBlurEnabled() {
        return motionBlurEnabled;
    }

    /**
     * Enables or disables motion blur post-processing effect.
     * 
     * @param motionBlurEnabled true to enable motion blur, false to disable
     */
    public void setMotionBlurEnabled(boolean motionBlurEnabled) {
        this.motionBlurEnabled = motionBlurEnabled;
    }

    /**
     * Gets the motion blur intensity factor.
     * Higher values create more pronounced blur effects during camera movement.
     * 
     * @return the motion blur intensity factor
     */
    public float getMotionBlurFactor() {
        return motionBlurFactor;
    }

    /**
     * Sets the motion blur intensity factor.
     * 
     * @param motionBlurFactor the intensity factor (typically 1.0 to 10.0)
     */
    public void setMotionBlurFactor(float motionBlurFactor) {
        this.motionBlurFactor = motionBlurFactor;
    }

    // Display Elements
    
    /**
     * Checks if coordinate origin axes are visible in the scene.
     * The axes provide spatial reference for understanding rocket orientation.
     * 
     * @return true if origin axes are displayed
     */
    public boolean isOriginAxesVisible() {
        return originAxesVisible;
    }

    /**
     * Shows or hides the coordinate origin axes in the scene.
     * 
     * @param originAxesVisible true to show axes, false to hide them
     */
    public void setOriginAxesVisible(boolean originAxesVisible) {
        this.originAxesVisible = originAxesVisible;
    }

    /**
     * @return true if CG/CP carets scale with the camera view (zoom)
     */
    public boolean isCaretScaleWithView() {
        return caretScaleWithView;
    }

    /**
     * Sets whether CG/CP carets scale with the camera view (zoom).
     * @param caretScaleWithView true to scale with view, false for fixed size
     */
    public void setCaretScaleWithView(boolean caretScaleWithView) {
        this.caretScaleWithView = caretScaleWithView;
    }

    // Global Particle Settings
    
    /**
     * Checks if particle effects are globally enabled.
     * This master setting controls all particle systems in the visualization.
     * 
     * @return true if particle effects are active
     */
    public boolean areParticleEffectsEnabled() {
        return particleEffectsEnabled;
    }

    /**
     * Globally enables or disables all particle effects.
     * When disabled, no particle systems will be rendered regardless of individual settings.
     * 
     * @param particleEffectsEnabled true to enable all particle systems, false to disable
     */
    public void setParticleEffectsEnabled(boolean particleEffectsEnabled) {
        this.particleEffectsEnabled = particleEffectsEnabled;
    }

    /**
     * Checks if particles are rendered in static mode.
     * Static particles freeze the particle simulation at a specific time point.
     * 
     * @return true if particles are static, false if they animate over time
     */
    public boolean areStaticParticles() {
        return staticParticles;
    }

    /**
     * Sets particle animation mode.
     * 
     * @param staticParticles true for frozen particle state, false for animated particles
     */
    public void setStaticParticles(boolean staticParticles) {
        this.staticParticles = staticParticles;
    }

    /**
     * Gets the particle simulation time when in static mode.
     * This determines which frame of the particle animation is displayed.
     * 
     * @return the simulation time in seconds
     */
    public float getParticleTime() {
        return particleTime;
    }

    /**
     * Sets the particle simulation time for static mode.
     * 
     * @param particleTime the simulation time in seconds
     */
    public void setParticleTime(float particleTime) {
        this.particleTime = particleTime;
    }

    // Individual Particle Types
    
    /**
     * Checks if spark particle effects are enabled.
     * Spark particles simulate high-energy debris from motor exhaust.
     * 
     * @return true if spark particles are active
     */
    public boolean areSparkParticlesEnabled() {
        return sparkParticlesEnabled;
    }

    /**
     * Enables or disables spark particle effects.
     * 
     * @param sparkParticlesEnabled true to enable spark particles, false to disable
     */
    public void setSparkParticlesEnabled(boolean sparkParticlesEnabled) {
        this.sparkParticlesEnabled = sparkParticlesEnabled;
    }

    /**
     * Checks if smoke particle effects are enabled.
     * Smoke particles simulate exhaust plumes and atmospheric interaction.
     * 
     * @return true if smoke particles are active
     */
    public boolean areSmokeParticlesEnabled() {
        return smokeParticlesEnabled;
    }

    /**
     * Enables or disables smoke particle effects.
     * 
     * @param smokeParticlesEnabled true to enable smoke particles, false to disable
     */
    public void setSmokeParticlesEnabled(boolean smokeParticlesEnabled) {
        this.smokeParticlesEnabled = smokeParticlesEnabled;
    }

    /**
     * Checks if flame particle effects are enabled.
     * Flame particles simulate the bright core of motor exhaust.
     * 
     * @return true if flame particles are active
     */
    public boolean areFlameParticlesEnabled() {
        return flameParticlesEnabled;
    }

    /**
     * Enables or disables flame particle effects.
     * 
     * @param flameParticlesEnabled true to enable flame particles, false to disable
     */
    public void setFlameParticlesEnabled(boolean flameParticlesEnabled) {
        this.flameParticlesEnabled = flameParticlesEnabled;
    }

    // Per-Motor Particle Control
    
    /**
     * Enable or disable particles for a specific motor component.
     * This allows fine-grained control over particle effects in multi-stage rockets
     * where different motors may be active at different times.
     * 
     * @param motorComponentId unique identifier for the motor component
     * @param enabled whether particles should be enabled for this motor
     */
    public void setMotorParticlesEnabled(String motorComponentId, boolean enabled) {
        perMotorParticleEnabled.put(motorComponentId, enabled);
    }
    
    /**
     * Check if particles are enabled for a specific motor component.
     * Returns true by default for motors that haven't been explicitly configured.
     * 
     * @param motorComponentId unique identifier for the motor component
     * @return true if particles are enabled for this motor, false otherwise
     */
    public boolean areMotorParticlesEnabled(String motorComponentId) {
        // Default to true if not explicitly set
        return perMotorParticleEnabled.getOrDefault(motorComponentId, true);
    }
    
    /**
     * Get all motor component IDs that have been explicitly configured.
     * This includes only motors that have had their particle settings modified.
     * 
     * @return set of motor component IDs with explicit particle settings
     */
    public Set<String> getConfiguredMotorIds() {
        return perMotorParticleEnabled.keySet();
    }
    
    /**
     * Enable particles for all previously configured motors.
     * This is a convenience method for bulk enabling of motor particle effects.
     */
    public void enableAllMotorParticles() {
        for (String motorId : perMotorParticleEnabled.keySet()) {
            perMotorParticleEnabled.put(motorId, true);
        }
    }
    
    /**
     * Disable particles for all previously configured motors.
     * This is a convenience method for bulk disabling of motor particle effects.
     */
    public void disableAllMotorParticles() {
        for (String motorId : perMotorParticleEnabled.keySet()) {
            perMotorParticleEnabled.put(motorId, false);
        }
    }
}
