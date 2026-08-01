package info.openrocket.swing.gui.figure3d.rendering.backgrounds;

import info.openrocket.swing.gui.figure3d.materials.Texture;

/**
 * A background sampled from a six-faced cubemap, drawn at infinite distance.
 *
 * <p>Faces are ordered +X, -X, +Y, -Y, +Z, -Z, and sampled along the view ray.</p>
 */
public class SkyboxBackground implements Background {

	private final Texture cubemapTexture;
	private final static BackgroundType TYPE = BackgroundType.SKYBOX;

	/**
	 * Creates a new skybox background from a cubemap texture.
	 * 
	 * The cubemap texture must contain six faces properly oriented for
	 * seamless environment mapping. Each face should represent the view
	 * in the corresponding direction from the center of the cube.
	 * 
	 * @param cubemapTexture The cubemap texture containing six faces for complete 360-degree coverage
	 */
	public SkyboxBackground(Texture cubemapTexture) {
		this.cubemapTexture = cubemapTexture;
	}

	/**
	 * Gets the cubemap texture used for skybox rendering.
	 * 
	 * @return The six-faced cubemap texture containing the environment
	 */
	public Texture getCubemapTexture() {
		return cubemapTexture;
	}

	@Override
	public BackgroundType getType() {
		return TYPE;
	}

	@Override
	public void cleanup() {
		if (cubemapTexture != null) {
			cubemapTexture.cleanup();
		}
	}
}