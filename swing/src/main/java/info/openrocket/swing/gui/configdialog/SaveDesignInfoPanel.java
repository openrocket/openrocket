package info.openrocket.swing.gui.configdialog;

import net.miginfocom.swing.MigLayout;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.startup.Preferences;

import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.widgets.SelectColorButton;

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
    private static final Preferences preferences = Application.getPreferences();

    public SaveDesignInfoPanel(OpenRocketDocument d, RocketComponent c, JDialog parent) {
        super(d, c, parent);

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
        this.cancelButton = new SelectColorButton(trans.get("dlg.but.cancel"));
        this.cancelButton.setToolTipText(trans.get("RocketCompCfg.btn.Cancel.ttip"));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // Don't do anything on cancel if you are editing an existing component, and it is not modified
                if (!isNewComponent && parent != null && (parent instanceof ComponentConfigDialog && !((ComponentConfigDialog) parent).isModified())) {
                    disposeDialog();
                    return;
                }
                // Apply the cancel operation if set to auto discard in preferences
                if (!preferences.isShowDiscardConfirmation()) {
                    ComponentConfigDialog.clearConfigListeners = false;		// Undo action => config listeners of new component will be cleared
                    disposeDialog();
                    document.undo();
                    return;
                }

                // Yes/No dialog: Are you sure you want to discard your changes?
                JPanel msg = createCancelOperationContent();
                int resultYesNo = JOptionPane.showConfirmDialog(SaveDesignInfoPanel.this, msg,
                        trans.get("RocketCompCfg.CancelOperation.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (resultYesNo == JOptionPane.YES_OPTION) {
                    ComponentConfigDialog.clearConfigListeners = false;		// Undo action => config listeners of new component will be cleared
                    disposeDialog();
                    document.undo();
                }
            }
        });
        buttonPanel.add(cancelButton, "split 2, right, gapleft 30lp");

        //// Ok button
        this.okButton = new SelectColorButton(trans.get("dlg.but.ok"));
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

    @Override
    public void updateFields() {
        // Do nothing
    }
}
