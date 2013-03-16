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
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration.SeparationEvent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

public class SeparationSelectionDialog extends JDialog {
	
	private static final Translator trans = Application.getTranslator();
	
	private StageSeparationConfiguration newConfiguration;
	
	SeparationSelectionDialog(JDialog parent, final Rocket rocket, final Stage component) {
		super(parent, trans.get("edtmotorconfdlg.title.Selectseparationconf"), Dialog.ModalityType.APPLICATION_MODAL);
		final String id = rocket.getDefaultConfiguration().getFlightConfigurationID();
		
		newConfiguration = component.getStageSeparationConfiguration().get(id).clone();
		
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		// FIXME: Edit Default or override option
		
		// Select separation event
		panel.add(new JLabel(trans.get("SeparationSelectionDialog.lbl.separation")), "");
		
		final JComboBox event = new JComboBox(new EnumModel<SeparationEvent>(newConfiguration, "SeparationEvent"));
		event.setSelectedItem(newConfiguration.getSeparationEvent());
		panel.add(event, "wrap rel");
		
		// ... and delay
		panel.add(new JLabel(trans.get("StageConfig.separation.lbl.plus")), "alignx 100%");
		
		final DoubleModel delay = new DoubleModel(newConfiguration, "SeparationDelay", UnitGroup.UNITS_SHORT_TIME, 0);
		JSpinner spin = new JSpinner(delay.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin, 3));
		panel.add(spin, "span, split");
		
		//// seconds
		panel.add(new JLabel(trans.get("StageConfig.separation.lbl.seconds")), "wrap para");
		
		
		panel.add(new JPanel(), "span, split, growx");
		
		JButton okButton = new JButton(trans.get("button.ok"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				component.getStageSeparationConfiguration().set(id, newConfiguration);
				SeparationSelectionDialog.this.setVisible(false);
			}
		});
		
		panel.add(okButton, "sizegroup btn");
		
		JButton cancel = new JButton(trans.get("button.cancel"));
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SeparationSelectionDialog.this.setVisible(false);
			}
		});
		
		panel.add(cancel, "sizegroup btn");
		
		this.setContentPane(panel);
		
		GUIUtil.setDisposableDialogOptions(this, okButton);
	}
	
}
