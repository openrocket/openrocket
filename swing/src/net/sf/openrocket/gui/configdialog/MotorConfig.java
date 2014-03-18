package net.sf.openrocket.gui.configdialog;


import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import javax.swing.BorderFactory;
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
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.IgnitionConfiguration;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

public class MotorConfig extends JPanel {
	
	private final MotorMount mount;
	private static final Translator trans = Application.getTranslator();
	
	public MotorConfig(MotorMount motorMount) {
		super(new MigLayout("fill"));
		
		this.mount = motorMount;
		
		BooleanModel model;
		
		model = new BooleanModel(motorMount, "MotorMount");
		JCheckBox check = new JCheckBox(model);
		////This component is a motor mount
		check.setText(trans.get("MotorCfg.checkbox.compmotormount"));
		this.add(check, "wrap");
		
		final JPanel panel = new JPanel(new MigLayout("fill"));
		this.add(panel, "grow, wrap");
		
		
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
		JComboBox combo = new JComboBox(new EnumModel<IgnitionConfiguration.IgnitionEvent>(ignitionConfig, "IgnitionEvent"));
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
		
		
		// Set enabled status
		
		setDeepEnabled(panel, motorMount.isMotorMount());
		check.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				setDeepEnabled(panel, mount.isMotorMount());
			}
		});
		
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
