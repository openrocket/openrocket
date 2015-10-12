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
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.motor.MotorInstance;
import net.sf.openrocket.rocketcomponent.FlightConfigurationID;
import net.sf.openrocket.rocketcomponent.IgnitionEvent;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

public class IgnitionSelectionDialog extends JDialog {
	private static final long serialVersionUID = -3399966098520607837L;

	private static final Translator trans = Application.getTranslator();
	
	//private RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);
	
	private MotorMount curMount;
	private MotorInstance destMotorInstance;
	
	private IgnitionEvent startIgnEvent;
	private double ignitionDelay;
	
	public IgnitionSelectionDialog(Window parent, final FlightConfigurationID curFCID, MotorMount _mount) {
		super(parent, trans.get("edtmotorconfdlg.title.Selectignitionconf"), Dialog.ModalityType.APPLICATION_MODAL);
		curMount = _mount;
		destMotorInstance = curMount.getMotorInstance(curFCID);
		startIgnEvent = destMotorInstance.getIgnitionEvent();
		ignitionDelay = destMotorInstance.getIgnitionDelay();
		final MotorInstance defaultMotorInstance = curMount.getDefaultMotorInstance();
				
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		// Edit default or override option
		boolean isDefault = curMount.isDefaultMotorInstance( destMotorInstance );
		panel.add(new JLabel(trans.get("IgnitionSelectionDialog.opt.title")), "span, wrap rel");
		final JRadioButton defaultButton = new JRadioButton(trans.get("IgnitionSelectionDialog.opt.default"), isDefault);
		panel.add(defaultButton, "span, gapleft para, wrap rel");
		String str = trans.get("IgnitionSelectionDialog.opt.override");
		//str = str.replace("{0}", descriptor.format(rocket, id));
		final JRadioButton overrideButton = new JRadioButton(str, !isDefault);
		panel.add(overrideButton, "span, gapleft para, wrap para");
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(defaultButton);
		buttonGroup.add(overrideButton);
		
		// Select the button based on current configuration.  If the configuration is overridden
		// The the overrideButton is selected.
		boolean isOverridden = !isDefault;
		if (isOverridden) {
			overrideButton.setSelected(true);
		}
		
		// Select ignition event
		//// Ignition at:
		panel.add(new JLabel(trans.get("MotorCfg.lbl.Ignitionat")), "");
		
		final JComboBox<IgnitionEvent> eventBox = new JComboBox<IgnitionEvent>(IgnitionEvent.events);
		//eventBox.setTit
		panel.add(eventBox, "growx, wrap");
		
		// ... and delay
		//// plus
		panel.add(new JLabel(trans.get("MotorCfg.lbl.plus")), "gap indent, skip 1, span, split");
		
		DoubleModel delay = new DoubleModel(destMotorInstance, "IgnitionDelay", UnitGroup.UNITS_SHORT_TIME, 0);
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
					System.err.println("setting motor ignition to.... default values");
					
					destMotorInstance.setIgnitionDelay( defaultMotorInstance.getIgnitionDelay());
					destMotorInstance.setIgnitionEvent( defaultMotorInstance.getIgnitionEvent());
				} else {
					System.err.println("setting motor ignition to.... new values: ");
					System.err.println("    "+destMotorInstance.getIgnitionEvent()+" w/ "+destMotorInstance.getIgnitionDelay());
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
				// if cancelled, reset to starting values
				destMotorInstance.setIgnitionEvent( startIgnEvent );
				destMotorInstance.setIgnitionDelay( ignitionDelay ); 
			}
		});
		
		panel.add(cancel, "sizegroup btn");
		
		this.setContentPane(panel);
		
		GUIUtil.setDisposableDialogOptions(this, okButton);
	}
}
