package net.sf.openrocket.gui.figure3d;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLMatrixFunc;

import net.sf.openrocket.appearance.Appearance;
import net.sf.openrocket.appearance.Decal;
import net.sf.openrocket.appearance.defaults.DefaultAppearance;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.figure3d.geometry.Geometry;
import net.sf.openrocket.gui.figure3d.geometry.Geometry.Surface;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Color;

import com.jogamp.opengl.util.texture.Texture;

public class RealisticRenderer extends RocketRenderer {
	private final float[] colorClear = { 0, 0, 0, 0 };
	private final float[] colorWhite = { 1, 1, 1, 1 };
	private final float[] color = new float[4];
	
	private final TextureCache textures;
	private float anisotrophy = 0;
	
	public RealisticRenderer(OpenRocketDocument document) {
		textures = new TextureCache();
	}
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
		
		textures.init(drawable);
		
		GL2 gl = drawable.getGL().getGL2();
		
		gl.glLightModelfv(GL2ES1.GL_LIGHT_MODEL_AMBIENT, new float[] { 0, 0, 0 }, 0);
		
		float amb = 0.3f;
		float dif = 1.0f - amb;
		float spc = 1.0f;
		gl.glLightfv(GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_AMBIENT, new float[] { amb, amb, amb, 1 }, 0);
		gl.glLightfv(GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_DIFFUSE, new float[] { dif, dif, dif, 1 }, 0);
		gl.glLightfv(GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_SPECULAR, new float[] { spc, spc, spc, 1 }, 0);
		
		gl.glEnable(GLLightingFunc.GL_LIGHT1);
		gl.glEnable(GLLightingFunc.GL_LIGHTING);
		gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
		
		gl.glEnable(GLLightingFunc.GL_NORMALIZE);
		
		if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic")) {
			float a[] = new float[1];
			gl.glGetFloatv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, a, 0);
			anisotrophy = a[0];
		}
	}
	
	@Override
	public void updateFigure(GLAutoDrawable drawable) {
		super.updateFigure(drawable);
		textures.advanceCacheGeneration(drawable);
	}
	
	@Override
	public void dispose(GLAutoDrawable drawable) {
		flushTextureCache(drawable);
		super.dispose(drawable);
		textures.dispose(drawable);
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
	protected void renderMotor(final GL2 gl, final Motor motor) {
		render(gl, cr.getGeometry(motor, Surface.OUTSIDE), DefaultAppearance.getDefaultAppearance(motor), true, 1);
	}
	
	@Override
	public void renderComponent(final GL2 gl, final RocketComponent c, final float alpha) {
		render(gl, cr.getGeometry(c, Surface.INSIDE), DefaultAppearance.getDefaultAppearance(c), true, 1.0f);
		render(gl, cr.getGeometry(c, Surface.OUTSIDE), getAppearance(c), true, alpha);
		render(gl, cr.getGeometry(c, Surface.EDGES), getAppearance(c), false, alpha);
	}
	
	private void render(GL2 gl, Geometry g, Appearance a, boolean decals, float alpha) {
		final Decal t = a.getTexture();
		final Texture tex = textures.getTexture(t);
		
		gl.glLightModeli(GL2.GL_LIGHT_MODEL_COLOR_CONTROL, GL2.GL_SEPARATE_SPECULAR_COLOR);
		
		
		convertColor(a.getPaint(), color);
		color[3] = alpha;
		gl.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_DIFFUSE, color, 0);
		gl.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_AMBIENT, color, 0);
		
		color[0] = color[1] = color[2] = (float) a.getShine();
		color[3] = alpha;
		gl.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_SPECULAR, color, 0);
		gl.glMateriali(GL.GL_FRONT, GLLightingFunc.GL_SHININESS, (int) (100 * a.getShine()));
		
		
		g.render(gl);
		
		if (decals && t != null && tex != null) {
			
			tex.enable(gl);
			tex.bind(gl);
			
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
			
			gl.glMatrixMode(GL.GL_TEXTURE);
			gl.glPushMatrix();
			
			gl.glTranslated(-t.getCenter().x, -t.getCenter().y, 0);
			gl.glRotated(57.2957795 * t.getRotation(), 0, 0, 1);
			gl.glTranslated(t.getCenter().x, t.getCenter().y, 0);
			
			gl.glScaled(t.getScale().x, t.getScale().y, 0);
			gl.glTranslated(t.getOffset().x, t.getOffset().y, 0);
			
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, toEdgeMode(t.getEdgeMode()));
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, toEdgeMode(t.getEdgeMode()));
			
			
			gl.glTexParameterfv(GL.GL_TEXTURE_2D, GL2.GL_TEXTURE_BORDER_COLOR, colorClear, 0);
			gl.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_DIFFUSE, colorWhite, 0);
			gl.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_AMBIENT, colorWhite, 0);
			
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			gl.glEnable(GL.GL_BLEND);
			gl.glDepthFunc(GL.GL_LEQUAL);
			
			gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
			
			if (anisotrophy > 0) {
				gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAX_ANISOTROPY_EXT, anisotrophy);
			}
			
			g.render(gl);
			
			if (t.getEdgeMode() == Decal.EdgeMode.STICKER) {
				gl.glDepthFunc(GL.GL_LESS);
			}
			
			gl.glMatrixMode(GL.GL_TEXTURE);
			gl.glPopMatrix();
			gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
			
			tex.disable(gl);
		}
		
	}
	
	@Override
	public void flushTextureCache(GLAutoDrawable drawable) {
		textures.flushTextureCache(drawable);
	}
	
	
	protected Appearance getAppearance(RocketComponent c) {
		Appearance ret = c.getAppearance();
		if (ret == null) {
			ret = DefaultAppearance.getDefaultAppearance(c);
		}
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
		case STICKER:
			return GL2.GL_CLAMP_TO_BORDER;
		default:
			return GL.GL_CLAMP_TO_EDGE;
		}
	}
	
	protected static void convertColor(Color color, float[] out) {
		if (color == null) {
			out[0] = 1;
			out[1] = 1;
			out[2] = 0;
		} else {
			out[0] = (float) color.getRed() / 255f;
			out[1] = (float) color.getGreen() / 255f;
			out[2] = (float) color.getBlue() / 255f;
		}
	}
}
