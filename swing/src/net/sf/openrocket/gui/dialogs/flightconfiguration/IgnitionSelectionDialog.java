package net.sf.openrocket.gui.dialogs.flightconfiguration;

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

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
import net.sf.openrocket.motor.IgnitionEvent;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

public class IgnitionSelectionDialog extends JDialog {
	private static final long serialVersionUID = -3399966098520607837L;

	private static final Translator trans = Application.getTranslator();
	
	private RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);
	
	private MotorMount curMount;
	private MotorConfiguration curMotorInstance;
	
	private IgnitionEvent startIgnitionEvent;
	private double startIgnitionDelay;
	
	public IgnitionSelectionDialog(Window parent, final FlightConfigurationId curFCID, MotorMount _mount) {
		super(parent, trans.get("edtmotorconfdlg.title.Selectignitionconf"), Dialog.ModalityType.APPLICATION_MODAL);
		curMount = _mount;
		curMotorInstance = curMount.getMotorConfig(curFCID);
	    startIgnitionEvent = curMotorInstance.getIgnitionEvent();
	    startIgnitionDelay =  curMotorInstance.getIgnitionDelay();
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		// Edit default or override option
		boolean isDefault = curMotorInstance.isEmpty();
		panel.add(new JLabel(trans.get("IgnitionSelectionDialog.opt.title")), "span, wrap rel");
		final JRadioButton defaultButton = new JRadioButton(trans.get("IgnitionSelectionDialog.opt.default"), isDefault);
		panel.add(defaultButton, "span, gapleft para, wrap rel");
		String str = trans.get("IgnitionSelectionDialog.opt.override");
		Rocket rkt = ((RocketComponent)_mount).getRocket();
		str = str.replace("{0}", descriptor.format(rkt, curFCID));
		
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
		panel.add(new JLabel(trans.get("MotorCfg.lbl.Ignitionat")), "");
		final EnumModel<IgnitionEvent> igEvModel = new EnumModel<IgnitionEvent>(curMotorInstance, "IgnitionEvent", IgnitionEvent.values());
		final JComboBox<IgnitionEvent> eventBox = new JComboBox<IgnitionEvent>( igEvModel);
		panel.add(eventBox, "growx, wrap");
		
		// ... and delay 
		//// plus
		panel.add(new JLabel(trans.get("MotorCfg.lbl.plus")), "gap indent, skip 1, span, split");
		
		DoubleModel delay = new DoubleModel(curMotorInstance, "IgnitionDelay", UnitGroup.UNITS_SHORT_TIME, 0);
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
					// retrieve our just-set values
					double cid = curMotorInstance.getIgnitionDelay();
					IgnitionEvent cie = curMotorInstance.getIgnitionEvent();
					
					// update the default instance
					final MotorConfiguration defaultMotorInstance = curMount.getDefaultMotorConfig();
					defaultMotorInstance.setIgnitionDelay( cid);
					defaultMotorInstance.setIgnitionEvent( cie);
					
					// and change all remaining configs
					// this seems like odd behavior to me, but it matches the text on the UI dialog popup. -teyrana (equipoise@gmail.com) 
					Iterator<MotorConfiguration> iter = curMount.getMotorIterator();
					while( iter.hasNext() ){
						MotorConfiguration next = iter.next();
						next.setIgnitionDelay( cid);
						next.setIgnitionEvent( cie);
					}
					
//					System.err.println("setting default motor ignition ("+defaultMotorInstance.getMotorID().toString()+") to: ");
//					System.err.println("    event: "+defaultMotorInstance.getIgnitionEvent().name+" w/delay: "+defaultMotorInstance.getIgnitionDelay());
//				}else {
//					System.err.println("setting motor ignition to.... new values: ");
//					//destMotorInstance.setIgnitionEvent((IgnitionEvent)eventBox.getSelectedItem());
//					System.err.println("    "+curMotorInstance.getIgnitionEvent()+" w/ "+curMotorInstance.getIgnitionDelay());
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
				curMotorInstance.setIgnitionEvent( startIgnitionEvent );
				curMotorInstance.setIgnitionDelay( startIgnitionDelay ); 
			}
		});
		
		panel.add(cancel, "sizegroup btn");
		
		this.setContentPane(panel);
		
		GUIUtil.setDisposableDialogOptions(this, okButton);
	}
}
