package info.openrocket.swing.gui.dialogs;

import info.openrocket.swing.gui.util.MessageWidthUtil;
import info.openrocket.swing.gui.util.SwingPreferences;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.DecalNotFoundException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.io.File;

/**
 * Dialog for handling a DecalNotFoundException.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public abstract class DecalNotFoundDialog {

    /**
     * Show a yes/no dialog telling the user that a certain decal source file can't be found and asking whether he/she
     * wants to look for that file. If prompted yes, a FileChooser opens up to select the file. If a file is selected,
     * the source file of the decal, present in <decex>, will be replaced with the selected file.
     * @param parent parent window for the pop-up windows
     * @param decex exception containing the decal file path as message and DecalImage
     * @return true if the decal has been replaced/found, false if the decal source file issue did not get fixed by the user
     */
    public static boolean showDialog(Component parent, DecalNotFoundException decex) {
        Translator trans = Application.getTranslator();

        // Show 'look up file" yes/no dialog
        String message = MessageWidthUtil.setMessageWidth(decex.getMessage(), 400);
        int resultYesNo = JOptionPane.showConfirmDialog(parent, message,
                trans.get("ExportDecalDialog.source.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        int resultFileChooser = JFileChooser.CANCEL_OPTION;

        // Look for the file
        if (resultYesNo == JOptionPane.YES_OPTION) {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());
            resultFileChooser = chooser.showOpenDialog(parent);
            if (resultFileChooser == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                decex.getDecal().setDecalFile(file);
                ((SwingPreferences) Application.getPreferences()).setDefaultDirectory(chooser.getCurrentDirectory());
            }
        }
        return (resultYesNo == JOptionPane.YES_OPTION) && (resultFileChooser == JFileChooser.APPROVE_OPTION);
    }
}
