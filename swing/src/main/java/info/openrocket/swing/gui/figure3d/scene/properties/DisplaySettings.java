package info.openrocket.swing.gui.figure3d.scene.properties;

/** Controls render mode and visibility of internal component surfaces. */
public class DisplaySettings {
	public static final RenderMode DEFAULT_RENDER_MODE = RenderMode.FINISHED;
	public static final boolean DEFAULT_RENDER_INTERNAL_SURFACES = true;

	/** Rendering modes available to the design and photo views. */
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

	private RenderMode mode = DEFAULT_RENDER_MODE;
	// Whether surfaces tagged as "inside" should be rendered (used for hollow geometry).
	private boolean renderInternalSurfaces = DEFAULT_RENDER_INTERNAL_SURFACES;

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
	 * Restores the display settings to their built-in defaults.
	 */
	public void resetToDefaults() {
		mode = DEFAULT_RENDER_MODE;
		renderInternalSurfaces = DEFAULT_RENDER_INTERNAL_SURFACES;
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
		return mode == RenderMode.WIREFRAME || mode == RenderMode.XRAY;
	}
}
