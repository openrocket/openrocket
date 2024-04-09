package info.openrocket.swing.gui.configdialog;


import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.BooleanModel;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.adaptors.EnumModel;
import info.openrocket.swing.gui.components.BasicSlider;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.motor.IgnitionEvent;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.Invalidatable;

public class MotorConfig extends JPanel implements Invalidatable, InvalidatingWidget {
	
	private static final long serialVersionUID = -4974509134239867067L;
	private final MotorMount mount;
	private static final Translator trans = Application.getTranslator();
	private final List<Invalidatable> invalidatables = new ArrayList<>();

	public MotorConfig(MotorMount motorMount, List<Component> order) {
		super(new MigLayout("fillx"));
		
		this.mount = motorMount;
		
		BooleanModel model;
		
		model = new BooleanModel(motorMount, "MotorMount");
		register(model);
		JCheckBox check = new JCheckBox(model);
		////This component is a motor mount
		check.setText(trans.get("MotorCfg.checkbox.compmotormount"));
		this.add(check, "wrap");
		order.add(check);
		
		final JPanel panel = new JPanel(new MigLayout("fill"));
		this.add(panel, "grow, wrap");
		
		
		//  Overhang
		//// Motor overhang:
		panel.add(new JLabel(trans.get("MotorCfg.lbl.Motoroverhang")));
		
		DoubleModel dm = new DoubleModel(motorMount, "MotorOverhang", UnitGroup.UNITS_LENGTH);
		register(dm);

		JSpinner spin = new JSpinner(dm.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "span, split, width :65lp:");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		panel.add(new UnitSelector(dm), "width :30lp:");
		panel.add(new BasicSlider(dm.getSliderModel(-0.02, 0.06)), "w 100lp, wrap unrel");
		
		
		// Select ignition event
		//// Ignition at:
		panel.add(new JLabel(trans.get("MotorCfg.lbl.Ignitionat") + " " + CommonStrings.dagger), "");
		
		MotorConfiguration motorInstance = mount.getDefaultMotorConfig();
		
		final EnumModel<IgnitionEvent> igEvModel = new EnumModel<IgnitionEvent>(motorInstance, "IgnitionEvent", IgnitionEvent.values());
		register(igEvModel);
		final JComboBox<IgnitionEvent> eventBox = new JComboBox<IgnitionEvent>( igEvModel);
		panel.add(eventBox , "wrap");
		order.add(eventBox);
		
		// ... and delay
		//// plus
		panel.add(new JLabel(trans.get("MotorCfg.lbl.plus")), "gap indent, skip 1, span, split");
		
		dm = new DoubleModel(motorInstance, "IgnitionDelay", 0);
		register(dm);
		spin = new JSpinner(dm.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin, 3));
		panel.add(spin, "gap rel rel");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
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

	@Override
	public void register(Invalidatable model) {
		this.invalidatables.add(model);
	}

	@Override
	public void invalidateMe() {
		super.invalidate();
		for (Invalidatable i : invalidatables) {
			i.invalidateMe();
		}
	}

}
