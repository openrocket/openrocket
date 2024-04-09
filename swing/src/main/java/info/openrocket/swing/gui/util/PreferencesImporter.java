package info.openrocket.swing.gui.util;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JFileChooser;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

public abstract class PreferencesImporter {
    private static final Translator trans = Application.getTranslator();
    private static final Logger log = LoggerFactory.getLogger(PreferencesImporter.class);

    /**
     * Import the preferences by first showing a file chooser dialog to locate the preferences file.
     * @param parent The parent window for the file chooser dialog.
     * @return true if the preferences were imported successfully, false otherwise.
     */
    public static boolean importPreferences(Window parent) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(trans.get("PreferencesImporter.chooser.title"));
        chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());
        chooser.setFileFilter(FileHelper.XML_FILTER);
        chooser.setAcceptAllFileFilterUsed(false);

        int returnVal = chooser.showOpenDialog(parent);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            log.info("Cancelled import of preferences.");
            return false;
        }

        ((SwingPreferences) Application.getPreferences()).setDefaultDirectory(chooser.getCurrentDirectory());

        File importFile = chooser.getSelectedFile();
        return importPreferences(importFile);
    }

    /**
     * Import preferences from an XML file.
     * @param importFile The XML file to import preferences from.
     * @return true if the preferences were imported successfully, false otherwise.
     */
    public static boolean importPreferences(File importFile) {
        try (FileInputStream fis = new FileInputStream(importFile)) {
            Preferences.importPreferences(fis);

            // Ensure units are updated
            ((SwingPreferences) Application.getPreferences()).loadDefaultUnits();

            log.info("Preferences imported successfully.");
            return true;
        } catch (IOException | InvalidPreferencesFormatException e) {
            log.warn("Error while importing preferences: " + e.getMessage());
        }
        return false;
    }
}
