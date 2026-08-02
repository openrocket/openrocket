package info.openrocket.swing.gui.figure3d.scene.properties;

import info.openrocket.core.util.MathUtil;
import info.openrocket.swing.gui.figure3d.constants.CameraConstants;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Visual-effects settings for figure3d.
 *
 * Includes particles, motion blur, helper overlays, and related tuning values.
 */
public class VisualEffectsSettings {
    public static final boolean DEFAULT_MOTION_BLUR_ENABLED = false;
    public static final float DEFAULT_MOTION_BLUR_FACTOR = 5.0f;
    public static final boolean DEFAULT_ORIGIN_AXES_VISIBLE = false;
    public static final boolean DEFAULT_LIGHT_VISUALIZERS_VISIBLE = false;
    public static final boolean DEFAULT_CAMERA_POINT_OF_INTEREST_VISIBLE = false;
    public static final boolean DEFAULT_CARETS_VISIBLE = true;
    public static final boolean DEFAULT_ROTATE_ROCKET_ON_DRAG = true;
    public static final float DEFAULT_DRAG_ROTATION_SENSITIVITY = CameraConstants.DEFAULT_ROTATION_SENSITIVITY_FACTOR;
    public static final boolean DEFAULT_CARET_SCALE_WITH_VIEW = false;
    public static final float DEFAULT_AMBIENT_LIGHT_FACTOR = 0.1f;
    public static final boolean DEFAULT_PARTICLE_EFFECTS_ENABLED = true;
    public static final boolean DEFAULT_STATIC_PARTICLES = true;
    public static final float DEFAULT_PARTICLE_TIME = 10.0f;
    public static final boolean DEFAULT_SPARK_PARTICLES_ENABLED = true;
    public static final boolean DEFAULT_SMOKE_PARTICLES_ENABLED = true;
    public static final boolean DEFAULT_FLAME_PARTICLES_ENABLED = true;
    public static final float DEFAULT_SMOKE_RED = 0.9f;
    public static final float DEFAULT_SMOKE_GREEN = 0.9f;
    public static final float DEFAULT_SMOKE_BLUE = 0.9f;
    public static final float DEFAULT_FLAME_RED = 1.0f;
    public static final float DEFAULT_FLAME_GREEN = 0.4f;
    public static final float DEFAULT_FLAME_BLUE = 0.2f;
    public static final float DEFAULT_SMOKE_OPACITY = 1.0f;
    public static final float DEFAULT_EXHAUST_SCALE = 1.0f;
    public static final float DEFAULT_FLAME_ASPECT_RATIO = 1.0f;
    public static final float DEFAULT_SPARK_CONCENTRATION = 1.0f;
    public static final float DEFAULT_SPARK_WEIGHT = 0.0f;
    public static final float DEFAULT_PARTICLE_LENGTH_SCALE = 1.0f;
    public static final float DEFAULT_SMOKE_LENGTH_SCALE = 1.0f;
    public static final float DEFAULT_FLAME_EXPOSURE_SCALE = 0.1f;
    public static final float DEFAULT_SPARK_SPREAD_SCALE = 1.0f;

    // Motion blur settings
    private boolean motionBlurEnabled = DEFAULT_MOTION_BLUR_ENABLED;
    private float motionBlurFactor = DEFAULT_MOTION_BLUR_FACTOR;
    
    // Display elements
    private boolean originAxesVisible = DEFAULT_ORIGIN_AXES_VISIBLE;
    private boolean lightVisualizersVisible = DEFAULT_LIGHT_VISUALIZERS_VISIBLE;
    private boolean cameraPointOfInterestVisible = DEFAULT_CAMERA_POINT_OF_INTEREST_VISIBLE;
    private boolean caretsVisible = DEFAULT_CARETS_VISIBLE;
    private boolean rotateRocketOnDrag = DEFAULT_ROTATE_ROCKET_ON_DRAG;
    private float dragRotationSensitivity = DEFAULT_DRAG_ROTATION_SENSITIVITY;
    private boolean caretScaleWithView = DEFAULT_CARET_SCALE_WITH_VIEW;
    private float ambientLightFactor = DEFAULT_AMBIENT_LIGHT_FACTOR;
    
