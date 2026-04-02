package info.openrocket.swing.gui.figure3d.photo.sky.builtin;

import info.openrocket.core.util.Chars;
import info.openrocket.swing.gui.figure3d.photo.sky.Sky;

public class Storm extends Sky implements Sky.Credit {
	public static final Storm instance = new Storm();

	private Storm() {
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
