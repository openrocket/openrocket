package net.sf.openrocket.gui.figure3d;

import javax.media.opengl.GL2;

import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Color;

public abstract class RenderStrategy {
	public abstract boolean isDrawn(RocketComponent c);
	public abstract boolean isDrawnTransparent(RocketComponent c);
	
	public abstract void preGeometry(GL2 gl, RocketComponent c, float alpha);
	
	public abstract void postGeometry(GL2 gl, RocketComponent c, float alpha);
	
	public void clearCaches(){
		
	}
	
	protected static void convertColor(Color color, float[] out) {
		if ( color == null ){
			out[0] = 1;
			out[1] = 1;
			out[2] = 0;
		} else {
			out[0] = Math.max(0.2f, (float) color.getRed() / 255f);
			out[1] = Math.max(0.2f, (float) color.getGreen() / 255f);
			out[2] = Math.max(0.2f, (float) color.getBlue() / 255f);
		}
	}
}
