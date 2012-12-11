package net.sf.openrocket.gui.dialogs.flightconfiguration;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.BasicEnumModel;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration.SeparationEvent;
import net.sf.openrocket.unit.UnitGroup;

public class SelectSeparationConfigDialog extends JDialog {

	StageSeparationConfiguration newConfiguration;

	SelectSeparationConfigDialog( JDialog parent, final Rocket rocket, final Stage component ) {
		super(parent, FlightConfigurationDialog.trans.get("edtmotorconfdlg.title.Selectseparationconf"),Dialog.ModalityType.APPLICATION_MODAL);
		final String configId = rocket.getDefaultConfiguration().getFlightConfigurationID();
		
		newConfiguration = component.getFlightConfiguration(configId);
		if ( newConfiguration == null ) {
			newConfiguration = component.getDefaultFlightConfiguration().clone();
		} else {
			// Clone the existing so cancel works.  When the user selects OK, this configuration
			// is put back in there.
			newConfiguration = newConfiguration.clone();
		}
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		// Select separation event
		panel.add(new StyledLabel(FlightConfigurationDialog.trans.get("StageConfig.separation.lbl.title"), Style.BOLD), "spanx, wrap rel");
		
		final JComboBox event = new JComboBox(new BasicEnumModel<SeparationEvent>(SeparationEvent.class));
		event.setSelectedItem( newConfiguration.getSeparationEvent() );
		panel.add(event, "");
		
		// ... and delay
		panel.add(new JLabel(FlightConfigurationDialog.trans.get("StageConfig.separation.lbl.plus")), "");
		
		final DoubleModel delay = new DoubleModel(newConfiguration.getSeparationDelay(), UnitGroup.UNITS_NONE, 0);
		JSpinner spin = new JSpinner(delay.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "width 45");
		
		//// seconds
		panel.add(new JLabel(FlightConfigurationDialog.trans.get("StageConfig.separation.lbl.seconds")), "wrap unrel");

		
		JButton okButton = new JButton(FlightConfigurationDialog.trans.get("button.ok"));
		okButton.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				//// extract event type;
				SeparationEvent eventType = (SeparationEvent) event.getSelectedItem();
				newConfiguration.setSeparationEvent(eventType);
				
				//// extract delay time;
				double separationDelay = delay.getValue();
				newConfiguration.setSeparationDelay(separationDelay);
				
				component.setFlightConfiguration(configId, newConfiguration);
				
				SelectSeparationConfigDialog.this.setVisible(false);
			}
			
		});
		
		panel.add( okButton );
		
		JButton cancel = new JButton(FlightConfigurationDialog.trans.get("button.cancel"));
		cancel.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				SelectSeparationConfigDialog.this.setVisible(false);
			}
			
		});
		
		panel.add( cancel );

		this.setContentPane(panel);
		this.validate();
		this.pack();
		this.setLocationByPlatform(true);

	}


}
