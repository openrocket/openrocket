package info.openrocket.swing.gui.figure3d.rendering;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

/**
 * Optimized OpenGL texture state manager for performance-critical rendering.
 * 
 * Reduces redundant OpenGL state changes by tracking current texture bindings
 * and parameters across multiple texture units. This is crucial for performance
 * as texture binding and parameter changes are expensive GPU operations.
 * 
 * Features:
 * - Tracks active texture unit to minimize glActiveTexture calls
 * - Caches texture bindings per unit to avoid redundant glBindTexture calls
 * - Caches texture parameters to avoid redundant glTexParameteri calls
 * - Supports up to 32 texture units for complex multi-texture rendering
 * 
 * This manager should be used throughout the rendering pipeline to ensure
 * optimal texture state management and maximum rendering performance.
 */
public class TextureStateManager implements TextureBinder {
	private static final int MAX_TEXTURE_UNITS = 32;
	private final int[] boundTextures = new int[MAX_TEXTURE_UNITS];
	private int activeTextureUnit = -1;

	/**
	 * Cached texture parameters for a single texture.
	 * 
	 * Stores the current parameter values to avoid redundant
	 * glTexParameteri calls when the same parameters are set repeatedly.
	 */
	private static class TextureParams {
		int wrapS = -1;
		int wrapT = -1;
		int minFilter = -1;
		int magFilter = -1;
	}

	// Map texture ID to its cached parameters
	private final Map<Integer, TextureParams> textureParamsCache = new HashMap<>();

	/**
	 * Creates a new texture state manager with all texture units unbound.
	 */
	public TextureStateManager() {
		// Initialize all texture units as unbound
		Arrays.fill(boundTextures, -1);
	}

	/**
	 * Binds a texture to the specified unit only if it's not already bound.
	 * 
	 * This method minimizes OpenGL state changes by checking if the texture
	 * is already bound and if the correct texture unit is already active.
	 * 
	 * @param unit The texture unit index (0-31)
	 * @param textureType The OpenGL texture type (e.g., GL_TEXTURE_2D)
	 * @param textureId The OpenGL texture ID to bind, or 0 to unbind
	 */
    @Override
    public void bindTexture(int unit, int textureType, int textureId) {
		if (unit < 0 || unit >= MAX_TEXTURE_UNITS) return;

		// Only switch texture unit if necessary
		if (activeTextureUnit != unit) {
			glActiveTexture(GL_TEXTURE0 + unit);
			activeTextureUnit = unit;
		}

		// Only bind if not already bound
		if (boundTextures[unit] != textureId) {
			glBindTexture(textureType, textureId);
			boundTextures[unit] = textureId;
		}
	}

	/**
	 * Sets texture parameters only if they differ from cached values.
	 * 
	 * Reduces redundant glTexParameteri calls by tracking the current
	 * parameter values for each texture.
	 * 
	 * @param textureId The texture ID to set parameters for
	 * @param wrapS Texture wrap mode for S coordinate
	 * @param wrapT Texture wrap mode for T coordinate
	 * @param minFilter Minification filter mode
	 * @param magFilter Magnification filter mode
	 */
    @Override
    public void setTextureParams(int textureId, int wrapS, int wrapT, int minFilter, int magFilter) {
		TextureParams params = textureParamsCache.computeIfAbsent(textureId, k -> new TextureParams());

		if (params.wrapS != wrapS) {
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapS);
			params.wrapS = wrapS;
		}

		if (params.wrapT != wrapT) {
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapT);
			params.wrapT = wrapT;
		}

		if (params.minFilter != minFilter) {
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter);
			params.minFilter = minFilter;
		}

		if (params.magFilter != magFilter) {
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter);
			params.magFilter = magFilter;
		}
	}

	/**
	 * Unbinds any texture from the specified texture unit.
	 * 
	 * @param unit The texture unit to unbind
	 */
    @Override
    public void unbindTexture(int unit) {
		bindTexture(unit, GL_TEXTURE_2D, 0);
	}

	/**
	 * Resets all cached state information.
	 * 
	 * Should be called when the OpenGL context is lost or recreated
	 * to ensure the cache remains consistent with actual GPU state.
	 */
    @Override
	public void reset() {
		Arrays.fill(boundTextures, -1);
		textureParamsCache.clear();
		// Force the next bind to reselect the real GL active texture unit. Direct-render
		// paths (particles, post-processing, HUD) may have changed it behind this cache.
		activeTextureUnit = -1;
	}
}
