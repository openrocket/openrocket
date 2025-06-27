package info.openrocket.swing.gui.choosers;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.preferences.ApplicationPreferences;

/**
 * Interface representing any instance that is responsible for the Options chosen within an OpenRocketDocument. Examples
 * include {@link OBJOptionChooser} & {@link StorageOptionChooser}.
 */
public interface OptionChooser {
    /**
     * Store the options this chooser is responsible for to the specified document.
     * @param document The document to store the options for.
     * @param preferences Application preferences that may need alteration due to the newly stored options.
     */
    void storeOptions(OpenRocketDocument document, ApplicationPreferences preferences);
}
