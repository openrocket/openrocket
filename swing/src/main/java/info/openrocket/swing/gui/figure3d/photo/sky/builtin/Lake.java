package info.openrocket.swing.gui.figure3d.photo.sky.builtin;

import info.openrocket.swing.gui.figure3d.photo.sky.Sky;

public class Lake extends Sky implements Sky.Credit {
	public static final Lake instance = new Lake();

	private Lake() {
	}

	@Override
	public String getCredit() {
		return "Sampo Niskanen.";
	}

	@Override
	public String toString() {
		return "Frozen Lake";
	}
}
