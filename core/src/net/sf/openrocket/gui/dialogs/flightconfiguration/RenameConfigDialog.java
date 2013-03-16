package net.sf.openrocket.gui.dialogs.flightconfiguration;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.startup.Application;

public class RenameConfigDialog extends JDialog {
	
	private static final Translator trans = Application.getTranslator();
	
	RenameConfigDialog(final FlightConfigurationDialog parent, final Rocket rocket) {
		super(parent, trans.get("edtmotorconfdlg.title.Renameconf"), Dialog.ModalityType.APPLICATION_MODAL);
		final Configuration config = rocket.getDefaultConfiguration();
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		final JTextArea textbox = new JTextArea(config.getFlightConfigurationDescription());
		panel.add(textbox, "span, w 200lp, wrap");
		
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.changeConfigurationName(textbox.getText());
				RenameConfigDialog.this.setVisible(false);
			}
			
		});
		
		panel.add(okButton);
		
		JButton defaultButton = new JButton("Reset to default");
		defaultButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.changeConfigurationName(null);
				RenameConfigDialog.this.setVisible(false);
			}
			
		});
		
		panel.add(defaultButton);
		
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				RenameConfigDialog.this.setVisible(false);
			}
			
		});
		
		panel.add(cancel);
		
		this.setContentPane(panel);
		this.validate();
		this.pack();
		this.setLocationByPlatform(true);
		
	}
	
}
