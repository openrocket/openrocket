package info.openrocket.swing.gui.figure3d.rendering.backgrounds;

import info.openrocket.swing.gui.figure3d.materials.Texture;

/**
 * Flat 2D background image rendered as a screen-aligned quad.
 */
public class ImageBackground implements Background {

	private static final BackgroundType TYPE = BackgroundType.IMAGE;
	private final Texture texture;

	public ImageBackground(Texture texture) {
		this.texture = texture;
	}

	public Texture getTexture() {
		return texture;
	}

	@Override
	public BackgroundType getType() {
		return TYPE;
	}

	@Override
	public void cleanup() {
		if (texture != null) {
			texture.cleanup();
		}
	}
}
