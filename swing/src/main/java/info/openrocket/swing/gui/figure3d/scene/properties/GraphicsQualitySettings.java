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
     * Returns the SSAO sample count to use for the current quality level.
     *
     * @return number of SSAO kernel samples
     */
    public int getAmbientOcclusionSampleCount() {
        return switch (quality) {
            case LOW -> 8;
            case MEDIUM -> 12;
            case HIGH -> 16;
        };
    }

    /**
     * Returns the screen-space AO sampling radius in view-space units.
     *
     * @return AO radius
     */
    public float getAmbientOcclusionRadius() {
        return switch (quality) {
            case LOW -> 0.20f;
            case MEDIUM -> 0.24f;
            case HIGH -> 0.28f;
        };
    }

    /**
     * Returns the AO intensity multiplier for the current quality level.
     *
     * @return AO strength multiplier
     */
    public float getAmbientOcclusionStrength() {
        return switch (quality) {
            case LOW -> 0.30f;
            case MEDIUM -> 0.38f;
            case HIGH -> 0.46f;
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
    }
}
