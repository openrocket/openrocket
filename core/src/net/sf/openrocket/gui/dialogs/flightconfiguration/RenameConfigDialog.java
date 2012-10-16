package net.sf.openrocket.gui.dialogs.flightconfiguration;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.Rocket;

public class RenameConfigDialog extends JDialog {
	
	RenameConfigDialog( final Rocket rocket, final FlightConfigurationDialog parent ) {
		super(parent);
		super.setModal(true);
		final Configuration config = rocket.getDefaultConfiguration();
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		final JTextArea textbox = new JTextArea( config.getMotorConfigurationDescription() );
		panel.add(textbox, "span, w 200lp, wrap");
		
		JButton okButton = new JButton("Ok");
		okButton.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				parent.changeConfigurationName(textbox.getText());
				RenameConfigDialog.this.setVisible(false);
			}
			
		});
		
		panel.add( okButton );
		
		JButton defaultButton = new JButton("Reset to default");
		defaultButton.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				parent.changeConfigurationName(null);
				RenameConfigDialog.this.setVisible(false);
			}
			
		});
		
		panel.add( defaultButton );
		
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				RenameConfigDialog.this.setVisible(false);
			}
			
		});
		
		panel.add( cancel );

		this.setContentPane(panel);
		this.validate();
		this.pack();
	}

}
