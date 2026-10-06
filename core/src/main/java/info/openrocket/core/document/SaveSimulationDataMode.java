package info.openrocket.core.document;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;

/**
 * Which simulated data to store when saving a design, or whether to ask the user.
 */
public enum SaveSimulationDataMode {

	/**
	 * Ask the user which simulated data to store.
	 */
	ASK("SaveSimulationDataMode.ASK"),

	/**
	 * Always store all simulated data.
	 */
	ALL_DATA("StorageOptChooser.rdbut.Allsimdata"),

	/**
	 * Always store only the summary data.
	 */
	SUMMARY_ONLY("StorageOptChooser.rdbut.Onlysummarydata");

	private static final Translator trans = Application.getTranslator();

	private final String nameKey;

	SaveSimulationDataMode(String nameKey) {
		this.nameKey = nameKey;
	}

	/**
	 * Returns the mode that always stores the given choice.
	 *
	 * @param saveSimulationData whether all simulated data is stored
	 * @return the mode for that choice
	 */
	public static SaveSimulationDataMode of(boolean saveSimulationData) {
		return saveSimulationData ? ALL_DATA : SUMMARY_ONLY;
	}

	/**
	 * Applies this mode to the storage options.
	 *
	 * @param options the storage options to update
	 * @return {@code false} if the user needs to be asked, {@code true} if the options were set
	 */
	public boolean applyTo(StorageOptions options) {
		if (this == ASK) {
			return false;
		}
		options.setSaveSimulationData(this == ALL_DATA);
		options.setExplicitlySet(true);
		return true;
	}

	@Override
	public String toString() {
		return trans.get(nameKey);
	}
}
