package info.openrocket.swing.gui.figure3d.scene.properties;

/**
 * Configuration for display modes and rendering styles within the OpenRocket 3D visualization system.
 * This class manages settings that control how rocket components and scene objects are visually
 * presented, supporting various rendering modes for different analysis and visualization needs.
 * 
 * <p>The display settings provide essential control over:</p>
 * <ul>
 *   <li><b>Material rendering:</b> Full textured appearance vs. simplified materials</li>
 *   <li><b>Wireframe modes:</b> Edge visualization for structural analysis</li>
 *   <li><b>Transparency effects:</b> X-ray rendering for internal component visibility</li>
 *   <li><b>Culling behavior:</b> Control over backface rendering optimization</li>
 * </ul>
 * 
 * <p>These settings integrate with the rendering pipeline to modify shader behavior,
 * blending modes, and geometry processing to achieve the desired visual effects
 * while maintaining optimal performance for each rendering style.</p>
 */
public class DisplaySettings {

    /**
     * Defines the available rendering modes for visualizing rocket geometry.
     * Each mode provides different visual characteristics suitable for specific
     * analysis or presentation purposes.
     */
    public enum RenderMode {
        /** Default rendering with full materials, textures, and surface details */
        FINISHED,
        /** Simplified rendering without textures or decals, using basic materials only */
        UNFINISHED,
        /** Edge-only rendering without backface culling for complete wireframe visibility */
        WIREFRAME,
        /** Edge-only rendering with backface culling for optimized wireframe display */
        WIREFRAME_CULLING,
        /** Transparent rendering mode for examining internal component relationships */
        XRAY,
        /** Cutaway rendering showing internal components by hiding front-facing surfaces */
        XRAY_CUTAWAY
    }

    private RenderMode mode = RenderMode.FINISHED;
    // Whether surfaces tagged as "inside" should be rendered (used for hollow geometry).
    private boolean renderInternalSurfaces = true;

    /**
     * Gets the current rendering mode.
     * 
     * @return the active RenderMode determining how geometry is displayed
     */
    public RenderMode getMode() {
        return mode;
    }

    /**
     * Sets the rendering mode for the 3D visualization.
     * This change affects how all geometry in the scene is rendered.
     * 
     * @param mode the RenderMode to activate
     */
    public void setMode(RenderMode mode) {
        this.mode = mode;
    }

    /**
     * Checks whether inside surfaces of hollow components should be rendered.
     *
     * @return true if internal surfaces should be drawn
     */
    public boolean isRenderInternalSurfaces() {
        return renderInternalSurfaces;
    }

    /**
     * Enables or disables rendering of inside surfaces on hollow geometry.
     *
     * @param renderInternalSurfaces true to render internal surfaces, false to suppress them
     */
    public void setRenderInternalSurfaces(boolean renderInternalSurfaces) {
        this.renderInternalSurfaces = renderInternalSurfaces;
    }

    /**
     * Checks if the current render mode requires transparency effects.
     * @return true if transparency is needed for the current mode
     */
    public boolean requiresTransparency() {
        return mode == RenderMode.XRAY;
    }

    /**
     * Checks if the current render mode uses wireframe rendering.
     * @return true if wireframe rendering is active
     */
    public boolean isWireframeMode() {
        return mode == RenderMode.WIREFRAME || mode == RenderMode.WIREFRAME_CULLING;
    }

    /**
     * Checks if backface culling should be disabled for the current mode.
     * @return true if culling should be disabled
     */
    public boolean shouldDisableCulling() {
        return mode == RenderMode.WIREFRAME;
    }
}
