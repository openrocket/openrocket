package info.openrocket.swing.gui.configdialog;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;

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

		// Collect recovery devices in this stage
		List<RecoveryDevice> devices = new ArrayList<>();
		for (RocketComponent comp : stage) {
			if (comp instanceof RecoveryDevice rd) {
				devices.add(rd);
			}
		}

		// Find any currently designated drogue
		RecoveryDevice currentDrogue = null;
		for (RecoveryDevice rd : devices) {
			if (rd.isDrogue()) {
				currentDrogue = rd;
				break;
			}
		}

		// --- Radio buttons ---
		ButtonGroup group = new ButtonGroup();

		JRadioButton noneRadio = new JRadioButton(trans.get("AxialStageConfig.recovery.radio.None"));
		noneRadio.setToolTipText(trans.get("AxialStageConfig.recovery.radio.None.ttip"));
		group.add(noneRadio);

		JRadioButton droguelessRadio = new JRadioButton(trans.get("AxialStageConfig.recovery.chk.Drogueless"));
		droguelessRadio.setToolTipText(trans.get("AxialStageConfig.recovery.chk.Drogueless.ttip"));
		group.add(droguelessRadio);

		JRadioButton selectDrogueRadio = new JRadioButton(trans.get("AxialStageConfig.recovery.radio.SelectDrogue"));
		selectDrogueRadio.setToolTipText(trans.get("AxialStageConfig.recovery.radio.SelectDrogue.ttip"));
		selectDrogueRadio.setEnabled(!devices.isEmpty());
		group.add(selectDrogueRadio);

		// --- Device combo box ---
		@SuppressWarnings("unchecked")
		JComboBox<RecoveryDevice> deviceCombo = new JComboBox<>();
		for (RecoveryDevice rd : devices) {
			deviceCombo.addItem(rd);
		}
		deviceCombo.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof RecoveryDevice rd) {
					setText(rd.getName());
				}
				return this;
			}
		});

		// Set initial radio/combo state
		if (stage.isDrogueless()) {
			droguelessRadio.setSelected(true);
			deviceCombo.setEnabled(false);
		} else if (currentDrogue != null) {
			selectDrogueRadio.setSelected(true);
			deviceCombo.setSelectedItem(currentDrogue);
			deviceCombo.setEnabled(true);
		} else {
			noneRadio.setSelected(true);
			deviceCombo.setEnabled(false);
		}

		if (devices.isEmpty()) {
			deviceCombo.setEnabled(false);
		}

		// --- Radio listeners ---
		noneRadio.addActionListener(e -> {
			clearStageDrogue(stage);
			stage.setDrogueless(false);
			deviceCombo.setEnabled(false);
		});

		droguelessRadio.addActionListener(e -> {
			clearStageDrogue(stage);
			stage.setDrogueless(true);
			deviceCombo.setEnabled(false);
		});

		selectDrogueRadio.addActionListener(e -> {
			stage.setDrogueless(false);
			deviceCombo.setEnabled(true);
			clearStageDrogue(stage);
			RecoveryDevice selected = (RecoveryDevice) deviceCombo.getSelectedItem();
			if (selected != null) {
				selected.setDrogue(true);
			}
		});

		// --- Combo listener ---
		deviceCombo.addActionListener(e -> {
			if (selectDrogueRadio.isSelected()) {
				clearStageDrogue(stage);
				RecoveryDevice selected = (RecoveryDevice) deviceCombo.getSelectedItem();
				if (selected != null) {
					selected.setDrogue(true);
				}
			}
		});

		// --- Layout ---
		panel.add(noneRadio, "spanx, wrap");
		panel.add(new StyledLabel(trans.get("AxialStageConfig.recovery.lbl.DualDeployment"), Style.BOLD),
				"spanx, gaptop unrel, wrap rel");
		panel.add(droguelessRadio, "spanx, wrap");
		panel.add(selectDrogueRadio, "spanx, wrap");
		panel.add(new JLabel(trans.get("AxialStageConfig.recovery.lbl.DrogueDevice")), "gapleft 20lp, split 2");
		panel.add(deviceCombo, "growx, wrap");

		order.add(noneRadio);
		order.add(droguelessRadio);
		order.add(selectDrogueRadio);
		order.add(deviceCombo);

		return panel;
	}

	private static void clearStageDrogue(AxialStage stage) {
		for (RocketComponent comp : stage) {
			if (comp instanceof RecoveryDevice rd && rd.isDrogue()) {
				rd.setDrogue(false);
			}
		}
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
