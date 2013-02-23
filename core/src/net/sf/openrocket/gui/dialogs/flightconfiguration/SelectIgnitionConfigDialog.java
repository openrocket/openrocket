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
import net.sf.openrocket.rocketcomponent.IgnitionConfiguration;
import net.sf.openrocket.rocketcomponent.IgnitionConfiguration.IgnitionEvent;
import net.sf.openrocket.rocketcomponent.MotorConfiguration;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.unit.UnitGroup;

public class SelectIgnitionConfigDialog extends JDialog {
	
	MotorConfiguration newConfiguration;
	
	SelectIgnitionConfigDialog(JDialog parent, final Rocket rocket, final MotorMount component) {
		super(parent, FlightConfigurationDialog.trans.get("edtmotorconfdlg.title.Selectignitionconf"), Dialog.ModalityType.APPLICATION_MODAL);
		final String configId = rocket.getDefaultConfiguration().getFlightConfigurationID();
		
		newConfiguration = component.getFlightConfiguration(configId);
		if (newConfiguration == null) {
			newConfiguration = component.getDefaultFlightConfiguration().clone();
		} else {
			// Clone the existing so cancel works.  When the user selects OK, this configuration
			// is put back in there.
			newConfiguration = newConfiguration.clone();
		}
		// MotorConfiguration is a little wierd.  It is possible for the MotorConfiguration
		// to be non-null (for example, a motor is selected) but the ignition spec is null
		// (ignition is not overridden).  In order to accomodate this, we need to test
		// for IgnitionEvent and copy from the default config.
		if (newConfiguration.getIgnitionEvent() == null) {
			MotorConfiguration oldConfig = component.getDefaultFlightConfiguration();
			newConfiguration.setIgnitionDelay(oldConfig.getIgnitionDelay());
			newConfiguration.setIgnitionEvent(oldConfig.getIgnitionEvent());
		}
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		// Select ignition event
		//// Ignition at:
		panel.add(new JLabel(FlightConfigurationDialog.trans.get("MotorCfg.lbl.Ignitionat")), "");
		
		final JComboBox event = new JComboBox(new BasicEnumModel<IgnitionConfiguration.IgnitionEvent>(IgnitionConfiguration.IgnitionEvent.class));
		event.setSelectedItem(newConfiguration.getIgnitionEvent());
		panel.add(event, "growx, wrap");
		
		// ... and delay
		//// plus
		panel.add(new JLabel(FlightConfigurationDialog.trans.get("MotorCfg.lbl.plus")), "gap indent, skip 1, span, split");
		
		Double delayValue = newConfiguration.getIgnitionDelay();
		final DoubleModel delay = new DoubleModel((delayValue == null ? 0 : delayValue.doubleValue()), UnitGroup.UNITS_NONE, 0d);
		JSpinner spin = new JSpinner(delay.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin, 3));
		panel.add(spin, "gap rel rel");
		
		//// seconds
		panel.add(new JLabel(FlightConfigurationDialog.trans.get("MotorCfg.lbl.seconds")), "wrap unrel");
		
		JButton okButton = new JButton(FlightConfigurationDialog.trans.get("button.ok"));
		okButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				//// extract ignition event type;
				IgnitionConfiguration.IgnitionEvent ignitionEvent = (IgnitionConfiguration.IgnitionEvent) event.getSelectedItem();
				newConfiguration.setIgnitionEvent(ignitionEvent);
				
				//// extract ignition delay time;
				double ignitionDelay = delay.getValue();
				newConfiguration.setIgnitionDelay(ignitionDelay);
				
				
				component.setFlightConfiguration(configId, newConfiguration);
				
				SelectIgnitionConfigDialog.this.setVisible(false);
			}
			
		});
		
		panel.add(okButton);
		
		JButton cancel = new JButton(FlightConfigurationDialog.trans.get("button.cancel"));
		cancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				SelectIgnitionConfigDialog.this.setVisible(false);
			}
			
		});
		
		panel.add(cancel);
		
		this.setContentPane(panel);
		this.validate();
		this.pack();
		this.setLocationByPlatform(true);
		
	}
}
