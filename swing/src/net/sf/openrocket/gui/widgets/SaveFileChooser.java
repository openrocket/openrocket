package net.sf.openrocket.gui.widgets;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.io.File;

public class SaveFileChooser extends JFileChooser {
    private static final Translator trans = Application.getTranslator();

    private static final char[] ILLEGAL_CHARS = new char[] { '/', '\\', ':', '*', '?', '"', '<', '>', '|' };

    private File cwd = null;
    private File currentFile = null;
    private String fileName = null;


    @Override
    public void setSelectedFile(File file) {
        currentFile = file;
        if (file != null) {
            if (file.getParentFile() != getCurrentDirectory()) {
                cwd = getCurrentDirectory();
                fileName = getFilenameInput(file);
                return;
            } else {
                fileName = file.getName();
            }
        }
        super.setSelectedFile(file);
    }

    @Override
    public void approveSelection() {
        if (!containsIllegalChars(fileName)) {
            super.setSelectedFile(currentFile);
            setCurrentDirectory(cwd);
            super.approveSelection();
        }
    }

    private boolean containsIllegalChars(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        for (char c : ILLEGAL_CHARS) {
            if (filename.indexOf(c) >= 0) {
                // Illegal character found
                JOptionPane.showMessageDialog(getParent(),
                        String.format(trans.get("SaveAsFileChooser.illegalFilename.message"), filename, c),
                        trans.get("SaveAsFileChooser.illegalFilename.title"),
                        JOptionPane.WARNING_MESSAGE);
                return true;
            }
        }
        return false;
    }

    private String getFilenameInput(File file) {
        if (file == null) {
            return null;
        }

        String fullPath = file.getAbsolutePath();
        String cwdPath = cwd.getAbsolutePath();

        String relativePath = fullPath.replaceFirst("^" + cwdPath, "").trim();
        relativePath = relativePath.replaceFirst("^" + File.separator, "");

        return relativePath;
    }
}