    // Global particle settings
    private boolean particleEffectsEnabled = DEFAULT_PARTICLE_EFFECTS_ENABLED;
    private boolean staticParticles = DEFAULT_STATIC_PARTICLES;
    private float particleTime = DEFAULT_PARTICLE_TIME;
    
    // Individual particle type settings
    private boolean sparkParticlesEnabled = DEFAULT_SPARK_PARTICLES_ENABLED;
    private boolean smokeParticlesEnabled = DEFAULT_SMOKE_PARTICLES_ENABLED;
    private boolean flameParticlesEnabled = DEFAULT_FLAME_PARTICLES_ENABLED;
    private Vector3f smokeColor = createDefaultSmokeColor();
    private Vector3f flameColor = createDefaultFlameColor();
    private float smokeOpacity = DEFAULT_SMOKE_OPACITY;
    private float exhaustScale = DEFAULT_EXHAUST_SCALE;
    private float flameAspectRatio = DEFAULT_FLAME_ASPECT_RATIO;
    private float sparkConcentration = DEFAULT_SPARK_CONCENTRATION;
    private float sparkWeight = DEFAULT_SPARK_WEIGHT;
    private float particleLengthScale = DEFAULT_PARTICLE_LENGTH_SCALE;
    private float smokeLengthScale = DEFAULT_SMOKE_LENGTH_SCALE;
    private float flameExposureScale = DEFAULT_FLAME_EXPOSURE_SCALE;
    private float sparkSpreadScale = DEFAULT_SPARK_SPREAD_SCALE;
    
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
     * Checks if scene light visualizers should be shown.
     *
     * @return true if light visualizers are visible
     */
    public boolean areLightVisualizersVisible() {
        return lightVisualizersVisible;
    }

    /**
     * Shows or hides scene light visualizers.
     *
     * @param lightVisualizersVisible true to show light visuals, false to hide them
     */
    public void setLightVisualizersVisible(boolean lightVisualizersVisible) {
        this.lightVisualizersVisible = lightVisualizersVisible;
    }

    /**
     * Checks if the camera point-of-interest marker should be shown.
     *
     * @return true if the camera pivot marker is visible
     */
    public boolean isCameraPointOfInterestVisible() {
        return cameraPointOfInterestVisible;
    }

