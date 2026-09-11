package info.openrocket.swing.gui.figure3d.rendering;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

/**
 * Caches texture-unit bindings and parameters to avoid redundant OpenGL state changes.
 */
public class TextureStateManager implements TextureBinder {
	private static final int MAX_TEXTURE_UNITS = 32;
	private static final Set<TextureStateManager> MANAGERS = Collections.newSetFromMap(new WeakHashMap<>());

	private final Map<Integer, int[]> boundTexturesByType = new HashMap<>();
	private int activeTextureUnit = -1;

	/** Cached parameters for one texture target and ID. */
	private static class TextureParams {
		int wrapS = -1;
		int wrapT = -1;
		int minFilter = -1;
		int magFilter = -1;
	}

	// Map texture target + ID to its cached parameters.
	private final Map<Long, TextureParams> textureParamsCache = new HashMap<>();

	public TextureStateManager() {
		synchronized (MANAGERS) {
			MANAGERS.add(this);
		}
	}

	public static void evictDeletedTexture(int textureId) {
		if (textureId == 0) {
			return;
		}
		synchronized (MANAGERS) {
			for (TextureStateManager manager : MANAGERS) {
				manager.evictTexture(textureId);
			}
		}
	}

	public void evictTexture(int textureId) {
		for (int[] boundTextures : boundTexturesByType.values()) {
			for (int i = 0; i < boundTextures.length; i++) {
				if (boundTextures[i] == textureId) {
					boundTextures[i] = -1;
				}
			}
		}
		textureParamsCache.keySet().removeIf(key -> textureIdFromKey(key) == textureId);
	}

	private int[] bindingsFor(int textureType) {
		int[] boundTextures = boundTexturesByType.get(textureType);
		if (boundTextures == null) {
			boundTextures = new int[MAX_TEXTURE_UNITS];
			Arrays.fill(boundTextures, -1);
			boundTexturesByType.put(textureType, boundTextures);
		}
		return boundTextures;
	}

	private static long cacheKey(int textureType, int textureId) {
		return ((long) textureType << 32) ^ Integer.toUnsignedLong(textureId);
	}

	private static int textureIdFromKey(long key) {
		return (int) key;
	}

	/**
	 * Binds a texture unless the requested binding is already cached.
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

		int[] boundTextures = bindingsFor(textureType);

		// Only bind if not already bound
		if (boundTextures[unit] != textureId) {
			glBindTexture(textureType, textureId);
			boundTextures[unit] = textureId;
		}
	}

	/**
	 * Sets only parameters that differ from their cached values.
	 *
	 * @param textureType The OpenGL texture target to update
	 * @param textureId The texture ID to set parameters for
	 * @param wrapS Texture wrap mode for S coordinate
	 * @param wrapT Texture wrap mode for T coordinate
	 * @param minFilter Minification filter mode
	 * @param magFilter Magnification filter mode
	 */
	@Override
	public void setTextureParams(int textureType, int textureId, int wrapS, int wrapT, int minFilter, int magFilter) {
		TextureParams params = textureParamsCache.computeIfAbsent(cacheKey(textureType, textureId), k -> new TextureParams());

		if (params.wrapS != wrapS) {
			glTexParameteri(textureType, GL_TEXTURE_WRAP_S, wrapS);
			params.wrapS = wrapS;
		}

		if (params.wrapT != wrapT) {
			glTexParameteri(textureType, GL_TEXTURE_WRAP_T, wrapT);
			params.wrapT = wrapT;
		}

		if (params.minFilter != minFilter) {
			glTexParameteri(textureType, GL_TEXTURE_MIN_FILTER, minFilter);
			params.minFilter = minFilter;
		}

		if (params.magFilter != magFilter) {
			glTexParameteri(textureType, GL_TEXTURE_MAG_FILTER, magFilter);
			params.magFilter = magFilter;
		}
	}

	/** Invalidates all cached state after external GL changes or context recreation. */
	@Override
	public void reset() {
		boundTexturesByType.clear();
		textureParamsCache.clear();
		// Force the next bind to reselect the real GL active texture unit. Direct-render
		// paths (particles, post-processing, HUD) may have changed it behind this cache.
		activeTextureUnit = -1;
	}
}
