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
        quality.setShadowsEnabled(true);
        quality.setAmbientOcclusionEnabled(true);
        notifyListeners();
    }

    /**
     * Resets all settings to their default values.
     */
    public void resetToDefaults() {
        // Visual Effects defaults
        visualEffects.setMotionBlurEnabled(false);
        visualEffects.setMotionBlurFactor(5.0f);
        visualEffects.setOriginAxesVisible(false);
        visualEffects.setAmbientLightFactor(0.1f);
        visualEffects.setParticleEffectsEnabled(true);
        visualEffects.setStaticParticles(true);
        visualEffects.setParticleTime(10.0f);
        visualEffects.setSparkParticlesEnabled(true);
        visualEffects.setSmokeParticlesEnabled(true);
        visualEffects.setFlameParticlesEnabled(true);
        visualEffects.setSmokeOpacity(1.0f);
        visualEffects.setExhaustScale(1.0f);
        visualEffects.setFlameAspectRatio(1.0f);
        visualEffects.setSparkConcentration(1.0f);
        visualEffects.setSparkWeight(0.0f);

        // Quality defaults
        quality.setQuality(GraphicsQualitySettings.RenderQuality.HIGH);
        quality.setXrayOpacity(0.1f);
        quality.setBackfaceCullingEnabled(true);
        quality.setRoughnessBumpEnabled(true);
        quality.setFXAAEnabled(true);
        quality.setShadowsEnabled(true);
        quality.setAmbientOcclusionEnabled(true);

        // Display defaults
        display.setMode(DisplaySettings.RenderMode.FINISHED);

        notifyListeners();
    }
    
    /**
     * Builder for {@link RenderingConfiguration}.
     */
    public static class Builder {
        private DisplaySettings.RenderMode renderMode = DisplaySettings.RenderMode.FINISHED;
        private GraphicsQualitySettings.RenderQuality quality = GraphicsQualitySettings.RenderQuality.HIGH;
        private boolean particleEffectsEnabled = true;
        private boolean motionBlurEnabled = false;
        private float motionBlurFactor = 5.0f;
        private boolean originAxesVisible = false;
        private boolean caretsVisible = true;
        private boolean caretScaleWithView = false;
        private boolean staticParticles = true;
        private float particleTime = 10.0f;
        private boolean sparkParticlesEnabled = true;
        private boolean smokeParticlesEnabled = true;
        private boolean flameParticlesEnabled = true;
        private float smokeOpacity = 1.0f;
        private float exhaustScale = 1.0f;
        private float flameAspectRatio = 1.0f;
        private float sparkConcentration = 1.0f;
        private float sparkWeight = 0.0f;
        private float xrayOpacity = 0.1f;
        private boolean backfaceCullingEnabled = true;
        private boolean roughnessBumpEnabled = true;
        private boolean fxaaEnabled = true;
        private boolean shadowsEnabled = true;
        private boolean ambientOcclusionEnabled = true;
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
         * Enables or disables CG/CP caret rendering.
         * @param visible Whether carets should be visible
         * @return This builder instance
         */
        public Builder withCarets(boolean visible) {
            this.caretsVisible = visible;
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
            config.getVisualEffects().setCaretsVisible(caretsVisible);
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
