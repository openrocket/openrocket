package info.openrocket.swing.gui.figure3d.scene.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Combined display, quality, and visual-effects settings for a figure3d view.
 */
public class RenderingConfiguration {

    private final VisualEffectsSettings visualEffects;
    private final GraphicsQualitySettings quality;
    private final DisplaySettings display;
    
    private final List<Consumer<RenderingConfiguration>> listeners = new ArrayList<>();

    /**
     * Creates a new rendering configuration with default settings.
     */
    private RenderingConfiguration() {
        this.visualEffects = new VisualEffectsSettings();
        this.quality = new GraphicsQualitySettings();
        this.display = new DisplaySettings();
    }

    /**
     * Gets the visual effects settings (particles, motion blur, display elements).
     * 
     * @return The visual effects configuration
     */
    public VisualEffectsSettings getVisualEffects() {
        return visualEffects;
    }

    /**
     * Gets the graphics quality settings (render quality, anti-aliasing, surface effects).
     * 
     * @return The graphics quality configuration
     */
    public GraphicsQualitySettings getQuality() {
        return quality;
    }

    /**
     * Gets the display settings (render modes, wireframe, transparency).
     * 
     * @return The display configuration
     */
    public DisplaySettings getDisplay() {
        return display;
    }

    // Change Notification System

    /**
     * Adds a listener that will be notified when any configuration changes.
     * 
     * @param listener The listener to add
     */
    public void addListener(Consumer<RenderingConfiguration> listener) {
        listeners.add(listener);
    }

