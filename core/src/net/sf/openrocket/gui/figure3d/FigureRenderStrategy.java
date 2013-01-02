package net.sf.openrocket.gui.figure3d;

import java.util.HashMap;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.fixedfunc.GLLightingFunc;

import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Color;

public class FigureRenderStrategy extends RenderStrategy {
	private final float[] color = new float[4];

	public FigureRenderStrategy() {
		super(null);
	}

	private static final HashMap<Class<?>, Color> defaultColorCache = new HashMap<Class<?>, Color>();

	public void preGeometry(GL2 gl, RocketComponent c, float alpha) {
		gl.glLightModeli(GL2ES1.GL_LIGHT_MODEL_TWO_SIDE, 1);
		Color figureColor = c.getColor();
		if (figureColor == null) {
			if (defaultColorCache.containsKey(c.getClass())) {
				figureColor = defaultColorCache.get(c.getClass());
			} else {
				figureColor = Application.getPreferences().getDefaultColor(c.getClass());
				defaultColorCache.put(c.getClass(), figureColor);
			}
		}

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
		
		color[0] = color[1] = color[2] = 0;
		gl.glMaterialfv(GL.GL_BACK, GLLightingFunc.GL_SPECULAR, color, 0);

		//Back A&D
		convertColor(figureColor, color);
		color[0] = color[0] * 0.4f;
		color[1] = color[1] * 0.4f;
		color[2] = color[2] * 0.4f;
		color[3] = alpha;
		gl.glMaterialfv(GL.GL_BACK, GLLightingFunc.GL_DIFFUSE, color, 0);
		gl.glMaterialfv(GL.GL_BACK, GLLightingFunc.GL_AMBIENT, color, 0);

	}
	
	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		
		gl.glLightModelfv(GL2ES1.GL_LIGHT_MODEL_AMBIENT, 
                new float[] { 0,0,0 }, 0);

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

	protected static void convertColor(Color color, float[] out) {
		if ( color == null ){
			out[0] = 1;
			out[1] = 1;
			out[2] = 0;
		} else {
			out[0] = Math.max(0.2f, (float) color.getRed() / 255f) * 2;
			out[1] = Math.max(0.2f, (float) color.getGreen() / 255f) * 2;
			out[2] = Math.max(0.2f, (float) color.getBlue() / 255f) * 2;
		}
	}
}
