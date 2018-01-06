package net.sf.openrocket.gui.figure3d;

import java.awt.Point;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.Vector;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.fixedfunc.GLLightingFunc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.gui.figure3d.geometry.ComponentRenderer;
import net.sf.openrocket.gui.figure3d.geometry.DisplayListComponentRenderer;
import net.sf.openrocket.gui.figure3d.geometry.Geometry;
import net.sf.openrocket.gui.figure3d.geometry.Geometry.Surface;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
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
	
	public RocketComponent pick(GLAutoDrawable drawable, FlightConfiguration configuration, Point p,
			Set<RocketComponent> ignore) {
		final GL2 gl = drawable.getGL().getGL2();
		gl.glEnable(GL.GL_DEPTH_TEST);
		
		// Store a vector of pickable parts.
		final Vector<RocketComponent> pickParts = new Vector<RocketComponent>();
		
		for (RocketComponent c : configuration.getActiveComponents()) {
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
			    cr.getComponentGeometry(c).render(gl, Surface.INSIDE);
			} else {
			    cr.getComponentGeometry(c).render(gl, Surface.ALL);
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
	    System.err.println(String.format("==== Building tree geometry ===="));
	    return getTreeGeometry("", new ArrayList<Geometry>(), config, config.getRocket(), Transformation.IDENTITY);
	}
	
	private Collection<Geometry> getTreeGeometry(String indent, Collection<Geometry> treeGeometry, FlightConfiguration config, RocketComponent comp, final Transformation parentTransform){
	    final int instanceCount = comp.getInstanceCount();
        double[] instanceAngles = comp.getInstanceAngles();
        Coordinate[] instanceLocations = comp.getInstanceLocations();

        if( instanceLocations.length != instanceAngles.length ){
            throw new ArrayIndexOutOfBoundsException(String.format("lengths of location array (%d) and angle arrays (%d) differs! (in: %s) ", instanceLocations.length, instanceAngles.length, comp.getName()));
        }

        // iterate over the aggregated instances for the whole tree.
        for( int instanceNumber = 0; instanceNumber < instanceCount; ++instanceNumber) {
            Coordinate currentLocation = instanceLocations[instanceNumber];
            final double currentAngle = instanceAngles[instanceNumber];
            
//            System.err.println( String.format("%s[ %s ]", indent, comp.getName()));
//            System.err.println( String.format("%s  :: %12.8g / %12.8g / %12.8g (m) @ %8.4g (rads) ", indent, currentLocation.x, currentLocation.y, currentLocation.z, currentAngle ));
            
            Transformation currentTransform = parentTransform
                    .applyTransformation( Transformation.getTranslationTransform( currentLocation))
                    .applyTransformation( Transformation.rotate_x( currentAngle ));

            
            // recurse into inactive trees: allow active stages inside inactive stages
            for(RocketComponent child: comp.getChildren()) {
                getTreeGeometry(indent+"   ", treeGeometry, config, child, currentTransform );
            }

            Geometry geom = cr.getComponentGeometry( comp, currentTransform );
            geom.active = config.isComponentActive( comp );
            treeGeometry.add( geom );
        }
        return treeGeometry;
	}
	
	private void renderTree( GL2 gl, final Collection<Geometry> geometryList){
	    for(Geometry geom: geometryList ) {
            if( geom.active ) {
                if( isDrawnTransparent( (RocketComponent)geom.obj) ){
                    // Draw T&T front faces blended, without depth test
                    renderComponent(gl, geom, 0.2f);
                }else{
                    renderComponent(gl, geom, 1.0f);
                }
            }
        }
    }
	
	private void renderMotors(GL2 gl, FlightConfiguration configuration) {
//		FlightConfigurationId motorID = configuration.getFlightConfigurationID();
//		
//		for( RocketComponent comp : configuration.getActiveComponents()){
//			if( comp instanceof MotorMount){
//			
//				MotorMount mount = (MotorMount) comp;
//				Motor motor = mount.getMotorInstance(motorID).getMotor();
//				if( null == motor )???;
//				double length = motor.getLength();
//			
//				Coordinate[] position = ((RocketComponent) mount).toAbsolute(new Coordinate(((RocketComponent) mount)
//						.getLength() + mount.getMotorOverhang() - length));
//			
//				for (int i = 0; i < position.length; i++) {
//					gl.glPushMatrix();
//					gl.glTranslated(position[i].x, position[i].y, position[i].z);
//					renderMotor(gl, motor);
//					gl.glPopMatrix();
//				}
//			}
//		}
		
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
