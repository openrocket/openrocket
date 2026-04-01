package info.openrocket.swing.gui.figure3d.scene.properties;

import org.joml.Vector3f;

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
    private boolean caretsVisible = true;
    private boolean caretScaleWithView = false;
    private float ambientLightFactor = 0.1f;
    
    // Global particle settings
    private boolean particleEffectsEnabled = true;
    private boolean staticParticles = true;
    private float particleTime = 10.0f;
    
    // Individual particle type settings
    private boolean sparkParticlesEnabled = true;
    private boolean smokeParticlesEnabled = true;
    private boolean flameParticlesEnabled = true;
    private Vector3f smokeColor = new Vector3f(0.9f, 0.9f, 0.9f);
    private Vector3f flameColor = new Vector3f(1.0f, 0.4f, 0.2f);
    private float smokeOpacity = 1.0f;
    private float exhaustScale = 1.0f;
    private float flameAspectRatio = 1.0f;
    private float sparkConcentration = 1.0f;
    private float sparkWeight = 0.0f;
    
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
     * Checks if CG/CP carets should be rendered.
     *
     * @return true if carets are visible
     */
    public boolean areCaretsVisible() {
        return caretsVisible;
    }

    /**
     * Shows or hides the CG/CP carets.
     *
     * @param caretsVisible true to render carets, false to suppress them
     */
    public void setCaretsVisible(boolean caretsVisible) {
        this.caretsVisible = caretsVisible;
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

    /**
     * Gets the ambient light factor applied to lit geometry.
     *
     * @return ambient light strength, where 0 disables ambient fill light
     */
    public float getAmbientLightFactor() {
        return ambientLightFactor;
    }

    /**
     * Sets the ambient light factor applied to lit geometry.
     *
     * @param ambientLightFactor ambient light strength
     */
    public void setAmbientLightFactor(float ambientLightFactor) {
        this.ambientLightFactor = Math.max(0.0f, ambientLightFactor);
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

    /**
     * Gets the base smoke color used for smoke particle generation.
     *
     * @return RGB smoke color in 0..1 space
     */
    public Vector3f getSmokeColor() {
        return new Vector3f(smokeColor);
    }

    /**
     * Sets the base smoke color used for smoke particle generation.
     *
     * @param smokeColor RGB smoke color in 0..1 space
     */
    public void setSmokeColor(Vector3f smokeColor) {
        if (smokeColor == null) {
            throw new IllegalArgumentException("smokeColor must not be null");
        }
        this.smokeColor = new Vector3f(smokeColor);
    }

    /**
     * Gets the base flame color used for flame particle generation.
     *
     * @return RGB flame color in 0..1 space
     */
    public Vector3f getFlameColor() {
        return new Vector3f(flameColor);
    }

    /**
     * Sets the base flame color used for flame particle generation.
     *
     * @param flameColor RGB flame color in 0..1 space
     */
    public void setFlameColor(Vector3f flameColor) {
        if (flameColor == null) {
            throw new IllegalArgumentException("flameColor must not be null");
        }
        this.flameColor = new Vector3f(flameColor);
    }

    public float getSmokeOpacity() {
        return smokeOpacity;
    }

    public void setSmokeOpacity(float smokeOpacity) {
        this.smokeOpacity = Math.max(0.0f, Math.min(1.0f, smokeOpacity));
    }

    public float getExhaustScale() {
        return exhaustScale;
    }

    public void setExhaustScale(float exhaustScale) {
        this.exhaustScale = Math.max(0.0f, exhaustScale);
    }

    public float getFlameAspectRatio() {
        return flameAspectRatio;
    }

    public void setFlameAspectRatio(float flameAspectRatio) {
        this.flameAspectRatio = Math.max(0.0f, flameAspectRatio);
    }

    public float getSparkConcentration() {
        return sparkConcentration;
    }

    public void setSparkConcentration(float sparkConcentration) {
        this.sparkConcentration = Math.max(0.0f, sparkConcentration);
    }

    public float getSparkWeight() {
        return sparkWeight;
    }

    public void setSparkWeight(float sparkWeight) {
        this.sparkWeight = Math.max(0.0f, sparkWeight);
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
