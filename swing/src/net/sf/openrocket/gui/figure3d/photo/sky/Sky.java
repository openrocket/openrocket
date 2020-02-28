package net.sf.openrocket.gui.figure3d.photo.sky;

import com.jogamp.opengl.GL2;

import net.sf.openrocket.gui.figure3d.TextureCache;

public abstract class Sky {
	public abstract void draw(GL2 gl, final TextureCache cache);
	
	public static interface Credit {
		public String getCredit();
	}
}
