package info.openrocket.swing.gui.figure3d.rendering.backgrounds;

import info.openrocket.swing.gui.figure3d.materials.Texture;

/**
 * A background sampled from a high dynamic range equirectangular image, tone
 * mapped for display.
 *
 * <p>The HDR image is a 2:1 rectangle covering the whole sphere, longitude across
 * and latitude down.</p>
 */
public class HDRIBackground implements Background {

	private final Texture hdriTexture;

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
	public void cleanup() {
		if (hdriTexture != null) {
			hdriTexture.cleanup();
		}
	}
}
