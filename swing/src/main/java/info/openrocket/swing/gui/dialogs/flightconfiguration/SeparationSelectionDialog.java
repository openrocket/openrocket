package info.openrocket.swing.gui.dialogs.flightconfiguration;

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;

import info.openrocket.core.formatting.RocketDescriptor;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.StageSeparationConfiguration;
import info.openrocket.core.rocketcomponent.StageSeparationConfiguration.SeparationEvent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;

import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.adaptors.EnumModel;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.widgets.SelectColorButton;

@SuppressWarnings("serial")
public class SeparationSelectionDialog extends JDialog {

	private static final Translator trans = Application.getTranslator();
	
	private RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);
	
	private StageSeparationConfiguration newConfiguration;

	private boolean isOverrideDefault;
	
	public SeparationSelectionDialog(Window parent, final Rocket rocket, final AxialStage stage, FlightConfigurationId id) {
		super(parent, trans.get("edtmotorconfdlg.title.Selectseparationconf"), Dialog.ModalityType.APPLICATION_MODAL);

		newConfiguration = stage.getSeparationConfigurations().get(id);
		if( stage.getSeparationConfigurations().isDefault( newConfiguration )){
			newConfiguration = newConfiguration.clone();
		}
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		
		// Select separation event
		panel.add(new JLabel(trans.get("SeparationSelectionDialog.opt.title")), "span, wrap rel");
		
		boolean isDefault = stage.getSeparationConfigurations().isDefault(id);
		final JRadioButton defaultButton = new JRadioButton(trans.get("SeparationSelectionDialog.opt.default"), isDefault);
		panel.add(defaultButton, "span, gapleft para, wrap rel");
		String str = trans.get("SeparationSelectionDialog.opt.override");
		str = str.replace("{0}", descriptor.format(rocket, id));
		final JRadioButton overrideButton = new JRadioButton(str);
		overrideButton.addItemListener(e -> isOverrideDefault = e.getStateChange() == ItemEvent.SELECTED);
		overrideButton.setSelected(false);
		panel.add(overrideButton, "span, gapleft para, wrap para");
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(defaultButton);
		buttonGroup.add(overrideButton);
		
		// Select the button based on current configuration.  If the configuration is overridden
		// The the overrideButton is selected.
		boolean isOverridden = !stage.getSeparationConfigurations().isDefault(id);
		if (isOverridden) {
			overrideButton.setSelected(true);
		}
		
		final JComboBox<SeparationEvent> event = new JComboBox<SeparationEvent>(new EnumModel<SeparationEvent>(newConfiguration, "SeparationEvent"));
		event.setSelectedItem(newConfiguration.getSeparationEvent());
		panel.add(event, "wrap rel");
		
		// ... and delay
		panel.add(new JLabel(trans.get("ComponentAssemblyConfig.separation.lbl.plus")), "alignx 100%");
		
		final DoubleModel delay = new DoubleModel(newConfiguration, "SeparationDelay", UnitGroup.UNITS_SHORT_TIME, 0);
		JSpinner spin = new JSpinner(delay.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin, 3));
		panel.add(spin, "span, split");
		
		//// seconds
		panel.add(new JLabel(trans.get("ComponentAssemblyConfig.separation.lbl.seconds")), "wrap para");
		
		
		panel.add(new JPanel(), "span, split, growx");
		
		JButton okButton = new SelectColorButton(trans.get("button.ok"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( newConfiguration.getSeparationEvent() == StageSeparationConfiguration.SeparationEvent.NEVER ){
					newConfiguration.setSeparationDelay(0);
				}
				if (defaultButton.isSelected()) {
					stage.getSeparationConfigurations().reset();
					stage.getSeparationConfigurations().setDefault( newConfiguration);
				} else {
					stage.getSeparationConfigurations().set(id, newConfiguration);
				}
				SeparationSelectionDialog.this.setVisible(false);
			}
		});
		
		panel.add(okButton, "sizegroup btn");
		
		JButton cancel = new SelectColorButton(trans.get("button.cancel"));
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SeparationSelectionDialog.this.setVisible(false);
			}
		});
		
		panel.add(cancel, "sizegroup btn");
		
		this.setContentPane(panel);
		
		GUIUtil.setDisposableDialogOptions(this, okButton);
		GUIUtil.installEscapeCloseButtonOperation(this, okButton);
	}

	/**
	 * Returns true if this dialog was used to override the default configuration.
	 * @return true if this dialog was used to override the default configuration.
	 */
	public boolean isOverrideDefault() {
		return isOverrideDefault;
	}
}
