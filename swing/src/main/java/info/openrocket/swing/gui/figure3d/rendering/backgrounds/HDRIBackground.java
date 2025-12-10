package info.openrocket.swing.gui.figure3d.rendering.backgrounds;

import info.openrocket.swing.gui.figure3d.materials.Texture;

/**
 * High Dynamic Range (HDR) equirectangular background renderer.
 * 
 * Provides realistic environment lighting and backgrounds using High Dynamic Range
 * images in equirectangular projection format. HDRI backgrounds offer superior
 * lighting quality compared to traditional LDR images, supporting a much wider
 * range of luminance values for realistic lighting and reflections.
 * 
 * Technical features:
 * - Equirectangular projection mapping for 360-degree coverage
 * - High dynamic range data preservation for realistic lighting
 * - Automatic tone mapping for display adaptation
 * - Seamless spherical mapping without visible seams
 * - Support for .HDR and .EXR format images
 * - Efficient GPU-based spherical coordinate conversion
 * 
 * HDRI backgrounds are particularly effective for:
 * - Realistic outdoor lighting scenarios
 * - Professional visualization and rendering
 * - Environment-based lighting contributions
 * - Accurate reflections on metallic surfaces
 * 
 * The equirectangular format maps the entire sphere onto a 2:1 aspect ratio
 * rectangle, with longitude mapped horizontally and latitude mapped vertically.
 */
public class HDRIBackground implements Background {

	private final Texture hdriTexture;
	private final static BackgroundType TYPE = BackgroundType.HDRI;

	/**
	 * Creates a new HDRI background from an equirectangular HDR texture.
	 * 
	 * The texture should contain HDR data in equirectangular projection format
	 * with a 2:1 aspect ratio. The texture data will be used for both background
	 * rendering and environment lighting calculations.
	 * 
	 * @param hdriTexture The HDR texture in equirectangular format containing environment data
	 */
	public HDRIBackground(Texture hdriTexture) {
		this.hdriTexture = hdriTexture;
	}

	/**
	 * Gets the HDR texture used for environment mapping.
	 * 
	 * @return The equirectangular HDR texture containing environment data
	 */
	public Texture getHdriTexture() {
		return hdriTexture;
	}

	@Override
	public BackgroundType getType() {
		return TYPE;
	}

	@Override
	public void cleanup() {
		if (hdriTexture != null) {
			hdriTexture.cleanup();
		}
	}
}