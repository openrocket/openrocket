package info.openrocket.swing.gui.widgets;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.FileUtils;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.io.File;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SaveFileChooser extends JFileChooser {
    private static final Translator trans = Application.getTranslator();


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
            if (FileUtils.getIllegalFilenameChar(fileName) != null) {
                return;
            }
        }

        super.setSelectedFile(file);
        fileName = file.getName();
        cwd = getCurrentDirectory();
    }

    @Override
    public void approveSelection() {
        Character c = FileUtils.getIllegalFilenameChar(fileName);
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
