package info.openrocket.swing.gui.figure3d.photo.sky.builtin;

import info.openrocket.swing.gui.figure3d.photo.sky.Sky.Credit;
import info.openrocket.swing.gui.figure3d.photo.sky.SkyBox;
import info.openrocket.core.util.Chars;

public class Miramar extends SkyBox implements Credit {
	public static final Miramar instance = new Miramar();
	
	private Miramar() {
		super(Miramar.class.getResource("/datafiles/sky/miramar/").toString() + "miramar_");
	}
	
	@Override
	public String getCredit() {
		return Chars.COPY + " Jockum Skoglund aka hipshot.\nCC-BY 3.0 Attribution License.";
	}
	
	@Override
	public String toString() {
		return "Miramar";
	}
}
