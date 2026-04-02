package info.openrocket.swing.gui.figure3d.photo.sky.builtin;

import info.openrocket.swing.gui.figure3d.photo.sky.Sky;

public class Orbit extends Sky implements Sky.Credit {
	public static final Orbit instance = new Orbit();

	private Orbit() {
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
