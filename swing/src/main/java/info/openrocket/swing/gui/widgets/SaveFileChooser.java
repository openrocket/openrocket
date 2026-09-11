package info.openrocket.swing.gui.widgets;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.FileUtils;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileSystemView;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SaveFileChooser extends JFileChooser {
    private static final Translator trans = Application.getTranslator();


    public enum SelectionMode {
        SINGLE_FILE,
        DIRECTORY
    }

    public enum ViewType {
        LIST,
        DETAILS
    }

    private File cwd = null;
    private File currentFile = null;
    private String fileName = null;
    private ViewType viewType = ViewType.DETAILS;

    @Override
    public void updateUI() {
        super.updateUI();
        SwingUtilities.invokeLater(this::applyViewType);
    }

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

    /**
     * Configure the chooser for either a single file save or selecting a target
     * directory for multiple files.
     *
     * @param targetNames
     *            the file names (may include paths) that will be exported
     * @param defaultDirectory
     *            optional directory to preselect
     * @return the resulting selection mode
     */
    public SelectionMode configureForTargets(List<String> targetNames, File defaultDirectory) {
        if (defaultDirectory != null) {
            setCurrentDirectory(defaultDirectory);
        }

        if (targetNames == null || targetNames.size() <= 1) {
            String baseName = "untitled";
            if (targetNames != null && !targetNames.isEmpty()) {
                baseName = new File(targetNames.get(0)).getName();
            }
            setDialogType(JFileChooser.SAVE_DIALOG);
            setFileSelectionMode(JFileChooser.FILES_ONLY);
            File target = defaultDirectory != null ? new File(defaultDirectory, baseName) : new File(baseName);
            setSelectedFile(target);
            return SelectionMode.SINGLE_FILE;
        }

        setDialogType(JFileChooser.OPEN_DIALOG);
        setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (defaultDirectory != null) {
            setSelectedFile(defaultDirectory);
        }
        return SelectionMode.DIRECTORY;
    }

    public void setInitialViewType(ViewType viewType) {
        this.viewType = viewType != null ? viewType : ViewType.DETAILS;
        applyViewType();
    }

    private void applyViewType() {
        ActionMap actionMap = getActionMap();
        if (actionMap == null) {
            return;
        }
        String key = viewType == ViewType.LIST ? "viewTypeList" : "viewTypeDetails";
        Action action = actionMap.get(key);
        if (action != null) {
            action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, key));
        }
    }

    @Override
    public void approveSelection() {
        File targetDirectory = getFileSelectionMode() == JFileChooser.DIRECTORIES_ONLY ? currentFile : cwd;
        if (!isFileSystemDirectory(targetDirectory, getFileSystemView())) {
            showInvalidSaveLocationWarning();
            return;
        }

        Character c = FileUtils.getIllegalFilenameChar(fileName);
        if (c != null) {
            // Illegal character found
            showIllegalFilenameWarning(fileName, c);
        } else {
            // Successful filename
            super.setSelectedFile(currentFile);
            setCurrentDirectory(cwd);
            super.approveSelection();
        }
    }

    /**
     * Shows the warning used when the entered filename contains an illegal character.
     *
     * @param fileName the filename entered by the user
     * @param c the offending character
     */
    protected void showIllegalFilenameWarning(String fileName, char c) {
        JOptionPane.showMessageDialog(getParent(),
                String.format(trans.get("SaveAsFileChooser.illegalFilename.message"), fileName, c),
                trans.get("SaveAsFileChooser.illegalFilename.title"),
                JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Shows the warning used when a virtual shell location cannot contain a saved file.
     */
    protected void showInvalidSaveLocationWarning() {
        JOptionPane.showMessageDialog(getParent(),
                trans.get("SaveAsFileChooser.invalidDirectory.message"),
                trans.get("SaveAsFileChooser.invalidDirectory.title"),
                JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Checks that a save destination is backed by the filesystem.  Windows exposes
     * virtual shell nodes such as "This PC" as {@link File} instances even though
     * files cannot be written directly inside them.
     *
     * @param directory the directory selected as, or containing, the save target
     * @param fileSystemView the platform file-system view used by the chooser
     * @return {@code true} when the directory is a real filesystem directory
     */
    static boolean isFileSystemDirectory(File directory, FileSystemView fileSystemView) {
        return directory != null && fileSystemView != null
                && fileSystemView.isFileSystem(directory) && directory.isDirectory();
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

        if (!fullPath.startsWith(cwdPath)) {
            // Windows may normalize the chooser directory (case or 8.3 short names),
            // so retry with canonical paths before treating the input as malformed.
            try {
                fullPath = file.getCanonicalPath();
                cwdPath = cwd.getCanonicalPath();
            } catch (IOException e) {
                // Fall back to the absolute paths.
            }
        }

        try {
            String relativePath = fullPath.replaceFirst(Pattern.quote(cwdPath), "").trim();
            relativePath = relativePath.replaceFirst(Pattern.quote(File.separator), "");
            return relativePath;
        } catch (PatternSyntaxException e) {
            return null;
        }
    }
}
