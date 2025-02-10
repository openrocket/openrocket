package info.openrocket.core.rocketcomponent;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;

import java.util.Locale;

/**
 * The type of design of a rocket, e.g. original design, clone kit, etc.
 */
public enum DesignType {
	ORIGINAL("DesignType.Originaldesign"),
	COMMERCIAL_KIT("DesignType.Commercialkit"),
	CLONE_KIT("DesignType.Clonekit"),
	UPSCALE_KIT("DesignType.Upscalekit"),
	DOWNSCALE_KIT("DesignType.Downscalekit"),
	MODIFIED_KIT("DesignType.Modificationkit"),
	KIT_BASH("DesignType.Kitbashkit");

	private static final Translator trans = Application.getTranslator();
	private final String translationKey;

	DesignType(String translationKey) {
		this.translationKey = translationKey;
	}

	public String getName() {
		return trans.get(translationKey);
	}

	/**
	 * Returns a string that can be used to store the design type in a file.
	 * The result is a lowercase string with underscores removed (for some historical reason, that's how OpenRocket
	 * does it)...
	 * @return the storable string
	 */
	public String getStorableString() {
		return name().toLowerCase(Locale.ENGLISH).replace("_", "");
	}
}
