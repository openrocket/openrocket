package info.openrocket.swing.gui.figure3d.scene.properties;

/**
 * Configuration for graphics quality and rendering techniques in the OpenRocket 3D visualization.
 * This class manages low-level rendering settings that directly impact both visual quality and
 * performance, allowing users to balance rendering fidelity with system capabilities.
 * 
 * <p>The graphics quality settings control:</p>
 * <ul>
 *   <li><b>Mesh tessellation:</b> Level of detail for curved surfaces and complex geometry</li>
 *   <li><b>Surface effects:</b> Advanced material properties like roughness and bump mapping</li>
 *   <li><b>Anti-aliasing:</b> Edge smoothing techniques to reduce visual artifacts</li>
 *   <li><b>Transparency:</b> Opacity levels for special rendering modes</li>
 *   <li><b>Culling optimization:</b> GPU performance optimizations for hidden surface removal</li>
 * </ul>
 * 
 * <p>These settings integrate with the shader system and geometry generators to provide
 * scalable rendering quality that can adapt to different hardware capabilities and
 * user preferences for the OpenRocket 3D rocket visualization.</p>
 */
public class GraphicsQualitySettings {
    public static final RenderQuality DEFAULT_QUALITY = RenderQuality.HIGH;
    public static final float DEFAULT_XRAY_OPACITY = 0.1f;
    public static final boolean DEFAULT_BACKFACE_CULLING = true;
    public static final boolean DEFAULT_ROUGHNESS_BUMP = true;
    public static final boolean DEFAULT_FXAA = true;
    public static final boolean DEFAULT_SHADOWS = false;
    public static final boolean DEFAULT_AMBIENT_OCCLUSION = false;
    public static final boolean DEFAULT_REDUCE_EFFECTS_DURING_INTERACTION = false;

    /**
     * Defines overall rendering quality levels that affect multiple visual aspects.
     * The quality level influences mesh tessellation detail, particle system density,
     * surface effect complexity, and other performance-sensitive rendering features.
     */
    public enum RenderQuality {
        /** Optimized for performance: lower mesh detail, reduced particle count, basic effects */
        LOW,
        /** Balanced rendering: moderate detail level with good performance */
        MEDIUM,
        /** Maximum visual quality: high mesh detail, full particle effects, advanced shading */
        HIGH
    }

    private RenderQuality quality = DEFAULT_QUALITY;
    private float xrayOpacity = DEFAULT_XRAY_OPACITY;
    private boolean useBackfaceCulling = DEFAULT_BACKFACE_CULLING;
    private boolean enableRoughnessBump = DEFAULT_ROUGHNESS_BUMP;
    private boolean enableFXAA = DEFAULT_FXAA; // Fast Approximate Anti-Aliasing
    private boolean shadowsEnabled = DEFAULT_SHADOWS;
    private boolean ambientOcclusionEnabled = DEFAULT_AMBIENT_OCCLUSION;
    private volatile boolean reduceEffectsDuringInteraction = DEFAULT_REDUCE_EFFECTS_DURING_INTERACTION;

    // Render Quality
    
    /**
     * Gets the current overall rendering quality level.
     * 
     * @return the active RenderQuality setting
     */
    public RenderQuality getQuality() {
        return quality;
    }

    /**
     * Sets the overall rendering quality level.
     * This affects mesh tessellation, particle density, and effect complexity.
     * 
     * @param quality the RenderQuality level to apply
     */
    public void setQuality(RenderQuality quality) {
        this.quality = quality;
    }

    // Transparency and Culling
    
    /**
     * Gets the opacity level used for X-ray transparency rendering.
     * 
     * @return opacity value between 0.0 (fully transparent) and 1.0 (fully opaque)
     */
    public float getXrayOpacity() {
        return xrayOpacity;
    }

    /**
     * Sets the opacity level for X-ray transparency rendering.
     * 
     * @param xrayOpacity opacity value between 0.0 (fully transparent) and 1.0 (fully opaque)
     */
    public void setXrayOpacity(float xrayOpacity) {
        this.xrayOpacity = xrayOpacity;
    }

    /**
     * Checks if backface culling optimization is enabled.
     * When enabled, surfaces facing away from the camera are not rendered, improving performance.
     * 
     * @return true if backface culling is active
     */
    public boolean isBackfaceCullingEnabled() {
        return useBackfaceCulling;
    }

