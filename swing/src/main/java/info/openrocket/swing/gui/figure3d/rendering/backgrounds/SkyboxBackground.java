package info.openrocket.swing.gui.figure3d.rendering.backgrounds;

import info.openrocket.swing.gui.figure3d.materials.Texture;

/**
 * Cubemap-based skybox background renderer for 360-degree environments.
 * 
 * Implements traditional skybox rendering using cubemap textures to create
 * seamless 360-degree backgrounds. Skyboxes are rendered at infinite distance,
 * providing consistent environmental visuals regardless of camera position
 * while maintaining excellent performance.
 * 
 * Technical implementation:
 * - Six-faced cubemap texture for complete 360-degree coverage
 * - Rendered at infinite distance using depth buffer tricks
 * - Direction-based texture sampling using view rays
 * - Optimized rendering with early depth rejection
 * - Seamless transitions between cube faces
 * - Support for both LDR and HDR cubemap formats
 * 
 * Cubemap organization:
 * - +X (Right), -X (Left)
 * - +Y (Top), -Y (Bottom) 
 * - +Z (Front), -Z (Back)
 * 
 * Skyboxes are ideal for:
 * - Outdoor environments and landscapes
 * - Space scenes and astronomical backgrounds
 * - Architectural visualization
 * - Any scenario requiring consistent distant environment
 * 
 * The cubemap approach provides better texture resolution distribution
 * compared to sphere mapping and avoids the polar distortion issues
 * common in equirectangular projections.
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