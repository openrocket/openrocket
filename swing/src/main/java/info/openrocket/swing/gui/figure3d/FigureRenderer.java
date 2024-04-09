package info.openrocket.swing.gui.figure3d;

import java.util.HashMap;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES1;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;

import info.openrocket.swing.gui.figure3d.geometry.Geometry;
import info.openrocket.swing.gui.figure3d.geometry.Geometry.Surface;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.ExternalComponent;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.SymmetricComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.ORColor;

public class FigureRenderer extends RocketRenderer {
	private final float[] color = new float[4];
	
	public FigureRenderer() {
	}
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
		
		GL2 gl = drawable.getGL().getGL2();
		
		gl.glLightModelfv(GL2ES1.GL_LIGHT_MODEL_AMBIENT,
				new float[] { 0, 0, 0 }, 0);
		
		float amb = 0.3f;
		float dif = 1.0f - amb;
		float spc = 1.0f;
		gl.glLightfv(GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_AMBIENT,
				new float[] { amb, amb, amb, 1 }, 0);
		gl.glLightfv(GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_DIFFUSE,
				new float[] { dif, dif, dif, 1 }, 0);
		gl.glLightfv(GLLightingFunc.GL_LIGHT1, GLLightingFunc.GL_SPECULAR,
				new float[] { spc, spc, spc, 1 }, 0);
		
		gl.glEnable(GLLightingFunc.GL_LIGHT1);
		gl.glEnable(GLLightingFunc.GL_LIGHTING);
		gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
		
		gl.glEnable(GLLightingFunc.GL_NORMALIZE);
	}
	
	@Override
	public boolean isDrawnTransparent(RocketComponent c) {
		if (c instanceof BodyTube)
			return true;
		if (c instanceof NoseCone)
			return false;
		if (c instanceof SymmetricComponent) {
			if (((SymmetricComponent) c).isFilled())
				return false;
		}
		if (c instanceof Transition) {
			Transition t = (Transition) c;
			return !t.isAftShoulderCapped() && !t.isForeShoulderCapped();
		}
		return false;
	}
	
	private static final HashMap<Class<?>, ORColor> defaultColorCache = new HashMap<Class<?>, ORColor>();
	
	@Override
	public void renderComponent(GL2 gl, Geometry geom, float alpha) {
		RocketComponent c = geom.getComponent();
		gl.glLightModeli(GL2ES1.GL_LIGHT_MODEL_TWO_SIDE, 1);
		ORColor figureColor = c.getColor();
		if (figureColor == null) {
			if (defaultColorCache.containsKey(c.getClass())) {
				figureColor = defaultColorCache.get(c.getClass());
			} else {
				figureColor = ((SwingPreferences) Application.getPreferences()).getDefaultColor(c.getClass());
				defaultColorCache.put(c.getClass(), figureColor);
			}
		}
		
		//Inside
		convertColor(figureColor, color);
		color[0] = color[0] * 0.7f;
		color[1] = color[1] * 0.7f;
		color[2] = color[2] * 0.7f;
		color[3] = 1.0f;
		gl.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_DIFFUSE, color, 0);
		gl.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_AMBIENT, color, 0);
		
		geom.render(gl,Surface.INSIDE);
		
		//Outside
		// Set up the front A&D color
		convertColor(figureColor, color);
		color[3] = alpha;
		gl.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_DIFFUSE, color, 0);
		gl.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_AMBIENT, color, 0);
		
		// Set up the Specular color & Shine
		convertColor(figureColor, color);
		float d = 0.9f;
		float m = (float) getShine(c) / 128.0f;
		color[0] = Math.max(color[0], d) * m;
		color[1] = Math.max(color[1], d) * m;
		color[2] = Math.max(color[2], d) * m;
		
		gl.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_SPECULAR, color, 0);
		gl.glMateriali(GL.GL_FRONT, GLLightingFunc.GL_SHININESS, getShine(c));
		
		geom.render(gl, Surface.OUTSIDE);
		geom.render(gl, Surface.EDGES);
		
		color[0] = color[1] = color[2] = 0;
		gl.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_SPECULAR, color, 0);
		
		
	}
	
	@Override
	public void flushTextureCache(GLAutoDrawable drawable) {
	}
	
	private static int getShine(RocketComponent c) {
		if (c instanceof ExternalComponent) {
			switch (((ExternalComponent) c).getFinish()) {
			case ROUGH:
				return 10;
			case UNFINISHED:
				return 30;
			case NORMAL:
				return 40;
			case SMOOTH:
				return 80;
			case POLISHED:
				return 128;
			default:
				return 100;
			}
		}
		return 20;
	}
	
	protected static void convertColor(ORColor color, float[] out) {
		if (color == null) {
			out[0] = 1;
			out[1] = 1;
			out[2] = 0;
		} else {
			out[0] = Math.max(0.2f, (float) color.getRed() / 255f) * 2;
			out[1] = Math.max(0.2f, (float) color.getGreen() / 255f) * 2;
			out[2] = Math.max(0.2f, (float) color.getBlue() / 255f) * 2;
		}
	}
	
	
	@Override
	protected void renderMotor(GL2 gl, Motor motor) {
		final float outside[] = { 0.3f, 0.3f, 0.3f, 1.0f };
		gl.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_DIFFUSE, outside, 0);
		gl.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_AMBIENT, outside, 0);
		super.renderMotor(gl, motor);
	}
}
