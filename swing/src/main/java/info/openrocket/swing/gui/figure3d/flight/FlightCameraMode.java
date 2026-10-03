package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;

/**
 * Camera behaviour for the 3D flight replay.
 */
public enum FlightCameraMode {
	/** Frame the entire flight trajectory; the user orbits/zooms around the whole path. */
	OVERVIEW("Flight3DFrame.camera.overview"),
	/** Keep the camera centered on the rocket as it flies. */
	FOLLOW("Flight3DFrame.camera.follow"),
	/** A fixed camera near the pad that tracks the rocket, like real launch footage. */
	PAD("Flight3DFrame.camera.pad");

	private static final Translator trans = Application.getTranslator();
	private final String labelKey;

	FlightCameraMode(String labelKey) {
		this.labelKey = labelKey;
	}

	@Override
	public String toString() {
		return trans.get(labelKey);
	}
}
