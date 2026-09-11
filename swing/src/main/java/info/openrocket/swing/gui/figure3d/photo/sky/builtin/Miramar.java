package info.openrocket.swing.gui.figure3d.photo.sky.builtin;

import info.openrocket.core.util.Chars;
import info.openrocket.swing.gui.figure3d.photo.sky.Sky;

public class Miramar extends Sky implements Sky.Credit {
	public static final Miramar instance = new Miramar();

	private Miramar() {
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
