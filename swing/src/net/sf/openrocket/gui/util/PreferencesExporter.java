package net.sf.openrocket.gui.util;

import net.sf.openrocket.gui.widgets.SaveFileChooser;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JFileChooser;
import java.awt.Window;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public abstract class PreferencesExporter {
    private static final Translator trans = Application.getTranslator();
    private static final Logger log = LoggerFactory.getLogger(PreferencesExporter.class);

    public static boolean exportPreferences(Window parent, Preferences preferences) {
        JFileChooser chooser = new SaveFileChooser();
        chooser.setDialogTitle(trans.get("PreferencesExporter.chooser.title"));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(FileHelper.XML_FILTER);
        chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());


        // TODO: add storageoptions to choose whether to export user directories or not (default to not)


        //// Ensures No Problems When Choosing File
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            log.info("Cancelled export of preferences.");
            return false;
        }

        ((SwingPreferences) Application.getPreferences()).setDefaultDirectory(chooser.getCurrentDirectory());

        File file = chooser.getSelectedFile();
        if (file == null) {
            log.info("No file selected to export preferences to.");
            return false;
        }

        file = FileHelper.forceExtension(file, "xml");
        if (!FileHelper.confirmWrite(file, parent)) {
            log.info("Cancelled export of preferences.");
            return false;
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            preferences.exportSubtree(fos);
            log.info("Preferences exported successfully.");
        } catch (IOException | BackingStoreException e) {
            log.warn("Error while importing preferences: " + e.getMessage());
        }

        return true;
    }
}
