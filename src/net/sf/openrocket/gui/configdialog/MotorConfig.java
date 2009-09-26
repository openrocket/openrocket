package net.sf.openrocket.gui.configdialog;


import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.BooleanModel;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.adaptors.MotorConfigurationModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.dialogs.MotorChooserDialog;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.MotorMount.IgnitionEvent;
import net.sf.openrocket.unit.UnitGroup;

public class MotorConfig extends JPanel {

	private final Rocket rocket;
	private final MotorMount mount;
	private final Configuration configuration;
	private JPanel panel;
	private JLabel motorLabel;
	
	public MotorConfig(MotorMount motorMount) {
		super(new MigLayout("fill"));

		this.rocket = ((RocketComponent)motorMount).getRocket();
		this.mount = motorMount;
		this.configuration = ((RocketComponent)motorMount).getRocket()
			.getDefaultConfiguration();
		
		BooleanModel model;
		
		model = new BooleanModel(motorMount, "MotorMount");
		JCheckBox check = new JCheckBox(model);
		check.setText("This component is a motor mount");
		this.add(check,"wrap");
		
		
		panel = new JPanel(new MigLayout("fill"));
		this.add(panel,"grow, wrap");
		

		// Motor configuration selector
		panel.add(new JLabel("Motor configuration:"), "shrink");
		
		JComboBox combo = new JComboBox(new MotorConfigurationModel(configuration));
		panel.add(combo,"growx");
		
		configuration.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				updateFields();
			}
		});
		
		JButton button = new JButton("New");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String id = rocket.newMotorConfigurationID();
				configuration.setMotorConfigurationID(id);
			}
		});
		panel.add(button, "wrap unrel");
		
		
		// Current motor
		panel.add(new JLabel("Current motor:"), "shrink");
		
		motorLabel = new JLabel();
		motorLabel.setFont(motorLabel.getFont().deriveFont(Font.BOLD));
		updateFields();
		panel.add(motorLabel,"wrap unrel");

		
		
		//  Overhang
		panel.add(new JLabel("Motor overhang:"));
		
		DoubleModel m = new DoubleModel(motorMount, "MotorOverhang", UnitGroup.UNITS_LENGTH);
		
		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"span, split, width :65lp:");
		
		panel.add(new UnitSelector(m),"width :30lp:");
		panel.add(new BasicSlider(m.getSliderModel(-0.02,0.06)),"w 100lp, wrap unrel");


		
		// Select ignition event
		panel.add(new JLabel("Ignition at:"),"");
		
		combo = new JComboBox(new EnumModel<IgnitionEvent>(mount, "IgnitionEvent"));
		panel.add(combo,"growx, wrap");
		
		// ... and delay
		panel.add(new JLabel("plus"),"gap indent, skip 1, span, split");
		
		m = new DoubleModel(mount,"IgnitionDelay",0);
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"gap rel rel");
		
		panel.add(new JLabel("seconds"),"wrap paragraph");


		
		
		// Select etc. buttons
		button = new JButton("Select motor");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String id = configuration.getMotorConfigurationID();
				
				MotorChooserDialog dialog = new MotorChooserDialog(mount.getMotor(id),
						mount.getMotorDelay(id), mount.getMotorMountDiameter());
				dialog.setVisible(true);
				Motor m = dialog.getSelectedMotor();
				double d = dialog.getSelectedDelay();
				
				if (m != null) {
					if (id == null) {
						id = rocket.newMotorConfigurationID();
						configuration.setMotorConfigurationID(id);
					}
					mount.setMotor(id, m);
					mount.setMotorDelay(id, d);
				}
				updateFields();
			}
		});
		panel.add(button,"span, split, growx");
		
		button = new JButton("Remove motor");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mount.setMotor(configuration.getMotorConfigurationID(), null);
				updateFields();
			}
		});
		panel.add(button,"growx, wrap");
		
		
		
		
		
		// Set enabled status
		
		setDeepEnabled(panel, motorMount.isMotorMount());
		check.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				setDeepEnabled(panel, mount.isMotorMount());
			}
		});
		
	}
	
	public void updateFields() {
		String id = configuration.getMotorConfigurationID();
		Motor m = mount.getMotor(id);
		if (m == null)
			motorLabel.setText("None");
		else
			motorLabel.setText(m.getManufacturer().getDisplayName() + " " +
					m.getDesignation(mount.getMotorDelay(id)));
	}
	
	
	private static void setDeepEnabled(Component component, boolean enabled) {
		component.setEnabled(enabled);
		if (component instanceof Container) {
			for (Component c: ((Container) component).getComponents()) {
				setDeepEnabled(c,enabled);
			}
		}
	}
	
}
