package info.openrocket.swing.gui.configdialog;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.RecoveryDevice;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.StageSeparationConfiguration;
import info.openrocket.core.rocketcomponent.StageSeparationConfiguration.SeparationEvent;
import info.openrocket.core.startup.Application;

import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.CustomFocusTraversalPolicy;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.adaptors.EnumModel;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.components.StyledLabel.Style;

public class AxialStageConfig extends ComponentAssemblyConfig {
	private static final long serialVersionUID = -944969957186522471L;
	private static final Translator trans = Application.getTranslator();
	
	public AxialStageConfig(OpenRocketDocument document, RocketComponent component, JDialog parent) {
		super(document, component, parent);
		
		// Stage separation config (for non-first stage)
		if (component.getStageNumber() > 0) {
			JPanel tab = separationTab((AxialStage) component);
			tabbedPane.insertTab(trans.get("ComponentAssemblyConfig.tab.Separation"), null, tab,
					trans.get("ComponentAssemblyConfig.tab.Separation.ttip"), 0);
			tabbedPane.setSelectedIndex(0);
		}

		// Recovery tab — always shown
		JPanel recoveryTab = recoveryTab((AxialStage) component);
		tabbedPane.addTab(trans.get("AxialStageConfig.tab.Recovery"), null, recoveryTab,
				trans.get("AxialStageConfig.tab.Recovery.ttip"));

		// Apply the custom focus travel policy to this config dialog
		//// Make sure the cancel & ok button is the last component
		order.add(cancelButton);
		order.add(okButton);
		CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(order);
		parent.setFocusTraversalPolicy(policy);
	}
	
	
	private JPanel recoveryTab(AxialStage stage) {
		JPanel panel = new JPanel(new MigLayout("fillx"));

		panel.add(new StyledLabel(trans.get("AxialStageConfig.recovery.lbl.title"), Style.BOLD),
				"spanx, gaptop unrel, wrap rel");

		// Detect whether this stage has a recovery device marked as a drogue
		String drogueDeviceName = null;
		for (RocketComponent comp : stage) {
			if (comp instanceof RecoveryDevice rd && rd.isDrogue()) {
				drogueDeviceName = rd.getName();
				break;
			}
		}
		if (drogueDeviceName != null) {
			panel.add(new StyledLabel(trans.get("AxialStageConfig.recovery.lbl.HasDrogue") + " " + drogueDeviceName, -1, Style.ITALIC),
					"spanx, wrap rel");
		} else {
			panel.add(new StyledLabel(trans.get("AxialStageConfig.recovery.lbl.NoDrogue"), -1, Style.ITALIC),
					"spanx, wrap rel");
		}

		JCheckBox droguelessCheck = new JCheckBox(trans.get("AxialStageConfig.recovery.chk.Drogueless"));
		droguelessCheck.setToolTipText(trans.get("AxialStageConfig.recovery.chk.Drogueless.ttip"));
		droguelessCheck.setSelected(stage.isDrogueless());
		droguelessCheck.addActionListener(e -> {
			if (droguelessCheck.isSelected()) {
				// Conflict: stage has a recovery device marked as drogue
				for (RocketComponent comp : stage) {
					if (comp instanceof RecoveryDevice rd && rd.isDrogue()) {
						JOptionPane.showMessageDialog(
								SwingUtilities.getWindowAncestor(panel),
								String.format(trans.get("AxialStageConfig.recovery.dlg.HasDrogueDevice.msg"), stage.getName(), rd.getName()),
								trans.get("AxialStageConfig.recovery.dlg.HasDrogueDevice.title"),
								JOptionPane.ERROR_MESSAGE);
						droguelessCheck.setSelected(false);
						return;
					}
				}
			}
			stage.setDrogueless(droguelessCheck.isSelected());
		});
		panel.add(droguelessCheck, "spanx, wrap");
		order.add(droguelessCheck);

		panel.add(new StyledLabel(trans.get("AxialStageConfig.recovery.lbl.DroguelessNote"), -1, Style.ITALIC),
				"spanx, wrap");

		return panel;
	}

	private JPanel separationTab(AxialStage stage) {
		JPanel panel = new JPanel(new MigLayout());
		
		// Select separation event
		panel.add(new StyledLabel(trans.get("ComponentAssemblyConfig.separation.lbl.title") + " " + CommonStrings.dagger, Style.BOLD),
				"spanx, gaptop unrel, wrap 30lp");

		StageSeparationConfiguration sepConfig = stage.getSeparationConfiguration();

		EnumModel<SeparationEvent> em = new EnumModel<>(sepConfig, "SeparationEvent", SeparationEvent.values());
		register(em);
		JComboBox<SeparationEvent> combo = new JComboBox<>(em);
		
		//combo.setSelectedItem(sepConfig);
		panel.add(combo);
		order.add(combo);
		
		// ... and delay
		panel.add(new JLabel(trans.get("ComponentAssemblyConfig.separation.lbl.plus")));
		
		DoubleModel dm = new DoubleModel( sepConfig, "SeparationDelay", 0);
		register(dm);
		JSpinner spin = new JSpinner(dm.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "width 65lp");
		order.add(((SpinnerEditor)spin.getEditor()).getTextField());
		
		//// seconds
		panel.add(new JLabel(trans.get("ComponentAssemblyConfig.separation.lbl.seconds")), "wrap unrel");
		
		panel.add(new StyledLabel(CommonStrings.override_description, -1), "spanx, pushy, wrap para");

		return panel;
	}

	
}
