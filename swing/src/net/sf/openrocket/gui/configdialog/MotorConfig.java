package net.sf.openrocket.gui.configdialog;


import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.BooleanModel;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.adaptors.FlightConfigurationModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.dialogs.flightconfiguration.FlightConfigurationDialog;
import net.sf.openrocket.gui.dialogs.motor.MotorChooserDialog;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.IgnitionConfiguration;
import net.sf.openrocket.rocketcomponent.MotorConfiguration;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

public class MotorConfig extends JPanel {
	
	private final Rocket rocket;
	private final MotorMount mount;
	private final Configuration configuration;
	private JPanel panel;
	private JLabel motorLabel;
	private static final Translator trans = Application.getTranslator();
	
	public MotorConfig(MotorMount motorMount) {
		super(new MigLayout("fill"));
		
		this.rocket = ((RocketComponent) motorMount).getRocket();
		this.mount = motorMount;
		this.configuration = ((RocketComponent) motorMount).getRocket().getDefaultConfiguration();
		
		BooleanModel model;
		
		model = new BooleanModel(motorMount, "MotorMount");
		JCheckBox check = new JCheckBox(model);
		////This component is a motor mount
		check.setText(trans.get("MotorCfg.checkbox.compmotormount"));
		this.add(check, "wrap");
		
		
		panel = new JPanel(new MigLayout("fill"));
		this.add(panel, "grow, wrap");
		
		
		// Motor configuration selector
		//// Motor configuration:
		panel.add(new JLabel(trans.get("MotorCfg.lbl.Flightcfg")), "shrink");
		
		JComboBox combo = new JComboBox(new FlightConfigurationModel(configuration));
		panel.add(combo, "growx");
		combo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateFields();
			}
			
		});
		
		//// New button
		JButton button = new JButton(trans.get("MotorCfg.but.New"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String id = rocket.newFlightConfigurationID();
				configuration.setFlightConfigurationID(id);
			}
		});
		panel.add(button, "");
		
		//// Edit button
		button = new JButton(trans.get("MotorCfg.but.FlightcfgEdit"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JDialog configDialog = new FlightConfigurationDialog(rocket, SwingUtilities.windowForComponent(MotorConfig.this));
				configDialog.show();
			}
		});
		panel.add(button, "wrap unrel");
		
		
		// Current motor:
		panel.add(new JLabel(trans.get("MotorCfg.lbl.Currentmotor")), "shrink");
		
		motorLabel = new JLabel();
		motorLabel.setFont(motorLabel.getFont().deriveFont(Font.BOLD));
		updateFields();
		panel.add(motorLabel, "wrap unrel");
		
		
		
		//  Overhang
		//// Motor overhang:
		panel.add(new JLabel(trans.get("MotorCfg.lbl.Motoroverhang")));
		
		DoubleModel dm = new DoubleModel(motorMount, "MotorOverhang", UnitGroup.UNITS_LENGTH);
		
		JSpinner spin = new JSpinner(dm.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "span, split, width :65lp:");
		
		panel.add(new UnitSelector(dm), "width :30lp:");
		panel.add(new BasicSlider(dm.getSliderModel(-0.02, 0.06)), "w 100lp, wrap unrel");
		
		
		
		// Select ignition event
		//// Ignition at:
		panel.add(new JLabel(trans.get("MotorCfg.lbl.Ignitionat") + " " + CommonStrings.dagger), "");
		
		IgnitionConfiguration ignitionConfig = mount.getIgnitionConfiguration().getDefault();
		combo = new JComboBox(new EnumModel<IgnitionConfiguration.IgnitionEvent>(ignitionConfig, "IgnitionEvent"));
		panel.add(combo, "growx, wrap");
		
		// ... and delay
		//// plus
		panel.add(new JLabel(trans.get("MotorCfg.lbl.plus")), "gap indent, skip 1, span, split");
		
		dm = new DoubleModel(ignitionConfig, "IgnitionDelay", 0);
		spin = new JSpinner(dm.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin, 3));
		panel.add(spin, "gap rel rel");
		
		//// seconds
		panel.add(new JLabel(trans.get("MotorCfg.lbl.seconds")), "wrap unrel");
		
		panel.add(new StyledLabel(CommonStrings.override_description, -1), "spanx, wrap para");
		
		
		// Check stage count
		RocketComponent c = (RocketComponent) mount;
		c = c.getRocket();
		int stages = c.getChildCount();
		
		if (stages == 1) {
			//// The current design has only one stage.
			//// Stages can be added by clicking \"New stage\".
			
			panel.add(new StyledLabel(trans.get("MotorCfg.lbl.longA1") + " " +
					trans.get("MotorCfg.lbl.longA2"), -1),
					"spanx, wrap para");
		} else {
			//// The current design has 
			//// stages.
			panel.add(new StyledLabel(trans.get("MotorCfg.lbl.longB1") + " " + stages + " " +
					trans.get("MotorCfg.lbl.longB2"), -1),
					"spanx, wrap para");
		}
		
		
		// Select etc. buttons
		//// Select motor
		button = new JButton(trans.get("MotorCfg.but.Selectmotor"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String id = configuration.getFlightConfigurationID();
				
				MotorChooserDialog dialog = new MotorChooserDialog(mount.getMotor(id),
						mount.getMotorDelay(id), mount.getMotorMountDiameter(),
						SwingUtilities.getWindowAncestor(MotorConfig.this));
				dialog.setVisible(true);
				Motor m = dialog.getSelectedMotor();
				double d = dialog.getSelectedDelay();
				
				if (m != null) {
					if (id == null) {
						id = rocket.newFlightConfigurationID();
						configuration.setFlightConfigurationID(id);
					}
					MotorConfiguration config = new MotorConfiguration();
					config.setMotor(m);
					config.setEjectionDelay(d);
					mount.getMotorConfiguration().set(id, config);
				}
				updateFields();
			}
		});
		panel.add(button, "span, split, growx");
		
		//// Remove motor
		button = new JButton(trans.get("MotorCfg.but.Removemotor"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mount.getMotorConfiguration().resetDefault(configuration.getFlightConfigurationID());
				updateFields();
			}
		});
		panel.add(button, "growx, wrap");
		
		
		
		
		
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
		String id = configuration.getFlightConfigurationID();
		Motor m = mount.getMotor(id);
		if (m == null) {
			//// None
			motorLabel.setText(trans.get("MotorCfg.lbl.motorLabel"));
		} else {
			String str = "";
			if (m instanceof ThrustCurveMotor)
				str = ((ThrustCurveMotor) m).getManufacturer() + " ";
			str += m.getDesignation(mount.getMotorDelay(id));
			motorLabel.setText(str);
		}
	}
	
	
	private static void setDeepEnabled(Component component, boolean enabled) {
		component.setEnabled(enabled);
		if (component instanceof Container) {
			for (Component c : ((Container) component).getComponents()) {
				setDeepEnabled(c, enabled);
			}
		}
	}
	
}