    /**
     * Shows or hides the camera point-of-interest marker.
     *
     * @param cameraPointOfInterestVisible true to show the marker, false to hide it
     */
    public void setCameraPointOfInterestVisible(boolean cameraPointOfInterestVisible) {
        this.cameraPointOfInterestVisible = cameraPointOfInterestVisible;
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
     * @return true if mouse drag rotates the rocket instead of orbiting the camera
     */
    public boolean isRotateRocketOnDrag() {
        return rotateRocketOnDrag;
    }

    /**
     * Sets whether mouse drag rotates the rocket instead of orbiting the camera.
     * @param rotateRocketOnDrag true to rotate the rocket, false to rotate the view
     */
    public void setRotateRocketOnDrag(boolean rotateRocketOnDrag) {
        this.rotateRocketOnDrag = rotateRocketOnDrag;
    }

    /**
     * @return the mouse-drag rotation sensitivity multiplier for 3D orbit controls
     */
    public float getDragRotationSensitivity() {
        return dragRotationSensitivity;
    }

    /**
     * Sets the mouse-drag rotation sensitivity multiplier for 3D orbit controls.
     * @param dragRotationSensitivity the sensitivity multiplier
     */
    public void setDragRotationSensitivity(float dragRotationSensitivity) {
        this.dragRotationSensitivity = Math.max(0.05f, dragRotationSensitivity);
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
        this.smokeOpacity = MathUtil.clamp(smokeOpacity, 0.0f, 1.0f);
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

    public float getParticleLengthScale() {
        return particleLengthScale;
    }

    public void setParticleLengthScale(float particleLengthScale) {
        this.particleLengthScale = Math.max(0.0f, particleLengthScale);
    }

    public float getSmokeLengthScale() {
        return smokeLengthScale;
    }

    public void setSmokeLengthScale(float smokeLengthScale) {
        this.smokeLengthScale = Math.max(0.0f, smokeLengthScale);
    }

    public float getFlameExposureScale() {
        return flameExposureScale;
    }

    public void setFlameExposureScale(float flameExposureScale) {
        this.flameExposureScale = Math.max(0.0f, flameExposureScale);
    }

    public float getSparkSpreadScale() {
        return sparkSpreadScale;
    }

    public void setSparkSpreadScale(float sparkSpreadScale) {
        this.sparkSpreadScale = Math.max(0.0f, sparkSpreadScale);
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

    /**
     * Restores the visual-effects settings to their built-in defaults.
     */
    public void resetToDefaults() {
        motionBlurEnabled = DEFAULT_MOTION_BLUR_ENABLED;
        motionBlurFactor = DEFAULT_MOTION_BLUR_FACTOR;
        originAxesVisible = DEFAULT_ORIGIN_AXES_VISIBLE;
        lightVisualizersVisible = DEFAULT_LIGHT_VISUALIZERS_VISIBLE;
        cameraPointOfInterestVisible = DEFAULT_CAMERA_POINT_OF_INTEREST_VISIBLE;
        caretsVisible = DEFAULT_CARETS_VISIBLE;
        rotateRocketOnDrag = DEFAULT_ROTATE_ROCKET_ON_DRAG;
        dragRotationSensitivity = DEFAULT_DRAG_ROTATION_SENSITIVITY;
        caretScaleWithView = DEFAULT_CARET_SCALE_WITH_VIEW;
        ambientLightFactor = DEFAULT_AMBIENT_LIGHT_FACTOR;
        particleEffectsEnabled = DEFAULT_PARTICLE_EFFECTS_ENABLED;
        staticParticles = DEFAULT_STATIC_PARTICLES;
        particleTime = DEFAULT_PARTICLE_TIME;
        sparkParticlesEnabled = DEFAULT_SPARK_PARTICLES_ENABLED;
        smokeParticlesEnabled = DEFAULT_SMOKE_PARTICLES_ENABLED;
        flameParticlesEnabled = DEFAULT_FLAME_PARTICLES_ENABLED;
        smokeColor = createDefaultSmokeColor();
        flameColor = createDefaultFlameColor();
        smokeOpacity = DEFAULT_SMOKE_OPACITY;
        exhaustScale = DEFAULT_EXHAUST_SCALE;
        flameAspectRatio = DEFAULT_FLAME_ASPECT_RATIO;
        sparkConcentration = DEFAULT_SPARK_CONCENTRATION;
        sparkWeight = DEFAULT_SPARK_WEIGHT;
        particleLengthScale = DEFAULT_PARTICLE_LENGTH_SCALE;
        smokeLengthScale = DEFAULT_SMOKE_LENGTH_SCALE;
        flameExposureScale = DEFAULT_FLAME_EXPOSURE_SCALE;
        sparkSpreadScale = DEFAULT_SPARK_SPREAD_SCALE;
        perMotorParticleEnabled.clear();
    }

    private static Vector3f createDefaultSmokeColor() {
        return new Vector3f(DEFAULT_SMOKE_RED, DEFAULT_SMOKE_GREEN, DEFAULT_SMOKE_BLUE);
    }

    private static Vector3f createDefaultFlameColor() {
        return new Vector3f(DEFAULT_FLAME_RED, DEFAULT_FLAME_GREEN, DEFAULT_FLAME_BLUE);
    }
}
