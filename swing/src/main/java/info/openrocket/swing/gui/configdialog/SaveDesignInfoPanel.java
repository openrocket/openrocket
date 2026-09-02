package info.openrocket.swing.gui.configdialog;

import info.openrocket.core.preferences.ApplicationPreferences;
import net.miginfocom.swing.MigLayout;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.ModID;

import info.openrocket.swing.gui.components.StyledLabel;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class is used to create a panel that is shown when a new design file is saved. It is used to fill in the design
 * information for the file.
 */
public class SaveDesignInfoPanel extends RocketConfig {
    private static final Translator trans = Application.getTranslator();
    private static final ApplicationPreferences preferences = Application.getPreferences();

    /** State of the rocket when this panel was created, used to detect whether Cancel has anything to undo. */
    private final ModID modIDAtOpen;

    public SaveDesignInfoPanel(OpenRocketDocument d, RocketComponent c, JDialog parent) {
        super(d, c, parent);

        this.modIDAtOpen = d.getRocket().getModID();

        // (Optional) Fill in the design information for this file
        StyledLabel label = new StyledLabel(trans.get("SaveDesignInfoPanel.lbl.FillInInfo"), StyledLabel.Style.BOLD);
        this.add(label, "spanx, wrap para", 0);
    }

    @Override
    protected void addButtons(JButton... buttons) {
        if (buttonPanel != null) {
            this.remove(buttonPanel);
        }

        buttonPanel = new JPanel(new MigLayout("fill, ins 5, hidemode 3"));

        //// Don't show this dialog again
        JCheckBox dontShowAgain = new JCheckBox(trans.get("welcome.dlg.checkbox.dontShowAgain"));
        dontShowAgain.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                preferences.setShowSaveRocketInfo(!((JCheckBox) e.getSource()).isSelected());
            }
        });
        buttonPanel.add(dontShowAgain, "gapright 10, growx");

        //// Cancel button
        this.cancelButton = new JButton(trans.get("dlg.but.cancel"));
        this.cancelButton.setToolTipText(trans.get("RocketCompCfg.btn.Cancel.ttip"));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // Yes/No dialog: Are you sure you want to discard your changes?
                JPanel msg = createCancelOperationContent();
                int resultYesNo = JOptionPane.showConfirmDialog(SaveDesignInfoPanel.this, msg,
                        trans.get("RocketCompCfg.CancelOperation.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (resultYesNo == JOptionPane.YES_OPTION) {
                    disposeDialog();
                    undoChangesSince(document, modIDAtOpen);
                }
            }
        });
        buttonPanel.add(cancelButton, "split 2, right, gapleft 30lp");

        //// Ok button
        this.okButton = new JButton(trans.get("dlg.but.ok"));
        this.okButton.setToolTipText(trans.get("RocketCompCfg.btn.OK.ttip"));
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                disposeDialog();
            }
        });
        buttonPanel.add(okButton);

        this.add(buttonPanel, "newline, spanx, growx");
    }

    /**
     * Discard the design info edits made in this dialog, restoring the state the rocket was in when the dialog
     * was opened. The caller is expected to have added an undo position just before opening the dialog (see
     * {@code BasicFrame.showSaveRocketInfoDialog()}), which is what that undo restores.
     * <p>
     * Nothing is undone when the rocket was not modified while the dialog was open: the document would then be in
     * a clean state, and {@link OpenRocketDocument#undo()} would step back past the undo position and roll back
     * whatever the user did <em>before</em> opening the dialog (see issue #2680).
     *
     * @param document      the document being edited
     * @param modIDAtOpen   the rocket's modification ID at the time the dialog was opened
     */
    static void undoChangesSince(OpenRocketDocument document, ModID modIDAtOpen) {
        if (document.getRocket().getModID() != modIDAtOpen) {
            document.undo();
        }
    }
}
