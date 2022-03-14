package net.sf.openrocket.gui.figure3d;

import java.awt.Point;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.gui.figure3d.geometry.ComponentRenderer;
import net.sf.openrocket.gui.figure3d.geometry.DisplayListComponentRenderer;
import net.sf.openrocket.gui.figure3d.geometry.Geometry;
import net.sf.openrocket.gui.figure3d.geometry.Geometry.Surface;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.InstanceContext;
import net.sf.openrocket.rocketcomponent.InstanceMap;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Transformation;

/*
 * @author Bill Kuker <bkuker@billkuker.com>
 * @author Daniel Williams <equipoise@gmail.com>
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
	
	public abstract void renderComponent(GL2 gl, Geometry geom, float alpha);
    
	public abstract boolean isDrawnTransparent(RocketComponent c);
	
	public abstract void flushTextureCache(GLAutoDrawable drawable);

	/**
	 * This function is a bit.... unusual.  Instead of computing an inverse transform from the UI window into design-space,
	 * this renders each component with a unique identifiable color ... to a dummy, throwaway canvas:
	 *
	 * Then, we read the pixel (RGB) color value at a point on the canvas, and use that color to identify the component
	 *
	 * @param drawable canvas to draw to
	 * @param configuration active configuration
	 * @param p point to select at
	 * @param ignore list of ignore components
	 * @return optional (nullable) component selection result
	 */
	public RocketComponent pick(GLAutoDrawable drawable, FlightConfiguration configuration, Point p, Set<RocketComponent> ignore) {
		final GL2 gl = drawable.getGL().getGL2();
		gl.glEnable(GL.GL_DEPTH_TEST);

		// Store a vector of pickable parts.
		final Map<Integer, RocketComponent> selectionMap = new HashMap<>();

		Collection<Geometry> geometryList = getTreeGeometry( configuration);
		for(Geometry geom: geometryList ) {
			final RocketComponent comp = geom.getComponent();
			if (ignore != null && ignore.contains(comp))
				continue;

			final int hashCode = comp.hashCode();
			
			selectionMap.put(hashCode, comp);
			
			gl.glColor4ub((byte) ((hashCode >> 24) & 0xFF),  // red channel (LSB)
						  (byte) ((hashCode >> 16) & 0xFF),  // green channel
						  (byte) ((hashCode >> 8) & 0xFF),  // blue channel
						  (byte) ((hashCode) & 0xFF));  // alpha channel (MSB)
			
			if (isDrawnTransparent(comp)) {
				geom.render(gl, Surface.INSIDE);
			} else {
				geom.render(gl, Surface.ALL);
			}
		}

		if (p == null)
			return null; //Allow pick to be called without a point for debugging

		final ByteBuffer buffer = ByteBuffer.allocateDirect(4);
		gl.glReadPixels(p.x, p.y, // coordinates of "first" pixel to read
						1, 1, // width, height of rectangle to read
						GL.GL_RGBA, GL.GL_UNSIGNED_BYTE,
						buffer);  // output buffer
		final int pixelValue = buffer.getInt();
		final RocketComponent selected = selectionMap.get(pixelValue);

		return selected;
	}
	
	public void render(GLAutoDrawable drawable, FlightConfiguration configuration, Set<RocketComponent> selection) {
		
		if (cr == null)
			throw new IllegalStateException(this + " Not Initialized");
		

        Collection<Geometry> geometry = getTreeGeometry( configuration);
        
		GL2 gl = drawable.getGL().getGL2();
		
		gl.glEnable(GL.GL_DEPTH_TEST); // enables depth testing
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		
		{ // Draw selection outline at nearest Z
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_EMISSION, selectedEmissive, 0);
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_DIFFUSE, colorBlack, 0);
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT, colorBlack, 0);
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, colorBlack, 0);
			gl.glLineWidth(5.0f);
			
			for (Geometry geom : geometry) {
			    RocketComponent rc = geom.getComponent();
				if (selection.contains( rc)) {
					// Draw as lines, set Z to nearest
					gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
					gl.glDepthRange(0, 0);
					geom.render(gl, Surface.ALL);
					
					// Draw polygons, always passing depth test,
					// setting Z to farthest
					gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
					gl.glDepthRange(1, 1);
					gl.glDepthFunc(GL.GL_ALWAYS);
					geom.render(gl, Surface.ALL);
					gl.glDepthFunc(GL.GL_LESS);
					gl.glDepthRange(0, 1);
				}
			}
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_EMISSION, colorBlack, 0);
		} // done with selection outline
		
		gl.glEnable(GL.GL_CULL_FACE);
		gl.glCullFace(GL.GL_BACK);
		gl.glEnable( GL.GL_BLEND );

		// needs to be rendered before the components
        renderMotors(gl, configuration);

		// render all components
		renderTree( gl, geometry );
		
		gl.glDisable( GL.GL_BLEND );
	}
	
	private Collection<Geometry> getTreeGeometry( FlightConfiguration config){
		// input
		final InstanceMap imap = config.getActiveInstances();

		// output buffer
		final Collection<Geometry> treeGeometry = new ArrayList<Geometry>();

		for(Map.Entry<RocketComponent, ArrayList<InstanceContext>> entry: imap.entrySet() ) {
			final RocketComponent comp = entry.getKey();
			
			final ArrayList<InstanceContext> contextList = entry.getValue();

			for(InstanceContext context: contextList ) {
				Geometry instanceGeometry = cr.getComponentGeometry( comp, context.transform );
				treeGeometry.add( instanceGeometry );
			}
		}
		return treeGeometry;
	}

	private void renderTree( GL2 gl, final Collection<Geometry> geometryList){
		//cycle through opaque components first, then transparent to preserve proper depth testing
		for(Geometry geom: geometryList ) {
			//if not transparent
			if( !isDrawnTransparent( (RocketComponent)geom.obj) ){
				renderComponent(gl, geom, 1.0f);
			}
		}
		for(Geometry geom: geometryList ) {
			if( isDrawnTransparent( (RocketComponent)geom.obj) ){
				// Draw T&T front faces blended, without depth test
				renderComponent(gl, geom, 0.2f);
			}
		}
	}

	private void renderMotors(GL2 gl, FlightConfiguration configuration) {
		for( MotorConfiguration curMotor : configuration.getActiveMotors()){
			MotorMount mount = curMotor.getMount();
			Motor motor = curMotor.getMotor();
			
			if( null == motor ){
				throw new NullPointerException(" null motor from configuration.getActiveMotors...  this is a bug.");
			}
			
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
		cr.getMotorGeometry(motor).render(gl, Surface.ALL);
	}
	
}
