package net.sf.openrocket.gui.figure3d;

import java.awt.Point;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.fixedfunc.GLLightingFunc;

import net.sf.openrocket.gui.figure3d.geometry.ComponentRenderer;
import net.sf.openrocket.gui.figure3d.geometry.DisplayListComponentRenderer;
import net.sf.openrocket.gui.figure3d.geometry.Geometry.Surface;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * @author Bill Kuker <bkuker@billkuker.com>
 */
public abstract class RocketRenderer {
	protected static final Logger log = LoggerFactory.getLogger(RocketRenderer.class);
	
	final ComponentRenderer cr = new DisplayListComponentRenderer();
	
	private final float[] selectedEmissive = { 1, 0, 0, 1 };
	private final float[] colorBlack = { 0, 0, 0, 1 };
	
	public void init(GLAutoDrawable drawable) {
		cr.init(drawable);
	}
	
	public void dispose(GLAutoDrawable drawable) {
	}
	
	public void updateFigure(GLAutoDrawable drawable) {
		cr.updateFigure(drawable);
	}
	
	public abstract void renderComponent(GL2 gl, RocketComponent c, float alpha);
	
	public abstract boolean isDrawn(RocketComponent c);
	
	public abstract boolean isDrawnTransparent(RocketComponent c);
	
	public abstract void flushTextureCache(GLAutoDrawable drawable);
	
	public RocketComponent pick(GLAutoDrawable drawable, Configuration configuration, Point p,
			Set<RocketComponent> ignore) {
		final GL2 gl = drawable.getGL().getGL2();
		gl.glEnable(GL.GL_DEPTH_TEST);
		
		// Store a vector of pickable parts.
		final Vector<RocketComponent> pickParts = new Vector<RocketComponent>();
		
		for (RocketComponent c : configuration) {
			if (ignore != null && ignore.contains(c))
				continue;
			
			// Encode the index of the part as a color
			// if index is 0x0ABC the color ends up as
			// 0xA0B0C000 with each nibble in the coresponding
			// high bits of the RG and B channels.
			gl.glColor4ub((byte) ((pickParts.size() >> 4) & 0xF0), (byte) ((pickParts.size() << 0) & 0xF0),
					(byte) ((pickParts.size() << 4) & 0xF0), (byte) 1);
			pickParts.add(c);
			
			if (isDrawnTransparent(c)) {
				cr.getGeometry(c, Surface.INSIDE).render(gl);
			} else {
				cr.getGeometry(c, Surface.ALL).render(gl);
			}
		}
		
		ByteBuffer bb = ByteBuffer.allocateDirect(4);

		if (p == null)
			return null; //Allow pick to be called without a point for debugging
			
		gl.glReadPixels(p.x, p.y, 1, 1, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, bb);
		
		final int pickColor = bb.getInt();
		final int pickIndex = ((pickColor >> 20) & 0xF00) | ((pickColor >> 16) & 0x0F0) | ((pickColor >> 12) & 0x00F);
		
		log.trace("Picked pixel color is {} index is {}", pickColor, pickIndex);
		
		if (pickIndex < 0 || pickIndex > pickParts.size() - 1)
			return null;
		
		return pickParts.get(pickIndex);
	}
	
	public void render(GLAutoDrawable drawable, Configuration configuration, Set<RocketComponent> selection) {
		
		if (cr == null)
			throw new IllegalStateException(this + " Not Initialized");
		
		GL2 gl = drawable.getGL().getGL2();
		
		gl.glEnable(GL.GL_DEPTH_TEST); // enables depth testing
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		
		{ // Draw selection outline at nearest Z
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_EMISSION, selectedEmissive, 0);
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_DIFFUSE, colorBlack, 0);
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT, colorBlack, 0);
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, colorBlack, 0);
			gl.glLineWidth(5.0f);
			
			for (RocketComponent c : configuration) {
				if (selection.contains(c)) {
					// Draw as lines, set Z to nearest
					gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
					gl.glDepthRange(0, 0);
					cr.getGeometry(c, Surface.ALL).render(gl);
					
					// Draw polygons, always passing depth test,
					// setting Z to farthest
					gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
					gl.glDepthRange(1, 1);
					gl.glDepthFunc(GL.GL_ALWAYS);
					cr.getGeometry(c, Surface.ALL).render(gl);
					gl.glDepthFunc(GL.GL_LESS);
					gl.glDepthRange(0, 1);
				}
			}
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_EMISSION, colorBlack, 0);
		} // done with selection outline
		
		gl.glEnable(GL.GL_CULL_FACE);
		gl.glCullFace(GL.GL_BACK);
		
		// Draw all inner components
		for (RocketComponent c : configuration) {
			if (isDrawn(c)) {
				if (!isDrawnTransparent(c)) {
					renderComponent(gl, c, 1.0f);
				}
			}
		}
		
		renderMotors(gl, configuration);
		
		// Draw T&T front faces blended, without depth test
		gl.glEnable(GL.GL_BLEND);
		for (RocketComponent c : configuration) {
			if (isDrawn(c)) {
				if (isDrawnTransparent(c)) {
					renderComponent(gl, c, 0.2f);
				}
			}
		}
		gl.glDisable(GL.GL_BLEND);
		
	}
	
	private void renderMotors(GL2 gl, Configuration configuration) {
		String motorID = configuration.getFlightConfigurationID();
		Iterator<MotorMount> iterator = configuration.motorIterator();
		while (iterator.hasNext()) {
			MotorMount mount = iterator.next();
			Motor motor = mount.getMotorConfiguration().get(motorID).getMotor();
			double length = motor.getLength();
			
			Coordinate[] position = ((RocketComponent) mount).toAbsolute(new Coordinate(((RocketComponent) mount)
					.getLength() + mount.getMotorOverhang() - length));
			
			for (int i = 0; i < position.length; i++) {
				gl.glPushMatrix();
				gl.glTranslated(position[i].x, position[i].y, position[i].z);
				renderMotor(gl, motor);
				gl.glPopMatrix();
			}
		}
		
	}
	
	protected void renderMotor(GL2 gl, Motor motor) {
		cr.getGeometry(motor, Surface.ALL).render(gl);
	}
	
}
