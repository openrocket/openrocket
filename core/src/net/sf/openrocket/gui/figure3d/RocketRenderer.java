package net.sf.openrocket.gui.figure3d;

import java.awt.Point;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.fixedfunc.GLLightingFunc;

import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.SymmetricComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.Coordinate;

/*
 * @author Bill Kuker <bkuker@billkuker.com>
 */
public class RocketRenderer {
	ComponentRenderer cr;

	private final float[] selectedEmissive = { 1, 0, 0, 1 };
	private final float[] colorBlack = { 0, 0, 0, 1 };
	private final float[] color = new float[4];

	public void init(GLAutoDrawable drawable) {
		cr = new ComponentRenderer();
		cr.init(drawable);
	}
	
	public void updateFigure() {
		cr.updateFigure();
	}

	private boolean isDrawn(RocketComponent c) {
		return true;
	}

	private boolean isDrawnTransparent(RocketComponent c) {
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

	public RocketComponent pick(GLAutoDrawable drawable,
			Configuration configuration, Point p, Set<RocketComponent> ignore) {
		final GL2 gl = drawable.getGL().getGL2();
		gl.glEnable(GL.GL_DEPTH_TEST);
		
		//Store a vector of pickable parts.
		final Vector<RocketComponent> pickParts = new Vector<RocketComponent>();
		
		for (RocketComponent c : configuration) {
			if ( ignore != null && ignore.contains(c) )
				continue;

			//Encode the index of the part as a color
			//if index is 0x0ABC the color ends up as
			//0xA0B0C000 with each nibble in the coresponding
			//high bits of the RG and B channels.
			gl.glColor4ub((byte) ((pickParts.size() >> 4) & 0xF0),
					(byte) ((pickParts.size() << 0) & 0xF0),
					(byte) ((pickParts.size() << 4) & 0xF0), (byte) 1);
			pickParts.add(c);
			
			if (isDrawnTransparent(c)) {
				gl.glEnable(GL.GL_CULL_FACE);
				gl.glCullFace(GL.GL_FRONT);
				cr.renderGeometry(gl, c);
				gl.glDisable(GL.GL_CULL_FACE);
			} else {
				cr.renderGeometry(gl, c);
			}
		}

		ByteBuffer bb = ByteBuffer.allocateDirect(4);

		gl.glReadPixels(p.x, p.y, 1, 1, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, bb);

		final int pickColor = bb.getInt();
		final int pickIndex = ((pickColor >> 20) & 0xF00) | ((pickColor >> 16) & 0x0F0)
				| ((pickColor >> 12) & 0x00F);

		if ( pickIndex < 0 || pickIndex > pickParts.size() - 1 )
			return null;
		
		return pickParts.get(pickIndex);
	}

	public void render(GLAutoDrawable drawable, Configuration configuration,
			Set<RocketComponent> selection) {
		if (cr == null)
			throw new IllegalStateException(this + " Not Initialized");

		GL2 gl = drawable.getGL().getGL2();

		gl.glEnable(GL.GL_DEPTH_TEST); // enables depth testing
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		// Draw all inner components
		for (RocketComponent c : configuration) {
			if (isDrawn(c)) {
				if (!isDrawnTransparent(c)) {
					renderComponent(gl, c, 1.0f);
				}
			}
		}

		renderMotors(gl, configuration);

		// Draw Tube and Transition back faces, blended with depth test
		// so that they show up behind.
		gl.glEnable(GL.GL_CULL_FACE);
		gl.glCullFace(GL.GL_FRONT);
		for (RocketComponent c : configuration) {
			if (isDrawn(c)) {
				if (isDrawnTransparent(c)) {
					renderComponent(gl, c, 1.0f);
				}
			}
		}
		gl.glDisable(GL.GL_CULL_FACE);

		// Draw T&T front faces blended, without depth test
		gl.glEnable(GL.GL_BLEND);
		gl.glEnable(GL.GL_CULL_FACE);
		gl.glCullFace(GL.GL_BACK);
		for (RocketComponent c : configuration) {
			if (isDrawn(c)) {
				if (isDrawnTransparent(c)) {
					renderComponent(gl, c, 0.2f);
				}
			}
		}
		gl.glDisable(GL.GL_BLEND);
		gl.glDisable(GL.GL_CULL_FACE);

		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_EMISSION,
				selectedEmissive, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_DIFFUSE, colorBlack, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT, colorBlack, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, colorBlack, 0);
		
		gl.glDepthMask(false);
		gl.glDisable(GL.GL_DEPTH_TEST);
		gl.glEnable(GL.GL_STENCIL_TEST);

		for (RocketComponent c : configuration) {
			if (selection.contains(c)) {
				// So it is faster to do this once before the loop,
				// but then the outlines are not as good if you multi-select.
				// Not sure which to do.

				gl.glStencilMask(1);
				gl.glDisable(GL.GL_SCISSOR_TEST);
				gl.glClearStencil(0);
				gl.glClear(GL.GL_STENCIL_BUFFER_BIT);
				gl.glStencilMask(0);

				gl.glStencilFunc(GL.GL_ALWAYS, 1, 1);
				gl.glStencilMask(1);
				gl.glColorMask(false, false, false, false);
				gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
				gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_REPLACE);
				cr.renderGeometry(gl, c);
				gl.glStencilMask(0);

				gl.glColorMask(true, true, true, true);
				gl.glStencilFunc(GL.GL_NOTEQUAL, 1, 1);
				gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
				gl.glLineWidth(5.0f);
				cr.renderGeometry(gl, c);
			}
		}
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
		gl.glDepthMask(true);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_EMISSION,
				colorBlack, 0);
		gl.glDisable(GL.GL_STENCIL_TEST);
		gl.glEnable(GL.GL_DEPTH_TEST);
	}

	private void renderMotors(GL2 gl, Configuration configuration) {
		String motorID = configuration.getMotorConfigurationID();
		Iterator<MotorMount> iterator = configuration.motorIterator();
		while (iterator.hasNext()) {
			MotorMount mount = iterator.next();
			Motor motor = mount.getMotor(motorID);
			double length = motor.getLength();
			double radius = motor.getDiameter() / 2;

			Coordinate[] position = ((RocketComponent) mount)
					.toAbsolute(new Coordinate(((RocketComponent) mount)
							.getLength() + mount.getMotorOverhang() - length));

			for (int i = 0; i < position.length; i++) {
				cr.renderMotor(gl, position[i], length, radius);
			}
		}

	}

	
	public void renderComponent(GL2 gl, RocketComponent c, float alpha) {
		gl.glLightModeli(GL2ES1.GL_LIGHT_MODEL_TWO_SIDE, 1);

		getOutsideColor(c, alpha, color);
		gl.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_DIFFUSE, color, 0);
		gl.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_AMBIENT, color, 0);

		getSpecularColor(c, alpha, color);
		gl.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_SPECULAR, color, 0);
		gl.glMateriali(GL.GL_FRONT, GLLightingFunc.GL_SHININESS,
				getShininess(c));

		getInsideColor(c, alpha, color);
		gl.glMaterialfv(GL.GL_BACK, GLLightingFunc.GL_DIFFUSE, color, 0);
		gl.glMaterialfv(GL.GL_BACK, GLLightingFunc.GL_AMBIENT, color, 0);

		cr.renderGeometry(gl, c);
	}

	private int getShininess(RocketComponent c) {
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
			}
			return 100;
		} else {
			return 20;
		}
	}

	private void getSpecularColor(RocketComponent c, float alpha, float[] out) {
		int shine = getShininess(c);
		float m = (float) shine / 128.0f;
		float d = 0.9f;
		getOutsideColor(c, alpha, out);
		out[0] = Math.max(out[0], d) * m;
		out[1] = Math.max(out[1], d) * m;
		out[2] = Math.max(out[2], d) * m;
	}

	private void getInsideColor(RocketComponent c, float alpha, float[] out) {
		float d = 0.4f;
		getOutsideColor(c, alpha, out);
		out[0] *= d;
		out[1] *=  d;
		out[2] *= d;
	}

	private HashMap<Class<?>, Color> defaultColorCache = new HashMap<Class<?>, Color>();
	private void getOutsideColor(RocketComponent c, float alpha, float[] out) {
		Color col;
		col = c.getColor();
		if (col == null){
			if ( defaultColorCache.containsKey(c.getClass()) ){
				col = defaultColorCache.get(c.getClass());
			} else {
				col = Application.getPreferences().getDefaultColor(c.getClass());
				defaultColorCache.put(c.getClass(), col);
			}
		}
			
		out[0] = Math.max(0.2f, (float) col.getRed() / 255f);
		out[1] = Math.max(0.2f, (float) col.getGreen() / 255f);
		out[2] = Math.max(0.2f, (float) col.getBlue() / 255f);
		out[3] = alpha;
	}
}
