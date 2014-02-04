package net.sf.openrocket.gui.figure3d.photo.sky.builtin;

import javax.media.opengl.GL2;

import net.sf.openrocket.gui.figure3d.TextureCache;
import net.sf.openrocket.gui.figure3d.photo.sky.Sky.Credit;
import net.sf.openrocket.gui.figure3d.photo.sky.SkySphere;

public class Lake extends SkySphere implements Credit {
	
	public static final Lake instance = new Lake();
	
	private Lake() {
		super(Lake.class.getResource("/datafiles/sky/lake.jpg"));
	}
	
	@Override
	public void draw(GL2 gl, final TextureCache cache) {
		//I have not been able to understand the projection in this
		//image but this scale seems to fix it.
		gl.glPushMatrix();
		gl.glScalef(1, 3, 1);
		super.draw(gl, cache);
		gl.glPopMatrix();
	}
	
	@Override
	public String getCredit() {
		return "Sampo Niskanen.";
	}
	
	@Override
	public String toString() {
		return "Frozen Lake";
	}
}