    /**
     * Enables or disables backface culling optimization.
     * Disabling culling is useful for wireframe modes or transparent materials.
     * 
     * @param useBackfaceCulling true to enable culling optimization, false to render all faces
     */
    public void setBackfaceCullingEnabled(boolean useBackfaceCulling) {
        this.useBackfaceCulling = useBackfaceCulling;
    }

    // Surface Effects
    
    /**
     * Checks if surface roughness bump mapping effects are enabled.
     * This adds surface detail through normal map perturbation for more realistic materials.
     * 
     * @return true if roughness bump mapping is active
     */
    public boolean isRoughnessBumpEnabled() {
        return enableRoughnessBump;
    }

    /**
     * Enables or disables surface roughness bump mapping effects.
     * When enabled, adds surface detail and realistic material appearance.
     * 
     * @param enableRoughnessBump true to enable advanced surface effects, false for basic materials
     */
    public void setRoughnessBumpEnabled(boolean enableRoughnessBump) {
        this.enableRoughnessBump = enableRoughnessBump;
    }

    // Anti-Aliasing
    
    /**
     * Checks if Fast Approximate Anti-Aliasing (FXAA) is enabled.
     * FXAA smooths jagged edges in the rendered image for better visual quality.
     * 
     * @return true if FXAA post-processing is active
     */
    public boolean isFXAAEnabled() {
        return enableFXAA;
    }

    /**
     * Enables or disables Fast Approximate Anti-Aliasing (FXAA).
     * FXAA is a post-processing technique that smooths jagged edges with minimal performance impact.
     * 
     * @param enableFXAA true to enable anti-aliasing, false to disable edge smoothing
     */
    public void setFXAAEnabled(boolean enableFXAA) {
        this.enableFXAA = enableFXAA;
    }

    /**
     * Provides a recommended shadow map resolution scale factor based on quality level.
     * The renderer can multiply the viewport size by this scale (and clamp) to pick a shadow map size.
     * @return scale multiplier for shadow map resolution
     */
    /**
     * Multisample count for the offscreen scene target at this quality level.
     *
     * <p>Measured on an M1 Pro at 3200x2000, this is the largest single cost in the frame
     * once the roughness bump is accounted for: 4x costs about 5.8 ms of a 12.7 ms frame,
     * 2x about 2.5 ms. Tying it to the quality level is what gives the level a real effect
     * on frame time — mesh tessellation, which is what it used to control, barely matters
     * for a renderer that is fill-rate bound.</p>
     *
     * @return the requested sample count, 0 to render without multisampling
     */
    public int getSceneSampleCount() {
        return switch (quality) {
            case LOW -> 0;
            case MEDIUM -> 2;
            case HIGH -> 4;
        };
    }

    /** Bump strength at the roughest finish; the smoothest is always 0. */
    private static final float MAX_ROUGHNESS_STRENGTH = 1.2f;
    /**
     * How strength grows across the roughness range at the levels below HIGH. The finishes
     * are not spread evenly over that range — everything from polished to regular paint sits
     * in the first eighth — so a linear ramp makes the ordinary ones grainier than they
     * should be. An exponent above 1 leaves the roughest alone and pulls the fine ones down.
     */
    private static final double SOFT_ROUGHNESS_EXPONENT = 1.25;
    /** Grain frequency in noise cells per world unit (1 unit = 50 mm), below HIGH. */
    private static final float SOFT_FINE_FREQUENCY = 14.0f;
    private static final float SOFT_COARSE_FREQUENCY = 8.0f;
    /** Grain frequency at HIGH, which keeps the original finer and more pronounced look. */
    private static final float DETAILED_MAX_FREQUENCY = 300.0f;

    /**
     * Bump strength to draw a surface of the given roughness with.
     *
     * @param roughnessAmount surface roughness normalised to 0 (mirror) .. 1 (roughest)
     * @return the bump strength, 0 for a surface with no visible grain
     */
    public float getRoughnessStrength(float roughnessAmount) {
        float amount = Math.max(0.0f, Math.min(1.0f, roughnessAmount));
        if (quality == RenderQuality.HIGH) {
            return MAX_ROUGHNESS_STRENGTH * amount;
        }
        return (float) (MAX_ROUGHNESS_STRENGTH * Math.pow(amount, SOFT_ROUGHNESS_EXPONENT));
    }

