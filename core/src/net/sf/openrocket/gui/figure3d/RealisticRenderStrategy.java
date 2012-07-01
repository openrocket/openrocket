package net.sf.openrocket.gui.figure3d;

import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.GLProfile;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLMatrixFunc;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

import net.sf.openrocket.appearance.Appearance;
import net.sf.openrocket.appearance.Decal;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;

public class RealisticRenderStrategy extends RenderStrategy {

	private final float[] color = new float[4];
	private static final LogHelper log = Application.getLogger();
	private Map<RocketComponent, Appearance> apMap = new HashMap<RocketComponent, Appearance>();
	private Map<Decal, Texture> texCache = new HashMap<Decal, Texture>();

	@Override
	public void clearCaches() {
		apMap.clear();
		texCache.clear();
	}

	@Override
	public boolean isDrawn(RocketComponent c) {
		return true;
	}

	@Override
	public boolean isDrawnTransparent(RocketComponent c) {
		return false;
	}

	@Override
	public void preGeometry(GL2 gl, RocketComponent c, float alpha) {
		Appearance a = getAppearance(c);
		gl.glLightModeli(GL2ES1.GL_LIGHT_MODEL_TWO_SIDE, 1);

		convertColor(a.getDiffuse(), color);
		color[3] = alpha;
		gl.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_DIFFUSE, color, 0);

		convertColor(a.getAmbient(), color);
		color[3] = alpha;
		gl.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_AMBIENT, color, 0);

		convertColor(a.getSpecular(), color);
		color[3] = alpha;
		gl.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_SPECULAR, color, 0);
		gl.glMateriali(GL.GL_FRONT, GLLightingFunc.GL_SHININESS, a.getShininess());

		convertColor(a.getDiffuse(), color);
		color[3] = alpha;
		gl.glMaterialfv(GL.GL_BACK, GLLightingFunc.GL_DIFFUSE, color, 0);
		gl.glMaterialfv(GL.GL_BACK, GLLightingFunc.GL_AMBIENT, color, 0);

		Decal t = a.getTexture();
		Texture tex = null;
		if (t != null) {
			tex = getTexture(t);
		}
		if (t != null && tex != null) {
			tex.enable(gl);
			tex.bind(gl);
			gl.glMatrixMode(GL.GL_TEXTURE);
			gl.glPushMatrix();

			gl.glTranslated(-t.getCenter().x, -t.getCenter().y, 0);
			gl.glRotated(57.2957795 * t.getRotation(), 0, 0, 1);
			gl.glTranslated(t.getCenter().x, t.getCenter().y, 0);

			gl.glScaled(t.getScale().x, t.getScale().y, 0);
			gl.glTranslated(t.getOffset().x, t.getOffset().y, 0);

			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, toEdgeMode(t.getEdgeMode()));

			gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		}
	}

	@Override
	public void postGeometry(GL2 gl, RocketComponent c, float alpha) {
		Appearance a = getAppearance(c);
		Decal t = a.getTexture();
		Texture tex = null;
		if (t != null) {
			tex = getTexture(t);
		}
		if (tex != null) {
			gl.glMatrixMode(GL.GL_TEXTURE);
			gl.glPopMatrix();
			gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
			tex.disable(gl);
		}
	}

	private Texture getTexture(Decal t) {
		if (texCache.containsKey(t))
			return texCache.get(t);
		Texture tex = null;
		try {
			log.debug("Loading texture " + t);
			TextureData data = TextureIO.newTextureData(GLProfile.getDefault(), t.getImageURL().openStream(), true,
					null);
			tex = TextureIO.newTexture(data);
		} catch (Throwable e) {
			log.error("Error loading Texture", e);
		}
		texCache.put(t, tex);
		return tex;
	}

	private Appearance getAppearance(RocketComponent c) {
		if (apMap.containsKey(c))
			return apMap.get(c);

		Appearance ret = c.getAppearance();

		if (ret == null) {
			ret = Appearance.MISSING;
		}

		apMap.put(c, ret);
		System.out.println(c + ": " + ret);
		return ret;
	}

	private int toEdgeMode(Decal.EdgeMode m) {
		switch (m) {
		case REPEAT:
			return GL.GL_REPEAT;
		case MIRROR:
			return GL.GL_MIRRORED_REPEAT;
		case CLAMP:
			return GL.GL_CLAMP_TO_EDGE;
		default:
			return GL.GL_CLAMP_TO_EDGE;
		}
	}

}
