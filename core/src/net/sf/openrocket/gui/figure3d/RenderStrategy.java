package net.sf.openrocket.gui.figure3d;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import net.sf.openrocket.rocketcomponent.RocketComponent;

public abstract class RenderStrategy {
	public abstract boolean isDrawn(RocketComponent c);

	public abstract boolean isDrawnTransparent(RocketComponent c);

	public abstract void preGeometry(GL2 gl, RocketComponent c, float alpha);

	public abstract void postGeometry(GL2 gl, RocketComponent c, float alpha);

	public void updateFigure() {

	}
	
	public void init(GLAutoDrawable drawable) {
		
	}
	
	public void dispose(GLAutoDrawable drawable) {
		
	}

}
