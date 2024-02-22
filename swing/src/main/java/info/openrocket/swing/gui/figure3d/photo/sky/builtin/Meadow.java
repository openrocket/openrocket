package info.openrocket.swing.gui.figure3d.photo.sky.builtin;

import info.openrocket.swing.gui.figure3d.photo.sky.Sky.Credit;
import info.openrocket.swing.gui.figure3d.photo.sky.SkyBox;
import info.openrocket.core.util.Chars;

public class Meadow extends SkyBox implements Credit {
	public static final Meadow instance = new Meadow();
	
	private Meadow() {
		super(Meadow.class.getResource("/datafiles/sky/Meadow/").toString());
	}
	
	@Override
	public String getCredit() {
		return Chars.COPY + " Emil Persson, aka Humus.\nCC-BY 3.0 Attribution License.";
	}
	
	@Override
	public String toString() {
		return "Meadow";
	}
}
