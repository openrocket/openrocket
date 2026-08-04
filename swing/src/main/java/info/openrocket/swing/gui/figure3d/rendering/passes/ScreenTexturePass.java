package info.openrocket.swing.gui.figure3d.rendering.passes;

/**
 * Interface for render passes that process screen-space textures.
 * 
 * This interface is implemented by post-processing passes that take a texture
 * as input, process it (e.g., apply effects, filters, or transformations),
 * and produce an output texture. This allows for chaining multiple post-processing
 * effects in a pipeline where the output of one pass becomes the input of the next.
 * 
 * Common use cases include:
 * - Anti-aliasing passes (FXAA, MSAA resolve)
 * - Motion blur and depth of field effects
 * - Color grading and tone mapping
 * - Screen-space ambient occlusion (SSAO)
 * - Outline and edge detection effects
 * 
 * The texture processing typically happens in screen-space using full-screen
 * quads and specialized fragment shaders.
 */
public interface ScreenTexturePass extends RenderPass {
	/**
	 * Sets the input texture that this pass will process.
	 * 
	 * This texture typically contains the rendered scene from a previous
	 * pass or the output from another post-processing effect.
	 * 
	 * @param textureId The OpenGL texture ID of the input texture
	 */
	void setInputTexture(int textureId);
	
	/**
	 * Gets the output texture containing this pass's processed result.
	 * 
	 * This texture can be used as input for subsequent passes or as the
	 * final result to display to the screen.
	 * 
	 * @return The OpenGL texture ID of the output texture
	 */
	int getOutputTexture();
}
