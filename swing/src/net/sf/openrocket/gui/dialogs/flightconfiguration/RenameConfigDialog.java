package net.sf.openrocket.gui.dialogs.flightconfiguration;

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.startup.Application;

public class RenameConfigDialog extends JDialog {
	private static final long serialVersionUID = -5423008694485357248L;
	private static final Translator trans = Application.getTranslator();
	
	public RenameConfigDialog(final Window parent, final Rocket rocket, final FlightConfigurationId fcid) {
		super(parent, trans.get("RenameConfigDialog.title"), Dialog.ModalityType.APPLICATION_MODAL);
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		panel.add(new JLabel(trans.get("RenameConfigDialog.lbl.name")), "span, wrap rel");
		
		final JTextField textbox = new JTextField(rocket.getFlightConfiguration(fcid).getName());
		panel.add(textbox, "span, w 200lp, growx, wrap para");
		
		panel.add(new JPanel(), "growx");
		
		JButton okButton = new JButton(trans.get("button.ok"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String newName = textbox.getText();
				rocket.getFlightConfiguration(fcid).setName( newName);
				RenameConfigDialog.this.setVisible(false);
			}
		});
		panel.add(okButton);
		
		JButton renameToDefaultButton = new JButton(trans.get("RenameConfigDialog.but.reset"));
		renameToDefaultButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				rocket.getFlightConfiguration(fcid).setName(null);
				RenameConfigDialog.this.setVisible(false);
			}
		});
		panel.add(renameToDefaultButton);
		
		JButton cancel = new JButton(trans.get("button.cancel"));
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RenameConfigDialog.this.setVisible(false);
			}
		});
		panel.add(cancel);
		
		this.add(panel);
		
		GUIUtil.setDisposableDialogOptions(this, okButton);
	}
}
