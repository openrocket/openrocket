package net.sf.openrocket.gui.widgets;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;

public class SaveFileChooser extends JFileChooser {
    private static final Translator trans = Application.getTranslator();

    private static final char[] ILLEGAL_CHARS = new char[] { '/', '\\', ':', '*', '?', '"', '<', '>', '|' };
    public static final int ILLEGAL_FILENAME_ERROR = 23111998;


    @Override
    public int showSaveDialog(Component parent) throws HeadlessException {
        int option = super.showSaveDialog(parent);
        if (option != JFileChooser.APPROVE_OPTION) {
            return option;
        }

        // Check for invalid characters
        File file = getSelectedFile();
        if (file == null) {
            return ERROR_OPTION;
        }
        String filename = file.getName();
        for (char c : ILLEGAL_CHARS) {
            if (filename.indexOf(c) >= 0) {
                // Illegal character found
                JOptionPane.showMessageDialog(parent,
                        String.format(trans.get("SaveAsFileChooser.illegalFilename.message"), filename, c),
                        trans.get("SaveAsFileChooser.illegalFilename.title"),
                        JOptionPane.WARNING_MESSAGE);
                return ILLEGAL_FILENAME_ERROR;
            }
        }

        return option;
    }
}
