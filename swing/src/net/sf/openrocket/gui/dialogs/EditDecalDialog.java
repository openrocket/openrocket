package net.sf.openrocket.gui.dialogs;

import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

public class EditDecalDialog extends JDialog {
	
	private static final Translator trans = Application.getTranslator();
	
	private JRadioButton systemRadio;
	private JRadioButton commandRadio;
	private JTextArea commandText;
	
	private JCheckBox savePref;
	
	private boolean isCancel = false;
	private boolean editOne = true;
	
	public EditDecalDialog(final Window owner, boolean promptForEditor, int usageCount) {
		super(owner, trans.get("EditDecalDialog.title"), Dialog.ModalityType.APPLICATION_MODAL);
		
		JPanel panel = new JPanel(new MigLayout("fill, ins para"));
		
		if (promptForEditor) {
			JLabel selectLbl = new JLabel(trans.get("EditDecalDialog.lbl.select"));
			panel.add(selectLbl, "gapright, wrap");
			
			ButtonGroup execGroup = new ButtonGroup();
			
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.EDIT)) {
				
				systemRadio = new JRadioButton(trans.get("EditDecalDialog.lbl.system"));
				systemRadio.setSelected(true);
				panel.add(systemRadio, "wrap");
				execGroup.add(systemRadio);
				
				commandRadio = new JRadioButton(trans.get("EditDecalDialog.lbl.cmdline"));
				commandRadio.setSelected(false);
				panel.add(commandRadio, "wrap");
				execGroup.add(commandRadio);
				
				commandText = new JTextArea();
				commandText.setEnabled(false);
				panel.add(commandText, "growx, wrap");
				
				final JButton chooser = new JButton(trans.get("EditDecalDialog.btn.chooser"));
				chooser.setEnabled(false);
				chooser.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						JFileChooser fc = new JFileChooser();
						int action = fc.showOpenDialog(owner);
						if (action == JFileChooser.APPROVE_OPTION) {
							commandText.setText(fc.getSelectedFile().getAbsolutePath());
						}
						
					}
					
				});
				panel.add(chooser, "growx, wrap");
				
				
				commandRadio.addChangeListener(new ChangeListener() {
					
					@Override
					public void stateChanged(ChangeEvent e) {
						boolean enabled = commandRadio.isSelected();
						commandText.setEnabled(enabled);
						chooser.setEnabled(enabled);
					}
					
				});
				
			} else {
				commandText = new JTextArea();
				commandText.setEnabled(false);
				panel.add(commandText, "growx, wrap");
				
				final JButton chooser = new JButton(trans.get("EditDecalDialog.btn.chooser"));
				chooser.setEnabled(false);
				chooser.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						JFileChooser fc = new JFileChooser();
						int action = fc.showOpenDialog(owner);
						if (action == JFileChooser.APPROVE_OPTION) {
							commandText.setText(fc.getSelectedFile().getAbsolutePath());
						}
						
					}
					
				});
				panel.add(chooser, "growx, wrap");
				
			}
		}
		
		if (usageCount > 1) {
			ButtonGroup bg = new ButtonGroup();
			final JRadioButton justThisOne = new JRadioButton("just this one", true);
			justThisOne.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					EditDecalDialog.this.editOne = justThisOne.isSelected();
				}
				
			});
			panel.add(justThisOne, "left");
			bg.add(justThisOne);
			JRadioButton all = new JRadioButton("all", false);
			panel.add(all, "gapleft para, right, wrap");
			bg.add(all);
		}
		
		if (promptForEditor) {
			savePref = new JCheckBox(trans.get("EditDecalDialog.lbl.always"));
			panel.add(savePref, "wrap");
		}
		
		// OK / Cancel buttons
		JButton okButton = new JButton(trans.get("dlg.but.ok"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ok();
			}
		});
		panel.add(okButton, "tag ok, spanx, split");
		
		//// Cancel button
		JButton cancelButton = new JButton(trans.get("dlg.but.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		panel.add(cancelButton, "tag cancel");
		
		this.add(panel);
		
		GUIUtil.rememberWindowSize(this);
		GUIUtil.setDisposableDialogOptions(this, okButton);
		
	}
	
	public boolean isCancel() {
		return isCancel;
	}
	
	public boolean isSavePreferences() {
		return savePref.isSelected();
	}
	
	public boolean isUseSystemEditor() {
		return systemRadio != null && systemRadio.isSelected();
	}
	
	public String getCommandLine() {
		return commandText.getText();
	}
	
	public boolean isEditOne() {
		return editOne;
	}
	
	public void ok() {
		isCancel = false;
		this.setVisible(false);
	}
	
	public void close() {
		isCancel = true;
		this.setVisible(false);
	}
	
}
