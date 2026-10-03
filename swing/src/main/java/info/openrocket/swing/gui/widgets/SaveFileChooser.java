package info.openrocket.swing.gui.widgets;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.FileUtils;

import javax.swing.JOptionPane;
import java.io.File;
import java.util.List;

/**
 * Native file chooser for saving files that rejects filenames containing characters
 * that are illegal on any supported platform.
 */
public class SaveFileChooser extends NativeFileChooser {
    private static final Translator trans = Application.getTranslator();


    public enum SelectionMode {
        SINGLE_FILE,
        DIRECTORY
    }

    public SaveFileChooser() {
        setDialogType(SAVE_DIALOG);
        setApproveCallback(this::approveSelection);
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
            setDialogType(SAVE_DIALOG);
            setFileSelectionMode(FILES_ONLY);
            File target = defaultDirectory != null ? new File(defaultDirectory, baseName) : new File(baseName);
            setSelectedFile(target);
            return SelectionMode.SINGLE_FILE;
        }

        setDialogType(OPEN_DIALOG);
        setFileSelectionMode(DIRECTORIES_ONLY);
        if (defaultDirectory != null) {
            setSelectedFile(defaultDirectory);
        }
        return SelectionMode.DIRECTORY;
    }

    int approveSelection(File[] selectedFiles, ApproveContext context) {
        if (isDirectorySelectionEnabled()) {
            return APPROVE_OPTION;
        }

        String fileName = selectedFiles[0].getName();
        Character c = FileUtils.getIllegalFilenameChar(fileName);
        if (c != null) {
            // Swing dialogs cannot be shown while the native dialog is open
            context.showMessageDialog(JOptionPane.WARNING_MESSAGE,
                    trans.get("SaveAsFileChooser.illegalFilename.title"),
                    String.format(trans.get("SaveAsFileChooser.illegalFilename.message"), fileName, c),
                    0);
            return CANCEL_OPTION;
        }
        return APPROVE_OPTION;
    }
}
