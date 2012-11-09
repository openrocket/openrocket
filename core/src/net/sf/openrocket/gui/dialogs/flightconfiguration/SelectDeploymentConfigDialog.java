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
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration.DeployEvent;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.unit.UnitGroup;

public class SelectDeploymentConfigDialog extends JDialog {

	DeploymentConfiguration newConfiguration;

	
	SelectDeploymentConfigDialog( JDialog parent, final Rocket rocket, final RecoveryDevice component ) {
		super(parent, FlightConfigurationDialog.trans.get("edtmotorconfdlg.title.Selectdeploymentconf"),Dialog.ModalityType.APPLICATION_MODAL);

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
		
		//// Deployment
		//// Deploys at:
		panel.add(new JLabel(FlightConfigurationDialog.trans.get("ParachuteCfg.lbl.Deploysat")), "");
		
		final JComboBox event = new JComboBox(new BasicEnumModel<DeployEvent>(DeployEvent.class));
		event.setSelectedItem( newConfiguration.getDeployEvent() );
		panel.add(event, "spanx 3, growx, wrap");
		
		// ... and delay
		//// plus
		panel.add(new JLabel(FlightConfigurationDialog.trans.get("ParachuteCfg.lbl.plusdelay")), "right");
		
		final DoubleModel delay = new DoubleModel(newConfiguration.getDeployDelay(), UnitGroup.UNITS_NONE, 0);
		final JSpinner delaySpinner = new JSpinner( delay.getSpinnerModel() );
		delaySpinner.setEditor(new SpinnerEditor(delaySpinner,3));
		panel.add(delaySpinner, "spanx, split");
		
		//// seconds
		panel.add(new JLabel(FlightConfigurationDialog.trans.get("ParachuteCfg.lbl.seconds")), "wrap paragraph");
		
		// Altitude:
		JLabel label = new JLabel(FlightConfigurationDialog.trans.get("ParachuteCfg.lbl.Altitude"));
		panel.add(label);
		
		final DoubleModel alt = new DoubleModel(newConfiguration.getDeployAltitude(), UnitGroup.UNITS_DISTANCE, 0);
		
		final JSpinner altSpinner = new JSpinner(alt.getSpinnerModel());
		altSpinner.setEditor(new SpinnerEditor(altSpinner));
		panel.add(altSpinner, "growx");
		UnitSelector unit = new UnitSelector(alt);
		panel.add(unit, "growx");
		BasicSlider slider = new BasicSlider(alt.getSliderModel(100, 1000));
		panel.add(slider, "w 100lp, wrap");

		event.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if ( event.getSelectedItem() == DeployEvent.ALTITUDE ) {
					altSpinner.setEnabled(true);
				} else {
					altSpinner.setEnabled(false);
				}
				
			}
			
		});

		// Set the value of the combo box at the end to take advantage of the action listener above.
		event.setSelectedItem( newConfiguration.getDeployEvent() );
		
		JButton okButton = new JButton(FlightConfigurationDialog.trans.get("button.ok"));
		okButton.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				//// extract deployment type;
				DeployEvent deployEvent = (DeployEvent) event.getSelectedItem();
				newConfiguration.setDeployEvent(deployEvent);
				
				//// extract deployment time;
				double deployDelay = delay.getValue();
				newConfiguration.setDeployDelay(deployDelay);
				
				//// extract altitude;
				double deployAltitude = alt.getValue();
				newConfiguration.setDeployAltitude(deployAltitude);
				
				component.setFlightConfiguration(configId, newConfiguration);
				
				SelectDeploymentConfigDialog.this.setVisible(false);
			}
			
		});
		
		panel.add( okButton );
		
		JButton cancel = new JButton(FlightConfigurationDialog.trans.get("button.cancel"));
		cancel.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				SelectDeploymentConfigDialog.this.setVisible(false);
			}
			
		});
		
		panel.add( cancel );

		this.setContentPane(panel);
		this.validate();
		this.pack();
		this.setLocationByPlatform(true);

	}


}
