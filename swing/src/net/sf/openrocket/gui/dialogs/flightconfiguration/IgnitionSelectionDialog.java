package net.sf.openrocket.gui.dialogs.flightconfiguration;

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.formatting.RocketDescriptor;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.IgnitionConfiguration;
import net.sf.openrocket.rocketcomponent.IgnitionConfiguration.IgnitionEvent;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

public class IgnitionSelectionDialog extends JDialog {
	
	private static final Translator trans = Application.getTranslator();
	
	private RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);
	
	
	private IgnitionConfiguration newConfiguration;
	
	public IgnitionSelectionDialog(Window parent, final Rocket rocket, final MotorMount component) {
		super(parent, trans.get("edtmotorconfdlg.title.Selectignitionconf"), Dialog.ModalityType.APPLICATION_MODAL);
		final String id = rocket.getDefaultConfiguration().getFlightConfigurationID();
		
		newConfiguration = component.getIgnitionConfiguration().get(id).clone();
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		// Edit default or override option
		boolean isDefault = component.getIgnitionConfiguration().isDefault(id);
		panel.add(new JLabel(trans.get("IgnitionSelectionDialog.opt.title")), "span, wrap rel");
		final JRadioButton defaultButton = new JRadioButton(trans.get("IgnitionSelectionDialog.opt.default"), isDefault);
		panel.add(defaultButton, "span, gapleft para, wrap rel");
		String str = trans.get("IgnitionSelectionDialog.opt.override");
		str = str.replace("{0}", descriptor.format(rocket, id));
		final JRadioButton overrideButton = new JRadioButton(str, !isDefault);
		panel.add(overrideButton, "span, gapleft para, wrap para");
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(defaultButton);
		buttonGroup.add(overrideButton);
		
		// Select the button based on current configuration.  If the configuration is overridden
		// The the overrideButton is selected.
		boolean isOverridden = !component.getIgnitionConfiguration().isDefault(id);
		if (isOverridden) {
			overrideButton.setSelected(true);
		}
		
		// Select ignition event
		//// Ignition at:
		panel.add(new JLabel(trans.get("MotorCfg.lbl.Ignitionat")), "");
		
		final JComboBox event = new JComboBox(new EnumModel<IgnitionEvent>(newConfiguration, "IgnitionEvent"));
		panel.add(event, "growx, wrap");
		
		// ... and delay
		//// plus
		panel.add(new JLabel(trans.get("MotorCfg.lbl.plus")), "gap indent, skip 1, span, split");
		
		DoubleModel delay = new DoubleModel(newConfiguration, "IgnitionDelay", UnitGroup.UNITS_SHORT_TIME, 0);
		JSpinner spin = new JSpinner(delay.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin, 3));
		panel.add(spin, "gap rel rel");
		
		//// seconds
		panel.add(new JLabel(trans.get("MotorCfg.lbl.seconds")), "wrap unrel");
		
		
		panel.add(new JPanel(), "span, split, growx");
		
		JButton okButton = new JButton(trans.get("button.ok"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (defaultButton.isSelected()) {
					component.getIgnitionConfiguration().setDefault(newConfiguration);
				} else {
					component.getIgnitionConfiguration().set(id, newConfiguration);
				}
				IgnitionSelectionDialog.this.setVisible(false);
			}
		});
		
		panel.add(okButton, "sizegroup btn");
		
		
		JButton cancel = new JButton(trans.get("button.cancel"));
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				IgnitionSelectionDialog.this.setVisible(false);
			}
		});
		
		panel.add(cancel, "sizegroup btn");
		
		this.setContentPane(panel);
		
		GUIUtil.setDisposableDialogOptions(this, okButton);
	}
}
