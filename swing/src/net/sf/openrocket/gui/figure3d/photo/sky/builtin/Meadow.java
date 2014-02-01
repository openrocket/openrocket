package net.sf.openrocket.gui.figure3d.photo.sky.builtin;

import net.sf.openrocket.gui.figure3d.photo.sky.Sky.Credit;
import net.sf.openrocket.gui.figure3d.photo.sky.SkyBox;

public class Meadow extends SkyBox implements Credit {
	public static final Meadow instance = new Meadow();
	
	private Meadow() {
		super(Meadow.class.getResource("/datafiles/sky/Meadow/").toString());
	}
	
	@Override
	public String getCredit() {
		return "© Emil Persson, aka Humus.\nCC-BY 3.0 Attribution License.";
	}
	
	@Override
	public String toString() {
		return "Meadow";
	}
}
