package info.openrocket.swing.gui.figure3d.rendering;

/**
 * Abstraction for texture state changes and parameter updates.
 */
public interface TextureBinder {
	void bindTexture(int unit, int textureType, int textureId);

	void setTextureParams(int textureType, int textureId, int wrapS, int wrapT, int minFilter, int magFilter);
	void reset();
}
