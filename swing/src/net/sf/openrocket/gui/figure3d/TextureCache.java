package net.sf.openrocket.gui.figure3d;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLProfile;

import net.sf.openrocket.appearance.Decal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

public class TextureCache {
	private static final Logger log = LoggerFactory.getLogger(TextureCache.class);
	
	private Map<String, Texture> oldTexCache = null;
	private Map<String, Texture> texCache = null;
	
	public void init(GLAutoDrawable drawable) {
		if (texCache != null)
			throw new IllegalStateException(this + " already initialized.");
		oldTexCache = new HashMap<String, Texture>();
		texCache = new HashMap<String, Texture>();
	}
	
	public void dispose(GLAutoDrawable drawable) {
		if (texCache == null)
			throw new IllegalStateException(this + " not initialized.");
		flushTextureCache(drawable);
		oldTexCache = null;
		texCache = null;
	}
	
	public void flushTextureCache(GLAutoDrawable drawable) {
		advanceCacheGeneration(drawable);
		advanceCacheGeneration(drawable);
	}
	
	public void advanceCacheGeneration(GLAutoDrawable drawable) {
		if (texCache == null)
			throw new IllegalStateException(this + " not initialized.");
		
		log.debug("ClearCaches");
		for (Map.Entry<String, Texture> e : oldTexCache.entrySet()) {
			log.debug("Destroying Texture for " + e.getKey());
			if (e.getValue() != null)
				e.getValue().destroy(drawable.getGL().getGL2());
		}
		oldTexCache = texCache;
		texCache = new HashMap<String, Texture>();
	}
	
	public Texture getTexture(URL uri) {
		if (texCache == null)
			throw new IllegalStateException(this + " not initialized.");
		
		if (uri == null)
			return null;
		
		String imageName = uri.toString();
		
		// Return the Cached value if available
		if (texCache.containsKey(imageName))
			return texCache.get(imageName);
		
		// If the texture is in the Old Cache, save it.
		if (oldTexCache.containsKey(imageName)) {
			texCache.put(imageName, oldTexCache.get(imageName));
			oldTexCache.remove(imageName);
			return texCache.get(imageName);
		}
		
		// Otherwise load it.
		Texture tex = null;
		try {
			log.debug("Loading texture " + uri);
			InputStream is = uri.openStream();
			TextureData data = TextureIO.newTextureData(GLProfile.getDefault(), is, GL.GL_RGBA, GL.GL_RGBA, true, null);
			tex = TextureIO.newTexture(data);
		} catch (Throwable e) {
			log.error("Error loading Texture", e);
		}
		texCache.put(imageName, tex);
		
		return tex;
	}
	
	public Texture getTexture(Decal decal) {
		if (texCache == null)
			throw new IllegalStateException(this + " not initialized.");
		
		if (decal == null)
			return null;
		
		String imageName = decal.getImage().getName();
		
		// Return the Cached value if available
		if (texCache.containsKey(imageName))
			return texCache.get(imageName);
		
		// If the texture is in the Old Cache, save it.
		if (oldTexCache.containsKey(imageName)) {
			texCache.put(imageName, oldTexCache.get(imageName));
			oldTexCache.remove(imageName);
			return texCache.get(imageName);
		}
		
		// Otherwise load it.
		Texture tex = null;
		try {
			log.debug("Loading texture " + decal);
			InputStream is = decal.getImage().getBytes();
			TextureData data = TextureIO.newTextureData(GLProfile.getDefault(), is, GL.GL_RGBA, GL.GL_RGBA, true, null);
			tex = TextureIO.newTexture(data);
		} catch (Throwable e) {
			log.error("Error loading Texture", e);
		}
		texCache.put(imageName, tex);
		
		return tex;
		
	}
}
