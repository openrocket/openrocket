package info.openrocket.swing.gui.figure3d.rendering.passes;

/** A post-processing pass whose output can feed the next pass in the chain. */
public interface ScreenTexturePass extends RenderPass {
	/**
	 * @param textureId OpenGL texture produced by the scene or preceding pass
	 */
	void setInputTexture(int textureId);

	/** @return OpenGL texture containing this pass's result */
	int getOutputTexture();
}
