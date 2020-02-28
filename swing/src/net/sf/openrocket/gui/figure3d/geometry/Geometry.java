
package net.sf.openrocket.gui.figure3d.geometry;

import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Transformation;

import com.jogamp.opengl.GL2;


/*
 * @author Daniel Williams <equipoise@gmail.com>
 */
public abstract class Geometry {
    public static enum Surface {
        ALL, OUTSIDE, INSIDE, EDGES;
    }

    public static final Geometry EMPTY = new Geometry(){
        @Override
        public void render(GL2 Gl, Surface which){}
    };
    
    public final Object obj;
    public final Transformation transform;
    
    public boolean active;
    
	public abstract void render(GL2 gl, Surface which );
	
	private Geometry() {
	    // seriously, don't call this.
	    this.obj = null;
	    this.transform = null;    
	}
	
    public Geometry( Rocket rocket ) {
        this.obj = rocket;
        this.transform = Transformation.IDENTITY;
    }

    public Geometry( RocketComponent component, Transformation transform) {
        this.obj = component;
        this.transform = transform;
    }

    public Geometry( Motor motor, Transformation transform ) {
        this.obj = motor;
        this.transform = transform;
    }
    
    public RocketComponent getComponent() {
        return (RocketComponent)this.obj;
    }
}
