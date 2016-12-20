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
import javax.swing.JSlider;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.formatting.RocketDescriptor;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration.DeployEvent;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

@SuppressWarnings("serial")
public class DeploymentSelectionDialog extends JDialog {
	
	private static final Translator trans = Application.getTranslator();
	
	private RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);
	
	private final DeploymentConfiguration newConfiguration;
	
	private final JLabel altText;
	private final JSpinner altSpinner;
	private final UnitSelector altUnit;
	private final JSlider altSlider;
	
	public DeploymentSelectionDialog(Window parent, final Rocket rocket, final RecoveryDevice component) {
		super(parent, trans.get("edtmotorconfdlg.title.Selectdeploymentconf"), Dialog.ModalityType.APPLICATION_MODAL);
		
		final FlightConfigurationId id = rocket.getSelectedConfiguration().getFlightConfigurationID();
		
		newConfiguration = component.getDeploymentConfigurations().get(id).clone();
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		panel.add(new JLabel(trans.get("DeploymentSelectionDialog.opt.title")), "span, wrap rel");
		final JRadioButton defaultButton = new JRadioButton(trans.get("DeploymentSelectionDialog.opt.default"), true);
		panel.add(defaultButton, "span, gapleft para, wrap rel");
		String str = trans.get("DeploymentSelectionDialog.opt.override");
		str = str.replace("{0}", descriptor.format(rocket, id));
		final JRadioButton overrideButton = new JRadioButton(str, false);
		panel.add(overrideButton, "span, gapleft para, wrap para");
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(defaultButton);
		buttonGroup.add(overrideButton);
		
		// Select the button based on current configuration.  If the configuration is overridden
		// The the overrideButton is selected.
		boolean isOverridden = !component.getDeploymentConfigurations().isDefault(id);
		if (isOverridden) {
			overrideButton.setSelected(true);
		}
		
		//// Deployment
		//// Deploys at:
		panel.add(new JLabel(trans.get("ParachuteCfg.lbl.Deploysat")), "");
		
		final JComboBox<DeployEvent> deployEvent = new JComboBox<DeployEvent>(new EnumModel<DeployEvent>(newConfiguration, "DeployEvent"));
		if( (component.getStageNumber() + 1 ) == rocket.getStageCount() ){
			//	This is the bottom stage:  Restrict deployment options.
		    deployEvent.removeItem( DeployEvent.LOWER_STAGE_SEPARATION );
		}
		panel.add( deployEvent, "spanx 3, growx, wrap");
		
		// ... and delay
		//// plus
		panel.add(new JLabel(trans.get("ParachuteCfg.lbl.plusdelay")), "right");
		
		final DoubleModel delay = new DoubleModel(newConfiguration, "DeployDelay", UnitGroup.UNITS_SHORT_TIME, 0);
		final JSpinner delaySpinner = new JSpinner(delay.getSpinnerModel());
		delaySpinner.setEditor(new SpinnerEditor(delaySpinner, 3));
		panel.add(delaySpinner, "spanx, split");
		
		//// seconds
		panel.add(new JLabel(trans.get("ParachuteCfg.lbl.seconds")), "wrap paragraph");
		
		// Altitude:
		altText = new JLabel(trans.get("ParachuteCfg.lbl.Altitude"));
		panel.add(altText);
		
		final DoubleModel alt = new DoubleModel(newConfiguration, "DeployAltitude", UnitGroup.UNITS_DISTANCE, 0);
		
		altSpinner = new JSpinner(alt.getSpinnerModel());
		altSpinner.setEditor(new SpinnerEditor(altSpinner));
		panel.add(altSpinner, "growx");
		altUnit = new UnitSelector(alt);
		panel.add(altUnit, "growx");
		altSlider = new BasicSlider(alt.getSliderModel(100, 1000));
		panel.add(altSlider, "w 100lp, wrap");
		
		deployEvent.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateState();
			}
		});
		updateState();
		
		panel.add(new JPanel(), "span, split, growx");
		
		JButton okButton = new JButton(trans.get("button.ok"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (defaultButton.isSelected()) {
					component.getDeploymentConfigurations().setDefault(newConfiguration);
				} else {
					component.getDeploymentConfigurations().set(id, newConfiguration);
				}
				DeploymentSelectionDialog.this.setVisible(false);
			}
		});
		
		panel.add(okButton, "sizegroup btn");
		
		JButton cancel = new JButton(trans.get("button.cancel"));
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DeploymentSelectionDialog.this.setVisible(false);
			}
		});
		
		panel.add(cancel, "sizegroup btn");
		
		this.setContentPane(panel);
		GUIUtil.setDisposableDialogOptions(this, okButton);
	}
	
	private void updateState() {
		boolean enabled = (newConfiguration.getDeployEvent() == DeployEvent.ALTITUDE);
		altText.setEnabled(enabled);
		altSpinner.setEnabled(enabled);
		altUnit.setEnabled(enabled);
		altSlider.setEnabled(enabled);
	}
	
	
}