    /**
     * Removes a previously added listener.
     * 
     * @param listener The listener to remove
     */
    public void removeListener(Consumer<RenderingConfiguration> listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies listeners after the configuration has changed.
     */
    public void notifyListeners() {
        for (Consumer<RenderingConfiguration> listener : listeners) {
            listener.accept(this);
        }
    }

    // Convenience Methods for Common Operations

    /**
     * Configures settings for optimal performance (lower quality, fewer effects).
     */
    public void setPerformanceMode() {
        quality.setQuality(GraphicsQualitySettings.RenderQuality.LOW);
        quality.setFXAAEnabled(false);
        quality.setRoughnessBumpEnabled(false);
        quality.setShadowsEnabled(false);
        quality.setAmbientOcclusionEnabled(false);
        notifyListeners();
    }

    /**
     * Configures settings for maximum visual quality.
     */
    public void setQualityMode() {
        quality.setQuality(GraphicsQualitySettings.RenderQuality.HIGH);
        quality.setFXAAEnabled(true);
        quality.setRoughnessBumpEnabled(true);
        quality.setShadowsEnabled(false);
        quality.setAmbientOcclusionEnabled(false);
        notifyListeners();
    }

    /**
     * Resets all settings to their default values.
     */
    public void resetToDefaults() {
        visualEffects.resetToDefaults();
        quality.resetToDefaults();
        display.resetToDefaults();

        notifyListeners();
    }
    
    /**
     * Builder for {@link RenderingConfiguration}.
     */
    public static class Builder {
        private DisplaySettings.RenderMode renderMode = DisplaySettings.DEFAULT_RENDER_MODE;
        private GraphicsQualitySettings.RenderQuality quality = GraphicsQualitySettings.DEFAULT_QUALITY;
        private boolean particleEffectsEnabled = VisualEffectsSettings.DEFAULT_PARTICLE_EFFECTS_ENABLED;
        private boolean motionBlurEnabled = VisualEffectsSettings.DEFAULT_MOTION_BLUR_ENABLED;
        private float motionBlurFactor = VisualEffectsSettings.DEFAULT_MOTION_BLUR_FACTOR;
        private boolean originAxesVisible = VisualEffectsSettings.DEFAULT_ORIGIN_AXES_VISIBLE;
        private boolean lightVisualizersVisible = VisualEffectsSettings.DEFAULT_LIGHT_VISUALIZERS_VISIBLE;
        private boolean caretsVisible = VisualEffectsSettings.DEFAULT_CARETS_VISIBLE;
        private boolean rotateRocketOnDrag = VisualEffectsSettings.DEFAULT_ROTATE_ROCKET_ON_DRAG;
        private float dragRotationSensitivity = VisualEffectsSettings.DEFAULT_DRAG_ROTATION_SENSITIVITY;
        private boolean caretScaleWithView = VisualEffectsSettings.DEFAULT_CARET_SCALE_WITH_VIEW;
        private boolean staticParticles = VisualEffectsSettings.DEFAULT_STATIC_PARTICLES;
        private float particleTime = VisualEffectsSettings.DEFAULT_PARTICLE_TIME;
        private boolean sparkParticlesEnabled = VisualEffectsSettings.DEFAULT_SPARK_PARTICLES_ENABLED;
        private boolean smokeParticlesEnabled = VisualEffectsSettings.DEFAULT_SMOKE_PARTICLES_ENABLED;
        private boolean flameParticlesEnabled = VisualEffectsSettings.DEFAULT_FLAME_PARTICLES_ENABLED;
        private float smokeOpacity = VisualEffectsSettings.DEFAULT_SMOKE_OPACITY;
        private float exhaustScale = VisualEffectsSettings.DEFAULT_EXHAUST_SCALE;
        private float flameAspectRatio = VisualEffectsSettings.DEFAULT_FLAME_ASPECT_RATIO;
        private float sparkConcentration = VisualEffectsSettings.DEFAULT_SPARK_CONCENTRATION;
        private float sparkWeight = VisualEffectsSettings.DEFAULT_SPARK_WEIGHT;
        private float xrayOpacity = GraphicsQualitySettings.DEFAULT_XRAY_OPACITY;
        private boolean backfaceCullingEnabled = GraphicsQualitySettings.DEFAULT_BACKFACE_CULLING;
        private boolean roughnessBumpEnabled = GraphicsQualitySettings.DEFAULT_ROUGHNESS_BUMP;
        private boolean fxaaEnabled = GraphicsQualitySettings.DEFAULT_FXAA;
        private boolean shadowsEnabled = GraphicsQualitySettings.DEFAULT_SHADOWS;
        private boolean ambientOcclusionEnabled = GraphicsQualitySettings.DEFAULT_AMBIENT_OCCLUSION;
        private boolean usePerformanceMode = false;
        private boolean useQualityMode = false;
        private final List<Consumer<RenderingConfiguration>> listeners = new ArrayList<>();
        
        /**
         * Sets the render mode.
         * @param renderMode The render mode to use
         * @return This builder instance
         */
        public Builder withRenderMode(DisplaySettings.RenderMode renderMode) {
            this.renderMode = renderMode;
            return this;
        }
        
        /**
         * Sets the overall graphics quality level.
         * @param quality The quality level to use
         * @return This builder instance
         */
        public Builder withQuality(GraphicsQualitySettings.RenderQuality quality) {
            this.quality = quality;
            return this;
        }
        
        /**
         * Enables or disables particle effects.
         * @param enabled Whether particle effects should be enabled
         * @return This builder instance
         */
        public Builder withParticleEffects(boolean enabled) {
            this.particleEffectsEnabled = enabled;
            return this;
        }
        
        /**
         * Configures motion blur settings.
         * @param enabled Whether motion blur should be enabled
         * @param factor The motion blur factor (default: 5.0)
         * @return This builder instance
         */
        public Builder withMotionBlur(boolean enabled, float factor) {
            this.motionBlurEnabled = enabled;
            this.motionBlurFactor = factor;
            return this;
        }
        
        /**
         * Enables or disables motion blur with default factor.
         * @param enabled Whether motion blur should be enabled
         * @return This builder instance
         */
        public Builder withMotionBlur(boolean enabled) {
            this.motionBlurEnabled = enabled;
            return this;
        }
        
        /**
         * Configures individual particle types.
         * @param sparks Whether spark particles should be enabled
         * @param smoke Whether smoke particles should be enabled
         * @param flames Whether flame particles should be enabled
         * @return This builder instance
         */
        public Builder withParticleTypes(boolean sparks, boolean smoke, boolean flames) {
            this.sparkParticlesEnabled = sparks;
            this.smokeParticlesEnabled = smoke;
            this.flameParticlesEnabled = flames;
            return this;
        }
        
        /**
         * Sets the X-ray opacity for transparent rendering.
         * @param opacity The opacity value (0.0 to 1.0)
         * @return This builder instance
         */
        public Builder withXrayOpacity(float opacity) {
            this.xrayOpacity = Math.max(0.0f, Math.min(1.0f, opacity));
            return this;
        }
        
        /**
         * Enables or disables FXAA anti-aliasing.
         * @param enabled Whether FXAA should be enabled
         * @return This builder instance
         */
        public Builder withFXAA(boolean enabled) {
            this.fxaaEnabled = enabled;
            return this;
        }
        
        /**
         * Enables or disables roughness bump mapping.
         * @param enabled Whether roughness bump mapping should be enabled
         * @return This builder instance
         */
        public Builder withRoughnessBump(boolean enabled) {
            this.roughnessBumpEnabled = enabled;
            return this;
        }

        /**
         * Enables or disables shadow rendering.
         * @param enabled whether shadows should be rendered
         * @return This builder instance
         */
        public Builder withShadows(boolean enabled) {
            this.shadowsEnabled = enabled;
            return this;
        }

        /**
         * Enables or disables ambient occlusion post-processing.
         * @param enabled whether ambient occlusion should be enabled
         * @return This builder instance
         */
        public Builder withAmbientOcclusion(boolean enabled) {
            this.ambientOcclusionEnabled = enabled;
            return this;
        }
        
        /**
         * Enables or disables backface culling.
         * @param enabled Whether backface culling should be enabled
         * @return This builder instance
         */
        public Builder withBackfaceCulling(boolean enabled) {
            this.backfaceCullingEnabled = enabled;
            return this;
        }
        
        /**
         * Enables or disables origin axes visualization.
         * @param visible Whether origin axes should be visible
         * @return This builder instance
         */
        public Builder withOriginAxes(boolean visible) {
            this.originAxesVisible = visible;
            return this;
        }

        /**
         * Enables or disables light visualizers.
         * @param visible Whether light visualizers should be visible
         * @return This builder instance
         */
        public Builder withLightVisualizers(boolean visible) {
            this.lightVisualizersVisible = visible;
            return this;
        }

        /**
         * Enables or disables CG/CP caret rendering.
         * @param visible Whether carets should be visible
         * @return This builder instance
         */
        public Builder withCarets(boolean visible) {
            this.caretsVisible = visible;
            return this;
        }

        /**
         * Sets whether drag rotates the rocket instead of orbiting the camera.
         * @param rotateRocketOnDrag true to rotate the rocket, false to rotate the view
         * @return This builder instance
         */
        public Builder withRotateRocketOnDrag(boolean rotateRocketOnDrag) {
            this.rotateRocketOnDrag = rotateRocketOnDrag;
            return this;
        }

        /**
         * Sets the mouse-drag rotation sensitivity for 3D orbit controls.
         * @param dragRotationSensitivity the sensitivity multiplier
         * @return This builder instance
         */
        public Builder withDragRotationSensitivity(float dragRotationSensitivity) {
            this.dragRotationSensitivity = dragRotationSensitivity;
            return this;
        }

        /**
         * Sets whether CG/CP carets scale with the camera view (zoom).
         * @param scaleWithView true to scale with view, false for fixed size
         * @return This builder instance
         */
        public Builder withCaretScaleWithView(boolean scaleWithView) {
            this.caretScaleWithView = scaleWithView;
            return this;
        }
        
        /**
         * Configures particle timing.
         * @param staticParticles Whether particles should be static
         * @param particleTime The particle simulation time
         * @return This builder instance
         */
        public Builder withParticleTiming(boolean staticParticles, float particleTime) {
            this.staticParticles = staticParticles;
            this.particleTime = particleTime;
            return this;
        }
        
        /**
         * Applies performance-optimized settings (overrides individual quality settings).
         * @return This builder instance
         */
        public Builder withPerformanceMode() {
            this.usePerformanceMode = true;
            this.useQualityMode = false;
            return this;
        }
        
        /**
         * Applies quality-optimized settings (overrides individual quality settings).
         * @return This builder instance
         */
        public Builder withQualityMode() {
            this.usePerformanceMode = false;
            this.useQualityMode = true;
            return this;
        }
        
        /**
         * Adds a listener that will be notified of configuration changes.
         * @param listener The listener to add
         * @return This builder instance
         */
        public Builder withListener(Consumer<RenderingConfiguration> listener) {
            this.listeners.add(listener);
            return this;
        }
        
        /**
         * Builds the RenderingConfiguration with the specified settings.
         * @return A new RenderingConfiguration instance
         */
        public RenderingConfiguration build() {
            RenderingConfiguration config = new RenderingConfiguration();
            
            // Apply display settings
            config.getDisplay().setMode(renderMode);
            
            // Apply quality settings
            config.getQuality().setQuality(quality);
            config.getQuality().setXrayOpacity(xrayOpacity);
            config.getQuality().setBackfaceCullingEnabled(backfaceCullingEnabled);
            config.getQuality().setRoughnessBumpEnabled(roughnessBumpEnabled);
            config.getQuality().setFXAAEnabled(fxaaEnabled);
            config.getQuality().setShadowsEnabled(shadowsEnabled);
            config.getQuality().setAmbientOcclusionEnabled(ambientOcclusionEnabled);
            
            // Apply visual effects settings
            config.getVisualEffects().setParticleEffectsEnabled(particleEffectsEnabled);
            config.getVisualEffects().setMotionBlurEnabled(motionBlurEnabled);
            config.getVisualEffects().setMotionBlurFactor(motionBlurFactor);
            config.getVisualEffects().setOriginAxesVisible(originAxesVisible);
            config.getVisualEffects().setLightVisualizersVisible(lightVisualizersVisible);
            config.getVisualEffects().setCaretsVisible(caretsVisible);
            config.getVisualEffects().setRotateRocketOnDrag(rotateRocketOnDrag);
            config.getVisualEffects().setDragRotationSensitivity(dragRotationSensitivity);
            config.getVisualEffects().setCaretScaleWithView(caretScaleWithView);
            config.getVisualEffects().setStaticParticles(staticParticles);
            config.getVisualEffects().setParticleTime(particleTime);
            config.getVisualEffects().setSparkParticlesEnabled(sparkParticlesEnabled);
            config.getVisualEffects().setSmokeParticlesEnabled(smokeParticlesEnabled);
            config.getVisualEffects().setFlameParticlesEnabled(flameParticlesEnabled);
            config.getVisualEffects().setSmokeOpacity(smokeOpacity);
            config.getVisualEffects().setExhaustScale(exhaustScale);
            config.getVisualEffects().setFlameAspectRatio(flameAspectRatio);
            config.getVisualEffects().setSparkConcentration(sparkConcentration);
            config.getVisualEffects().setSparkWeight(sparkWeight);
            
            // Apply performance/quality modes (these override individual settings)
            if (usePerformanceMode) {
                config.setPerformanceMode();
            } else if (useQualityMode) {
                config.setQualityMode();
            }
            
            // Add listeners
            for (Consumer<RenderingConfiguration> listener : listeners) {
                config.addListener(listener);
            }
            
            return config;
        }
    }
    
    /**
     * Creates a new rendering configuration builder.
     * @return A new RenderingConfiguration.Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
}
