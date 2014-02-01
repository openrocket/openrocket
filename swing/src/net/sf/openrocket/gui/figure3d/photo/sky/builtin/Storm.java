package net.sf.openrocket.gui.figure3d.photo.sky.builtin;

import net.sf.openrocket.gui.figure3d.photo.sky.Sky.Credit;
import net.sf.openrocket.gui.figure3d.photo.sky.SkyBoxCross;
import net.sf.openrocket.util.Chars;

public class Storm extends SkyBoxCross implements Credit {
	public static final Storm instance = new Storm();
	
	private Storm() {
		super(Storm.class.getResource("/datafiles/sky/cross1.jpg"));
	}
	
	@Override
	public String getCredit() {
		return Chars.COPY + " Jockum Skoglund aka hipshot.\nCC-BY 3.0 Attribution License.";
	}
	
	@Override
	public String toString() {
		return "Stormy Days";
	}
}
