package info.openrocket.swing.gui.choosers;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.preferences.ApplicationPreferences;

public interface OptionChooser {
    void storeOptions(OpenRocketDocument document, ApplicationPreferences preferences);
}
