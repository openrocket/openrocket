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
		/** Transparent rendering mode for examining internal component relationships */
		XRAY
	}

	/** How surfaces are shaded. Only the file thumbnail uses the styles other than {@link #LIT}. */
	public enum SurfaceShading {
		/** Lit by the scene's lights */
		LIT,
		/** Without lighting: paint and decals, only lightly darkened where the surface turns away from the viewer */
		FLAT,
		/** Each object opaque and in a flat colour of its own, to find the outlines of the parts */
		OBJECT_IDS
	}

	private RenderMode mode = DEFAULT_RENDER_MODE;
	private SurfaceShading surfaceShading = SurfaceShading.LIT;
	// Whether surfaces tagged as "inside" should be rendered (used for hollow geometry).
	private boolean renderInternalSurfaces = DEFAULT_RENDER_INTERNAL_SURFACES;

	public RenderMode getMode() {
		return mode;
	}

	public void setMode(RenderMode mode) {
		this.mode = mode;
	}

	public SurfaceShading getSurfaceShading() {
		return surfaceShading;
	}

	public void setSurfaceShading(SurfaceShading surfaceShading) {
		this.surfaceShading = surfaceShading;
	}

	public boolean isRenderInternalSurfaces() {
		return renderInternalSurfaces;
	}

	public void setRenderInternalSurfaces(boolean renderInternalSurfaces) {
		this.renderInternalSurfaces = renderInternalSurfaces;
	}

	/** Restores the built-in defaults. */
	public void resetToDefaults() {
		mode = DEFAULT_RENDER_MODE;
		surfaceShading = SurfaceShading.LIT;
		renderInternalSurfaces = DEFAULT_RENDER_INTERNAL_SURFACES;
	}
}
