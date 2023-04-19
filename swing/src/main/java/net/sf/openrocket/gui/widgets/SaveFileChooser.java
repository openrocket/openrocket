package net.sf.openrocket.gui.widgets;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.io.File;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SaveFileChooser extends JFileChooser {
    private static final Translator trans = Application.getTranslator();

    private static final char[] ILLEGAL_CHARS = new char[] { '/', '\\', ':', '*', '?', '"', '<', '>', '|' };

    private File cwd = null;
    private File currentFile = null;
    private String fileName = null;


    @Override
    public void setSelectedFile(File file) {
        currentFile = file;
        if (file == null) {
            super.setSelectedFile(null);
            return;
        }

        if (file.getParentFile() != getCurrentDirectory()) {
            cwd = getCurrentDirectory();
            fileName = getFilenameInput(currentFile, cwd);
            if (getIllegalChar(fileName) != null) {
                return;
            }
        }

        super.setSelectedFile(file);
        fileName = file.getName();
        cwd = getCurrentDirectory();
    }

    @Override
    public void approveSelection() {
        Character c = getIllegalChar(fileName);
        if (c != null) {
            // Illegal character found
            JOptionPane.showMessageDialog(getParent(),
                    String.format(trans.get("SaveAsFileChooser.illegalFilename.message"), fileName, c),
                    trans.get("SaveAsFileChooser.illegalFilename.title"),
                    JOptionPane.WARNING_MESSAGE);
        } else {
            // Successful filename
            super.setSelectedFile(currentFile);
            setCurrentDirectory(cwd);
            super.approveSelection();
        }
    }

    /**
     * Returns an illegal character if one is found in the filename, otherwise returns null.
     * @param filename The filename to check
     * @return The illegal character, or null if none is found
     */
    private Character getIllegalChar(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        for (char c : ILLEGAL_CHARS) {
            if (filename.indexOf(c) >= 0) {
                return c;
            }
        }
        return null;
    }

    /**
     * Returns the filename input by the user, or null if the filename is invalid.
     * You can't simply use getSelectedFile().getName() because it won't work for malformed filenames.
     * @param file The file to get the filename from
     * @param cwd The current working directory
     * @return The filename input by the user, or null if the filename is invalid
     */
    private String getFilenameInput(File file, File cwd) {
        if (file == null || cwd == null) {
            return null;
        }

        String fullPath = file.getAbsolutePath();
        String cwdPath = cwd.getAbsolutePath();

        try {
            String relativePath = fullPath.replaceFirst(Pattern.quote(cwdPath), "").trim();
            relativePath = relativePath.replaceFirst(Pattern.quote(File.separator), "");
            return relativePath;
        } catch (PatternSyntaxException e) {
            return null;
        }
    }
}