    /**
     * Grain frequency, in noise cells per world unit, to draw a surface of the given
     * roughness with.
     *
     * <p>The two levels differ in character, not just in cost. Below HIGH the grain is
     * coarse and grows with the roughness, which is how a rougher surface actually behaves.
     * HIGH keeps the original finer, denser grain, which reads as more detailed up close
     * even though it inverts that relationship.</p>
     *
     * @param roughnessAmount surface roughness normalised to 0 (mirror) .. 1 (roughest)
     * @return grain frequency; higher means smaller features
     */
    public float getRoughnessFrequency(float roughnessAmount) {
        float amount = Math.max(0.0f, Math.min(1.0f, roughnessAmount));
        if (quality == RenderQuality.HIGH) {
            return DETAILED_MAX_FREQUENCY * amount;
        }
        return SOFT_FINE_FREQUENCY + (SOFT_COARSE_FREQUENCY - SOFT_FINE_FREQUENCY) * amount;
    }

    /**
     * Whether the procedural roughness bump should be evaluated at this quality level.
     *
     * <p>It is the most expensive per-fragment work the shader does, so the lowest quality
     * level skips it regardless of the user's preference — that is what "low" is for.</p>
     *
     * @return {@code true} when the bump is both wanted and affordable
     */
    public boolean isRoughnessBumpRendered() {
        return enableRoughnessBump && quality != RenderQuality.LOW;
    }

    public float getShadowResolutionScale() {
        return switch (quality) {
            case LOW -> 0.6f;
            case MEDIUM -> 1.0f;
            case HIGH -> 1.4f;
        };
    }

    /**
     * Checks if shadow rendering is enabled.
     * @return true when shadow mapping should be performed
     */
    public boolean isShadowsEnabled() {
        return shadowsEnabled;
    }

    /**
     * Enables or disables shadow rendering.
     * @param enabled whether shadows should be rendered
     */
    public void setShadowsEnabled(boolean enabled) {
        this.shadowsEnabled = enabled;
    }

    /**
     * Checks if screen-space ambient occlusion is enabled.
     *
     * @return true when the AO post-process should run
     */
    public boolean isAmbientOcclusionEnabled() {
        return ambientOcclusionEnabled;
    }

    /**
     * Enables or disables screen-space ambient occlusion.
     *
     * @param enabled whether AO should be rendered
     */
    public void setAmbientOcclusionEnabled(boolean enabled) {
        this.ambientOcclusionEnabled = enabled;
    }

    /**
     * Checks whether costly effects should be suspended while the user moves the camera.
     *
     * @return true when interaction rendering should favor stability and responsiveness
     */
    public boolean shouldReduceEffectsDuringInteraction() {
        return reduceEffectsDuringInteraction;
    }

    /**
     * Selects whether shadows and costly post-processing are suspended during camera interaction.
     *
     * @param reduce true to reduce effects until the interaction ends
     */
    public void setReduceEffectsDuringInteraction(boolean reduce) {
        this.reduceEffectsDuringInteraction = reduce;
    }

    /**
     * Returns the SSAO sample count to use for the current quality level.
     *
     * @return number of SSAO kernel samples
     */
    public int getAmbientOcclusionSampleCount() {
        return switch (quality) {
            case LOW -> 12;
            case MEDIUM -> 24;
            case HIGH -> 32;
        };
    }

    /**
     * Returns the screen-space AO sampling radius in view-space units.
     *
     * @return AO radius
     */
    public float getAmbientOcclusionRadius() {
        return switch (quality) {
            case LOW -> 0.30f;
            case MEDIUM -> 0.40f;
            case HIGH -> 0.50f;
        };
    }

    /**
     * Returns the AO intensity multiplier for the current quality level.
     *
     * @return AO strength multiplier
     */
    public float getAmbientOcclusionStrength() {
        return switch (quality) {
            case LOW -> 0.70f;
            case MEDIUM -> 0.85f;
            case HIGH -> 1.0f;
        };
    }

    /**
     * Returns a small depth bias used to reduce self-occlusion artifacts.
     *
     * @return AO bias
     */
    public float getAmbientOcclusionBias() {
        return 0.015f;
    }

    /**
     * Restores the graphics quality settings to their built-in defaults.
     */
    public void resetToDefaults() {
        quality = DEFAULT_QUALITY;
        xrayOpacity = DEFAULT_XRAY_OPACITY;
        useBackfaceCulling = DEFAULT_BACKFACE_CULLING;
        enableRoughnessBump = DEFAULT_ROUGHNESS_BUMP;
        enableFXAA = DEFAULT_FXAA;
        shadowsEnabled = DEFAULT_SHADOWS;
        ambientOcclusionEnabled = DEFAULT_AMBIENT_OCCLUSION;
        reduceEffectsDuringInteraction = DEFAULT_REDUCE_EFFECTS_DURING_INTERACTION;
    }
}
