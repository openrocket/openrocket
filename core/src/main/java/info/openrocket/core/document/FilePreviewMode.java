package info.openrocket.core.document;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;

/**
 * Which view of the rocket to store as the preview image in design files, if any.
 */
public enum FilePreviewMode {

	/**
	 * Store the 3D view of the rocket.
	 */
	VIEW_3D("FilePreviewMode.VIEW_3D"),

	/**
	 * Store the 2D side view of the rocket.
	 */
	VIEW_2D("FilePreviewMode.VIEW_2D"),

	/**
	 * Do not store a preview image.
	 */
	NONE("FilePreviewMode.NONE");

	private static final Translator trans = Application.getTranslator();

	private final String nameKey;

	FilePreviewMode(String nameKey) {
		this.nameKey = nameKey;
	}

	@Override
	public String toString() {
		return trans.get(nameKey);
	}
}
