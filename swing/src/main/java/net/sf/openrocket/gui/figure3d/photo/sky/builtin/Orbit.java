package net.sf.openrocket.gui.figure3d.photo.sky.builtin;

import net.sf.openrocket.gui.figure3d.photo.sky.Sky.Credit;
import net.sf.openrocket.gui.figure3d.photo.sky.SkyPhoto;

public class Orbit extends SkyPhoto implements Credit {
	public static final Orbit instance = new Orbit();
	
	private Orbit() {
		super(Orbit.class.getResource("/datafiles/sky/space.jpg"));
	}
	
	@Override
	public String getCredit() {
		return "NASA";
	}
	
	@Override
	public String toString() {
		return "Orbit";
	}
}
