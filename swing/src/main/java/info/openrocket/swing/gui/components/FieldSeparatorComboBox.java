package info.openrocket.swing.gui.components;

import javax.swing.JComboBox;

import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;

/**
 * A reusable combo box for selecting CSV field separators.
 * Provides the same options as CsvOptionPanel: comma, semicolon, space, and tab.
 */
@SuppressWarnings("serial")
public class FieldSeparatorComboBox extends JComboBox<String> {

	private static final Translator trans = Application.getTranslator();
	private static final String SPACE = trans.get("CsvOptionPanel.separator.space");
	private static final String TAB = trans.get("CsvOptionPanel.separator.tab");

	/**
	 * Creates a new field separator combo box with default options.
	 * The combo box is editable, allowing users to enter custom separators.
	 */
	public FieldSeparatorComboBox() {
		super(new String[] { ",", ";", SPACE, TAB });
		setEditable(true);
	}

	/**
	 * Gets the selected separator as a character.
	 * Handles special cases for space and tab (which are displayed as translated strings).
	 *
	 * @return the separator character
	 */
	public char getSeparatorChar() {
		String separator = getSelectedItem().toString();
		if (separator.equals(SPACE)) {
			return ' ';
		} else if (separator.equals(TAB)) {
			return '\t';
		} else if (separator.length() > 0) {
			return separator.charAt(0);
		}
		return ',';
	}

	/**
	 * Sets the selected separator from a character.
	 * Converts the character to the appropriate display string.
	 *
	 * @param separator the separator character
	 */
	public void setSeparatorChar(char separator) {
		switch (separator) {
			case ',':
				setSelectedItem(",");
				break;
			case ';':
				setSelectedItem(";");
				break;
			case ' ':
				setSelectedItem(SPACE);
				break;
			case '\t':
				setSelectedItem(TAB);
				break;
			default:
				setSelectedItem(String.valueOf(separator));
				break;
		}
	}

	/**
	 * Gets the selected separator as a string (for compatibility with existing code).
	 *
	 * @return the separator string
	 */
	public String getSeparatorString() {
		String separator = getSelectedItem().toString();
		if (separator.equals(SPACE)) {
			return " ";
		} else if (separator.equals(TAB)) {
			return "\t";
		}
		return separator;
	}

	/**
	 * Loads the separator from application preferences.
	 * Handles special cases for space and tab characters.
	 *
	 * @param preferenceKey the preference key to load from
	 * @param defaultValue the default value if the preference is not set
	 */
	public void loadFromPreferences(String preferenceKey, String defaultValue) {
		String savedSeparator = Application.getPreferences().getString(preferenceKey, defaultValue);
		// Handle special cases for space and tab
		if (savedSeparator.equals(" ")) {
			setSeparatorChar(' ');
		} else if (savedSeparator.equals("\t")) {
			setSeparatorChar('\t');
		} else if (savedSeparator.length() > 0) {
			setSeparatorChar(savedSeparator.charAt(0));
		}
	}

	/**
	 * Loads the separator from the standard export field separator preference.
	 * This is a convenience method for the most common use case.
	 */
	public void loadFromExportPreferences() {
		loadFromPreferences(ApplicationPreferences.EXPORT_FIELD_SEPARATOR, ",");
	}
}

