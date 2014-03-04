package net.sf.openrocket.gui.figure3d.photo.sky.builtin;

import net.sf.openrocket.gui.figure3d.photo.sky.Sky.Credit;
import net.sf.openrocket.gui.figure3d.photo.sky.SkyBox;

public class Mountains extends SkyBox implements Credit {
	public static final Mountains instance = new Mountains();
	
	private Mountains() {
		super(Mountains.class.getResource("/datafiles/sky/box/").toString());
	}
	
	@Override
	public String getCredit() {
		return "Rendering by Bill Kuker.\nCC0 Public Domain.";
	}
	
	@Override
	public String toString() {
		return "Mountains";
	}
}
