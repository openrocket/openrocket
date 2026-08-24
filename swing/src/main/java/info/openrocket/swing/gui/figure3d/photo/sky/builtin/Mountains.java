package info.openrocket.swing.gui.figure3d.photo.sky.builtin;

import info.openrocket.swing.gui.figure3d.photo.sky.Sky;

public class Mountains extends Sky implements Sky.Credit {
	public static final Mountains instance = new Mountains();

	private Mountains() {
	}

	@Override
	public String getCredit() {
		return "Rendering by Bill Kuker.\nCC0 Public Domain.";
	}

	@Override
	public String toString() {
		return "Mountains";
	}
}
