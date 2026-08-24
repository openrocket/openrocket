package info.openrocket.swing.gui.figure3d.photo.sky.builtin;

import info.openrocket.core.util.Chars;
import info.openrocket.swing.gui.figure3d.photo.sky.Sky;

public class Meadow extends Sky implements Sky.Credit {
	public static final Meadow instance = new Meadow();

	private Meadow() {
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
